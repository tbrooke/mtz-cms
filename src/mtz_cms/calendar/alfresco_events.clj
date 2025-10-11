(ns mtz-cms.calendar.alfresco-events
  "Calendar events from Alfresco properties with iCal4j recurrence expansion

   Alfresco Internet Archive calendar model stores:
   - :ia:fromDate / :ia:toDate - Event dates
   - :ia:whatEvent - Title
   - :ia:descriptionEvent - Description
   - :ia:whereEvent - Location
   - :ia:recurrenceRule - RRULE for recurring events
   - :ia:recurrenceLastMeeting - Last occurrence
   - :ia:ignoreEventList - Exception dates (EXDATE)
   - :ia:isOutlook - Whether from Outlook"
  (:require
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.config.core :as config]
   [clojure.string :as str]
   [clojure.tools.logging :as log])
  (:import
   [net.fortuna.ical4j.model DateTime Period Recur]
   [net.fortuna.ical4j.model.property RRule]
   [java.time ZonedDateTime LocalDateTime ZoneId]
   [java.time.format DateTimeFormatter]))

;; Configure iCal4j timezone cache
(System/setProperty "net.fortuna.ical4j.timezone.cache.impl"
                    "net.fortuna.ical4j.util.MapTimeZoneCache")

;; --- DATE PARSING ---

