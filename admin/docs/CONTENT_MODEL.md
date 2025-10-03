# Alfresco Content Model Documentation

Complete reference for the Mount Zion CMS custom content model (`web:` aspects).

## Overview

The `web:` namespace contains custom aspects for managing website content in Alfresco. These aspects provide metadata for pages, components, and publishing workflow.

---

## Aspects

### `web:siteMeta` (Base Aspect)

Base aspect for site metadata. Typically applied with child aspects like `web:pageType`.

**Properties:**
- `web:menuItem` (boolean, default: false)
- `web:kind` (text, default: "page", mandatory)
- `web:menuLabel` (text, optional)
- `web:componentType` (text, default: "Plain HTML")

### `web:pageType` (extends `web:siteMeta`)

Defines the type of page or component.

**Properties:**

#### `web:menuItem`
- **Type:** boolean
- **Default:** false
- **Mandatory:** No
- **Description:** Show this page in the navigation menu?
- **Usage:** Only applies to pages (kind="page")

#### `web:kind`
- **Type:** text (constrained)
- **Default:** "page"
- **Mandatory:** Yes
- **Values:**
  - `page` - A full website page
  - `component` - A reusable component (hero, section, etc.)
- **Description:** Determines if this is a standalone page or a component

#### `web:menuLabel`
- **Type:** text
- **Default:** null
- **Mandatory:** No
- **Description:** Label to display in navigation menu
- **Usage:** Only used when menuItem=true

#### `web:pagetype`
- **Type:** text (constrained)
- **Default:** "Dynamic"
- **Mandatory:** Yes
- **Constraint Values:**
  - **`Static`** - Pre-generated static page (fast, no Alfresco calls)
  - **`Dynamic`** - Fetched from Alfresco at runtime (cached)
  - **`Listing`** - Page that lists other content (blog index, events list)
  - **`Gallery`** - Image gallery page
  - **`Form`** - Page containing a form (contact, registration)
- **Description:** How the page is rendered and served
- **Migration Note:** Replaces `web:generateStatic` aspect

#### `web:componentType`
- **Type:** text (constrained)
- **Default:** "Plain HTML"
- **Mandatory:** No (only applies when kind="component")
- **Constraint Values:**
  - **`Plain HTML`** - Simple HTML content
  - **`Hero`** - Hero banner component
  - **`Section`** - Content section
  - **`Feature`** - Feature card/block
  - **`Card`** - Card component
  - **`Blog Post`** - Blog post content
  - **`Gallery Item`** - Individual gallery item
  - **`List Item`** - List item component
  - **`Banner`** - Banner/alert component
  - **`Image`** - Image component
  - **`Video`** - Video component
- **Description:** The type of component for rendering
- **Usage:** Only used when kind="component"

### `web:publishable`

Publishing workflow metadata.

**Properties:**

#### `web:publishDate`
- **Type:** datetime
- **Multivalue:** Yes
- **Mandatory:** No
- **Description:** Dates for publish/unpublish events

#### `web:publishState`
- **Type:** text
- **Default:** "Draft"
- **Mandatory:** Yes
- **Constraint Values:**
  - `Draft` - Work in progress
  - `Ready to Publish` - Awaiting publish
  - `Published` - Live on website
  - `Archive` - No longer active
- **Description:** Current publishing state

#### `web:targetPath`
- **Type:** text
- **Mandatory:** No
- **Description:** Absolute repo path to mirrored destination in /API Published

### `web:generateStatic` (DEPRECATED)

**Status:** DEPRECATED - Use `web:pagetype="Static"` instead

**Migration:**
- Old: Apply `web:generateStatic` aspect
- New: Set `web:pagetype="Static"` in `web:pageType` aspect

---

## Usage Examples

### Static Page (About, Worship Info)

```javascript
// Alfresco node properties
{
  "aspectNames": ["web:pageType", "web:publishable"],
  "properties": {
    "cm:name": "about",
    "cm:title": "About Mount Zion UCC",
    "web:kind": "page",
    "web:pagetype": "Static",        // ← Pre-generated static content
    "web:menuItem": true,
    "web:menuLabel": "About",
    "web:publishState": "Published"
  }
}
```

**Result:**
- ✅ Served from `resources/content/static/about.edn`
- ✅ Fast (no Alfresco API call)
- ✅ Shows in navigation menu as "About"

### Dynamic Page (News, Events)

