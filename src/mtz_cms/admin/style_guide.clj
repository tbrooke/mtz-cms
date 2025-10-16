(ns mtz-cms.admin.style-guide
  "Living Style Guide for Mount Zion CMS

   This is an interactive documentation page showing all design system
   components, tokens, and usage examples.

   Purpose:
   - Visual documentation of design tokens
   - Interactive component examples
   - Code snippets for developers
   - Design consistency reference
   - Onboarding tool for new developers"
  (:require
   [mtz-cms.ui.design-system :as ds]
   [mtz-cms.components.primitives :as primitives]
   [mtz-cms.components.card :as card]))

;; =============================================================================
;; UTILITY FUNCTIONS
;; =============================================================================

(defn code-example
  "Render a code example with syntax highlighting.

   Args:
     code-str - String of Clojure code to display"
  [code-str]
  [:pre {:class (ds/classes [(ds/bg :gray-50)
                             (ds/border)
                             (ds/border-color :border-default)
                             (ds/rounded :md)
                             (ds/p :md)
                             (ds/text-size :sm)
                             "overflow-x-auto"])}
   [:code {:class "text-gray-900 font-mono"}
    code-str]])

(defn section
  "Style guide section with heading and content.

   Args:
     title   - Section title
     content - Hiccup content vector"
  [title content]
  [:section {:class (ds/classes [(ds/mb :3xl)])}
   [:h2 {:class (ds/heading 2)}
    title]
   [:div {:class (ds/mt :lg)}
    content]])

(defn subsection
  "Subsection within a section."
  [title content]
  [:div {:class (ds/classes [(ds/mb :xl)])}
   [:h3 {:class (ds/heading 3)}
    title]
   [:div {:class (ds/mt :md)}
    content]])

(defn example-box
  "Box to display component examples with optional code."
  [component & [code-str]]
  [:div {:class (ds/classes [(ds/border)
                             (ds/border-color :border-default)
                             (ds/rounded :lg)
                             (ds/mb :lg)])}
   ;; Visual example
   [:div {:class (ds/classes [(ds/p :lg)
                              (ds/bg :white)
                              "border-b"
                              (ds/border-color :border-default)])}
    component]

   ;; Code example (if provided)
   (when code-str
     [:div {:class (ds/p :md)}
      (code-example code-str)])])

;; =============================================================================
;; COLOR SWATCHES
;; =============================================================================

(defn color-swatch
  "Display a single color swatch with details.

   Args:
     label      - Display name
     color-key  - Semantic color keyword
     usage-note - When to use this color"
  [label color-key usage-note]
  [:div {:class (ds/classes [(ds/border)
                             (ds/border-color :border-default)
                             (ds/rounded :lg)
                             "overflow-hidden"])}
   ;; Color block
   [:div {:class (str (ds/bg color-key) " h-24")}]

   ;; Details
   [:div {:class (ds/p :md)}
    [:p {:class (ds/classes [(ds/font-weight :semibold)
                            (ds/text :text-primary)
                            (ds/mb :sm)])}
     label]
    [:p {:class (ds/classes [(ds/text-size :xs)
                            (ds/text :text-muted)
                            (ds/mb :sm)])}
     (str ":" (name color-key))]
    [:p {:class (ds/classes [(ds/text-size :sm)
                            (ds/text :text-secondary)])}
     usage-note]]])

