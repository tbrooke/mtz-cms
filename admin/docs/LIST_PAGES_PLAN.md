# List Pages Implementation Plan

**Date:** 2025-10-03
**Status:** Planning Phase

## Overview

Implement three dynamic list-based pages with clickable items:
1. **Events** - Calendar events (from ICS files)
2. **Blog** - Blog posts (HTML content)
3. **Sunday Worship** - Worship bulletins/recordings

## Requirements Summary

### 1. Events Page (`/events`)
- **Data Source:** Alfresco Calendar folder with ICS files
- **Layout:** Full-page width vertical list
- **Item Design:** Simple horizontal line top/bottom per item
- **Click Behavior:** Click anywhere in div → detail page
- **Publishing:** Use aspects for metadata (publish status, date, etc.)
- **Navigation:** Under top-level "Events" menu item

### 2. Blog Page (`/blog`)
- **Data Source:** Separate folder at top site level (outside Web Site structure)
- **Content Type:** Alfresco HTML documents
- **Layout:** Full-page width vertical list
- **Item Design:** Simple horizontal line top/bottom per item
- **Click Behavior:** Click anywhere in div → full blog post
- **Publishing:** Aspect-based publish mechanism
- **Navigation:** Top-level menu item

### 3. Sunday Worship Page (`/worship/sunday`)
- **Data Source:** New folder structure
- **Content Items:** Bulletin + optional media (recording/stream link)
- **Layout:** Full-page width vertical list
- **Item Design:** Simple horizontal line top/bottom per item
- **Click Behavior:** Click anywhere in div → worship detail page
- **Navigation:** Under "Worship" menu item

## Common Patterns

All three pages share similar architecture:

```
List Page (Full Width)
├── Item 1 [Clickable Div]
│   ├── Title
│   ├── Date/Metadata
│   ├── Short Description/Excerpt
│   └── [Horizontal lines top/bottom]
├── Item 2 [Clickable Div]
│   └── ...
└── Item N [Clickable Div]
```

## Architecture Plan

### Phase 1: Alfresco Content Model Extensions

**Create/Update Aspects:**

```xml
<!-- Event Aspect (calendar events) -->
<aspect name="cms:event">
  <title>Event</title>
  <properties>
    <property name="cms:eventDate">
      <type>d:datetime</type>
      <mandatory>true</mandatory>
    </property>
    <property name="cms:eventEndDate">
      <type>d:datetime</type>
      <mandatory>false</mandatory>
    </property>
    <property name="cms:eventLocation">
      <type>d:text</type>
    </property>
    <property name="cms:eventStatus">
      <type>d:text</type>
      <constraints>
        <constraint type="LIST">
          <parameter name="allowedValues">
            <list>
              <value>draft</value>
              <value>published</value>
              <value>cancelled</value>
            </list>
          </parameter>
        </constraint>
      </constraints>
    </property>
  </properties>
</aspect>

<!-- Blog Post Aspect -->
<aspect name="cms:blogPost">
  <title>Blog Post</title>
  <properties>
    <property name="cms:publishDate">
      <type>d:datetime</type>
    </property>
    <property name="cms:author">
      <type>d:text</type>
    </property>
    <property name="cms:excerpt">
      <type>d:text</type>
    </property>
    <property name="cms:tags">
      <type>d:text</type>
      <multiple>true</multiple>
    </property>
  </properties>
</aspect>

<!-- Worship Bulletin Aspect -->
<aspect name="cms:worshipBulletin">
  <title>Worship Bulletin</title>
  <properties>
    <property name="cms:worshipDate">
      <type>d:datetime</type>
      <mandatory>true</mandatory>
    </property>
    <property name="cms:sermon">
      <type>d:text</type>
    </property>
    <property name="cms:bulletinPDF">
      <type>d:noderef</type>
    </property>
    <property name="cms:recordingLink">
      <type>d:text</type>
    </property>
  </properties>
</aspect>
```

**Update Alfresco Model:** `src/mtz_cms/mtz.xml`

### Phase 2: ICS Calendar Parser

**Create:** `src/mtz_cms/alfresco/ics_parser.clj`

