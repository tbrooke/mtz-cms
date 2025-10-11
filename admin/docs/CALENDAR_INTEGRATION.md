# Calendar Integration with iCal4j & Recurring Events

## Overview

Mount Zion CMS now has **full support for Alfresco calendar events** including:

- ✅ **Property-based event reading** from Alfresco `ia:calendarEvent` nodes
- ✅ **Recurring event expansion** (RRULE support via iCal4j)
- ✅ **Tag resolution** (tag node IDs → tag names with caching)
- ✅ **Two display modes**: Events list (published only) and Calendar (all events)
- ✅ **Week/Month calendar views** with responsive design
- ✅ **REPL tools** for calendar management

**Live Pages:**
- **`/events`** - Events list showing published events only (filtered by "publish" tag)
- **`/events/calendar`** - Calendar with week/month tabs showing ALL events

---

## Architecture

### Components

```
Alfresco Calendar Events (ia:calendarEvent properties)
    ↓
Read properties (:ia:recurrenceRule, :ia:fromDate, etc.)
    ↓
Resolve tag IDs to tag names (with caching)
    ↓
Expand recurring events using iCal4j Recur
    ↓
Calendar Service (filter, sort, group)
    ↓
Web UI (events list + calendar views)
```

### Key Files

- **`src/mtz_cms/calendar/alfresco_events.clj`** - Property-based event reading, RRULE expansion, tag resolution
- **`src/mtz_cms/calendar/service.clj`** - High-level calendar service (delegates to alfresco_events)
- **`src/mtz_cms/components/events.clj`** - Event list and calendar view components
- **`src/mtz_cms/ui/pages.clj`** - Event pages (events-page, calendar-page)
- **`src/mtz_cms/routes/main.clj`** - Routes for /events and /events/calendar

---

## Quick Start

### 1. View Calendar on Website

**Published Events List:**
```
http://localhost:3000/events
```
Shows only events tagged with "publish"

**Full Calendar:**
```
http://localhost:3000/events/calendar
```
Shows ALL events in week/month views (no tag filter)

### 2. Test Calendar in REPL

```clojure
;; Load calendar service
(require '[mtz-cms.calendar.alfresco-events :as cal])

(def ctx {})

;; Get all upcoming events (next 90 days)
(def events (cal/get-upcoming-events ctx :days 90))
(count events)

;; Get only published events
(def published (cal/get-upcoming-events ctx :days 90 :tag-filter "publish"))
(count published)

;; View first event
(clojure.pprint/pprint (first events))
```

### 3. Add Events in Alfresco

1. **Go to Alfresco Share** - http://admin.mtzcg.com/share
2. **Navigate to Calendar** - Site → Calendar
3. **Create Event** - Click "Add Event"
4. **Set Recurrence** - For recurring events, set RRULE pattern
5. **Tag as "publish"** - To show on public events list
6. **Save** - Event automatically appears in CMS

---

## Calendar Event Structure

### Alfresco Properties

Calendar events in Alfresco have these properties:

```clojure
{:nodeType "ia:calendarEvent"
 :name "1758462011749-8540.ics"
 :properties
 {:ia:fromDate "2025-09-28T18:30:00.000+0000"
  :ia:toDate "2025-09-28T19:30:00.000+0000"
  :ia:whatEvent "Liberty Commons Worship"
  :ia:descriptionEvent "Join us for Worship at Liberty Commons"
  :ia:whereEvent "Liberty Commons"
  :ia:recurrenceRule "FREQ=WEEKLY;BYDAY=MO"  ; For recurring events
  :ia:isOutlook false  ; True if from Outlook
  :cm:taggable ["66f4d099-b1b9-41dc-b4d0-99b1b931dc1e"]}}  ; Tag node IDs
```

**Important Notes:**
- `:ia:recurrenceRule` contains the RRULE string for recurring events
- `:cm:taggable` contains tag **node IDs**, not tag names
- Tags are automatically resolved to names (e.g., "publish") with caching

### Parsed Event Data

After processing by `alfresco-events/node-to-events`:

