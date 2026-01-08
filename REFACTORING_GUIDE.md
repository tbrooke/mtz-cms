# Design System Refactoring Guide

**Status**: In Progress
**Completed**: 2 of 13 components (15%)
**Priority Components Remaining**: 11

---

## ✅ Completed Refactoring

### 1. ✅ primitives.clj (DONE)
- **Status**: Fully refactored
- **Changes**:
  - Added `mtz-cms.ui.design-system` require
  - Refactored `error-message` to use `(ds/alert :error)`
  - Refactored `success-message` to use `(ds/alert :success)`
  - Refactored `button` to use `(ds/button variant opts)`
- **Impact**: High - used throughout app
- **Testing**: ✅ Tested in style guide

### 2. ✅ navigation.clj (DONE)
- **Status**: Fully refactored
- **Changes**:
  - Added `mtz-cms.ui.design-system` require
  - Refactored `site-header`:
    - Header background: `(ds/bg :bg-header)`
    - Text colors: `(ds/text :text-on-dark)`
    - Container: `(ds/container :7xl)`
    - Typography: `(ds/text-size :2xl)`, `(ds/font-weight :bold)`
    - Navigation links use design system colors and transitions
    - Dropdown menus use consistent styling
  - Refactored `site-footer`:
    - Background: `(ds/bg :bg-page)`
    - Text colors: `(ds/text :text-secondary)`, `(ds/text :text-muted)`
    - Spacing: `(ds/py :xl)`, `(ds/mt :3xl)`
  - Refactored `breadcrumbs`:
    - Link colors: `(ds/text :primary)`, `(ds/hover-text :primary-dark)`
    - Typography and spacing using design system
- **Impact**: CRITICAL - appears on every page
- **Testing**: ✅ Should reload server to see changes

---

## 🔄 Refactoring Pattern

### Before (Hardcoded):
```clojure
[:div {:class "bg-white rounded-lg shadow-md p-6 text-gray-900"}
 [:h2 {:class "text-2xl font-bold mb-4"} "Title"]
 [:p {:class "text-gray-600"} "Content"]]
```

### After (Design System):
```clojure
[:div {:class (ds/card {:padding :lg})}
 [:h2 {:class (ds/heading 2)} "Title"]
 [:p {:class (ds/text :text-secondary)} "Content"]]
```

Or more explicit:
```clojure
[:div {:class (ds/classes [(ds/bg :white)
                           (ds/rounded :lg)
                           (ds/shadow :md)
                           (ds/p :lg)])}
 [:h2 {:class (ds/classes [(ds/text-size :2xl)
                           (ds/font-weight :bold)
                           (ds/mb :md)])} "Title"]
 [:p {:class (ds/text :text-secondary)} "Content"]]
```

---

## 📋 Remaining Components (Priority Order)

### High Priority (Most Visible / Most Used)

#### 3. 🔄 card.clj (IN PROGRESS)
- **Instances**: 1 hardcoded class pattern
- **Impact**: High - cards used throughout site
- **Refactor**:
  ```clojure
  ;; Add to ns:
  (:require [mtz-cms.ui.design-system :as ds])

  ;; Replace:
  "bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
  ;; With:
  (ds/card {:hover? true})

  ;; Replace colors:
  "text-gray-900" → (ds/text :text-primary)
  "text-gray-600" → (ds/text :text-secondary)
  "text-blue-600" → (ds/text :primary)
  "bg-blue-100" → (ds/bg :primary-lighter)
  ```

#### 4. ⏳ blog.clj
- **Instances**: 7 hardcoded patterns
- **Impact**: High - blog pages are content-heavy
- **Refactor**:
  - Blog list item borders and colors
  - Typography (titles, dates, excerpts)
  - Link colors and hover states
  - Spacing and padding

