(ns mtz-cms.layouts.templates
  "Layout system for composing components"
  (:require
   [mtz-cms.components.templates :as templates]
   [mtz-cms.ui.components :as ui]))

;; --- LAYOUT DEFINITIONS ---

(defn hero-features-layout
  "Layout: Hero section followed by features in a grid"
  [page-data]
  (let [hero-data (:home/hero page-data)
        features-data (:home/features page-data)]
    [:div
     ;; Hero section
     (when hero-data
       (templates/render-hero hero-data))

     ;; Features section
     (when (seq features-data)
       [:section {:class "py-12 bg-white"}
        [:div {:class "mx-auto max-w-7xl px-6 lg:px-8"}
         [:div {:class "mx-auto max-w-2xl lg:text-center mb-12"}
          [:h2 {:class "text-base font-semibold leading-7 text-blue-600"} "Our Community"]
          [:p {:class "mt-2 text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl"}
           "What's Happening at Mount Zion"]]

         ;; Features grid
         [:div {:class "mx-auto mt-16 max-w-2xl sm:mt-20 lg:mt-24 lg:max-w-none"}
          [:dl {:class "grid max-w-xl grid-cols-1 gap-x-8 gap-y-16 lg:max-w-none lg:grid-cols-3"}
           (for [feature-data features-data]
             [:div {:key (:feature/node-id feature-data)
                    :class "flex flex-col"}
              (templates/render-feature feature-data)])]]]])

     ;; Call to action section
     [:section {:class "bg-blue-600"}
      [:div {:class "mx-auto max-w-7xl py-12 px-6 lg:px-8 lg:py-24"}
       [:div {:class "lg:grid lg:grid-cols-2 lg:gap-8 lg:items-center"}
        [:div
         [:h2 {:class "text-3xl font-bold tracking-tight text-white sm:text-4xl"}
          "Join Our Community"]
         [:p {:class "mt-3 max-w-3xl text-lg text-blue-100"}
          "Experience the warmth and fellowship of Mount Zion UCC. All are welcome in our progressive Christian community."]]
        [:div {:class "mt-8 lg:mt-0"}
         [:div {:class "inline-flex rounded-md shadow"}
          [:a {:href "/worship"
               :class "inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-blue-600 bg-white hover:bg-blue-50"}
           "Plan Your Visit"]]
         [:div {:class "ml-3 inline-flex"}
          [:a {:href "/contact"
               :class "inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-500 hover:bg-blue-400"}
           "Contact Us"]]]]]]]))

(defn simple-content-layout
  "Layout: Simple content with sidebar"
  [page-data]
  [:div {:class "mx-auto max-w-7xl px-6 lg:px-8 py-12"}
   [:div {:class "lg:grid lg:grid-cols-3 lg:gap-8"}
    ;; Main content
    [:div {:class "lg:col-span-2"}
     [:div {:class "prose prose-lg max-w-none"}
      (:page/content page-data)]]

    ;; Sidebar
    [:div {:class "mt-12 lg:mt-0 lg:col-span-1"}
     [:div {:class "bg-gray-50 rounded-lg p-6"}
      [:h3 {:class "text-lg font-semibold text-gray-900 mb-4"} "Quick Links"]
      [:ul {:class "space-y-3"}
       [:li [:a {:href "/worship" :class "text-blue-600 hover:text-blue-800"} "Worship Services"]]
       [:li [:a {:href "/events" :class "text-blue-600 hover:text-blue-800"} "Upcoming Events"]]
       [:li [:a {:href "/contact" :class "text-blue-600 hover:text-blue-800"} "Contact Information"]]
       [:li [:a {:href "/about" :class "text-blue-600 hover:text-blue-800"} "About Our Church"]]]]]]])

(defn cards-grid-layout
  "Layout: Grid of cards"
  [page-data]
  (let [items (:page/items page-data)]
    [:div {:class "bg-gray-50 py-12"}
     [:div {:class "mx-auto max-w-7xl px-6 lg:px-8"}
      [:div {:class "mx-auto max-w-2xl lg:text-center mb-12"}
       [:h2 {:class "text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl"}
        (:page/title page-data)]
       [:p {:class "mt-4 text-lg text-gray-600"}
        (:page/description page-data)]]

      [:div {:class "grid gap-6 sm:grid-cols-2 lg:grid-cols-3"}
       (for [item items]
         [:div {:key (:id item)}
          (templates/feature-card item)])]]]))  ; <- Fixed: added the missing closing parenthesis

;; --- LAYOUT SELECTOR ---

(defn render-layout
  "Render page with the specified layout"
  [layout-type page-data]
  (case layout-type
    :hero-features-layout (hero-features-layout page-data)
    :simple-content-layout (simple-content-layout page-data)
    :cards-grid-layout (cards-grid-layout page-data)
    ;; Default layout
    (simple-content-layout page-data)))

;; --- LAYOUT REGISTRY ---

(def available-layouts
  "Registry of available layouts for Alfresco selection"
  {:hero-features-layout
   {:name "Hero + Features"
    :description "Large hero section followed by feature grid"
    :components [:hero :features]
    :suitable-for [:home :landing]}

   :simple-content-layout
   {:name "Simple Content"
    :description "Clean content layout with sidebar"
    :components [:content :sidebar]
    :suitable-for [:about :pages]}

   :cards-grid-layout
   {:name "Cards Grid"
    :description "Grid of card components"
    :components [:cards]
    :suitable-for [:events :ministries :news]}})