```clojure
(ns mtz-cms.alfresco.ics-parser
  "Parse ICS calendar files from Alfresco"
  (:require
   [clojure.string :as str]
   [clojure.instant :as instant]))

(defn parse-ics-content
  "Parse ICS file content to event data"
  [ics-string]
  ;; Parse VEVENT blocks
  ;; Extract: SUMMARY, DTSTART, DTEND, DESCRIPTION, LOCATION
  ;; Return: {:title ... :start-date ... :end-date ... :description ... :location ...}
  )

(defn extract-events-from-folder
  "Get all ICS files from calendar folder and parse"
  [ctx folder-node-id]
  ;; Fetch all .ics files
  ;; Parse each one
  ;; Filter by publish status (if aspect exists)
  ;; Sort by date
  ;; Return list of events
  )
```

**Dependencies:** Check if we need `clj-time` or use Java time

### Phase 3: Pathom List Resolvers

**Create:** `src/mtz_cms/alfresco/list_resolvers.clj`

```clojure
(ns mtz-cms.alfresco.list-resolvers
  "Pathom resolvers for list-based content"
  (:require
   [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.alfresco.ics-parser :as ics]
   [mtz-cms.config.core :as config]))

(defresolver events-list
  "Get published events from calendar folder"
  [ctx input]
  {::pco/output [:events/list]}
  (let [calendar-folder-id (config/get-node-id :calendar)
        events (ics/extract-events-from-folder ctx calendar-folder-id)
        published (filter #(= "published" (:status %)) events)]
    {:events/list (sort-by :start-date > published)}))

(defresolver blog-posts-list
  "Get published blog posts"
  [ctx input]
  {::pco/output [:blog/list]}
  (let [blog-folder-id (config/get-node-id :blog)
        children-result (alfresco/get-node-children ctx blog-folder-id)
        posts (get-in children-result [:data :list :entries])
        ;; Filter by cms:blogPost aspect + published status
        published (filter-published-posts posts)]
    {:blog/list (sort-by :publish-date > published)}))

(defresolver worship-bulletins-list
  "Get Sunday worship bulletins"
  [ctx input]
  {::pco/output [:worship/list]}
  (let [worship-folder-id (config/get-node-id :worship-sunday)
        children-result (alfresco/get-node-children ctx worship-folder-id)
        bulletins (get-in children-result [:data :list :entries])
        ;; Filter by cms:worshipBulletin aspect
        published (filter-published-bulletins bulletins)]
    {:worship/list (sort-by :worship-date > published)}))
```

### Phase 4: List Components

**Create:** `src/mtz_cms/components/list_item.clj`

```clojure
(ns mtz-cms.components.list-item
  "Clickable list item component"
  (:require
   [clojure.string :as str]))

(defn list-item
  "Generic clickable list item with horizontal borders

   Data structure:
   {:title \"Event Title\"
    :date \"2025-10-15\"
    :description \"Short description...\"
    :link \"/events/detail/123\"
    :metadata {:location \"Church Hall\" :author \"John Doe\"}}

   Renders full-width clickable div with borders."
  [item-data]
  [:a {:href (:link item-data)
       :class "block border-t border-b border-gray-200 py-6 hover:bg-gray-50 transition-colors"}
   [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"}
    ;; Title
    [:h3 {:class "text-xl font-semibold text-gray-900 mb-2"}
     (:title item-data)]

    ;; Date/Metadata row
    [:div {:class "flex items-center text-sm text-gray-500 mb-3"}
     [:time {:datetime (:date item-data)}
      (:formatted-date item-data)]

     (when-let [metadata (:metadata item-data)]
       (for [[key val] metadata]
         [:span {:key key :class "ml-4"}
          [:span {:class "text-gray-400"} "•"]
          " " val]))]

    ;; Description/Excerpt
    (when (:description item-data)
      [:p {:class "text-gray-600"}
       (:description item-data)])]])

(defn event-item
  "Event-specific list item"
  [event-data]
  (list-item
   {:title (:title event-data)
    :date (:start-date event-data)
    :formatted-date (format-event-date event-data)
    :description (:description event-data)
    :link (str "/events/" (:id event-data))
    :metadata {:location (:location event-data)}}))

(defn blog-item
  "Blog post list item"
  [post-data]
  (list-item
   {:title (:title post-data)
    :date (:publish-date post-data)
    :formatted-date (format-blog-date post-data)
    :description (:excerpt post-data)
    :link (str "/blog/" (:slug post-data))
    :metadata {:author (:author post-data)}}))

(defn worship-item
  "Worship bulletin list item"
  [bulletin-data]
  (list-item
   {:title (str "Sunday Worship - " (:formatted-date bulletin-data))
    :date (:worship-date bulletin-data)
    :formatted-date (format-worship-date bulletin-data)
    :description (:sermon bulletin-data)
    :link (str "/worship/sunday/" (:id bulletin-data))
    :metadata (when (:has-recording bulletin-data)
                {:recording "Recording Available"})}))
```

