(ns mtz-cms.components.card
  "Card component - compact content display for grids

   Architecture:
   - One component per file for clarity
   - Pure presentation - accepts data, returns Hiccup
   - No Alfresco calls, no business logic
   - Multiple card variants for different use cases
   - Uses design system for consistent styling"
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

;; --- CARD HELPER COMPONENTS ---

(defn- card-image
  "Card image section"
  [image item-data]
  [:div {:class "h-48 bg-gray-200"}
   [:img {:src (str "http://localhost:8080" (:url image))
          :alt (or (:name image) (:title item-data))
          :class "w-full h-full object-cover"}]])

(defn- card-title
  "Card title element"
  [item-data]
  [:h3 {:class (ds/classes [(ds/text-size :lg)
                            (ds/font-weight :semibold)
                            (ds/text :text-primary)
                            (ds/mb :sm)])}
   (or (:title item-data) (:name item-data) "Untitled")])

(defn- card-description
  "Card description with HTML stripping and truncation"
  [item-data]
  [:p {:class (ds/classes [(ds/text :text-secondary)
                          (ds/mb :md)
                          "line-clamp-3"])}
   (or (extract-text-from-html (:description item-data))
       (extract-text-from-html (:content item-data))
       "No description available")])

(defn- card-metadata
  "Card metadata section (date, category)"
  [item-data]
  (when (or (:date item-data) (:category item-data))
    [:div {:class (ds/classes ["flex items-center"
                              (ds/text-size :sm)
                              (ds/text :text-secondary)
                              (ds/mb :md)])}
     (when (:date item-data)
       [:span {:class "mr-4"} (:date item-data)])
     (when (:category item-data)
       [:span {:class (ds/classes [(ds/px :sm)
                                  "py-1"
                                  "bg-blue-100"
                                  "text-blue-700"
                                  (ds/rounded :full)
                                  (ds/text-size :xs)])}
        (:category item-data)])]))

(defn- card-link
  "Card call-to-action link with arrow"
  [link]
  [:a {:href link
       :class (ds/classes ["inline-flex items-center"
                          (ds/text :primary)
                          (ds/hover-text :primary-dark)
                          (ds/font-weight :medium)])}
   [:span "Learn more"]
   [:svg {:class "ml-2 w-4 h-4"
          :fill "none"
          :stroke "currentColor"
          :viewBox "0 0 24 24"}
    [:path {:stroke-linecap "round"
            :stroke-linejoin "round"
            :stroke-width "2"
            :d "M9 5l7 7-7 7"}]]])

;; --- CARD COMPONENTS ---

(defn card
  "Standard card component for grid displays

   Data structure:
   {:title \"Event Name\"              ; or :name
    :description \"Event details...\"   ; or :content
    :image {:url \"/proxy/image/123\" :name \"Event\"}  ; optional
    :date \"2025-10-15\"                ; optional
    :category \"Events\"                ; optional
    :link \"/events/details\"           ; optional
    :id \"event-123\"}

   Renders a card with:
   - Image at top (optional)
   - Title (required)
   - Description/content (optional, HTML stripped, truncated to 3 lines)
   - Metadata: date, category (optional)
   - Link/CTA (optional)

   Used in: Event listings, ministry cards, news grids"
  [item-data]
  [:div {:class (ds/classes [(ds/bg :bg-card)
                             (ds/rounded :lg)
                             (ds/shadow :md)
                             "overflow-hidden"
                             (ds/hover-shadow :lg)
                             (ds/transition :shadow)])}
   ;; Image section (if available)
   (when-let [image (:image item-data)]
     (card-image image item-data))

   ;; Content section
   [:div {:class (ds/p :lg)}
    (card-title item-data)
    (card-description item-data)
    (card-metadata item-data)

    ;; Call to action link
    (when (:link item-data)
      (card-link (:link item-data)))]])

(defn simple-card
  "Simplified card without image - text only

   Data structure:
   {:title \"Ministry Name\"
    :description \"Ministry description...\"
    :link \"/ministries/details\"}

   Used in: Simple listings, text-heavy content"
  [item-data]
  [:div {:class (ds/classes [(ds/bg :bg-card)
                             (ds/rounded :lg)
                             (ds/shadow :sm)
                             (ds/border)
                             (ds/border-color :border-default)
                             (ds/p :lg)
                             "hover:border-blue-300"
                             (ds/transition :colors)])}
   (card-title item-data)

   [:p {:class (ds/classes [(ds/text :text-secondary)
                           (ds/mb :md)])}
    (or (extract-text-from-html (:description item-data))
        (extract-text-from-html (:content item-data))
        "No description available")]

   (when (:link item-data)
     [:a {:href (:link item-data)
          :class (ds/classes [(ds/text :primary)
                             (ds/hover-text :primary-dark)
                             (ds/font-weight :medium)])}
      "Learn more →"])])

(defn icon-card
  "Card with icon instead of image

   Data structure:
   {:title \"Service Name\"
    :description \"Service description...\"
    :icon \"⛪\"                        ; emoji or icon class
    :link \"/services/details\"}

   Used in: Service highlights, feature cards"
  [item-data]
  [:div {:class (ds/classes [(ds/bg :bg-card)
                             (ds/rounded :lg)
                             (ds/shadow :md)
                             (ds/p :lg)
                             "text-center"
                             (ds/hover-shadow :lg)
                             (ds/transition :shadow)])}
   ;; Icon
   (when-let [icon (:icon item-data)]
     [:div {:class (ds/classes [(ds/text-size :5xl)
                               (ds/mb :md)])}
      icon])

   ;; Title
   [:h3 {:class (ds/classes [(ds/text-size :lg)
                             (ds/font-weight :semibold)
                             (ds/text :text-primary)
                             (ds/mb :sm)])}
    (or (:title item-data) (:name item-data) "Untitled")]

   ;; Description
   [:p {:class (ds/classes [(ds/text :text-secondary)
                           (ds/mb :md)])}
    (or (extract-text-from-html (:description item-data))
        (extract-text-from-html (:content item-data))
        "No description available")]

   ;; Link
   (when (:link item-data)
     [:a {:href (:link item-data)
          :class (ds/classes [(ds/text :primary)
                             (ds/hover-text :primary-dark)
                             (ds/font-weight :medium)])}
      "Learn more →"])])

(defn feature-card
  "Feature card variant - backwards compatibility

   DEPRECATED: Use `card` function instead.
   This wrapper exists for compatibility during refactoring."
  [item-data]
  (card item-data))

;; --- REPL TESTING ---

(comment
  ;; Test standard card
  (card {:title "Sunday Service"
         :description "Join us for worship every Sunday at 10am"
         :image {:url "/proxy/image/123" :name "Worship"}
         :date "2025-10-15"
         :category "Events"
         :link "/events/sunday-service"})

  ;; Test simple card (no image)
  (simple-card {:title "Bible Study"
                :description "Weekly Bible study on Wednesdays"
                :link "/ministries/bible-study"})

  ;; Test icon card
  (icon-card {:title "Community Outreach"
              :description "Serving our local community"
              :icon "🤝"
              :link "/ministries/outreach"}))
