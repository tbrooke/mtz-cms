(ns mtz-cms.validation.schemas
  "Malli schemas for Mount Zion CMS data pipeline validation
   
   This provides type safety for the transformation:
   Alfresco Raw Data → Pathom Resolution → HTMX Components → Rendered HTML"
  (:require
   [malli.core :as m]
   [malli.transform :as mt]
   [malli.error :as me]
   [clojure.tools.logging :as log]))

;; --- GENERATED ALFRESCO SCHEMAS (from your babashka scripts) ---

(def alfresco-node-schema
  "Schema for Alfresco node data (generated from live API)"
  [:map
   [:id :string]
   [:name :string]
   [:nodeType :string]
   [:isFile :boolean]
   [:isFolder :boolean]
   [:createdAt :string]
   [:modifiedAt :string]
   [:properties [:map]]
   [:aspectNames [:sequential :string]]
   [:createdByUser [:map
                    [:id :string]
                    [:displayName :string]]]
   [:modifiedByUser [:map
                     [:id :string]
                     [:displayName :string]]]])

(def alfresco-children-response-schema
  "Schema for Alfresco children API response"
  [:map
   [:list [:map
           [:pagination [:map
                         [:count :int]
                         [:hasMoreItems :boolean]
                         [:totalItems :int]
                         [:skipCount :int]
                         [:maxItems :int]]]
           [:entries [:sequential [:map
                                   [:entry alfresco-node-schema]]]]]]])

;; --- PATHOM COMPONENT SCHEMAS ---

(def hero-component-input-schema
  "Schema for hero component input"
  [:map
   [:hero/node-id :string]])

(def hero-component-output-schema
  "Schema for hero component output"
  [:map
   [:hero/title :string]
   [:hero/content :string]
   [:hero/image [:maybe [:map
                         [:id :string]
                         [:name :string]
                         [:url :string]]]]])

(def feature-component-input-schema
  "Schema for feature component input"
  [:map
   [:feature/node-id :string]])

(def feature-component-output-schema
  "Schema for feature component output"
  [:map
   [:feature/title :string]
   [:feature/content :string]
   [:feature/type [:enum :feature-with-image :feature-text-only :feature-image-only :feature-placeholder]]
   [:feature/image [:maybe [:map
                            [:id :string]
                            [:name :string]
                            [:url :string]]]]])

;; --- PAGE COMPOSITION SCHEMAS ---

(def home-page-config-schema
  "Schema for home page component configuration"
  [:map
   [:home/hero hero-component-input-schema]
   [:home/features [:sequential feature-component-input-schema]]
   [:home/layout [:enum :hero-features-layout]]])

(def page-data-schema
  "Schema for resolved page data ready for rendering"
  [:map
   [:page/title :string]
   [:page/content :string]
   [:page/node-id [:maybe :string]]
   [:page/exists :boolean]])

;; --- HTMX RESPONSE SCHEMAS ---

(def htmx-fragment-schema
  "Schema for HTMX HTML fragment response"
  [:map
   [:status [:= 200]]
   [:headers [:map
              ["Content-Type" [:= "text/html"]]]]
   [:body :string]])

;; --- VALIDATION REGISTRY ---

(def schema-registry
  "Central registry of all schemas for validation"
  {;; Alfresco layer
   :alfresco/node alfresco-node-schema
   :alfresco/children-response alfresco-children-response-schema

   ;; Pathom component layer  
   :hero/input hero-component-input-schema
   :hero/output hero-component-output-schema
   :feature/input feature-component-input-schema
   :feature/output feature-component-output-schema

   ;; Page composition layer
   :home/config home-page-config-schema
   :page/data page-data-schema

   ;; HTMX response layer
   :htmx/fragment htmx-fragment-schema})

;; --- VALIDATION FUNCTIONS ---

(defn validate
  "Validate data against a schema from the registry"
  [schema-key data]
  (if-let [schema (get schema-registry schema-key)]
    (let [result (m/validate schema data)]
      (if result
        {:valid? true :data data}
        {:valid? false
         :errors (me/humanize (m/explain schema data))
         :schema-key schema-key
         :data data}))
    {:valid? false
     :errors {:schema "Schema not found in registry"}
     :schema-key schema-key}))

(defn validate!
  "Validate data and throw exception if invalid"
  [schema-key data]
  (let [result (validate schema-key data)]
    (if (:valid? result)
      (:data result)
      (throw (ex-info (str "Validation failed for " schema-key)
                      {:validation-errors (:errors result)
                       :schema schema-key
                       :data data})))))

(defn transform
  "Transform data using Malli transformers"
  [schema-key data transformer]
  (if-let [schema (get schema-registry schema-key)]
    (m/decode schema data transformer)
    data))

;; --- PIPELINE VALIDATION ---

(defn validate-alfresco-response
  "Validate raw Alfresco API response"
  [response]
  (validate :alfresco/children-response response))

(defn validate-pathom-component
  "Validate Pathom component resolution"
  [component-type input output]
  (let [input-result (validate (keyword (name component-type) "input") input)
        output-result (validate (keyword (name component-type) "output") output)]
    {:input-valid? (:valid? input-result)
     :output-valid? (:valid? output-result)
     :input-errors (:errors input-result)
     :output-errors (:errors output-result)}))

(defn validate-htmx-response
  "Validate HTMX fragment response"
  [response]
  (validate :htmx/fragment response))

;; --- DEBUGGING HELPERS ---

(defn explain-schema
  "Get human-readable explanation of a schema"
  [schema-key]
  (when-let [schema (get schema-registry schema-key)]
    (m/form schema)))

(defn list-schemas
  "List all available schemas"
  []
  (keys schema-registry))

;; --- INTEGRATION HELPERS ---

(defn with-validation
  "Wrapper that adds validation to any function"
  [f input-schema-key output-schema-key]
  (fn [& args]
    (let [input (first args)
          _ (validate! input-schema-key input)
          result (apply f args)
          _ (validate! output-schema-key result)]
      result)))

(comment
  ;; Usage examples:

  ;; Validate Alfresco data
  (validate :alfresco/node {:id "123" :name "test" :nodeType "cm:content"
                            :isFile true :isFolder false :createdAt "2024-01-01"})

  ;; Validate component input/output
  (validate :hero/input {:hero/node-id "abc-123"})
  (validate :hero/output {:hero/title "Welcome" :hero/content "Test" :hero/image nil})

  ;; List all schemas
  (list-schemas)

  ;; Explain a schema
  (explain-schema :hero/output))