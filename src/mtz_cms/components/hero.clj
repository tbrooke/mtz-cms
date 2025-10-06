(ns mtz-cms.components.hero
  "Hero component - large banner at top of page

   NOTE: For HTMX dynamic pages, the hero is rendered by routes/api.clj
   This component is used by non-HTMX layouts via components/templates.clj wrappers

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

(defn simple-hero
  "Simplified hero component - backwards compatibility

   DEPRECATED: Use `hero` function instead.
   This wrapper exists for compatibility during refactoring."
  [hero-data]
  (hero hero-data))

(defn hero-carousel
  "Hero component with image carousel for dynamic HTMX loading

   Data structure:
   {:hero/title \"Mount Zion UCC\"
    :hero/images [{:url \"/api/image/123\" :alt \"Church\"}]
    :hero/content \"A progressive Christian community\"}

   Renders:
   - Title above carousel
   - Multi-image carousel with controls
   - Auto-rotation and manual navigation"
  [hero-data]
  (let [images (or (:hero/images hero-data) [])
        content (or (:hero/content hero-data) "A progressive Christian community welcoming all people.")
        random-index (if (seq images) (rand-int (count images)) 0)]
    [:div {:class "bg-white min-h-screen"}
     ;; Title above the image
     [:div {:class "mx-auto max-w-screen-xl px-4 pt-8 text-center"}
      [:h1 {:class "text-4xl font-extrabold sm:text-6xl text-gray-900"}
       "Mount Zion UCC"
       [:strong {:class "block font-extrabold text-blue-600 mt-2"} "United Church of Christ"]]]

     ;; Hero image carousel section
     (when (seq images)
       [:div {:class "relative"}
        ;; Image display
        [:div {:id "hero-carousel" :class "relative min-h-screen flex items-center justify-center"}
         (for [[idx img] (map-indexed vector images)]
           [:div {:key idx
                  :id (str "hero-image-" idx)
                  :class (str "hero-slide w-full absolute inset-0 "
                             (if (= idx random-index) "" "hidden"))
                  :style (str "background-image: url('" (:url img) "'); "
                             "background-size: contain; "
                             "background-position: center; "
                             "background-repeat: no-repeat; "
                             "min-height: 100vh;")}])]

        ;; Carousel controls
        [:div {:class "absolute bottom-8 left-0 right-0 flex items-center justify-center gap-6 z-10"}
         ;; Previous button
         [:button {:onclick "changeHeroImage(-1)"
                   :class "bg-white/80 hover:bg-white text-gray-800 rounded-full p-2 shadow-lg transition-all"}
          [:svg {:class "w-6 h-6" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
           [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M15 19l-7-7 7-7"}]]]

         ;; Dot indicators
         [:div {:class "flex gap-2"}
          (for [[idx _] (map-indexed vector images)]
            [:button {:key idx
                      :onclick (str "showHeroImage(" idx ")")
                      :id (str "hero-dot-" idx)
                      :class (str "w-3 h-3 rounded-full transition-all "
                                 (if (= idx random-index)
                                   "bg-blue-600 scale-125"
                                   "bg-white/60 hover:bg-white/80"))}])]

         ;; Next button
         [:button {:onclick "changeHeroImage(1)"
                   :class "bg-white/80 hover:bg-white text-gray-800 rounded-full p-2 shadow-lg transition-all"}
          [:svg {:class "w-6 h-6" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
           [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M9 5l7 7-7 7"}]]]]

        ;; JavaScript for carousel
        [:script
         (str "let currentHeroSlide = " random-index ";
const totalHeroSlides = " (count images) ";

function showHeroImage(index) {
  // Hide all slides
  document.querySelectorAll('.hero-slide').forEach(slide => {
    slide.classList.add('hidden');
  });

  // Update all dots
  document.querySelectorAll('[id^=\"hero-dot-\"]').forEach(dot => {
    dot.classList.remove('bg-blue-600', 'scale-125');
    dot.classList.add('bg-white/60');
  });

  // Show selected slide
  const selectedSlide = document.getElementById('hero-image-' + index);
  if (selectedSlide) {
    selectedSlide.classList.remove('hidden');
  }

  // Highlight selected dot
  const selectedDot = document.getElementById('hero-dot-' + index);
  if (selectedDot) {
    selectedDot.classList.remove('bg-white/60');
    selectedDot.classList.add('bg-blue-600', 'scale-125');
  }

  currentHeroSlide = index;
}

function changeHeroImage(direction) {
  let newIndex = currentHeroSlide + direction;

  // Wrap around
  if (newIndex < 0) {
    newIndex = totalHeroSlides - 1;
  } else if (newIndex >= totalHeroSlides) {
    newIndex = 0;
  }

  showHeroImage(newIndex);
}
")]])]))

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
