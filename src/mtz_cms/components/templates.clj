(ns mtz-cms.components.templates
  "Working component templates for Mount Zion CMS"
  (:require
   [clojure.string :as str]))

;; --- UTILITY FUNCTIONS ---

(defn extract-text-from-html
  "Extract clean text content from HTML"
  [html-content]
  (when html-content
    (-> html-content
        (str/replace #"<[^>]+>" " ") ; Strip HTML tags
        (str/replace #"\s+" " ") ; Normalize whitespace
        str/trim)))

;; --- SIMPLE WORKING COMPONENTS ---

(defn simple-hero
  "Simple hero component that works"
  [hero-data]
  [:section {:class "bg-blue-600 text-white py-16"}
   [:div {:class "max-w-7xl mx-auto px-4 text-center"}
    [:h1 {:class "text-4xl font-bold mb-4"}
     (or (:hero/title hero-data) "Welcome to Mount Zion UCC")]
    [:p {:class "text-xl mb-8"}
     (or (extract-text-from-html (:hero/content hero-data))
         "A progressive Christian community")]
    [:div {:class "space-x-4"}
     [:a {:href "/about" :class "bg-white text-blue-600 px-6 py-3 rounded-md font-semibold"}
      "Learn More"]
     [:a {:href "/worship" :class "border border-white text-white px-6 py-3 rounded-md font-semibold"}
      "Join Us"]]]])

(defn simple-feature
  "Simple feature component that works"
  [feature-data]
  [:div {:class "bg-white p-6 rounded-lg shadow-md"}
   [:h3 {:class "text-xl font-semibold mb-4 text-gray-900"}
    (or (:feature/title feature-data) "Feature")]
   [:div {:class "text-gray-600"}
    (or (extract-text-from-html (:feature/content feature-data))
        "Feature content goes here")]
   (when (:feature/image feature-data)
     [:img {:src (str "http://localhost:8080" (get-in feature-data [:feature/image :url]))
            :alt (get-in feature-data [:feature/image :name])
            :class "mt-4 w-full h-48 object-cover rounded"}])])

;; --- RENDER FUNCTIONS ---

(defn render-hero [hero-data]
  (simple-hero hero-data))

(defn render-feature [feature-data]
  (simple-feature feature-data))

(defn feature-card
  "Card component for grid layouts"
  [item-data]
  [:div {:class "bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"}
   ;; Image section if available
   (when (:image item-data)
     [:div {:class "h-48 bg-gray-200"}
      [:img {:src (str "http://localhost:8080" (get-in item-data [:image :url]))
             :alt (or (get-in item-data [:image :name]) (:title item-data))
             :class "w-full h-full object-cover"}]])

   ;; Content section
   [:div {:class "p-6"}
    ;; Title
    [:h3 {:class "text-lg font-semibold text-gray-900 mb-2"}
     (or (:title item-data) (:name item-data) "Untitled")]

    ;; Description or content
    [:p {:class "text-gray-600 mb-4 line-clamp-3"}
     (or (extract-text-from-html (:description item-data))
         (extract-text-from-html (:content item-data))
         "No description available")]

    ;; Optional metadata (date, category, etc.)
    (when (or (:date item-data) (:category item-data))
      [:div {:class "flex items-center text-sm text-gray-500 mb-4"}
       (when (:date item-data)
         [:span {:class "mr-4"} (:date item-data)])
       (when (:category item-data)
         [:span {:class "px-2 py-1 bg-blue-100 text-blue-700 rounded-full text-xs"}
          (:category item-data)])])

    ;; Call to action
    (when (:link item-data)
      [:a {:href (:link item-data)
           :class "inline-flex items-center text-blue-600 hover:text-blue-800 font-medium"}
       [:span "Learn more"]
       [:svg {:class "ml-2 w-4 h-4"
              :fill "none"
              :stroke "currentColor"
              :viewBox "0 0 24 24"}
        [:path {:stroke-linecap "round"
                :stroke-linejoin "round"
                :stroke-width "2"
                :d "M9 5l7 7-7 7"}]]])]])