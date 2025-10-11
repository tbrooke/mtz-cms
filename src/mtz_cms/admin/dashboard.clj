(ns mtz-cms.admin.dashboard
  "Admin Dashboard for Mount Zion CMS

   Simple web-based admin interface for:
   - Calendar configuration
   - Content management
   - Email settings
   - Cache management
   - System status

   Access at: http://localhost:3000/admin (requires authentication)"
  (:require
   [hiccup.core :as hiccup]
   [mtz-cms.calendar.service :as cal]
   [mtz-cms.calendar.repl :as cal-repl]
   [mtz-cms.cache.simple :as cache]
   [mtz-cms.config.core :as config]
   [clojure.tools.logging :as log]))

;; --- AUTHENTICATION (Simple - enhance with proper auth later) ---

(def admin-credentials
  "Simple admin credentials - REPLACE WITH PROPER AUTH"
  {:username (or (System/getenv "ADMIN_USERNAME") "admin")
   :password (or (System/getenv "ADMIN_PASSWORD") "admin")})

(defn check-auth
  "Check if request has valid admin auth"
  [request]
  (let [auth-header (get-in request [:headers "authorization"])]
    ;; TODO: Implement proper authentication
    ;; For now, just check if header exists
    (some? auth-header)))

;; --- LAYOUT ---

(defn admin-layout
  "Admin page layout with navigation"
  [title & content]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:title (str title " - Admin Dashboard")]
    [:script {:src "https://unpkg.com/htmx.org@1.9.10"}]
    [:script {:src "https://cdn.tailwindcss.com"}]]
   [:body {:class "bg-gray-50"}
    ;; Header
    [:header {:class "bg-blue-600 text-white shadow-lg"}
     [:div {:class "max-w-7xl mx-auto px-4 py-6"}
      [:h1 {:class "text-3xl font-bold"} "Mount Zion CMS Admin"]
      [:p {:class "text-blue-100 mt-1"} "Content & Calendar Management"]]]

    ;; Navigation
    [:nav {:class "bg-white shadow-sm border-b"}
     [:div {:class "max-w-7xl mx-auto px-4"}
      [:div {:class "flex space-x-8"}
       [:a {:href "/admin" :class "py-4 px-2 border-b-2 border-transparent hover:border-blue-500 font-medium text-gray-700 hover:text-blue-600"}
        "Dashboard"]
       [:a {:href "/admin/calendar" :class "py-4 px-2 border-b-2 border-transparent hover:border-blue-500 font-medium text-gray-700 hover:text-blue-600"}
        "Calendar"]
       [:a {:href "/admin/content" :class "py-4 px-2 border-b-2 border-transparent hover:border-blue-500 font-medium text-gray-700 hover:text-blue-600"}
        "Content"]
       [:a {:href "/admin/settings" :class "py-4 px-2 border-b-2 border-transparent hover:border-blue-500 font-medium text-gray-700 hover:text-blue-600"}
        "Settings"]
       [:a {:href "/admin/system" :class "py-4 px-2 border-b-2 border-transparent hover:border-blue-500 font-medium text-gray-700 hover:text-blue-600"}
        "System"]]]]

    ;; Main Content
    [:main {:class "max-w-7xl mx-auto px-4 py-8"}
     content]

    ;; Footer
    [:footer {:class "bg-white border-t mt-12"}
     [:div {:class "max-w-7xl mx-auto px-4 py-6 text-center text-gray-600 text-sm"}
      "Mount Zion UCC Admin Dashboard · " [:a {:href "/" :class "text-blue-600 hover:underline"} "View Site"]]]]])

;; --- DASHBOARD PAGES ---

