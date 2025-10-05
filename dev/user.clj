(ns user
  "Development namespace for Mount Zion CMS"
  (:require
   [clojure.tools.namespace.repl :as repl]
   [mtz-cms.core :as core]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.pathom.resolvers :as pathom]))

;; --- DEVELOPMENT SERVER ---

(def server (atom nil))

(defn start []
  (when @server
    (.stop @server))
  (reset! server (core/start-server 3000))
  (println "🚀 Mount Zion CMS started on http://localhost:3000"))

(defn stop []
  (when @server
    (.stop @server)
    (reset! server nil))
  (println "🛑 Mount Zion CMS stopped"))

(defn restart []
  (stop)
  (repl/refresh :after 'user/start))

;; --- DEVELOPMENT HELPERS ---

(defn test-alfresco []
  (alfresco/test-connection {}))

(defn test-pathom []
  (pathom/query {} [{[:test/name "Development"] [:test/greeting]}]))

(defn test-pathom-real []
  "Test real Pathom with Alfresco connection"
  (pathom/query {} [:alfresco/root]))

(defn explore-alfresco []
  "Explore Alfresco structure to help set up page mappings"
  (alfresco/explore-structure {}))

(defn discover-nodes []
  "Discover available nodes for page mapping"
  (pathom/discover-page-nodes {}))

(defn test-page-content [page-key]
  "Test getting content for a specific page"
  (pathom/query {} [{[:page/key page-key] [:page/title :page/content :page/node-id]}]))

(defn discover-all-pages []
  "Discover all pages dynamically from Alfresco"
  (pathom/query {} [:site/pages]))

(defn test-dynamic-page [slug]
  "Test getting content for a dynamic page by slug"
  (pathom/query {} [{[:page/slug slug] [:page/title :page/content :page/exists]}]))

(defn get-navigation []
  "Get dynamic navigation"
  (pathom/query {} [:site/navigation]))

(defn test-hero-component []
  "Test hero component with real data"
  (pathom/query {} [{:hero/node-id "39985c5c-201a-42f6-985c-5c201a62f6d8"}
                    [:hero/title :hero/image :hero/content]]))

(defn test-feature-component [feature-num]
  "Test feature component with real data"
  (let [node-id (case feature-num
                  1 "264ab06c-984e-4f64-8ab0-6c984eaf6440" ; Feature 1
                  2 "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89" ; Feature 2
                  3 "6737d1b1-5465-4625-b7d1-b15465b62530" ; Feature 3
                  "264ab06c-984e-4f64-8ab0-6c984eaf6440")]
    (pathom/query {} [{:feature/node-id node-id}
                      [:feature/title :feature/content :feature/image :feature/type]])))

(defn test-home-components []
  "Test complete home page component composition"
  (pathom/query {} [:home/hero :home/features :home/layout]))

(defn test-htmx-hero []
  "Test HTMX hero component with node ID"
  (let [node-id "39985c5c-201a-42f6-985c-5c201a62f6d8"]
    (pathom/query {} [{:hero/node-id node-id} [:hero/title :hero/image :hero/content]])))

(defn test-htmx-feature [feature-num]
  "Test HTMX feature component with node ID"
  (let [node-id (case feature-num
                  1 "264ab06c-984e-4f64-8ab0-6c984eaf6440" ; Feature 1
                  2 "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89" ; Feature 2
                  3 "6737d1b1-5465-4625-b7d1-b15465b62530" ; Feature 3
                  "264ab06c-984e-4f64-8ab0-6c984eaf6440")]
    (pathom/query {} [{:feature/node-id node-id} [:feature/title :feature/content :feature/image :feature/type]])))

(defn test-htmx-home []
  "Test HTMX home page configuration"
  (try
    (require '[mtz-cms.components.htmx :as htmx] :reload)
    ((resolve 'mtz-cms.components.htmx/get-page-component-config) :home)
    (catch Exception e
      (println "Error loading htmx namespace:")
      (println (.getMessage e))
      (println "\nTrying alternative approach...")
      ;; Return mock data if namespace fails to load
      {:layout :hero-features-layout
       :components {:hero {:node-id "39985c5c-201a-42f6-985c-5c201a62f6d8"}
                    :features [{:node-id "264ab06c-984e-4f64-8ab0-6c984eaf6440"}
                               {:node-id "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89"}
                               {:node-id "6737d1b1-5465-4625-b7d1-b15465b62530"}]}})))

(defn routes []
  (require 'mtz-cms.routes.main :reload)
  @(resolve 'mtz-cms.routes.main/all-routes))

;; --- BLOG DISCOVERY FUNCTIONS ---

(defn find-blog-folder []
  "Find the blog folder in Alfresco at Sites/swsdp/blog"
  (alfresco/find-nodes-by-path {} "//cm:swsdp/cm:blog"))

(defn explore-blog []
  "Explore the blog folder structure and content"
  (let [blog-result (find-blog-folder)]
    (if (:success blog-result)
      (let [entries (get-in blog-result [:data :list :entries])]
        (if (seq entries)
          (let [blog-node (first entries)
                blog-id (get-in blog-node [:entry :id])]
            (println "📁 Found blog folder:" blog-id)
            (alfresco/get-node-children {} blog-id {:include "properties,aspectNames"}))
          {:error "Blog folder not found at Sites/swsdp/blog"}))
      blog-result)))

(defn get-blog-post [node-id]
  "Get detailed information about a specific blog post"
  (let [node-result (alfresco/get-node {} node-id)]
    (when (:success node-result)
      (let [node-data (get-in node-result [:data :entry])]
        {:node-id node-id
         :name (:name node-data)
         :title (get-in node-data [:properties :cm:title])
         :description (get-in node-data [:properties :cm:description])
         :created-at (:createdAt node-data)
         :modified-at (:modifiedAt node-data)
         :created-by (get-in node-data [:createdByUser :displayName])
         :aspect-names (:aspectNames node-data)
         :properties (:properties node-data)
         :full-node node-data}))))

(defn analyze-blog-structure []
  "Analyze blog structure and extract schema information"
  (let [blog-result (explore-blog)]
    (if (:success blog-result)
      (let [entries (get-in blog-result [:data :list :entries])]
        (println "\n📊 Blog Analysis:")
        (println "  Total blog posts:" (count entries))
        (println "\n📝 Blog Post Details:\n")
        (doseq [entry entries]
          (let [node-id (get-in entry [:entry :id])
                post (get-blog-post node-id)]
            (println "  ---")
            (println "  Title:" (:title post))
            (println "  Name:" (:name post))
            (println "  Description:" (:description post))
            (println "  Node ID:" node-id)
            (println "  Created:" (:created-at post))
            (println "  Author:" (:created-by post))
            (println "  Aspects:" (:aspect-names post))
            (println "  Properties:")
            (doseq [[k v] (:properties post)]
              (println "    " k "=" v))))
        {:entries (map #(get-blog-post (get-in % [:entry :id])) entries)})
      blog-result)))

;; --- BLOG TEST FUNCTIONS ---

(defn test-blog-list []
  "Test blog list retrieval"
  (pathom/query {} [:blog/list]))

(defn test-blog-detail [node-id]
  "Test blog detail retrieval by node ID"
  (pathom/query {} [{[:blog/id node-id]
                     [:blog/slug
                      :blog/title
                      :blog/content
                      :blog/published-at
                      :blog/author
                      :blog/tags]}]))

(defn test-blog-by-slug [slug]
  "Test blog retrieval by slug"
  (pathom/query {} [{[:blog/slug slug]
                     [:blog/id
                      :blog/title
                      :blog/content
                      :blog/published-at
                      :blog/author]}]))

;; --- REPL HELPERS ---

(comment
  ;; Start the server
  (start)

  ;; Stop the server
  (stop)

  ;; Restart with code reload
  (restart)

  ;; Test connections
  (test-alfresco)
  (test-pathom) ; Test mock resolver
  (test-pathom-real) ; Test real Pathom with Alfresco

  ;; Explore Alfresco structure
  (explore-alfresco) ; See what's available in Alfresco
  (discover-nodes) ; Find nodes for page mapping

  ;; Test page content
  (test-page-content :home)
  (test-page-content :about)

  ;; Test dynamic discovery
  (discover-all-pages) ; See all discovered pages
  (get-navigation) ; See dynamic navigation
  (test-dynamic-page "home-page") ; Test dynamic page access
  (test-dynamic-page "about") ; Test another page

  ;; Test component system
  (test-hero-component) ; Test hero with real Alfresco data
  (test-feature-component 1) ; Test feature 1
  (test-feature-component 2) ; Test feature 2
  (test-home-components) ; Test complete home page composition

  ;; View routes
  (routes))