(ns mtz-cms.alfresco.client
  "Alfresco REST API client for Mount Zion CMS with Malli validation"
  (:require
   [clj-http.client :as http]
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [mtz-cms.validation.schemas :as schemas]))

;; --- CONFIGURATION ---

(def default-config
  {:base-url "http://localhost:8080"
   :username "admin"
   :password "admin"})

(defn get-config [ctx]
  (merge default-config 
         (select-keys ctx [:alfresco/base-url :alfresco/username :alfresco/password])))

;; --- HTTP CLIENT ---

(defn make-request
  "Make authenticated HTTP request to Alfresco API"
  [ctx method path & [options]]
  (let [{:keys [base-url username password]} (get-config ctx)
        api-base (str base-url "/alfresco/api/-default-/public/alfresco/versions/1")
        full-url (str api-base path)]

    (log/debug "Alfresco API request:" method full-url)

    (try
      (let [response (http/request 
                      (merge {:method method
                              :url full-url
                              :basic-auth [username password]
                              :accept :json
                              :content-type :json
                              :throw-exceptions false}
                             options))]

        (if (< (:status response) 400)
          {:success true
           :status (:status response)
           :data (when (:body response)
                   (json/read-str (:body response) :key-fn keyword))}
          {:success false
           :status (:status response)
           :error (:body response)}))

      (catch Exception e
        (log/error "Alfresco API request failed:" (.getMessage e))
        {:success false
         :error (.getMessage e)}))))

;; --- NODE OPERATIONS ---

(defn get-root-node
  "Get the root Company Home node"
  [ctx]
  (make-request ctx :get "/nodes/-root-"))

(defn get-node
  "Get a specific node by ID with validation

   Returns validated node data or logs validation warnings."
  [ctx node-id]
  (let [response (make-request ctx :get (str "/nodes/" node-id))]
    (when (:success response)
      ;; Validate the node data structure
      (let [node-data (get-in response [:data :entry])
            validation (schemas/validate :alfresco/node node-data)]
        (when-not (:valid? validation)
          (log/warn "Alfresco node validation failed for" node-id
                   ":" (:errors validation)))))
    response))

(defn get-node-children
  "Get children of a node with optional filtering"
  [ctx node-id & [options]]
  (let [query-params (when options
                       (str "?" (str/join "&"
                                          (for [[k v] options]
                                            (str (name k) "=" (java.net.URLEncoder/encode (str v) "UTF-8"))))))]
    (make-request ctx :get (str "/nodes/" node-id "/children" (or query-params "")))))

(defn get-folders
  "Get all folders under a node"
  [ctx node-id]
  (get-node-children ctx node-id {:where "(isFolder=true)"}))

(defn get-files
  "Get all files under a node"
  [ctx node-id]
  (get-node-children ctx node-id {:where "(isFile=true)"}))

(defn get-node-content
  "Download content of a file node"
  [ctx node-id]
  (let [{:keys [base-url username password]} (get-config ctx)
        api-base (str base-url "/alfresco/api/-default-/public/alfresco/versions/1")
        full-url (str api-base "/nodes/" node-id "/content")]

    (log/debug "Fetching content for node:" node-id)

    (try
      (let [response (http/get full-url
                               {:basic-auth [username password]
                                :throw-exceptions false
                                :as :byte-array})]

        (if (< (:status response) 400)
          {:success true
           :status (:status response)
           :data (:body response)}
          {:success false
           :status (:status response)
           :error (:body response)}))

      (catch Exception e
        (log/error "Failed to fetch node content:" (.getMessage e))
        {:success false
         :error (.getMessage e)}))))

(defn get-pdf-thumbnail
  "Get thumbnail rendition for a PDF node

   Tries common Alfresco rendition names for PDF thumbnails.
   Falls back to default SVG if no rendition available."
  [ctx node-id]
  (let [{:keys [base-url username password]} (get-config ctx)
        api-base (str base-url "/alfresco/api/-default-/public/alfresco/versions/1")
        rendition-names ["imgpreview" "doclib" "medium" "pdf"]]

    (log/debug "Fetching PDF thumbnail for node:" node-id)

    (loop [names rendition-names]
      (if (empty? names)
        ;; No rendition worked, return failure
        (do
          (log/warn "No PDF rendition available for node:" node-id)
          {:success false
           :error "No rendition available"})

        ;; Try this rendition name
        (let [rendition-name (first names)
              full-url (str api-base "/nodes/" node-id "/renditions/" rendition-name "/content")
              response (try
                        (http/get full-url
                                  {:basic-auth [username password]
                                   :throw-exceptions false
                                   :as :byte-array})
                        (catch Exception e
                          (log/debug "Rendition" rendition-name "failed, trying next...")
                          nil))]

          (if (and response (< (:status response) 400))
            (do
              (log/info "✅ Found PDF thumbnail using rendition:" rendition-name)
              {:success true
               :status (:status response)
               :data (:body response)})
            ;; This rendition didn't work, try next
            (recur (rest names))))))))

;; --- CONTENT EXTRACTION ---

