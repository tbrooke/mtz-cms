(ns mtz-cms.admin.calendar
  (:require
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.admin.layout :as layout]
   [mtz-cms.config.core :as config]
   [ring.util.response :as response]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

(def ^:private calendar-folder (config/get-node-id :calendar))
(defn- ctx [] {})

;; Alfresco ISO: "2025-09-28T18:30:00.000+0000"
;; HTML datetime-local: "2025-09-28T18:30"
(defn- iso->html-dt [iso]
  (when (and iso (>= (count iso) 16)) (subs iso 0 16)))

(defn- html-dt->iso [s]
  (when (not (str/blank? s)) (str s ":00.000+0000")))

(defn- fmt-date [iso]
  (when (and iso (> (count iso) 9)) (subs iso 0 10)))

;; --- LIST ---

(defn- event-row [entry]
  (let [id    (get-in entry [:entry :id])
        props (get-in entry [:entry :properties])
        title (or (:ia:whatEvent props) (get-in entry [:entry :name]) "Untitled")
        date  (fmt-date (:ia:fromDate props))
        where (or (:ia:whereEvent props) "—")]
    [:tr {:class "border-b border-slate-100 hover:bg-slate-50"}
     [:td {:class "py-3 px-4"}
      [:div {:class "font-medium text-slate-800 text-sm"} title]]
     [:td {:class "py-3 px-4 text-sm text-slate-500"} date]
     [:td {:class "py-3 px-4 text-sm text-slate-500"} where]
     [:td {:class "py-3 px-4"}
      [:div {:class "flex gap-2"}
       [:a {:href  (str "/admin/calendar/edit/" id)
            :class "text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 px-3 py-1 rounded-md"}
        "Edit"]
       [:button {:hx-delete  (str "/admin/calendar/delete/" id)
                 :hx-confirm (str "Delete " " title " "?")
                 :hx-target  "closest tr"
                 :hx-swap    "outerHTML swap:300ms"
                 :class      "text-xs bg-red-50 hover:bg-red-100 text-red-600 px-3 py-1 rounded-md"}
        "Delete"]]]]))

(defn calendar-list-handler [request]
  (let [result (alfresco/get-node-children
                (ctx) calendar-folder
                {:include "properties" :maxItems 200
                 :orderBy "cm:created DESC"})]
    (layout/html-response
     (layout/admin-page
      "Calendar Events"
      (if (:success result)
        (let [events (get-in result [:data :list :entries])]
          [:div
           [:div {:class "flex items-center justify-between mb-6"}
            [:div
             [:h1 {:class "text-xl font-bold text-slate-800"} "Calendar Events"]
             [:p {:class "text-sm text-slate-500 mt-0.5"}
              (str (count events) " event" (when (not= 1 (count events)) "s"))]]
            [:a {:href  "/admin/calendar/new"
                 :class "bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg"}
             "+ New Event"]]
           [:div {:class "bg-white rounded-xl shadow-sm overflow-hidden"}
            [:table {:class "w-full"}
             [:thead
              [:tr {:class "border-b border-slate-200 bg-slate-50"}
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Event"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Date"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Location"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} ""]]]
             [:tbody
              (if (seq events)
                (map event-row events)
                [:tr [:td {:colspan 4 :class "py-10 text-center text-slate-400 text-sm"}
                      "No events yet."]])]]]])
        [:p {:class "text-red-600 text-sm"} "Could not load events from Alfresco."])
      :current-path "/admin/calendar"))))

;; --- FORM ---

(defn- field [id label input-el]
  [:div
   [:label {:for id :class "block text-sm font-medium text-slate-700 mb-1"} label]
   input-el])

(defn- text-input [id name value & [placeholder]]
  [:input {:type "text" :id id :name name :value (or value "")
           :placeholder (or placeholder "")
           :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}])

(defn- dt-input [id name value]
  [:input {:type "datetime-local" :id id :name name :value (or value "")
           :class "border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}])

(defn- event-form [event-id props]
  (layout/admin-page
   (if event-id "Edit Event" "New Event")
   [:div
    [:div {:class "flex items-center gap-2 mb-6 text-sm"}
     [:a {:href "/admin/calendar" :class "text-slate-400 hover:text-slate-600"} "Calendar Events"]
     [:span {:class "text-slate-300"} "/"]
     [:span {:class "text-slate-600"} (if event-id "Edit Event" "New Event")]]

    [:form {:method "post"
            :action (if event-id (str "/admin/calendar/save/" event-id) "/admin/calendar/create")
            :class  "space-y-5"}

     (field "what" "Event Title *"
            [:input {:type "text" :id "what" :name "what" :required true
                     :value (or (:ia:whatEvent props) "")
                     :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}])

     [:div {:class "grid grid-cols-2 gap-4"}
      (field "from-date" "Start Date & Time *"
             (dt-input "from-date" "from-date" (iso->html-dt (:ia:fromDate props))))
      (field "to-date" "End Date & Time"
             (dt-input "to-date" "to-date" (iso->html-dt (:ia:toDate props))))]

     (field "where" "Location" (text-input "where" "where" (:ia:whereEvent props)))

     [:div
      [:label {:for "description" :class "block text-sm font-medium text-slate-700 mb-1"} "Description"]
      [:textarea {:id "description" :name "description" :rows 4
                  :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}
       (or (:ia:descriptionEvent props) "")]]

     [:div {:class "flex items-center gap-3 pt-2"}
      [:button {:type  "submit"
                :class "bg-blue-600 hover:bg-blue-700 text-white font-medium px-5 py-2 rounded-lg text-sm"}
       (if event-id "Save Changes" "Create Event")]
      [:a {:href "/admin/calendar" :class "text-slate-500 hover:text-slate-700 text-sm px-4 py-2"}
       "Cancel"]]]]
   :current-path "/admin/calendar"))

;; --- HANDLERS ---

(defn calendar-new-handler [_request]
  (layout/html-response (event-form nil {})))

(defn calendar-edit-handler [request]
  (let [id     (get-in request [:path-params :id])
        result (alfresco/get-node (ctx) id)]
    (if (:success result)
      (layout/html-response
       (event-form id (get-in result [:data :entry :properties])))
      {:status 404 :headers {"Content-Type" "text/plain"} :body "Event not found"})))

(defn calendar-create-handler [request]
  (let [{:keys [what from-date to-date where description]} (:params request)
        node-name (str "event-" (System/currentTimeMillis) ".ics")
        result    (alfresco/create-node
                   (ctx) calendar-folder
                   {:name       node-name
                    :nodeType   "ia:calendarEvent"
                    :properties (cond-> {"ia:whatEvent"        what
                                         "ia:isOutlook"        false}
                                  from-date   (assoc "ia:fromDate" (html-dt->iso from-date))
                                  to-date     (assoc "ia:toDate"   (html-dt->iso to-date))
                                  where       (assoc "ia:whereEvent"       where)
                                  description (assoc "ia:descriptionEvent" description))})]
    (if (:success result)
      (response/redirect "/admin/calendar" :see-other)
      (do
        (log/error "Failed to create event:" (:error result))
        {:status 500 :headers {"Content-Type" "text/plain"} :body "Failed to create event"}))))

(defn calendar-save-handler [request]
  (let [id                             (get-in request [:path-params :id])
        {:keys [what from-date to-date where description]} (:params request)
        result (alfresco/update-node
                (ctx) id
                {:properties (cond-> {"ia:whatEvent" what
                                      "ia:isOutlook" false}
                               from-date   (assoc "ia:fromDate" (html-dt->iso from-date))
                               to-date     (assoc "ia:toDate"   (html-dt->iso to-date))
                               where       (assoc "ia:whereEvent"       where)
                               description (assoc "ia:descriptionEvent" description))})]
    (if (:success result)
      (response/redirect "/admin/calendar" :see-other)
      (do
        (log/error "Failed to save event:" (:error result))
        {:status 500 :headers {"Content-Type" "text/plain"} :body "Failed to save event"}))))

(defn calendar-delete-handler [request]
  (let [id (get-in request [:path-params :id])]
    (alfresco/delete-node (ctx) id)
    {:status 200 :body ""}))
