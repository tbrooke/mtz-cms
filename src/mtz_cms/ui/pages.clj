(ns mtz-cms.ui.pages
  "Page templates for Mount Zion CMS"
  (:require
   [mtz-cms.ui.components :as ui]))

;; --- PAGE LAYOUTS ---

(defn base-layout [title content]
  [:html {:class "h-full"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title title]
    (ui/tailwind-cdn)
    [:script {:src "https://unpkg.com/htmx.org@1.9.6"}]
    (ui/custom-styles)]
   [:body {:class "h-full bg-gray-50"}
    (ui/site-header)
    [:main {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"}
     content]
    (ui/site-footer)]])

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
        [:div {:class "prose max-w-none"} (:page/content page-data)]])
     
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

(defn about-page [page-data]
  (base-layout
    "About Us - Mount Zion UCC"
    [:div
     [:h1 "About Mount Zion UCC"]
     
     (if page-data
       [:div {:class "page-content"}
        [:div (:page/content page-data)]]
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
        [:p "Building connections and fellowship."]]]]]))

(defn demo-page [data]
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

(comment
  ;; Test pages
  (home-page {:page/title "Home" :page/content "Welcome!"})
  (demo-page {:greeting "Hello World!"
              :alfresco {:success true :message "Connected"}}))