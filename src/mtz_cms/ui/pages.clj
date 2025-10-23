(ns mtz-cms.ui.pages
  "Page templates for Mount Zion CMS

   All pages use the unified base-page from ui/base.clj.
   This file only contains page-specific content composition."
  (:require
   [mtz-cms.ui.base :as base]
   [mtz-cms.ui.layouts :as layouts]
   [mtz-cms.components.contact-form :as contact-form]
   [mtz-cms.components.events :as events]
   [hiccup.compiler :as compiler]))

;; --- RAW HTML HELPER ---

(deftype RawHtml [html]
  hiccup.compiler/HtmlRenderer
  (render-html [_] html))

(defn strip-html-wrapper
  "Strips HTML document wrapper tags and inline styles from content.

   Removes:
   - <!DOCTYPE html>
   - <html>, <head>, <style>, <body> tags
   - Inline style attributes from all elements
   - Returns just the body content for styling with design system

   Example:
   Input: '<!DOCTYPE html><html><head><style>...</style></head><body><h1>Title</h1></body></html>'
   Output: '<h1>Title</h1>'"
  [html-string]
  (if (clojure.string/blank? html-string)
    ""
    (-> html-string
        ;; Remove DOCTYPE
        (clojure.string/replace #"(?i)<!DOCTYPE[^>]*>" "")
        ;; Remove opening html tag
        (clojure.string/replace #"(?i)<html[^>]*>" "")
        ;; Remove closing html tag
        (clojure.string/replace #"(?i)</html>" "")
        ;; Remove entire head section (including styles)
        (clojure.string/replace #"(?is)<head[^>]*>.*?</head>" "")
        ;; Remove opening body tag
        (clojure.string/replace #"(?i)<body[^>]*>" "")
        ;; Remove closing body tag
        (clojure.string/replace #"(?i)</body>" "")
        ;; Remove inline style attributes from all elements
        (clojure.string/replace #"(?i)\s+style=\"[^\"]*\"" "")
        (clojure.string/replace #"(?i)\s+style='[^']*'" "")
        ;; Trim whitespace
        clojure.string/trim)))

(defn raw-html
  "Wraps HTML string so Hiccup won't escape it.
   Use this for HTML content from Alfresco that should be rendered as-is."
  [html-string]
  (RawHtml. html-string))

(defn clean-html
  "Cleans HTML content by stripping wrapper tags and inline styles,
   then wraps it for safe rendering.

   Use this for Alfresco content to ensure design system styles apply."
  [html-string]
  (raw-html (strip-html-wrapper html-string)))

;; --- COMPATIBILITY LAYER ---
;;
;; base-layout is kept for backward compatibility during transition.
;; New code should use base/base-page directly.

(defn base-layout
  "DEPRECATED: Use mtz-cms.ui.base/base-page instead.

   This wrapper exists for backward compatibility during refactoring."
  ([title content] (base/base-page title content nil))
  ([title content ctx] (base/base-page title content ctx)))

;; --- PAGE TEMPLATES ---

(defn home-page [page-data]
  (base-layout
    "Mount Zion UCC - Home"
    [:div
     ;; Hero Section
     [:div {:class "bg-white rounded-lg shadow-sm p-8 mb-8"}
      [:h1 {:class "text-4xl font-bold text-gray-900 mb-4"} "Welcome to Mount Zion UCC"]
      [:p {:class "text-xl text-gray-600 mb-6"} "A community of faith, hope, and love."]
      [:div {:class "flex flex-col sm:flex-row gap-4"}
       [:a {:href "/about" :class "bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700 transition-colors text-center"} "Learn About Us"]
       [:a {:href "/worship" :class "bg-white text-blue-600 border border-blue-600 px-6 py-3 rounded-md hover:bg-blue-50 transition-colors text-center"} "Join Us for Worship"]]]
     
     ;; Dynamic Content
     (when page-data
       [:div {:class "bg-white rounded-lg shadow-sm p-6 mb-8"}
        [:h2 {:class "text-2xl font-semibold text-gray-900 mb-4"} (:page/title page-data)]
        [:div {:class "prose prose-2xl max-w-none"}
         (clean-html (or (:page/content page-data) ""))]])
     
     ;; Quick Links
     [:div {:class "bg-white rounded-lg shadow-sm p-6"}
      [:h2 {:class "text-2xl font-semibold text-gray-900 mb-6"} "Quick Links"]
      [:div {:class "grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4"}
       [:a {:href "/about" :class "group block p-4 border border-gray-200 rounded-lg hover:border-blue-300 hover:shadow-md transition-all"}
        [:h3 {:class "font-medium text-gray-900 group-hover:text-blue-600"} "About Us"]
        [:p {:class "text-sm text-gray-500 mt-1"} "Our story and mission"]]
       [:a {:href "/worship" :class "group block p-4 border border-gray-200 rounded-lg hover:border-blue-300 hover:shadow-md transition-all"}
        [:h3 {:class "font-medium text-gray-900 group-hover:text-blue-600"} "Worship"]
        [:p {:class "text-sm text-gray-500 mt-1"} "Service times and style"]]
       [:a {:href "/events" :class "group block p-4 border border-gray-200 rounded-lg hover:border-blue-300 hover:shadow-md transition-all"}
        [:h3 {:class "font-medium text-gray-900 group-hover:text-blue-600"} "Events"]
        [:p {:class "text-sm text-gray-500 mt-1"} "Upcoming activities"]]
       [:a {:href "/contact" :class "group block p-4 border border-gray-200 rounded-lg hover:border-blue-300 hover:shadow-md transition-all"}
        [:h3 {:class "font-medium text-gray-900 group-hover:text-blue-600"} "Contact"]
        [:p {:class "text-sm text-gray-500 mt-1"} "Get in touch with us"]]]]]))

(defn about-page [page-data ctx]
  (base-layout
    "About Us - Mount Zion UCC"
    [:div
     [:h1 "About Mount Zion UCC"]

     (if page-data
       [:div {:class "page-content"}
        [:div (clean-html (or (:page/content page-data) ""))]]
       [:div {:class "placeholder"}
        [:p "Our story of faith, community, and service."]])

     [:div {:class "ministry-areas"}
      [:h2 "Ministry Areas"]
      [:div {:class "grid"}
       [:div {:class "card"}
        [:h3 "Worship"]
        [:p "Join us for meaningful worship services."]]
       [:div {:class "card"}
        [:h3 "Education"]
        [:p "Growing in faith through learning."]]
       [:div {:class "card"}
        [:h3 "Community"]
        [:p "Building connections and fellowship."]]]]]
    ctx))

(defn demo-page [data ctx]
  (base-layout
    "Demo - Mount Zion CMS"
    [:div
     ;; Header
     [:div {:class "bg-gradient-to-r from-green-500 to-blue-600 text-white rounded-lg shadow-xl p-8 mb-8"}
      [:h1 {:class "text-4xl font-bold mb-2"} "🚀 Mount Zion CMS Demo"]
      [:p {:class "text-green-100 text-lg"} "Testing our Alfresco → Pathom → HTMX stack"]]
     
     ;; Status Grid
     [:div {:class "grid md:grid-cols-2 gap-6 mb-8"}
      ;; Pathom Integration
      [:div {:class "bg-white rounded-lg shadow-md p-6 border-l-4 border-green-500"}
       [:div {:class "flex items-center mb-4"}
        [:div {:class "bg-green-100 rounded-full p-2 mr-3"}
         [:svg {:class "w-6 h-6 text-green-600" :fill "currentColor" :viewBox "0 0 20 20"}
          [:path {:d "M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"}]]]
        [:h2 {:class "text-xl font-bold text-gray-900"} "Pathom Integration"]]
       [:p {:class "text-gray-600 mb-2"} [:strong "Test Greeting:"] " " (:greeting data)]
       [:div {:class "bg-green-50 text-green-700 px-3 py-2 rounded-md text-sm font-medium"}
        "✅ Pathom working!"]]
      
      ;; Alfresco Connection
      [:div {:class (str "bg-white rounded-lg shadow-md p-6 border-l-4 " 
                         (if (get-in data [:alfresco :success]) "border-blue-500" "border-red-500"))}
       [:div {:class "flex items-center mb-4"}
        [:div {:class (str "rounded-full p-2 mr-3 " 
                           (if (get-in data [:alfresco :success]) "bg-blue-100" "bg-red-100"))}
         [:svg {:class (str "w-6 h-6 " 
                           (if (get-in data [:alfresco :success]) "text-blue-600" "text-red-600")) 
                :fill "currentColor" :viewBox "0 0 20 20"}
          [:path {:d "M10 12a2 2 0 100-4 2 2 0 000 4z"}]
          [:path {:fill-rule "evenodd" :d "M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" :clip-rule "evenodd"}]]]
        [:h2 {:class "text-xl font-bold text-gray-900"} "Alfresco Connection"]]
       [:p {:class "text-gray-600 mb-2"} [:strong "Status:"] " " (get-in data [:alfresco :message])]
       (when (get-in data [:alfresco :success])
         [:div {:class "bg-blue-50 text-blue-700 px-3 py-2 rounded-md text-sm"}
          [:p [:strong "Company Home:"] " " (get-in data [:alfresco :company-home])]
          [:p {:class "text-green-600 font-medium mt-1"} "✅ Connected to Alfresco!"]])]]
     
     ;; HTMX Demo Section  
     [:div {:class "bg-white rounded-lg shadow-md p-6 mb-8"}
      [:h2 {:class "text-xl font-bold text-gray-900 mb-4 flex items-center"}
       [:span {:class "text-2xl mr-2"} "⚡"]
       "HTMX Interactive Test"]
      [:p {:class "text-gray-600 mb-4"} "Test dynamic content loading without page refresh:"]
      [:button {:hx-get "/api/time"
                :hx-target "#time-result"
                :class "bg-purple-600 hover:bg-purple-700 text-white font-semibold py-2 px-4 rounded-md transition-colors"}
       "Get Server Time"]
      [:div {:id "time-result" :class "mt-4 p-4 bg-gray-50 rounded-md border-2 border-dashed border-gray-300 text-gray-500 text-center"}
       "Click button to test HTMX"]]
     
     ;; Architecture Overview
     [:div {:class "bg-white rounded-lg shadow-md p-6"}
      [:h2 {:class "text-xl font-bold text-gray-900 mb-4 flex items-center"}
       [:span {:class "text-2xl mr-2"} "🎯"]
       "Clean Architecture Stack"]
      [:div {:class "grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4"}
       [:div {:class "text-center p-4 bg-blue-50 rounded-lg"}
        [:div {:class "text-2xl mb-2"} "🗂️"]
        [:div {:class "font-semibold text-blue-900"} "Alfresco"]
        [:div {:class "text-xs text-blue-600"} "Content Repository"]]
       [:div {:class "text-center p-4 bg-green-50 rounded-lg"}
        [:div {:class "text-2xl mb-2"} "🔍"]
        [:div {:class "font-semibold text-green-900"} "Pathom"]
        [:div {:class "text-xs text-green-600"} "Data Resolution"]]
       [:div {:class "text-center p-4 bg-purple-50 rounded-lg"}
        [:div {:class "text-2xl mb-2"} "🛣️"]
        [:div {:class "font-semibold text-purple-900"} "Reitit"]
        [:div {:class "text-xs text-purple-600"} "HTTP Routing"]]
       [:div {:class "text-center p-4 bg-orange-50 rounded-lg"}
        [:div {:class "text-2xl mb-2"} "⚡"]
        [:div {:class "font-semibold text-orange-900"} "HTMX"]
        [:div {:class "text-xs text-orange-600"} "Frontend Magic"]]
       [:div {:class "text-center p-4 bg-pink-50 rounded-lg"}
        [:div {:class "text-2xl mb-2"} "🎨"]
        [:div {:class "font-semibold text-pink-900"} "Tailwind"]
        [:div {:class "text-xs text-pink-600"} "Beautiful UI"]]]]]))

;; --- COMPONENT-BASED PAGES ---

(defn component-home-page [component-data]
  "Component-based home page using layouts and components"
  (base-layout 
    "Mount Zion UCC - Home"
    (layouts/render-layout (:home/layout component-data) component-data)))

;; --- DYNAMIC PAGES ---

(defn dynamic-page [page-data ctx]
  "Template for dynamically discovered pages"
  (base-layout
    (:page/title page-data)
    [:div
     [:h1 {:class "text-3xl font-bold text-gray-900 mb-6"}
      (:page/title page-data)]

     [:div {:class "bg-white rounded-lg shadow-sm p-6 mb-8"}
      [:div {:class "prose prose-2xl max-w-none"}
       ;; Render HTML content from Alfresco with cleaned markup
       ;; This strips inline styles to allow design system to apply
       (clean-html (or (:page/content page-data) ""))]]

     [:div {:class "bg-blue-50 rounded-lg p-4"}
      [:p {:class "text-sm text-blue-600"}
       "📁 This is a dynamically discovered page from Alfresco"]
      [:p {:class "text-xs text-blue-500 mt-1"}
       "Node ID: " (:page/node-id page-data)]]]
    ctx))

(defn pages-list-page [{:keys [pages navigation]}]
  "Template for listing all discovered pages"
  (base-layout 
    "All Pages - Mount Zion UCC"
    [:div
     [:h1 {:class "text-3xl font-bold text-gray-900 mb-6"} "All Pages"]
     
     [:div {:class "bg-green-50 rounded-lg p-4 mb-6"}
      [:h2 {:class "text-lg font-semibold text-green-800 mb-2"} 
       "🚀 Dynamic Page Discovery"]
      [:p {:class "text-green-600 text-sm"}
       "These pages are automatically discovered from your Alfresco Web Site folder. "
       "Add a new folder there and it will appear here!"]]
     
     [:div {:class "grid gap-4 md:grid-cols-2 lg:grid-cols-3"}
      (for [page pages]
        [:div {:key (:page/key page)
               :class "bg-white rounded-lg shadow-sm p-6 border border-gray-200 hover:border-blue-300 hover:shadow-md transition-all"}
         [:h3 {:class "text-lg font-semibold text-gray-900 mb-2"}
          (:page/name page)]
         [:p {:class "text-sm text-gray-600 mb-3"}
          "Slug: " (:page/slug page)]
         [:div {:class "flex gap-2"}
          [:a {:href (str "/page/" (:page/slug page))
               :class "bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700 transition-colors"}
           "View Page"]
          [:span {:class "bg-gray-100 text-gray-600 px-3 py-1 rounded text-xs"}
           "Node: " (subs (:page/node-id page) 0 8) "..."]]])]
     
     [:div {:class "mt-8 bg-white rounded-lg shadow-sm p-6"}
      [:h2 {:class "text-xl font-semibold text-gray-900 mb-4"} "Navigation Items"]
      [:div {:class "space-y-2"}
       (for [nav-item navigation]
         [:div {:key (:nav/key nav-item)
                :class "flex justify-between items-center py-2 px-3 bg-gray-50 rounded"}
          [:span {:class "font-medium"} (:nav/label nav-item)]
          [:a {:href (:nav/path nav-item)
               :class "text-blue-600 hover:text-blue-800 text-sm"}
           (:nav/path nav-item)]])]]]))

(defn not-found-page [slug]
  "Template for 404 pages"
  (base-layout
    "Page Not Found - Mount Zion UCC"
    [:div {:class "text-center py-12"}
     [:h1 {:class "text-4xl font-bold text-gray-900 mb-4"} "Page Not Found"]
     [:p {:class "text-xl text-gray-600 mb-6"}
      "The page \"" slug "\" could not be found."]
     [:div {:class "bg-yellow-50 rounded-lg p-6 mb-6"}
      [:p {:class "text-yellow-800"}
       "This page might not exist in the Alfresco Web Site folder, or there might be a connection issue."]]
     [:a {:href "/pages"
          :class "bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700 transition-colors"}
      "View All Available Pages"]]))

;; --- FEATURE DETAIL PAGE ---

(defn feature-detail-page
  "Template for individual feature detail pages

   Data structure:
   {:feature/title \"Welcome to Mount Zion\"
    :feature/content \"<div>...HTML content...</div>\"
    :feature/image {:url \"/proxy/image/123\" :alt \"Image\"}
    :feature/id \"feature1\"}"
  [feature-data ctx]
  (base-layout
   (str (:feature/title feature-data "Feature") " - Mount Zion UCC")
   [:div
    ;; Breadcrumb navigation
    [:nav {:class "mb-6"}
     [:ol {:class "flex items-center space-x-2 text-sm text-gray-600"}
      [:li
       [:a {:href "/" :class "hover:text-blue-600"} "Home"]]
      [:li [:span {:class "mx-2"} "→"]]
      [:li {:class "text-gray-900 font-medium"}
       (:feature/title feature-data)]]]

    ;; Feature content container
    [:div {:class "bg-white rounded-lg shadow-lg overflow-hidden"}

     ;; Hero image (if available)
     (when-let [image (:feature/image feature-data)]
       [:div {:class "h-64 md:h-96 bg-gradient-to-br from-blue-50 to-blue-100 overflow-hidden"}
        [:img {:src (:url image)
               :alt (or (:alt image) (:feature/title feature-data))
               :class "w-full h-full object-cover"}]])

     ;; Content section
     [:div {:class "p-8 md:p-12"}
      ;; Title
      [:h1 {:class "text-4xl md:text-5xl font-bold text-gray-900 mb-6"}
       (or (:feature/title feature-data) "Untitled Feature")]

      ;; Feature content (full HTML)
      [:div {:class "prose prose-2xl max-w-none mb-8"}
       (if (:feature/content feature-data)
         [:div {:dangerouslySetInnerHTML {:__html (:feature/content feature-data)}}]
         [:p {:class "text-gray-500 italic"} "No content available for this feature."])]

      ;; Back to home button
      [:div {:class "pt-6 border-t border-gray-200"}
       [:a {:href "/"
            :class "inline-flex items-center text-blue-600 hover:text-blue-800 font-medium"}
        [:svg {:class "mr-2 w-5 h-5"
               :fill "none"
               :stroke "currentColor"
               :viewBox "0 0 24 24"}
         [:path {:stroke-linecap "round"
                 :stroke-linejoin "round"
                 :stroke-width "2"
                 :d "M15 19l-7-7 7-7"}]]
        [:span "Back to Home"]]]]]]
   ctx))

;; --- HERO DETAIL PAGE ---

(defn hero-detail-page
  "Template for individual hero image detail pages

   Data structure:
   {:hero/id \"img-123\"
    :hero/title \"Welcome to Mount Zion\"
    :hero/description \"Join us for worship...\"
    :hero/content \"<div>...additional HTML content...</div>\"  ; optional
    :hero/image {:url \"/api/image/123\" :alt \"Church\"}}     ; the image

   Displays:
   - Image centered at top
   - Title centered below (H1)
   - Description below as lead paragraph
   - Additional content if available
   - Back to home link"
  [hero-data ctx]
  (base-layout
   (str (:hero/title hero-data "Hero") " - Mount Zion UCC")
   [:div
    ;; Breadcrumb navigation
    [:nav {:class "mb-6"}
     [:ol {:class "flex items-center space-x-2 text-sm text-gray-600"}
      [:li
       [:a {:href "/" :class "hover:text-blue-600"} "Home"]]
      [:li [:span {:class "mx-2"} "→"]]
      [:li {:class "text-gray-900 font-medium"}
       (:hero/title hero-data)]]]

    ;; Hero content container
    [:div {:class "bg-white rounded-lg shadow-lg overflow-hidden max-w-4xl mx-auto"}

     ;; Hero image centered at top
     (when-let [image (:hero/image hero-data)]
       [:div {:class "flex justify-center bg-gray-50 p-8"}
        [:div {:class "relative w-full max-w-3xl overflow-hidden rounded-lg shadow-md"
               :style "padding-bottom: 56.25%;"} ;; 16:9 aspect ratio
         [:img {:src (:url image)
                :alt (or (:alt image) (:hero/title hero-data))
                :class "absolute inset-0 w-full h-full object-cover"}]]])

     ;; Content section
     [:div {:class "p-8 md:p-12"}
      ;; Title centered
      [:h1 {:class "text-4xl md:text-5xl font-bold text-gray-900 mb-6 text-center"}
       (or (:hero/title hero-data) "Untitled")]

      ;; Description as lead paragraph
      (when (:hero/description hero-data)
        [:p {:class "text-xl text-gray-700 mb-8 text-center leading-relaxed"}
         (:hero/description hero-data)])

      ;; Additional content (if provided)
      (when (:hero/content hero-data)
        [:div {:class "prose prose-2xl max-w-none mb-8 border-t border-gray-200 pt-8"}
         [:div {:dangerouslySetInnerHTML {:__html (:hero/content hero-data)}}]])

      ;; Back to home button
      [:div {:class "pt-6 border-t border-gray-200 text-center"}
       [:a {:href "/"
            :class "inline-flex items-center text-blue-600 hover:text-blue-800 font-medium"}
        [:svg {:class "mr-2 w-5 h-5"
               :fill "none"
               :stroke "currentColor"
               :viewBox "0 0 24 24"}
         [:path {:stroke-linecap "round"
                 :stroke-linejoin "round"
                 :stroke-width "2"
                 :d "M15 19l-7-7 7-7"}]]
        [:span "Back to Home"]]]]]
   ]  ; Close outer div from line 330
   ctx))

;; --- CONTACT PAGE ---

(defn contact-page
  "Contact page with SendGrid-powered contact form"
  [ctx]
  (base-layout
   "Contact Us - Mount Zion UCC"
   [:div {:class "py-12"}
    (contact-form/contact-form)]
   ctx))

;; --- EVENTS PAGES ---

(defn events-page
  "Events listing page"
  [ctx]
  (base-layout
   "Events - Mount Zion UCC"
   (events/events-list-page ctx)
   ctx))

(defn calendar-page
  "Calendar page with week/month views"
  [ctx]
  (base-layout
   "Calendar - Mount Zion UCC"
   (events/calendar-page ctx)
   ctx))

(defn privacy-page
  "Privacy Policy page"
  [ctx]
  (base-layout
   "Privacy Policy - Mount Zion UCC"
   [:div {:class "max-w-4xl mx-auto px-6 py-12"}
    [:h1 {:class "text-4xl font-bold text-gray-900 mb-8"} "Privacy Policy"]

    [:div {:class "prose prose-lg max-w-none"}
     [:p {:class "text-gray-600 mb-6"}
      "Last updated: January 2025"]

     [:h2 {:class "text-2xl font-bold text-gray-900 mt-8 mb-4"} "Information We Collect"]
     [:p {:class "text-gray-700 mb-4"}
      "Mount Zion United Church of Christ collects information that you provide directly to us when you:"]
     [:ul {:class "list-disc pl-6 mb-6 text-gray-700"}
      [:li "Contact us through our website forms"]
      [:li "Subscribe to our newsletter or communications"]
      [:li "Register for events or activities"]
      [:li "Make donations or participate in church activities"]]

     [:h2 {:class "text-2xl font-bold text-gray-900 mt-8 mb-4"} "How We Use Your Information"]
     [:p {:class "text-gray-700 mb-4"}
      "We use the information we collect to:"]
     [:ul {:class "list-disc pl-6 mb-6 text-gray-700"}
      [:li "Respond to your inquiries and communicate with you"]
      [:li "Send you information about church events and activities"]
      [:li "Process donations and event registrations"]
      [:li "Improve our website and services"]]

     [:h2 {:class "text-2xl font-bold text-gray-900 mt-8 mb-4"} "Information Sharing"]
     [:p {:class "text-gray-700 mb-6"}
      "We do not sell, trade, or rent your personal information to third parties. We may share information with trusted service providers who assist us in operating our website and conducting church activities, provided they agree to keep this information confidential."]

     [:h2 {:class "text-2xl font-bold text-gray-900 mt-8 mb-4"} "Cookies and Tracking"]
     [:p {:class "text-gray-700 mb-6"}
      "Our website may use cookies to enhance your browsing experience. You can choose to disable cookies through your browser settings, though some features of our website may not function properly."]

     [:h2 {:class "text-2xl font-bold text-gray-900 mt-8 mb-4"} "Data Security"]
     [:p {:class "text-gray-700 mb-6"}
      "We implement appropriate security measures to protect your personal information. However, no method of transmission over the internet is 100% secure, and we cannot guarantee absolute security."]

     [:h2 {:class "text-2xl font-bold text-gray-900 mt-8 mb-4"} "Children's Privacy"]
     [:p {:class "text-gray-700 mb-6"}
      "Our website is not directed to children under 13. We do not knowingly collect personal information from children under 13. If you believe we have collected information from a child under 13, please contact us."]

     [:h2 {:class "text-2xl font-bold text-gray-900 mt-8 mb-4"} "Your Rights"]
     [:p {:class "text-gray-700 mb-6"}
      "You have the right to access, correct, or delete your personal information. To exercise these rights, please contact us using the information below."]

     [:h2 {:class "text-2xl font-bold text-gray-900 mt-8 mb-4"} "Changes to This Policy"]
     [:p {:class "text-gray-700 mb-6"}
      "We may update this privacy policy from time to time. We will notify you of any changes by posting the new privacy policy on this page with an updated \"Last updated\" date."]

     [:h2 {:class "text-2xl font-bold text-gray-900 mt-8 mb-4"} "Contact Us"]
     [:p {:class "text-gray-700 mb-4"}
      "If you have questions about this privacy policy, please contact us:"]
     [:address {:class "not-italic text-gray-700 mb-6"}
      [:div {:class "mb-2"} "Mount Zion United Church of Christ"]
      [:div {:class "mb-2"} "1415 S Main St"]
      [:div {:class "mb-2"} "China Grove, NC 28023"]
      [:div
       "Phone: "
       [:a {:href "tel:+17048571169" :class "text-blue-600 hover:underline"} "(704) 857-1169"]]]]]
   ctx))

(comment
  ;; Test pages
  (home-page {:page/title "Home" :page/content "Welcome!"})
  (demo-page {:greeting "Hello World!"
              :alfresco {:success true :message "Connected"}})
  (dynamic-page {:page/title "Test Page" :page/content "Test content" :page/node-id "123"})
  (contact-page {}))