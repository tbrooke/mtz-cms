(ns mtz-cms.calendar.ical
  "iCalendar parsing and recurring event expansion using iCal4j

   Handles parsing .ics files from Alfresco calendar events and expanding
   recurring events (RRULE) according to RFC 5545 specification.

   Supports:
   - Single events
   - Recurring events (daily, weekly, monthly, yearly)
   - Event exceptions (EXDATE)
   - Outlook-generated calendar rules"
  (:require
   [clojure.tools.logging :as log]
   [clojure.string :as str]
   [mtz-cms.alfresco.client :as alfresco]
   [clj-http.client])
  (:import
   [net.fortuna.ical4j.data CalendarBuilder]
   [net.fortuna.ical4j.model Calendar DateTime Period]
   [net.fortuna.ical4j.model.component VEvent]
   [net.fortuna.ical4j.model.property RRule Summary Description Location]
   [net.fortuna.ical4j.util MapTimeZoneCache]
   [java.io StringReader]
   [java.time ZonedDateTime LocalDateTime ZoneId]
   [java.time.format DateTimeFormatter]))

;; Configure iCal4j timezone cache
(System/setProperty "net.fortuna.ical4j.timezone.cache.impl"
                    "net.fortuna.ical4j.util.MapTimeZoneCache")

;; --- DATE/TIME UTILITIES ---

(defn ical-datetime->instant
  "Convert iCal4j DateTime to Java Instant"
  [^DateTime dt]
  (when dt
    (.toInstant (.toZonedDateTime dt))))

(defn instant->local-datetime
  "Convert Instant to LocalDateTime in specified timezone"
  [instant timezone-id]
  (when instant
    (-> instant
        (ZonedDateTime/ofInstant (ZoneId/of (or timezone-id "America/New_York")))
        .toLocalDateTime)))

(defn format-datetime
  "Format LocalDateTime for display"
  [local-dt]
  (when local-dt
    (.format local-dt (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))))

;; --- ICALENDAR PARSING ---

(defn parse-ics-content
  "Parse iCalendar content string into Calendar object

   Returns: iCal4j Calendar object or nil on error"
  [ics-content]
  (try
    (let [builder (CalendarBuilder.)
          reader (StringReader. ics-content)]
      (.build builder reader))
    (catch Exception e
      (log/error "Failed to parse iCalendar content:" (.getMessage e))
      nil)))

(defn fetch-ics-feed
  "Fetch iCal feed from Alfresco Share feed service

   Note: Share feed service must be accessed via the actual server URL,
   not through SSH tunnel. Uses http://admin.mtzcg.com directly.

   Returns: String content of .ics feed or nil on error"
  [ctx site-id]
  (try
    (let [config (alfresco/get-config ctx)
          ;; Share feed service needs actual server URL, not tunnel
          share-base-url (or (System/getenv "ALFRESCO_SHARE_URL") "http://admin.mtzcg.com")
          username (:username config)
          password (:password config)
          feed-url (str share-base-url "/share/feedservice/components/calendar/ical/eventList-" site-id ".ics?site=" site-id)]

      (log/info "Fetching iCal feed from:" feed-url)

      (let [response (clj-http.client/get feed-url
                                         {:basic-auth [username password]
                                          :throw-exceptions false})]

        (if (= 200 (:status response))
          (do
            (log/info "✅ Successfully fetched iCal feed -" (count (:body response)) "bytes")
            (:body response))
          (do
            (log/error "❌ Failed to fetch iCal feed - status:" (:status response))
            nil))))
    (catch Exception e
      (log/error "Error fetching iCal feed:" (.getMessage e))
      (.printStackTrace e)
      nil)))

(defn fetch-ics-content
  "Fetch .ics file content from Alfresco by node ID

   Returns: String content of .ics file or nil on error"
  [ctx node-id]
  (try
    (let [result (alfresco/get-node-content ctx node-id)]
      (if (:success result)
        ;; Convert byte array to string
        (String. (:data result) "UTF-8")
        (do
          (log/error "Failed to fetch .ics content for node:" node-id)
          nil)))
    (catch Exception e
      (log/error "Error fetching .ics content:" (.getMessage e))
      nil)))

;; --- EVENT EXTRACTION ---

(defn extract-event-data
  "Extract data from VEvent component

   Returns map with:
   - :summary - Event title
   - :description - Event description
   - :location - Event location
   - :start - Start DateTime
   - :end - End DateTime
   - :recurrence-rule - RRULE string (if recurring)"
  [^VEvent vevent]
  (let [summary (.getValue (.getSummary vevent))
        description (some-> vevent .getDescription .getValue)
        location (some-> vevent .getLocation .getValue)
        start-date (.getStartDate vevent)
        end-date (.getEndDate vevent)
        rrule-prop (.getProperty vevent "RRULE")
        rrule (when rrule-prop (.getValue rrule-prop))]
    {:summary summary
     :description description
     :location location
     :start (.getDate start-date)
     :end (.getDate end-date)
     :recurrence-rule (when rrule (.getValue rrule))
     :is-recurring (some? rrule)}))

;; --- RECURRING EVENT EXPANSION ---

