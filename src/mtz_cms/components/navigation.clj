(ns mtz-cms.components.navigation
  "Navigation UI components - renders navigation HTML only

   IMPORTANT: This file does NOT build navigation data.
   Data comes from navigation/menu.clj which handles:
   - Fetching from Alfresco
   - Building menu structure
   - Discovering submenus

   This file ONLY renders the HTML.")

;; --- ARCHITECTURE NOTE ---
;;
;; Navigation is split into two concerns:
;;
;; 1. DATA (navigation/menu.clj):
;;    - Fetches menu items from Alfresco
;;    - Builds navigation structure with submenus
;;    - Returns nav-data vector
;;
;; 2. RENDERING (this file):
;;    - Receives nav-data as parameter
;;    - Renders HTML with Hiccup
;;    - No Alfresco calls, no data fetching
;;
;; This separation allows:
;; - Testing rendering without Alfresco
;; - Reusing nav data in multiple places
;; - Clear separation of concerns

;; --- NAVIGATION COMPONENTS ---

(defn site-header
  "Renders site header with dynamic navigation.

   Accepts nav-data vector with structure:
   [{:key :home
     :label \"Home\"
     :path \"/\"
     :has-children? false}
    {:key :activities
     :label \"Activities\"
     :path \"/activities\"
     :has-children? true
     :submenu [{:label \"Pickle Ball\" :path \"/activities/pickle-ball\" :node-id \"123\"}]}]

   If nav-data is empty/nil, renders fallback static navigation."
  [nav-data]
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
       (if (seq nav-data)
         ;; Dynamic navigation from data
         (for [item nav-data]
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
              ;; Dropdown panel - SIMPLE BOX, NO ROUNDED CORNERS
              [:div {:class "absolute left-0 mt-2 w-48 bg-white shadow-lg border border-gray-200 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50"}
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

     ;; Mobile menu button (placeholder for future)
     [:div {:class "md:hidden"}
      [:button {:class "text-white" :aria-label "Open menu"}
       "☰ Menu"]]]]])

(defn site-footer
  "Renders site footer.

   Simple footer with copyright and branding."
  []
  [:footer {:class "bg-gray-50 border-t border-gray-200 mt-12"}
   [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"}
    [:div {:class "text-center text-gray-600"}
     [:p {:class "text-sm"} "© 2025 Mount Zion United Church of Christ"]
     [:p {:class "text-xs text-gray-500 mt-2"} "Powered by Mount Zion CMS"]]]])

(defn breadcrumbs
  "Renders breadcrumb navigation.

   Accepts crumbs vector:
   [{:label \"Home\" :path \"/\"}
    {:label \"About\" :path \"/about\"}
    {:label \"Our Story\" :path \"/about/story\"}]

   Last item is not clickable (current page)."
  [crumbs]
  (when (seq crumbs)
    [:nav {:class "flex items-center space-x-2 text-sm text-gray-600 mb-4"}
     (for [[idx crumb] (map-indexed vector crumbs)]
       (let [is-last? (= idx (dec (count crumbs)))]
         [:span {:key idx :class "flex items-center"}
          (if is-last?
            ;; Current page - not clickable
            [:span {:class "text-gray-900 font-medium"} (:label crumb)]
            ;; Link to parent page
            [:a {:href (:path crumb)
                 :class "text-blue-600 hover:text-blue-800 hover:underline"}
             (:label crumb)])

          ;; Chevron separator (except after last item)
          (when-not is-last?
            [:svg {:class "w-4 h-4 mx-2 text-gray-400"
                   :fill "none"
                   :stroke "currentColor"
                   :viewBox "0 0 24 24"}
             [:path {:stroke-linecap "round"
                     :stroke-linejoin "round"
                     :stroke-width "2"
                     :d "M9 5l7 7-7 7"}]])]))]))

;; --- REPL TESTING ---

(comment
  ;; Test with empty nav
  (site-header [])

  ;; Test with simple nav
  (site-header [{:key :home :label "Home" :path "/" :has-children? false}
                {:key :about :label "About" :path "/about" :has-children? false}])

  ;; Test with dropdown
  (site-header [{:key :home :label "Home" :path "/" :has-children? false}
                {:key :activities
                 :label "Activities"
                 :path "/activities"
                 :has-children? true
                 :submenu [{:label "Pickle Ball" :path "/activities/pickle-ball" :node-id "123"}
                           {:label "Youth Group" :path "/activities/youth-group" :node-id "456"}]}])

  ;; Test footer
  (site-footer)

  ;; Test breadcrumbs
  (breadcrumbs [{:label "Home" :path "/"}
                {:label "About" :path "/about"}
                {:label "Our Story" :path "/about/story"}]))
