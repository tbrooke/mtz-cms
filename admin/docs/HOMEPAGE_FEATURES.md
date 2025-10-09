# Homepage Feature Cards System

**Status**: ✅ **COMPLETE - Production Ready**
**Date**: October 9, 2025

## Overview

The homepage displays three featured content items as tall, clickable cards that link to individual detail pages. This system provides a modern, card-based UI for highlighting important content on the Mount Zion UCC homepage.

## Architecture

### Component Stack

```
Homepage
  └─ Hero Section (dynamic HTMX)
  └─ Feature Cards Grid (3-column layout)
      ├─ Feature Card 1 → /features/feature1
      ├─ Feature Card 2 → /features/feature2
      └─ Feature Card 3 → /features/feature3
```

### Data Flow

```
Browser Request: /
         ↓
home-handler
         ↓
htmx-hero-features-layout
         ↓
htmx-features-grid (3 cards with HTMX loading)
         ↓
HTMX triggers: GET /api/features/card/:node-id
         ↓
feature-card-handler
         ↓
Pathom Query: [:feature/title :feature/content :feature/image]
         ↓
Fetch from Alfresco (Home Page/Feature X folders)
         ↓
Extract description (first 150 chars from content)
         ↓
Render feature-card component
         ↓
HTML Response
```

```
User Clicks Card → /features/feature1
         ↓
feature-detail-handler
         ↓
Pathom Query: Feature data by node-id
         ↓
Render feature-detail-page
         ↓
Full page with hero image, title, and complete content
```

## Implementation Details

### 1. Feature Cards Component

**File**: `src/mtz_cms/components/home_features.clj`

**Key Functions**:
- `feature-card` - Renders a single tall card (600px height)
- `placeholder-feature-card` - Placeholder for empty features
- `features-grid` - 3-column grid layout
- `htmx-feature-card-container` - HTMX loading container
- `htmx-features-grid` - HTMX-powered grid

**Card Design**:
- **Dimensions**: ~300px wide × 600px tall (portrait orientation)
- **Image Section**: 320px tall at top
- **Content Section**: ~280px with title, description, and CTA
- **Hover Effects**: Shadow and border color change, arrow animation
- **Entire card is clickable** - Links to detail page

### 2. Homepage Layout

**File**: `src/mtz_cms/components/htmx.clj`

**Function**: `htmx-hero-features-layout`

**Layout Structure**:
```clojure
[:div
 ;; Hero section (dynamic HTMX)
 (htmx-hero-container hero-node-id)

 ;; Feature cards (NEW - 3-column grid)
 (htmx-features-grid
  [{:node-id "264ab06c-..." :slug "feature1"}
   {:node-id "fe3c64bf-..." :slug "feature2"}
   {:node-id "6737d1b1-..." :slug "feature3"}])

 ;; Call to action section
 ...]
```

### 3. Feature Detail Pages

**File**: `src/mtz_cms/ui/pages.clj`

**Function**: `feature-detail-page`

**Page Structure**:
- Breadcrumb navigation (Home → Feature Title)
- Large hero image (if available) - 384px tall
- Title (4xl/5xl font)
- Full HTML content (prose styling)
- Back to Home link

### 4. Routes

**File**: `src/mtz_cms/routes/main.clj`

**Routes Added**:
```clojure
;; Feature detail pages
["/features/:slug" {:get feature-detail-handler}]
```

**Handler**: `feature-detail-handler`
- Maps slug to node-id (feature1/feature2/feature3)
- Fetches content from Alfresco via Pathom
- Renders detail page template

**Slug to Node-ID Mapping**:
```clojure
{"feature1" "264ab06c-984e-4f64-8ab0-6c984eaf6440"
 "feature2" "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89"
 "feature3" "6737d1b1-5465-4625-b7d1-b15465b62530"}
```

### 5. API Endpoints

**File**: `src/mtz_cms/routes/api.clj`

**Endpoint**: `GET /api/features/card/:node-id`

**Handler**: `feature-card-handler`

**Process**:
1. Fetch feature data from Pathom (`:feature/title`, `:feature/content`, `:feature/image`)
2. Extract description from content (first 150 characters, HTML stripped)
3. Map node-id to slug for detail page link
4. Render `feature-card` component
5. Return HTML fragment for HTMX

**Response**:
```html
<a href="/features/feature1" class="block group">
  <div class="bg-white rounded-lg shadow-md..." style="height: 600px;">
    <!-- Image section -->
    <div class="h-80 bg-gradient-to-br...">
      <img src="/proxy/image/..." />
    </div>
    <!-- Content section -->
    <div class="p-6 flex flex-col flex-grow">
      <h3>Feature Title</h3>
      <p>Description (150 chars)...</p>
      <div class="flex items-center...">Learn more →</div>
    </div>
  </div>
</a>
```

