(ns sync-content
  "Static Content Sync Script
   Pulls pages marked with web:pagetype='Static' from Alfresco
   and generates static EDN files for fast loading"
  (:require [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [mtz-cms.alfresco.client :as alfresco]))

(defn slugify
  "Convert a string to URL-friendly slug
   Example: 'Pickle Ball' -> 'pickle-ball'"
  [s]
  (-> s
      str/lower-case
      (str/replace #"\s+" "-")
      (str/replace #"[^a-z0-9\-]" "")))  ; Remove non-alphanumeric except hyphens

(def config
  "Sync configuration"
  {:output-dir "resources/content/static"
   :search-query "web:pagetype:'Static' AND web:kind:'page'"})

(defn fetch-static-pages
  "Query Alfresco for all pages with web:pagetype='Static'"
  [ctx]
  (println "🔍 Searching for pages marked as static...")
  (println "   Query:" (:search-query config))

  (try
    ;; Search using AFTS query
    (let [result (alfresco/search-nodes ctx (:search-query config))
          entries (get-in result [:data :list :entries])]

      (if (:success result)
        (do
          (println "   ✅ Found" (count entries) "static pages")
          (mapv (fn [entry]
                  (let [node (get entry :entry)
                        props (:properties node)
                        title (or (get props :cm:title) (:name node))
                        menu-label (get props :web:menuLabel)]
                    {:node-id (:id node)
                     :name (:name node)
                     :title title
                     :slug (slugify (or menu-label title (:name node)))  ; URL-friendly slug
                     :original-name (:name node)
                     :menu-label menu-label
                     :menu-item (get props :web:menuItem)}))
                entries))
        (do
          (println "   ❌ Search failed:" (:error result))
          [])))

    (catch Exception e
      (println "❌ Error fetching pages:" (.getMessage e))
      (.printStackTrace e)
      [])))

(defn fetch-page-content
  "Fetch full page content from Alfresco by node-id"
  [ctx node-id page-meta]
  (println "  📥 Fetching content for node:" node-id)

  (try
    (let [;; Get the folder/node info
          node-result (alfresco/get-node ctx node-id)
          node-data (get-in node-result [:data :entry])
          is-folder (:isFolder node-data)

          ;; Get content - if folder, look for HTML content in children
          content (if is-folder
                    (let [children-result (alfresco/get-node-children ctx node-id)
                          children (get-in children-result [:data :list :entries])
                          ;; Look for HTML files (either .html extension OR text/html mime type)
                          html-file (first (filter (fn [child]
                                                     (let [entry (:entry child)
                                                           name (:name entry)
                                                           mime (get-in entry [:content :mimeType])]
                                                       (or (str/ends-with? name ".html")
                                                           (= mime "text/html"))))
                                                  children))]
                      (if html-file
                        (let [file-id (get-in html-file [:entry :id])
                              content-result (alfresco/get-node-content ctx file-id)]
                          (if (:success content-result)
                            (String. (:data content-result) "UTF-8")
                            "<p>Unable to read HTML content</p>"))
                        (str "<p>No HTML content found in folder</p>"
                             "<p>Children: " (pr-str (map #(get-in % [:entry :name]) children)) "</p>")))
                    ;; If it's a file, get its content directly
                    (let [content-result (alfresco/get-node-content ctx node-id)]
                      (if (:success content-result)
                        (String. (:data content-result) "UTF-8")
                        "<p>Unable to read content</p>")))]

      {:title (:title page-meta)
       :slug (:slug page-meta)
       :original-name (:original-name page-meta)
       :node-id node-id
       :content content
       :menu-label (:menu-label page-meta)
       :menu-item (:menu-item page-meta)
       :static true
       :generated-at (str (java.time.Instant/now))})

    (catch Exception e
      (println "    ❌ Error:" (.getMessage e))
      (.printStackTrace e)
      nil)))

(defn ensure-output-dir!
  "Create output directory if it doesn't exist"
  []
  (let [dir (io/file (:output-dir config))]
    (when-not (.exists dir)
      (.mkdirs dir)
      (println "📁 Created directory:" (.getPath dir)))))

(defn write-static-file
  "Write page content to static EDN file"
  [slug content]
  (let [output-file (io/file (:output-dir config) (str slug ".edn"))]

    (with-open [w (io/writer output-file)]
      (binding [*print-length* nil
                *print-level* nil]
        (pprint content w)))

    (println "  ✅ Generated:" (.getPath output-file))))

(defn sync-page!
  "Sync a single page from Alfresco to static file"
  [ctx page-meta]
  (let [{:keys [node-id slug title]} page-meta]
    (println "\n📄" title (str "(" slug ")"))

    (if-let [content (fetch-page-content ctx node-id page-meta)]
      (do
        (write-static-file slug content)
        :success)
      (do
        (println "  ⚠️  Skipped - failed to fetch content")
        :failed))))

(defn sync-all!
  "Main sync function - pull all static pages from Alfresco"
  []
  (println "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
  (println "🔄 Static Content Sync - Mount Zion CMS")
  (println "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

  (ensure-output-dir!)

  ;; Create context for Alfresco calls
  (let [ctx {} ;; Uses default config
        pages (fetch-static-pages ctx)
        results (mapv #(sync-page! ctx %) pages)
        success-count (count (filter #(= % :success) results))
        total-count (count pages)]

    (println "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    (println "✅ Sync Complete!")
    (println "   Generated:" success-count "/" total-count "pages")
    (println "   Location:" (:output-dir config))
    (println "\n📝 Next steps:")
    (println "   1. Review generated files in" (:output-dir config))
    (println "   2. Edit files manually if needed")
    (println "   3. Commit changes: git add resources/content/static/")
    (println "   4. Deploy: ./deploy.sh")
    (println "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")))

(defn -main [& args]
  (sync-all!))

(comment
  ;; For REPL usage
  (sync-all!)

  ;; Sync a specific page
  (sync-page! {} {:node-id "abc-123" :slug "about" :title "About"})
  )