#### 5. ⏳ contact_form.clj
- **Instances**: 9 hardcoded patterns (MOST!)
- **Impact**: Medium - single page but important
- **Refactor**:
  - Form inputs: Use `(ds/input)` and `(ds/input {:error? true})`
  - Labels: Use `(ds/text :text-secondary)`
  - Submit button: Use `(ds/button :primary)`
  - Error/success messages: Use `(ds/alert :error)` / `(ds/alert :success)`

#### 6. ⏳ hero.clj
- **Instances**: 2 hardcoded patterns
- **Impact**: High - homepage hero is first impression
- **Refactor**:
  - Hero container backgrounds and overlays
  - Typography (large headings)
  - CTA buttons

### Medium Priority

#### 7. ⏳ sunday_worship.clj
- **Instances**: 7 patterns
- **Refactor**: List and detail page styling

#### 8. ⏳ events.clj
- **Instances**: 6 patterns
- **Refactor**: Event cards and calendar styling

#### 9. ⏳ htmx.clj & htmx_templates.clj
- **Instances**: 8 each (16 total)
- **Refactor**: HTMX component containers and loading states

### Lower Priority

#### 10-13. ⏳ Other Components
- home_features.clj (4 patterns)
- section.clj (4 patterns)
- sections.clj (2 patterns)
- Others with 1-2 patterns

---

## 🚀 Quick Refactoring Workflow

### Step 1: Add Design System Require
```clojure
(ns your-component
  (:require
   ;; ... existing requires ...
   [mtz-cms.ui.design-system :as ds]))
```

### Step 2: Find Hardcoded Patterns
Search for:
- `"bg-blue-` → `(ds/bg :primary)`
- `"text-blue-` → `(ds/text :primary)`
- `"bg-white"` → `(ds/bg :bg-card)` or `(ds/bg :white)`
- `"text-gray-900"` → `(ds/text :text-primary)`
- `"text-gray-600"` → `(ds/text :text-secondary)`
- `"px-6 py-3"` → `(ds/px :lg)` and `(ds/py :md)`
- `"rounded-lg shadow-md"` → `(ds/rounded :lg)` and `(ds/shadow :md)`

### Step 3: Use Component Builders
```clojure
;; Buttons:
"px-6 py-3 bg-blue-600 text-white hover:bg-blue-700 rounded-md"
→ (ds/button :primary)

;; Cards:
"bg-white rounded-lg shadow-md overflow-hidden"
→ (ds/card)

;; Alerts:
"bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md"
→ (ds/alert :error)

;; Inputs:
"px-4 py-2 border border-gray-200 rounded-md"
→ (ds/input)
```

### Step 4: Combine with `ds/classes`
```clojure
;; When you need multiple utilities:
(ds/classes [(ds/bg :white)
             (ds/p :lg)
             (ds/shadow :md)
             (ds/rounded :lg)
             "custom-class"])  ; Can mix in custom classes
```

### Step 5: Test
```clojure
;; In REPL:
(require 'your-namespace :reload)
(user/restart)

;; Visit in browser to verify styling
```

---

## 📊 Progress Tracking

| Component | Instances | Priority | Status | % Done |
|-----------|-----------|----------|--------|--------|
| primitives.clj | 2 | High | ✅ Done | 100% |
| navigation.clj | 6 | Critical | ✅ Done | 100% |
| card.clj | 1 | High | 🔄 In Progress | 0% |
| contact_form.clj | 9 | Medium | ⏳ Pending | 0% |
| htmx_templates.clj | 8 | Medium | ⏳ Pending | 0% |
| htmx.clj | 8 | Medium | ⏳ Pending | 0% |
| blog.clj | 7 | High | ⏳ Pending | 0% |
| sunday_worship.clj | 7 | Medium | ⏳ Pending | 0% |
| events.clj | 6 | Medium | ⏳ Pending | 0% |
| home_features.clj | 4 | Medium | ⏳ Pending | 0% |
| section.clj | 4 | Low | ⏳ Pending | 0% |
| hero.clj | 2 | High | ⏳ Pending | 0% |
| sections.clj | 2 | Low | ⏳ Pending | 0% |
| **TOTAL** | **66** | | | **12%** |

