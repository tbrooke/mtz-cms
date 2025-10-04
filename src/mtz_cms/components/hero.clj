(ns mtz-cms.components.hero
  "Hero component - large banner at top of page

   Architecture:
   - One component per file for clarity
   - Pure presentation - accepts data, returns Hiccup
   - No Alfresco calls, no business logic
   - Utility functions for text processing included"
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

;; --- HERO COMPONENT ---

(defn hero
  "Hero banner component for page headers

   Data structure:
   {:hero/title \"Welcome to Mount Zion UCC\"
    :hero/content \"<p>A progressive Christian community</p>\"
    :hero/image \"/path/to/image.jpg\"     ; optional
    :hero/cta-primary {:text \"Learn More\" :href \"/about\"}  ; optional
    :hero/cta-secondary {:text \"Join Us\" :href \"/worship\"}} ; optional

   Renders a large banner with:
   - Title (required)
   - Content text (optional, HTML will be stripped)
   - Background image (optional)
   - Up to 2 call-to-action buttons (optional)"
  [hero-data]
  [:section {:class (str "relative py-16 "
                        (if (:hero/image hero-data)
                          "bg-cover bg-center bg-gray-900"
                          "bg-blue-600"))}
   ;; Background image overlay if present
   (when (:hero/image hero-data)
     [:div {:class "absolute inset-0 bg-black opacity-50"}])

   ;; Content container
   [:div {:class "relative max-w-7xl mx-auto px-4 text-center text-white"}
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

(defn simple-hero
  "Simplified hero component - backwards compatibility

   DEPRECATED: Use `hero` function instead.
   This wrapper exists for compatibility during refactoring."
  [hero-data]
  (hero hero-data))

;; --- REPL TESTING ---

(comment
  ;; Test basic hero
  (hero {:hero/title "Welcome to Mount Zion UCC"
         :hero/content "A progressive Christian community"})

  ;; Test with CTAs
  (hero {:hero/title "Join Our Community"
         :hero/content "<p>Experience the warmth and fellowship</p>"
         :hero/cta-primary {:text "Learn More" :href "/about"}
         :hero/cta-secondary {:text "Visit Us" :href "/worship"}})

  ;; Test with background image
  (hero {:hero/title "Welcome"
         :hero/content "A community of faith"
         :hero/image "/images/hero-bg.jpg"}))