### Phase 5: List Layouts

**Create:** `src/mtz_cms/ui/list_layouts.clj`

```clojure
(ns mtz-cms.ui.list-layouts
  "List page layouts"
  (:require
   [mtz-cms.components.list-item :as list-item]))

(defn full-width-list-layout
  "Full-width list layout with items

   Data structure:
   {:page/title \"Events\"
    :page/description \"Upcoming events at Mount Zion\"
    :list/items [...] ;; vector of item data
    :list/item-component :event | :blog | :worship}"
  [page-data]
  (let [items (:list/items page-data)
        item-component (case (:list/item-component page-data)
                        :event list-item/event-item
                        :blog list-item/blog-item
                        :worship list-item/worship-item
                        list-item/list-item)]
    [:div {:class "bg-white"}
     ;; Page header
     [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"}
      [:h1 {:class "text-3xl font-bold text-gray-900 mb-2"}
       (:page/title page-data)]
      (when (:page/description page-data)
        [:p {:class "text-lg text-gray-600"}
         (:page/description page-data)])]

     ;; List items (no container - full width with borders)
     [:div {:class "divide-y divide-gray-200"}
      (for [item items]
        [:div {:key (:id item)}
         (item-component item)])]

     ;; Empty state
     (when (empty? items)
       [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center"}
        [:p {:class "text-gray-500"}
         "No items to display."]])]))
```

### Phase 6: Detail Pages

**Create:** `src/mtz_cms/ui/detail_pages.clj`

```clojure
(ns mtz-cms.ui.detail-pages
  "Detail pages for list items"
  (:require
   [mtz-cms.ui.base :as base]))

(defn event-detail-page
  "Event detail page"
  [event-data ctx]
  (base/base-page
   (:title event-data)
   [:div {:class "max-w-4xl mx-auto"}
    [:h1 {:class "text-4xl font-bold text-gray-900 mb-4"}
     (:title event-data)]

    [:div {:class "flex items-center text-gray-600 mb-6"}
     [:time {:datetime (:start-date event-data)}
      (:formatted-date event-data)]
     (when (:location event-data)
       [:span {:class "ml-4"}
        "📍 " (:location event-data)])]

    [:div {:class "prose max-w-none"}
     (:description event-data)]]
   ctx))

(defn blog-post-page
  "Blog post detail page"
  [post-data ctx]
  (base/base-page
   (:title post-data)
   [:article {:class "max-w-4xl mx-auto"}
    [:h1 {:class "text-4xl font-bold text-gray-900 mb-4"}
     (:title post-data)]

    [:div {:class "text-gray-600 mb-6"}
     [:time {:datetime (:publish-date post-data)}
      (:formatted-date post-data)]
     " by " (:author post-data)]

    [:div {:class "prose prose-lg max-w-none"}
     (:content post-data)]]
   ctx))

(defn worship-bulletin-page
  "Worship bulletin detail page"
  [bulletin-data ctx]
  (base/base-page
   (str "Sunday Worship - " (:formatted-date bulletin-data))
   [:div {:class "max-w-4xl mx-auto"}
    [:h1 {:class "text-3xl font-bold text-gray-900 mb-4"}
     "Sunday Worship"]
    [:p {:class "text-xl text-gray-600 mb-6"}
     (:formatted-date bulletin-data)]

    ;; Bulletin PDF download
    (when (:bulletin-pdf bulletin-data)
      [:div {:class "mb-6"}
       [:a {:href (:bulletin-pdf bulletin-data)
            :class "inline-flex items-center bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700"}
        "📄 Download Bulletin (PDF)"]])

    ;; Recording link
    (when (:recording-link bulletin-data)
      [:div {:class "mb-6"}
       [:a {:href (:recording-link bulletin-data)
            :class "inline-flex items-center bg-green-600 text-white px-6 py-3 rounded-md hover:bg-green-700"}
        "🎥 Watch Recording"]])

    ;; Sermon/Content
    [:div {:class "prose max-w-none mt-8"}
     [:h2 "Sermon"]
     (:sermon-content bulletin-data)]]
   ctx))
```

