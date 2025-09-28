(ns mtz-cms.components.templates
  "HyperUI-based component templates for Mount Zion CMS"
  (:require
   [clojure.string :as str]))

;; --- UTILITY FUNCTIONS ---

(defn clean-html
  "Clean HTML content for safe rendering"
  [html-content]
  (when html-content
    ;; Basic HTML cleaning - in production you'd want a proper HTML sanitizer
    (-> html-content
        (str/replace #"style=\"[^\"]*\"" "") ; Remove inline styles
        (str/replace #"<img[^>]*>" "")       ; Remove images for now
        str/trim)))

(defn extract-title-from-html
  "Extract title from HTML content"
  [html-content]
  (when html-content
    (or
     ;; Try to extract from h1 tag
     (when-let [match (re-find #"<h1[^>]*>([^<]+)</h1>" html-content)]
       (second match))
     ;; Try to extract from h2 tag
     (when-let [match (re-find #"<h2[^>]*>([^<]+)</h2>" html-content)]
       (second match))
     ;; Fallback to first strong text
     (when-let [match (re-find #"<strong>([^<]+)</strong>" html-content)]
       (second match)))))

(defn extract-text-from-html
  "Extract clean text content from HTML"
  [html-content]
  (when html-content
    (-> html-content
        (str/replace #"<[^>]+>" " ")  ; Strip HTML tags
        (str/replace #"\s+" " ")     ; Normalize whitespace
        str/trim)))

;; --- HERO COMPONENTS ---

(defn hero-with-image
  "Hero component with background image - HyperUI pattern"
  [hero-data]
  (let [title (or (:hero/title hero-data) "Welcome")
        content (:hero/content hero-data)
        image (:hero/image hero-data)
        clean-title (or (extract-title-from-html content) title)
        clean-text (extract-text-from-html content)]
    [:section {:class "relative bg-gradient-to-r from-blue-600 to-blue-800 text-white"}
     ;; Background image overlay
     (when image
       [:div {:class "absolute inset-0 bg-black/20"}])
     
     ;; Background image
     (when image
       [:img {:src (str "http://localhost:8080" (:url image))
              :alt (:name image)
              :class "absolute inset-0 h-full w-full object-cover"}])
     
     ;; Content overlay
     [:div {:class "relative mx-auto max-w-screen-xl px-4 py-32 sm:px-6 lg:flex lg:h-screen lg:items-center lg:px-8"}
      [:div {:class "max-w-xl text-center ltr:sm:text-left rtl:sm:text-right"}
       [:h1 {:class "text-3xl font-extrabold sm:text-5xl"}
        clean-title
        [:strong {:class "block font-extrabold text-blue-200"} "United Church of Christ"]]
       
       [:p {:class "mt-4 max-w-lg sm:text-xl/relaxed"}
        (or clean-text "A progressive Christian community welcoming all people.")]
       
       [:div {:class "mt-8 flex flex-wrap gap-4 text-center"}
        [:a {:href "/about"
             :class "block w-full rounded bg-blue-600 px-12 py-3 text-sm font-medium text-white shadow hover:bg-blue-700 focus:outline-none focus:ring active:bg-blue-500 sm:w-auto"}
         "Learn About Us"]
        
        [:a {:href "/worship"
             :class "block w-full rounded bg-white px-12 py-3 text-sm font-medium text-blue-600 shadow hover:bg-gray-100 focus:outline-none focus:ring active:bg-gray-200 sm:w-auto"}
         "Join Us for Worship"]]]]]))

(defn hero-text-only
  "Text-only hero component - HyperUI pattern"
  [hero-data]
  (let [title (or (:hero/title hero-data) "Welcome")
        content (:hero/content hero-data)
        clean-title (or (extract-title-from-html content) title)
        clean-text (extract-text-from-html content)]
    [:section {:class "bg-gradient-to-r from-blue-600 to-blue-800 text-white"}
     [:div {:class "mx-auto max-w-screen-xl px-4 py-32 lg:flex lg:h-screen lg:items-center"}
      [:div {:class "mx-auto max-w-3xl text-center"}
       [:h1 {:class "bg-gradient-to-r from-white to-blue-200 bg-clip-text text-3xl font-extrabold text-transparent sm:text-5xl"}
        clean-title
        [:span {:class "sm:block"} "Mount Zion UCC"]]
       
       [:p {:class "mx-auto mt-4 max-w-xl sm:text-xl/relaxed"}
        (or clean-text "A progressive Christian community in the heart of China Grove, North Carolina.")]
       
       [:div {:class "mt-8 flex flex-wrap justify-center gap-4"}
        [:a {:href "/about"
             :class "block w-full rounded border border-blue-600 bg-blue-600 px-12 py-3 text-sm font-medium text-white hover:bg-transparent hover:text-white focus:outline-none focus:ring active:text-opacity-75 sm:w-auto"}
         "Learn More"]
        
        [:a {:href "/contact"
             :class "block w-full rounded border border-blue-600 px-12 py-3 text-sm font-medium text-white hover:bg-blue-600 focus:outline-none focus:ring active:bg-blue-500 sm:w-auto"}
         "Get in Touch"]]]]]))

;; --- FEATURE COMPONENTS ---

(defn feature-with-image
  "Feature component with image - HyperUI pattern"
  [feature-data]
  (let [title (:feature/title feature-data)
        content (:feature/content feature-data)
        image (:feature/image feature-data)
        clean-title (or (extract-title-from-html content) title)
        clean-text (extract-text-from-html content)]
    [:section {:class "overflow-hidden bg-white py-8 sm:py-16"}
     [:div {:class "mx-auto max-w-7xl px-6 lg:px-8"}
      [:div {:class "mx-auto grid max-w-2xl grid-cols-1 gap-x-8 gap-y-16 sm:gap-y-20 lg:mx-0 lg:max-w-none lg:grid-cols-2"}
       [:div {:class "lg:pr-8 lg:pt-4"}
        [:div {:class "lg:max-w-lg"}
         [:h2 {:class "text-base font-semibold leading-7 text-blue-600"} "Mount Zion UCC"]
         [:p {:class "mt-2 text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl"}
          clean-title]
         [:div {:class "mt-6 text-lg leading-8 text-gray-600 prose"}
          ;; Render HTML content safely
          [:div {:dangerouslySetInnerHTML {:__html (clean-html content)}}]]
         [:div {:class "mt-8"}
          [:a {:href "/about"
               :class "inline-flex items-center gap-x-2 rounded-md bg-blue-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"}
           "Learn More"
           [:svg {:class "h-4 w-4" :fill "none" :viewBox "0 0 24 24" :stroke-width "1.5" :stroke "currentColor"}
            [:path {:stroke-linecap "round" :stroke-linejoin "round" :d "M4.5 12h15m0 0l-6.75-6.75M19.5 12l-6.75 6.75"}]]]]]]
       
       ;; Image section
       [:div {:class "flex items-start justify-end lg:order-first"}
        (if image
          [:img {:src (str "http://localhost:8080" (:url image))
                 :alt (:name image)
                 :class "w-[48rem] max-w-none rounded-xl shadow-xl ring-1 ring-gray-400/10 sm:w-[57rem]"}]
          [:div {:class "w-[48rem] max-w-none rounded-xl bg-gray-100 shadow-xl ring-1 ring-gray-400/10 sm:w-[57rem] h-96 flex items-center justify-center"}
           [:div {:class "text-gray-500 text-center"}
            [:svg {:class "mx-auto h-12 w-12 text-gray-400" :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
             [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"}]]
            [:p {:class "mt-2 text-sm text-gray-500"} "Image placeholder"]]])]]]))

(defn feature-text-only
  "Text-only feature component - HyperUI pattern"
  [feature-data]
  (let [title (:feature/title feature-data)
        content (:feature/content feature-data)
        clean-title (or (extract-title-from-html content) title)
        clean-text (extract-text-from-html content)]
    [:section {:class "bg-gray-50 py-8 sm:py-16"}
     [:div {:class "mx-auto max-w-7xl px-6 lg:px-8"}
      [:div {:class "mx-auto max-w-2xl lg:text-center"}
       [:h2 {:class "text-base font-semibold leading-7 text-blue-600"} "Mount Zion UCC"]
       [:p {:class "mt-2 text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl"}
        clean-title]
       [:div {:class "mt-6 text-lg leading-8 text-gray-600 prose max-w-none"}
        ;; Render HTML content safely
        [:div {:dangerouslySetInnerHTML {:__html (clean-html content)}}]]
       [:div {:class "mt-8"}
        [:a {:href "/about"
             :class "inline-flex items-center gap-x-2 rounded-md bg-blue-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-500"}
         "Learn More"]]]]]))

(defn feature-card
  "Card-style feature component - HyperUI pattern"
  [feature-data]
  (let [title (:feature/title feature-data)
        content (:feature/content feature-data)
        image (:feature/image feature-data)
        clean-title (or (extract-title-from-html content) title)
        clean-text (extract-text-from-html content)]
    [:div {:class "group relative block bg-black rounded-lg overflow-hidden"}
     ;; Image
     (if image
       [:img {:src (str "http://localhost:8080" (:url image))
              :alt (:name image)
              :class "absolute inset-0 h-full w-full object-cover opacity-75 transition-opacity group-hover:opacity-50"}]
       [:div {:class "absolute inset-0 h-full w-full bg-gradient-to-r from-blue-600 to-blue-800"}])
     
     ;; Content overlay
     [:div {:class "relative p-4 sm:p-6 lg:p-8"}
      [:p {:class "text-sm font-medium uppercase tracking-widest text-pink-500"} "Mount Zion UCC"]
      [:p {:class "text-xl font-bold text-white sm:text-2xl"} clean-title]
      [:div {:class "mt-32 sm:mt-48 lg:mt-64"}
       [:div {:class "translate-y-8 transform opacity-0 transition-all group-hover:translate-y-0 group-hover:opacity-100"}
        [:p {:class "text-sm text-white line-clamp-3"}
         (or clean-text "Click to learn more about this feature.")]
        [:div {:class "mt-4"}
         [:a {:href "#"
              :class "inline-block rounded bg-white px-4 py-2 text-sm font-semibold text-black hover:bg-gray-100"}
          "Learn More"]]]]]]))

;; --- COMPONENT RENDERER ---

(defn render-hero
  "Render hero component based on type and data"
  [hero-data]
  (let [has-image (some? (:hero/image hero-data))]
    (if has-image
      (hero-with-image hero-data)
      (hero-text-only hero-data))))

(defn render-feature
  "Render feature component based on type and data"
  [feature-data]
  (case (:feature/type feature-data)
    :feature-with-image (feature-with-image feature-data)
    :feature-text-only (feature-text-only feature-data)
    :feature-card (feature-card feature-data)
    ;; Default to text-only
    (feature-text-only feature-data)))