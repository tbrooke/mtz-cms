(ns mtz-cms.alfresco.client
  "Alfresco REST API client for Mount Zion CMS"
  (:require 
   [clj-http.client :as http]
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

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
  "Get a specific node by ID"
  [ctx node-id]
  (make-request ctx :get (str "/nodes/" node-id)))

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
                                :as :string})]

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
         :company-home (get-in result [:data :entry :name])})
      (do
        (log/error "❌ Alfresco connection failed:" (:error result))
        {:success false 
         :message (str "Connection failed: " (:error result))}))))

(comment
  ;; Test connection
  (test-connection {})
  
  ;; Get root node
  (get-root-node {})
  
  ;; Get node children
  (get-node-children {} "some-node-id"))