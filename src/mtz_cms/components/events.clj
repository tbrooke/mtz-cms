(ns mtz-cms.components.events
  "Event listing and calendar display components"
  (:require
   [mtz-cms.calendar.service :as cal-service]
   [clojure.string :as str])
  (:import
   [java.time LocalDateTime LocalDate DayOfWeek]
   [java.time.temporal TemporalAdjusters ChronoUnit]
   [java.time.format DateTimeFormatter]))

;; --- DATE UTILITIES ---

(defn format-date
  "Format LocalDateTime for display"
  [dt format-str]
  (when dt
    (.format dt (DateTimeFormatter/ofPattern format-str))))

(defn format-time
  "Format time portion only"
  [dt]
  (format-date dt "h:mm a"))

(defn format-date-short
  "Short date format (Oct 11)"
  [dt]
  (format-date dt "MMM d"))

(defn format-date-full
  "Full date format (Saturday, October 11, 2025)"
  [dt]
  (format-date dt "EEEE, MMMM d, yyyy"))

(defn get-week-range
  "Get start and end of current week (Sunday to Saturday)"
  []
  (let [now (LocalDateTime/now)
        start-of-week (.with now (TemporalAdjusters/previousOrSame DayOfWeek/SUNDAY))
        end-of-week (.with now (TemporalAdjusters/nextOrSame DayOfWeek/SATURDAY))
        end-of-week-day (.plusHours end-of-week 23)]
    [start-of-week end-of-week-day]))

(defn get-month-range
  "Get start and end of current month"
  []
  (let [now (LocalDateTime/now)
        start-of-month (.withDayOfMonth now 1)
        end-of-month (.with now (TemporalAdjusters/lastDayOfMonth))
        end-of-month-day (.plusHours end-of-month 23)]
    [start-of-month end-of-month-day]))

;; --- EVENT CARD COMPONENTS ---

(defn event-card
  "Single event card for list display"
  [event]
  (let [start (:start-local event)
        end (:end-local event)]
    [:div {:class "bg-white rounded-lg shadow-md hover:shadow-xl transition-shadow border-l-4 border-blue-600 p-6"}
     ;; Date badge
     [:div {:class "flex items-start justify-between mb-4"}
      [:div {:class "flex items-center space-x-4"}
       ;; Calendar icon with date
       [:div {:class "flex-shrink-0 bg-blue-600 text-white rounded-lg p-3 text-center"}
        [:div {:class "text-xs font-semibold uppercase"} (format-date start "MMM")]
        [:div {:class "text-2xl font-bold"} (format-date start "d")]
        [:div {:class "text-xs"} (format-date start "EEE")]]

       ;; Event details
       [:div {:class "flex-grow"}
        [:h3 {:class "text-xl font-bold text-gray-900 mb-1"}
         (:summary event)]
        [:div {:class "flex items-center text-gray-600 text-sm space-x-4"}
         [:div {:class "flex items-center"}
          [:svg {:class "w-4 h-4 mr-1" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
           [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
                   :d "M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"}]]
          [:span (format-time start) " - " (format-time end)]]
         (when (:location event)
           [:div {:class "flex items-center"}
            [:svg {:class "w-4 h-4 mr-1" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
             [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
                     :d "M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"}]
             [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
                     :d "M15 11a3 3 0 11-6 0 3 3 0 016 0z"}]]
            [:span (:location event)]])]]]

      ;; Recurring badge
      (when (:is-recurring event)
        [:span {:class "inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-purple-100 text-purple-800"}
         [:svg {:class "w-4 h-4 mr-1" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
          [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
                  :d "M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"}]]
         "Recurring"])]

     ;; Description
     (when (:description event)
       [:p {:class "text-gray-700 mt-3 line-clamp-2"}
        (:description event)])]))

;; --- CALENDAR GRID COMPONENTS ---

(defn calendar-day-cell
  "Single day cell in calendar grid"
  [date events]
  [:div {:class "min-h-[100px] bg-white border border-gray-200 p-2"}
   ;; Date number
   [:div {:class "font-semibold text-gray-700 mb-2"}
    (.getDayOfMonth date)]

   ;; Events for this day
   [:div {:class "space-y-1"}
    (for [event (take 3 events)]  ; Show max 3 events per day
      [:div {:class "text-xs bg-blue-50 hover:bg-blue-100 rounded px-2 py-1 border-l-2 border-blue-600 cursor-pointer"}
       [:div {:class "font-semibold text-blue-900 truncate"}
        (format-time (:start-local event)) " " (:summary event)]
       (when (:location event)
         [:div {:class "text-gray-600 truncate"} (:location event)])])

    ;; Show "X more" if there are more events
    (when (> (count events) 3)
      [:div {:class "text-xs text-gray-500 italic"}
       "+ " (- (count events) 3) " more"])]])

