(ns mtz-cms.components.primitives
  "Basic reusable UI components for Mount Zion CMS

   These are primitive, composable components that can be used throughout
   the application. They are pure presentation - no business logic.

   All components:
   - Accept data as parameters
   - Return Hiccup vectors
   - Have no side effects
   - Can be easily tested in REPL
   - Use design system for consistent styling"
  (:require
   [mtz-cms.ui.design-system :as ds]))

;; --- LOADING STATES ---

(defn loading-spinner
  "Loading spinner with message.

   Displays an animated spinner with 'Loading...' text.
   Uses Tailwind's animate-spin utility.

   Returns: Hiccup vector"
  []
  [:div {:class (ds/classes ["flex items-center justify-center"
                            (ds/py :md)])}
   [:div {:class (ds/classes ["animate-spin"
                             (ds/rounded :full)
                             "h-8 w-8 border-b-2"
                             "border-blue-600"])}]
   [:span {:class (ds/classes ["ml-2"
                              (ds/text :text-secondary)])}
    "Loading..."]])

;; --- MESSAGE COMPONENTS ---

(defn error-message
  "Error message display component.

   Args:
     message - String error message to display

   Example:
     (error-message \"Failed to load data\")

   Returns: Hiccup vector with red error styling (using design system)"
  [message]
  [:div {:class (ds/alert :error)}
   [:div {:class "flex"}
    [:svg {:class "h-5 w-5 text-red-400 mr-2" :fill "currentColor" :viewBox "0 0 20 20"}
     [:path {:fill-rule "evenodd"
             :d "M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
             :clip-rule "evenodd"}]]
    [:span "Error: " message]]])

(defn success-message
  "Success message display component.

   Args:
     message - String success message to display

   Example:
     (success-message \"Data saved successfully\")

   Returns: Hiccup vector with green success styling (using design system)"
  [message]
  [:div {:class (ds/alert :success)}
   [:div {:class "flex"}
    [:svg {:class "h-5 w-5 text-green-400 mr-2" :fill "currentColor" :viewBox "0 0 20 20"}
     [:path {:fill-rule "evenodd"
             :d "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
             :clip-rule "evenodd"}]]
    [:span message]]])

(defn info-message
  "Info message display component.

   Args:
     message - String info message to display

   Example:
     (info-message \"Processing your request...\")

   Returns: Hiccup vector with blue info styling (using design system)"
  [message]
  [:div {:class (ds/alert :info)}
   [:div {:class "flex"}
    [:svg {:class "h-5 w-5 text-blue-400 mr-2" :fill "currentColor" :viewBox "0 0 20 20"}
     [:path {:fill-rule "evenodd"
             :d "M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
             :clip-rule "evenodd"}]]
    [:span message]]])

(defn warning-message
  "Warning message display component.

   Args:
     message - String warning message to display

   Example:
     (warning-message \"This action cannot be undone\")

   Returns: Hiccup vector with yellow warning styling (using design system)"
  [message]
  [:div {:class (ds/alert :warning)}
   [:div {:class "flex"}
    [:svg {:class "h-5 w-5 text-yellow-400 mr-2" :fill "currentColor" :viewBox "0 0 20 20"}
     [:path {:fill-rule "evenodd"
             :d "M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
             :clip-rule "evenodd"}]]
    [:span message]]])

;; --- BUTTON COMPONENTS ---

(defn button
  "Styled button component using design system.

   Args:
     data - Map with keys:
       :text     - Button text (required)
       :href     - Optional link URL (makes it an <a> tag)
       :on-click - Optional click handler attribute
       :variant  - Button style variant:
                   :primary (default) - blue background
                   :secondary - white with border
                   :warm - warm complementary accent
                   :danger - red background
                   :link - link style
       :size     - Optional size: :sm, :md (default), :lg
       :class    - Optional additional CSS classes
       :disabled - Optional boolean for disabled state

   Examples:
     (button {:text \"Click me\"})
     (button {:text \"Learn More\" :href \"/about\"})
     (button {:text \"Delete\" :variant :danger :on-click \"handleDelete()\"})
     (button {:text \"Small\" :size :sm :variant :secondary})

   Returns: Hiccup vector for button or link styled as button"
  [{:keys [text href on-click variant class disabled size]
    :or {variant :primary size :md}}]
  (let [button-class (ds/button variant {:size size
                                         :disabled? disabled
                                         :class class})
        attrs (cond-> {:class button-class}
                href (assoc :href href)
                on-click (assoc :onclick on-click)
                disabled (assoc :disabled true))]
    (if href
      [:a attrs text]
      [:button attrs text])))

;; --- REPL TESTING ---

(comment
  ;; Test loading spinner
  (loading-spinner)
  ;; => [:div {:class "..."} ...]

  ;; Test message components
  (error-message "Something went wrong")
  (success-message "Operation completed")
  (info-message "Processing your request...")
  (warning-message "This action cannot be undone")

  ;; Test button variants
  (button {:text "Primary Button"})
  (button {:text "Secondary Button" :variant :secondary})
  (button {:text "Warm Accent" :variant :warm})
  (button {:text "Danger Button" :variant :danger})
  (button {:text "Link Style" :variant :link})

  ;; Test button as link
  (button {:text "Learn More" :href "/about"})

  ;; Test button sizes
  (button {:text "Small" :size :sm :variant :secondary})
  (button {:text "Large" :size :lg :variant :primary})

  ;; Test disabled button
  (button {:text "Can't Click" :disabled true})

  ;; Test with click handler
  (button {:text "Click Me" :on-click "alert('clicked')"})

  ;; All functions return Hiccup vectors ready for rendering
  )
