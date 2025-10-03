# 🔧 Component System Update Plan

## 📋 **Project Context**

**Date**: 2025-09-29
**Current Status**: Production-ready Content Model integration with full visual display (see AI_CONTEXT.md)
**Next Phase**: Generic Component System with Registry and Auto-Generated Schemas

---

## 🎯 **Objective**

Create a generic, extensible component system for Mount Zion CMS that:
1. Uses Alfresco Content Model as single source of truth
2. Auto-generates Malli schemas from Alfresco Model API
3. Provides central component registry for discovery
4. Establishes clear procedure for adding new components
5. Maintains type safety throughout the data pipeline

---

## 📐 **Architecture Overview**

### **Data Flow**
```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Alfresco Content Model (XML)                                 │
│    - cms:item type (title, slug, body, componentType, etc.)    │
│    - cms:placeable aspect (sitePath, region, slot)             │
│    - cms:publishable aspect (status, dates, workflow)          │
└───────────────────┬─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. Babashka Script (component-model-sync.clj)                   │
│    - Queries /alfresco/api/.../types?where=...                 │
│    - Queries /alfresco/api/.../aspects?where=...               │
│    - Extracts properties, constraints, datatypes               │
└───────────────────┬─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. Auto-Generated Malli Schemas                                 │
│    - generated-model/component-registry.edn (data)             │
│    - src/mtz_cms/validation/component_schemas.clj (code)       │
│    - Translates d:text → :string, d:int → :int, etc.          │
└───────────────────┬─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. Component Registry (registry.clj)                            │
│    - Central atom-based registry                                │
│    - Maps component ID → resolver, template, schema, metadata  │
│    - Provides discovery API                                     │
└───────────────────┬─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. Component Definitions (components/*.clj)                     │
│    - Use auto-generated schemas                                 │
│    - Define Pathom resolvers                                    │
│    - Define Hiccup templates                                    │
│    - Register with metadata                                     │
└───────────────────┬─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. Runtime Validation & Rendering                               │
│    - Validate at every pipeline step                            │
│    - API handlers use registry to resolve components            │
│    - HTMX endpoints serve validated HTML                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🗂️ **Alfresco Content Model Extension**

### **File**: `alfresco-config/cms-content-model.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<model name="cms:model" xmlns="http://www.alfresco.org/model/dictionary/1.0">

  <description>Mount Zion CMS Content Model</description>
  <author>Mount Zion Development Team</author>
  <version>1.0</version>

  <!-- Namespace -->
  <namespaces>
    <namespace uri="http://www.mtzcg.com/model/cms/1.0" prefix="cms"/>
  </namespaces>

  <!-- Imports -->
  <imports>
    <import uri="http://www.alfresco.org/model/dictionary/1.0" prefix="d"/>
    <import uri="http://www.alfresco.org/model/content/1.0" prefix="cm"/>
  </imports>

  <!-- Types -->
  <types>
    <type name="cms:item">
      <title>CMS Content Item</title>
      <description>Base type for all CMS components</description>
      <parent>cm:content</parent>

      <properties>
        <!-- Core Properties -->
        <property name="cms:title">
          <title>Title</title>
          <type>d:text</type>
          <mandatory>true</mandatory>
          <index enabled="true">
            <tokenised>true</tokenised>
          </index>
        </property>

        <property name="cms:slug">
          <title>URL Slug</title>
          <type>d:text</type>
          <mandatory>false</mandatory>
          <index enabled="true">
            <tokenised>false</tokenised>
          </index>
        </property>

        <property name="cms:body">
          <title>Body Content</title>
          <type>d:text</type>
          <mandatory>false</mandatory>
        </property>

        <property name="cms:componentType">
          <title>Component Type</title>
          <type>d:text</type>
          <mandatory>true</mandatory>
          <constraints>
            <constraint name="cms:componentTypeConstraint" type="LIST">
              <parameter name="allowedValues">
                <list>
                  <value>hero</value>
                  <value>section</value>
                  <value>feature</value>
                  <value>card</value>
                  <value>cta</value>
                  <value>gallery</value>
                  <value>testimonial</value>
                  <value>accordion</value>
                  <value>tabs</value>
                  <value>calendar</value>
                  <value>form</value>
                  <value>custom</value>
                </list>
              </parameter>
            </constraint>
          </constraints>
          <index enabled="true">
            <tokenised>false</tokenised>
          </index>
        </property>

        <property name="cms:ordering">
          <title>Display Order</title>
          <type>d:int</type>
          <mandatory>false</mandatory>
          <default>0</default>
        </property>

        <property name="cms:imageRef">
          <title>Image Reference</title>
          <description>Node reference to associated image</description>
          <type>d:noderef</type>
          <mandatory>false</mandatory>
        </property>

        <property name="cms:metadata">
          <title>Component Metadata</title>
          <description>JSON metadata for component-specific properties</description>
          <type>d:text</type>
          <mandatory>false</mandatory>
        </property>
      </properties>
    </type>
  </types>

  <!-- Aspects -->
  <aspects>
    <!-- Placement Aspect -->
    <aspect name="cms:placeable">
      <title>Placeable Content</title>
      <description>Controls where content appears on the site</description>

      <properties>
        <property name="cms:sitePath">
          <title>Site Path</title>
          <description>e.g., /home, /worship, /about</description>
          <type>d:text</type>
          <mandatory>false</mandatory>
          <index enabled="true">
            <tokenised>false</tokenised>
          </index>
        </property>

        <property name="cms:pagePath">
          <title>Page Path</title>
          <description>Specific page or folder path</description>
          <type>d:text</type>
          <mandatory>false</mandatory>
          <index enabled="true">
            <tokenised>false</tokenised>
          </index>
        </property>

        <property name="cms:region">
          <title>Region</title>
          <description>Page region where component appears</description>
          <type>d:text</type>
          <mandatory>false</mandatory>
          <constraints>
            <constraint name="cms:regionConstraint" type="LIST">
              <parameter name="allowedValues">
                <list>
                  <value>header</value>
                  <value>hero</value>
                  <value>main</value>
                  <value>sidebar</value>
                  <value>footer</value>
                  <value>menu</value>
                </list>
              </parameter>
            </constraint>
          </constraints>
          <index enabled="true">
            <tokenised>false</tokenised>
          </index>
        </property>

        <property name="cms:slot">
          <title>Slot</title>
          <description>Position within region (0-based)</description>
          <type>d:int</type>
          <mandatory>false</mandatory>
          <default>0</default>
        </property>
      </properties>
    </aspect>

    <!-- Publishing Aspect -->
    <aspect name="cms:publishable">
      <title>Publishable Content</title>
      <description>Workflow and publishing controls</description>

      <properties>
        <property name="cms:publishStatus">
          <title>Publish Status</title>
          <type>d:text</type>
          <mandatory>false</mandatory>
          <default>draft</default>
          <constraints>
            <constraint name="cms:statusConstraint" type="LIST">
              <parameter name="allowedValues">
                <list>
                  <value>draft</value>
                  <value>review</value>
                  <value>published</value>
                  <value>archived</value>
                </list>
              </parameter>
            </constraint>
          </constraints>
          <index enabled="true">
            <tokenised>false</tokenised>
          </index>
        </property>

        <property name="cms:publishedDate">
          <title>Published Date</title>
          <type>d:datetime</type>
          <mandatory>false</mandatory>
        </property>

        <property name="cms:publishedBy">
          <title>Published By</title>
          <type>d:text</type>
          <mandatory>false</mandatory>
        </property>

        <property name="cms:embargoDate">
          <title>Embargo Date</title>
          <description>Do not publish before this date</description>
          <type>d:datetime</type>
          <mandatory>false</mandatory>
        </property>

        <property name="cms:expiryDate">
          <title>Expiry Date</title>
          <description>Unpublish after this date</description>
          <type>d:datetime</type>
          <mandatory>false</mandatory>
        </property>
      </properties>
    </aspect>
  </aspects>

