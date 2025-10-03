#!/usr/bin/env bb
;; Static Content Sync Script
;; Pulls pages marked with web:pagetype="Static" from Alfresco
;; and generates static EDN files for fast loading
;;
;; Usage from project root: admin/scripts/Babashka/sync-content.clj
;; or:    clojure -M -m sync-content

(ns sync-content
  (:require [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]))

;; Load the Alfresco client
;; NOTE: This needs to be run with full Clojure, not just babashka
;; Run with: clojure -M -m sync-content from project root

;; Ensure we're in project root
(def project-root
  (let [script-dir (-> (System/getProperty "user.dir")
                       (io/file))]
    (if (.exists (io/file script-dir "deps.edn"))
      script-dir
      (-> script-dir (.getParentFile) (.getParentFile) (.getParentFile)))))

(def config
  "Sync configuration"
  {:output-dir (str project-root "/resources/content/static")
   :search-query "web:pagetype:'Static' AND web:kind:'page'"})

(defn fetch-static-pages
  "Query Alfresco for all pages with web:pagetype='Static'"
  [ctx]
  (println "🔍 Searching for pages marked as static...")
  (println "   Query:" (:search-query config))

  ;; We need to use Clojure to call alfresco client
  ;; This won't work with babashka - needs full Clojure runtime
  (try
    ;; Load alfresco client namespace
    (require '[mtz-cms.alfresco.client :as alfresco])

    ;; Search using AFTS query
    (let [search-fn (resolve 'mtz-cms.alfresco.client/search-nodes)
          result (search-fn ctx (:search-query config))
          entries (get-in result [:data :list :entries])]

      (if (:success result)
        (do
          (println "   ✅ Found" (count entries) "static pages")
          (map (fn [entry]
                 (let [node (get entry :entry)
                       props (:properties node)]
                   {:node-id (:id node)
                    :name (:name node)
                    :title (or (get props :cm:title) (:name node))
                    :slug (or (get props :web:menuLabel)
                             (:name node))
                    :menu-label (get props :web:menuLabel)
                    :menu-item (get props :web:menuItem)}))
               entries))
        (do
          (println "   ❌ Search failed:" (:error result))
          [])))

    (catch Exception e
      (println "❌ Error fetching pages:" (.getMessage e))
      (println "   Make sure to run with: clojure -M -m sync-content")
      [])))

(defn fetch-page-content
  "Fetch full page content from Alfresco by node-id"
  [ctx node-id page-meta]
  (println "  📥 Fetching content for node:" node-id)

  (try
    (require '[mtz-cms.alfresco.client :as alfresco])

    (let [get-node-fn (resolve 'mtz-cms.alfresco.client/get-node)
          get-children-fn (resolve 'mtz-cms.alfresco.client/get-node-children)
          get-content-fn (resolve 'mtz-cms.alfresco.client/get-node-content)

          ;; Get the folder/node info
          node-result (get-node-fn ctx node-id)
          node-data (get-in node-result [:data :entry])
          is-folder (:isFolder node-data)

          ;; Get content - if folder, look for HTML file in children
          content (if is-folder
                    (let [children-result (get-children-fn ctx node-id)
                          children (get-in children-result [:data :list :entries])
                          html-file (first (filter #(clojure.string/ends-with?
                                                     (get-in % [:entry :name])
                                                     ".html")
                                                  children))]
                      (if html-file
                        (let [file-id (get-in html-file [:entry :id])
                              content-result (get-content-fn ctx file-id)]
                          (if (:success content-result)
                            (String. (:data content-result) "UTF-8")
                            "<p>Unable to read HTML content</p>"))
                        "<p>No HTML file found in folder</p>"))
                    ;; If it's a file, get its content directly
                    (let [content-result (get-content-fn ctx node-id)]
                      (if (:success content-result)
                        (String. (:data content-result) "UTF-8")
                        "<p>Unable to read content</p>")))]

      {:title (:title page-meta)
       :slug (:slug page-meta)
       :node-id node-id
       :content content
       :menu-label (:menu-label page-meta)
       :menu-item (:menu-item page-meta)
       :static true
       :generated-at (str (java.time.Instant/now))})

    (catch Exception e
      (println "    ❌ Error:" (.getMessage e))
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
        results (map #(sync-page! ctx %) pages)
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

;; Enable running as script
(when (= *file* (System/getProperty "babashka.file"))
  (-main))

(comment
  ;; For REPL usage
  (sync-all!)

  ;; Sync a specific page
  (sync-page! {} {:node-id "abc-123" :slug "about" :title "About"})
  )
