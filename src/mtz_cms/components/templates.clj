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
        (str/replace #"<[^>]+>" " ")  ; Strip HTML tags
        (str/replace #"\s+" " ")     ; Normalize whitespace
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