(def alfresco-date-formatter
  "Alfresco uses ISO 8601: 2025-10-10T14:00:00.000+0000"
  (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSSX"))

(defn parse-alfresco-date
  "Parse Alfresco date string to LocalDateTime"
  [date-str timezone-id]
  (when (and date-str (not (str/blank? date-str)))
    (try
      (-> date-str
          (ZonedDateTime/parse alfresco-date-formatter)
          (.withZoneSameInstant (ZoneId/of timezone-id))
          .toLocalDateTime)
      (catch Exception e
        (log/error "Failed to parse date:" date-str (.getMessage e))
        nil))))

(defn local-datetime->ical-datetime
  "Convert LocalDateTime to iCal4j DateTime"
  [local-dt timezone-id]
  (when local-dt
    (-> local-dt
        (.atZone (ZoneId/of timezone-id))
        .toInstant
        DateTime.)))

;; --- RECURRING EVENT EXPANSION ---

(defn expand-recurrence
  "Expand recurring event using RRULE

   Parameters:
   - start-date: Event start (LocalDateTime)
   - end-date: Event end (LocalDateTime)
   - rrule-str: RRULE string (e.g., 'FREQ=WEEKLY;BYDAY=MO,WE,FR')
   - range-start: Range start to expand (LocalDateTime)
   - range-end: Range end to expand (LocalDateTime)
   - timezone-id: Timezone

   Returns: Sequence of occurrence dates (LocalDateTime)"
  [start-date end-date rrule-str range-start range-end timezone-id]
  (try
    (when (and rrule-str (not (str/blank? rrule-str)))
      (let [;; Parse RRULE
            recur (Recur. rrule-str)

            ;; Convert to iCal4j DateTime
            event-start (local-datetime->ical-datetime start-date timezone-id)
            period-start (local-datetime->ical-datetime range-start timezone-id)
            period-end (local-datetime->ical-datetime range-end timezone-id)

            ;; Create period for expansion
            period (Period. period-start period-end)

            ;; Get occurrences
            dates (.getDates recur event-start period-start period-end)

            ;; Filter to period and convert back to LocalDateTime
            duration (.between java.time.temporal.ChronoUnit/MINUTES start-date end-date)]

        (log/info "Expanding RRULE:" rrule-str "- found" (count dates) "occurrences")

        (mapv (fn [ical-date]
                (let [occ-start (-> (.toInstant ical-date)
                                   (ZonedDateTime/ofInstant (ZoneId/of timezone-id))
                                   .toLocalDateTime)]
                  {:start occ-start
                   :end (.plusMinutes occ-start duration)}))
              dates)))
    (catch Exception e
      (log/error "Failed to expand RRULE:" rrule-str (.getMessage e))
      [])))

;; --- TAG RESOLUTION ---

(def tag-name-cache
  "Cache for tag ID -> name resolution"
  (atom {}))

(defn resolve-tag-name
  "Resolve tag node ID to tag name

   Returns tag name or nil if not found"
  [ctx tag-id]
  (if-let [cached (@tag-name-cache tag-id)]
    cached
    (try
      (let [result (alfresco/get-node ctx tag-id)]
        (when (:success result)
          (let [tag-name (get-in result [:data :entry :name])]
            (swap! tag-name-cache assoc tag-id tag-name)
            tag-name)))
      (catch Exception e
        (log/warn "Failed to resolve tag" tag-id ":" (.getMessage e))
        nil))))

(defn resolve-tag-names
  "Resolve all tag IDs to tag names

   Returns vector of tag names"
  [ctx tag-ids]
  (when (seq tag-ids)
    (vec (keep #(resolve-tag-name ctx %) tag-ids))))

;; --- EVENT CONVERSION ---

(defn node-to-events
  "Convert Alfresco calendar node to event occurrence(s)

   Returns sequence of event maps (multiple if recurring)"
  [ctx node range-start range-end timezone-id]
  (let [entry (:entry node)
        props (:properties entry)
        start-str (:ia:fromDate props)
        end-str (:ia:toDate props)
        start-local (parse-alfresco-date start-str timezone-id)
        end-local (parse-alfresco-date end-str timezone-id)
        rrule (:ia:recurrenceRule props)
        has-recurrence (and rrule (not (str/blank? rrule)))
        tag-ids (or (:cm:taggable props) [])
        tag-names (resolve-tag-names ctx tag-ids)]

    (when (and start-local end-local)
      (let [base-event {:summary (:ia:whatEvent props)
                       :description (:ia:descriptionEvent props)
                       :location (:ia:whereEvent props)
                       :is-recurring has-recurrence
                       :recurrence-rule rrule
                       :node-id (:id entry)
                       :tags tag-names  ; Now contains actual tag names
                       :tag-ids tag-ids  ; Keep IDs for reference
                       :alfresco-properties props
                       :is-outlook (:ia:isOutlook props false)}]

        (if has-recurrence
          ;; Recurring event - expand occurrences
          (let [occurrences (expand-recurrence start-local end-local rrule
                                              range-start range-end timezone-id)]
            (log/info "Recurring event" (:ia:whatEvent props) "-" (count occurrences) "occurrences")
            (mapv (fn [occ]
                    (assoc base-event
                           :start-local (:start occ)
                           :end-local (:end occ)
                           :occurrence-date (:start occ)
                           :recurrence-instance true))
                  occurrences))

          ;; Single event - check if in range
          (if (and (.isAfter start-local range-start)
                   (.isBefore start-local range-end))
            [(assoc base-event
                    :start-local start-local
                    :end-local end-local
                    :occurrence-date start-local
                    :recurrence-instance false)]
            []))))))

;; --- EVENT FETCHING ---

(defn fetch-calendar-nodes
  "Fetch calendar event nodes from Alfresco"
  [ctx]
  (if-let [calendar-folder-id (config/get-node-id :calendar)]
    (let [result (alfresco/get-node-children ctx calendar-folder-id
                                              {:include "properties"})]
      (if (:success result)
        (let [entries (get-in result [:data :list :entries])
              cal-events (filter #(= "ia:calendarEvent" (get-in % [:entry :nodeType]))
                                entries)]
          (log/info "Found" (count cal-events) "calendar event nodes in Alfresco")
          cal-events)
        (do
          (log/error "Failed to fetch calendar nodes:" (:message result))
          [])))
    (do
      (log/warn "No calendar folder configured in config")
      [])))

(defn get-events-for-range
  "Get calendar events within date range with recurrence expansion

   Parameters:
   - ctx: Alfresco context
   - start-date: Range start (LocalDateTime)
   - end-date: Range end (LocalDateTime)
   - timezone-id: Timezone (default: America/New_York)
   - tag-filter: Optional tag to filter by

   Returns: Sequence of event occurrences sorted by date"
  [ctx start-date end-date timezone-id & {:keys [tag-filter]}]
  (let [nodes (fetch-calendar-nodes ctx)

        ;; Convert each node to events (may be multiple if recurring)
        all-events (mapcat #(node-to-events ctx % start-date end-date timezone-id) nodes)

        ;; Filter by tag if specified
        filtered (if tag-filter
                  (filter #(contains? (set (:tags %)) tag-filter) all-events)
                  all-events)]

    (log/info "Total event occurrences in range:" (count filtered))
    (sort-by :occurrence-date filtered)))

(defn get-upcoming-events
  "Get upcoming events (next N days)

   Parameters:
   - ctx: Alfresco context
   - days: Number of days to look ahead (default: 30)
   - timezone-id: Timezone (default: America/New_York)
   - limit: Max number of events to return
   - tag-filter: Optional tag to filter by"
  [ctx & {:keys [days timezone-id limit tag-filter]
          :or {days 30 timezone-id "America/New_York"}}]
  (let [now (LocalDateTime/now (ZoneId/of timezone-id))
        end (.plusDays now days)
        events (get-events-for-range ctx now end timezone-id :tag-filter tag-filter)]
    (if limit
      (take limit events)
      events)))

(defn get-events-by-day
  "Get events grouped by day for calendar display"
  [ctx & {:keys [start-date end-date timezone-id tag-filter]
          :or {timezone-id "America/New_York"}}]
  (let [events (get-events-for-range ctx start-date end-date timezone-id
                                     :tag-filter tag-filter)]
    (group-by (fn [event]
                (when-let [dt (:occurrence-date event)]
                  (.toLocalDate dt)))
              events)))

;; --- REPL TESTING ---

(comment
  (require '[mtz-cms.calendar.alfresco-events :as cal])

  (def ctx {})

  ;; Get upcoming events
  (def events (cal/get-upcoming-events ctx :days 90))
  (count events)
  (first events)

  ;; Get events for specific range
  (def now (java.time.LocalDateTime/now))
  (def end (.plusDays now 90))
  (cal/get-events-for-range ctx now end "America/New_York")

  ;; Group by day
  (def by-day (cal/get-events-by-day ctx
                :start-date now
                :end-date end))
  (keys by-day))
