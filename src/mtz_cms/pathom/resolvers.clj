(ns mtz-cms.pathom.resolvers
  "Pathom resolvers for Mount Zion CMS"
  (:require
   [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.alfresco.resolvers :as components]
   [mtz-cms.alfresco.blog-resolvers :as blog]
   [mtz-cms.alfresco.sunday-worship-resolvers :as worship]
   [mtz-cms.config.core :as config]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- RESOLVERS ---

;; Simple test resolver
(defresolver hello-content
  "Simple test resolver"
  [ctx {:test/keys [name]}]
  {::pco/input [:test/name]
   ::pco/output [:test/greeting]}
  {:test/greeting (str "Hello " name " from Mount Zion CMS!")})

;; Alfresco node resolver
(defresolver alfresco-node
  "Fetch a node from Alfresco by ID"
  [ctx {:alfresco/keys [node-id]}]
  {::pco/input [:alfresco/node-id]
   ::pco/output [:alfresco/node]}
  (let [result (alfresco/get-node ctx node-id)]
    (if (:success result)
      {:alfresco/node (:data result)}
      (do
        (log/error "Failed to fetch node" node-id (:error result))
        {:alfresco/node nil}))))

;; Alfresco node children resolver
(defresolver alfresco-node-children
  "Fetch children of an Alfresco node"
  [ctx {:alfresco/keys [node-id]}]
  {::pco/input [:alfresco/node-id]
   ::pco/output [:alfresco/children]}
  (let [result (alfresco/get-node-children ctx node-id)]
    (if (:success result)
      {:alfresco/children (get-in result [:data :list :entries])}
      (do
        (log/error "Failed to fetch node children" node-id (:error result))
        {:alfresco/children []}))))

;; Alfresco content resolver
(defresolver alfresco-content
  "Fetch content from Alfresco"
  [ctx {:alfresco/keys [node-id]}]
  {::pco/input [:alfresco/node-id]
   ::pco/output [:alfresco/content]}
  (let [result (alfresco/get-node-content ctx node-id)]
    (if (:success result)
      {:alfresco/content (:data result)}
      (do
        (log/error "Failed to fetch content for node" node-id (:error result))
        {:alfresco/content ""}))))

;; Page content resolver - this connects page keys to actual Alfresco nodes
(defresolver page-content
  "Get page content by page key"
  [ctx {:page/keys [key]}]
  {::pco/input [:page/key]
   ::pco/output [:page/title :page/content :page/node-id :page/folder-info]}
  (if-let [node-id (config/get-node-id key)]
    (let [node-result (alfresco/get-node ctx node-id)]
      (if (:success node-result)
        (let [node-data (get-in node-result [:data :entry])
              is-folder (:isFolder node-data)
              title (or (get-in node-data [:properties :cm:title])
                       (:name node-data)
                       (str "Mount Zion UCC - " (name key)))
              
              ;; If it's a folder, get children; if it's a file, get content
              content-info (if is-folder
                            (let [children-result (alfresco/get-node-children ctx node-id)]
                              (if (:success children-result)
                                (let [children (get-in children-result [:data :list :entries])
                                      files (filter #(get-in % [:entry :isFile]) children)
                                      folders (filter #(get-in % [:entry :isFolder]) children)]
                                  {:type "folder"
                                   :files-count (count files)
                                   :folders-count (count folders)
                                   :children children
                                   :content (if (empty? children)
                                            (str "This " (name key) " section is ready for content.")
                                            (str "Found " (count files) " files and " (count folders) " folders in " (name key) " section."))})
                                {:type "folder" :content "Unable to access folder contents."}))
                            ;; It's a file, try to get content
                            (let [content-result (alfresco/get-node-content ctx node-id)]
                              (if (:success content-result)
                                {:type "file" :content (:data content-result)}
                                {:type "file" :content "Unable to read file content."})))]
          
          {:page/title title
           :page/content (:content content-info)
           :page/node-id node-id
           :page/folder-info content-info})
        
        (do
          (log/warn "Failed to fetch node for page" key "node-id" node-id)
          {:page/title (str "Mount Zion UCC - " (name key))
           :page/content (str "Unable to load content for " (name key) " page.")
           :page/node-id node-id
           :page/folder-info {:type "error"}})))
    
    (do
      (log/warn "No node mapping found for page" key)
      {:page/title (str "Mount Zion UCC - " (name key))
       :page/content (str "Content for " (name key) " page. No node mapping configured.")
       :page/node-id nil
       :page/folder-info {:type "unmapped"}})))

;; Calendar events resolver
(defresolver calendar-events
  "Get calendar events from Alfresco"
  [ctx input]
  {::pco/output [:calendar/events]}
  (if-let [calendar-node-id (config/get-node-id :calendar)]
    (let [children-result (alfresco/get-node-children ctx calendar-node-id)]
      (if (:success children-result)
        (let [events (get-in children-result [:data :list :entries])
              calendar-events (filter #(= "ia:calendarEvent" (get-in % [:entry :nodeType])) events)]
          {:calendar/events calendar-events})
        (do
          (log/error "Failed to fetch calendar events")
          {:calendar/events []})))
    (do
      (log/warn "No calendar node mapping configured")
      {:calendar/events []})))

;; Alfresco root discovery resolver
(defresolver alfresco-root
  "Get the Alfresco root node information"
  [ctx input]
  {::pco/output [:alfresco/root]}
  (let [result (alfresco/get-root-node ctx)]
    (if (:success result)
      {:alfresco/root (:data result)}
      (do
        (log/error "Failed to fetch root node")
        {:alfresco/root nil}))))

;; Dynamic page discovery resolver
(defresolver discovered-pages
  "Dynamically discover all pages from the Web Site folder"
  [ctx input]
  {::pco/output [:site/pages]}
  (if-let [website-node-id (config/get-node-id :website)]
    (let [children-result (alfresco/get-node-children ctx website-node-id)]
      (if (:success children-result)
        (let [folders (filter #(get-in % [:entry :isFolder]) 
                             (get-in children-result [:data :list :entries]))
              pages (map (fn [folder-entry]
                          (let [entry (:entry folder-entry)
                                folder-name (:name entry)
                                folder-id (:id entry)
                                page-key (keyword (str/lower-case 
                                                  (str/replace folder-name #" " "-")))]
                            {:page/key page-key
                             :page/name folder-name
                             :page/node-id folder-id
                             :page/slug (str/lower-case 
                                        (str/replace folder-name #" " "-"))
                             :page/discovered true}))
                        folders)]
          {:site/pages pages})
        (do
          (log/error "Failed to fetch website children")
          {:site/pages []})))
    (do
      (log/warn "No website node ID configured")
      {:site/pages []})))

;; Dynamic page content resolver that uses the folder discovery directly
(defresolver dynamic-page-content
  "Get content for any discovered page by slug"
  [ctx {:page/keys [slug]}]
  {::pco/input [:page/slug]
   ::pco/output [:page/title :page/content :page/node-id :page/exists]}
  (if-let [website-node-id (config/get-node-id :website)]
    (let [children-result (alfresco/get-node-children ctx website-node-id)]
      (if (:success children-result)
        (let [folders (filter #(get-in % [:entry :isFolder]) 
                             (get-in children-result [:data :list :entries]))
              matching-folder (first (filter (fn [folder-entry]
                                              (let [folder-name (get-in folder-entry [:entry :name])
                                                    folder-slug (str/lower-case 
                                                                (str/replace folder-name #" " "-"))]
                                                (= folder-slug slug)))
                                            folders))]
          (if matching-folder
            (let [node-data (:entry matching-folder)
                  node-id (:id node-data)
                  title (or (get-in node-data [:properties :cm:title])
                           (:name node-data))
                  folder-children-result (alfresco/get-node-children ctx node-id)
                  content (if (:success folder-children-result)
                           (let [children (get-in folder-children-result [:data :list :entries])
                                 files (filter #(get-in % [:entry :isFile]) children)
                                 html-files (filter #(let [name (get-in % [:entry :name])]
                                                      (or (.endsWith name ".html")
                                                          (.endsWith name ".htm")))
                                                   files)]
                             (if (empty? children)
                               (str "This " (:name node-data) " section is ready for content.")
                               ;; If we have HTML files, read content from the first one
                               (if (seq html-files)
                                 (let [first-html (first html-files)
                                       file-id (get-in first-html [:entry :id])
                                       content-result (alfresco/get-node-content ctx file-id)]
                                   (if (:success content-result)
                                     (String. (:data content-result) "UTF-8")
                                     "Unable to read HTML file content."))
                                 ;; Otherwise, try reading the first file if available
                                 (if (seq files)
                                   (let [first-file (first files)
                                         file-id (get-in first-file [:entry :id])
                                         content-result (alfresco/get-node-content ctx file-id)]
                                     (if (:success content-result)
                                       (String. (:data content-result) "UTF-8")
                                       "Unable to read file content."))
                                   (str "Found " (count files) " files and " (count folders) " folders in " (:name node-data) " section.")))))
                           "Unable to access folder contents.")]
              {:page/title title
               :page/content content
               :page/node-id node-id
               :page/exists true})
            {:page/title (str "Not Found - " slug)
             :page/content "Page not found"
             :page/node-id nil
             :page/exists false}))
        {:page/title (str "Error - " slug)
         :page/content "Unable to access website folder"
         :page/node-id nil
         :page/exists false}))
    {:page/title (str "Configuration Error - " slug)
     :page/content "Website node not configured"
     :page/node-id nil
     :page/exists false}))

;; Site navigation resolver
(defresolver site-navigation
  "Get dynamic navigation based on discovered pages"
  [ctx input]
  {::pco/output [:site/navigation]}
  (if-let [website-node-id (config/get-node-id :website)]
    (let [children-result (alfresco/get-node-children ctx website-node-id)]
      (if (:success children-result)
        (let [folders (filter #(get-in % [:entry :isFolder]) 
                             (get-in children-result [:data :list :entries]))
              nav-items (map (fn [folder-entry]
                            (let [entry (:entry folder-entry)
                                  folder-name (:name entry)
                                  folder-slug (str/lower-case 
                                              (str/replace folder-name #" " "-"))]
                              {:nav/label folder-name
                               :nav/path (str "/page/" folder-slug)
                               :nav/key (keyword folder-slug)}))
                            folders)]
          {:site/navigation nav-items})
        {:site/navigation []}))
    {:site/navigation []}))

(def all-resolvers
  "All Pathom resolvers"
  (concat
   [hello-content
    alfresco-node
    alfresco-node-children
    alfresco-content
    page-content
    calendar-events
    alfresco-root
    discovered-pages
    dynamic-page-content
    site-navigation]
   ;; Add component resolvers
   components/component-resolvers
   ;; Add blog resolvers
   blog/blog-resolvers
   ;; Add Sunday Worship resolvers
   worship/sunday-worship-resolvers))

;; --- PATHOM PROCESSOR ---

(def pathom-processor
  "Pathom 3 processor with all resolvers"
  (delay
    (p.eql/boundary-interface
      (pci/register all-resolvers))))

(defn query 
  "Execute a Pathom query"
  [ctx eql-query]
  (try
    (let [processor @pathom-processor]
      (processor ctx eql-query))
    (catch Exception e
      (log/error "Pathom query failed:" (.getMessage e))
      {:error (.getMessage e) :query eql-query})))

;; --- HELPER FUNCTIONS ---

(defn discover-page-nodes
  "Helper function to discover actual node IDs for page mapping"
  [ctx]
  (let [root-result (alfresco/get-root-node ctx)]
    (if (:success root-result)
      (let [root-id (get-in root-result [:data :entry :id])
            children-result (alfresco/get-node-children ctx root-id)]
        (if (:success children-result)
          (get-in children-result [:data :list :entries])
          []))
      [])))

(comment
  ;; Test the new real Pathom setup:
  (query {} [{[:test/name "World"] [:test/greeting]}])
  (query {} [{[:page/key :home] [:page/title :page/content]}])
  (query {} [:alfresco/root])
  (query {} [:calendar/events])
  
  ;; Discover nodes for mapping:
  (discover-page-nodes {}))