```javascript
{
  "aspectNames": ["web:pageType", "web:publishable"],
  "properties": {
    "cm:name": "events",
    "cm:title": "Upcoming Events",
    "web:kind": "page",
    "web:pagetype": "Dynamic",       // ← Fetched from Alfresco at runtime
    "web:menuItem": true,
    "web:menuLabel": "Events",
    "web:publishState": "Published"
  }
}
```

**Result:**
- ✅ Fetched from Alfresco when requested
- ✅ Cached for 1 hour
- ✅ Always fresh content

### Listing Page (Blog Index)

```javascript
{
  "aspectNames": ["web:pageType"],
  "properties": {
    "cm:name": "blog",
    "cm:title": "Church Blog",
    "web:kind": "page",
    "web:pagetype": "Listing",       // ← Lists child content
    "web:menuItem": true,
    "web:menuLabel": "Blog"
  }
}
```

**Result:**
- ✅ Queries Alfresco for child nodes
- ✅ Displays as list/grid
- ✅ Pagination support

### Hero Component

```javascript
{
  "aspectNames": ["web:pageType"],
  "properties": {
    "cm:name": "home-hero",
    "cm:title": "Welcome to Mount Zion",
    "web:kind": "component",         // ← This is a component
    "web:componentType": "Hero"      // ← Hero type
  }
}
```

**Result:**
- ✅ Used in page composition
- ✅ Cached via Pathom resolvers
- ✅ Renders with hero template

### Feature Component

```javascript
{
  "aspectNames": ["web:pageType"],
  "properties": {
    "cm:name": "worship-feature",
    "cm:title": "Join Us for Worship",
    "web:kind": "component",
    "web:componentType": "Feature"
  }
}
```

---

## Page Type Decision Matrix

| Page Type | Use When | Alfresco Call | Cache | Example |
|-----------|----------|---------------|-------|---------|
| **Static** | Content rarely changes | No (pre-generated) | Forever | About, Worship Info, Contact |
| **Dynamic** | Content updates regularly | Yes | 1 hour | News, Events, Home |
| **Listing** | Shows list of items | Yes (query) | 1 hour | Blog Index, Events List |
| **Gallery** | Image gallery | Yes | 1 hour | Photo Gallery, Media |
| **Form** | Contains form | No (form on page) | N/A | Contact, Registration |

## Component Type Decision Matrix

| Component Type | Use For | Example |
|----------------|---------|---------|
| **Plain HTML** | Simple text content | Text blocks, paragraphs |
| **Hero** | Large banner at top | Homepage hero, page headers |
| **Section** | Content sections | About sections, info blocks |
| **Feature** | Feature highlights | 3-column features, service highlights |
| **Card** | Card-style content | Team members, ministries |
| **Blog Post** | Blog content | Blog posts, news articles |
| **Gallery Item** | Individual gallery images | Photo items in gallery |
| **List Item** | List entries | Event listings, resource lists |
| **Banner** | Alert/announcement | Service times, announcements |
| **Image** | Standalone images | Featured images, photos |
| **Video** | Video embeds | YouTube, Vimeo, uploaded videos |

---

## Migration Guide

### From `web:generateStatic` to `web:pagetype`

**Old approach:**
```javascript
// Apply web:generateStatic aspect to mark as static
{
  "aspectNames": ["web:generateStatic", "web:pageType"],
  "properties": {
    "web:kind": "page"
  }
}
```

**New approach:**
```javascript
// Use web:pagetype property instead
{
  "aspectNames": ["web:pageType"],
  "properties": {
    "web:kind": "page",
    "web:pagetype": "Static"  // ← Use this instead
  }
}
```

**Update sync script:**
```clojure
;; OLD: admin/scripts/Babashka/sync-content.clj
(defn fetch-static-pages [ctx]
  ;; Query for nodes with web:generateStatic aspect
  (alfresco/search ctx "ASPECT:'web:generateStatic'"))

;; NEW: Update query
(defn fetch-static-pages [ctx]
  ;; Query for nodes with web:pagetype="Static"
  (alfresco/search ctx "web:pagetype:'Static'"))
```

---

## Pathom Resolver Integration

### Query by Page Type

```clojure
;; Get all static pages
(pathom/query {} [{:pages/by-type {:type "Static"}}
                  [:page/title :page/slug]])

;; Get all gallery pages
(pathom/query {} [{:pages/by-type {:type "Gallery"}}
                  [:page/title :page/content]])

;; Get all hero components
(pathom/query {} [{:components/by-type {:type "Hero"}}
                  [:component/title :component/image]])
```

