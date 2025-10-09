(ns mtz-cms.routes.api
  "API routes for HTMX dynamic component loading with validation"
  (:require
   [hiccup.core :as hiccup]
   [mtz-cms.pathom.resolvers :as pathom]
   [mtz-cms.components.htmx-templates :as htmx-templates]
   [mtz-cms.components.templates :as templates]
   [mtz-cms.components.section :as section]
   [mtz-cms.components.hero :as hero]
   [mtz-cms.components.home-features :as home-features]
   [mtz-cms.validation.middleware :as validation]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.cache.simple :as cache]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- RESPONSE HELPERS ---

(defn html-fragment-response [hiccup-content]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html hiccup-content)})

(defn json-response [data]
  {:status 200
   :headers {"Content-Type" "application/json"
             "HX-Trigger" "dataUpdated"}  ; HTMX trigger
   :body (pr-str data)})

(defn htmx-response [hiccup-content & [triggers]]
  {:status 200
   :headers (merge {"Content-Type" "text/html"}
                   (when triggers {"HX-Trigger" (pr-str triggers)}))
   :body (hiccup/html hiccup-content)})

;; --- COMPONENT API HANDLERS ---

(defn hero-component-handler [request]
  "Serve hero component data dynamically via HTMX

   Fetches hero data from Pathom and delegates rendering to hero/hero-carousel component.
   This is the API handler - the actual component logic lives in components/hero.clj"
  (try
    (let [ctx {}
          node-id (get-in request [:path-params :node-id])
          _ (log/info "🔍 Loading hero component for node:" node-id)

          ;; Query Pathom for hero data
          result (pathom/query ctx [{[:hero/node-id node-id]
                                    [:hero/title :hero/image :hero/images :hero/content]}])
          _ (log/info "📊 Pathom query result:" result)

          hero-data (get result [:hero/node-id node-id])
          _ (log/info "🎨 Hero data extracted:" hero-data)
          _ (log/info "🖼️  Images count:" (count (:hero/images hero-data [])))

          ;; Delegate rendering to hero component
          response (html-fragment-response (hero/hero-carousel hero-data))]

      ;; Validate HTMX response
      (validation/validate-htmx-response response "hero-component")

      (log/info "✅ Hero component rendered successfully for node:" node-id)
      response)

    (catch Exception e
      (log/error "❌ Error in hero component handler:" (.getMessage e))
      (log/error "Stack trace:" e)
      ;; Return fallback response
      (html-fragment-response
       [:div {:class "bg-red-50 border border-red-200 rounded p-4"}
        [:h2 {:class "text-red-800"} "Content Loading Error"]
        [:p {:class "text-red-600"} "Unable to load hero content. Please try again."]
        [:p {:class "text-red-500 text-sm mt-2"} (str "Error: " (.getMessage e))]]))))

(defn feature-component-handler [request]
  "Serve feature component data dynamically"
  (let [ctx {}
        node-id (get-in request [:path-params :node-id])
        result (pathom/query ctx [{[:feature/node-id node-id]
                                  [:feature/title :feature/content :feature/image :feature/type]}])]
    (log/info "Loading feature component for node:" node-id "result:" result)
    (let [feature-data (assoc (get result [:feature/node-id node-id]) :feature/node-id node-id)]
      (html-fragment-response
       (htmx-templates/htmx-feature-with-image feature-data)))))

(defn feature-content-handler [request]
  "Serve just the content portion of a feature component"
  (let [ctx {}
        node-id (get-in request [:path-params :node-id])
        result (pathom/query ctx [{[:feature/node-id node-id]
                                  [:feature/content]}])]
    (log/info "Loading feature content for node:" node-id)
    (let [content (:feature/content (get result [:feature/node-id node-id]))]
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str "<div>" (or content "") "</div>")})))

