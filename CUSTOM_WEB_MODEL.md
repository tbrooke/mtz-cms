# 🎯 Custom Web Model (`web:web-api`)

**Created with:** Alfresco Model Manager
**Namespace:** `http://mtzcg.com/model/web/1.0`
**Prefix:** `web`
**Author:** Tom Brooke

---

## 📦 Aspects Created

### **1. `web:publishable` - Publishing Workflow**

**Description:** "Will this be Published and if so is it published yet"

**Properties:**

| Property | Type | Mandatory | Description | Constraint |
|----------|------|-----------|-------------|------------|
| `web:publishState` | `d:text` | ✅ Yes | Publish status | LIST: Draft, Ready, Publish, Archived |
| `web:publishDate` | `d:datetime` | ❌ No | Date(s) published/removed | Multi-valued |
| `web:targetPath` | `d:text` | ❌ No | Destination path in /API Published | - |

**Default Value:** `publishState` = "Draft"

**Use Cases:**
- Content approval workflow
- Scheduled publishing
- Content archiving
- Mirroring to published location

---

### **2. `web:siteMeta` - Page/Component Metadata**

**Description:** "Metadata to add for Web Site Content"

**Properties:**

| Property | Type | Mandatory | Description | Constraint |
|----------|------|-----------|-------------|------------|
| `web:kind` | `d:text` | ✅ Yes | Is this a page or component? | LIST: page, component |
| `web:componentType` | `d:text` | ❌ No | Type of component | LIST: Plain HTML, Hero, Section, Feature, Blog Post, Gallery |
| `web:menuItem` | `d:boolean` | ❌ No | Show in navigation menu? | - |
| `web:menuLabel` | `d:text` | ❌ No | Menu label text | - |

**Default Values:**
- `kind` = "page"
- `componentType` = "Plain HTML"
- `menuItem` = "false"

**Use Cases:**
- Distinguish pages from components
- Component type selection (Hero, Section, Feature, etc.)
- Navigation menu generation
- Custom menu labels

---

## 🎨 Design Analysis

### **✅ What's Great About This Design:**

1. **Separation of Concerns**
   - `web:publishable` = Workflow/publishing
   - `web:siteMeta` = Content structure/display
   - Can be applied independently or together

2. **Component Type Taxonomy**
   - Exactly what you need: Hero, Section, Feature, Blog Post, Gallery
   - Aligns with HyperUI templates
   - Extensible via Model Manager

3. **Page vs Component Distinction**
   - Clear hierarchy: Pages contain Components
   - `web:kind` makes queries simple

4. **Navigation Control**
   - `web:menuItem` boolean for visibility
   - `web:menuLabel` for custom text
   - Enables auto-menu generation

5. **Publishing States**
   - Draft → Ready → Publish → Archived
   - Clear workflow path
   - Can query for published content only

---

## 🔧 How to Use

### **Apply Aspects to Nodes**

**For a Page (e.g., Home Page):**
```
Add aspects:
- web:siteMeta

Set properties:
- web:kind = "page"
- web:menuItem = true
- web:menuLabel = "Home"
```

**For a Component (e.g., Hero):**
```
Add aspects:
- web:siteMeta
- web:publishable

Set properties:
- web:kind = "component"
- web:componentType = "Hero"
- web:publishState = "Publish"
```

---

## 📊 Pathom Integration

### **Query Pattern**

```clojure
;; Query for published Hero components
(alfresco/search-nodes ctx
  {:query "ASPECT:'web:siteMeta' AND web:kind:'component' AND web:componentType:'Hero' AND ASPECT:'web:publishable' AND web:publishState:'Publish'"})

;; Query for menu items
(alfresco/search-nodes ctx
  {:query "ASPECT:'web:siteMeta' AND web:menuItem:true"
   :orderBy [{:type "FIELD" :field "web:menuLabel" :ascending true}]})

;; Get component type
(get-in node [:properties :web:componentType])
;; => "Hero"

;; Check if published
(= "Publish" (get-in node [:properties :web:publishState]))
;; => true
```

### **Updated Resolvers**