### Resolver Examples

```clojure
;; src/mtz_cms/pathom/resolvers.clj

(pc/defresolver pages-by-type-resolver [env {:keys [type]}]
  {::pc/input #{:type}
   ::pc/output [{:pages/by-type [:page/title :page/slug :page/node-id]}]}
  (let [ctx (:ctx env)
        query (str "web:pagetype:'" type "' AND web:kind:'page'")
        results (alfresco/search ctx query)]
    {:pages/by-type (map format-page-data results)}))

(pc/defresolver components-by-type-resolver [env {:keys [type]}]
  {::pc/input #{:type}
   ::pc/output [{:components/by-type [:component/title :component/node-id]}]}
  (let [ctx (:ctx env)
        query (str "web:componentType:'" type "' AND web:kind:'component'")
        results (alfresco/search ctx query)]
    {:components/by-type (map format-component-data results)}))
```

---

## API Queries

### Search for Static Pages

```bash
# Using Alfresco Search API
curl -X POST "http://localhost:8080/alfresco/api/-default-/public/search/versions/1/search" \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "query": "web:pagetype:\"Static\" AND web:kind:\"page\""
    }
  }'
```

### Search for Hero Components

```bash
curl -X POST "http://localhost:8080/alfresco/api/-default-/public/search/versions/1/search" \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "query": "web:componentType:\"Hero\" AND web:kind:\"component\""
    }
  }'
```

### Get Node with Aspects

```bash
curl "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}?include=properties,aspectNames" \
  -u admin:admin
```

---

## Content Editor Workflows

### Creating a Static Page

1. **Create folder in Alfresco**
   - Name: `contact`
   - Type: Folder

2. **Add aspects**
   - Apply `web:pageType`
   - Apply `web:publishable` (optional)

3. **Set properties**
   - `web:kind` = "page"
   - `web:pagetype` = "Static"
   - `web:menuItem` = true
   - `web:menuLabel` = "Contact Us"
   - `web:publishState` = "Published"

4. **Add content**
   - Create HTML file in folder
   - Add images if needed

5. **Sync to static**
   ```bash
   admin/scripts/Babashka/sync-content.clj
   ```

6. **Deploy**
   ```bash
   ./deploy.sh
   ```

### Creating a Hero Component

1. **Create folder**
   - Name: `home-hero`

2. **Add aspect**
   - Apply `web:pageType`

3. **Set properties**
   - `web:kind` = "component"
   - `web:componentType` = "Hero"

4. **Add content**
   - Create HTML content
   - Add hero image

5. **Reference in page config**
   ```clojure
   {:hero/node-id "abc-123-node-id"}
   ```

---

## REPL Commands

```clojure
;; Load in REPL
(load-file "admin/scripts/Repl/admin.clj")

;; Find all static pages
(require '[mtz-cms.alfresco.client :as alfresco])
(alfresco/search {} "web:pagetype:'Static' AND web:kind:'page'")

;; Find all hero components
(alfresco/search {} "web:componentType:'Hero' AND web:kind:'component'")

;; Get page properties
(alfresco/get-node {} "node-id-here")

;; Query via Pathom
(require '[mtz-cms.pathom.resolvers :as pathom])
(pathom/query {} [{:pages/by-type {:type "Static"}}
                  [:page/title :page/slug]])
```

---

## Validation Rules

### Pages Must Have:
- ✅ `web:kind` = "page"
- ✅ `web:pagetype` (one of: Static, Dynamic, Listing, Gallery, Form)
- ✅ `cm:name` (slug-friendly)
- ✅ `cm:title`

### Components Must Have:
- ✅ `web:kind` = "component"
- ✅ `web:componentType` (one of 11 types)
- ✅ `cm:name`
- ✅ Content (HTML or image)

### Menu Items Must Have:
- ✅ `web:menuItem` = true
- ✅ `web:menuLabel` (text)
- ✅ `web:kind` = "page" (only pages can be menu items)

---

## See Also

- [STATIC_CONTENT_GUIDE.md](STATIC_CONTENT_GUIDE.md) - Static content workflow
- [CACHE_MANAGEMENT.md](CACHE_MANAGEMENT.md) - Cache configuration
- [../scripts/README.md](../scripts/README.md) - Admin scripts
