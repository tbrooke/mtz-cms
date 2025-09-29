(ns mtz-cms.components.htmx-templates
  "HTMX-enhanced component templates for Mount Zion CMS"
  (:require
   [clojure.string :as str]))

;; --- HTMX-ENHANCED COMPONENTS ---

(defn htmx-hero-with-image
  "HTMX-enhanced hero component with dynamic content loading"
  [hero-data]
  (let [title (or (:hero/title hero-data) "Welcome")
        content (:hero/content hero-data)
        image (:hero/image hero-data)
        node-id (:hero/node-id hero-data)]
    [:section {:class "relative bg-gradient-to-r from-blue-600 to-blue-800 text-white"
               :id "hero-section"}
     ;; Background image
     (when image
       [:img {:src (str "http://localhost:8080" (:url image))
              :alt (:name image)
              :class "absolute inset-0 h-full w-full object-cover"}])
     
     ;; Content overlay with HTMX refresh capability
     [:div {:class "relative mx-auto max-w-screen-xl px-4 py-32 sm:px-6 lg:flex lg:h-screen lg:items-center lg:px-8"}
      [:div {:class "max-w-xl text-center ltr:sm:text-left rtl:sm:text-right"}
       ;; Dynamic content that can be refreshed
       [:div {:hx-get (str "/api/components/hero/" node-id)
              :hx-trigger "every 30s"  ; Auto-refresh every 30 seconds
              :hx-swap "innerHTML"
              :id "hero-content"}
        [:h1 {:class "text-3xl font-extrabold sm:text-5xl"}
         title
         [:strong {:class "block font-extrabold text-blue-200"} "United Church of Christ"]]
        
        [:p {:class "mt-4 max-w-lg sm:text-xl/relaxed"}
         (or content "A progressive Christian community welcoming all people.")]]
       
       ;; Interactive buttons
       [:div {:class "mt-8 flex flex-wrap gap-4 text-center"}
        [:button {:class "block w-full rounded bg-blue-600 px-12 py-3 text-sm font-medium text-white shadow hover:bg-blue-700 focus:outline-none focus:ring active:bg-blue-500 sm:w-auto"
                  :hx-get "/page/about"
                  :hx-target "#main-content"
                  :hx-push-url "true"}
         "Learn About Us"]
        
        [:button {:class "block w-full rounded bg-white px-12 py-3 text-sm font-medium text-blue-600 shadow hover:bg-gray-100 focus:outline-none focus:ring active:bg-gray-200 sm:w-auto"
                  :hx-get "/page/worship"
                  :hx-target "#main-content"  
                  :hx-push-url "true"}
         "Join Us for Worship"]]]]]))


