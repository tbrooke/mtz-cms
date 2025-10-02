# Mystery Files Analysis

**Date**: 2025-10-01  
**Files Analyzed**: htmx.clj, aspect_discovery.clj

---

## File 1: components/htmx.clj (149 lines)

### Purpose
HTMX dynamic loading containers and page configuration

### What It Does
1. **Dynamic Containers**: Creates placeholder components that load via HTMX
   - htmx-hero-container - Loads hero dynamically from Alfresco
   - htmx-feature-container - Loads features dynamically
   - htmx-card-container - Loads cards dynamically

2. **Dynamic Layouts**: HTMX-powered page layouts
   - htmx-hero-features-layout - Similar to layouts/templates but with HTMX

3. **Interactive Features**:
   - htmx-editable-content - In-place editing
   - htmx-live-preview - Auto-refresh every 30s
   - htmx-component-selector - UI for choosing component types

4. **Page Config**: Hardcoded page configuration
   - get-page-component-config - Returns hero/feature node IDs for home page

### Used By
- routes/main.clj - Home handler uses htmx/htmx-hero-features-layout
- Currently ACTIVE in production!

### Status
**CRITICAL - IN USE**

### Analysis
This implements a COMPLETELY DIFFERENT rendering strategy than templates.clj!

**Two Parallel Systems**:
1. Static rendering (templates.clj) - Renders everything server-side
2. Dynamic loading (htmx.clj) - Renders placeholders, loads via AJAX

**Problems**:
- Duplicate layout logic (htmx-hero-features-layout vs layouts/templates.clj)
- Hardcoded node IDs (should come from Alfresco aspects!)
- No clear strategy - when to use static vs dynamic?

---

## File 2: components/aspect_discovery.clj (199 lines)

### Purpose
Alfresco aspect-based content discovery and validation

### What It Does
1. **Aspect Detection**:
   - has-web-site-meta? - Checks for web:siteMeta aspect
   - has-web-publishable? - Checks for web:publishable aspect

2. **Property Extraction**:
   - get-web-kind - Returns "page" or "component"
   - get-web-component-type - Returns "Hero", "Feature", etc.
   - get-web-publish-state - Returns "Publish" or "Draft"
   - get-web-menu-item? - Should show in navigation?
   - get-web-menu-label - Menu label text

3. **Type Detection**:
   - is-component? - Detects if node is a component
   - is-page? - Detects if node is a page
   - is-published? - Checks publish status
   - component-type-keyword - Returns :hero, :feature, etc.

4. **Navigation Building**:
   - should-show-in-menu? - Pages only, not components
   - get-menu-label - Extract menu text
   - build-navigation - Query for publishable menu pages

5. **Validation**:
   - validate-component-structure - Checks aspect correctness
   - Warns if components have menu properties (invalid)

6. **Search**:
   - search-components - Find components by type
   - Uses Alfresco FTS queries

### Used By
- navigation/menu.clj - Uses aspect/ functions heavily for menu building

### Status
**CRITICAL - CORE INFRASTRUCTURE**

### Analysis
This is THE BRAIN of the Alfresco integration!

**Why It Exists**:
Your Alfresco content is tagged with custom aspects:
- web:siteMeta (with web:kind, web:componentType)
- web:publishable (with web:publishState)
- Menu properties (web:menuItem, web:menuLabel)

This file provides the API to work with those aspects.

**It is ESSENTIAL** - without it, you cannot:
- Build navigation from Alfresco
- Distinguish pages from components
- Find published content
- Validate content structure

---

## The Big Picture

### Current Architecture Has TWO Rendering Paths:

**Path 1: Static Rendering** (templates.clj → layouts/templates.clj)
```
Request → Route → Pathom → Templates → Layouts → Response
```
Components are rendered server-side with all data.

**Path 2: Dynamic Loading** (htmx.clj)
```
Request → Route → HTMX Containers → Browser loads → API calls back
```
Components are placeholder shells that load content via HTMX.

### Which One Is Actually Used?

**routes/main.clj home-handler**:
```clojure
(defn home-handler [request]
  (let [page-config (htmx/get-page-component-config :home)]
    (pages/base-layout
      "Mount Zion UCC - Home"
      (htmx/htmx-hero-features-layout page-config)  ← HTMX path!
      ctx)))
```

**Currently using HTMX dynamic loading for home page!**

But layouts/templates.clj exists and is complete... just not used!

---

## Decisions to Make

### Question 1: Static vs Dynamic?

**Option A: Static (templates.clj + layouts/)**
✅ Faster initial load
✅ Better SEO
✅ Simpler architecture
❌ No live updates
❌ Full page reload for changes

**Option B: Dynamic (htmx.clj)**
✅ Live updates (every 30s)
✅ Can edit in place
✅ Progressive loading
❌ Slower initial render
❌ More complex
❌ SEO concerns

**Option C: Hybrid**
✅ Initial static render (fast)
✅ HTMX for updates/editing
✅ Best of both worlds
❌ Most complex

### Question 2: Hardcoded vs Aspect-Based Config?

**Currently**: htmx.clj has hardcoded node IDs
```clojure
:hero {:node-id "39985c5c-201a-42f6-985c-5c201a62f6d8"}
```

**Should Be**: Use aspect-discovery to find components!
```clojure
(aspect/search-components ctx :component-type :hero :parent-path "/Web Site/Home")
```

---

## Recommendations

### Keep Both Files But Clarify Roles

**aspect_discovery.clj** (199 lines) - **KEEP AS-IS**
- This is core infrastructure
- Well designed
- Essential for Alfresco integration
- Maybe move to alfresco/aspects.clj?

**htmx.clj** (149 lines) - **REFACTOR**
1. Remove hardcoded node IDs
2. Use aspect-discovery to find components
3. Decide on rendering strategy
4. Merge duplicate layout logic with layouts/templates.clj

### Consolidation Strategy

**Phase 1: Pick a Strategy** (Recommend Hybrid)
- Static render for initial page load
- HTMX attributes for live updates
- Use ONE set of components that support both

**Phase 2: Merge Duplicate Layouts**
- htmx-hero-features-layout → Use layouts/templates with HTMX attrs
- Keep container functions for dynamic loading

**Phase 3: Replace Hardcoded Config**
- Delete get-page-component-config
- Use aspect-discovery search functions
- Make page config come from Alfresco aspects

### File Structure After Refactor

```
alfresco/
  aspects.clj                ← Rename from components/aspect_discovery.clj

components/
  hero.clj                   ← Merge templates + htmx versions
  feature.clj
  section.clj
  
layouts/
  home.clj                   ← Use components with optional HTMX
  content.clj
  
ui/
  base.clj                   ← Base page shell
  htmx_containers.clj        ← Keep dynamic loading utilities
```

---

## Next Steps

1. **Decide on rendering strategy** (static/dynamic/hybrid)
2. **Test current behavior** - Understand what works today
3. **Consolidate templates** - One component = one file
4. **Remove hardcoded config** - Use aspect discovery
5. **Simplify layouts** - One layout, optional HTMX

---

## Questions for You

1. Do you want live updates (30s auto-refresh)?
2. Do you need in-place editing?
3. Is faster initial load more important?
4. Should we support both rendering modes?

