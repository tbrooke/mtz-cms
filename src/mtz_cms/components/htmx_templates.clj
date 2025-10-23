(ns mtz-cms.components.htmx-templates
  "HTMX-enhanced component templates for Mount Zion CMS"
  (:require
   [mtz-cms.ui.design-system :as ds]
   [clojure.string :as str]))

;; --- HTMX-ENHANCED COMPONENTS ---
;; NOTE: Hero component is in routes/api.clj (HTMX endpoint)
;; This file contains feature and page editor components only

;; Helper functions for HTMX feature component
(defn- feature-edit-button
  "Edit button overlay for feature component"
  [node-id]
  [:button {:class (ds/classes ["absolute -top-2 -right-2"
                                "opacity-0 group-hover:opacity-100"
                                (ds/bg :primary)
                                (ds/text :text-on-primary)
                                (ds/rounded :full)
                                "w-8 h-8 flex items-center justify-center"
                                (ds/transition :opacity)])
            :hx-get (str "/api/components/feature/" node-id "/edit")
            :hx-target (str "#feature-" node-id)
            :title "Edit this feature"}
   "✏️"])

(defn- feature-learn-more-button
  "Learn more button with arrow"
  []
  [:div {:class (ds/mt :xl)}
   [:button {:class (ds/classes ["inline-flex items-center gap-x-2"
                                 (ds/rounded :md)
                                 (ds/bg :primary)
                                 "px-3.5 py-2.5"
                                 (ds/text-size :sm)
                                 (ds/font-weight :semibold)
                                 (ds/text :text-on-primary)
                                 (ds/shadow :sm)
                                 (ds/hover-bg :primary-dark)])
             :hx-get "/about"
             :hx-target "#main-content"
             :hx-push-url "true"}
    "Learn More"
    [:svg {:class "h-4 w-4" :fill "none" :viewBox "0 0 24 24" :stroke-width "1.5" :stroke "currentColor"}
     [:path {:stroke-linecap "round" :stroke-linejoin "round" :d "M4.5 12h15m0 0l-6.75-6.75M19.5 12l-6.75 6.75"}]]]])

(defn- feature-image-placeholder
  "Placeholder for missing image with upload button"
  [node-id]
  [:div {:class (ds/classes ["w-[48rem] max-w-none"
                             (ds/rounded :xl)
                             "bg-gray-100"
                             (ds/shadow :xl)
                             "ring-1 ring-gray-400/10"
                             "sm:w-[57rem] h-96"
                             "flex flex-col items-center justify-center"])}
   [:button {:class (ds/classes [(ds/text :text-muted)
                                 "hover:text-blue-600"
                                 "cursor-pointer"])
             :hx-get (str "/api/components/feature/" node-id "/upload-image")
             :hx-target (str "#feature-" node-id)}
    [:svg {:class "mx-auto h-12 w-12" :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
     [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"}]]
    [:p {:class (ds/classes [(ds/mt :sm)
                            (ds/text-size :sm)])}
     "Click to add image"]]])

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
         [:div {:class "mt-6 text-lg leading-8 text-gray-600 prose prose-2xl"
                :hx-get (str "/api/components/feature/" node-id "/content")
                :hx-trigger "every 60s" ; Refresh content periodically
                :hx-swap "innerHTML"}
          [:div (or content "")]]

         [:div {:class "mt-8"}
          [:button {:class "inline-flex items-center gap-x-2 rounded-md bg-blue-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-500"
                    :hx-get "/about"
                    :hx-target "#main-content"
                    :hx-push-url "true"}
           "Learn More"
           [:svg {:class "h-4 w-4" :fill "none" :viewBox "0 0 24 24" :stroke-width "1.5" :stroke "currentColor"}
            [:path {:stroke-linecap "round" :stroke-linejoin "round" :d "M4.5 12h15m0 0l-6.75-6.75M19.5 12l-6.75 6.75"}]]]]]]

       ;; Image with lazy loading
       [:div {:class "flex items-start justify-end lg:order-first"}
        (if image
          [:img {:src (str "http://localhost:8080" (:url image))
                 :alt (:name image)
                 :class "w-[48rem] max-w-none rounded-xl shadow-xl ring-1 ring-gray-400/10 sm:w-[57rem]"
                 :loading "lazy"}]
          [:div {:class "w-[48rem] max-w-none rounded-xl bg-gray-100 shadow-xl ring-1 ring-gray-400/10 sm:w-[57rem] h-96 flex flex-col items-center justify-center"}
           [:button {:class "text-gray-500 hover:text-blue-600 cursor-pointer"
                     :hx-get (str "/api/components/feature/" node-id "/upload-image")
                     :hx-target (str "#feature-" node-id)}
            [:svg {:class "mx-auto h-12 w-12" :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
             [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"}]]
            [:p {:class "mt-2 text-sm"} "Click to add image"]]])]]]]))

