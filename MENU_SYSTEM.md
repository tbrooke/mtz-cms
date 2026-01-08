# 🎯 Hierarchical Menu System - WORKING!

**Status:** ✅ Fully Functional
**Test Date:** 2025-09-30
**Test Case:** Pickle Ball submenu under Activities

---

## 🎉 What We Built

A **two-tier hierarchical navigation system** that combines:

1. **Top-level pages** (9 main menu items - hardcoded node IDs)
2. **Dynamic submenus** (auto-discovered from Alfresco via `web:menuItem` aspect)

### **Example Menu Structure:**

```
▸ Home
▸ About
▸ Worship
▸ Events
▸ Activities
  └─ Pickle Ball  ← Discovered automatically!
▸ News
▸ Outreach
▸ Preschool
▸ Contact
```

---

## 🏗️ Architecture

### **Top-Level Pages (Fixed)**

Defined in `src/mtz_cms/navigation/menu.clj`:

```clojure
(def top-level-menu
  [{:key :home :label "Home" :path "/" :icon "home"}
   {:key :about :label "About" :path "/about" :icon "info"}
   {:key :worship :label "Worship" :path "/worship" :icon "church"}
   {:key :events :label "Events" :path "/events" :icon "calendar"}
   {:key :activities :label "Activities" :path "/activities" :icon "activity"
    :has-submenu? true}  ← Will check for children
   {:key :news :label "News" :path "/news" :icon "newspaper"}
   {:key :outreach :label "Outreach" :path "/outreach" :icon "heart"
    :has-submenu? true}
   {:key :preschool :label "Preschool" :path "/preschool" :icon "school"}
   {:key :contact :label "Contact" :path "/contact" :icon "mail"}])
```

### **Node ID Mapping**

Stored in `src/mtz_cms/config/core.clj`:

```clojure
(def page-node-mapping
  {:home "9faac48b-6c77-4266-aac4-8b6c7752668a"
   :about "8158a6aa-dbd7-4f5b-98a6-aadbd72f5b3b"
   :worship "2cf1aac5-8577-499e-b1aa-c58577a99ea0"
   :events "7c1da411-886a-4009-9da4-11886a6009c0"
   :activities "bb44a590-1c61-416b-84a5-901c61716b5e"  ← Parent of Pickle Ball
   :news "fd02c48b-3d27-4df7-82c4-8b3d27adf701"
   :outreach "b0774f12-4ea4-4851-b74f-124ea4f851a7"
   :preschool "915ea06b-4d65-4d5c-9ea0-6b4d65bd5cba"
   :contact "acfd9bd1-1e61-4c3b-bd9b-d11e611c3bc0"})
```

