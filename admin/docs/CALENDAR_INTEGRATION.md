# Calendar Integration with iCal4j & Recurring Events

## Overview

Mount Zion CMS now has **full support for Alfresco calendar events** including:

- ✅ **iCalendar (.ics) parsing** using iCal4j library
- ✅ **Recurring event expansion** (RRULE support)
- ✅ **Outlook calendar integration** via Alfresco
- ✅ **REPL tools** for calendar management
- ✅ **Admin Dashboard** for web-based configuration

---

## Architecture

### Components

```
Alfresco Calendar Events (.ics files)
    ↓
iCal4j Parser (RFC 5545 compliant)
    ↓
Calendar Service (expand recurring events)
    ↓
Pathom Resolvers (query interface)
    ↓
Web UI / REPL Tools
```

### Key Files

- **`src/mtz_cms/calendar/ical.clj`** - iCal4j integration, .ics parsing, RRULE expansion
- **`src/mtz_cms/calendar/service.clj`** - High-level calendar service with date ranges
- **`src/mtz_cms/calendar/repl.clj`** - REPL tools for testing and management
- **`src/mtz_cms/admin/dashboard.clj`** - Web-based admin interface

---

## Quick Start

### 1. Restart REPL to Load iCal4j

```bash
# Stop current REPL
# Start fresh
clojure -M:dev
```

### 2. Test Calendar in REPL

```clojure
;; Load calendar tools
(require '[mtz-cms.calendar.repl :as cal])

;; Show help
(cal/help)

;; List upcoming events (next 90 days)
(cal/list-events)

;; List events for next 30 days
(cal/list-events 30)

;; List only published events
(cal/list-events 30 "publish")

;; Show events grouped by month
(cal/list-events-by-month)

;; List all calendar nodes in Alfresco
(cal/list-calendar-nodes)

;; Test a specific recurring event
(cal/test-recurring-event "04cf1c10-8a1c-4b91-8f1c-108a1c9b913e")

;; Show calendar configuration
(cal/show-config)
```

### 3. Access Admin Dashboard

Visit: **http://localhost:3000/admin**

Features:
- View upcoming events
- Configure timezone and date ranges
- Clear cache
- System status monitoring

---

## Calendar Event Structure

### Alfresco Properties

Calendar events in Alfresco have these properties:

```clojure
{:nodeType "ia:calendarEvent"
 :name "1758462011749-8540.ics"  ; .ics file
 :properties
 {:ia:fromDate "2025-09-28T18:30:00.000+0000"
  :ia:toDate "2025-09-28T19:30:00.000+0000"
  :ia:whatEvent "Liberty Commons Worship"
  :ia:descriptionEvent "Join us for Worship at Liberty Commons"
  :ia:whereEvent "Liberty Commons"
  :ia:isOutlook false  ; True if from Outlook
  :cm:taggable ["publish"]}}  ; Tags for filtering
```

### Parsed Event Data

After parsing with iCal4j:

```clojure
{:summary "Liberty Commons Worship"
 :description "Join us for Worship at Liberty Commons"
 :location "Liberty Commons"
 :start-local #object[java.time.LocalDateTime "2025-09-28T18:30:00"]
 :end-local #object[java.time.LocalDateTime "2025-09-28T19:30:00"]
 :occurrence-date #object[java.time.LocalDateTime "2025-09-28T18:30:00"]
 :is-recurring false
 :recurrence-rule nil  ; e.g., "FREQ=WEEKLY;BYDAY=SA"
 :node-id "04cf1c10-8a1c-4b91-8f1c-108a1c9b913e"
 :tags ["publish"]
 :alfresco-properties {...}}
```

---

## Recurring Events

### How Recurring Events Work

1. **Event stored in Alfresco** with RRULE (e.g., "Every Saturday at 6:30 PM")
2. **iCal4j parses** the .ics file and RRULE
3. **Calendar service expands** into individual occurrences within date range
4. **Each occurrence** becomes a separate event in the result list

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
