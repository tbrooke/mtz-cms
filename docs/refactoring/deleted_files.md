# Deleted Files Log

**Date**: October 1, 2025
**Branch**: refactor-components-2025
**Commit**: 44e7108

## Files Deleted

### Entire Directory Removed
- ✅ `src/mtz_cms/components.backup/` - 6 files, 899 lines total
  - htmx.clj (152 lines)
  - htmx_templates.clj (221 lines)
  - layouts.clj (124 lines)
  - resolvers.clj (131 lines)
  - templates.clj (55 lines)
  - templates_broken.clj (216 lines)

## Rationale
All files in `components.backup/` were redundant backups or broken versions.
Active code exists in the main components directory.

## Git Recovery
If needed, these files can be recovered from git history:
```bash
git show 44e7108^:src/mtz_cms/components.backup/htmx.clj
```

## Active Components (Kept)
- ✅ `components/htmx_templates.clj` - ACTIVE (used by routes/api.clj)
- ✅ `components/layouts.clj` - ACTIVE
- ✅ `components/templates.clj` - ACTIVE
- ✅ `components/resolvers.clj` - ACTIVE
- ✅ `components/sections.clj` - ACTIVE
- ✅ `components/htmx.clj` - ACTIVE
- ✅ `components/aspect_discovery.clj` - ACTIVE

## Space Saved
899 lines of redundant code removed! ✨

## Phase 5: Layout Reorganization (2025-10-01 14:45)

### File Moved and Deleted
**File**: src/mtz_cms/components/layouts.clj  
**Action**: Moved to src/mtz_cms/layouts/templates.clj, then original deleted  
**Lines**: 135 lines  
**Reason**: Layouts deserve their own top-level namespace for better organization  

**Migration Details**:
- Created new directory: `src/mtz_cms/layouts/`
- Content copied to: `src/mtz_cms/layouts/templates.clj`
- Updated import in: `src/mtz_cms/ui/pages.clj`
- Original file deleted after verification

**Imports Updated**:
- From: `[mtz-cms.components.layouts :as layouts]`
- To: `[mtz-cms.layouts.templates :as layouts]`

**Why This is Better**:
1. Layouts are distinct from components conceptually
2. Easier to find page composition logic
3. Room to grow (layout strategies, variants, etc)
4. Matches common patterns in web frameworks