### Phase 7: Routes

**Update:** `src/mtz_cms/routes/main.clj`

```clojure
;; List page routes
["/events" {:get (fn [request]
                   (let [ctx {}
                         result (pathom/query ctx [:events/list])
                         events (:events/list result)]
                     (html-response
                      (pages/events-list-page events ctx))))}]

["/events/:event-id" {:get (fn [request]
                             (let [ctx {}
                                   event-id (get-in request [:path-params :event-id])
                                   result (pathom/query ctx [{[:event/id event-id] [:event/detail]}])
                                   event (get result [:event/id event-id])]
                               (html-response
                                (pages/event-detail-page event ctx))))}]

["/blog" {:get (fn [request]
                 (let [ctx {}
                       result (pathom/query ctx [:blog/list])
                       posts (:blog/list result)]
                   (html-response
                    (pages/blog-list-page posts ctx))))}]

["/blog/:slug" {:get (fn [request]
                       (let [ctx {}
                             slug (get-in request [:path-params :slug])
                             result (pathom/query ctx [{[:blog/slug slug] [:blog/detail]}])
                             post (get result [:blog/slug slug])]
                         (html-response
                          (pages/blog-post-page post ctx))))}]

["/worship/sunday" {:get (fn [request]
                           (let [ctx {}
                                 result (pathom/query ctx [:worship/list])
                                 bulletins (:worship/list result)]
                             (html-response
                              (pages/worship-list-page bulletins ctx))))}]

["/worship/sunday/:bulletin-id" {:get (fn [request]
                                        (let [ctx {}
                                              bulletin-id (get-in request [:path-params :bulletin-id])
                                              result (pathom/query ctx [{[:worship/id bulletin-id] [:worship/detail]}])
                                              bulletin (get result [:worship/id bulletin-id])]
                                          (html-response
                                           (pages/worship-bulletin-page bulletin ctx))))}]
```

### Phase 8: Configuration Updates

**Update:** `src/mtz_cms/config/core.clj`

Add node IDs for new folders:

```clojure
(def node-mapping
  {;; Existing
   :home "..."
   :about "..."

   ;; New list folders
   :calendar "xxx-calendar-folder-id"      ;; Calendar events (ICS files)
   :blog "xxx-blog-folder-id"              ;; Blog posts (outside Web Site)
   :worship-sunday "xxx-worship-folder-id" ;; Sunday worship bulletins
   })
```

### Phase 9: Malli Schemas

**Update:** `src/mtz_cms/validation/schemas.clj`

```clojure
;; Event schemas
(def event-list-item-schema
  [:map
   [:id :string]
   [:title :string]
   [:start-date :string]
   [:end-date {:optional true} :string]
   [:description {:optional true} :string]
   [:location {:optional true} :string]
   [:status [:enum "draft" "published" "cancelled"]]])

(def events-list-schema
  [:sequential event-list-item-schema])

;; Blog schemas
(def blog-post-list-item-schema
  [:map
   [:id :string]
   [:title :string]
   [:slug :string]
   [:excerpt {:optional true} :string]
   [:author {:optional true} :string]
   [:publish-date :string]
   [:tags {:optional true} [:sequential :string]]])

(def blog-list-schema
  [:sequential blog-post-list-item-schema])

;; Worship schemas
(def worship-bulletin-list-item-schema
  [:map
   [:id :string]
   [:worship-date :string]
   [:sermon {:optional true} :string]
   [:bulletin-pdf {:optional true} :string]
   [:recording-link {:optional true} :string]
   [:has-recording :boolean]])

(def worship-list-schema
  [:sequential worship-bulletin-list-item-schema])
```

## Implementation Phases

### Phase 1: Foundation (Week 1)
- [ ] Update Alfresco content model (mtz.xml) with new aspects
- [ ] Deploy model to Alfresco
- [ ] Add node IDs to config
- [ ] Test aspect application in Alfresco

### Phase 2: ICS Parser & Calendar (Week 1-2)
- [ ] Create ICS parser (`alfresco/ics_parser.clj`)
- [ ] Create calendar list resolver
- [ ] Test with sample ICS files
- [ ] Create event list component
- [ ] Create event detail page
- [ ] Add routes
- [ ] Test end-to-end

