(ns mtz-cms.components.navigation
  "Navigation UI components - renders navigation HTML only

   IMPORTANT: This file does NOT build navigation data.
   Data comes from navigation/menu.clj which handles:
   - Fetching from Alfresco
   - Building menu structure
   - Discovering submenus

   This file ONLY renders the HTML.

   Styling: Uses design system for consistent, maintainable styles."
  (:require
   [mtz-cms.ui.design-system :as ds]))

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

   If nav-data is empty/nil, renders fallback static navigation.

   Uses design system for consistent styling."
  [nav-data]
  (let [;; Filter out Contact from navigation
        filtered-nav (remove #(= (:key %) :contact) nav-data)
        ;; Split navigation items in half
        mid-point (quot (count filtered-nav) 2)
        left-nav (take mid-point filtered-nav)
        right-nav (drop mid-point filtered-nav)

        ;; Helper to render nav item
        render-nav-item (fn [item]
                         (if (:has-children? item)
                           ;; Dropdown menu for items with submenus
                           [:div {:class "relative group"}
                            [:button {:class (ds/classes [(ds/text :text-on-dark)
                                                          "hover:text-gray-600"
                                                          (ds/transition :colors)
                                                          "flex items-center gap-1"
                                                          (ds/py :sm)
                                                          (ds/text-size :lg)
                                                          "font-menu"])}
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
                            [:div {:class (ds/classes ["absolute left-0 mt-2 w-48"
                                                      (ds/bg :bg-header)
                                                      (ds/shadow :lg)
                                                      "border border-black"
                                                      "opacity-0 invisible"
                                                      "group-hover:opacity-100 group-hover:visible"
                                                      (ds/transition :all)
                                                      (ds/duration :normal)
                                                      "z-50"])}
                             (for [sub (:submenu item)]
                               [:a {:href  (:path sub)
                                    :class (ds/classes ["block"
                                                       (ds/px :md)
                                                       (ds/py :sm)
                                                       (ds/text :text-on-dark)
                                                       "hover:bg-gray-200"
                                                       "first:rounded-t-md last:rounded-b-md"
                                                       "font-menu"])}
                                (:label sub)])]]

                           ;; Regular link (no submenu)
                           [:a {:href  (:path item)
                                :class (ds/classes [(ds/text :text-on-dark)
                                                   "hover:text-gray-600"
                                                   (ds/transition :colors)
                                                   (ds/text-size :lg)
                                                   "font-menu"])}
                            (:label item)]))]

    [:header {:class (ds/classes [(ds/bg :bg-header)
                                  (ds/text :text-on-dark)
                                  "border-b border-black"])}
     [:div {:class (ds/container :7xl)}
      [:div {:class (ds/classes ["flex justify-center items-center"
                                 (ds/py :lg)
                                 "gap-16"])}

       ;; Left Navigation
       [:nav {:class "hidden md:flex space-x-6 items-center"}
        (if (seq left-nav)
          (for [item left-nav]
            (render-nav-item item))
          ;; Fallback static left nav
          (let [nav-link-class (ds/classes [(ds/text :text-on-dark)
                                           "hover:text-gray-600"
                                           (ds/transition :colors)
                                           (ds/text-size :lg)
                                           "font-menu"])]
            (list
             [:a {:href "/" :class nav-link-class} "Home"]
             [:a {:href "/about" :class nav-link-class} "About"]
             [:a {:href "/worship" :class nav-link-class} "Worship"]
             [:a {:href "/events" :class nav-link-class} "Events"])))]

       ;; Center Logo/Title
       [:div {:class "flex-shrink-0 px-8"}
        [:a {:href "/" :class "block text-center"}
         [:h1 {:class (ds/classes [(ds/text-size :4xl)
                                   (ds/font-weight :bold)
                                   "font-garamond"])}
          "MT ZION UCC"]]]

       ;; Right Navigation
       [:nav {:class "hidden md:flex space-x-6 items-center"}
        (if (seq right-nav)
          (for [item right-nav]
            (render-nav-item item))
          ;; Fallback static right nav
          (let [nav-link-class (ds/classes [(ds/text :text-on-dark)
                                           "hover:text-gray-600"
                                           (ds/transition :colors)
                                           (ds/text-size :lg)
                                           "font-menu"])]
            (list
             [:a {:href "/activities" :class nav-link-class} "Activities"]
             [:a {:href "/news" :class nav-link-class} "News"]
             [:a {:href "/outreach" :class nav-link-class} "Outreach"]
             [:a {:href "/preschool" :class nav-link-class} "Preschool"])))]

       ;; Mobile menu button (placeholder for future)
       [:div {:class "md:hidden"}
        [:button {:class "text-white" :aria-label "Open menu"}
         "☰ Menu"]]]]]))

(defn site-footer
  "Renders site footer with Join Our Community call-to-action.

   Includes community invitation and action buttons.
   Uses design system for consistent styling with light teal background."
  []
  [:footer {:class (ds/classes [(ds/bg :bg-header)  ; Light teal background
                                "border-t"
                                (ds/border-color :border-default)
                                (ds/mt :3xl)])}
   [:div {:class (ds/classes [(ds/container :7xl)
                              (ds/py :3xl)])}

    ;; Join Our Community section
    [:div {:class "lg:grid lg:grid-cols-2 lg:gap-8 lg:items-center mb-8"}
     [:div
      [:h2 {:class (ds/classes [(ds/text-size :3xl)
                                (ds/font-weight :bold)
                                "tracking-tight"
                                (ds/text :text-primary)
                                "sm:text-4xl"])}
       "Join Our Community"]
      [:p {:class (ds/classes [(ds/mt :md)
                               "max-w-3xl"
                               (ds/text-size :lg)
                               (ds/text :text-secondary)])}
       "Experience the warmth and fellowship of Mount Zion UCC. All are welcome in our progressive Christian community."]]
     [:div {:class "mt-8 lg:mt-0"}
      [:div {:class "inline-flex rounded-md shadow"}
       [:a {:href "/worship"
            :class (ds/button :primary)}
        "Plan Your Visit"]]
      [:div {:class "ml-3 inline-flex"}
       [:a {:href "/contact"
            :class (ds/button :secondary)}
        "Contact Us"]]]]

    ;; Copyright section
    [:div {:class (ds/classes ["text-center"
                               (ds/text :text-secondary)
                               (ds/mt :2xl)
                               "border-t"
                               (ds/border-color :border-default)
                               "pt-8"])}
     [:p {:class (ds/text-size :sm)}
      "© 2025 Mount Zion United Church of Christ"]
     [:p {:class (ds/classes [(ds/text-size :xs)
                             (ds/text :text-muted)
                             (ds/mt :sm)])}
      "Powered by Mount Zion CMS"]]]])

(defn breadcrumbs
  "Renders breadcrumb navigation.

   Accepts crumbs vector:
   [{:label \"Home\" :path \"/\"}
    {:label \"About\" :path \"/about\"}
    {:label \"Our Story\" :path \"/about/story\"}]

   Last item is not clickable (current page).
   Uses design system for consistent styling."
  [crumbs]
  (when (seq crumbs)
    [:nav {:class (ds/classes ["flex items-center space-x-2"
                               (ds/text-size :sm)
                               (ds/text :text-secondary)
                               (ds/mb :md)])}
     (for [[idx crumb] (map-indexed vector crumbs)]
       (let [is-last? (= idx (dec (count crumbs)))]
         [:span {:key idx :class "flex items-center"}
          (if is-last?
            ;; Current page - not clickable
            [:span {:class (ds/classes [(ds/text :text-primary)
                                       (ds/font-weight :medium)])}
             (:label crumb)]
            ;; Link to parent page
            [:a {:href (:path crumb)
                 :class (ds/classes [(ds/text :primary)
                                    (ds/hover-text :primary-dark)
                                    "hover:underline"])}
             (:label crumb)])

          ;; Chevron separator (except after last item)
          (when-not is-last?
            [:svg {:class (ds/classes ["w-4 h-4 mx-2"
                                      (ds/text :text-light)])
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
