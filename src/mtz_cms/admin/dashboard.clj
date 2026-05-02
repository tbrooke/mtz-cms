(ns mtz-cms.admin.dashboard
  (:require
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.admin.layout :as layout]
   [mtz-cms.config.core :as config]))

(defn- ctx [] {})

(defn- count-items [folder-id filter]
  (let [result (alfresco/get-node-children
                (ctx) folder-id {:maxItems 1 :where filter})]
    (if (:success result)
      (get-in result [:data :list :pagination :totalItems] 0)
      "?")))

(defn- stat-card [label count href border-color]
  [:a {:href href :class "block"}
   [:div {:class (str "bg-white rounded-xl shadow-sm p-5 border-l-4 " border-color
                      " hover:shadow-md transition-shadow")}
    [:div {:class "text-2xl font-bold text-slate-800"} (str count)]
    [:div {:class "text-sm text-slate-500 mt-0.5"} label]]])

(defn- quick-link [href label]
  [:a {:href href
       :class "block bg-white hover:bg-slate-50 rounded-lg shadow-sm px-4 py-3 text-sm font-medium text-slate-700 border border-slate-200 transition-colors"}
   label])

(defn dashboard-handler [request]
  (let [blog-count     (count-items (config/get-node-id :blog) "(isFile=true)")
        calendar-count (count-items (config/get-node-id :calendar) "(isFile=true)")
        history-count  (count-items (config/get-node-id :history-sections) "(isFile=true)")
        user           (get-in request [:session :admin/user])]
    (layout/html-response
     (layout/admin-page
      "Dashboard"
      [:div
       [:div {:class "mb-6"}
        [:h1 {:class "text-xl font-bold text-slate-800"} "Dashboard"]
        (when user
          [:p {:class "text-sm text-slate-500 mt-0.5"} (str "Signed in as " user)])]

       [:div {:class "grid grid-cols-3 gap-4 mb-8"}
        (stat-card "Blog Posts"      blog-count     "/admin/blog"     "border-blue-500")
        (stat-card "Calendar Events" calendar-count "/admin/calendar" "border-green-500")
        (stat-card "History Docs"    history-count  "/admin/history"  "border-amber-500")]

       [:div {:class "grid grid-cols-2 gap-6"}
        [:div
         [:h2 {:class "text-sm font-semibold text-slate-700 mb-3"} "Quick Actions"]
         [:div {:class "space-y-2"}
          (quick-link "/admin/blog/new"       "+ New Blog Post")
          (quick-link "/admin/calendar/new"   "+ New Calendar Event")
          (quick-link "/admin/history/upload" "+ Upload History Document")]]
        [:div
         [:h2 {:class "text-sm font-semibold text-slate-700 mb-3"} "Sections"]
         [:div {:class "space-y-2"}
          (quick-link "/admin/blog"     "Manage Blog Posts")
          (quick-link "/admin/calendar" "Manage Calendar")
          (quick-link "/admin/history"  "History Archive")]]]]
      :current-path "/admin"))))
