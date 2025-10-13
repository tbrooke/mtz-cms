(ns mtz-cms.calendar.repl
  "REPL tools for calendar management and testing

   Quick commands for:
   - Viewing calendar events
   - Testing recurring event expansion
   - Managing calendar configuration
   - Debugging calendar issues"
  (:require
   [mtz-cms.calendar.service :as cal]
   [mtz-cms.calendar.ical :as ical]
   [mtz-cms.calendar.alfresco-events :as alfresco-events]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.config.core :as config]
   [clojure.string :as str]
   [clojure.pprint :as pprint]
   [clojure.tools.logging :as log])
  (:import
   [java.time LocalDateTime ZoneId]
   [java.time.format DateTimeFormatter]))

;; --- DISPLAY HELPERS ---

(defn format-event
  "Format event for pretty printing"
  [event]
  {:summary (:summary event)
   :start (str (:start-local event))
   :end (str (:end-local event))
   :location (:location event)
   :recurring? (:is-recurring event false)
   :recurrence-rule (:recurrence-rule event)
   :tags (:tags event)
   :node-id (:node-id event)})

(defn print-events
  "Pretty print list of events"
  [events]
  (if (empty? events)
    (println "No events found.")
    (do
      (println "\n📅 Found" (count events) "event(s):\n")
      (doseq [[idx event] (map-indexed vector events)]
        (println (str (inc idx) ".") (:summary event))
        (println "   Start:" (str (:start-local event)))
        (println "   End:  " (str (:end-local event)))
        (when (:location event)
          (println "   Location:" (:location event)))
        (when (:is-recurring event)
          (println "   🔁 Recurring:" (:recurrence-rule event)))
        (when (seq (:tags event))
          (println "   Tags:" (str/join ", " (:tags event))))
        (println)))))

(defn print-events-by-month
  "Pretty print events grouped by month"
  [events-by-month]
  (doseq [[month events] (sort events-by-month)]
    (println "\n📅" month "(" (count events) "events)")
    (println (apply str (repeat 50 "-")))
    (doseq [event (sort-by :start-local events)]
      (println "  •" (:summary event)
               "-" (.format (:start-local event)
                           (DateTimeFormatter/ofPattern "MMM dd, HH:mm"))))))

;; --- CALENDAR QUERIES ---

