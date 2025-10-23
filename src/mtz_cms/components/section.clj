(ns mtz-cms.components.section
  "Section component - content blocks with optional images

   Architecture:
   - One component per file for clarity
   - Pure presentation - accepts data, returns Hiccup
   - No Alfresco calls, no business logic
   - Replaces components/sections.clj (plural → singular naming)"
  (:require
   [hiccup.core :refer [html]]
   [mtz-cms.ui.design-system :as ds]))

;; --- SECTION COMPONENTS ---

(defn section
  "Two-column section with text and optional image

   Data structure:
   {:section/subtitle \"About Our Community\"
    :section/body \"<p>HTML content here...</p>\"
    :section/image {:url \"/proxy/image/123\" :name \"Community\"}  ; optional
    :section/type :with-image | :text-only}                        ; optional

   Renders a section with:
   - Title (subtitle)
   - Body content (HTML, rendered safely)
   - Optional image on the right

   Used in: Content pages, about sections, feature explanations"
  [{:section/keys [subtitle body image type] :as data}]
  [:section {:class (ds/py :xl)}
   [:div {:class (ds/classes [(ds/container :7xl)
                              (ds/py :xl)])}
    [:div {:class (ds/classes ["grid grid-cols-1 gap-4 md:grid-cols-2 md:items-center"
                              (ds/gap :xl)])}

     ;; Text content column
     [:div
      [:div {:class "max-w-prose md:max-w-none"}
       ;; Title
       [:h2 {:class (ds/classes [(ds/text-size :2xl)
                                 (ds/font-weight :semibold)
                                 (ds/text :text-primary)
                                 "sm:text-3xl"])}
        (or subtitle "Section Title")]

       ;; Body content (HTML rendered safely)
       [:div {:class (ds/classes [(ds/mt :md)
                                 (ds/text :text-secondary)
                                 "prose prose-2xl max-w-none"])}
        [:div {:dangerouslySetInnerHTML {:__html (or body "")}}]]]]

     ;; Image column (only show if image exists)
     (when (and image (:url image))
       [:div
        [:img {:src (:url image)
               :class (ds/classes [(ds/rounded :md)
                                  (ds/shadow :lg)
                                  "w-full h-auto object-cover"])
               :alt (or (:name image) subtitle "")}]])]]])

(defn section-text-only
  "Section with just text, no image - full width

   Data structure:
   {:section/subtitle \"Our Mission\"
    :section/body \"<p>HTML content here...</p>\"}

   Renders a centered text section without image.

   Used in: Text-heavy pages, mission statements"
  [{:section/keys [subtitle body]}]
  [:section {:class (ds/py :xl)}
   [:div {:class (ds/classes [(ds/container :7xl)
                              (ds/py :xl)])}
    [:div {:class "max-w-3xl mx-auto"}
     ;; Title
     [:h2 {:class (ds/classes [(ds/text-size :2xl)
                               (ds/font-weight :semibold)
                               (ds/text :text-primary)
                               "sm:text-3xl"
                               (ds/mb :lg)])}
      (or subtitle "Section Title")]

     ;; Body content (HTML rendered safely)
     [:div {:class (ds/classes [(ds/text :text-secondary)
                               "prose prose-2xl max-w-none"])}
      [:div {:dangerouslySetInnerHTML {:__html (or body "")}}]]]]])

(defn section-with-background
  "Section with background color/image

   Data structure:
   {:section/subtitle \"Featured Content\"
    :section/body \"<p>HTML content here...</p>\"
    :section/background-color \"bg-blue-50\"         ; optional Tailwind class
    :section/background-image \"/images/bg.jpg\"}    ; optional

   Used in: Highlighted sections, call-to-action blocks"
  [{:section/keys [subtitle body background-color background-image] :as data}]
  [:section {:class (ds/classes ["py-12"
                                 (or background-color "bg-gray-50")])
             :style (when background-image
                     {:background-image (str "url(" background-image ")")
                      :background-size "cover"
                      :background-position "center"})}
   [:div {:class (ds/classes [(ds/container :7xl)
                              (ds/py :xl)])}
    [:div {:class "max-w-3xl mx-auto text-center"}
     ;; Title
     [:h2 {:class (ds/classes [(ds/text-size :3xl)
                               (ds/font-weight :bold)
                               (ds/text :text-primary)
                               "sm:text-4xl"
                               (ds/mb :lg)])}
      (or subtitle "Section Title")]

     ;; Body content
     [:div {:class (ds/classes [(ds/text :text-secondary)
                               "prose prose-2xl max-w-none mx-auto"])}
      [:div {:dangerouslySetInnerHTML {:__html (or body "")}}]]]]])

;; --- SMART RENDERER ---

(defn render-section
  "Smart renderer that picks the right section layout based on type

   Automatically selects appropriate section variant:
   - :text-only → section-text-only
   - :with-image → section (two-column)
   - :with-background → section-with-background
   - Default: auto-detect based on data

   Example:
     (render-section {:section/type :text-only
                     :section/subtitle \"About Us\"
                     :section/body \"<p>Welcome...</p>\"})"
  [data]
  (case (:section/type data)
    :text-only (section-text-only data)
    :with-image (section data)
    :with-background (section-with-background data)
    :placeholder [:div {:class (ds/classes [(ds/p :xl)
                                           (ds/text :text-muted)])}
                  "Section content loading..."]
    ;; Default: auto-detect based on data
    (cond
      (:section/background-color data) (section-with-background data)
      (:section/image data) (section data)
      :else (section-text-only data))))

;; --- BACKWARDS COMPATIBILITY ---

(defn section-component
  "DEPRECATED: Use `section` function instead.
   Kept for backwards compatibility during refactoring."
  [data]
  (section data))

(defn section-component-text-only
  "DEPRECATED: Use `section-text-only` function instead.
   Kept for backwards compatibility during refactoring."
  [data]
  (section-text-only data))

;; --- REPL TESTING ---

(comment
  ;; Test section with image
  (section {:section/subtitle "About Our Community"
            :section/body "<p>We are a welcoming community...</p>"
            :section/image {:url "/proxy/image/123" :name "Community"}})

  ;; Test text-only section
  (section-text-only {:section/subtitle "Our Mission"
                      :section/body "<p>To serve with love...</p>"})

  ;; Test section with background
  (section-with-background {:section/subtitle "Join Us"
                            :section/body "<p>All are welcome!</p>"
                            :section/background-color "bg-blue-50"})

  ;; Test smart renderer
  (render-section {:section/type :text-only
                   :section/subtitle "Welcome"
                   :section/body "<p>Hello world</p>"}))
