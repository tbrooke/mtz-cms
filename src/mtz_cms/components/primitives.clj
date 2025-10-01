(ns mtz-cms.components.primitives
  "Basic reusable UI components for Mount Zion CMS
   
   These are primitive, composable components that can be used throughout
   the application. They are pure presentation - no business logic.
   
   All components:
   - Accept data as parameters
   - Return Hiccup vectors
   - Have no side effects
   - Can be easily tested in REPL")

;; --- LOADING STATES ---

(defn loading-spinner
  "Loading spinner with message.
   
   Displays an animated spinner with 'Loading...' text.
   Uses Tailwind's animate-spin utility.
   
   Returns: Hiccup vector"
  []
  [:div {:class "flex items-center justify-center py-4"}
   [:div {:class "animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"}]
   [:span {:class "ml-2 text-gray-600"} "Loading..."]])

;; --- MESSAGE COMPONENTS ---

(defn error-message
  "Error message display component.
   
   Args:
     message - String error message to display
   
   Example:
     (error-message \"Failed to load data\")
   
   Returns: Hiccup vector with red error styling"
  [message]
  [:div {:class "bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md"}
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
   
   Returns: Hiccup vector with green success styling"
  [message]
  [:div {:class "bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-md"}
   [:div {:class "flex"}
    [:svg {:class "h-5 w-5 text-green-400 mr-2" :fill "currentColor" :viewBox "0 0 20 20"}
     [:path {:fill-rule "evenodd"
             :d "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
             :clip-rule "evenodd"}]]
    [:span message]]])

;; --- BUTTON COMPONENTS ---

(defn button
  "Styled button component.
   
   Args:
     data - Map with keys:
       :text    - Button text (required)
       :href    - Optional link URL (makes it an <a> tag)
       :on-click - Optional click handler attribute
       :variant - Button style variant:
                  :primary (default) - blue background
                  :secondary - white with border
                  :danger - red background
       :class   - Optional additional CSS classes
       :disabled - Optional boolean for disabled state
   
   Examples:
     (button {:text \"Click me\"})
     (button {:text \"Learn More\" :href \"/about\"})
     (button {:text \"Delete\" :variant :danger :on-click \"handleDelete()\"})
   
   Returns: Hiccup vector for button or link styled as button"
  [{:keys [text href on-click variant class disabled]
    :or {variant :primary}}]
  (let [base-classes "px-6 py-3 rounded-md font-medium transition-colors text-center inline-block"
        variant-classes (case variant
                          :primary "bg-blue-600 text-white hover:bg-blue-700"
                          :secondary "bg-white text-blue-600 border border-blue-600 hover:bg-blue-50"
                          :danger "bg-red-600 text-white hover:bg-red-700"
                          "bg-blue-600 text-white hover:bg-blue-700")
        disabled-classes (when disabled "opacity-50 cursor-not-allowed")
        all-classes (str base-classes " " variant-classes " " disabled-classes " " class)
        attrs (cond-> {:class all-classes}
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

  ;; Test error message
  (error-message "Something went wrong")
  ;; => [:div {:class "bg-red-50 ..."} ...]

  ;; Test success message
  (success-message "Operation completed")
  ;; => [:div {:class "bg-green-50 ..."} ...]

  ;; Test button variants
  (button {:text "Primary Button"})
  ;; => [:button {:class "..."} "Primary Button"]

  (button {:text "Secondary Button" :variant :secondary})
  ;; => [:button {:class "..."} "Secondary Button"]

  (button {:text "Danger Button" :variant :danger})
  ;; => [:button {:class "..."} "Danger Button"]

  ;; Test button as link
  (button {:text "Learn More" :href "/about"})
  ;; => [:a {:class "..." :href "/about"} "Learn More"]

  ;; Test disabled button
  (button {:text "Can't Click" :disabled true})
  ;; => [:button {:class "..." :disabled true} "Can't Click"]

  ;; Test with click handler
  (button {:text "Click Me" :on-click "alert('clicked')"})
  ;; => [:button {:class "..." :onclick "..."} "Click Me"]

  ;; All functions return Hiccup vectors ready for rendering
  )
