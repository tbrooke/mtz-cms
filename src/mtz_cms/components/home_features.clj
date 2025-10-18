(ns mtz-cms.components.home-features
  "Homepage feature cards - clickable cards that link to detail pages"
  (:require
   [clojure.string :as str]
   [mtz-cms.ui.design-system :as ds]))

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

(defn- feature-card-image-only
  "Render feature card with only an image (graphic-only mode)"
  [feature-data]
  (let [image (:feature/image feature-data)]
    [:a {:href (:feature/link feature-data)
         :class "block group"}
     [:div {:class (ds/classes [(ds/rounded :lg)
                                (ds/shadow :md)
                                "overflow-hidden"
                                (ds/hover-shadow :xl)
                                (ds/transition :all)
                                (ds/duration :normal)
                                "border-2 border-transparent"
                                "group-hover:border-blue-500"
                                "relative"])
            :style "height: 1200px;"}
      ;; Full-height image
      [:img {:src (:url image)
             :alt (or (:alt image) (:feature/title feature-data) "Feature image")
             :class "w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"}]

      ;; Optional overlay with title (if title exists)
      (when (:feature/title feature-data)
        [:div {:class "absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent p-6"}
         [:h3 {:class "text-white text-2xl font-bold"}
          (:feature/title feature-data)]])]]))

(defn- feature-card-text-only
  "Render feature card with only text (no image)"
  [feature-data]
  [:a {:href (:feature/link feature-data)
       :class "block group"}
   [:div {:class (ds/classes [(ds/bg :bg-card)
                              (ds/rounded :lg)
                              (ds/shadow :md)
                              "overflow-hidden"
                              (ds/hover-shadow :xl)
                              (ds/transition :all)
                              (ds/duration :normal)
                              "border-2 border-transparent"
                              "group-hover:border-blue-500"
                              "flex flex-col"
                              "bg-gradient-to-br from-blue-50 via-white to-blue-50"])
          :style "height: 1200px;"}

    [:div {:class (ds/classes [(ds/p :2xl) "flex flex-col h-full justify-center"])}
     ;; Title
     [:h3 {:class (ds/classes [(ds/text-size :3xl)
                               (ds/font-weight :bold)
                               (ds/text :text-primary)
                               (ds/mb :lg)
                               "group-hover:text-blue-600"
                               (ds/transition :colors)])}
      (or (:feature/title feature-data) "Untitled Feature")]

     ;; Description - larger text for text-only cards
     [:p {:class (ds/classes [(ds/text :text-secondary)
                             (ds/text-size :lg)
                             (ds/mb :xl)
                             "leading-relaxed"])}
      (or (:feature/description feature-data)
          "Click to learn more about this feature.")]

     ;; Read more indicator
     [:div {:class (ds/classes ["flex items-center"
                               (ds/text :primary)
                               (ds/font-weight :medium)
                               (ds/text-size :base)
                               "mt-auto"])}
      [:span "Learn more"]
      [:svg {:class (ds/classes ["ml-2 w-5 h-5"
                                "group-hover:translate-x-1"
                                (ds/transition :transform)])
             :fill "none"
             :stroke "currentColor"
             :viewBox "0 0 24 24"}
       [:path {:stroke-linecap "round"
               :stroke-linejoin "round"
               :stroke-width "2"
               :d "M9 5l7 7-7 7"}]]]]]])