---

## 🎯 Benefits of Refactoring

### Immediate Benefits (After Each Component):
- ✅ Consistent colors (when you change :primary, it updates everywhere)
- ✅ Cleaner code (less verbose)
- ✅ Type safety (design system functions are documented)
- ✅ Easier debugging (semantic names vs raw classes)

### Long-term Benefits (After Full Refactoring):
- ✅ Single source of truth for design
- ✅ Easy theme changes (change colors once)
- ✅ Faster development (reusable patterns)
- ✅ Better maintenance (clear naming)
- ✅ Consistent UX across entire site

---

## 🧪 Testing Strategy

After refactoring each component:

### 1. Visual Testing
```bash
# Restart server
(user/restart)

# Visit pages that use the component:
http://localhost:3000/              # Homepage
http://localhost:3000/blog          # Blog
http://localhost:3000/contact       # Contact
http://localhost:3000/admin/style-guide  # Style guide
```

### 2. Functional Testing
- Click all buttons
- Fill out forms
- Navigate menus
- Check hover states
- Test on mobile (responsive)

### 3. Color Testing
After refactoring, try changing primary color to verify consistency:
```clojure
;; In design_system.clj line 86:
:primary :purple-600  ; Change from :blue-600

;; Reload and check - everything should update!
(require 'mtz-cms.ui.design-system :reload)
(user/restart)
```

---

## 💡 Tips

### DO:
- ✅ Refactor one component at a time
- ✅ Test after each refactoring
- ✅ Use semantic colors (`:primary` not `:blue-600`)
- ✅ Use component builders when available
- ✅ Keep existing responsive classes (`md:`, `lg:`, etc.)
- ✅ Commit after each successful refactoring

### DON'T:
- ❌ Change multiple components without testing
- ❌ Mix hardcoded and design system styles in same file
- ❌ Remove responsive modifiers
- ❌ Skip testing after changes
- ❌ Refactor production code without backup

---

## 🚨 If Something Breaks

### 1. Undo Last Change
```bash
git diff src/mtz_cms/components/[file].clj
git checkout src/mtz_cms/components/[file].clj
```

### 2. Reload and Restart
```clojure
(require 'mtz-cms.ui.design-system :reload)
(require 'your-component :reload)
(user/restart)
```

### 3. Check Design System
```clojure
;; Test individual functions:
(mtz-cms.ui.design-system/text :primary)
(mtz-cms.ui.design-system/button :primary)
```

---

## 📅 Suggested Refactoring Schedule

### Session 1 (Done - 2 components)
- ✅ primitives.clj
- ✅ navigation.clj

### Session 2 (Next - 3 high-impact components)
- 🔄 card.clj
- ⏳ blog.clj
- ⏳ hero.clj

### Session 3 (Medium priority - 3 components)
- ⏳ contact_form.clj
- ⏳ sunday_worship.clj
- ⏳ events.clj

### Session 4 (HTMX components - 2 components)
- ⏳ htmx.clj
- ⏳ htmx_templates.clj

### Session 5 (Remaining - 3 components)
- ⏳ home_features.clj
- ⏳ section.clj
- ⏳ sections.clj

**Estimated Time**: 15-20 minutes per component = 3-4 hours total

---

## 🎉 When You're Done

After all components are refactored:

1. **Test color changes** - Change `:primary` and verify entire site updates
2. **Check style guide** - Visit `/admin/style-guide` to see consistency
3. **Review all pages** - Navigate entire site
4. **Update documentation** - Note any custom patterns
5. **Celebrate!** 🎉 You have a fully systematized design!

---

*Status as of: October 12, 2025*
*Last Updated: After completing navigation.clj*
*Next Up: card.clj, blog.clj, hero.clj*
