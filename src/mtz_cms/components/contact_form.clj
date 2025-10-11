(ns mtz-cms.components.contact-form
  "Contact form component for Mount Zion CMS

   HTMX-powered contact form with SendGrid integration")

(defn contact-form
  "Contact form with HTMX submission

   Features:
   - Required fields: name, email, subject, message
   - Optional field: phone
   - HTMX POST to /api/contact/submit
   - Client-side validation
   - Loading state during submission
   - Success/error messages"
  []
  [:div {:class "max-w-2xl mx-auto"}
   ;; Form header
   [:div {:class "text-center mb-8"}
    [:h1 {:class "text-4xl font-bold text-gray-900 mb-4"}
     "Contact Us"]
    [:p {:class "text-xl text-gray-600"}
     "We'd love to hear from you! Fill out the form below and we'll get back to you soon."]]

   ;; Contact form
   [:form {:id "contact-form"
           :hx-post "/api/contact/submit"
           :hx-target "#form-response"
           :hx-swap "innerHTML"
           :hx-indicator "#submit-spinner"
           :class "bg-white shadow-lg rounded-lg p-8"}

    ;; Form response area (for success/error messages)
    [:div {:id "form-response" :class "mb-6"}]

    ;; Name field
    [:div {:class "mb-6"}
     [:label {:for "name" :class "block text-sm font-medium text-gray-700 mb-2"}
      "Your Name " [:span {:class "text-red-500"} "*"]]
     [:input {:type "text"
              :id "name"
              :name "name"
              :required true
              :class "w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              :placeholder "John Smith"}]]

    ;; Email field
    [:div {:class "mb-6"}
     [:label {:for "email" :class "block text-sm font-medium text-gray-700 mb-2"}
      "Email Address " [:span {:class "text-red-500"} "*"]]
     [:input {:type "email"
              :id "email"
              :name "email"
              :required true
              :class "w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              :placeholder "john@example.com"}]]

    ;; Phone field (optional)
    [:div {:class "mb-6"}
     [:label {:for "phone" :class "block text-sm font-medium text-gray-700 mb-2"}
      "Phone Number " [:span {:class "text-gray-400 text-sm"} "(optional)"]]
     [:input {:type "tel"
              :id "phone"
              :name "phone"
              :class "w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              :placeholder "555-1234"}]]

    ;; Subject field
    [:div {:class "mb-6"}
     [:label {:for "subject" :class "block text-sm font-medium text-gray-700 mb-2"}
      "Subject " [:span {:class "text-red-500"} "*"]]
     [:input {:type "text"
              :id "subject"
              :name "subject"
              :required true
              :class "w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              :placeholder "How can we help you?"}]]

    ;; Message field
    [:div {:class "mb-6"}
     [:label {:for "message" :class "block text-sm font-medium text-gray-700 mb-2"}
      "Message " [:span {:class "text-red-500"} "*"]]
     [:textarea {:id "message"
                 :name "message"
                 :required true
                 :rows 6
                 :class "w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                 :placeholder "Tell us how we can help you..."}]]

    ;; Submit button
    [:div {:class "flex items-center justify-between"}
     [:button {:type "submit"
               :class "bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors"}
      "Send Message"]

     ;; Loading spinner (shown during submission)
     [:div {:id "submit-spinner" :class "htmx-indicator flex items-center text-blue-600"}
      [:svg {:class "animate-spin h-5 w-5 mr-2" :fill "none" :viewBox "0 0 24 24"}
       [:circle {:class "opacity-25" :cx "12" :cy "12" :r "10" :stroke "currentColor" :stroke-width "4"}]
       [:path {:class "opacity-75" :fill "currentColor" :d "M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"}]]
      [:span "Sending..."]]]]])

(defn success-message
  "Success message shown after form submission"
  [name]
  [:div {:class "bg-green-50 border-2 border-green-500 rounded-lg p-6 mb-6"}
   [:div {:class "flex items-start"}
    [:svg {:class "w-6 h-6 text-green-500 mr-3 flex-shrink-0 mt-0.5"
           :fill "currentColor"
           :viewBox "0 0 20 20"}
     [:path {:fill-rule "evenodd"
             :d "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
             :clip-rule "evenodd"}]]
    [:div
     [:h3 {:class "text-lg font-semibold text-green-900 mb-2"}
      "Message Sent Successfully!"]
     [:p {:class "text-green-800 mb-2"}
      "Thank you, " name "! We've received your message and sent a confirmation to your email."]
     [:p {:class "text-green-700 text-sm"}
      "We'll respond to your inquiry within 1-2 business days."]
     [:div {:class "mt-4"}
      [:a {:href "/"
           :class "inline-block bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition-colors"}
       "← Back to Home"]]]]])

(defn error-message
  "Error message shown if submission fails"
  [error-text]
  [:div {:class "bg-red-50 border-2 border-red-500 rounded-lg p-6 mb-6"}
   [:div {:class "flex items-start"}
    [:svg {:class "w-6 h-6 text-red-500 mr-3 flex-shrink-0 mt-0.5"
           :fill "currentColor"
           :viewBox "0 0 20 20"}
     [:path {:fill-rule "evenodd"
             :d "M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
             :clip-rule "evenodd"}]]
    [:div
     [:h3 {:class "text-lg font-semibold text-red-900 mb-2"}
      "Error Sending Message"]
     [:p {:class "text-red-800 mb-2"}
      error-text]
     [:p {:class "text-red-700 text-sm"}
      "Please try again or contact us directly at office@mtzcg.com"]]]])

;; --- REPL TESTING ---

(comment
  ;; Test form rendering
  (contact-form)

  ;; Test success message
  (success-message "John Smith")

  ;; Test error message
  (error-message "Failed to send email. Please try again."))