(defn colors-section
  "Display all semantic colors with swatches."
  []
  (section
   "Color Palette"
   [:div
    [:p {:class (ds/classes [(ds/text :text-secondary)
                            (ds/mb :lg)])}
     "Semantic color tokens organized by purpose. Use these instead of raw Tailwind colors for consistency."]

    ;; Custom OKLCH Mint Palette
    (subsection
     "Custom OKLCH Mint Palette"
     [:div
      [:p {:class (ds/classes [(ds/text :text-secondary)
                              (ds/mb :md)
                              (ds/text-size :sm)])}
       "Custom OKLCH mint/teal palette. Mint Light uses hue 180.72° (cyan) for the header/background. Other mint colors use hue 166.113° (teal) for buttons and accents."]
      [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-6"}
       (color-swatch "Mint Light" :mint-light "Backgrounds, header, subtle accents")
       (color-swatch "Mint Accent" :mint-accent "Highlights, hover backgrounds")
       (color-swatch "Mint Primary" :mint-primary "Buttons, links, primary actions")
       (color-swatch "Mint Dark" :mint-dark "Hover states, active elements")]])

    ;; Brand Colors
    (subsection
     "Brand Colors (Semantic Mappings)"
     [:div
      [:p {:class (ds/classes [(ds/text :text-secondary)
                              (ds/mb :md)
                              (ds/text-size :sm)])}
       "These semantic names map to the OKLCH mint palette above. Use these for consistency."]
      [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-6"}
       (color-swatch "Primary" :primary "→ Mint Primary")
       (color-swatch "Primary Light" :primary-light "→ Mint Accent")
       (color-swatch "Primary Dark" :primary-dark "→ Mint Dark")
       (color-swatch "Warm Accent" :secondary-warm "Complementary warm tone")]])

    ;; Status Colors
    (subsection
     "Status Colors"
     [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-6"}
      (color-swatch "Success" :success "Success messages, confirmations")
      (color-swatch "Error" :error "Error messages, warnings")
      (color-swatch "Warning" :warning "Caution messages")
      (color-swatch "Info" :info "Informational messages")])

    ;; Text Colors
    (subsection
     "Text Colors"
     [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-6"}
      (color-swatch "Primary Text" :text-primary "Main content text")
      (color-swatch "Secondary Text" :text-secondary "Less important text")
      (color-swatch "Muted Text" :text-muted "Subtle text, captions")
      (color-swatch "Text on Dark" :text-on-dark "Text on dark backgrounds")])

    ;; Background Colors
    (subsection
     "Background Colors"
     [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-6"}
      (color-swatch "Page Background" :bg-page "Main page background")
      (color-swatch "Card Background" :bg-card "Cards, panels")
      (color-swatch "Hover Background" :bg-hover "Interactive hover states")
      (color-swatch "Header Background (Mint)" :bg-header "Navigation header - custom OKLCH mint color")])

    ;; Usage example
    [:div {:class (ds/mt :xl)}
     [:h4 {:class (ds/heading 4)} "Usage Example"]
     (code-example
      ";; Using semantic colors (now mapped to mint OKLCH palette)
(ds/text :primary)            ; => \"text-mint-primary\"
(ds/bg :primary)              ; => \"bg-mint-primary\"
(ds/bg :bg-header)            ; => \"bg-mint-light\"
(ds/hover-bg :primary-dark)   ; => \"hover:bg-mint-dark\"

;; Status colors remain unchanged
(ds/bg :error-bg)             ; => \"bg-red-50\"
(ds/text :success)            ; => \"text-green-700\"")]]))

;; =============================================================================
;; TYPOGRAPHY SECTION
;; =============================================================================

