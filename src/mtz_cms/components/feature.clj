(ns mtz-cms.components.feature
  "Feature component - highlights key content with optional image

   Architecture:
   - One component per file for clarity
   - Pure presentation - accepts data, returns Hiccup
   - No Alfresco calls, no business logic
   - Multiple variants for different layouts"
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

;; --- FEATURE COMPONENTS ---

(defn feature
  "Standard feature component with optional image

   Data structure:
   {:feature/title \"Worship Services\"
    :feature/content \"<p>Join us every Sunday...</p>\"
    :feature/image {:url \"/proxy/image/123\" :name \"Worship\"}  ; optional
    :feature/node-id \"abc123\"}

   Renders a feature box with:
   - Title (required)
   - Content text (optional, HTML will be stripped)
   - Image (optional, displayed below content)"
  [feature-data]
  [:div {:class "bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow"}
   ;; Title
   [:h3 {:class "text-xl font-semibold mb-4 text-gray-900"}
    (or (:feature/title feature-data) "Feature")]

   ;; Content
   [:div {:class "text-gray-600 mb-4"}
    (or (extract-text-from-html (:feature/content feature-data))
        "Feature content goes here")]

   ;; Image (if present)
   (when-let [image (:feature/image feature-data)]
     [:img {:src (str "http://localhost:8080" (:url image))
            :alt (or (:name image) (:feature/title feature-data))
            :class "w-full h-48 object-cover rounded"}])])

(defn feature-with-icon
  "Feature component with icon instead of image

   Data structure:
   {:feature/title \"Community\"
    :feature/content \"Building connections...\"
    :feature/icon \"👥\"}  ; emoji or icon class

   Used in: Landing pages, service highlights"
  [feature-data]
  [:div {:class "text-center"}
   ;; Icon
   (when-let [icon (:feature/icon feature-data)]
     [:div {:class "text-5xl mb-4"}
      icon])

   ;; Title
   [:h3 {:class "text-xl font-semibold mb-3 text-gray-900"}
    (or (:feature/title feature-data) "Feature")]

   ;; Content
   [:p {:class "text-gray-600"}
    (or (extract-text-from-html (:feature/content feature-data))
        "Feature content goes here")]])

(defn feature-with-image-left
  "Feature component with image on left side

   Data structure: Same as standard feature

   Used in: About pages, detailed features"
  [feature-data]
  [:div {:class "flex flex-col md:flex-row gap-6 bg-white p-6 rounded-lg shadow-md"}
   ;; Image (left side on desktop)
   (when-let [image (:feature/image feature-data)]
     [:div {:class "md:w-1/3"}
      [:img {:src (str "http://localhost:8080" (:url image))
             :alt (or (:name image) (:feature/title feature-data))
             :class "w-full h-full object-cover rounded"}]])

   ;; Content (right side on desktop)
   [:div {:class "md:w-2/3"}
    [:h3 {:class "text-2xl font-semibold mb-4 text-gray-900"}
     (or (:feature/title feature-data) "Feature")]
    [:div {:class "text-gray-600"}
     (or (extract-text-from-html (:feature/content feature-data))
         "Feature content goes here")]]])

(defn simple-feature
  "Simplified feature component - backwards compatibility

   DEPRECATED: Use `feature` function instead.
   This wrapper exists for compatibility during refactoring."
  [feature-data]
  (feature feature-data))

;; --- RENDER FUNCTION (for templates.clj compatibility) ---

(defn render-feature
  "Render a feature component - backwards compatibility

   DEPRECATED: Use `feature` function directly.
   This wrapper exists for compatibility during refactoring."
  [feature-data]
  (feature feature-data))

;; --- REPL TESTING ---

(comment
  ;; Test basic feature
  (feature {:feature/title "Worship Services"
            :feature/content "<p>Join us every Sunday at 10am</p>"})

  ;; Test with image
  (feature {:feature/title "Community Events"
            :feature/content "Regular fellowship gatherings"
            :feature/image {:url "/proxy/image/123" :name "Events"}})

  ;; Test with icon
  (feature-with-icon {:feature/title "Welcome"
                      :feature/content "All are welcome here"
                      :feature/icon "🏛️"})

  ;; Test image left layout
  (feature-with-image-left {:feature/title "Our Mission"
                            :feature/content "Serving the community..."
                            :feature/image {:url "/proxy/image/456" :name "Mission"}}))
