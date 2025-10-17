(ns mtz-cms.components.hero
  "Hero component - large banner at top of page with 16:9 images

   Architecture:
   - One component per file for clarity
   - Pure presentation - accepts data, returns Hiccup
   - No Alfresco calls, no business logic
   - Fixed layout (no carousel) - displays 1 or 2 images in 16:9 format"
  (:require
   [clojure.string :as str]
   [mtz-cms.ui.design-system :as ds]))

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
    [:div {:class (ds/classes ["relative w-full overflow-hidden"
                               (ds/rounded :lg)])
           :style "padding-bottom: 56.25%;"} ;; 16:9 aspect ratio
     [:img {:src (:url image-data)
            :alt (:title image-data (:name image-data))
            :class "absolute inset-0 w-full h-full object-cover"}]]]

   ;; Content below image
   [:div {:class (ds/classes [(ds/mt :md) "text-center"])}
    ;; Title (H2) - from cm:title property, not filename
    [:h2 {:class (ds/classes [(ds/text-size :2xl)
                              (ds/font-weight :bold)
                              (ds/text :text-primary)
                              (ds/mb :sm)])}
     (or (:title image-data) "Untitled")]

    ;; Description - from cm:description property
    (when-let [desc (:description image-data)]
      (when (not (empty? desc))
        [:p {:class (ds/classes [(ds/text :text-secondary)
                                (ds/mb :sm)])}
         desc]))

    ;; "Click for more" link
    [:a {:href (:link image-data (str "/hero/" (:id image-data)))
         :class (ds/classes ["inline-block"
                            (ds/text :primary)
                            (ds/hover-text :primary-dark)
                            (ds/text-size :sm)
                            (ds/font-weight :medium)])}
     "...click for more →"]]])

;; --- HELPER LAYOUT FUNCTIONS ---

(defn- hero-welcome-message
  "Welcome message displayed at top of hero section"
  []
  [:div {:class (ds/classes [(ds/container :7xl)
                             "pb-8"
                             "text-center"])}
   [:p {:class (ds/classes [(ds/text-size :xl)
                            "sm:text-2xl"
                            (ds/text :text-primary)
                            "max-w-4xl mx-auto leading-relaxed"])}
    "We are a family oriented church located in China Grove. If you're looking for a place to call home, please come join us one Sunday morning or at one of our community events. Maybe you'll find that Mount Zion is the family you've been looking for."]])

(defn- hero-single-image-layout
  "Layout for single hero image - full width"
  [image]
  [:div {:class "max-w-6xl mx-auto"}
   (hero-image-card image)])

(defn- hero-two-image-layout
  "Layout for two hero images - side by side"
  [images]
  [:div {:class (ds/classes ["grid grid-cols-1 md:grid-cols-2"
                             (ds/gap :xl)])}
   (for [[idx img] (map-indexed vector images)]
     [:div {:key (or (:id img) idx)}
      (hero-image-card img)])])

(defn- hero-error-message
  "Error message when too many images in hero"
  [image-count]
  [:div {:class (ds/classes ["max-w-4xl mx-auto"
                             (ds/alert :warning)
                             "text-center"])}
   [:p {:class (ds/font-weight :semibold)}
    "⚠️ Warning: Hero component has " image-count " images"]
   [:p {:class (ds/classes [(ds/text-size :sm)
                           (ds/mt :sm)])}
    "Only 1 or 2 images are allowed. Please remove extra images from the Hero folder in Alfresco."]])

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
    [:div {:class (ds/classes [(ds/bg :bg-page)
                               (ds/py :xl)])}
     ;; Welcome message at top
     (hero-welcome-message)

     ;; Hero images section - ONLY 1 or 2 images
     (when (seq images)
       [:div {:class (ds/container :7xl)}
        (cond
          (= image-count 1) (hero-single-image-layout (first images))
          (= image-count 2) (hero-two-image-layout images)
          :else (hero-error-message image-count))])]))

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
