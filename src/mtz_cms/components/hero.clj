(ns mtz-cms.components.hero
  "Hero component - large banner at top of page with 16:9 images

   Architecture:
   - One component per file for clarity
   - Pure presentation - accepts data, returns Hiccup
   - No Alfresco calls, no business logic
   - Fixed layout (no carousel) - displays 1 or 2 images in 16:9 format"
  (:require
   [clojure.string :as str]))

;; --- UTILITY FUNCTIONS ---

(defn extract-text-from-html
  "Extract clean text content from HTML

   Strips HTML tags and normalizes whitespace for clean display."
  [html-content]
  (when html-content
    (-> html-content
        (str/replace #"<[^>]+>" " ")     ; Strip HTML tags
        (str/replace #"\s+" " ")         ; Normalize whitespace
        str/trim)))

;; --- HERO IMAGE CARD ---

(defn hero-image-card
  "Single hero image card with title, description, and link

   Data structure:
   {:url \"/api/image/123\"
    :id \"image-123\"
    :name \"Church Building\"
    :title \"Welcome to Mount Zion\"      ; from image cm:title
    :description \"Join us for worship\"  ; from image cm:description
    :link \"/hero/image-123\"}

   Renders a 16:9 image card with:
   - 16:9 aspect ratio image
   - Title (H2) centered below
   - Description text
   - 'click for more' link"
  [image-data]
  [:div {:class "flex flex-col group"}
   ;; 16:9 Image container
   [:a {:href (:link image-data (str "/hero/" (:id image-data)))
        :class "block"}
    [:div {:class "relative w-full overflow-hidden rounded-lg"
           :style "padding-bottom: 56.25%;"} ;; 16:9 aspect ratio
     [:img {:src (:url image-data)
            :alt (:title image-data (:name image-data))
            :class "absolute inset-0 w-full h-full object-cover"}]]]

   ;; Content below image
   [:div {:class "mt-4 text-center"}
    ;; Title (H2) - from cm:title property, not filename
    [:h2 {:class "text-2xl font-bold text-gray-900 mb-2"}
     (or (:title image-data) "Untitled")]

    ;; Description - from cm:description property
    (when-let [desc (:description image-data)]
      (when (not (empty? desc))
        [:p {:class "text-gray-600 mb-3"}
         desc]))

    ;; "Click for more" link
    [:a {:href (:link image-data (str "/hero/" (:id image-data)))
         :class "inline-block text-blue-600 hover:text-blue-800 text-sm font-medium"}
     "...click for more →"]]])

;; --- MAIN HERO COMPONENT ---

(defn hero-carousel
  "Fixed hero component - displays 1 or 2 images in 16:9 format (NO carousel)

   Data structure:
   {:hero/title \"Mount Zion UCC\"
    :hero/images [{:id \"123\"
                   :url \"/api/image/123\"
                   :name \"Church\"
                   :title \"Welcome\"        ; from cm:title
                   :description \"Join us\"}] ; from cm:description
    :hero/content \"A progressive Christian community\"}

   Renders:
   - Site title at top
   - 1 image: Single large 16:9 image (full width)
   - 2 images: Two side-by-side 16:9 images
   - Title, description, and 'click for more' below each image

   NOTE: Only 1 or 2 images allowed (enforced by schema)
   NOTE: Named hero-carousel for backwards compatibility, but NO carousel"
  [hero-data]
  (let [images (or (:hero/images hero-data) [])
        image-count (count images)]
    [:div {:class "bg-white py-8"}
     ;; Welcome message at top
     [:div {:class "mx-auto max-w-screen-xl px-4 pb-8 text-center"}
      [:p {:class "text-xl sm:text-2xl text-gray-800 max-w-4xl mx-auto leading-relaxed"}
       "We are a family oriented church located in China Grove. If you're looking for a place to call home, please come join us one Sunday morning or at one of our community events. Maybe you'll find that Mount Zion is the family you've been looking for."]]

     ;; Hero images section - ONLY 1 or 2 images
     (when (seq images)
       [:div {:class "mx-auto max-w-screen-xl px-4"}
        (cond
          ;; Single image - large full width display (max-w-6xl = 1152px)
          (= image-count 1)
          [:div {:class "max-w-6xl mx-auto"}
           (hero-image-card (first images))]

          ;; Two images - side by side
          (= image-count 2)
          [:div {:class "grid grid-cols-1 md:grid-cols-2 gap-8"}
           (for [[idx img] (map-indexed vector images)]
             [:div {:key (or (:id img) idx)}
              (hero-image-card img)])]

          ;; More than 2 images - ERROR (should be caught by schema)
          :else
          [:div {:class "max-w-4xl mx-auto bg-yellow-50 border-2 border-yellow-300 rounded-lg p-6 text-center"}
           [:p {:class "text-yellow-800 font-semibold"}
            "⚠️ Warning: Hero component has " image-count " images"]
           [:p {:class "text-yellow-700 text-sm mt-2"}
            "Only 1 or 2 images are allowed. Please remove extra images from the Hero folder in Alfresco."]])])]))

;; --- BACKWARDS COMPATIBILITY ---

(defn hero
  "DEPRECATED: Basic hero banner component

   Use hero-carousel instead for image-based heroes.
   This function is kept for backwards compatibility."
  [hero-data]
  [:section {:class (str "relative py-16 "
                        (if (:hero/image hero-data)
                          "bg-cover bg-center bg-gray-900"
                          "bg-white"))}
   ;; Background image overlay if present
   (when (:hero/image hero-data)
     [:div {:class "absolute inset-0 bg-black opacity-50"}])

   ;; Content container
   [:div {:class (str "relative max-w-7xl mx-auto px-4 text-center "
                     (if (:hero/image hero-data)
                       "text-white"
                       "text-gray-900"))}
    ;; Title
    [:h1 {:class "text-4xl md:text-5xl lg:text-6xl font-bold mb-4"}
     (or (:hero/title hero-data) "Welcome to Mount Zion UCC")]

    ;; Content (strip HTML for clean text display)
    (when (:hero/content hero-data)
      [:p {:class "text-xl md:text-2xl mb-8 max-w-3xl mx-auto"}
       (extract-text-from-html (:hero/content hero-data))])

    ;; Call-to-action buttons
    (when (or (:hero/cta-primary hero-data) (:hero/cta-secondary hero-data))
      [:div {:class "flex flex-col sm:flex-row justify-center gap-4"}
       ;; Primary CTA
       (when-let [cta (:hero/cta-primary hero-data)]
         [:a {:href (:href cta "/about")
              :class "bg-white text-blue-600 px-8 py-3 rounded-md font-semibold hover:bg-gray-100 transition-colors"}
          (:text cta "Learn More")])

       ;; Secondary CTA
       (when-let [cta (:hero/cta-secondary hero-data)]
         [:a {:href (:href cta "/worship")
              :class "border-2 border-white text-white px-8 py-3 rounded-md font-semibold hover:bg-white hover:text-blue-600 transition-colors"}
          (:text cta "Join Us")])])]])

(def simple-hero hero)
(def hero-fixed hero-carousel)

;; --- REPL TESTING ---

(comment
  ;; Test single image
  (hero-carousel {:hero/images [{:id "img1"
                                 :url "/api/image/123"
                                 :name "Church Building"
                                 :title "Welcome to Mount Zion"
                                 :description "Join us for worship every Sunday"
                                 :link "/hero/img1"}]})

  ;; Test two images
  (hero-carousel {:hero/images [{:id "img1"
                                 :url "/api/image/123"
                                 :title "Sunday Worship"
                                 :description "Join us every Sunday at 10am"}
                                {:id "img2"
                                 :url "/api/image/456"
                                 :title "Community Events"
                                 :description "Stay connected with our church family"}]})

  ;; Test image card
  (hero-image-card {:id "test-123"
                    :url "/api/image/123"
                    :name "Church"
                    :title "Welcome"
                    :description "Join us for worship"}))
