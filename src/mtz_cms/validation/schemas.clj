(ns mtz-cms.validation.schemas
  "Malli schemas for Mount Zion CMS data pipeline validation
   
   This provides type safety for the transformation:
   Alfresco Raw Data → Pathom Resolution → HTMX Components → Rendered HTML"
  (:require
   [malli.core :as m]
   [malli.transform :as mt]
   [malli.error :as me]
   [clojure.tools.logging :as log]))

;; --- ALFRESCO BASE SCHEMAS (lightweight, grows as needed) ---

(def alfresco-node-schema
  "Lightweight schema for Alfresco node - validates core fields only

   Philosophy: We don't control Alfresco, so validate minimally.
   This schema checks that we have the essential fields we need."
  [:map
   [:id :string]
   [:name :string]
   [:nodeType :string]
   [:isFile {:optional true} :boolean]
   [:isFolder {:optional true} :boolean]
   ;; Everything else is optional - Alfresco can have any properties
   [:properties {:optional true} [:map-of :keyword :any]]
   [:aspectNames {:optional true} [:sequential :string]]
   [:createdAt {:optional true} :string]
   [:modifiedAt {:optional true} :string]
   [:createdByUser {:optional true} [:map [:id :string] [:displayName :string]]]
   [:modifiedByUser {:optional true} [:map [:id :string] [:displayName :string]]]])

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

;; --- SUNDAY WORSHIP SCHEMAS ---

(def worship-pdf-schema
  "Schema for a PDF document (bulletin or presentation)"
  [:map
   [:pdf/id :string]
   [:pdf/name :string]
   [:pdf/url :string]
   [:pdf/thumbnail [:maybe :string]]])

(def worship-video-schema
  "Schema for a video file (sermon recording)"
  [:map
   [:video/id :string]
   [:video/name :string]
   [:video/url :string]
   [:video/mime-type :string]])

(def worship-service-schema
  "Schema for a Sunday Worship service (one date folder)"
  [:map
   [:worship/date :string]                     ; e.g., "09-21-25"
   [:worship/date-formatted :string]           ; e.g., "September 21, 2025"
   [:worship/folder-id :string]
   [:worship/bulletin [:maybe worship-pdf-schema]]
   [:worship/presentation [:maybe worship-pdf-schema]]
   [:worship/video {:optional true} [:maybe worship-video-schema]]])

(def worship-list-schema
  "Schema for list of Sunday Worship services"
  [:sequential worship-service-schema])

;; --- BLOG SCHEMAS ---

(def blog-post-alfresco-schema
  "Schema for blog post from Alfresco (raw data)
   Based on discovered structure from Sites/swsdp/blog"
  [:map
   [:node-id :string]
   [:name :string]
   [:title [:maybe :string]]
   [:description [:maybe :string]]
   [:created :string]
   [:modified :string]
   [:author [:maybe :string]]
   [:aspects [:sequential :string]]
   [:properties [:map
                 [:cm:title {:optional true} [:maybe :string]]
                 [:cm:published {:optional true} [:maybe :string]]
                 [:cm:updated {:optional true} [:maybe :string]]
                 [:cm:taggable {:optional true} [:maybe [:or :string [:sequential :string]]]]
                 [:cm:lastThumbnailModification {:optional true} :any]]]])

(def blog-post-display-schema
  "Schema for blog post transformed for display"
  [:map
   [:blog/id :string]
   [:blog/slug :string]
   [:blog/title :string]
   [:blog/description [:maybe :string]]
   [:blog/excerpt [:maybe :string]]
   [:blog/published-at [:maybe :string]]
   [:blog/updated-at [:maybe :string]]
   [:blog/author [:maybe :string]]
   [:blog/tags {:optional true} [:sequential :string]]
   [:blog/thumbnail {:optional true} [:maybe :string]]])

(def blog-list-schema
  "Schema for list of blog posts"
  [:sequential blog-post-display-schema])

(def blog-detail-schema
  "Schema for full blog post with content"
  [:map
   [:blog/id :string]
   [:blog/slug :string]
   [:blog/title :string]
   [:blog/content :string]
   [:blog/description [:maybe :string]]
   [:blog/published-at [:maybe :string]]
   [:blog/updated-at [:maybe :string]]
   [:blog/author [:maybe :string]]
   [:blog/tags {:optional true} [:sequential :string]]
   [:blog/thumbnail {:optional true} [:maybe :string]]])

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

;; --- DATA TRANSFORMERS ---

(def string-transformer
  "Transformer for cleaning string data"
  (mt/transformer
   {:name :string-cleaner
    :decoders
    {:string {:compile (fn [_ _]
                        (fn [x]
                          (when x
                            (-> x
                                str
                                clojure.string/trim))))}}}))

(def html-strip-transformer
  "Transformer for stripping HTML tags from strings"
  (mt/transformer
   {:name :html-stripper
    :decoders
    {:string {:compile (fn [_ _]
                        (fn [x]
                          (when x
                            (-> x
                                str
                                (clojure.string/replace #"<[^>]+>" " ")
                                (clojure.string/replace #"\s+" " ")
                                clojure.string/trim))))}}}))

(def component-transformer
  "Combined transformer for component data (trim + strip HTML)"
  (mt/transformer
   string-transformer
   html-strip-transformer
   mt/strip-extra-keys-transformer))

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

   ;; Blog layer
   :blog/post-alfresco blog-post-alfresco-schema
   :blog/post-display blog-post-display-schema
   :blog/list blog-list-schema
   :blog/detail blog-detail-schema

   ;; Sunday Worship layer
   :worship/pdf worship-pdf-schema
   :worship/service worship-service-schema
   :worship/list worship-list-schema

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
  "Transform data using Malli transformers

   Usage:
     (transform :hero/output raw-data component-transformer)

   This will trim strings, strip HTML, and remove extra keys."
  [schema-key data transformer]
  (if-let [schema (get schema-registry schema-key)]
    (m/decode schema data transformer)
    data))

(defn validate-and-transform
  "Validate and transform data in one step

   Returns: {:valid? true :data cleaned-data} or {:valid? false :errors ...}"
  [schema-key data transformer]
  (if-let [schema (get schema-registry schema-key)]
    (let [;; First transform
          cleaned (m/decode schema data transformer)
          ;; Then validate
          result (m/validate schema cleaned)]
      (if result
        {:valid? true :data cleaned}
        {:valid? false
         :errors (me/humanize (m/explain schema cleaned))
         :schema-key schema-key
         :original-data data}))
    {:valid? false
     :errors {:schema "Schema not found in registry"}
     :schema-key schema-key}))

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