(defn htmx-dynamic-components-container
  "Container that can dynamically load and reorder components"
  [page-data]
  (let [components (:home/features page-data)]
    [:div {:class "space-y-12"
           :id "components-container"
           :hx-get "/api/components/refresh"
           :hx-trigger "refreshComponents from:body"}

     ;; Component management toolbar
     [:div {:class (ds/classes [(ds/bg :secondary-lighter)
                                "border-l-4"
                                "border-blue-500"
                                (ds/p :md)
                                (ds/mb :xl)])}
      [:div {:class "flex items-center justify-between"}
       [:div
        [:h3 {:class (ds/classes [(ds/text-size :lg)
                                  (ds/font-weight :medium)
                                  (ds/text :text-primary)])}
         "Page Components"]
        [:p {:class (ds/classes [(ds/text-size :sm)
                                (ds/text :text-secondary)])}
         "Components will refresh automatically as content changes"]]
       [:div {:class "flex gap-2"}
        [:button {:class (ds/button :primary {:size :md})
                  :hx-get "/api/components/add"
                  :hx-target "#components-container"
                  :hx-swap "beforeend"}
         "+ Add Component"]
        [:button {:class (ds/button :secondary {:size :md})
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
   [:div {:class (ds/classes [(ds/bg :bg-card)
                              (ds/rounded :lg)
                              (ds/shadow :lg)
                              (ds/border)
                              (ds/border-color :border-default)
                              (ds/p :md)])}
    [:h4 {:class (ds/classes [(ds/font-weight :semibold)
                              (ds/text :text-primary)
                              (ds/mb :md)])}
     "Page Editor"]
    [:div {:class "space-y-2"}
     [:button {:class (ds/classes ["w-full text-left"
                                   (ds/px :md)
                                   (ds/py :sm)
                                   (ds/text-size :sm)
                                   "bg-blue-50 hover:bg-blue-100"
                                   (ds/rounded :md)])
               :hx-get "/api/page/edit-mode"
               :hx-target "body"
               :hx-swap "outerHTML"}
      "✏️ Edit Mode"]
     [:button {:class (ds/classes ["w-full text-left"
                                   (ds/px :md)
                                   (ds/py :sm)
                                   (ds/text-size :sm)
                                   "bg-green-50 hover:bg-green-100"
                                   (ds/rounded :md)])
               :hx-post "/api/page/publish"
               :hx-target "#notification-area"}
      "🚀 Publish Changes"]
     [:button {:class (ds/classes ["w-full text-left"
                                   (ds/px :md)
                                   (ds/py :sm)
                                   (ds/text-size :sm)
                                   (ds/bg :secondary-lighter)
                                   "hover:bg-gray-100"
                                   (ds/rounded :md)])
               :hx-get "/api/page/preview"
               :hx-target "_blank"}
      "👁️ Preview"]]]])

;; --- HTMX LAYOUT WITH PARTIAL UPDATES ---
;; NOTE: The actual htmx-hero-features-layout is in components/htmx.clj
;; This file only contains feature and editor components

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
   [:div {:class (ds/classes ["flex items-center justify-center"
                              (ds/p :md)])}
    [:div {:class (ds/classes ["animate-spin"
                               (ds/rounded :full)
                               "h-8 w-8 border-b-2"
                               "border-blue-600"])}]
    [:span {:class (ds/classes ["ml-2"
                               (ds/text :text-secondary)])}
     "Loading..."]]])