(defn extract-node-metadata
  "Extract useful metadata from a node entry"
  [node-entry]
  (let [entry (:entry node-entry)]
    {:id (:id entry)
     :name (:name entry)
     :type (if (:isFolder entry) "folder" "file")
     :mime-type (:mimeType entry)
     :size (:sizeInBytes entry)
     :created-at (:createdAt entry)
     :modified-at (:modifiedAt entry)
     :created-by (get-in entry [:createdByUser :displayName])
     :modified-by (get-in entry [:modifiedByUser :displayName])
     :path (:path entry)
     :parent-id (:parentId entry)
     :aspect-names (:aspectNames entry)
     :properties (:properties entry)}))

;; --- SEARCH FUNCTIONS ---

(defn search-nodes
  "Search for nodes using AFTS query via Search API

   Uses the Search API v1 endpoint:
   POST /search/versions/1/search

   Example:
     (search-nodes ctx \"web:pagetype:'Static'\")"
  [ctx query-string & [options]]
  (let [{:keys [base-url username password]} (get-config ctx)
        search-url (str base-url "/alfresco/api/-default-/public/search/versions/1/search")
        body {:query {:query query-string
                     :language "afts"}
              :include ["properties" "aspectNames"]  ; Include properties in results
              :paging {:maxItems (get options :maxItems 100)
                      :skipCount (get options :skipCount 0)}}]

    (log/debug "Alfresco Search API request:" query-string)

    (try
      (let [response (http/post search-url
                               {:basic-auth [username password]
                                :content-type :json
                                :accept :json
                                :body (json/write-str body)
                                :throw-exceptions false})]

        (if (< (:status response) 400)
          {:success true
           :status (:status response)
           :data (json/read-str (:body response) :key-fn keyword)}
          {:success false
           :status (:status response)
           :error (:body response)}))

      (catch Exception e
        (log/error "Search API request failed:" (.getMessage e))
        {:success false
         :error (.getMessage e)}))))

(defn find-nodes-by-name
  "Find nodes by name pattern"
  [ctx name-pattern]
  (search-nodes ctx (str "cm:name:\"" name-pattern "\"")))

(defn find-nodes-by-path
  "Find nodes by path"
  [ctx path-pattern]
  (search-nodes ctx (str "PATH:\"" path-pattern "\"")))

;; --- SITE FUNCTIONS ---

(defn get-sites
  "Get all sites"
  [ctx]
  (make-request ctx :get "/sites"))

(defn get-site
  "Get a specific site"
  [ctx site-id]
  (make-request ctx :get (str "/sites/" site-id)))

(defn get-site-containers
  "Get containers (document library, etc.) for a site"
  [ctx site-id]
  (make-request ctx :get (str "/sites/" site-id "/containers")))

;; --- DISCOVERY FUNCTIONS ---

(defn discover-content-structure
  "Discover the content structure starting from root"
  [ctx & [max-depth]]
  (letfn [(explore-node [node-id depth]
            (when (< depth (or max-depth 3))
              (let [node-result (get-node ctx node-id)
                    children-result (get-node-children ctx node-id)]
                (when (and (:success node-result) (:success children-result))
                  (let [node-data (get-in node-result [:data :entry])
                        children (get-in children-result [:data :list :entries])]
                    {:node (extract-node-metadata {:entry node-data})
                     :children (map #(explore-node (get-in % [:entry :id]) (inc depth))
                                   (take 10 children))})))))]
    (let [root-result (get-root-node ctx)]
      (when (:success root-result)
        (explore-node (get-in root-result [:data :entry :id]) 0)))))

;; --- HIGH-LEVEL FUNCTIONS ---

(defn get-page-content
  "Get content for a specific page (placeholder - needs node mapping)"
  [ctx page-key]
  (log/info "Getting content for page:" page-key)
  ;; TODO: Implement page-to-node-id mapping
  {:success false :error "Page mapping not implemented yet"})

(defn test-connection
  "Test connection to Alfresco"
  [ctx]
  (let [result (get-root-node ctx)]
    (if (:success result)
      (do
        (log/info "✅ Alfresco connection successful")
        {:success true 
         :message "Connected to Alfresco successfully!"
         :company-home (get-in result [:data :entry :name])
         :root-id (get-in result [:data :entry :id])})
      (do
        (log/error "❌ Alfresco connection failed:" (:error result))
        {:success false 
         :message (str "Connection failed: " (:error result))}))))

(defn explore-structure
  "Explore and display Alfresco structure for setup"
  [ctx]
  (log/info "🔍 Exploring Alfresco structure...")
  (let [root-result (get-root-node ctx)]
    (if (:success root-result)
      (let [root-id (get-in root-result [:data :entry :id])
            sites-result (get-sites ctx)
            children-result (get-node-children ctx root-id)]
        {:success true
         :root-node (get-in root-result [:data :entry])
         :sites (when (:success sites-result) 
                  (get-in sites-result [:data :list :entries]))
         :root-children (when (:success children-result)
                         (get-in children-result [:data :list :entries]))})
      {:success false 
       :error (:error root-result)})))

(comment
  ;; Test connection
  (test-connection {})
  
  ;; Get root node
  (get-root-node {})
  
  ;; Get node children
  (get-node-children {} "some-node-id"))