(defn typography-section
  "Display typography scale and examples."
  []
  (section
   "Typography"
   [:div
    ;; Headings
    (subsection
     "Headings"
     [:div {:class "space-y-6"}
      (for [level (range 1 7)]
        [:div {:key level}
         [:h1 {:class (ds/heading level)}
          (str "Heading " level " - The quick brown fox")]
         [:p {:class (ds/classes [(ds/text-size :sm)
                                 (ds/text :text-muted)
                                 (ds/mt :sm)])}
          (str "(heading " level ")")]])])

    ;; Text Sizes
    (subsection
     "Text Sizes"
     [:div {:class "space-y-4"}
      (for [[size-key size-class] (sort-by key ds/typography-scale)]
        [:div {:key size-key}
         [:p {:class size-class}
          (str (name size-key) " - The quick brown fox jumps over the lazy dog")]
         [:p {:class (ds/classes [(ds/text-size :xs)
                                 (ds/text :text-muted)])}
          (str "(text-size " size-key ")")]])])

    ;; Font Weights
    (subsection
     "Font Weights"
     [:div {:class "space-y-4"}
      (for [[weight-key weight-class] (sort-by key ds/font-weights)]
        [:div {:key weight-key}
         [:p {:class (str weight-class " " (ds/text-size :lg))}
          (str (name weight-key) " - The quick brown fox")]
         [:p {:class (ds/classes [(ds/text-size :xs)
                                 (ds/text :text-muted)])}
          (str "(font-weight " weight-key ")")]])])

    ;; Font Families
    (subsection
     "Font Families"
     [:div {:class "space-y-6"}
      ;; EB Garamond
      [:div
       [:h4 {:class (str (ds/font-family :garamond) " " (ds/text-size :3xl))}
        "EB Garamond - Elegant Serif"]
       [:p {:class (ds/classes [(ds/text-size :sm) (ds/text :text-muted)])}
        "(font-family :garamond) - Use for elegant headings and decorative text"]
       [:p {:class (str (ds/font-family :garamond) " " (ds/text-size :base) " " (ds/mt :sm))}
        "The quick brown fox jumps over the lazy dog. ABCDEFGHIJKLMNOPQRSTUVWXYZ 1234567890"]]

      ;; IBM Plex Sans Menu
      [:div
       [:h4 {:class (str (ds/font-family :menu) " " (ds/text-size :lg))}
        "Navigation Menu Font"]
       [:p {:class (ds/classes [(ds/text-size :sm) (ds/text :text-muted)])}
        "(font-family :menu) - IBM Plex Sans with uppercase styling for navigation"]
       [:p {:class (str (ds/font-family :menu) " " (ds/mt :sm))}
        "Home • About • Services • Contact"]]

      ;; IBM Plex Sans
      [:div
       [:h4 {:class (str (ds/font-family :ibm-plex) " " (ds/text-size :2xl))}
        "IBM Plex Sans - Clean Sans-Serif"]
       [:p {:class (ds/classes [(ds/text-size :sm) (ds/text :text-muted)])}
        "(font-family :ibm-plex) - Use for UI elements and clean headings"]
       [:p {:class (str (ds/font-family :ibm-plex) " " (ds/text-size :base) " " (ds/mt :sm))}
        "The quick brown fox jumps over the lazy dog. ABCDEFGHIJKLMNOPQRSTUVWXYZ 1234567890"]]

      ;; Source Serif 4
      [:div
       [:h4 {:class (str "font-serif " (ds/text-size :2xl))}
        "Source Serif 4 - Body Text (Default)"]
       [:p {:class (ds/classes [(ds/text-size :sm) (ds/text :text-muted)])}
        "Default body font - Readable serif for long-form content"]
       [:p {:class (str "font-serif " (ds/text-size :base) " " (ds/mt :sm))}
        "The quick brown fox jumps over the lazy dog. This is the default body font used throughout the site for optimal readability in long-form content."]]])

    ;; Usage example
    [:div {:class (ds/mt :xl)}
     [:h4 {:class (ds/heading 4)} "Usage Examples"]
     (code-example
      ";; Typography utilities
(ds/heading 1)            ; => \"text-4xl font-extrabold text-gray-900\"
(ds/text-size :lg)        ; => \"text-lg\"
(ds/font-weight :bold)    ; => \"font-bold\"
(ds/font-family :garamond) ; => \"font-garamond\"
(ds/font-family :menu)    ; => \"font-menu\"")]]))

;; =============================================================================
;; SPACING SECTION
;; =============================================================================