(defn- feature-card-image-and-text
  "Render feature card with both image and text (standard mode)"
  [feature-data]
  (let [image (:feature/image feature-data)]
    [:a {:href (:feature/link feature-data)
         :class "block group"}
     [:div {:class (ds/classes [(ds/bg :bg-card)
                                (ds/rounded :lg)
                                (ds/shadow :md)
                                "overflow-hidden"
                                (ds/hover-shadow :xl)
                                (ds/transition :all)
                                (ds/duration :normal)
                                "border-2 border-transparent"
                                "group-hover:border-blue-500"
                                "flex flex-col"])
            :style "height: 1200px;"}

      ;; Image section - takes up majority of card
      [:div {:class "bg-gradient-to-br from-blue-50 to-blue-100 overflow-hidden flex-shrink-0"
             :style "height: 900px;"}
       [:img {:src (:url image)
              :alt (or (:alt image) (:feature/title feature-data))
              :class "w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"}]]

      ;; Content section - flexible to fill remaining space
      [:div {:class (ds/classes [(ds/p :lg) "flex flex-col flex-grow"])}
       ;; Title
       [:h3 {:class (ds/classes [(ds/text-size :xl)
                                 (ds/font-weight :bold)
                                 (ds/text :text-primary)
                                 (ds/mb :sm)
                                 "group-hover:text-blue-600"
                                 (ds/transition :colors)])}
        (or (:feature/title feature-data) "Untitled Feature")]

       ;; Description - takes up available space
       [:p {:class (ds/classes [(ds/text :text-secondary)
                               (ds/mb :md)
                               "flex-grow"])}
        (or (:feature/description feature-data)
            "Click to learn more about this feature.")]

       ;; Read more indicator - stays at bottom
       [:div {:class (ds/classes ["flex items-center"
                                 (ds/text :primary)
                                 (ds/font-weight :medium)
                                 (ds/text-size :sm)
                                 "mt-auto"])}
        [:span "Learn more"]
        [:svg {:class (ds/classes ["ml-2 w-4 h-4"
                                  "group-hover:translate-x-1"
                                  (ds/transition :transform)])
               :fill "none"
               :stroke "currentColor"
               :viewBox "0 0 24 24"}
         [:path {:stroke-linecap "round"
                 :stroke-linejoin "round"
                 :stroke-width "2"
                 :d "M9 5l7 7-7 7"}]]]]]]))

(defn placeholder-feature-card
  "Placeholder card for empty/loading features - tall format to match feature cards"
  [feature-id]
  [:div {:class (ds/classes [(ds/bg :bg-card)
                             (ds/rounded :lg)
                             (ds/shadow :md)
                             "overflow-hidden"
                             "border-2 border-dashed"
                             (ds/border-color :border-default)
                             "flex flex-col items-center justify-center"])
         :style "height: 1200px;"}  ;; Match tall card height
   [:div {:class (ds/classes [(ds/p :lg) "text-center"])}
    [:div {:class (ds/classes [(ds/text :text-muted)
                              (ds/mb :md)])}
     [:svg {:class "w-16 h-16 mx-auto"
            :fill "none"
            :stroke "currentColor"
            :viewBox "0 0 24 24"}
      [:path {:stroke-linecap "round"
              :stroke-linejoin "round"
              :stroke-width "2"
              :d "M12 6v6m0 0v6m0-6h6m-6 0H6"}]]]
    [:h3 {:class (ds/classes [(ds/text-size :lg)
                              (ds/font-weight :semibold)
                              (ds/text :text-secondary)
                              (ds/mb :sm)])}
     "Feature Coming Soon"]
    [:p {:class (ds/classes [(ds/text-size :sm)
                            (ds/text :text-muted)])}
     "This feature will be added soon."]]])

