(ns mtz-cms.ui.components
  "UI components for Mount Zion CMS"
  (:require
   [mtz-cms.navigation.menu :as menu]))

;; --- TAILWIND CSS CDN ---

(defn tailwind-cdn []
  "Tailwind CSS CDN for development"
  [:script {:src "https://cdn.tailwindcss.com"}])

(defn custom-styles []
  "Custom CSS to complement Tailwind"
  [:style
   "
   /* HTMX loading indicators */
   .htmx-indicator {
     opacity: 0;
     transition: opacity 200ms ease-in;
   }

   .htmx-request .htmx-indicator {
     opacity: 1;
   }

   .htmx-request.htmx-indicator {
     opacity: 1;
   }
   "])

;; --- COMPONENTS ---

(defn site-header
  "Dynamic site header with hierarchical navigation.

   Accepts optional navigation data. If not provided, will use static fallback."
  ([] (site-header nil))
  ([nav]
   (println "site-header received nav:" (pr-str nav))
   [:header {:class "bg-blue-600 text-white shadow-lg"}
    [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"}
     [:div {:class "flex justify-between items-center py-6"}
      ;; Logo/Title
      [:div
       [:a {:href "/" :class "block"}
        [:h1 {:class "text-2xl font-bold"} "Mount Zion UCC"]
        [:p {:class "text-blue-100 text-sm"} "United Church of Christ"]]]

      ;; Navigation Menu
      [:nav {:class "hidden md:flex space-x-6 items-center"}
       (concat
        (if (and nav (seq nav))
          ;; Dynamic navigation from Alfresco
          (for [item nav]
            (if (:has-children? item)
              ;; Dropdown menu for items with submenus
              [:div {:class "relative group"}
               [:button {:class "text-white hover:text-blue-200 transition-colors flex items-center gap-1 py-2"}
                (:label item)
                ;; Dropdown arrow
                [:svg {:class   "w-4 h-4"
                       :fill    "none"
                       :stroke  "currentColor"
                       :viewBox "0 0 24 24"}
                 [:path {:stroke-linecap  "round"
                         :stroke-linejoin "round"
                         :stroke-width    "2"
                         :d               "M19 9l-7 7-7-7"}]]]
               ;; Dropdown panel
               [:div {:class "absolute left-0 mt-2 w-48 bg-white rounded-md shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50"}
                (for [sub (:submenu item)]
                  [:a {:href  (:path sub)
                       :class "block px-4 py-2 text-gray-700 hover:bg-blue-50 hover:text-blue-600 first:rounded-t-md last:rounded-b-md"}
                   (:label sub)])]]

              ;; Regular link (no submenu)
              [:a {:href  (:path item)
                   :class "text-white hover:text-blue-200 transition-colors"}
               (:label item)]))

          ;; Fallback: static navigation
          [[:a {:href  "/"
                :class "text-white hover:text-blue-200 transition-colors"} "Home"]
           [:a {:href  "/about"
                :class "text-white hover:text-blue-200 transition-colors"} "About"]
           [:a {:href  "/worship"
                :class "text-white hover:text-blue-200 transition-colors"} "Worship"]
           [:a {:href  "/events"
                :class "text-white hover:text-blue-200 transition-colors"} "Events"]
           [:a {:href  "/activities"
                :class "text-white hover:text-blue-200 transition-colors"} "Activities"]
           [:a {:href  "/contact"
                :class "text-white hover:text-blue-200 transition-colors"} "Contact"]])

        ;; Demo link (always shown)
        [[:a {:href  "/demo"
              :class "text-blue-200 hover:text-white transition-colors border border-blue-400 px-3 py-1 rounded"} "Demo"]])]

      ;; Mobile menu button (for future mobile implementation)
      [:div {:class "md:hidden"}
       [:button {:class "text-white" :aria-label "Open menu"}
        "☰ Menu"]]]]]))  ;; Now properly closes: button, mobile-div, flex-div, container-div, header

(defn site-footer []
  [:footer {:class "bg-gray-50 border-t border-gray-200 mt-12"}
   [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"}
    [:div {:class "text-center text-gray-600"}
     [:p {:class "text-sm"} "© 2025 Mount Zion United Church of Christ"]
     [:p {:class "text-xs text-gray-500 mt-2"} "Powered by Mount Zion CMS"]]]])

(defn loading-spinner []
  [:div {:class "flex items-center justify-center py-4"}
   [:div {:class "animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"}]
   [:span {:class "ml-2 text-gray-600"} "Loading..."]])

(defn error-message [message]
  [:div {:class "bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md"}
   [:div {:class "flex"}
    [:svg {:class "h-5 w-5 text-red-400 mr-2" :fill "currentColor" :viewBox "0 0 20 20"}
     [:path {:fill-rule "evenodd" :d "M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" :clip-rule "evenodd"}]]
    [:span "Error: " message]]])

(defn success-message [message]
  [:div {:class "bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-md"}
   [:div {:class "flex"}
    [:svg {:class "h-5 w-5 text-green-400 mr-2" :fill "currentColor" :viewBox "0 0 20 20"}
     [:path {:fill-rule "evenodd" :d "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" :clip-rule "evenodd"}]]
    [:span message]]])

(comment
  ;; Test components
  (site-header)
  (site-footer))