(defn list-events
  "List all upcoming events

   Usage:
   (list-events)           ; Next 90 days
   (list-events 30)        ; Next 30 days
   (list-events 30 \"publish\") ; Next 30 days, published only"
  ([]
   (list-events 90 nil))
  ([days]
   (list-events days nil))
  ([days tag-filter]
   (let [ctx {}
         events (cal/get-upcoming-events ctx :days days :tag-filter tag-filter)]
     (print-events events)
     events)))

(defn list-events-this-month
  "List events for current month"
  []
  (let [ctx {}
        now (LocalDateTime/now)
        start (.withDayOfMonth now 1)
        end (.withDayOfMonth (.plusMonths start 1) 1)
        events (cal/get-calendar-events ctx :start-date start :end-date end)]
    (print-events events)
    events))

(defn list-events-by-month
  "Show calendar events grouped by month

   Usage:
   (list-events-by-month)      ; Next 90 days
   (list-events-by-month 180)  ; Next 180 days"
  ([]
   (list-events-by-month 90))
  ([days]
   (let [ctx {}
         now (LocalDateTime/now)
         end (.plusDays now days)
         events-by-month (cal/get-events-by-month ctx :start-date now :end-date end)]
     (print-events-by-month events-by-month)
     events-by-month)))

(defn show-event
  "Show detailed information for a specific event by node ID"
  [node-id]
  (let [ctx {}
        [start end] (cal/get-default-range)
        events (ical/parse-calendar-event ctx node-id start end cal/default-timezone)]
    (if (empty? events)
      (println "No events found for node:" node-id)
      (do
        (println "\n📅 Event Details:")
        (pprint/pprint (format-event (first events)))
        (when (> (count events) 1)
          (println "\n🔁 Recurring Event - Showing" (count events) "occurrences:"))
        (doseq [[idx event] (map-indexed vector events)]
          (println (str (inc idx) ".") (str (:start-local event))))))
    events))

;; --- CALENDAR MANAGEMENT ---

(defn list-calendar-nodes
  "List all calendar event nodes in Alfresco"
  []
  (let [ctx {}
        nodes (alfresco-events/fetch-calendar-nodes ctx)]
    (if (empty? nodes)
      (println "No calendar events found in Alfresco")
      (do
        (println "\n📁 Calendar Events in Alfresco:\n")
        (doseq [[idx node] (map-indexed vector nodes)]
          (let [entry (:entry node)
                props (:properties entry)]
            (println (str (inc idx) ".") (:name entry))
            (println "   ID:" (:id entry))
            (println "   Title:" (:ia:whatEvent props))
            (println "   From:" (:ia:fromDate props))
            (println "   To:" (:ia:toDate props))
            (when (:ia:whereEvent props)
              (println "   Where:" (:ia:whereEvent props)))
            (println)))))
    nodes))

(defn test-recurring-event
  "Test parsing and expansion of a specific recurring event

   Usage:
   (test-recurring-event \"node-id\")
   (test-recurring-event \"node-id\" 365) ; Next 365 days"
  ([node-id]
   (test-recurring-event node-id 90))
  ([node-id days]
   (let [ctx {}
         now (LocalDateTime/now)
         end (.plusDays now days)
         events (ical/parse-calendar-event ctx node-id now end cal/default-timezone)]
     (println "\n🔁 Testing Recurring Event Expansion:")
     (println "Node ID:" node-id)
     (println "Date Range:" (str now) "to" (str end))
     (println "Total Occurrences:" (count events))
     (when (seq events)
       (let [first-event (first events)]
         (println "\nFirst occurrence:")
         (pprint/pprint (format-event first-event))
         (when (:recurrence-rule first-event)
           (println "\nRecurrence Rule:" (:recurrence-rule first-event))
           (println "\nAll occurrence dates:")
           (doseq [[idx event] (map-indexed vector (take 20 events))]
             (println "  " (inc idx) "." (str (:start-local event)))
             (when (= idx 19)
               (println "  ... (showing first 20 of" (count events) "total)"))))))
     events)))

;; --- CONFIGURATION ---

(defn show-config
  "Show current calendar configuration"
  []
  (println "\n⚙️  Calendar Configuration:\n")
  (println "Timezone:" (:timezone cal/calendar-config))
  (println "Default Range:" (:default-range-days cal/calendar-config) "days")
  (println "Max Range:" (:max-range-days cal/calendar-config) "days")
  (println "Past Events:" (if (:enable-past-events cal/calendar-config) "Enabled" "Disabled"))
  (println "\nCalendar Folder ID:" (config/get-node-id :calendar))
  cal/calendar-config)

(defn set-timezone!
  "Set calendar timezone (requires server restart to persist)

   Usage:
   (set-timezone! \"America/New_York\")
   (set-timezone! \"America/Chicago\")
   (set-timezone! \"UTC\")"
  [timezone-id]
  (try
    (ZoneId/of timezone-id)  ; Validate timezone
    (System/setProperty "CALENDAR_TIMEZONE" timezone-id)
    (println "✅ Timezone set to:" timezone-id)
    (println "⚠️  Restart server for changes to take effect")
    timezone-id
    (catch Exception e
      (println "❌ Invalid timezone:" timezone-id)
      nil)))

;; --- DEBUGGING ---

(defn test-ics-parsing
  "Test direct .ics file parsing from Alfresco

   Usage:
   (test-ics-parsing \"node-id\")"
  [node-id]
  (let [ctx {}
        ics-content (ical/fetch-ics-content ctx node-id)]
    (if ics-content
      (do
        (println "\n📄 .ics Content Retrieved:")
        (println ics-content)
        (println "\n🔍 Parsing iCalendar...")
        (if-let [calendar (ical/parse-ics-content ics-content)]
          (let [vevents (.getComponents calendar "VEVENT")]
            (println "✅ Parsed successfully!")
            (println "Events found:" (count vevents))
            (doseq [vevent vevents]
              (let [event-data (ical/extract-event-data vevent)]
                (println "\nEvent:")
                (pprint/pprint event-data))))
          (println "❌ Failed to parse iCalendar")))
      (println "❌ Failed to fetch .ics content for node:" node-id))))

;; --- HELP ---

(defn help
  "Show available calendar REPL commands"
  []
  (println "\n📅 Calendar REPL Commands:\n")
  (println "QUERIES:")
  (println "  (list-events)                    - List upcoming events (90 days)")
  (println "  (list-events 30)                 - List events for next N days")
  (println "  (list-events 30 \"publish\")       - Filter by tag")
  (println "  (list-events-this-month)         - Events for current month")
  (println "  (list-events-by-month)           - Group events by month")
  (println "  (show-event \"node-id\")           - Show event details")
  (println "\nMANAGEMENT:")
  (println "  (list-calendar-nodes)            - List all calendar nodes")
  (println "  (test-recurring-event \"node-id\") - Test recurring event expansion")
  (println "  (show-config)                    - Show calendar configuration")
  (println "  (set-timezone! \"America/Chicago\") - Set timezone")
  (println "\nDEBUGGING:")
  (println "  (test-ics-parsing \"node-id\")     - Test .ics file parsing")
  (println "  (help)                           - Show this help\n"))

(comment
  ;; Quick start examples
  (help)
  (list-events)
  (list-events-by-month)
  (list-calendar-nodes)
  (show-config))