(defn spacing-section
  "Display spacing scale with visual examples."
  []
  (section
   "Spacing"
   [:div
    [:p {:class (ds/classes [(ds/text :text-secondary)
                            (ds/mb :lg)])}
     "Spacing scale for padding, margins, and gaps. Use semantic keywords for consistency."]

    ;; Spacing Scale
    (subsection
     "Spacing Scale"
     [:div {:class "space-y-4"}
      (for [[size-key _] (sort-by key ds/spacing-scale)]
        [:div {:key size-key
               :class "flex items-center gap-4"}
         [:div {:class (str "h-8 bg-blue-200 " (ds/px size-key))}]
         [:span {:class (ds/text-size :sm)}
          (str (name size-key) " - " (ds/px size-key))]])])

    ;; Usage example
    [:div {:class (ds/mt :xl)}
     [:h4 {:class (ds/heading 4)} "Usage Examples"]
     (code-example
      ";; Spacing utilities
(ds/px :lg)   ; => \"px-6\"
(ds/py :md)   ; => \"py-4\"
(ds/mb :xl)   ; => \"mb-8\"
(ds/gap :md)  ; => \"gap-4\"
(ds/p :lg)    ; => \"p-6\"")]]))

;; =============================================================================
;; BUTTONS SECTION
;; =============================================================================

(defn buttons-section
  "Display all button variants and sizes."
  []
  (section
   "Buttons"
   [:div
    ;; Button Variants
    (subsection
     "Button Variants"
     [:div {:class "space-y-6"}
      ;; Primary
      (example-box
       [:button {:class (ds/button :primary)}
        "Primary Button"]
       "(ds/button :primary)")

      ;; Secondary
      (example-box
       [:button {:class (ds/button :secondary)}
        "Secondary Button"]
       "(ds/button :secondary)")

      ;; Warm Accent
      (example-box
       [:button {:class (ds/button :warm)}
        "Warm Accent Button"]
       "(ds/button :warm)")

      ;; Danger
      (example-box
       [:button {:class (ds/button :danger)}
        "Danger Button"]
       "(ds/button :danger)")

      ;; Link style
      (example-box
       [:button {:class (ds/button :link)}
        "Link Button"]
       "(ds/button :link)")])

    ;; Button Sizes
    (subsection
     "Button Sizes"
     [:div {:class "flex flex-wrap items-center gap-4"}
      [:button {:class (ds/button :primary {:size :sm})}
       "Small"]
      [:button {:class (ds/button :primary {:size :md})}
       "Medium"]
      [:button {:class (ds/button :primary {:size :lg})}
       "Large"]])

    ;; Disabled State
    (subsection
     "Disabled State"
     [:button {:class (ds/button :primary {:disabled? true})}
      "Disabled Button"])

    ;; Using primitives component
    (subsection
     "Using Primitives Component"
     [:div {:class "space-y-6"}
      (example-box
       (primitives/button {:text "Primary Button" :variant :primary})
       "(primitives/button {:text \"Primary Button\" :variant :primary})")

      (example-box
       (primitives/button {:text "Learn More" :href "/about" :variant :secondary})
       "(primitives/button {:text \"Learn More\" :href \"/about\" :variant :secondary})")])]))

;; =============================================================================
;; CARDS SECTION
;; =============================================================================

(defn cards-section
  "Display card component variants."
  []
  (section
   "Cards"
   [:div
    ;; Standard Card
    (subsection
     "Standard Card"
     (example-box
      [:div {:class (ds/card)}
       [:h3 {:class (ds/heading 3)} "Card Title"]
       [:p {:class (ds/text :text-secondary)}
        "This is a standard card with default styling. It includes padding, shadow, and hover effects."]]
      "(ds/card)"))

    ;; Card with Custom Options
    (subsection
     "Card with Custom Options"
     (example-box
      [:div {:class (ds/card {:shadow :lg :hover? true})}
       [:h3 {:class (ds/heading 3)} "Enhanced Card"]
       [:p {:class (ds/text :text-secondary)}
        "This card has a larger shadow and hover effect."]]
      "(ds/card {:shadow :lg :hover? true})"))

    ;; Using Card Component
    (subsection
     "Using Card Component"
     [:div {:class "grid grid-cols-1 md:grid-cols-2 gap-6"}
      (card/simple-card {:title "Simple Card"
                        :description "Text-only card without image"
                        :link "/example"})
      (card/icon-card {:title "Icon Card"
                      :description "Card with icon instead of image"
                      :icon "⛪"
                      :link "/example"})])]))

