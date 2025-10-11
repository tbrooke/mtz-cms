(ns mtz-cms.calendar.feed
  "Calendar events from Alfresco iCal feed

   Fetches the entire site calendar feed and parses all events"
  (:require
   [mtz-cms.calendar.ical :as ical]
   [mtz-cms.config.core :as config]
   [clojure.tools.logging :as log])
  (:import
   [java.time LocalDateTime ZoneId]))

(def default-site-id "swsdp")

(defn get-all-events-from-feed
  "Fetch and parse all events from Alfresco iCal feed

   Parameters:
   - ctx: Alfresco context
   - start-date: Filter events after this date (LocalDateTime)
   - end-date: Filter events before this date (LocalDateTime)
   - timezone-id: Timezone (default: America/New_York)
   - site-id: Alfresco site ID (default: swsdp)

   Returns: Sequence of event occurrences"
  [ctx start-date end-date timezone-id & {:keys [site-id] :or {site-id default-site-id}}]
  (try
    (log/info "Fetching calendar feed for site:" site-id)

    ;; Fetch the iCal feed
    (if-let [feed-content (ical/fetch-ics-feed ctx site-id)]
      (do
        (log/info "✅ Got iCal feed, parsing...")

        ;; Parse the feed
        (if-let [calendar (ical/parse-ics-content feed-content)]
          (let [vevents (.getComponents calendar "VEVENT")
                event-count (count vevents)]

            (log/info "Found" event-count "events in feed")

            ;; Expand each event (handles recurring)
            (mapcat #(ical/expand-recurring-event % start-date end-date timezone-id)
                    vevents))

          (do
            (log/error "Failed to parse iCal feed")
            [])))

      (do
        (log/error "Failed to fetch iCal feed")
        []))

    (catch Exception e
      (log/error "Error getting events from feed:" (.getMessage e))
      (.printStackTrace e)
      [])))

(defn get-upcoming-events
  "Get upcoming events from feed

   Parameters:
   - ctx: Alfresco context
   - days: Number of days to look ahead (default: 30)
   - timezone-id: Timezone (default: America/New_York)
   - limit: Max number of events
   - site-id: Alfresco site ID"
  [ctx & {:keys [days timezone-id limit site-id]
          :or {days 30 timezone-id "America/New_York" site-id default-site-id}}]
  (let [now (LocalDateTime/now (ZoneId/of timezone-id))
        end (.plusDays now days)
        events (get-all-events-from-feed ctx now end timezone-id :site-id site-id)]

    (if limit
      (take limit events)
      events)))

(comment
  (require '[mtz-cms.calendar.feed :as feed])

  (def ctx {})

  ;; Get upcoming events
  (def events (feed/get-upcoming-events ctx :days 90))
  (count events)
  (first events))