(defn feature-card
  "Single feature card for homepage - intelligently renders based on content type

   Data structure:
   {:feature/id \"feature1\"
    :feature/title \"Welcome to Mount Zion\"
    :feature/description \"Join us for worship...\"
    :feature/image {:url \"/proxy/image/123\" :alt \"Image\"}  ; optional
    :feature/link \"/features/feature1\"}

   Rendering logic:
   - Image only: Full-height image card (1200px)
   - Text only: Text-focused card with gradient background
   - Both: Image (900px) + text (300px) standard layout
   - Neither: Falls back to placeholder"
  [feature-data]
  (let [has-image (and (:feature/image feature-data)
                       (:url (:feature/image feature-data)))
        has-text (or (:feature/title feature-data)
                     (:feature/description feature-data))]
    (cond
      ;; Both image and text
      (and has-image has-text) (feature-card-image-and-text feature-data)

      ;; Image only
      has-image (feature-card-image-only feature-data)

      ;; Text only
      has-text (feature-card-text-only feature-data)

      ;; Neither - show placeholder
      :else (placeholder-feature-card (:feature/id feature-data)))))

;; --- FEATURE GRID LAYOUT ---

(defn- render-feature
  "Render a single feature with its wrapper div"
  [feature]
  [:div {:key (or (:feature/id feature) (:feature/title feature))}
   (if (:feature/is-placeholder feature)
     (placeholder-feature-card (:feature/id feature))
     (feature-card feature))])

(defn- single-feature-layout
  "Layout for a single feature - full width centered"
  [feature]
  [:div {:class "max-w-4xl mx-auto"}
   (render-feature feature)])

(defn- two-feature-layout
  "Layout for two features - side by side"
  [features]
  [:div {:class (ds/classes ["grid grid-cols-1 md:grid-cols-2"
                             (ds/gap :xl)])}
   (for [feature features]
     (render-feature feature))])

(defn- multi-feature-layout
  "Layout for 3+ features - three columns with natural wrapping"
  [features]
  [:div {:class (ds/classes ["grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3"
                             (ds/gap :xl)])}
   (for [feature features]
     (render-feature feature))])

(defn- features-section-header
  "Header for features section"
  []
  [:div {:class (ds/classes ["mx-auto max-w-2xl text-center"
                            (ds/mb :3xl)])}
   [:h2 {:class (ds/classes [(ds/text-size :base)
                             (ds/font-weight :semibold)
                             "leading-7"
                             (ds/text :primary)])}
    "Discover Mount Zion"]
   [:p {:class (ds/classes [(ds/mt :sm)
                           (ds/text-size :3xl)
                           (ds/font-weight :bold)
                           "tracking-tight"
                           (ds/text :text-primary)
                           "sm:text-4xl"])}
    "What's Happening at Our Church"]])

(defn features-grid
  "Grid of feature cards with dynamic layout based on count

   - 1 feature: Full width centered
   - 2 features: Side by side
   - 3+ features: Three columns (wraps naturally for 4+)

   Matches Hero component behavior where layout adapts to content count."
  [features]
  (let [feature-count (count features)]
    [:section {:class (ds/classes [(ds/py :4xl)
                                   (ds/bg :bg-page)])}
     [:div {:class (ds/classes [(ds/container :7xl)])}

      ;; Section header
      (features-section-header)

      ;; Features display - dynamic based on count
      (cond
        (= feature-count 1) (single-feature-layout (first features))
        (= feature-count 2) (two-feature-layout features)
        :else (multi-feature-layout features))]]))

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
   [:div {:class (ds/classes [(ds/bg :bg-card)
                              (ds/rounded :lg)
                              (ds/shadow :md)
                              "overflow-hidden animate-pulse flex flex-col"])
          :style "height: 1200px;"}
    [:div {:class "bg-gray-200 flex-shrink-0"
           :style "height: 900px;"}]
    [:div {:class (ds/classes [(ds/p :lg) "flex flex-col flex-grow"])}
     [:div {:class "h-6 bg-gray-300 rounded w-3/4 mb-3"}]
     [:div {:class "h-4 bg-gray-200 rounded w-full mb-2"}]
     [:div {:class "h-4 bg-gray-200 rounded w-5/6 mb-2"}]
     [:div {:class "h-4 bg-gray-200 rounded w-2/3 mb-4"}]
     [:div {:class "h-4 bg-gray-200 rounded w-1/3 mt-auto"}]]]])

(defn- htmx-single-feature-layout
  "HTMX layout for a single feature - full width centered"
  [config]
  [:div {:class "max-w-4xl mx-auto"}
   [:div {:key (:node-id config)}
    (htmx-feature-card-container (:node-id config) (:slug config))]])

(defn- htmx-two-feature-layout
  "HTMX layout for two features - side by side"
  [configs]
  [:div {:class (ds/classes ["grid grid-cols-1 md:grid-cols-2"
                             (ds/gap :xl)])}
   (for [config configs]
     [:div {:key (:node-id config)}
      (htmx-feature-card-container (:node-id config) (:slug config))])])

(defn- htmx-multi-feature-layout
  "HTMX layout for 3+ features - three columns with natural wrapping"
  [configs]
  [:div {:class (ds/classes ["grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3"
                             (ds/gap :xl)])}
   (for [config configs]
     [:div {:key (:node-id config)}
      (htmx-feature-card-container (:node-id config) (:slug config))])])

(defn htmx-features-grid
  "HTMX-powered features grid with dynamic loading and dynamic layout

   - 1 feature: Full width centered
   - 2 features: Side by side
   - 3+ features: Three columns (wraps naturally for 4+)

   Matches the features-grid dynamic layout behavior."
  [feature-configs]
  (let [feature-count (count feature-configs)]
    [:section {:class (ds/classes [(ds/py :4xl)
                                   (ds/bg :bg-page)])}
     [:div {:class (ds/classes [(ds/container :7xl)])}

      ;; Section header
      (features-section-header)

      ;; Features display - dynamic based on count
      (cond
        (= feature-count 1) (htmx-single-feature-layout (first feature-configs))
        (= feature-count 2) (htmx-two-feature-layout feature-configs)
        :else (htmx-multi-feature-layout feature-configs))]]))

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