;; =============================================================================
;; ALERTS SECTION
;; =============================================================================

(defn alerts-section
  "Display alert/message box variants."
  []
  (section
   "Alerts & Messages"
   [:div {:class "space-y-6"}
    ;; Success
    (example-box
     [:div {:class (ds/alert :success)}
      [:p "✓ Operation completed successfully!"]]
     "(ds/alert :success)")

    ;; Error
    (example-box
     [:div {:class (ds/alert :error)}
      [:p "✗ An error occurred. Please try again."]]
     "(ds/alert :error)")

    ;; Warning
    (example-box
     [:div {:class (ds/alert :warning)}
      [:p "⚠ Warning: This action cannot be undone."]]
     "(ds/alert :warning)")

    ;; Info
    (example-box
     [:div {:class (ds/alert :info)}
      [:p "ℹ For more information, visit our help center."]]
     "(ds/alert :info)")

    ;; Using Primitives Components
    (subsection
     "Using Primitives Components"
     [:div {:class "space-y-6"}
      (example-box
       (primitives/success-message "Your changes have been saved.")
       "(primitives/success-message \"Your changes have been saved.\")")

      (example-box
       (primitives/error-message "Failed to load data from server.")
       "(primitives/error-message \"Failed to load data from server.\")")])]))

;; =============================================================================
;; FORM INPUTS SECTION
;; =============================================================================

(defn form-inputs-section
  "Display form input styling."
  []
  (section
   "Form Inputs"
   [:div {:class "space-y-6"}
    ;; Standard Input
    (example-box
     [:input {:type "text"
              :placeholder "Enter your name"
              :class (ds/input)}]
     "(ds/input)")

    ;; Error State
    (example-box
     [:input {:type "text"
              :placeholder "Enter your email"
              :class (ds/input {:error? true})}]
     "(ds/input {:error? true})")

    ;; Disabled State
    (example-box
     [:input {:type "text"
              :placeholder "Disabled field"
              :disabled true
              :class (ds/input {:disabled? true})}]
     "(ds/input {:disabled? true})")

    ;; Textarea
    (example-box
     [:textarea {:placeholder "Enter your message"
                 :rows 4
                 :class (ds/input)}]
     "(ds/input) ; Works for textarea too")]))

;; =============================================================================
;; LAYOUT SECTION
;; =============================================================================