```clojure
{:summary "Pickle Ball"
 :description "Intermediate and experienced Pickle Ball"
 :location "Mt Zion Son Court"
 :start-local #object[java.time.LocalDateTime "2025-10-20T17:30:00"]
 :end-local #object[java.time.LocalDateTime "2025-10-20T19:00:00"]
 :occurrence-date #object[java.time.LocalDateTime "2025-10-20T17:30:00"]
 :is-recurring "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO"
 :recurrence-rule "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO"
 :recurrence-instance false  ; true for individual occurrences of recurring event
 :node-id "abc-123-def"
 :tags ["publish"]  ; Resolved from tag node IDs
 :tag-ids ["66f4d099-b1b9-41dc-b4d0-99b1b931dc1e"]
 :alfresco-properties {...}}
```

**Recurring Event Expansion:**
The "Pickle Ball" event above expands to 12 occurrences (every Monday for 90 days):
- 2025-10-20 (Oct 20)
- 2025-10-27 (Oct 27)
- 2025-11-03 (Nov 3)
- ... (continues weekly)

---

## Technical Implementation

### Tag Resolution with Caching

Tags in Alfresco are stored as tag node IDs (e.g., `"66f4d099-b1b9-41dc-b4d0-99b1b931dc1e"`). The system automatically resolves these to tag names:

```clojure
;; In alfresco_events.clj
(def tag-name-cache (atom {}))

(defn resolve-tag-name [ctx tag-id]
  (if-let [cached (@tag-name-cache tag-id)]
    cached
    (let [result (alfresco/get-node ctx tag-id)
          tag-name (get-in result [:data :entry :name])]
      (swap! tag-name-cache assoc tag-id tag-name)
      tag-name)))
```

**Benefits:**
- First lookup hits Alfresco API
- Subsequent lookups use cache
- Dramatically reduces API calls

### DateTime Conversion (Critical Fix)

iCal4j 3.x requires epoch milliseconds for DateTime constructor:

```clojure
(defn local-datetime->ical-datetime [local-dt timezone-id]
  (-> local-dt
      (.atZone (ZoneId/of timezone-id))
      .toInstant
      .toEpochMilli  ; ← Critical: must convert to long millis
      DateTime.))
```

**Without `.toEpochMilli`:**
- DateTime constructor fails with IllegalArgumentException
- Recurring events expand to 0 occurrences
- No events appear on calendar

**With `.toEpochMilli`:**
- DateTime constructed correctly
- RRULE expansion works
- All recurring events display properly

---

## Recurring Events

### How Recurring Events Work

1. **Event stored in Alfresco** with `:ia:recurrenceRule` (e.g., "FREQ=WEEKLY;BYDAY=MO")
2. **Read RRULE from properties** (not from .ics file)
3. **iCal4j Recur.getDates()** expands into individual dates
4. **Calendar service** converts dates to LocalDateTime and creates event occurrences
5. **Each occurrence** becomes a separate event in the result list

### Example: Weekly Recurring Event

**RRULE:** `FREQ=WEEKLY;BYDAY=SA;UNTIL=20251231T235959Z`

**Expands to:**
```
1. 2025-10-11 18:30:00 (Saturday)
2. 2025-10-18 18:30:00 (Saturday)
3. 2025-10-25 18:30:00 (Saturday)
4. 2025-11-01 18:30:00 (Saturday)
... (continues until end date)
```

### Supported Recurrence Patterns

iCal4j supports all RFC 5545 patterns:

- **DAILY** - Every day
- **WEEKLY** - Every week (with BYDAY: MO,TU,WE,TH,FR,SA,SU)
- **MONTHLY** - Every month (by day of month or week)
- **YEARLY** - Every year
- **Complex** - Multiple rules, exceptions (EXDATE), etc.

### Testing Recurring Events

```clojure
;; Test a recurring event expansion
(cal/test-recurring-event "node-id" 365)  ; Expand for next 365 days

;; Parse and examine .ics file directly
(cal/test-ics-parsing "node-id")
```

---

## Calendar Configuration

### Environment Variables

```bash
# Set in .env or system environment
CALENDAR_TIMEZONE=America/New_York  # Default timezone
```