(defn feature-card-handler [request]
  "Serve feature card for homepage HTMX loading

   Returns a feature card with title, description, and link to detail page"
  (try
    (let [ctx {}
          node-id (get-in request [:path-params :node-id])
          _ (log/info "🃏 Loading feature card for node:" node-id)

          ;; Query Pathom for feature data
          result (pathom/query ctx [{[:feature/node-id node-id]
                                    [:feature/title :feature/content :feature/image]}])
          feature-raw (get result [:feature/node-id node-id])

          ;; Map node-id to feature slug
          slug-mapping {"264ab06c-984e-4f64-8ab0-6c984eaf6440" "feature1"
                        "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89" "feature2"
                        "6737d1b1-5465-4625-b7d1-b15465b62530" "feature3"}
          slug (get slug-mapping node-id "feature-unknown")

          ;; Extract description from content (first 150 chars)
          description (when (:feature/content feature-raw)
                        (home-features/extract-text-from-html
                         (:feature/content feature-raw)
                         150))

          ;; Build card data
          feature-card-data {:feature/id slug
                            :feature/title (or (:feature/title feature-raw) "Feature")
                            :feature/description (or description "Click to learn more about this feature.")
                            :feature/image (:feature/image feature-raw)
                            :feature/link (str "/features/" slug)}

          response (html-fragment-response
                   (home-features/feature-card feature-card-data))]

      (log/info "✅ Feature card rendered for node:" node-id "slug:" slug)
      response)

    (catch Exception e
      (log/error "❌ Error in feature card handler:" (.getMessage e))
      ;; Return placeholder card on error
      (html-fragment-response
       (home-features/placeholder-feature-card "error")))))

(defn components-refresh-handler [request]
  "Refresh all components on a page"
  (let [ctx {}
        result (pathom/query ctx [:home/features])]
    (log/info "Refreshing all components")
    (htmx-response
     (htmx-templates/htmx-dynamic-components-container {:home/features (:home/features result)})
     {:componentsRefreshed true})))

(defn component-add-handler [request]
  "Add a new component (placeholder for now)"
  (log/info "Adding new component")
  (html-fragment-response
   [:div {:class "bg-yellow-50 border border-yellow-200 rounded-lg p-6 mb-8"}
    [:h3 {:class "text-lg font-medium text-yellow-800"} "New Component"]
    [:p {:class "text-yellow-700"} "Component creation interface would go here."]
    [:button {:class "mt-4 bg-yellow-600 text-white px-4 py-2 rounded hover:bg-yellow-700"
              :hx-delete "#"
              :hx-confirm "Remove this component?"}
     "Remove"]]))

(defn cta-component-handler [request]
  "Serve call-to-action component"
  (html-fragment-response
   [:div {:class "lg:grid lg:grid-cols-2 lg:gap-8 lg:items-center"}
    [:div
     [:h2 {:class "text-3xl font-bold tracking-tight text-white sm:text-4xl"}
      "Join Our Community"]
     [:p {:class "mt-3 max-w-3xl text-lg text-blue-100"}
      "Experience the warmth and fellowship of Mount Zion UCC. All are welcome."]]
    [:div {:class "mt-8 lg:mt-0"}
     [:button {:class "inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-blue-600 bg-white hover:bg-blue-50"
               :hx-get "/page/worship"
               :hx-target "#main-content"
               :hx-push-url "true"}
      "Plan Your Visit"]
     [:button {:class "ml-3 inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-500 hover:bg-blue-400"
               :hx-get "/page/contact"
               :hx-target "#main-content"
               :hx-push-url "true"}
      "Contact Us"]]]))

(defn section-component-handler [request]
  "Serve section component data dynamically from Alfresco"
  (try
    (let [ctx {}
          node-id (get-in request [:path-params :node-id])
          _ (log/info "🔍 Loading section component for node:" node-id)

          ;; Query Pathom for section data
          result (pathom/query ctx [{[:section/node-id node-id]
                                    [:section/title :section/subtitle :section/body
                                     :section/description :section/image :section/type]}])
          _ (log/debug "Pathom section query result:" result)

          section-data (get result [:section/node-id node-id])

          ;; Render section using the smart renderer
          response (html-fragment-response
                   (section/render-section section-data))]

      (log/info "✅ Section component rendered successfully for node:" node-id)
      response)

    (catch Exception e
      (log/error "❌ Error in section component handler:" (.getMessage e))
      ;; Return fallback response
      (html-fragment-response
       [:div {:class "bg-red-50 border border-red-200 rounded p-4"}
        [:h2 {:class "text-red-800"} "Content Loading Error"]
        [:p {:class "text-red-600"} "Unable to load section content. Please try again."]]))))