(defn layout-section
  "Display layout utilities and containers."
  []
  (section
   "Layout & Containers"
   [:div
    ;; Container Sizes
    (subsection
     "Container Sizes"
     [:div {:class "space-y-4"}
      (for [[size-key size-class] (select-keys ds/container-sizes [:sm :md :lg :xl :7xl])]
        [:div {:key size-key}
         [:div {:class (str size-class " " (ds/bg :gray-200) " " (ds/p :md) " " (ds/mx :auto))}
          [:p {:class (ds/text-size :sm)}
           (str (name size-key) " - " size-class)]]
         [:div {:class (ds/my :sm)}]])])

    ;; Usage example
    [:div {:class (ds/mt :xl)}
     [:h4 {:class (ds/heading 4)} "Usage Examples"]
     (code-example
      ";; Container utilities
(ds/container :7xl)      ; => \"max-w-7xl mx-auto px-4\"
(ds/container :4xl :lg)  ; => \"max-w-4xl mx-auto px-6\"

;; Combining utilities
(ds/classes [(ds/container :7xl)
             (ds/py :xl)
             (ds/bg :white)])")]]))

;; =============================================================================
;; UTILITIES SECTION
;; =============================================================================

(defn utilities-section
  "Display utility classes and helpers."
  []
  (section
   "Utility Classes"
   [:div
    ;; Borders & Radius
    (subsection
     "Borders & Radius"
     [:div {:class "grid grid-cols-2 md:grid-cols-4 gap-6"}
      (for [[size-key _] ds/border-radius]
        [:div {:key size-key
               :class (str (ds/classes [(ds/border)
                                       (ds/border-color :border-default)
                                       (ds/bg :gray-50)
                                       (ds/p :lg)
                                       "text-center"])
                          " "
                          (ds/rounded size-key))}
         [:p {:class (ds/text-size :sm)}
          (name size-key)]])])

    ;; Shadows
    (subsection
     "Shadows"
     [:div {:class "grid grid-cols-2 md:grid-cols-4 gap-6"}
      (for [[size-key _] ds/shadows]
        [:div {:key size-key
               :class (str (ds/classes [(ds/bg :white)
                                       (ds/p :lg)
                                       (ds/rounded :md)
                                       "text-center"])
                          " "
                          (ds/shadow size-key))}
         [:p {:class (ds/text-size :sm)}
          (name size-key)]])])

    ;; Transitions
    (subsection
     "Transitions"
     [:div {:class "flex flex-wrap gap-4"}
      [:button {:class (str (ds/button :primary)
                           " "
                           (ds/transition :colors)
                           " "
                           (ds/duration :fast))}
       "Fast Transition"]
      [:button {:class (str (ds/button :primary)
                           " "
                           (ds/transition :transform)
                           " hover:scale-105")}
       "Transform on Hover"]])

    ;; Usage example
    [:div {:class (ds/mt :xl)}
     [:h4 {:class (ds/heading 4)} "Usage Examples"]
     (code-example
      ";; Utility functions
(ds/rounded :lg)           ; => \"rounded-lg\"
(ds/shadow :md)            ; => \"shadow-md\"
(ds/hover-shadow :lg)      ; => \"hover:shadow-lg\"
(ds/transition :colors)    ; => \"transition-colors\"
(ds/duration :fast)        ; => \"duration-150\"")]]))

;; =============================================================================
;; COMPOSING CLASSES SECTION
;; =============================================================================

(defn composition-section
  "Show how to compose design system utilities."
  []
  (section
   "Composing Classes"
   [:div
    [:p {:class (ds/classes [(ds/text :text-secondary)
                            (ds/mb :lg)])}
     "The design system provides a " [:code "classes"] " function to combine multiple utilities into a single class string."]

    ;; Basic Composition
    (subsection
     "Basic Composition"
     (code-example
      ";; Combine multiple utilities
(ds/classes [(ds/bg :white)
             (ds/shadow :md)
             (ds/p :lg)
             (ds/rounded :lg)])
; => \"bg-white shadow-md p-6 rounded-lg\""))

    ;; Conditional Classes
    (subsection
     "Conditional Classes"
     (code-example
      ";; Use when for conditional classes
(ds/classes [(ds/text :primary)
             (when error? (ds/text :error))
             (when disabled? \"opacity-50\")])

;; nil values are automatically filtered out"))

    ;; Building Custom Components
    (subsection
     "Building Custom Components"
     (code-example
      ";; Create reusable component styles
(defn my-panel [opts]
  [:div {:class (ds/classes [(ds/container :4xl)
                             (ds/bg :bg-card)
                             (ds/shadow :lg)
                             (ds/py :xl)
                             (ds/rounded :lg)
                             (:class opts)])}
   ;; content...
   ])"))

    ;; Live Example
    (subsection
     "Live Example"
     [:div {:class (ds/classes [(ds/bg :white)
                               (ds/shadow :lg)
                               (ds/p :xl)
                               (ds/rounded :lg)
                               (ds/border)
                               (ds/border-color :primary)])}
      [:h3 {:class (ds/heading 3)} "Composed Component"]
      [:p {:class (ds/classes [(ds/text :text-secondary)
                              (ds/mt :md)])}
       "This component uses multiple design system utilities combined with the "
       [:code "classes"] " function."]])]))

;; =============================================================================
;; MAIN STYLE GUIDE PAGE
;; =============================================================================

(defn style-guide-page
  "Main style guide page with all sections."
  []
  [:div {:class (ds/bg :bg-page)}
   ;; Header
   [:div {:class (ds/classes [(ds/bg :bg-header)
                             (ds/text :text-on-dark)
                             (ds/py :3xl)])}
    [:div {:class (ds/container :7xl)}
     [:h1 {:class (ds/classes [(ds/text-size :6xl)
                              (ds/font-weight :extrabold)
                              (ds/font-family :garamond)
                              (ds/mb :md)])}
      "Mount Zion CMS"]
     [:p {:class (ds/classes [(ds/text-size :xl)
                             (ds/font-family :ibm-plex)])}
      "Design System & Living Style Guide"]]]

   ;; Introduction
   [:div {:class (ds/container :7xl)}
    [:div {:class (ds/py :xl)}
     [:div {:class (ds/classes [(ds/bg :info-bg)
                               (ds/border)
                               (ds/border-color :info-light)
                               (ds/p :lg)
                               (ds/rounded :lg)
                               (ds/mb :3xl)])}
      [:h2 {:class (ds/heading 3)} "About This Style Guide"]
      [:p {:class (ds/classes [(ds/text :text-secondary)
                              (ds/mt :md)])}
       "This living style guide documents the Mount Zion CMS design system. "
       "It provides visual examples, code snippets, and usage guidelines for all design tokens and components. "
       "Use this as a reference when building new features or maintaining existing code."]]

     ;; Table of Contents
     [:nav {:class (ds/classes [(ds/bg :white)
                               (ds/shadow :md)
                               (ds/p :lg)
                               (ds/rounded :lg)
                               (ds/mb :3xl)])}
      [:h2 {:class (ds/heading 3)} "Table of Contents"]
      [:ul {:class "list-disc list-inside space-y-2 mt-4"}
       [:li [:a {:href "#colors" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Color Palette"]]
       [:li [:a {:href "#typography" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Typography"]]
       [:li [:a {:href "#spacing" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Spacing"]]
       [:li [:a {:href "#buttons" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Buttons"]]
       [:li [:a {:href "#cards" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Cards"]]
       [:li [:a {:href "#alerts" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Alerts & Messages"]]
       [:li [:a {:href "#forms" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Form Inputs"]]
       [:li [:a {:href "#layout" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Layout & Containers"]]
       [:li [:a {:href "#utilities" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Utility Classes"]]
       [:li [:a {:href "#composition" :class (ds/classes [(ds/text :primary) "hover:underline"])} "Composing Classes"]]]]

     ;; All sections
     [:div {:id "colors"} (colors-section)]
     [:div {:id "typography"} (typography-section)]
     [:div {:id "spacing"} (spacing-section)]
     [:div {:id "buttons"} (buttons-section)]
     [:div {:id "cards"} (cards-section)]
     [:div {:id "alerts"} (alerts-section)]
     [:div {:id "forms"} (form-inputs-section)]
     [:div {:id "layout"} (layout-section)]
     [:div {:id "utilities"} (utilities-section)]
     [:div {:id "composition"} (composition-section)]]]

   ;; Footer
   [:div {:class (ds/classes [(ds/bg :white)
                             (ds/border-color :border-default)
                             "border-t"
                             (ds/py :xl)
                             (ds/mt :3xl)])}
    [:div {:class (ds/container :7xl)}
     [:p {:class (ds/classes [(ds/text :text-muted)
                             (ds/text-size :sm)
                             "text-center"])}
      "Mount Zion CMS Design System • Version 1.0 • "
      [:a {:href "https://github.com/tailwindlabs/tailwindcss"
           :class (ds/text :primary)
           :target "_blank"}
       "Built with Tailwind CSS"]]]]])

;; =============================================================================
;; REPL TESTING
;; =============================================================================

(comment
  ;; Test the style guide page
  (style-guide-page)

  ;; Test individual sections
  (colors-section)
  (typography-section)
  (buttons-section))
