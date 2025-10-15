(ns mtz-cms.components.home-features
  "Homepage feature cards - clickable cards that link to detail pages"
  (:require
   [clojure.string :as str]))

;; --- UTILITY FUNCTIONS ---

(defn extract-text-from-html
  "Extract clean text from HTML for card descriptions"
  [html-content max-length]
  (when html-content
    (let [clean-text (-> html-content
                         (str/replace #"<[^>]+>" " ")
                         (str/replace #"\s+" " ")
                         str/trim)]
      (if (> (count clean-text) max-length)
        (str (subs clean-text 0 max-length) "...")
        clean-text))))

;; --- FEATURE CARD COMPONENT ---

(defn feature-card
  "Single feature card for homepage

   Data structure:
   {:feature/id \"feature1\"
    :feature/title \"Welcome to Mount Zion\"
    :feature/description \"Join us for worship...\"
    :feature/image {:url \"/proxy/image/123\" :alt \"Image\"}  ; optional
    :feature/link \"/features/feature1\"}

   Renders a tall card (height = 2x width) with:
   - Optional image at top (takes up more space in tall card)
   - Title
   - Description
   - Entire card is clickable"
  [feature-data]
  [:a {:href (:feature/link feature-data)
       :class "block group"}
   [:div {:class "bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-all duration-300 border-2 border-transparent group-hover:border-blue-500 flex flex-col"
          :style "height: 600px;"}  ;; Fixed height for tall cards

    ;; Image section (if available) - takes up upper portion of tall card
    (when-let [image (:feature/image feature-data)]
      [:div {:class "h-80 bg-gradient-to-br from-blue-50 to-blue-100 overflow-hidden flex-shrink-0"}
       [:img {:src (:url image)
              :alt (or (:alt image) (:feature/title feature-data))
              :class "w-full h-full object-cover"}]])

    ;; Content section - flexible to fill remaining space
    [:div {:class "p-6 flex flex-col flex-grow"}
     ;; Title
     [:h3 {:class "text-xl font-bold text-gray-900 mb-3 group-hover:text-blue-600 transition-colors"}
      (or (:feature/title feature-data) "Untitled Feature")]

     ;; Description - takes up available space
     [:p {:class "text-gray-600 mb-4 flex-grow"}
      (or (:feature/description feature-data)
          "Click to learn more about this feature.")]

     ;; Read more indicator - stays at bottom
     [:div {:class "flex items-center text-blue-600 font-medium text-sm mt-auto"}
      [:span "Learn more"]
      [:svg {:class "ml-2 w-4 h-4 group-hover:translate-x-1 transition-transform"
             :fill "none"
             :stroke "currentColor"
             :viewBox "0 0 24 24"}
       [:path {:stroke-linecap "round"
               :stroke-linejoin "round"
               :stroke-width "2"
               :d "M9 5l7 7-7 7"}]]]]]])

(defn placeholder-feature-card
  "Placeholder card for empty/loading features - tall format to match feature cards"
  [feature-id]
  [:div {:class "bg-white rounded-lg shadow-md overflow-hidden border-2 border-dashed border-gray-300 flex flex-col items-center justify-center"
         :style "height: 600px;"}  ;; Match tall card height
   [:div {:class "p-6 text-center"}
    [:div {:class "text-gray-400 mb-4"}
     [:svg {:class "w-16 h-16 mx-auto"
            :fill "none"
            :stroke "currentColor"
            :viewBox "0 0 24 24"}
      [:path {:stroke-linecap "round"
              :stroke-linejoin "round"
              :stroke-width "2"
              :d "M12 6v6m0 0v6m0-6h6m-6 0H6"}]]]
    [:h3 {:class "text-lg font-semibold text-gray-500 mb-2"}
     "Feature Coming Soon"]
    [:p {:class "text-sm text-gray-400"}
     "This feature will be added soon."]]])

;; --- FEATURE GRID LAYOUT ---

(defn features-grid
  "Grid of feature cards

   Takes a collection of feature data and renders them in a 3-column grid"
  [features]
  [:section {:class "py-16 bg-gray-50"}
   [:div {:class "mx-auto max-w-7xl px-6 lg:px-8"}

    ;; Section header
    [:div {:class "mx-auto max-w-2xl text-center mb-12"}
     [:h2 {:class "text-base font-semibold leading-7 text-blue-600"}
      "Discover Mount Zion"]
     [:p {:class "mt-2 text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl"}
      "What's Happening at Our Church"]]

    ;; Features grid
    [:div {:class "grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3"}
     (for [feature features]
       [:div {:key (or (:feature/id feature) (:feature/title feature))}
        (if (:feature/is-placeholder feature)
          (placeholder-feature-card (:feature/id feature))
          (feature-card feature))])]]])

;; --- HTMX DYNAMIC LOADING VERSION ---

(defn htmx-feature-card-container
  "HTMX container that loads a feature card dynamically

   This version loads the card content from the API endpoint"
  [node-id feature-slug]
  [:div {:class "htmx-feature-card"
         :hx-get (str "/api/features/card/" node-id)
         :hx-trigger "load"
         :hx-swap "innerHTML"}
   ;; Loading state - tall card format
   [:div {:class "bg-white rounded-lg shadow-md overflow-hidden animate-pulse flex flex-col"
          :style "height: 600px;"}
    [:div {:class "h-80 bg-gray-200 flex-shrink-0"}]
    [:div {:class "p-6 flex flex-col flex-grow"}
     [:div {:class "h-6 bg-gray-300 rounded w-3/4 mb-3"}]
     [:div {:class "h-4 bg-gray-200 rounded w-full mb-2"}]
     [:div {:class "h-4 bg-gray-200 rounded w-5/6 mb-2"}]
     [:div {:class "h-4 bg-gray-200 rounded w-2/3 mb-4"}]
     [:div {:class "h-4 bg-gray-200 rounded w-1/3 mt-auto"}]]]])

(defn htmx-features-grid
  "HTMX-powered features grid with dynamic loading"
  [feature-configs]
  [:section {:class "py-16 bg-gray-50"}
   [:div {:class "mx-auto max-w-7xl px-6 lg:px-8"}

    ;; Section header
    [:div {:class "mx-auto max-w-2xl text-center mb-12"}
     [:h2 {:class "text-base font-semibold leading-7 text-blue-600"}
      "Discover Mount Zion"]
     [:p {:class "mt-2 text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl"}
      "What's Happening at Our Church"]]

    ;; Features grid with HTMX loading
    [:div {:class "grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3"}
     (for [config feature-configs]
       [:div {:key (:node-id config)}
        (htmx-feature-card-container (:node-id config) (:slug config))])]]])

;; --- REPL TESTING ---

(comment
  ;; Test single feature card
  (feature-card {:feature/id "feature1"
                 :feature/title "Welcome to Mount Zion"
                 :feature/description "Join us for worship every Sunday at 10am. We are a welcoming progressive Christian community."
                 :feature/image {:url "/images/building.jpg" :alt "Church building"}
                 :feature/link "/features/welcome"})

  ;; Test grid with multiple features
  (features-grid [{:feature/id "feature1"
                   :feature/title "Sunday Worship"
                   :feature/description "Join us every Sunday at 10am for inspiring worship and fellowship."
                   :feature/link "/features/worship"}
                  {:feature/id "feature2"
                   :feature/title "Community Events"
                   :feature/description "Stay connected with upcoming church events and activities."
                   :feature/image {:url "/images/events.jpg"}
                   :feature/link "/features/events"}
                  {:feature/id "feature3"
                   :feature/is-placeholder true}]))
