# Practical Plan: Simple Caching + Keep What Works

**Date**: 2025-10-01
**Goal**: Speed up the site with caching, keep navigation dropdowns, prepare for blog

---

## Understanding the Real Needs

### Current Site (mtzionchinagrove.com)
- Astro static site
- YOU are tired of manually updating HTML
- Want others to contribute via Alfresco

### What You Actually Need
1. **Blog**: Pastor Jim Reflects (from Alfresco blog dashlet)
2. **Events page**: List of upcoming events
3. **Calendar**: Visual calendar (if possible)
4. **Navigation**: Keep existing menu structure with dropdowns
5. **Speed**: Cache heavy content from Alfresco

### Alfresco Role
- Content gathering/staging area
- Collaboration tool (not auto-publishing database)
- YOU decide what goes live

---

## Phase 1: Add Simple Caching (NOW)

### Already Created
✅ cache/simple.clj - In-memory TTL cache

### Changes Needed

#### 1. Add cache to ui/pages.clj
```clojure
(ns mtz-cms.ui.pages
  (:require
   [mtz-cms.cache.simple :as cache]  ; ADD THIS
   ...))

(defn base-layout
  ([title content] (base-layout title content nil))
  ([title content ctx]
   (let [nav (when ctx
               (cache/cached :navigation 3600  ; Cache 1 hour
                 #(try
                    (menu/build-navigation ctx)
                    (catch Exception e
                      (println "ERROR:" (.getMessage e))
                      nil))))]
     ...)))
```

**Result**: Navigation cached for 1 hour instead of built every request.

---

## Phase 2: Clean Up Unused Files (15 minutes)

### Files to Review & Potentially Delete

#### Check if actually used:
```bash
cd /Users/tombrooke/code/trust-server/mtzion/mtz-cms
grep -r "htmx-templates" src/ --include="*.clj"
grep -r "sections" src/ --include="*.clj"
```

#### If NOT used, delete:
- components/htmx_templates.clj (218 lines) - IF only used for demo
- components/sections.clj (65 lines) - IF redundant with templates.clj

#### Keep (definitely needed):
- components/templates.clj - Hero/feature rendering
- components/aspect_discovery.clj - Alfresco integration
- components/navigation.clj - Menu building
- layouts/templates.clj - Page layouts

---

## Phase 3: Navigation Structure (Keep Dropdowns!)

### Current Navigation from Alfresco
The aspect-discovery system ALREADY supports hierarchical menus via:
- Parent/child folder structure in Alfresco
- aspect/should-show-in-menu? for visibility

### Keep Current System
✅ navigation/menu.clj already handles:
- Building menu from Alfresco folders
- Dropdown submenus
- Proper hierarchy

### Just Cache It
The caching we add in Phase 1 will speed it up.

---

## Phase 4: Blog Support (NEEDED)

### Current Status
- Alfresco has blog dashlet
- Pastor Jim needs to post reflections
- Need listing page + individual post pages

### Create Blog Components

#### New file: components/blog.clj
```clojure
(ns mtz-cms.components.blog
  "Blog post components for Pastor Jim Reflects")

(defn blog-post-card
  "Card for blog listing page"
  [post]
  [:article {:class "bg-white rounded-lg shadow p-6 mb-4"}
   [:h2 {:class "text-2xl font-bold mb-2"} (:title post)]
   [:div {:class "text-gray-600 text-sm mb-4"}
    [:span "By " (:author post)]
    [:span " | " (:date post)]]
   [:div {:class "prose mb-4"}
    (:excerpt post)]
   [:a {:href (str "/blog/" (:id post))
        :class "text-blue-600 hover:underline"}
    "Read more →"]])

(defn blog-post-full
  "Full blog post page"
  [post]
  [:article {:class "bg-white rounded-lg shadow p-8"}
   [:h1 {:class "text-3xl font-bold mb-4"} (:title post)]
   [:div {:class "text-gray-600 mb-6"}
    [:span "By " (:author post)]
    [:span " | " (:date post)]]
   [:div {:class "prose max-w-none"}
    (:content post)]])
```