**Why hardcode these?**
- Fast lookups (no search queries needed)
- Stable structure (these 9 pages won't change)
- Easy to maintain as ENV variables if needed

---

## 🔍 Submenu Discovery Algorithm

For each top-level page with `:has-submenu? true`:

1. **Fetch children** of the parent page
   ```clojure
   (alfresco/get-node-children ctx parent-node-id
                               {:include "aspectNames,properties"})
   ```

2. **Filter for menu items**
   ```clojure
   ;; Keep only nodes that:
   - Have web:siteMeta aspect
   - Have web:kind = "page"
   - Have web:menuItem = true
   - Have web:publishable aspect
   - Have web:publishState = "Publish"
   ```

3. **Extract label**
   ```clojure
   ;; Priority:
   1. web:menuLabel (custom label)
   2. cm:title (standard title)
   3. name (folder name)
   ```

4. **Build submenu structure**
   ```clojure
   {:label "Pickle Ball"
    :path "/page/2a2aca9f-2285-4563-aaca-9f2285c563e9"
    :node-id "2a2aca9f-2285-4563-aaca-9f2285c563e9"}
   ```

---

## ✅ Validation Rules

### **What makes a valid submenu item?**

| Property | Requirement | Why |
|----------|-------------|-----|
| **Node Type** | `cm:folder` | Pages are folders |
| **web:siteMeta** | Must have aspect | Identifies it as a page |
| **web:kind** | Must = "page" | Not a component |
| **web:menuItem** | Must = `true` | Explicitly shown in menu |
| **web:publishable** | Must have aspect | Workflow control |
| **web:publishState** | Must = "Publish" | Only published content |

### **Why components can't be menu items:**

```clojure
;; Components are filtered out
(defn should-show-in-menu? [node]
  (and (is-page? node)        ← Must be a page
       (get-web-menu-item? node)))  ← Must have menuItem=true

;; Components have web:kind = "component" → is-page? returns false
```

---

## 📊 Test Results

### **Test Case: Pickle Ball Page**

Created in Alfresco under Activities:

**Node Properties:**
```json
{
  "name": "Pickle Ball",
  "id": "2a2aca9f-2285-4563-aaca-9f2285c563e9",
  "aspectNames": ["cm:titled", "cm:auditable", "web:siteMeta",
                  "cm:taggable", "web:publishable"],
  "properties": {
    "cm:title": "Pickle Ball",
    "web:kind": "page",
    "web:menuItem": true,
    "web:menuLabel": "Pickle Ball",
    "web:publishState": "Publish"
  }
}
```

**Test Script Output:**
```
▸ Activities
  └─ Pickle Ball

Summary:
  Top-level items: 9
  Items with submenus: 1
  Total submenu items: 1
```

✅ **SUCCESS!** Menu system correctly discovered Pickle Ball as a submenu item.

---

## 🚀 Usage

### **In Clojure Code:**

```clojure
(require '[mtz-cms.navigation.menu :as menu])

;; Build full navigation
(def nav (menu/build-navigation ctx))

;; Example result:
[{:key :activities
  :label "Activities"
  :path "/activities"
  :node-id "bb44a590-1c61-416b-84a5-901c61716b5e"
  :has-children? true
  :submenu [{:label "Pickle Ball"
             :path "/page/2a2aca9f-2285-4563-aaca-9f2285c563e9"
             :node-id "2a2aca9f-2285-4563-aaca-9f2285c563e9"}]}
 ...]

;; Get breadcrumbs
(menu/get-breadcrumbs nav "2a2aca9f-2285-4563-aaca-9f2285c563e9")
;; => [{:label "Home" :path "/"}
;;     {:label "Activities" :path "/activities"}
;;     {:label "Pickle Ball" :path "/page/..."}]

;; Find menu item
(menu/find-menu-item nav :activities)
;; => {:key :activities, :label "Activities", ...}

;; Get menu summary
(menu/menu-summary nav)
;; => {:top-level-count 9, :items-with-submenus 1, :total-submenu-items 1}
```

### **In HTMX Templates:**

```clojure
;; Generate dropdown menu
[:nav {:class "flex gap-4"}
 (for [item nav]
   (if (:has-children? item)
     ;; Dropdown menu
     [:div {:class "relative group"}
      [:button {:class "hover:text-blue-600"}
       (:label item)]
      [:div {:class "hidden group-hover:block absolute bg-white shadow-lg"}
       (for [sub (:submenu item)]
         [:a {:href (:path sub)
              :class "block px-4 py-2 hover:bg-gray-100"}
          (:label sub)])]]

     ;; Regular link
     [:a {:href (:path item)
          :class "hover:text-blue-600"}
      (:label item)]))]
```

---

## 📝 Adding New Submenu Items

**In Alfresco:**

1. Navigate to parent page folder (e.g., Activities)
2. Create new folder (e.g., "Youth Group")
3. Add aspects:
   - `web:siteMeta`
   - `web:publishable`
4. Set properties:
   - `web:kind` = "page"
   - `web:componentType` = "Plain HTML" (or appropriate type)
   - `web:menuItem` = true ✓
   - `web:menuLabel` = "Youth Group"
   - `web:publishState` = "Publish"
5. Save

**Result:** "Youth Group" automatically appears in Activities dropdown!

---

## 🔧 Configuration Options

### **Add More Top-Level Pages**

Edit `navigation/menu.clj`:

```clojure
(def top-level-menu
  [...existing items...
   {:key :calendar
    :label "Calendar"
    :path "/calendar"
    :icon "calendar"
    :has-submenu? true}])
```

Add to `config/core.clj`:

```clojure
(def page-node-mapping
  {...existing mappings...
   :calendar "your-calendar-node-id"})
```

### **Environment Variables (Future)**

Could move node IDs to ENV:

```bash
export MTZ_HOME_NODE_ID="9faac48b-6c77-4266-aac4-8b6c7752668a"
export MTZ_ACTIVITIES_NODE_ID="bb44a590-1c61-416b-84a5-901c61716b5e"
...
```

---

## 🎯 Files

- **`src/mtz_cms/navigation/menu.clj`** - Main menu system
- **`src/mtz_cms/components/aspect_discovery.clj`** - Aspect helpers
- **`src/mtz_cms/config/core.clj`** - Node ID mappings
- **`workbench/bb/test-menu-system.clj`** - Test script

**Run test:** `bb workbench/bb/test-menu-system.clj`

---

## ✨ Summary

You now have a **production-ready hierarchical menu system** that:

✅ Uses hardcoded node IDs for fast top-level navigation
✅ Automatically discovers subpages via `web:menuItem` aspect
✅ Enforces that only **pages** (not components) can be menu items
✅ Respects publishing workflow (`web:publishState`)
✅ Supports custom menu labels
✅ Generates breadcrumbs automatically
✅ **Tested and working with Pickle Ball example**

Content editors can now add submenu items just by checking a box in Alfresco!