## Content Structure in Alfresco

### Home Page Features

```
Home Page (9faac48b-6c77-4266-aac4-8b6c7752668a)
├── Hero/ (39985c5c-201a-42f6-985c-5c201a62f6d8)
├── Feature 1/ (264ab06c-984e-4f64-8ab0-6c984eaf6440)
│   ├── cm:title - "Feature Title"
│   ├── cm:content - HTML content (text + images)
│   └── Children: Image nodes (optional)
├── Feature 2/ (fe3c64bf-bb1b-456f-bc64-bfbb1b656f89)
│   ├── cm:title - "Feature Title"
│   ├── cm:content - HTML content
│   └── Children: Image nodes
└── Feature 3/ (6737d1b1-5465-4625-b7d1-b15465b62530)
    └── (Currently empty)
```

### Feature Data Model

```clojure
{:feature/id "feature1"                     ; Slug for URL
 :feature/title "Welcome to Mount Zion"     ; From cm:title
 :feature/description "Join us for..."      ; Extracted from content (150 chars)
 :feature/content "<div>...</div>"          ; Full HTML from cm:content
 :feature/image {:url "/proxy/image/123"    ; First child image (optional)
                 :alt "Image description"}
 :feature/link "/features/feature1"}        ; Link to detail page
```

## Design Specifications

### Feature Card (Homepage)

```
┌─────────────────────────────────┐
│                                 │
│      IMAGE (320px tall)         │ ← If available
│                                 │
├─────────────────────────────────┤
│                                 │
│  Feature Title                  │ ← Bold, large
│                                 │
│  Description text here that     │
│  gives preview of the content.  │ ← 150 chars max
│  This helps users decide if...  │
│                                 │
│                                 │
│  Learn more →                   │ ← Bottom-aligned CTA
└─────────────────────────────────┘
     300px wide × 600px tall
```

**Styling**:
- Border: 2px transparent → blue-500 on hover
- Shadow: md → xl on hover
- Image: Scale 1.0 → 1.05 on hover
- Arrow: Translates right on hover
- Title: Gray-900 → Blue-600 on hover

### Feature Detail Page

```
┌───────────────────────────────────────────┐
│ Home → Feature Title                      │ ← Breadcrumb
├───────────────────────────────────────────┤
│                                           │
│        HERO IMAGE (384px tall)            │ ← If available
│                                           │
├───────────────────────────────────────────┤
│                                           │
│  Feature Title (Large Heading)            │
│                                           │
│  [Full HTML Content Here]                 │
│  - Rich text formatting                   │
│  - Images processed via proxy             │
│  - Links preserved                        │
│  - Full content display                   │
│                                           │
│  ← Back to Home                           │
└───────────────────────────────────────────┘
```

## How to Update Feature Content

### Option 1: Edit in Alfresco