;; --- PAGE API HANDLERS ---

(defn page-publish-handler [request]
  "Handle page publishing"
  (log/info "Publishing page changes")
  (html-fragment-response
   [:div {:class "bg-green-50 border border-green-200 rounded-lg p-4 mb-4"}
    [:div {:class "flex"}
     [:div {:class "flex-shrink-0"}
      [:svg {:class "h-5 w-5 text-green-400" :viewBox "0 0 20 20" :fill "currentColor"}
       [:path {:fill-rule "evenodd" :d "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" :clip-rule "evenodd"}]]]
     [:div {:class "ml-3"}
      [:p {:class "text-sm font-medium text-green-800"}
       "Page published successfully!"]]]]))

;; --- PATHOM QUERY API ---

(defn pathom-query-handler [request]
  "Handle dynamic Pathom queries from HTMX"
  (let [ctx {}
        query-str (get-in request [:params :query])
        query (try
                (read-string query-str)
                (catch Exception e
                  (log/error "Failed to parse query:" query-str (.getMessage e))
                  nil))
        result (if query
                 (pathom/query ctx query)
                 {:error "Invalid query format"})]
    (log/info "Pathom query:" query "result:" result)
    (json-response result)))

;; --- IMAGE PROXY HANDLER ---

(defn image-proxy-handler [request]
  "Proxy images from Alfresco with authentication and caching.
   Images are cached for 24 hours (86400 seconds) since they rarely change."
  (let [node-id (get-in request [:path-params :node-id])
        ctx {}
        ;; Cache images for 24 hours - they rarely change
        result (cache/cached
                (keyword "image" node-id)
                86400  ;; 24 hours in seconds
                #(alfresco/get-node-content ctx node-id))]
    (if (:success result)
      {:status 200
       :headers {"Content-Type" "image/png"
                 "Cache-Control" "public, max-age=86400"}  ;; Tell browser to cache too
       :body (:data result)}
      {:status 404
       :headers {"Content-Type" "text/plain"}
       :body "Image not found"})))

;; --- TIME API HANDLER ---

(defn time-handler [request]
  "Return current server time for HTMX demo"
  (html-fragment-response
   [:div {:class "bg-green-50 border border-green-200 rounded-md p-4 text-green-800"}
    [:div {:class "flex items-center"}
     [:svg {:class "w-5 h-5 mr-2" :fill "currentColor" :viewBox "0 0 20 20"}
      [:path {:fill-rule "evenodd" :d "M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" :clip-rule "evenodd"}]]
     [:span {:class "font-medium"} "Server Time: " (str (java.util.Date.))]]]))

;; --- API ROUTES ---

(def api-routes
  "API routes for HTMX dynamic loading"
  [["/api"
    ["/time" {:get time-handler}]
    
    ["/components"
     ["/hero/:node-id" {:get hero-component-handler}]
     ["/feature/:node-id" {:get feature-component-handler}]
     ["/feature/:node-id/content" {:get feature-content-handler}]
     ["/section/:node-id" {:get section-component-handler}]
     ["/refresh" {:get components-refresh-handler
                  :post components-refresh-handler}]
     ["/add" {:get component-add-handler}]
     ["/cta" {:get cta-component-handler}]]

    ["/features"
     ["/card/:node-id" {:get feature-card-handler}]]
    
    ["/page"
     ["/publish" {:post page-publish-handler}]]

    ["/pathom" {:post pathom-query-handler}]

    ["/image/:node-id" {:get image-proxy-handler}]]])