```clojure
(pco/defresolver hero-component-resolver
  [ctx {:hero/keys [node-id]}]
  {::pco/input [:hero/node-id]
   ::pco/output [:hero/title :hero/image :hero/published? :hero/component-type]}

  (let [node (alfresco/get-node ctx node-id)
        props (:properties node)

        ;; Check it's actually a hero via aspect
        is-hero? (and (= "component" (:web:kind props))
                     (= "Hero" (:web:componentType props)))

        ;; Check publish status
        published? (= "Publish" (:web:publishState props))

        ;; Get image from children
        children (alfresco/get-node-children ctx node-id)
        image (first (filter image-file? children))]

    (when-not is-hero?
      (log/warn "Node" node-id "is not a Hero component"))

    {:hero/title (:cm:title props)
     :hero/image (when image {:id (:id image) :url (image-url image)})
     :hero/published? published?
     :hero/component-type (:web:componentType props)}))
```

---

## 🚀 Next Steps

### **1. Apply Aspects to Existing Nodes**

Update existing Hero/Feature folders:
```
Feature 1: Add web:siteMeta + web:publishable
  - web:kind = "component"
  - web:componentType = "Feature"
  - web:publishState = "Publish"

Feature 2: Add web:siteMeta + web:publishable
  - web:kind = "component"
  - web:componentType = "Feature"
  - web:publishState = "Publish"

Feature 3: Add web:siteMeta + web:publishable
  - web:kind = "component"
  - web:componentType = "Feature"
  - web:publishState = "Draft"  (since it's new)

Hero: Add web:siteMeta + web:publishable
  - web:kind = "component"
  - web:componentType = "Hero"
  - web:publishState = "Publish"

Home Page folder: Add web:siteMeta
  - web:kind = "page"
  - web:menuItem = true
  - web:menuLabel = "Home"
```

### **2. Update Pathom Resolvers**

- Add aspect/property checking
- Use `web:componentType` for component routing
- Filter by `web:publishState` = "Publish"
- Query by aspects instead of folder structure

### **3. Build Auto-Navigation**

```clojure
(defn get-menu-items [ctx]
  "Get all published menu items"
  (let [results (alfresco/search-nodes ctx
                  {:query "ASPECT:'web:siteMeta' AND web:menuItem:true AND ASPECT:'web:publishable' AND web:publishState:'Publish'"
                   :orderBy [{:type "FIELD" :field "web:menuLabel" :ascending true}]})]
    (map (fn [node]
           {:label (get-in node [:properties :web:menuLabel])
            :path (node-to-path node)})
         results)))
```

### **4. Component Type Registry**

Map `web:componentType` to renderers:
```clojure
(def component-renderers
  {"Hero" hero-template
   "Section" section-template
   "Feature" feature-template
   "Blog Post" blog-post-template
   "Gallery" gallery-template
   "Plain HTML" html-template})

(defn render-component [node]
  (let [comp-type (get-in node [:properties :web:componentType])
        renderer (get component-renderers comp-type html-template)]
    (renderer node)))
```

### **5. Publishing Workflow**

Create admin endpoints:
```clojure
POST /api/admin/publish/:node-id
  → Set web:publishState = "Publish"
  → Set web:publishDate = now
  → Mirror to targetPath if specified

POST /api/admin/archive/:node-id
  → Set web:publishState = "Archived"

GET /api/admin/pending
  → Query: web:publishState:'Ready'
  → Return list for review
```

---

## 📈 Malli Schemas

Auto-generated from aspects:

```clojure
(def web-publishable-schema
  [:map {:description "Publishing workflow aspect"}
   [:web:publishState [:enum "Draft" "Ready" "Publish" "Archived"]]
   [:web:publishDate {:optional true} [:sequential :string]] ; ISO datetime
   [:web:targetPath {:optional true} :string]])

(def web-site-meta-schema
  [:map {:description "Page/Component metadata aspect"}
   [:web:kind [:enum "page" "component"]]
   [:web:componentType {:optional true}
    [:enum "Plain HTML" "Hero" "Section" "Feature" "Blog Post" "Gallery"]]
   [:web:menuItem {:optional true} :boolean]
   [:web:menuLabel {:optional true} :string]])

(def web-component-schema
  [:map
   [:id :string]
   [:name :string]
   [:nodeType :string]
   [:aspects [:sequential :string]]
   [:properties [:merge
                 [:map [:cm:title :string]]
                 web-site-meta-schema
                 web-publishable-schema]]])
```

---

## 🎯 Summary

You've created a **clean, professional content model** using Model Manager that:

✅ Separates publishing workflow from content metadata
✅ Provides component type taxonomy (Hero, Feature, Section, etc.)
✅ Enables page/component distinction
✅ Supports auto-navigation generation
✅ Includes draft/publish workflow

**Next:** Apply these aspects to your existing nodes and update resolvers to leverage them!
