(ns mtz-cms.routes.api
  "API routes for HTMX dynamic component loading"
  (:require
   [hiccup.core :as hiccup]
   [mtz-cms.pathom.resolvers :as pathom]
   [mtz-cms.components.htmx-templates :as htmx-templates]
   [mtz-cms.components.templates :as templates]
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
  "Serve hero component data dynamically"
  (let [ctx {}
        node-id (get-in request [:path-params :node-id])
        result (pathom/query ctx [{:hero/node-id node-id} 
                                  [:hero/title :hero/image :hero/content]])]
    (log/info "Loading hero component for node:" node-id)
    (html-fragment-response
     [:div
      [:h1 {:class "text-3xl font-extrabold sm:text-5xl"}
       (:hero/title (get result {:hero/node-id node-id}))
       [:strong {:class "block font-extrabold text-blue-200"} "United Church of Christ"]]
      [:p {:class "mt-4 max-w-lg sm:text-xl/relaxed"}
       (or (:hero/content (get result {:hero/node-id node-id}))
           "A progressive Christian community welcoming all people.")]])))

(defn feature-component-handler [request]
  "Serve feature component data dynamically"
  (let [ctx {}
        node-id (get-in request [:path-params :node-id])
        result (pathom/query ctx [{:feature/node-id node-id} 
                                  [:feature/title :feature/content :feature/image :feature/type]])]
    (log/info "Loading feature component for node:" node-id)
    (html-fragment-response
     (htmx-templates/htmx-feature-with-image (get result {:feature/node-id node-id})))))

(defn feature-content-handler [request]
  "Serve just the content portion of a feature component"
  (let [ctx {}
        node-id (get-in request [:path-params :node-id])
        result (pathom/query ctx [{:feature/node-id node-id} 
                                  [:feature/content]])]
    (log/info "Loading feature content for node:" node-id)
    (html-fragment-response
     [:div {:dangerouslySetInnerHTML 
            {:__html (:feature/content (get result {:feature/node-id node-id}))}}])))

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
        query (get-in request [:params :query])
        result (pathom/query ctx query)]
    (log/info "Pathom query:" query)
    (json-response result)))

;; --- API ROUTES ---

(def api-routes
  "API routes for HTMX dynamic loading"
  [["/api"
    ["/components"
     ["/hero/:node-id" {:get hero-component-handler}]
     ["/feature/:node-id" {:get feature-component-handler}]
     ["/feature/:node-id/content" {:get feature-content-handler}]
     ["/refresh" {:get components-refresh-handler
                  :post components-refresh-handler}]
     ["/add" {:get component-add-handler}]
     ["/cta" {:get cta-component-handler}]]
    
    ["/page"
     ["/publish" {:post page-publish-handler}]]
    
    ["/pathom" {:post pathom-query-handler}]]])