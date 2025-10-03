# Architecture Refactoring Plan - Mount Zion CMS

**Date:** 2025-10-03
**Status:** Proposed
**Issue:** Duplication and inconsistent organization across UI, components, layouts, and content directories

---

## Current Issues

### 1. Menu Navigation Bug ✅ FIXED
**Problem:** Menu disappeared on all pages except home page
**Root Cause:** Page functions weren't passing `ctx` to `base-layout`, so navigation wasn't being built
**Fix Applied:**
- Updated `about-page`, `demo-page`, and `dynamic-page` to accept and pass `ctx` parameter
- Updated all route handlers to pass `ctx` when calling page functions
- Removed Demo link from navigation (page still accessible at `/demo` for testing)

### 2. Directory Structure Confusion

**Current State:**
```
src/mtz_cms/
├── ui/
│   ├── components.clj    # Re-export wrapper (compatibility layer)
│   ├── pages.clj         # Full page templates
│   └── styles.clj        # CSS/Tailwind utilities
│
├── components/
│   ├── templates.clj     # Hero, Feature, Card components
│   ├── sections.clj      # Section components
│   ├── htmx.clj          # HTMX containers
│   ├── htmx_templates.clj
│   ├── primitives.clj    # Button, Loading, Error components
│   ├── navigation.clj    # Header, Footer rendering
│   ├── aspect_discovery.clj  # Alfresco logic (misplaced!)
│   └── resolvers.clj     # Pathom resolvers (misplaced!)
│
├── layouts/
│   └── templates.clj     # Layout compositions
│
├── navigation/
│   └── menu.clj          # Menu data building
│
└── content/
    └── static_loader.clj # Static content loading
```

**Problems:**
1. **Duplication:** `components/templates.clj` and `components/sections.clj` have overlapping purposes
2. **Duplication:** `components/htmx.clj` duplicates layout logic from `layouts/templates.clj`
3. **Mixed Concerns:** Alfresco business logic (`aspect_discovery`, `resolvers`) mixed with UI components
4. **Confusing Separation:** UI vs Components vs Layouts - not clear what goes where
5. **Re-export Layer:** `ui/components.clj` exists only for backward compatibility

### 3. Component Organization Issues

**No Clear Pattern:**
- Multiple components in single files (templates.clj has hero, feature, card)
- Inconsistent naming (templates vs sections vs primitives)
- Hard to find specific components
- Difficult to test components in isolation

---

## Proposed Architecture

### Core Principle: Simplicity & Clarity

**Every file should have ONE clear purpose**

```
src/mtz_cms/
├── ui/
│   ├── base.clj          # ONE base HTML shell (header, footer, CSS)
│   ├── layouts.clj       # Page layouts (hero-features, simple-content, cards-grid)
│   └── pages.clj         # Specific page compositions (home, about, 404)
│
├── components/           # Pure UI components - ONE per file
│   ├── primitives/
│   │   ├── button.clj
│   │   ├── loading.clj
│   │   └── message.clj
│   ├── navigation/
│   │   ├── header.clj    # Header rendering
│   │   ├── footer.clj    # Footer rendering
│   │   └── breadcrumb.clj
│   ├── hero.clj          # Hero component
│   ├── feature.clj       # Feature component
│   ├── card.clj          # Card component
│   └── section.clj       # Section component
│
├── alfresco/             # All Alfresco-specific code
│   ├── client.clj        # HTTP client
│   ├── discovery.clj     # Aspect discovery (moved from components/)
│   └── resolvers.clj     # Pathom resolvers (moved from components/)
│
├── navigation/
│   └── menu.clj          # Menu data building (no changes)
│
└── content/
    └── static.clj        # Static content loading (renamed from static_loader.clj)
```

### Separation of Concerns