#### New layout: layouts/blog.clj
```clojure
(ns mtz-cms.layouts.blog
  (:require [mtz-cms.components.blog :as blog]))

(defn blog-listing-layout
  "Layout for blog listing page"
  [posts]
  [:div {:class "max-w-4xl mx-auto"}
   [:h1 {:class "text-4xl font-bold mb-8"} "Pastor Jim Reflects"]
   (for [post posts]
     (blog/blog-post-card post))])

(defn blog-post-layout
  "Layout for individual blog post"
  [post]
  [:div {:class "max-w-4xl mx-auto"}
   (blog/blog-post-full post)
   [:div {:class "mt-8"}
    [:a {:href "/blog" :class "text-blue-600 hover:underline"}
     "← Back to all posts"]]])
```

#### Add Pathom resolver for blogs
In pathom/resolvers.clj, add blog query support.

---

## Phase 5: Events Page (NEEDED)

### Simple Events List
Similar to blog but for events:
- Query Alfresco for event folders
- Display upcoming events
- Past events archived

### Components needed:
- components/events.clj
- layouts/events.clj

---

## Phase 6: Calendar (NICE TO HAVE)

### Options:
1. **Simple**: Just list events chronologically
2. **Calendar widget**: Add a JS calendar library
3. **Future**: Full calendar integration

### Start with #1, add calendar widget later if needed.

---

## File Structure After Cleanup

```
src/mtz_cms/
├── cache/
│   └── simple.clj              ✅ NEW - Caching
├── components/
│   ├── aspect_discovery.clj    ✅ Keep - Alfresco integration
│   ├── blog.clj                🆕 NEW - Blog components
│   ├── events.clj              🆕 NEW - Events components  
│   ├── navigation.clj          ✅ Keep - Menu building
│   ├── primitives.clj          ✅ Keep - Utilities
│   ├── templates.clj           ✅ Keep - Hero/feature rendering
│   ├── htmx.clj                ⚠️  Keep IF used, else delete
│   ├── htmx_templates.clj      ❌ Delete IF unused
│   ├── sections.clj            ❌ Delete IF redundant
│   └── resolvers.clj           ✅ Keep - Pathom resolvers
├── layouts/
│   ├── templates.clj           ✅ Keep - Page layouts
│   ├── blog.clj                🆕 NEW - Blog layouts
│   └── events.clj              🆕 NEW - Events layouts
└── ui/
    ├── base.clj                ⚠️  Consider creating from pages.clj
    ├── pages.clj               ✅ Keep + Add caching
    └── styles.clj              ✅ Keep - CSS
```

---

## Testing Plan

### After Phase 1 (Caching)
1. Start app
2. Visit home page (should see "Navigation built" once)
3. Refresh page (should NOT see "Navigation built" again)
4. Wait 1 hour, refresh (should see "Navigation built" again)
5. Test /admin/clear-cache endpoint

### After Phase 4 (Blog)
1. Create blog post in Alfresco blog dashlet
2. Visit /blog (should list posts)
3. Click post (should show full content)
4. Test that posts are queryable via Pathom

---

## Priority Order

### Week 1: Performance
1. ✅ Add caching to navigation (Phase 1)
2. ✅ Test speed improvement
3. ✅ Add admin cache clear endpoint

### Week 2: Cleanup
1. Review which files are actually used
2. Delete unused files
3. Test everything still works

### Week 3: Blog
1. Create blog components
2. Add blog routes
3. Test with real blog posts from Alfresco

### Week 4: Events
1. Create events components
2. Add events page
3. Test with real events

### Future: Calendar
- Research calendar libraries
- Add when actually needed

---

## Admin Endpoints to Add

```clojure
;; routes/api.clj

(GET "/admin/clear-cache" []
  (cache/clear-cache!)
  {:status 200 
   :headers {"Content-Type" "text/plain"}
   :body "Cache cleared successfully"})

(GET "/admin/cache-stats" []
  {:status 200
   :body {:cache-size (count @cache/cache)
          :keys (keys @cache/cache)}})
```

---

## Benefits of This Approach

✅ **Simple**: Just add caching, minimal changes
✅ **Fast**: Cache solves the slow load problem  
✅ **Practical**: Blog + Events = what you actually need
✅ **Maintainable**: Clean up unused code
✅ **Flexible**: Build calendar/preschool features when needed

---

## Next Immediate Steps

1. Add caching to ui/pages.clj (5 minutes)
2. Test that navigation caching works
3. Commit changes
4. Then decide on cleanup vs blog next

Ready to add the caching?