1. Navigate to: **Sites/swsdp/documentLibrary/Web Site/Home Page/**
2. Open **Feature 1**, **Feature 2**, or **Feature 3** folder
3. Edit properties:
   - `cm:title` - The feature title (shown on card and detail page)
   - `cm:content` - The HTML content (first 150 chars on card, full content on detail page)
4. Add images as child nodes (optional)
5. Save changes
6. Refresh website - changes appear within 1 hour (or force cache clear)

### Option 2: Direct Content Update

```clojure
;; In REPL
(require '[mtz-cms.alfresco.client :as alfresco])

;; Update feature title
(alfresco/update-node
  {}
  "264ab06c-984e-4f64-8ab0-6c984eaf6440"  ; Feature 1 node-id
  {:cm:title "New Feature Title"})

;; Update content
(alfresco/update-node-content
  {}
  "264ab06c-984e-4f64-8ab0-6c984eaf6440"
  "<div><h2>New Content</h2><p>Feature details...</p></div>")
```

## Testing

### Test Homepage Cards

**URL**: http://localhost:3000

**Expected**:
- ✅ Three tall cards in grid (3 columns on desktop)
- ✅ Cards load dynamically via HTMX
- ✅ Images display at top of cards (when available)
- ✅ Titles and descriptions shown
- ✅ Hover effects: shadow, border, arrow animation
- ✅ Entire card is clickable
- ✅ Responsive (1 col mobile, 2 tablet, 3 desktop)

### Test Feature Detail Pages

**URLs**:
- http://localhost:3000/features/feature1
- http://localhost:3000/features/feature2
- http://localhost:3000/features/feature3

**Expected**:
- ✅ Breadcrumb navigation shows
- ✅ Hero image displays (if available)
- ✅ Title renders correctly
- ✅ Full HTML content displays
- ✅ Images in content load via proxy
- ✅ "Back to Home" link works

### REPL Testing

```clojure
;; Start server
(require 'user)
(user/start)

;; Test feature card API endpoint
(require '[mtz-cms.routes.api :as api])
(api/feature-card-handler
  {:path-params {:node-id "264ab06c-984e-4f64-8ab0-6c984eaf6440"}})
;; => {:status 200 :headers {...} :body "<a href=\"/features/feature1\"..."}

;; Test feature detail handler
(require '[mtz-cms.routes.main :as routes])
(routes/feature-detail-handler
  {:path-params {:slug "feature1"}})
;; => Full page HTML

;; Test Pathom feature resolver
(require '[mtz-cms.pathom.resolvers :as pathom])
(pathom/query {} [{[:feature/node-id "264ab06c-984e-4f64-8ab0-6c984eaf6440"]
                   [:feature/title :feature/content :feature/image]}])
;; => {:feature/title "..." :feature/content "<div>..." :feature/image {...}}
```

## Files Created/Modified

### Created
- ✅ `src/mtz_cms/components/home_features.clj` - Feature card components
- ✅ `admin/docs/HOMEPAGE_FEATURES.md` - This documentation

### Modified
- ✅ `src/mtz_cms/components/htmx.clj` - Updated layout to use card grid
- ✅ `src/mtz_cms/ui/pages.clj` - Added `feature-detail-page` template
- ✅ `src/mtz_cms/routes/main.clj` - Added feature detail handler and route
- ✅ `src/mtz_cms/routes/api.clj` - Added feature card API endpoint

## Future Enhancements

### Phase 2: Content Management
- [ ] Admin interface to reorder features
- [ ] Drag-and-drop card arrangement
- [ ] Preview feature cards before publishing
- [ ] Feature card templates (different layouts)

### Phase 3: Advanced Features
- [ ] Support for more than 3 features (pagination/carousel)
- [ ] Featured content categories/tags
- [ ] Scheduled feature rotation
- [ ] Analytics tracking on card clicks
- [ ] A/B testing different card designs

### Phase 4: Alfresco Integration
- [ ] Use `web:componentType` aspect for feature type selection
- [ ] Add `web:featured` aspect to mark featured content
- [ ] Auto-discover featured content from any page
- [ ] Featured content priority/ordering in Alfresco

## Troubleshooting

### Cards Not Loading

1. **Check SSH tunnel**: Ensure Alfresco connection is active
   ```bash
   ps aux | grep "ssh.*trust"
   ssh -L 8080:localhost:8080 -N -f tmb@trust
   ```

2. **Check Pathom resolver**: Test feature data fetch
   ```clojure
   (pathom/query {} [{[:feature/node-id "264ab06c-..."] [:feature/title]}])
   ```

3. **Check API endpoint**: Test card rendering
   ```bash
   curl http://localhost:3000/api/features/card/264ab06c-984e-4f64-8ab0-6c984eaf6440
   ```

4. **Check browser console**: Look for HTMX errors or failed requests

### Cards Display Empty

1. **Verify Alfresco content**: Check that Feature folders have `cm:title` and `cm:content`
2. **Check logs**: Look for errors in feature-card-handler
3. **Clear cache**: Force content reload
   ```clojure
   (require '[mtz-cms.cache.simple :as cache])
   (cache/clear-cache!)
   ```

### Images Not Displaying

1. **Check image proxy**: Verify `/proxy/image/:node-id` route works
2. **Check Alfresco content**: Ensure child image nodes exist
3. **Check browser network tab**: Look for 404s on image requests
4. **Verify node-id**: Ensure correct node-id in image URL

### Detail Page Not Found

1. **Check slug mapping**: Verify slug in URL matches mapping in `feature-detail-handler`
2. **Check route**: Ensure `/features/:slug` route is registered
3. **Check node-id**: Verify node-id exists in Alfresco
4. **Check logs**: Look for errors in handler

## Related Documentation

- [CONTENT_MODEL.md](CONTENT_MODEL.md) - Alfresco content model and aspects
- [MENU_SYSTEM.md](MENU_SYSTEM.md) - Navigation system
- [CACHE_MANAGEMENT.md](CACHE_MANAGEMENT.md) - Cache configuration
- [AI_CONTEXT.md](AI_CONTEXT.md) - Overall system architecture

## Summary

The homepage feature card system provides a modern, visually appealing way to highlight important content on the Mount Zion UCC website. The tall card format (600px height) creates a gallery-like appearance, with:

✅ **Dynamic loading** via HTMX
✅ **Clickable cards** linking to detail pages
✅ **Responsive design** for all screen sizes
✅ **Easy content management** through Alfresco
✅ **Beautiful hover effects** for enhanced UX
✅ **Production-ready** with error handling and caching

The system is complete and ready for production use! 🎉