</model>
```

---

## 🤖 **Babashka Schema Generator**

### **File**: `component-model-sync.clj`

```clojure
#!/usr/bin/env bb
;; Component Model Schema Generator
;; Queries Alfresco Model API and generates Malli schemas

(require '[babashka.curl :as curl]
         '[cheshire.core :as json]
         '[clojure.edn :as edn]
         '[clojure.string :as str]
         '[clojure.java.io :as io]
         '[clojure.pprint :as pprint])

;; --- CONFIGURATION ---
(def alfresco-host "http://localhost:8080")
(def alfresco-user "admin")
(def alfresco-pass "admin")
(def api-base (str alfresco-host "/alfresco/api/-default-/public/alfresco/versions/1"))

;; --- HTTP CLIENT ---
(defn get-json [url]
  (let [resp (curl/get url {:basic-auth [alfresco-user alfresco-pass]})]
    (if (= 200 (:status resp))
      (json/parse-string (:body resp) true)
      (throw (ex-info (str "Failed to fetch " url) resp)))))

;; --- FETCH ALFRESCO MODEL ---
(defn fetch-custom-types []
  "Fetch custom types from Alfresco (cms:* namespace)"
  (println "📥 Fetching custom types...")
  (let [url (str api-base "/types?where=(namespaceUri matches('http://www.mtzcg.com/model/.*'))&include=properties,mandatoryAspects")
        response (get-json url)
        types (get-in response [:list :entries])]
    (println (str "   Found " (count types) " custom types"))
    types))

(defn fetch-custom-aspects []
  "Fetch custom aspects from Alfresco (cms:* namespace)"
  (println "📥 Fetching custom aspects...")
  (let [url (str api-base "/aspects?where=(namespaceUri matches('http://www.mtzcg.com/model/.*'))&include=properties")
        response (get-json url)
        aspects (get-in response [:list :entries])]
    (println (str "   Found " (count aspects) " custom aspects"))
    aspects))

;; --- SCHEMA TRANSLATION ---
(defn alfresco-datatype->malli
  "Translate Alfresco datatype to Malli schema"
  [datatype is-multi-valued]
  (let [base-type (case datatype
                    "d:text" :string
                    "d:mltext" :string
                    "d:int" :int
                    "d:long" :int
                    "d:float" :double
                    "d:double" :double
                    "d:boolean" :boolean
                    "d:datetime" :string  ; ISO date string
                    "d:date" :string
                    "d:noderef" :string   ; Node reference
                    "d:content" :string   ; Binary content reference
                    :any)]
    (if is-multi-valued
      [:sequential base-type]
      base-type)))

(defn property->malli-field
  "Convert Alfresco property to Malli field"
  [prop]
  (let [prop-id (keyword (:id prop))
        malli-type (alfresco-datatype->malli (:dataType prop) (:isMultiValued prop))
        is-mandatory (:isMandatory prop false)]
    (if is-mandatory
      [prop-id malli-type]
      [prop-id {:optional true} malli-type])))

(defn type->malli-schema
  "Convert Alfresco type to Malli schema"
  [type-entry]
  (let [type-data (:entry type-entry)
        type-id (:id type-data)
        properties (:properties type-data)
        component-name (last (str/split type-id #":"))]

    {:type-id type-id
     :component-type component-name
     :malli-schema (into [:map] (map property->malli-field properties))}))

(defn aspect->malli-schema
  "Convert Alfresco aspect to Malli schema"
  [aspect-entry]
  (let [aspect-data (:entry aspect-entry)
        aspect-id (:id aspect-data)
        properties (:properties aspect-data)
        aspect-name (last (str/split aspect-id #":"))]

    {:aspect-id aspect-id
     :aspect-name aspect-name
     :malli-schema (into [:map] (map property->malli-field properties))}))

;; --- EXTRACT CONSTRAINTS ---
(defn extract-component-types
  "Extract allowed component types from cms:componentType constraint"
  [types]
  (let [cms-item (first (filter #(= "cms:item" (get-in % [:entry :id])) types))]
    (when cms-item
      (let [properties (get-in cms-item [:entry :properties])
            comp-type-prop (first (filter #(= "cms:componentType" (:id %)) properties))
            constraints (:constraints comp-type-prop)]
        (when constraints
          (set (flatten
                (for [constraint constraints]
                  (when (= "LIST" (:type constraint))
                    (get-in constraint [:parameters :allowedValues]))))))))))

;; --- GENERATE REGISTRY ---
(defn generate-component-registry
  "Create complete schema registry"
  [types aspects]
  (let [type-schemas (map type->malli-schema types)
        aspect-schemas (map aspect->malli-schema aspects)
        component-types (extract-component-types types)]

    {:component-types component-types
     :type-schemas (into {} (map (juxt :type-id :malli-schema) type-schemas))
     :aspect-schemas (into {} (map (juxt :aspect-id :malli-schema) aspect-schemas))
     :metadata {:types (map #(select-keys % [:type-id :component-type]) type-schemas)
                :aspects (map #(select-keys % [:aspect-id :aspect-name]) aspect-schemas)}}))

;; --- GENERATE CLOJURE CODE ---
(defn generate-schema-namespace
  "Generate Clojure namespace with schemas"
  [registry]
  (str
   ";; AUTO-GENERATED - DO NOT EDIT MANUALLY\n"
   ";; Generated: " (java.time.Instant/now) "\n"
   ";; Run: bb component-model-sync.clj to regenerate\n\n"

   "(ns mtz-cms.validation.component-schemas\n"
   "  \"Auto-generated Malli schemas from Alfresco Content Model\"\n"
   "  (:require [malli.core :as m]))\n\n"

   ";; Component Types\n"
   "(def component-types\n"
   "  " (pr-str (:component-types registry)) ")\n\n"

   ";; Type Schemas\n"
   (str/join "\n\n"
             (for [[type-id schema-def] (:type-schemas registry)]
               (let [type-name (last (str/split (name type-id) #":"))]
                 (str "(def " type-name "-schema\n"
                      "  \"Schema for " type-id "\"\n"
                      "  " (pr-str schema-def) ")"))))
   "\n\n"

   ";; Aspect Schemas\n"
   (str/join "\n\n"
             (for [[aspect-id schema-def] (:aspect-schemas registry)]
               (let [aspect-name (last (str/split (name aspect-id) #":"))]
                 (str "(def " aspect-name "-aspect-schema\n"
                      "  \"Schema for " aspect-id "\"\n"
                      "  " (pr-str schema-def) ")"))))
   "\n\n"

   ";; Registry\n"
   "(def schema-registry\n"
   "  {:types " (pr-str (:type-schemas registry)) "\n"
   "   :aspects " (pr-str (:aspect-schemas registry)) "})\n"))

;; --- MAIN ---
(defn -main []
  (println "🔄 Component Model Schema Generator")
  (println "   Connecting to Alfresco at" alfresco-host)

  (try
    (let [types (fetch-custom-types)
          aspects (fetch-custom-aspects)
          registry (generate-component-registry types aspects)]

      ;; Create directories
      (io/make-parents "generated-model/component-registry.edn")
      (io/make-parents "src/mtz_cms/validation/component_schemas.clj")

      ;; Save EDN registry
      (println "💾 Saving registry...")
      (with-open [writer (io/writer "generated-model/component-registry.edn")]
        (pprint/pprint registry writer))

      ;; Save raw data
      (with-open [writer (io/writer "generated-model/component-model-raw.edn")]
        (pprint/pprint {:types types :aspects aspects} writer))

      ;; Generate Clojure namespace
      (println "💾 Generating Clojure schemas...")
      (spit "src/mtz_cms/validation/component_schemas.clj"
            (generate-schema-namespace registry))

      (println "\n✅ Success!")
      (println "   📄 generated-model/component-registry.edn")
      (println "   📄 generated-model/component-model-raw.edn")
      (println "   📄 src/mtz_cms/validation/component_schemas.clj")
      (println "\n📊 Summary:")
      (println "   Component types:" (count (:component-types registry)))
      (println "   Custom types:" (count (:type-schemas registry)))
      (println "   Custom aspects:" (count (:aspect-schemas registry))))

    (catch Exception e
      (println "❌ Error:" (.getMessage e))
      (.printStackTrace e)
      (System/exit 1))))

(-main)
```

---

## 📦 **Component Registry System**

### **File**: `src/mtz_cms/components/registry.clj`

```clojure
(ns mtz-cms.components.registry
  "Central registry for CMS components")

(defonce ^:private component-registry (atom {}))

(defn register-component!
  "Register a component with metadata and rendering function

  Required keys:
  - :id - Keyword identifier (e.g., :section)
  - :type - Component type string (e.g., 'section')
  - :resolver-fn - Pathom resolver function
  - :template-fn - Hiccup template function

  Optional keys:
  - :category - HyperUI category (e.g., 'marketing')
  - :collection - HyperUI collection (e.g., 'sections')
  - :api-route - API endpoint path
  - :input-schema - Malli input schema
  - :output-schema - Malli output schema
  - :cms-schema - Generated CMS schema from Alfresco
  - :alfresco-type - Alfresco content type (e.g., 'cms:item')
  - :alfresco-aspects - List of required aspects
  - :metadata - Additional metadata (description, thumbnail, etc.)"
  [{:keys [id type category collection resolver-fn template-fn
           api-route input-schema output-schema cms-schema
           alfresco-type alfresco-aspects metadata]
    :as component-def}]
  (when-not (and id type resolver-fn template-fn)
    (throw (ex-info "Missing required component registration keys"
                    {:required [:id :type :resolver-fn :template-fn]
                     :provided (keys component-def)})))

  (swap! component-registry assoc id component-def)
  (println "✅ Registered component:" id))

(defn unregister-component!
  "Remove a component from the registry"
  [id]
  (swap! component-registry dissoc id))

(defn get-component
  "Get component definition by ID"
  [id]
  (get @component-registry id))

(defn list-components
  "List all registered components"
  []
  (vals @component-registry))

(defn component-types
  "Get set of all registered component types"
  []
  (set (map :type (vals @component-registry))))

(defn find-by-type
  "Find all components of a given type"
  [component-type]
  (filter #(= component-type (:type %)) (vals @component-registry)))

(defn find-by-category
  "Find all components in a category"
  [category]
  (filter #(= category (:category %)) (vals @component-registry)))

(defn registry-stats
  "Get registry statistics"
  []
  {:total-components (count @component-registry)
   :component-types (component-types)
   :categories (set (keep :category (vals @component-registry)))
   :collections (set (keep :collection (vals @component-registry)))})
```

---

## 🔨 **Implementation Plan**

### **Phase 1: Alfresco Content Model Setup**
- [ ] Create `alfresco-config/cms-content-model.xml`
- [ ] Deploy to Alfresco server
- [ ] Restart Alfresco to load model
- [ ] Verify model loaded via Alfresco admin console

### **Phase 2: Schema Generation**
- [ ] Create `component-model-sync.clj` Babashka script
- [ ] Run script to generate initial schemas
- [ ] Verify generated files:
  - [ ] `generated-model/component-registry.edn`
  - [ ] `generated-model/component-model-raw.edn`
  - [ ] `src/mtz_cms/validation/component_schemas.clj`

### **Phase 3: Component Registry**
- [ ] Create `src/mtz_cms/components/registry.clj`
- [ ] Add tests for registry functions
- [ ] Update `src/mtz_cms/pathom/resolvers.clj` to use registry

### **Phase 4: Convert Existing Components**
- [ ] Update `hero-component` to use new pattern
- [ ] Update `feature-component` to use new pattern
- [ ] Update `section-component` to use new pattern
- [ ] Verify all existing functionality still works

### **Phase 5: Dynamic API Handler**
- [ ] Create generic component handler that uses registry
- [ ] Update `src/mtz_cms/routes/api.clj` to use dynamic handler
- [ ] Test all existing API endpoints

### **Phase 6: Documentation & Testing**
- [ ] Create component addition guide
- [ ] Document Babashka workflow
- [ ] Create example new component (e.g., testimonial)
- [ ] Write integration tests

---

## 📝 **Component Addition Procedure**

### **Adding a New Component Type**

#### **1. Update Alfresco Content Model**
```bash
# Edit alfresco-config/cms-content-model.xml
# Add new value to cms:componentType constraint
<value>testimonial</value>

# Deploy and restart Alfresco
```

#### **2. Regenerate Schemas**
```bash
# Run Babashka script
bb component-model-sync.clj

# Verify generated files updated
cat generated-model/component-registry.edn | grep testimonial
```

#### **3. Design Component Template**
```bash
# Browse HyperUI for component
# https://www.hyperui.dev/

# Save HTML and convert to Hiccup
bb convert-to-hiccup.clj testimonial.html > testimonial.clj.bak
```

#### **4. Create Component File**
```clojure
;; src/mtz_cms/components/testimonial.clj

(ns mtz-cms.components.testimonial
  (:require [mtz-cms.components.registry :as registry]
            [mtz-cms.components.resolvers :refer [resolve-content-component]]
            [mtz-cms.validation.component-schemas :as cms-schemas]
            [com.wsscode.pathom3.connect.operation :as pco]))

;; Define schemas
(def testimonial-input-schema
  [:map [:testimonial/node-id :string]])

(def testimonial-output-schema
  [:map
   [:testimonial/title :string]
   [:testimonial/content :string]
   [:testimonial/author {:optional true} :string]
   [:testimonial/image {:optional true}
    [:map [:id :string] [:url :string] [:name :string]]]
   [:testimonial/cms-properties {:optional true} cms-schemas/item-schema]])

;; Define resolver
(pco/defresolver testimonial-resolver
  [ctx {:testimonial/keys [node-id]}]
  {::pco/input [:testimonial/node-id]
   ::pco/output [:testimonial/title :testimonial/content :testimonial/author
                 :testimonial/image :testimonial/cms-properties]}
  (let [data (resolve-content-component ctx node-id :testimonial)]
    {:testimonial/title (:title data)
     :testimonial/content (:content data)
     :testimonial/author (get-in data [:cms-properties :cms:author])
     :testimonial/image (:image data)
     :testimonial/cms-properties (:cms-properties data)}))

;; Define template
(defn testimonial-template
  [{:testimonial/keys [title content author image]}]
  [:div {:class "bg-white rounded-lg shadow-md p-6"
         :data-component-type "testimonial"}
   (when image
     [:img {:src (:url image)
            :alt (:name image)
            :class "w-16 h-16 rounded-full mb-4"}])
   [:blockquote {:class "text-gray-700 italic mb-4"}
    [:p content]]
   [:div {:class "text-gray-900 font-semibold"} (or author "Anonymous")]])

;; Register component
(registry/register-component!
  {:id :testimonial
   :type "testimonial"
   :category "marketing"
   :collection "testimonials"
   :alfresco-type "cms:item"
   :alfresco-aspects ["cms:placeable" "cms:publishable"]
   :resolver-fn testimonial-resolver
   :template-fn testimonial-template
   :input-schema testimonial-input-schema
   :output-schema testimonial-output-schema
   :cms-schema cms-schemas/item-schema
   :api-route "/api/components/testimonial/:node-id"
   :metadata {:description "Customer testimonial with optional image"
              :thumbnail "/assets/components/testimonial.png"}})
```

#### **5. Add API Handler**
```clojure
;; src/mtz_cms/routes/api.clj

(defn testimonial-component-handler [request]
  (let [node-id (get-in request [:path-params :node-id])
        component (registry/get-component :testimonial)
        result ((:resolver-fn component) {} {:testimonial/node-id node-id})]
    (html-fragment-response
     ((:template-fn component) result))))

;; Add to routes
["/testimonial/:node-id" {:get testimonial-component-handler}]
```

#### **6. Create Content in Alfresco**
```
1. Create node with type cms:item
2. Set cms:title = "Great Experience"
3. Set cms:componentType = "testimonial"
4. Set cms:body = "Mount Zion has been..."
5. Add cms:placeable aspect
   - cms:sitePath = "/home"
   - cms:region = "main"
   - cms:slot = 3
6. Add cms:publishable aspect
   - cms:publishStatus = "published"
```

#### **7. Test Component**
```clojure
;; In REPL
(require '[mtz-cms.components.testimonial])
(registry/get-component :testimonial)

;; Via HTTP
curl http://localhost:3000/api/components/testimonial/<node-id>
```

---

## 🎯 **Success Criteria**

### **Schema Generation Working**
- [ ] Babashka script queries Alfresco successfully
- [ ] EDN registry file generated
- [ ] Clojure namespace generated with valid schemas
- [ ] Schemas validate correctly with Malli

### **Registry System Working**
- [ ] Components can be registered
- [ ] Components can be queried by ID, type, category
- [ ] Registry provides discovery API
- [ ] Stats and metadata accessible

### **Component Pattern Working**
- [ ] Existing components converted to new pattern
- [ ] All existing functionality preserved
- [ ] New components can be added easily
- [ ] Schemas validate at every step

### **Documentation Complete**
- [ ] Component addition procedure documented
- [ ] Babashka workflow documented
- [ ] Example component provided
- [ ] Developer guide complete

---

## 🔗 **Related Files**

### **Current Files**
- `AI_CONTEXT.md` - Project history and current state
- `src/mtz_cms/components/resolvers.clj` - Current component resolvers
- `src/mtz_cms/components/sections.clj` - Section component implementation
- `src/mtz_cms/routes/api.clj` - API handlers
- `model_sync_working.clj` - Existing schema generator

### **New Files to Create**
- `COMPONENT_UPDATE.md` - This file (implementation plan)
- `alfresco-config/cms-content-model.xml` - Content model extension
- `component-model-sync.clj` - Schema generator
- `src/mtz_cms/components/registry.clj` - Component registry
- `src/mtz_cms/validation/component_schemas.clj` - Auto-generated schemas
- `generated-model/component-registry.edn` - Generated registry data

---

## 📞 **Questions & Decisions**

### **Resolved**
- ✅ Use Alfresco Content Model as single source of truth
- ✅ Auto-generate schemas with Babashka
- ✅ Central registry for component discovery
- ✅ Maintain existing `resolve-content-component` helper for common patterns

### **To Resolve**
- [ ] Should we support component variants (e.g., section-two-column vs section-three-column)?
- [ ] How to handle component composition (components within components)?
- [ ] Should registry persist to disk or remain in-memory?
- [ ] How to version component definitions?

---

## 🚀 **Next Steps**

When resuming this work:

1. **Read this document** to understand the plan
2. **Review AI_CONTEXT.md** for project history
3. **Start with Phase 1**: Create Alfresco Content Model XML
4. **Follow phases sequentially**: Each phase builds on previous
5. **Test incrementally**: Verify each phase before moving to next

---

**End of Component Update Plan**