(defn calendar-week-view
  "Calendar grid for current week"
  [events-by-day]
  (let [[start-date end-date] (get-week-range)
        days (for [i (range 7)]
               (.plusDays (.toLocalDate start-date) i))]
    [:div {:class "bg-gray-50 rounded-lg p-4"}
     [:h3 {:class "text-xl font-bold text-gray-900 mb-4 flex items-center"}
      [:svg {:class "w-6 h-6 mr-2 text-blue-600" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
       [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
               :d "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"}]]
      "This Week at Mt Zion"
      [:span {:class "ml-2 text-sm font-normal text-gray-600"}
       (format-date-short start-date) " - " (format-date-short end-date)]]

     ;; Week grid
     [:div {:class "grid grid-cols-7 gap-2"}
      ;; Day headers
      (for [day days]
        [:div {:class "text-center font-semibold text-gray-700 py-2"}
         (format-date day "EEE")])

      ;; Day cells with events
      (for [day days]
        (let [day-events (get events-by-day day [])]
          (calendar-day-cell day day-events)))]]))

(defn calendar-month-view
  "Calendar grid for current month"
  [events-by-day]
  (let [[start-date end-date] (get-month-range)
        first-day-of-month (.toLocalDate start-date)
        last-day-of-month (.toLocalDate end-date)

        ;; Start from previous Sunday if month doesn't start on Sunday
        calendar-start (.with first-day-of-month (TemporalAdjusters/previousOrSame DayOfWeek/SUNDAY))
        ;; End on next Saturday if month doesn't end on Saturday
        calendar-end (.with last-day-of-month (TemporalAdjusters/nextOrSame DayOfWeek/SATURDAY))

        ;; Get all days in calendar grid
        num-days (inc (.between ChronoUnit/DAYS calendar-start calendar-end))
        all-days (for [i (range num-days)]
                   (.plusDays calendar-start i))]

    [:div {:class "bg-gray-50 rounded-lg p-4"}
     [:h3 {:class "text-xl font-bold text-gray-900 mb-4 flex items-center"}
      [:svg {:class "w-6 h-6 mr-2 text-blue-600" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
       [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
               :d "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"}]]
      "This Month at Mt Zion"
      [:span {:class "ml-2 text-sm font-normal text-gray-600"}
       (format-date start-date "MMMM yyyy")]]

     ;; Month grid
     [:div {:class "grid grid-cols-7 gap-2"}
      ;; Day headers
      (for [day-name ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"]]
        [:div {:class "text-center font-semibold text-gray-700 py-2"}
         day-name])

      ;; Day cells with events
      (for [day all-days]
        (let [is-current-month (and (.isAfter day first-day-of-month)
                                   (.isBefore day last-day-of-month)
                                   (or (= day first-day-of-month)
                                       (= day last-day-of-month)))
              day-events (get events-by-day day [])]
          [:div {:class (str "min-h-[100px] border border-gray-200 p-2 "
                            (if is-current-month
                              "bg-white"
                              "bg-gray-100"))}
           ;; Date number
           [:div {:class (str "font-semibold mb-2 "
                             (if is-current-month
                               "text-gray-700"
                               "text-gray-400"))}
            (.getDayOfMonth day)]

           ;; Events for this day (only show if in current month)
           (when is-current-month
             [:div {:class "space-y-1"}
              (for [event (take 2 day-events)]  ; Show max 2 events per day in month view
                [:div {:class "text-xs bg-blue-50 hover:bg-blue-100 rounded px-2 py-1 border-l-2 border-blue-600 cursor-pointer"}
                 [:div {:class "font-semibold text-blue-900 truncate"}
                  (format-time (:start-local event)) " " (:summary event)]])

              ;; Show "X more" if there are more events
              (when (> (count day-events) 2)
                [:div {:class "text-xs text-gray-500 italic"}
                 "+ " (- (count day-events) 2) " more"])])]))]]))

;; --- MAIN PAGES ---

(defn events-list-page
  "Events list page - shows upcoming events in list format

   Only shows published events (tagged with 'publish')"
  [ctx]
  (let [;; Fetch upcoming events (next 90 days) - PUBLISHED ONLY
        events (cal-service/get-upcoming-events ctx :days 90 :tag-filter "publish")]

    [:div {:class "max-w-7xl mx-auto px-4 py-8"}
     ;; Page header
     [:div {:class "mb-8"}
      [:h1 {:class "text-4xl font-bold text-gray-900 mb-2"} "Upcoming Events"]
      [:p {:class "text-xl text-gray-600"} "Join us for worship, fellowship, and community events"]

      ;; Link to calendar view
      [:div {:class "mt-4"}
       [:a {:href "/events/calendar"
            :class "inline-flex items-center text-blue-600 hover:text-blue-800 font-medium"}
        [:svg {:class "w-5 h-5 mr-2" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
         [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
                 :d "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"}]]
        "View Calendar"]]]

     ;; Events list
     (if (empty? events)
       [:div {:class "text-center py-12 bg-gray-50 rounded-lg"}
        [:svg {:class "mx-auto h-12 w-12 text-gray-400" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
         [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
                 :d "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"}]]
        [:h3 {:class "mt-2 text-lg font-medium text-gray-900"} "No upcoming events"]
        [:p {:class "mt-1 text-gray-500"} "Check back soon for new events!"]]
       [:div {:class "space-y-6"}
        (for [event events]
          (event-card event))])]))

(defn calendar-page
  "Calendar page with week and month tabs

   Shows ALL events (not filtered by publish tag)"
  [ctx]
  (let [;; Group events by day for calendar views - SHOW ALL EVENTS
        week-events-by-day (cal-service/get-events-by-day ctx
                                                          :start-date (first (get-week-range))
                                                          :end-date (second (get-week-range)))
        month-events-by-day (cal-service/get-events-by-day ctx
                                                           :start-date (first (get-month-range))
                                                           :end-date (second (get-month-range)))]

    [:div {:class "max-w-7xl mx-auto px-4 py-8"}
     ;; Page header
     [:div {:class "mb-8"}
      [:h1 {:class "text-4xl font-bold text-gray-900 mb-2"} "Event Calendar"]
      [:p {:class "text-xl text-gray-600"} "View events by week or month"]

      ;; Link back to list view
      [:div {:class "mt-4"}
       [:a {:href "/events"
            :class "inline-flex items-center text-blue-600 hover:text-blue-800 font-medium"}
        [:svg {:class "w-5 h-5 mr-2" :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
         [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
                 :d "M4 6h16M4 10h16M4 14h16M4 18h16"}]]
        "View List"]]]

     ;; Tabs
     [:div {:class "mb-6"}
      [:div {:class "border-b border-gray-200"}
       [:nav {:class "-mb-px flex space-x-8"}
        [:button {:class "tab-button border-b-2 border-blue-600 py-4 px-1 text-blue-600 font-medium"
                  :onclick "showTab('week')"}
         "This Week"]
        [:button {:class "tab-button border-b-2 border-transparent py-4 px-1 text-gray-500 hover:text-gray-700 hover:border-gray-300 font-medium"
                  :onclick "showTab('month')"}
         "This Month"]]]]

     ;; Tab content
     ;; Week view
     [:div {:id "tab-week" :class "tab-content"}
      (calendar-week-view week-events-by-day)]

     ;; Month view
     [:div {:id "tab-month" :class "tab-content hidden"}
      (calendar-month-view month-events-by-day)]

     ;; JavaScript for tab switching
     [:script "
       function showTab(tabName) {
         // Hide all tab contents
         document.querySelectorAll('.tab-content').forEach(el => el.classList.add('hidden'));
         // Remove active styles from all tabs
         document.querySelectorAll('.tab-button').forEach(el => {
           el.classList.remove('border-blue-600', 'text-blue-600');
           el.classList.add('border-transparent', 'text-gray-500');
         });
         // Show selected tab
         document.getElementById('tab-' + tabName).classList.remove('hidden');
         // Highlight active tab button
         event.target.classList.remove('border-transparent', 'text-gray-500');
         event.target.classList.add('border-blue-600', 'text-blue-600');
       }
     "]]))
