(ns mtz-cms.calendar.service
  "High-level calendar service with Pathom integration

   Provides calendar event queries with:
   - Recurring event expansion (via RRULE from properties)
   - Date range filtering
   - Tag-based filtering
   - Monthly/daily grouping"
  (:require
   [mtz-cms.calendar.alfresco-events :as alfresco-events]
   [clojure.tools.logging :as log])
  (:import
   [java.time LocalDateTime ZoneId]
   [java.time.temporal ChronoUnit]))

;; --- CONFIGURATION ---

(def default-timezone "America/New_York")

(def calendar-config
  "Calendar configuration"
  {:timezone (or (System/getenv "CALENDAR_TIMEZONE") default-timezone)
   :default-range-days 90  ; Default to 90 days forward
   :max-range-days 365     ; Maximum 1 year range
   :enable-past-events false}) ; Hide past events by default

;; --- DATE RANGE UTILITIES ---

(defn get-default-range
  "Get default date range (now to N days in future)

   Returns: [start-date end-date] as LocalDateTime"
  []
  (let [now (LocalDateTime/now (ZoneId/of (:timezone calendar-config)))
        start (if (:enable-past-events calendar-config)
                (.minusDays now 30)  ; Include past 30 days
                now)
        end (.plusDays now (:default-range-days calendar-config))]
    [start end]))

(defn parse-date-string
  "Parse ISO date string to LocalDateTime"
  [date-str]
  (try
    (LocalDateTime/parse date-str)
    (catch Exception e
      (log/warn "Failed to parse date:" date-str)
      nil)))

(defn validate-date-range
  "Validate and constrain date range to max allowed"
  [start end]
  (let [max-days (:max-range-days calendar-config)
        days-between (.between ChronoUnit/DAYS start end)]
    (if (> days-between max-days)
      (do
        (log/warn "Date range too large, constraining to" max-days "days")
        [start (.plusDays start max-days)])
      [start end])))

;; --- MAIN SERVICE FUNCTIONS ---
;; Delegates to alfresco-events which reads from node properties

(defn get-calendar-events
  "Get all calendar events for date range with recurring event expansion

   Reads event data from Alfresco node properties (ia:calendarEvent model)
   including RRULE from :ia:recurrenceRule property.

   Parameters (optional):
   - :start-date - Start date (LocalDateTime or ISO string)
   - :end-date - End date (LocalDateTime or ISO string)
   - :tag-filter - Filter by tag (e.g., 'publish')
   - :timezone - Timezone ID (default: America/New_York)

   Returns: Sorted list of event occurrences with metadata"
  [ctx & {:keys [start-date end-date tag-filter timezone]
          :or {timezone (:timezone calendar-config)}}]
  (try
    ;; Parse and validate date range
    (let [[start end] (if (and start-date end-date)
                        (let [s (if (string? start-date)
                                 (parse-date-string start-date)
                                 start-date)
                              e (if (string? end-date)
                                 (parse-date-string end-date)
                                 end-date)]
                          (validate-date-range s e))
                        (get-default-range))]

      (log/info "Getting calendar events from" start "to" end)

      ;; Delegate to alfresco-events
      (alfresco-events/get-events-for-range ctx start end timezone
                                            :tag-filter tag-filter))

    (catch Exception e
      (log/error "Error getting calendar events:" (.getMessage e))
      [])))

(defn get-events-by-month
  "Get events grouped by month for calendar grid display"
  [ctx & opts]
  ;; Delegate to alfresco-events which has grouping built-in
  ;; But return same format for compatibility
  (let [events (apply get-calendar-events ctx opts)]
    (group-by (fn [event]
                (when-let [dt (:occurrence-date event)]
                  (.format dt (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM"))))
              events)))

(defn get-events-by-day
  "Get events grouped by day for list display"
  [ctx & opts]
  (apply alfresco-events/get-events-by-day ctx opts))

(defn get-upcoming-events
  "Get upcoming events (next N days)

   Parameters:
   - :days - Number of days to look ahead (default: 30)
   - :limit - Max number of events to return
   - :tag-filter - Filter by tag"
  [ctx & {:keys [days limit tag-filter] :or {days 30}}]
  (alfresco-events/get-upcoming-events ctx
                                       :days days
                                       :limit limit
                                       :tag-filter tag-filter
                                       :timezone (:timezone calendar-config)))

;; --- REPL TESTING ---

(comment
  (require '[mtz-cms.calendar.service :as cal])

  ;; Get all events for next 90 days (default)
  (def ctx {})
  (cal/get-calendar-events ctx)

  ;; Get events for specific range
  (cal/get-calendar-events ctx
                          :start-date "2025-10-01T00:00:00"
                          :end-date "2025-10-31T23:59:59")

  ;; Get only published events
  (cal/get-calendar-events ctx :tag-filter "publish")

  ;; Get upcoming 10 events
  (cal/get-upcoming-events ctx :limit 10)

  ;; Get events grouped by month
  (cal/get-events-by-month ctx)

  ;; Get events grouped by day
  (cal/get-events-by-day ctx))