### Runtime Configuration

```clojure
;; In REPL
(cal/set-timezone! "America/Chicago")
(cal/show-config)
```

### Service Configuration

Edit `src/mtz_cms/calendar/service.clj`:

```clojure
(def calendar-config
  {:timezone "America/New_York"
   :default-range-days 90   ; Default to 90 days forward
   :max-range-days 365      ; Maximum 1 year range
   :enable-past-events false}) ; Hide past events by default
```

---

## Using Calendar in Your Code

### Query Calendar Events

```clojure
(require '[mtz-cms.calendar.service :as cal])

(def ctx {})  ; Alfresco context

;; Get all events for next 90 days (default)
(def events (cal/get-calendar-events ctx))

;; Get events for specific date range
(def events (cal/get-calendar-events ctx
              :start-date "2025-10-01T00:00:00"
              :end-date "2025-10-31T23:59:59"))

;; Get only published events
(def published (cal/get-calendar-events ctx :tag-filter "publish"))

;; Get upcoming 10 events
(def upcoming (cal/get-upcoming-events ctx :limit 10))
```

### Group Events

```clojure
;; Group by month for calendar grid
(def by-month (cal/get-events-by-month ctx))
;; Returns: {"2025-10" [event1 event2 ...], "2025-11" [...]}

;; Group by day for list view
(def by-day (cal/get-events-by-day ctx))
;; Returns: {#object[java.time.LocalDate "2025-10-11"] [event1 event2 ...]}
```

### Display Events

```clojure
;; In your page template
(defn events-page [ctx]
  (let [events (cal/get-upcoming-events ctx :days 30 :tag-filter "publish")]
    [:div
     [:h1 "Upcoming Events"]
     (for [event events]
       [:div {:class "event-card"}
        [:h2 (:summary event)]
        [:p "📅 " (str (:start-local event))]
        (when (:location event)
          [:p "📍 " (:location event)])
        (when (:is-recurring event)
          [:span {:class "badge"} "🔁 Recurring"])
        [:p (:description event)]])]))
```

---

## Admin Dashboard

### Accessing the Dashboard

**URL:** http://localhost:3000/admin

### Features

#### Dashboard Home
- Quick stats (event count, cache status, Alfresco connection)
- Quick actions (manage calendar, content, settings)

#### Calendar Management
- View upcoming events
- Configure timezone
- Set default date range
- Refresh events list

#### Settings
- Email configuration (Mailgun)
- Cache management
- System settings

### Adding Dashboard to Routes

In `src/mtz_cms/routes/main.clj`:

```clojure
(require '[mtz-cms.admin.dashboard :as admin])

;; Add to routes
(def routes
  [;; ... existing routes

   ;; Admin routes
   ["/admin" {:get admin/admin-home-handler}]
   ["/admin/calendar" {:get admin/admin-calendar-handler}]
   ["/admin/settings" {:get admin/admin-settings-handler}]
   ["/admin/api/stats/events-count" {:get admin/api-events-count-handler}]
   ["/admin/api/system/alfresco-status" {:get admin/api-alfresco-status-handler}]
   ["/admin/api/calendar/upcoming" {:get admin/api-calendar-upcoming-handler}]])
```

---

## Integration with Outlook

### How Alfresco + Outlook Works

1. **Alfresco IMAP** - Alfresco can receive calendar invites via email
2. **Outlook sends** calendar invite to Alfresco email address
3. **Alfresco creates** `ia:calendarEvent` node with `.ics` file
4. **Mount Zion CMS** parses and displays the event

### Setting Up Outlook Integration

See `admin/docs/MAILGUN_SETUP.md` for:
- Alfresco IMAP configuration
- Mailgun inbound routing
- Email-to-calendar workflows

### Outlook Properties

When `ia:isOutlook` is `true`, the event came from Outlook and may have:
- More complex recurrence rules
- Attendee information
- Meeting organizer details
- Outlook-specific metadata

---

## Pathom Integration

### Creating Calendar Resolver

Add to `src/mtz_cms/pathom/resolvers.clj`:

```clojure
(require '[mtz-cms.calendar.service :as cal-service])

(defresolver upcoming-calendar-events
  "Get upcoming calendar events with recurring event expansion"
  [{:keys [ctx]} {:keys [days limit tag-filter]}]
  {::pco/output [:calendar/upcoming-events]}
  (let [events (cal-service/get-upcoming-events ctx
                 :days (or days 30)
                 :limit limit
                 :tag-filter tag-filter)]
    {:calendar/upcoming-events events}))

(defresolver calendar-events-by-month
  "Get calendar events grouped by month"
  [{:keys [ctx]} {:keys [start-date end-date]}]
  {::pco/output [:calendar/events-by-month]}
  (let [events-map (cal-service/get-events-by-month ctx
                     :start-date start-date
                     :end-date end-date)]
    {:calendar/events-by-month events-map}))

;; Register resolvers
(def resolvers
  [upcoming-calendar-events
   calendar-events-by-month
   ;; ... other resolvers
   ])
```

### Querying via Pathom

```clojure
;; In component or handler
(require '[mtz-cms.pathom.resolvers :as pathom])

(def result (pathom/query ctx
  [{:calendar/upcoming-events
    [:summary :start-local :end-local :location :is-recurring :tags]}]))
```

---

## Troubleshooting

### Event not showing up?

```clojure
;; Check if event node exists
(cal/list-calendar-nodes)

;; Test parsing the specific event
(cal/show-event "node-id")

;; Test .ics parsing directly
(cal/test-ics-parsing "node-id")
```

### Recurring event not expanding?

```clojure
;; Test recurring event expansion
(cal/test-recurring-event "node-id" 365)

;; Check for RRULE in output
;; If no RRULE, the event isn't actually recurring in Alfresco
```

### Timezone issues?

```clojure
;; Check current timezone
(cal/show-config)

;; Set correct timezone
(cal/set-timezone! "America/New_York")

;; Restart REPL for changes to take effect
```

### Cache showing stale events?

```clojure
;; Clear cache via REPL
(require '[mtz-cms.cache.simple :as cache])
(cache/clear-cache!)

;; Or via admin dashboard
;; Visit http://localhost:3000/admin → Click "Clear Cache"
```

---

## Performance Considerations

### Date Range Limits

- **Default range:** 90 days (configurable)
- **Maximum range:** 365 days
- Large ranges with many recurring events can be slow

### Caching Strategy

- Calendar queries are **not cached** by default (events change frequently)
- Consider caching for:
  - Public events page (5-15 minute cache)
  - Monthly calendars (longer cache)

### Optimization Tips

1. **Filter by tag** - Use tags to show only published events
2. **Limit results** - Use `:limit` parameter for large lists
3. **Narrow date range** - Query specific months instead of whole year
4. **Group by month/day** - More efficient for calendar displays

---

## Next Steps

### Enhance Admin Dashboard

1. **Add authentication** - Replace simple auth with proper session management
2. **Event editing** - Create/edit events via web interface
3. **Bulk operations** - Publish/unpublish multiple events
4. **Analytics** - Track event views and engagement

### Advanced Calendar Features

1. **Event categories** - Group events by type (worship, outreach, etc.)
2. **Calendar feeds** - Generate iCal feed for external calendar apps
3. **Email reminders** - Send reminders before events
4. **Registration** - Allow people to RSVP to events

### Integration Ideas

1. **Google Calendar sync** - Two-way sync with Google Calendar
2. **Newsletter integration** - Auto-include upcoming events in email
3. **Homepage widget** - Show next 3 events on homepage
4. **Mobile app** - Expose calendar via REST API

---

## Resources

- **iCal4j Documentation:** https://www.ical4j.org/
- **RFC 5545 (iCalendar):** https://tools.ietf.org/html/rfc5545
- **Alfresco Calendar API:** https://docs.alfresco.com/content-services/latest/develop/rest-api-guide/
- **Admin Dashboard Code:** `src/mtz_cms/admin/dashboard.clj`
- **REPL Tools:** `src/mtz_cms/calendar/repl.clj`
