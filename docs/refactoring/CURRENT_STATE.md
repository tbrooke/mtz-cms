# Current State - Before Refactoring

**Date**: $(date +%Y-%m-%d)
**Branch**: refactor-components-2025

## Current File Structure

### Files to Keep (Good Structure)
- ✅ `src/mtz_cms/navigation/menu.clj` - Well structured, leave as-is
- ✅ `src/mtz_cms/alfresco/client.clj` - Alfresco API client
- ✅ `src/mtz_cms/alfresco/content_processor.clj` - Content processing
- ✅ `src/mtz_cms/config/core.clj` - Configuration
- ✅ `src/mtz_cms/validation/schemas.clj` - Malli schemas (good foundation)
- ✅ `src/mtz_cms/routes/main.clj` - HTTP routes
- ✅ `src/mtz_cms/routes/api.clj` - API routes

### Files to Delete
- ❌ `src/mtz_cms/components.backup/` - Entire directory (redundant)
- ❌ `src/mtz_cms/components/htmx_templates.clj` - Duplicate
- ❌ `src/mtz_cms/components/templates_broken.clj` - Broken

### Files to Refactor/Split
- 🔄 `src/mtz_cms/ui/components.clj` (300+ lines) → Split into:
  - `ui/styles.clj` (~20 lines)
  - `components/navigation.clj` (~100 lines)
  - `components/primitives.clj` (~60 lines)
  - `ui/components.clj` (~50 lines - re-exports only)

### Files to Move
- 📦 `src/mtz_cms/components/layouts.clj` → `src/mtz_cms/layouts/templates.clj`

### Files to Create
- 🆕 `src/mtz_cms/validation/pipeline.clj` - Validation enforcement
- 🆕 `src/mtz_cms/components/registry.clj` - Component registry
- 🆕 `src/mtz_cms/components/content.clj` - Content components (hero, feature)

## Current Architecture Problems

### Problem 1: Bloated ui/components.clj
**Lines**: 300+
**Issues**: 
- Mixes CSS (Tailwind CDN, custom styles)
- Mixes navigation rendering
- Mixes primitive components
- Has inline navigation logic (should be in navigation/menu.clj)

### Problem 2: Redundant Files
- `components.backup/` directory contains old versions
- Multiple template files with overlapping functionality
- Confusion about which file is authoritative

### Problem 3: Schema Disconnect
- Excellent Malli schemas in `validation/schemas.clj`
- But NO enforcement in the pipeline
- Data flows unvalidated: Alfresco → Pathom → Components → HTML

### Problem 4: Layout Confusion
- `components/layouts.clj` mixes layout structure with rendering
- Should be in separate namespace
- No clear component registry

## Dependencies Map

### navigation/menu.clj (✅ Keep)
- Depends on: config/core.clj, alfresco/client.clj
- Used by: ui/components.clj (currently), ui/pages.clj
- **Good structure**: Builds menu DATA only

### ui/components.clj (🔄 Refactor)
- Depends on: navigation/menu.clj
- Used by: ui/pages.clj, layouts.clj
- **Problem**: Does too much, mixes concerns

### validation/schemas.clj (✅ Keep + Extend)
- Depends on: malli
- Used by: (NOBODY - this is the problem!)
- **Need**: validation/pipeline.clj to enforce these

## Data Flow (Current)

```
Alfresco API
    ↓
alfresco/client.clj (fetch data)
    ↓
pathom/resolvers.clj (resolve queries)
    ↓
??? (no validation)
    ↓
ui/components.clj (inline rendering)
    ↓
ui/pages.clj (page composition)
    ↓
HTML → HTMX
```

## Data Flow (Target)

```
Alfresco API
    ↓
alfresco/client.clj (fetch data)
    ↓
validation/pipeline.clj (validate Alfresco response)
    ↓
pathom/resolvers.clj (resolve queries - return DATA not HTML)
    ↓
validation/pipeline.clj (validate Pathom output)
    ↓
components/registry.clj (render components)
    ↓
validation/pipeline.clj (validate component data)
    ↓
layouts/templates.clj (compose page)
    ↓
ui/page.clj (base HTML shell)
    ↓
HTML → HTMX
```

## Technical Notes

### SSH Tunnel Required
```bash
ssh -L 8080:localhost:8080 tmb@trust
```
Keep this running during development/testing.

### REPL Workflow
```clojure
; Start REPL
lein repl

; Reload namespaces after changes
(require '[namespace] :reload-all)

; Test functions
(function-call args)
```

### Git Strategy
- Branch: refactor-components-2025
- Commit after each major step
- Can always rollback if needed

## Context Documents Status

Need to review and potentially update:
- Any CONTEXT.md files
- Any GUIDE.md files  
- README files
- Architecture documentation

Will update these after refactoring is complete.