**ui/base.clj** - The Foundation
```clojure
(defn base-page
  "Base HTML shell - ALWAYS THE SAME
   - <html> tag
   - <head> with CSS, meta tags
   - <body> with header, main, footer
   - Navigation is built here once
   - Cached for performance"
  [title content-layout ctx]
  [:html
   [:head
    (tailwind-cdn)
    (custom-styles)
    title]
   [:body
    (header/render (menu/build-navigation ctx))
    [:main content-layout]
    (footer/render)]])
```

**ui/layouts.clj** - Content Arrangements
```clojure
(defn hero-features-layout
  "Layout: Big hero + feature grid"
  [data]
  [:div
   (hero/render (:hero data))
   (features-grid (:features data))])

(defn simple-content-layout
  "Layout: Content + sidebar"
  [data]
  [:div.grid
   [:div.content (:content data)]
   [:div.sidebar (quick-links)]])
```

**ui/pages.clj** - Page Compositions
```clojure
(defn home-page [ctx]
  (base-page
    "Home - Mount Zion UCC"
    (layouts/hero-features (get-home-data ctx))
    ctx))

(defn about-page [ctx]
  (base-page
    "About - Mount Zion UCC"
    (layouts/simple-content (get-about-data ctx))
    ctx))
```

**components/*.clj** - Pure Presentation
```clojure
;; components/hero.clj
(defn render [data]
  [:section.hero
   [:h1 (:title data)]
   [:p (:subtitle data)]
   (button {:text "Learn More" :href "/about"})])

;; components/feature.clj
(defn render [data]
  [:div.feature
   [:h3 (:title data)]
   [:p (:content data)]
   (when (:image data)
     [:img {:src (:image data)}])])
```

---

## Migration Plan

### Phase 1: Fix Immediate Issues ✅ DONE
- [x] Fix menu navigation bug (pass ctx to all pages)
- [x] Remove Demo from navigation menu

### Phase 2: Move Misplaced Files
1. Create `src/mtz_cms/alfresco/` directory
2. Move `components/aspect_discovery.clj` → `alfresco/discovery.clj`
3. Move `components/resolvers.clj` → `alfresco/resolvers.clj`
4. Update imports across codebase

### Phase 3: Create Base Layout
1. Extract base HTML shell from `ui/pages.clj` into `ui/base.clj`
2. Create single `base-page` function that handles:
   - HTML structure
   - Head section (CSS, meta tags)
   - Navigation building (once, with caching)
   - Header/Footer rendering
3. Update all page functions to use new `base-page`

### Phase 4: Consolidate Layouts
1. Merge `layouts/templates.clj` into `ui/layouts.clj`
2. Remove HTMX layout duplication from `components/htmx.clj`
3. Keep only layout composition logic (no component rendering)

### Phase 5: Split Components (One Per File)
1. Create `components/hero.clj` - extract from templates.clj
2. Create `components/feature.clj` - extract from templates.clj
3. Create `components/card.clj` - extract from templates.clj
4. Move `components/sections.clj` → `components/section.clj` (singular)
5. Split `components/primitives.clj` into:
   - `components/primitives/button.clj`
   - `components/primitives/loading.clj`
   - `components/primitives/message.clj`
6. Split `components/navigation.clj` into:
   - `components/navigation/header.clj`
   - `components/navigation/footer.clj`
   - `components/navigation/breadcrumb.clj`

### Phase 6: Clean Up
1. Delete `ui/components.clj` (re-export wrapper)
2. Delete `components/htmx_templates.clj` if unused
3. Delete `layouts/` directory (merged into ui/layouts.clj)
4. Rename `content/static_loader.clj` → `content/static.clj`
5. Update all imports across codebase

### Phase 7: Documentation
1. Add docstrings to all new files
2. Create component library documentation
3. Update README with new architecture

---

## Benefits of New Structure

### 1. Crystal Clear Organization
- **ui/** - Page-level concerns (base HTML, layouts, pages)
- **components/** - Reusable UI pieces (one per file)
- **alfresco/** - All CMS integration logic
- **navigation/** - Menu system
- **content/** - Static content

### 2. Easier Testing
- Each component in its own file
- Pure functions, easy to test in REPL
- No mixing of concerns

### 3. Better Developer Experience
- "Where does X go?" has a clear answer
- Easy to find components
- No duplication confusion

### 4. Performance
- Base page cached once
- Navigation built once per request
- No redundant layout rendering

### 5. Maintainability
- One component per file = easier to modify
- Clear separation = less coupling
- Simple structure = less cognitive load

---

## Example: How a Page Request Works

**Current Flow (after menu fix):**
```
Request → Route Handler → Page Function (with ctx) → base-layout (with ctx) → Response
                                                           ↓
                                                      build-navigation
                                                      (from Alfresco)
```

**Proposed Flow:**
```
Request → Route Handler → Page Function → base-page → Response
                              ↓              ↓
                         Layout         Navigation
                         Component      (cached, built once)
                              ↓
                         Components
                         (hero, feature, etc.)
```

---

## Key Design Principles

### 1. Single Responsibility
Every file has ONE job:
- `ui/base.clj` - HTML shell
- `ui/layouts.clj` - Content arrangements
- `ui/pages.clj` - Page compositions
- `components/hero.clj` - Hero component
- `components/feature.clj` - Feature component

### 2. Pure Presentation Components
Components should:
- Accept data as parameters
- Return Hiccup vectors
- Have NO side effects
- Have NO Alfresco calls
- Be testable in REPL

### 3. Clear Data Flow
```
Alfresco → Pathom Resolvers → Page Data → Layout → Components → Hiccup → HTML
   ↑                                         ↑
   └─────── alfresco/ ──────────────────────┴──── ui/ + components/
```

### 4. Caching Strategy
- Base page structure: Static (never changes)
- Navigation: Cached 1 hour
- Page content: Dynamic or static (based on web:pagetype)

---

## Migration Checklist

### Before Starting
- [ ] Create feature branch: `refactor/architecture-cleanup`
- [ ] Run tests to establish baseline
- [ ] Document current import dependencies

### During Migration
- [ ] Make one change at a time
- [ ] Test after each change
- [ ] Update imports immediately
- [ ] Run REPL tests for each component

### After Completion
- [ ] All tests passing
- [ ] No unused files
- [ ] All imports updated
- [ ] Documentation complete
- [ ] Code review
- [ ] Merge to main

---

## Decision Log

### Why ONE component per file?
- Easier to find
- Easier to test
- Clearer purpose
- Standard practice in modern frontend development

### Why separate ui/ from components/?
- **ui/** = Page-level orchestration
- **components/** = Reusable building blocks
- Clear hierarchy: pages use layouts use components

### Why move Alfresco code out of components/?
- Components should be pure UI
- Alfresco is business logic
- Easier to test UI without mocking Alfresco

### Why consolidate layouts?
- One place for all layout logic
- No duplication between layouts/templates and htmx layouts
- Simpler mental model

---

## Notes

- **Backward Compatibility:** During migration, keep old files until all imports are updated
- **Testing:** Use REPL extensively to verify components work after refactoring
- **HTMX:** Keep HTMX integration but simplify - use components directly instead of duplicating layouts
- **Static Pages:** The static content system (sync-content.clj + static-loader) works well, keep it

---

## Questions for Discussion

1. Should we keep component-home-page or always use regular home-page?
2. Do we need htmx-specific templates or can we use regular components with hx-* attributes?
3. Should primitives be in subdirectory or individual files at components/ level?
4. Timeline for migration? (Recommend: incremental over 1-2 weeks)

---

## Success Criteria

The refactoring is complete when:

1. ✅ Every file has ONE clear purpose
2. ✅ No duplication between files
3. ✅ Alfresco logic separated from UI
4. ✅ Components are pure and testable
5. ✅ New developers can easily find code
6. ✅ "Where does X go?" has an obvious answer
