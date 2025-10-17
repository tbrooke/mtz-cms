(ns mtz-cms.ui.design-system
  "Mount Zion CMS Design System

   Central source of truth for all design tokens and styling patterns.
   This namespace provides:
   - Color palette (semantic and raw, including custom OKLCH colors)
   - Typography scale and utilities
   - Custom font families (EB Garamond, IBM Plex Sans, Source Serif 4)
   - Spacing scale
   - Border radius values
   - Shadow utilities
   - Component class builders

   Philosophy:
   - Single source of truth for design decisions
   - Semantic naming (use purpose, not color names)
   - Composable class builder functions
   - Works with Tailwind CDN (returns class strings)
   - Type-safe with clear documentation

   Usage:
   (require '[mtz-cms.ui.design-system :as ds])

   ;; Use semantic colors
   (ds/text :primary)        ; => \"text-blue-600\"
   (ds/bg :primary)          ; => \"bg-blue-600\"
   (ds/bg :bg-header)        ; => \"bg-mint-light\" (custom OKLCH)

   ;; Use custom fonts
   (ds/font-family :garamond) ; => \"font-garamond\"
   (ds/font-family :menu)     ; => \"font-menu\"

   ;; Build buttons and components
   (ds/button :primary)      ; => \"px-6 py-3 bg-blue-600...\"

   ;; Build custom component classes
   (ds/classes [(ds/bg :white) (ds/shadow :md) (ds/rounded :lg)])
   ; => \"bg-white shadow-md rounded-lg\"")

;; =============================================================================
;; COLOR PALETTE
;; =============================================================================

(def ^:private color-palette
  "Raw color values mapped to Tailwind classes.
   These are the foundation - prefer semantic colors below."
  {:blue-50   "blue-50"
   :blue-100  "blue-100"
   :blue-200  "blue-200"
   :blue-600  "blue-600"
   :blue-700  "blue-700"
   :blue-800  "blue-800"

   :gray-50   "gray-50"
   :gray-100  "gray-100"
   :gray-200  "gray-200"
   :gray-400  "gray-400"
   :gray-500  "gray-500"
   :gray-600  "gray-600"
   :gray-700  "gray-700"
   :gray-900  "gray-900"

   :red-50    "red-50"
   :red-200   "red-200"
   :red-400   "red-400"
   :red-600   "red-600"
   :red-700   "red-700"

   :green-50  "green-50"
   :green-200 "green-200"
   :green-400 "green-400"
   :green-700 "green-700"

   :yellow-50  "yellow-50"
   :yellow-300 "yellow-300"
   :yellow-700 "yellow-700"
   :yellow-800 "yellow-800"

   :white     "white"
   :black     "black"
   :transparent "transparent"

   ;; Custom OKLCH colors (defined in ui/styles.clj custom CSS)
   ;; Mint-light uses cyan hue 180.72°, others use teal hue 166.113°
   :mint-light    "mint-light"    ; Very light cyan background (180.72°)
   :mint-primary  "mint-primary"  ; Medium mint for buttons/links
   :mint-dark     "mint-dark"     ; Dark mint for hover states
   :mint-accent   "mint-accent"   ; Light mint for highlights
   :warm-accent   "warm-accent"}) ; Warm complementary accent

(def semantic-colors
  "Semantic color mapping for consistent brand application.
   Use these instead of raw colors for maintainability.

   Categories:
   - :primary/:secondary - Brand colors
   - :success/:error/:warning/:info - Status colors
   - :text-* - Text colors
   - :bg-* - Background colors
   - :border-* - Border colors"
  {;; Brand colors - using coordinated mint OKLCH palette
   :primary           :mint-primary   ; Main brand color for buttons/links
   :primary-light     :mint-accent    ; Lighter for highlights
   :primary-lighter   :mint-light     ; Very light for backgrounds
   :primary-dark      :mint-dark      ; Darker for hover states
   :primary-darker    :mint-dark      ; Darkest for active states

   :secondary         :gray-600
   :secondary-light   :gray-200
   :secondary-lighter :gray-50
   :secondary-warm    :warm-accent    ; Warm complementary accent

   ;; Status colors
   :success           :green-700
   :success-light     :green-200
   :success-bg        :green-50

   :error             :red-700
   :error-light       :red-400
   :error-bg          :red-50
   :error-border      :red-200
   :error-strong      :red-600

   :warning           :yellow-700
   :warning-light     :yellow-300
   :warning-bg        :yellow-50
   :warning-dark      :yellow-800

   :info              :mint-primary  ; Use mint for info (coordinated theme)
   :info-light        :mint-accent   ; Light mint for info highlights
   :info-bg           :mint-light    ; Very light mint for info backgrounds

   ;; Text colors
   :text-primary      :gray-900
   :text-secondary    :gray-600
   :text-muted        :gray-500
   :text-light        :gray-400
   :text-on-primary   :white
   :text-on-dark      :black

   ;; Background colors
   :bg-page           :white         ; Pure white (#ffffff) - base page background
   :bg-card           :white         ; Pure white (#ffffff) - card backgrounds
   :bg-hover          :mint-light    ; Subtle mint hover (coordinated)
   :bg-header         :mint-light    ; Light mint header background

   ;; Border colors
   :border-default    :gray-200
   :border-light      :gray-100
   :border-strong     :gray-400})

;; =============================================================================
;; COLOR UTILITIES
;; =============================================================================

(defn- resolve-color
  "Resolve a semantic color to its Tailwind class name."
  [color-key]
  (let [resolved (get semantic-colors color-key color-key)]
    (get color-palette resolved (name resolved))))

(defn text
  "Generate text color class.

   Args:
     color - Semantic color keyword (e.g., :primary, :error)

   Examples:
     (text :primary)     ; => \"text-blue-600\"
     (text :error)       ; => \"text-red-700\"
     (text :text-muted)  ; => \"text-gray-500\""
  [color]
  (str "text-" (resolve-color color)))

(defn bg
  "Generate background color class.

   Args:
     color - Semantic color keyword

   Examples:
     (bg :primary)   ; => \"bg-blue-600\"
     (bg :bg-card)   ; => \"bg-white\""
  [color]
  (str "bg-" (resolve-color color)))

(defn border-color
  "Generate border color class.

   Args:
     color - Semantic color keyword

   Examples:
     (border-color :border-default)  ; => \"border-gray-200\""
  [color]
  (str "border-" (resolve-color color)))

(defn hover-text
  "Generate hover text color class.

   Examples:
     (hover-text :primary-dark)  ; => \"hover:text-blue-700\""
  [color]
  (str "hover:text-" (resolve-color color)))

(defn hover-bg
  "Generate hover background color class.

   Examples:
     (hover-bg :primary-dark)  ; => \"hover:bg-blue-700\""
  [color]
  (str "hover:bg-" (resolve-color color)))

;; =============================================================================
;; TYPOGRAPHY
;; =============================================================================

(def typography-scale
  "Typography size scale with semantic names."
  {:xs   "text-xs"      ; 12px
   :sm   "text-sm"      ; 14px
   :base "text-base"    ; 16px
   :lg   "text-lg"      ; 18px
   :xl   "text-xl"      ; 20px
   :2xl  "text-2xl"     ; 24px
   :3xl  "text-3xl"     ; 30px
   :4xl  "text-4xl"     ; 36px
   :5xl  "text-5xl"     ; 48px
   :6xl  "text-6xl"})   ; 60px

(def font-weights
  "Font weight utilities."
  {:normal    "font-normal"
   :medium    "font-medium"
   :semibold  "font-semibold"
   :bold      "font-bold"
   :extrabold "font-extrabold"})

(def font-families
  "Custom font family utilities.
   These map to fonts defined in ui/styles.clj"
  {:garamond  "font-garamond"    ; EB Garamond - elegant serif for headings
   :menu      "font-menu"        ; IBM Plex Sans - uppercase menu font
   :ibm-plex  "font-ibm-plex"    ; IBM Plex Sans - clean sans-serif
   :serif     "font-serif"})     ; Source Serif 4 (default body)

(defn font-family
  "Get custom font family class.

   Examples:
     (font-family :garamond)  ; => \"font-garamond\"
     (font-family :menu)      ; => \"font-menu\""
  [family]
  (get font-families family "font-serif"))

(defn text-size
  "Get typography size class.

   Args:
     size - Keyword from typography-scale

   Examples:
     (text-size :lg)   ; => \"text-lg\"
     (text-size :3xl)  ; => \"text-3xl\""
  [size]
  (get typography-scale size "text-base"))

(defn font-weight
  "Get font weight class.

   Examples:
     (font-weight :bold)      ; => \"font-bold\"
     (font-weight :semibold)  ; => \"font-semibold\""
  [weight]
  (get font-weights weight "font-normal"))

(defn heading
  "Generate heading classes with size and weight.

   Args:
     level - Heading level (1-6) or size keyword

   Examples:
     (heading 1)      ; => \"text-4xl font-extrabold text-gray-900\"
     (heading :hero)  ; => \"text-6xl font-extrabold text-gray-900\""
  [level]
  (case level
    1     (str (text-size :4xl) " " (font-weight :extrabold) " " (text :text-primary))
    2     (str (text-size :3xl) " " (font-weight :bold) " " (text :text-primary))
    3     (str (text-size :2xl) " " (font-weight :bold) " " (text :text-primary))
    4     (str (text-size :xl) " " (font-weight :semibold) " " (text :text-primary))
    5     (str (text-size :lg) " " (font-weight :semibold) " " (text :text-primary))
    6     (str (text-size :base) " " (font-weight :semibold) " " (text :text-primary))
    :hero (str (text-size :6xl) " " (font-weight :extrabold) " " (text :text-primary))
    ;; Default to h3
    (str (text-size :2xl) " " (font-weight :bold) " " (text :text-primary))))

;; =============================================================================
;; SPACING
;; =============================================================================

(def spacing-scale
  "Spacing scale (padding/margin) with semantic names.
   Values map to Tailwind's spacing scale."
  {:xs   "1"      ; 4px
   :sm   "2"      ; 8px
   :md   "4"      ; 16px
   :lg   "6"      ; 24px
   :xl   "8"      ; 32px
   :2xl  "12"     ; 48px
   :3xl  "16"     ; 64px
   :4xl  "24"})   ; 96px

(defn- resolve-spacing
  "Resolve spacing keyword to Tailwind spacing number."
  [spacing]
  (if (keyword? spacing)
    (get spacing-scale spacing "4")
    (str spacing)))

(defn px
  "Generate horizontal padding class.

   Args:
     spacing - Keyword (:xs, :sm, :md, etc.) or number

   Examples:
     (px :md)  ; => \"px-4\"
     (px :lg)  ; => \"px-6\""
  [spacing]
  (str "px-" (resolve-spacing spacing)))

(defn py
  "Generate vertical padding class.

   Examples:
     (py :md)  ; => \"py-4\"
     (py :xl)  ; => \"py-8\""
  [spacing]
  (str "py-" (resolve-spacing spacing)))

(defn p
  "Generate all-sides padding class.

   Examples:
     (p :md)  ; => \"p-4\""
  [spacing]
  (str "p-" (resolve-spacing spacing)))

(defn mx
  "Generate horizontal margin class.

   Examples:
     (mx :auto)  ; => \"mx-auto\""
  [spacing]
  (if (= spacing :auto)
    "mx-auto"
    (str "mx-" (resolve-spacing spacing))))

(defn my
  "Generate vertical margin class."
  [spacing]
  (str "my-" (resolve-spacing spacing)))

(defn mb
  "Generate bottom margin class.

   Examples:
     (mb :md)  ; => \"mb-4\"
     (mb :lg)  ; => \"mb-6\""
  [spacing]
  (str "mb-" (resolve-spacing spacing)))

(defn mt
  "Generate top margin class."
  [spacing]
  (str "mt-" (resolve-spacing spacing)))

(defn gap
  "Generate gap class for flex/grid layouts.

   Examples:
     (gap :md)  ; => \"gap-4\"
     (gap :lg)  ; => \"gap-6\""
  [spacing]
  (str "gap-" (resolve-spacing spacing)))

;; =============================================================================
;; BORDERS
;; =============================================================================

(def border-radius
  "Border radius utilities."
  {:none "rounded-none"
   :sm   "rounded-sm"
   :md   "rounded-md"
   :lg   "rounded-lg"
   :xl   "rounded-xl"
   :full "rounded-full"})

(defn rounded
  "Generate border radius class.

   Examples:
     (rounded :md)    ; => \"rounded-md\"
     (rounded :full)  ; => \"rounded-full\""
  [size]
  (get border-radius size "rounded"))

(def border-widths
  "Border width utilities."
  {:default "border"
   :0       "border-0"
   :2       "border-2"
   :4       "border-4"})

(defn border
  "Generate border width class.

   Examples:
     (border)       ; => \"border\"
     (border :2)    ; => \"border-2\""
  ([] "border")
  ([width] (get border-widths width "border")))

;; =============================================================================
;; SHADOWS
;; =============================================================================

(def shadows
  "Box shadow utilities."
  {:none "shadow-none"
   :sm   "shadow-sm"
   :md   "shadow-md"
   :lg   "shadow-lg"
   :xl   "shadow-xl"})

(defn shadow
  "Generate box shadow class.

   Examples:
     (shadow :md)  ; => \"shadow-md\"
     (shadow :lg)  ; => \"shadow-lg\""
  [size]
  (get shadows size "shadow"))

(defn hover-shadow
  "Generate hover shadow class.

   Examples:
     (hover-shadow :lg)  ; => \"hover:shadow-lg\""
  [size]
  (str "hover:" (shadow size)))

;; =============================================================================
;; LAYOUT UTILITIES
;; =============================================================================

(def container-sizes
  "Max-width container utilities."
  {:sm  "max-w-screen-sm"   ; 640px
   :md  "max-w-screen-md"   ; 768px
   :lg  "max-w-screen-lg"   ; 1024px
   :xl  "max-w-screen-xl"   ; 1280px
   :2xl "max-w-screen-2xl"  ; 1536px
   :full "max-w-full"
   :prose "max-w-prose"     ; ~65ch
   :4xl "max-w-4xl"         ; Custom size used in app
   :6xl "max-w-6xl"         ; Custom size for hero
   :7xl "max-w-7xl"})       ; Main container width

(defn container
  "Generate container class with max-width.

   Examples:
     (container :7xl)  ; => \"max-w-7xl mx-auto px-4\""
  ([size]
   (container size :md))
  ([size padding]
   (str (get container-sizes size "max-w-7xl")
        " " (mx :auto)
        " " (px padding))))

;; =============================================================================
;; COMPONENT CLASS BUILDERS
;; =============================================================================

(defn classes
  "Combine multiple class strings into one.
   Filters out nil values for conditional classes.

   Args:
     class-list - Vector of class strings

   Examples:
     (classes [(bg :white) (shadow :md) (rounded :lg)])
     ; => \"bg-white shadow-md rounded-lg\"

     (classes [(text :primary) (when error? \"border-red-500\")])
     ; => \"text-blue-600 border-red-500\" (if error? is true)"
  [class-list]
  (->> class-list
       (filter some?)
       (clojure.string/join " ")))

(defn button
  "Generate button classes with variant styling.

   Args:
     variant - Button variant (:primary, :secondary, :danger)
     opts    - Optional map with :size, :disabled?, :class

   Examples:
     (button :primary)
     ; => \"px-6 py-3 rounded-md font-medium transition-colors...\"

     (button :secondary {:size :sm})
     ; => \"px-4 py-2 rounded-md font-medium...\""
  ([variant]
   (button variant {}))
  ([variant {:keys [size disabled? class]
             :or {size :md}}]
   (let [base "font-medium transition-colors text-center inline-block"

         sizing (case size
                  :sm  (str (px :md) " " (py :sm) " " (text-size :sm))
                  :md  (str (px :lg) " " (py :md))
                  :lg  (str (px :xl) " " (py :md) " " (text-size :lg))
                  (str (px :lg) " " (py :md)))

         variant-classes
         (case variant
           :primary   (classes [(bg :primary) (text :text-on-primary)
                               (hover-bg :primary-dark) (rounded :md)])
           :secondary (classes [(bg :white) (text :primary)
                               (border) (border-color :primary)
                               (hover-bg :primary-lighter) (rounded :md)])
           :warm      (classes [(bg :secondary-warm) (text :text-on-primary)
                               "hover:bg-opacity-90" (rounded :md)])
           :danger    (classes [(bg :error-strong) (text :text-on-primary)
                               "hover:bg-red-700" (rounded :md)])
           :link      (classes [(text :primary) (hover-text :primary-dark)
                               (font-weight :medium)])
           ;; Default to primary
           (classes [(bg :primary) (text :text-on-primary)
                    (hover-bg :primary-dark) (rounded :md)]))

         disabled-classes (when disabled? "opacity-50 cursor-not-allowed")]

     (classes [base sizing variant-classes disabled-classes class]))))

(defn card
  "Generate card container classes.

   Args:
     opts - Optional map with :padding, :shadow, :hover?, :class

   Examples:
     (card)
     ; => \"bg-white rounded-lg shadow-md overflow-hidden\"

     (card {:hover? true :shadow :lg})
     ; => \"bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl...\""
  ([]
   (card {}))
  ([{:keys [padding shadow hover? border? class]
     :or {padding :lg shadow :md hover? true border? false}}]
   (classes [(bg :bg-card)
             (rounded :lg)
             (shadow shadow)
             (when hover? (str (hover-shadow :lg) " transition-shadow"))
             (when border? (str (border) " " (border-color :border-default)))
             "overflow-hidden"
             (when padding (p padding))
             class])))

(defn input
  "Generate form input field classes.

   Args:
     opts - Optional map with :error?, :disabled?, :class

   Examples:
     (input)
     ; => \"px-4 py-2 border border-gray-200 rounded-md...\"

     (input {:error? true})
     ; => \"px-4 py-2 border border-red-500 rounded-md...\""
  ([]
   (input {}))
  ([{:keys [error? disabled? class]
     :or {error? false disabled? false}}]
   (classes [(px :md) (py :sm)
             (border)
             (if error?
               "border-red-500 focus:ring-red-500"
               (str (border-color :border-default) " focus:ring-blue-500"))
             (rounded :md)
             "focus:outline-none focus:ring-2"
             (when disabled? "bg-gray-100 cursor-not-allowed opacity-50")
             class])))

(defn alert
  "Generate alert/message box classes.

   Args:
     variant - Alert type (:success, :error, :warning, :info)
     opts    - Optional map with :class

   Examples:
     (alert :error)
     ; => \"bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md\"

     (alert :success)
     ; => \"bg-green-50 border border-green-200 text-green-700...\""
  ([variant]
   (alert variant {}))
  ([variant {:keys [class]}]
   (let [variant-classes
         (case variant
           :success (classes [(bg :success-bg) (border) (border-color :success-light)
                             (text :success) (px :md) (py :md) (rounded :md)])
           :error   (classes [(bg :error-bg) (border) (border-color :error-border)
                             (text :error) (px :md) (py :md) (rounded :md)])
           :warning (classes [(bg :warning-bg) (border) (border-color :warning-light)
                             (text :warning) (px :md) (py :md) (rounded :md)])
           :info    (classes [(bg :info-bg) (border) (border-color :info-light)
                             (text :info) (px :md) (py :md) (rounded :md)])
           ;; Default to info
           (classes [(bg :info-bg) (border) (border-color :info-light)
                    (text :info) (px :md) (py :md) (rounded :md)]))]
     (classes [variant-classes class]))))

;; =============================================================================
;; TRANSITION & ANIMATION
;; =============================================================================

(def transitions
  "Transition utilities."
  {:default "transition"
   :all     "transition-all"
   :colors  "transition-colors"
   :opacity "transition-opacity"
   :shadow  "transition-shadow"
   :transform "transition-transform"})

(defn transition
  "Generate transition class.

   Examples:
     (transition :colors)  ; => \"transition-colors\"
     (transition :shadow)  ; => \"transition-shadow\""
  ([]
   "transition")
  ([type]
   (get transitions type "transition")))

(def durations
  "Animation duration utilities."
  {:fast   "duration-150"
   :normal "duration-200"
   :slow   "duration-300"})

(defn duration
  "Generate duration class.

   Examples:
     (duration :fast)  ; => \"duration-150\""
  [speed]
  (get durations speed "duration-200"))

;; =============================================================================
;; REPL TESTING & EXAMPLES
;; =============================================================================

(comment
  ;; Test color utilities
  (text :primary)           ; => "text-blue-600"
  (bg :error-bg)           ; => "bg-red-50"
  (hover-text :primary-dark) ; => "hover:text-blue-700"

  ;; Test typography
  (heading 1)              ; => "text-4xl font-extrabold text-gray-900"
  (text-size :3xl)         ; => "text-3xl"
  (font-weight :bold)      ; => "font-bold"
  (font-family :garamond)  ; => "font-garamond"
  (font-family :menu)      ; => "font-menu"

  ;; Test spacing
  (px :lg)                 ; => "px-6"
  (py :md)                 ; => "py-4"
  (mb :xl)                 ; => "mb-8"
  (gap :md)                ; => "gap-4"

  ;; Test borders & shadows
  (rounded :lg)            ; => "rounded-lg"
  (shadow :md)             ; => "shadow-md"
  (hover-shadow :lg)       ; => "hover:shadow-lg"

  ;; Test layout
  (container :7xl)         ; => "max-w-7xl mx-auto px-4"
  (container :4xl :lg)     ; => "max-w-4xl mx-auto px-6"

  ;; Test component builders
  (button :primary)
  ; => "font-medium transition-colors text-center inline-block px-6 py-4 bg-blue-600 text-white hover:bg-blue-700 rounded-md"

  (button :secondary {:size :sm})
  ; => "font-medium... px-4 py-2... bg-white text-blue-600 border..."

  (card {:hover? true})
  ; => "bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"

  (input {:error? true})
  ; => "px-4 py-2 border border-red-500 rounded-md focus:outline-none..."

  (alert :error)
  ; => "bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md"

  ;; Test class composition
  (classes [(bg :white) (shadow :md) (p :lg) (rounded :lg)])
  ; => "bg-white shadow-md p-6 rounded-lg"

  (classes [(text :primary) (when true "font-bold") (when false "hidden")])
  ; => "text-blue-600 font-bold"

  ;; Build custom component
  (classes [(container :7xl)
            (bg :bg-card)
            (shadow :lg)
            (py :xl)
            (rounded :lg)])
  ; => "max-w-7xl mx-auto px-4 bg-white shadow-lg py-8 rounded-lg"
  )
