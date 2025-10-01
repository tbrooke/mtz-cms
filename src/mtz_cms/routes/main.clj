(ns mtz-cms.routes.main
  "Main routing for Mount Zion CMS"
  (:require
   [hiccup.core :as hiccup]
   [mtz-cms.pathom.resolvers :as pathom]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.ui.pages :as pages]
   [mtz-cms.components.templates :as components]
   [mtz-cms.components.htmx :as htmx]
   [mtz-cms.routes.api :as api]
   [mtz-cms.validation.dashboard :as dashboard]
   [clojure.tools.logging :as log]))

;; --- HANDLER HELPERS ---

(defn html-response [hiccup-content]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html hiccup-content)})

(defn json-response [data]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (pr-str data)})

;; --- ROUTE HANDLERS ---

(defn home-handler [request]
  "Home page with HTMX dynamic components"
  (let [ctx {}  ; Alfresco client uses default-config
        page-config (htmx/get-page-component-config :home)]
    (html-response
     (pages/base-layout
      "Mount Zion UCC - Home"
      (htmx/htmx-hero-features-layout page-config)
      ctx))))

(defn about-handler [request]
  (let [ctx {}
        result (pathom/query ctx [{[:page/key :about] [:page/title :page/content]}])
        page-data (get result [:page/key :about])]
    (html-response (pages/about-page page-data))))

(defn demo-handler [request]
  (let [ctx {}
        ;; Test Pathom
        greeting-result (pathom/query ctx [{[:test/name "Mount Zion CMS"] [:test/greeting]}])
        greeting (get-in greeting-result [[:test/name "Mount Zion CMS"] :test/greeting])

        ;; Test Alfresco
        alfresco-result (alfresco/test-connection ctx)]

    (html-response (pages/demo-page {:greeting greeting
                                     :alfresco alfresco-result}))))

(defn dynamic-page-handler [request]
  "Handle any page by discovering it from Alfresco"
  (let [ctx {}
        slug (get-in request [:path-params :slug])
        result (pathom/query ctx [{[:page/slug slug] [:page/title :page/content :page/exists]}])
        page-data (get result [:page/slug slug])]
    (if (:page/exists page-data)
      (html-response (pages/dynamic-page page-data))
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body (hiccup/html (pages/not-found-page slug))})))

(defn pages-list-handler [request]
  "List all discovered pages"
  (let [ctx {}
        result (pathom/query ctx [:site/pages :site/navigation])
        pages (:site/pages result)
        navigation (:site/navigation result)]
    (html-response (pages/pages-list-page {:pages pages :navigation navigation}))))

(defn image-proxy-handler [request]
  "Proxy images from Alfresco by node ID"
  (let [ctx {}
        node-id (get-in request [:path-params :node-id])]
    (log/debug "Proxying image for node:" node-id)
    (try
      (let [result (alfresco/get-node-content ctx node-id)]
        (if (:success result)
          (let [node-info (alfresco/get-node ctx node-id)
                mime-type (if (:success node-info)
                            (get-in node-info [:data :entry :content :mimeType])
                            "image/jpeg")]
            {:status 200
             :headers {"Content-Type" mime-type
                       "Cache-Control" "public, max-age=3600"}
             :body (:data result)})
          (do
            (log/error "Failed to fetch image from Alfresco:" (:error result))
            {:status 404
             :body "Image not found"})))
      (catch Exception e
        (log/error "Error proxying image:" (.getMessage e))
        {:status 500
         :body "Image proxy error"}))))

;; --- ROUTES ---

(def all-routes
  "All application routes including API routes"
  (concat
   [["/" {:get home-handler}]

    ["/about" {:get about-handler}]

    ["/demo" {:get demo-handler}]

    ["/pages" {:get pages-list-handler}]

    ;; Image proxy - must come before dynamic page handler
    ["/proxy/image/:node-id" {:get image-proxy-handler}]

;; Dynamic page handler - catches any page slug
    ["/page/:slug" {:get dynamic-page-handler}]

    ;; Static assets (basic)
    ["/assets/*" {:get (fn [request]
                         {:status 404
                          :body "Static assets not implemented"})}]]

   ;; API routes for HTMX dynamic loading
   api/api-routes
   
   ;; Validation dashboard routes
   dashboard/dashboard-routes))

(comment
  ;; Test routes
  (home-handler {})
  (demo-handler {}))