(defn expand-recurring-event
  "Expand recurring event within date range

   Parameters:
   - vevent: VEvent component
   - start-date: Range start (java.time.LocalDateTime)
   - end-date: Range end (java.time.LocalDateTime)
   - timezone-id: Timezone ID (default: America/New_York)

   Returns: Sequence of event occurrence maps"
  [^VEvent vevent start-date end-date timezone-id]
  (try
    (let [event-data (extract-event-data vevent)
          rrule (.getProperty vevent "RRULE")]

      (if-not rrule
        ;; Single event - just return if within range
        (let [event-start (instant->local-datetime
                           (ical-datetime->instant (:start event-data))
                           timezone-id)]
          (if (and event-start
                   (.isAfter event-start start-date)
                   (.isBefore event-start end-date))
            [(assoc event-data
                    :start-local event-start
                    :end-local (instant->local-datetime
                               (ical-datetime->instant (:end event-data))
                               timezone-id)
                    :occurrence-date event-start)]
            []))

        ;; Recurring event - expand using RRULE
        (let [period-start (-> start-date
                              (.atZone (ZoneId/of timezone-id))
                              .toInstant
                              DateTime.)
              period-end (-> end-date
                            (.atZone (ZoneId/of timezone-id))
                            .toInstant
                            DateTime.)
              period (Period. period-start period-end)
              occurrences (.getOccurrences vevent period)]

          (log/info "Expanding recurring event:" (:summary event-data)
                   "found" (count occurrences) "occurrences")

          (mapv (fn [occurrence-period]
                  (let [occ-start (ical-datetime->instant (.getStart occurrence-period))
                        occ-end (ical-datetime->instant (.getEnd occurrence-period))
                        occ-start-local (instant->local-datetime occ-start timezone-id)
                        occ-end-local (instant->local-datetime occ-end timezone-id)]
                    (assoc event-data
                           :start-local occ-start-local
                           :end-local occ-end-local
                           :occurrence-date occ-start-local
                           :recurrence-instance true)))
                occurrences))))

    (catch Exception e
      (log/error "Error expanding recurring event:" (.getMessage e))
      [])))

;; --- HIGH-LEVEL API ---

(defn parse-calendar-event
  "Parse calendar event from Alfresco and expand recurring events

   Parameters:
   - ctx: Alfresco context
   - node-id: Calendar event node ID
   - start-date: Range start (LocalDateTime)
   - end-date: Range end (LocalDateTime)
   - timezone-id: Timezone (default: America/New_York)

   Returns: Sequence of event occurrence maps"
  [ctx node-id start-date end-date timezone-id]
  (try
    (when-let [ics-content (fetch-ics-content ctx node-id)]
      (when-let [calendar (parse-ics-content ics-content)]
        (let [vevents (.getComponents calendar "VEVENT")]
          (log/info "Parsed calendar for node" node-id "- found" (count vevents) "events")
          (mapcat #(expand-recurring-event % start-date end-date timezone-id)
                  vevents))))
    (catch Exception e
      (log/error "Error parsing calendar event:" (.getMessage e))
      [])))

(defn get-events-for-range
  "Get all calendar events for a date range from multiple Alfresco event nodes

   Parameters:
   - ctx: Alfresco context
   - event-nodes: Collection of calendar event node maps from Alfresco
   - start-date: Range start (LocalDateTime)
   - end-date: Range end (LocalDateTime)
   - timezone-id: Timezone (default: America/New_York)

   Returns: Sequence of all event occurrences sorted by date"
  [ctx event-nodes start-date end-date timezone-id]
  (let [all-occurrences
        (mapcat (fn [node]
                  (let [node-id (get-in node [:entry :id])
                        node-name (get-in node [:entry :name])]
                    (log/info "Processing calendar event node:" node-name)
                    (parse-calendar-event ctx node-id start-date end-date timezone-id)))
                event-nodes)]

    ;; Sort by occurrence date
    (sort-by :occurrence-date all-occurrences)))

;; --- EVENT FILTERING & GROUPING ---

(defn filter-events-by-tag
  "Filter events by Alfresco tag (e.g., 'publish')

   Note: This requires fetching node properties/tags from Alfresco"
  [events tag-filter]
  (if tag-filter
    (filter #(contains? (set (:tags %)) tag-filter) events)
    events))

(defn group-events-by-month
  "Group events by year-month for calendar display"
  [events]
  (group-by (fn [event]
              (when-let [dt (:occurrence-date event)]
                (.format dt (DateTimeFormatter/ofPattern "yyyy-MM"))))
            events))

(defn group-events-by-day
  "Group events by date for daily listing"
  [events]
  (group-by (fn [event]
              (when-let [dt (:occurrence-date event)]
                (.toLocalDate dt)))
            events))

;; --- REPL TESTING ---

(comment
  ;; Test parsing a calendar event
  (require '[mtz-cms.calendar.ical :as ical])
  (require '[java.time :as jt])

  ;; Get events for next 30 days
  (def start (LocalDateTime/now))
  (def end (.plusDays start 30))

  ;; Parse single event
  (def ctx {})
  (def event-node-id "04cf1c10-8a1c-4b91-8f1c-108a1c9b913e")
  (ical/parse-calendar-event ctx event-node-id start end "America/New_York")

  ;; Parse multiple events
  (def event-nodes [{:entry {:id "04cf1c10-8a1c-4b91-8f1c-108a1c9b913e"
                             :name "event1.ics"}}])
  (ical/get-events-for-range ctx event-nodes start end "America/New_York")

  ;; Group by month for calendar view
  (def events (ical/get-events-for-range ctx event-nodes start end "America/New_York"))
  (ical/group-events-by-month events))