(defn htmx-feature-with-image
  "HTMX-enhanced feature component with edit capabilities"
  [feature-data]
  (let [title (:feature/title feature-data)
        content (:feature/content feature-data)
        image (:feature/image feature-data)
        node-id (:feature/node-id feature-data)]
    [:section {:class "overflow-hidden bg-white py-8 sm:py-16"
               :id (str "feature-" node-id)}
     [:div {:class "mx-auto max-w-7xl px-6 lg:px-8"}
      [:div {:class "mx-auto grid max-w-2xl grid-cols-1 gap-x-8 gap-y-16 sm:gap-y-20 lg:mx-0 lg:max-w-none lg:grid-cols-2"}
       
       ;; Editable content area
       [:div {:class "lg:pr-8 lg:pt-4"}
        [:div {:class "lg:max-w-lg relative group"}
         ;; Edit button (appears on hover)
         [:button {:class "absolute -top-2 -right-2 opacity-0 group-hover:opacity-100 bg-blue-600 text-white rounded-full w-8 h-8 flex items-center justify-center transition-opacity"
                   :hx-get (str "/api/components/feature/" node-id "/edit")
                   :hx-target (str "#feature-" node-id)
                   :title "Edit this feature"}
          "✏️"]
         
         [:h2 {:class "text-base font-semibold leading-7 text-blue-600"} "Mount Zion UCC"]
         [:p {:class "mt-2 text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl"}
          title]
         
         ;; Dynamic content area
         [:div {:class "mt-6 text-lg leading-8 text-gray-600 prose"
                :hx-get (str "/api/components/feature/" node-id "/content")
                :hx-trigger "every 60s"  ; Refresh content periodically
                :hx-swap "innerHTML"}
          [:div (or content "")]]
         
         [:div {:class "mt-8"}
          [:button {:class "inline-flex items-center gap-x-2 rounded-md bg-blue-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-500"
                    :hx-get "/about"
                    :hx-target "#main-content"
                    :hx-push-url "true"}
           "Learn More"
           [:svg {:class "h-4 w-4" :fill "none" :viewBox "0 0 24 24" :stroke-width "1.5" :stroke "currentColor"}
            [:path {:stroke-linecap "round" :stroke-linejoin "round" :d "M4.5 12h15m0 0l-6.75-6.75M19.5 12l-6.75 6.75"}]]]]]]  ; <- FIXED: Added missing bracket here
       
       ;; Image with lazy loading
       [:div {:class "flex items-start justify-end lg:order-first"}
        (if image
          [:img {:src (str "http://localhost:8080" (:url image))
                 :alt (:name image)
                 :class "w-[48rem] max-w-none rounded-xl shadow-xl ring-1 ring-gray-400/10 sm:w-[57rem]"
                 :loading "lazy"}]
          [:div {:class "w-[48rem] max-w-none rounded-xl bg-gray-100 shadow-xl ring-1 ring-gray-400/10 sm:w-[57rem] h-96 flex items-center justify-center"}
           [:button {:class "text-gray-500 hover:text-blue-600 cursor-pointer"
                     :hx-get (str "/api/components/feature/" node-id "/upload-image")
                     :hx-target (str "#feature-" node-id)}
            [:svg {:class "mx-auto h-12 w-12" :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
             [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"}]]
            [:p {:class "mt-2 text-sm"} "Click to add image"]]])]]]))


(defn htmx-dynamic-components-container
  "Container that can dynamically load and reorder components"
  [page-data]
  (let [components (:home/features page-data)]
    [:div {:class "space-y-12"
           :id "components-container"
           :hx-get "/api/components/refresh"
           :hx-trigger "refreshComponents from:body"}
     
     ;; Component management toolbar
     [:div {:class "bg-gray-50 border-l-4 border-blue-500 p-4 mb-8"}
      [:div {:class "flex items-center justify-between"}
       [:div
        [:h3 {:class "text-lg font-medium text-gray-900"} "Page Components"]
        [:p {:class "text-sm text-gray-600"} "Components will refresh automatically as content changes"]]
       [:div {:class "flex gap-2"}
        [:button {:class "bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
                  :hx-get "/api/components/add"
                  :hx-target "#components-container"
                  :hx-swap "beforeend"}
         "+ Add Component"]
        [:button {:class "bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700"
                  :hx-post "/api/components/refresh"
                  :hx-target "#components-container"}
         "↻ Refresh All"]]]]
     
     ;; Dynamic components
     (for [component components]
       [:div {:key (:feature/node-id component)
              :class "relative"
              :id (str "component-" (:feature/node-id component))}
        (htmx-feature-with-image component)])]))

(defn htmx-page-editor
  "HTMX-powered page editor interface"
  [page-data]
  [:div {:class "fixed bottom-4 right-4 z-50"}
   [:div {:class "bg-white rounded-lg shadow-lg border border-gray-200 p-4"}
    [:h4 {:class "font-semibold text-gray-900 mb-3"} "Page Editor"]
    [:div {:class "space-y-2"}
     [:button {:class "w-full text-left px-3 py-2 text-sm bg-blue-50 hover:bg-blue-100 rounded"
               :hx-get "/api/page/edit-mode"
               :hx-target "body"
               :hx-swap "outerHTML"}
      "✏️ Edit Mode"]
     [:button {:class "w-full text-left px-3 py-2 text-sm bg-green-50 hover:bg-green-100 rounded"
               :hx-post "/api/page/publish"
               :hx-target "#notification-area"}
      "🚀 Publish Changes"]
     [:button {:class "w-full text-left px-3 py-2 text-sm bg-gray-50 hover:bg-gray-100 rounded"
               :hx-get "/api/page/preview"
               :hx-target "_blank"}
      "👁️ Preview"]]]])

;; --- HTMX LAYOUT WITH PARTIAL UPDATES ---

(defn htmx-hero-features-layout
  "HTMX-enhanced layout with partial page updates"
  [page-data]
  [:div {:id "main-content"}
   ;; Hero section
   (when (:home/hero page-data)
     (htmx-hero-with-image (:home/hero page-data)))
   
   ;; Dynamic components container
   (htmx-dynamic-components-container page-data)
   
   ;; Call to action with dynamic content
   [:section {:class "bg-blue-600"
              :hx-get "/api/components/cta"
              :hx-trigger "every 120s"  ; Refresh CTA every 2 minutes
              :hx-swap "innerHTML"}
    [:div {:class "mx-auto max-w-7xl py-12 px-6 lg:px-8 lg:py-24"}
     [:div {:class "lg:grid lg:grid-cols-2 lg:gap-8 lg:items-center"}
      [:div
       [:h2 {:class "text-3xl font-bold tracking-tight text-white sm:text-4xl"}
        "Join Our Community"]
       [:p {:class "mt-3 max-w-3xl text-lg text-blue-100"}
        "Experience the warmth and fellowship of Mount Zion UCC."]]
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
        "Contact Us"]]]]]
   
   ;; Page editor (only show if in edit mode)
   (htmx-page-editor page-data)])

;; --- NOTIFICATION SYSTEM ---

(defn htmx-notification-area
  "HTMX notification area for user feedback"
  []
  [:div {:id "notification-area"
         :class "fixed top-4 right-4 z-50 space-y-2"}])

;; --- LOADING INDICATORS ---

(defn htmx-loading-component
  "Beautiful loading indicator for HTMX requests"
  []
  [:div {:class "htmx-indicator"}
   [:div {:class "flex items-center justify-center p-4"}
    [:div {:class "animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"}]
    [:span {:class "ml-2 text-gray-600"} "Loading..."]]])