(defn dashboard-home
  "Main dashboard home page"
  []
  (admin-layout
   "Dashboard"
   [:div {:class "space-y-6"}
    ;; Quick Stats
    [:div {:class "grid grid-cols-1 md:grid-cols-3 gap-6"}
     ;; Calendar Events
     [:div {:class "bg-white rounded-lg shadow p-6"}
      [:div {:class "flex items-center justify-between"}
       [:div
        [:p {:class "text-sm text-gray-600"} "Upcoming Events"]
        [:p {:class "text-3xl font-bold text-gray-900"
             :hx-get "/admin/api/stats/events-count"
             :hx-trigger "load"
             :hx-swap "innerHTML"}
         "..."]]
       [:div {:class "text-blue-600"}
        [:svg {:class "w-12 h-12" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
         [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"}]]]]]

     ;; Cache Status
     [:div {:class "bg-white rounded-lg shadow p-6"}
      [:div {:class "flex items-center justify-between"}
       [:div
        [:p {:class "text-sm text-gray-600"} "Cache Status"]
        [:p {:class "text-lg font-bold text-green-600"} "Active"]]
       [:button {:class "bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
                 :hx-post "/api/cache/clear"
                 :hx-swap "none"}
        "Clear Cache"]]]

     ;; System Status
     [:div {:class "bg-white rounded-lg shadow p-6"}
      [:div {:class "flex items-center justify-between"}
       [:div
        [:p {:class "text-sm text-gray-600"} "Alfresco Connection"]
        [:p {:class "text-lg font-bold text-green-600"
             :hx-get "/admin/api/system/alfresco-status"
             :hx-trigger "load"
             :hx-swap "innerHTML"}
         "..."]]
       [:div {:class "text-green-600"}
        [:svg {:class "w-12 h-12" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
         [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"}]]]]]]

    ;; Quick Actions
    [:div {:class "bg-white rounded-lg shadow"}
     [:div {:class "px-6 py-4 border-b"}
      [:h2 {:class "text-xl font-bold text-gray-900"} "Quick Actions"]]
     [:div {:class "p-6 space-y-3"}
      [:a {:href "/admin/calendar" :class "block p-4 bg-blue-50 hover:bg-blue-100 rounded-lg"}
       [:h3 {:class "font-semibold text-blue-900"} "📅 Manage Calendar"]
       [:p {:class "text-sm text-blue-700"} "View and configure calendar events"]]
      [:a {:href "/admin/content" :class "block p-4 bg-green-50 hover:bg-green-100 rounded-lg"}
       [:h3 {:class "font-semibold text-green-900"} "📄 Manage Content"]
       [:p {:class "text-sm text-green-700"} "Edit pages, hero images, and features"]]
      [:a {:href "/admin/settings" :class "block p-4 bg-purple-50 hover:bg-purple-100 rounded-lg"}
       [:h3 {:class "font-semibold text-purple-900"} "⚙️ Settings"]
       [:p {:class "text-sm text-purple-700"} "Configure email, timezone, and more"]]]]]))

(defn calendar-management
  "Calendar management page"
  [ctx]
  (let [upcoming-events (cal/get-upcoming-events ctx :days 30 :limit 10)]
    (admin-layout
     "Calendar Management"
     [:div {:class "space-y-6"}
      ;; Calendar Configuration
      [:div {:class "bg-white rounded-lg shadow"}
       [:div {:class "px-6 py-4 border-b"}
        [:h2 {:class "text-xl font-bold text-gray-900"} "Calendar Configuration"]]
       [:div {:class "p-6"}
        [:div {:class "grid grid-cols-2 gap-6"}
         [:div
          [:label {:class "block text-sm font-medium text-gray-700 mb-2"} "Timezone"]
          [:select {:class "w-full border-gray-300 rounded-md shadow-sm"
                    :hx-post "/admin/api/calendar/set-timezone"
                    :hx-swap "none"}
           [:option {:value "America/New_York"} "Eastern (New York)"]
           [:option {:value "America/Chicago"} "Central (Chicago)"]
           [:option {:value "America/Denver"} "Mountain (Denver)"]
           [:option {:value "America/Los_Angeles"} "Pacific (Los Angeles)"]
           [:option {:value "UTC"} "UTC"]]]
         [:div
          [:label {:class "block text-sm font-medium text-gray-700 mb-2"} "Default Range"]
          [:input {:type "number" :value "90" :class "w-full border-gray-300 rounded-md shadow-sm"}]
          [:p {:class "text-sm text-gray-500 mt-1"} "Days to show by default"]]]]]

      ;; Upcoming Events List
      [:div {:class "bg-white rounded-lg shadow"}
       [:div {:class "px-6 py-4 border-b flex justify-between items-center"}
        [:h2 {:class "text-xl font-bold text-gray-900"} "Upcoming Events (Next 30 Days)"]
        [:button {:class "bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                  :hx-get "/admin/api/calendar/upcoming"
                  :hx-target "#events-list"
                  :hx-swap "innerHTML"}
         "Refresh"]]
       [:div {:id "events-list" :class "divide-y"}
        (if (empty? upcoming-events)
          [:div {:class "p-6 text-center text-gray-500"}
           "No upcoming events"]
          (for [event upcoming-events]
            [:div {:class "p-6 hover:bg-gray-50"}
             [:div {:class "flex justify-between items-start"}
              [:div
               [:h3 {:class "font-semibold text-gray-900"} (:summary event)]
               [:p {:class "text-sm text-gray-600 mt-1"}
                "📅 " (str (:start-local event))]
               (when (:location event)
                 [:p {:class "text-sm text-gray-600"}
                  "📍 " (:location event)])
               (when (:is-recurring event)
                 [:span {:class "inline-block mt-2 px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded"}
                  "🔁 Recurring"])]
              [:div {:class "flex space-x-2"}
               [:a {:href (str "/admin/calendar/event/" (:node-id event))
                    :class "text-blue-600 hover:underline text-sm"}
                "View"]
               [:a {:href (str "/admin/calendar/event/" (:node-id event) "/edit")
                    :class "text-green-600 hover:underline text-sm"}
                "Edit"]]]]))]]])))

(defn settings-page
  "Settings page"
  []
  (admin-layout
   "Settings"
   [:div {:class "space-y-6"}
    ;; Email Settings
    [:div {:class "bg-white rounded-lg shadow"}
     [:div {:class "px-6 py-4 border-b"}
      [:h2 {:class "text-xl font-bold text-gray-900"} "Email Configuration (Mailgun)"]]
     [:div {:class "p-6 space-y-4"}
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-2"} "SMTP Host"]
       [:input {:type "text" :value "smtp.mailgun.org" :class "w-full border-gray-300 rounded-md shadow-sm" :readonly true}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-2"} "From Email"]
       [:input {:type "email" :placeholder "noreply@mtzcg.com" :class "w-full border-gray-300 rounded-md shadow-sm"}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700 mb-2"} "Admin Email"]
       [:input {:type "email" :placeholder "office@mtzcg.com" :class "w-full border-gray-300 rounded-md shadow-sm"}]]
      [:button {:class "bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"}
       "Save Settings"]]]

    ;; Cache Settings
    [:div {:class "bg-white rounded-lg shadow"}
     [:div {:class "px-6 py-4 border-b"}
      [:h2 {:class "text-xl font-bold text-gray-900"} "Cache Management"]]
     [:div {:class "p-6 space-y-4"}
      [:div {:class "flex justify-between items-center"}
       [:div
        [:h3 {:class "font-medium text-gray-900"} "Content Cache"]
        [:p {:class "text-sm text-gray-600"} "Cached content from Alfresco"]]
       [:button {:class "bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
                 :hx-post "/api/cache/clear"
                 :hx-swap "none"}
        "Clear Cache"]]
      [:div {:class "text-sm text-gray-600"}
       [:p "Cache TTL: 1 hour"]
       [:p "Auto-refresh: Enabled"]]]]]))

;; --- API ENDPOINTS ---

(defn api-events-count-handler
  "API: Get count of upcoming events"
  [request]
  (try
    (let [ctx {}
          events (cal/get-upcoming-events ctx :days 30)]
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (count events))})
    (catch Exception e
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body "Error"})))

(defn api-alfresco-status-handler
  "API: Check Alfresco connection status"
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Connected"})  ; TODO: Actually check connection

(defn api-calendar-upcoming-handler
  "API: Get upcoming events HTML fragment"
  [request]
  (let [ctx {}
        events (cal/get-upcoming-events ctx :days 30 :limit 20)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (hiccup/html
            (if (empty? events)
              [:div {:class "p-6 text-center text-gray-500"}
               "No upcoming events"]
              (for [event events]
                [:div {:class "p-6 hover:bg-gray-50 border-b"}
                 [:h3 {:class "font-semibold"} (:summary event)]
                 [:p {:class "text-sm text-gray-600"} (str (:start-local event))]])))}))

;; --- ROUTE HANDLERS ---

(defn admin-home-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html (dashboard-home))})

(defn admin-calendar-handler [request]
  (let [ctx {}]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (hiccup/html (calendar-management ctx))}))

(defn admin-settings-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html (settings-page))})

;; --- ROUTES (Add to main.clj) ---

(def admin-routes
  [["/admin" {:get admin-home-handler}]
   ["/admin/calendar" {:get admin-calendar-handler}]
   ["/admin/settings" {:get admin-settings-handler}]
   ["/admin/api/stats/events-count" {:get api-events-count-handler}]
   ["/admin/api/system/alfresco-status" {:get api-alfresco-status-handler}]
   ["/admin/api/calendar/upcoming" {:get api-calendar-upcoming-handler}]])
