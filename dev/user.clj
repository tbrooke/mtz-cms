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