### Phase 3: Blog System (Week 2)
- [ ] Create blog list resolver
- [ ] Create blog list component
- [ ] Create blog detail page
- [ ] Add routes
- [ ] Test publishing workflow
- [ ] Test end-to-end

### Phase 4: Worship Bulletins (Week 2-3)
- [ ] Create worship list resolver
- [ ] Create worship list component
- [ ] Create worship detail page with PDF/recording links
- [ ] Add routes
- [ ] Test end-to-end

### Phase 5: Polish & Validation (Week 3)
- [ ] Add Malli validation to all list resolvers
- [ ] Add data transformations
- [ ] Add caching (1 hour for lists)
- [ ] Test all three systems
- [ ] Update documentation

## Navigation Updates

**Add to navigation menu (navigation/menu.clj):**

```clojure
;; Top-level items
{:key :events
 :label "Events"
 :path "/events"
 :has-children? false}

{:key :blog
 :label "Blog"
 :path "/blog"
 :has-children? false}

{:key :worship
 :label "Worship"
 :path "/worship"
 :has-children? true
 :submenu [{:label "Sunday Worship" :path "/worship/sunday"}
           {:label "Service Times" :path "/worship/times"}]}
```

## Data Flow

```
┌─────────────────────────────────────────────────────┐
│ Alfresco Folders                                    │
│                                                     │
│ /Calendar (ICS files + cms:event aspect)          │
│ /Blog (HTML + cms:blogPost aspect)                │
│ /Worship/Sunday (Content + cms:worshipBulletin)   │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│ List Resolvers (Pathom)                            │
│                                                     │
│ • Filter by publish status (aspect property)       │
│ • Sort by date (descending)                        │
│ • Parse ICS files (for calendar)                   │
│ • Extract metadata (title, date, excerpt)          │
│ • Validate with Malli                              │
│ • Transform data (trim, clean)                     │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│ List Components                                     │
│                                                     │
│ • Full-width clickable divs                        │
│ • Horizontal borders (top/bottom)                  │
│ • Title + Date + Metadata + Excerpt                │
│ • Hover effects                                     │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│ Detail Pages                                        │
│                                                     │
│ • Full content display                             │
│ • PDF download (bulletins)                         │
│ • Recording links (worship)                        │
│ • Formatted content                                │
└─────────────────────────────────────────────────────┘
```

## Publishing Workflow

**For content editors in Alfresco:**

1. **Create Content:**
   - Upload ICS file / Create HTML / Create bulletin folder

2. **Apply Aspect:**
   - Add `cms:event` / `cms:blogPost` / `cms:worshipBulletin`

3. **Set Properties:**
   - Status: "draft" or "published"
   - Date, author, metadata

4. **Publish:**
   - Change status to "published"
   - Content appears on website (cache updates within 1 hour)

## Testing Strategy

1. **Unit Tests:**
   - ICS parser with sample files
   - Malli schema validation
   - Component rendering

2. **Integration Tests:**
   - Pathom resolver queries
   - Full page rendering
   - Navigation menu updates

3. **Manual Tests:**
   - Publishing workflow in Alfresco
   - Click behavior on list items
   - Responsive design (mobile/desktop)
   - PDF downloads
   - Recording links

## Open Questions

1. **ICS Library:** Do we have a Clojure ICS parser or build our own?
2. **Date Formatting:** What format for dates? (e.g., "October 15, 2025" vs "Oct 15")
3. **Pagination:** For lists with many items, do we need pagination?
4. **Search:** Do we need search/filter for these lists?
5. **RSS Feeds:** Should blog have an RSS feed?
6. **Recording Storage:** Where are worship recordings stored? YouTube? Vimeo? Self-hosted?

## Success Criteria

- [ ] Events page displays published calendar events
- [ ] Blog page displays published posts
- [ ] Worship page displays bulletins with downloads
- [ ] All items are clickable (entire div)
- [ ] Detail pages show full content
- [ ] Publishing workflow works in Alfresco
- [ ] Validation prevents bad data
- [ ] Mobile responsive
- [ ] Performance: lists load < 500ms
- [ ] Cache prevents excessive Alfresco calls

## Next Steps

1. Review this plan with user
2. Get answers to open questions
3. Create Alfresco folder structure
4. Begin Phase 1 implementation
