#!/usr/bin/env bb
;; XML → Malli Schema Converter
;; Converts Alfresco Content Model XML to Malli EDN schemas

(require '[clojure.java.io :as io]
         '[clojure.pprint :as pprint]
         '[clojure.string :as str]
         '[clojure.data.xml :as xml]
         '[clojure.zip :as zip]
         '[clojure.data.zip.xml :as zx])

;; --- TYPE MAPPING ---

(def alfresco-type->malli
  "Map Alfresco data types to Malli types"
  {"d:text"     :string
   "d:mltext"   :string
   "d:int"      :int
   "d:long"     :int
   "d:float"    :double
   "d:double"   :double
   "d:boolean"  :boolean
   "d:datetime" :string  ; ISO-8601 string
   "d:date"     :string
   "d:noderef"  :string
   "d:content"  :string})

;; --- XML PARSING HELPERS ---

(defn get-attr [loc attr]
  "Get attribute from element"
  (zx/attr loc attr))

(defn get-text [loc]
  "Get text content from element"
  (zx/text loc))

;; --- CONSTRAINT EXTRACTION ---

(defn extract-list-constraint-values
  "Extract allowed values from LIST constraint"
  [constraint-loc]
  (let [values (zx/xml-> constraint-loc :parameter :list :value)]
    (into #{} (map zx/text values))))

(defn parse-constraint
  "Parse constraint definition"
  [constraint-loc]
  (let [name (get-attr constraint-loc :name)
        type (get-attr constraint-loc :type)]
    (when (= "LIST" type)
      {:name name
       :type :list
       :allowed-values (extract-list-constraint-values constraint-loc)})))

;; --- PROPERTY EXTRACTION ---

(defn parse-property
  "Convert XML property to Malli schema field"
  [prop-loc constraints-map]
  (let [name (get-attr prop-loc :name)
        type-str (str/trim (or (first (zx/xml-> prop-loc :type zx/text)) "d:text"))
        mandatory? (= "true" (str/trim (or (first (zx/xml-> prop-loc :mandatory zx/text)) "false")))
        default-val (first (zx/xml-> prop-loc :default zx/text))

        ;; Check for constraints
        constraint-refs (zx/xml-> prop-loc :constraints :constraint (zx/attr :ref))

        ;; Get base Malli type
        malli-type (get alfresco-type->malli type-str :any)

        ;; Add enum if constraint exists
        final-type (if-let [constraint-ref (first constraint-refs)]
                     (if-let [constraint (get constraints-map constraint-ref)]
                       [:enum {:description (str "Allowed: " (:allowed-values constraint))}
                        (vec (sort (:allowed-values constraint)))]
                       malli-type)
                     malli-type)]

    {:name name
     :alfresco-type type-str
     :malli-type malli-type
     :mandatory? mandatory?
     :default default-val
     :constraints constraint-refs
     :final-schema (if mandatory?
                     [(keyword name) final-type]
                     [(keyword name) {:optional true} final-type])}))

;; --- TYPE/ASPECT EXTRACTION ---

(defn parse-type
  "Parse type definition to Malli schema"
  [type-loc constraints-map]
  (let [name (get-attr type-loc :name)
        title (str/trim (or (first (zx/xml-> type-loc :title zx/text)) ""))
        parent (first (zx/xml-> type-loc :parent zx/text))

        ;; Find properties
        prop-locs (zx/xml-> type-loc :properties :property)

        properties (map #(parse-property % constraints-map) prop-locs)
        schema-fields (map :final-schema properties)]

    {:type-id name
     :title title
     :parent parent
     :properties properties
     :malli-schema (into [:map {:description title}] schema-fields)}))

(defn parse-aspect
  "Parse aspect definition to Malli schema"
  [aspect-loc constraints-map]
  (let [name (get-attr aspect-loc :name)
        title (str/trim (or (first (zx/xml-> aspect-loc :title zx/text)) ""))

        ;; Find properties
        prop-locs (zx/xml-> aspect-loc :properties :property)

        properties (map #(parse-property % constraints-map) prop-locs)
        schema-fields (map :final-schema properties)]

    {:aspect-id name
     :title title
     :properties properties
     :malli-schema (into [:map {:description title}] schema-fields)}))

;; --- MAIN CONVERSION ---

(defn parse-alfresco-model
  "Parse complete Alfresco content model XML"
  [xml-file]
  (let [;; Read file and skip any leading comments/whitespace before <?xml
        xml-content (slurp xml-file)
        clean-xml (if (str/starts-with? (str/trim xml-content) "<!--")
                    (str/replace-first xml-content #"(?s)<!--.*?-->\s*" "")
                    xml-content)
        ;; Parse with namespace-aware=false to ignore namespaces
        root (xml/parse-str clean-xml :namespace-aware false)
        loc (zip/xml-zip root)
        model-name (get-attr loc :name)

        ;; Extract constraints (direct children, not nested in :constraints)
        constraint-locs (zx/xml-> loc :constraints :constraint)
        constraints (into {} (keep (fn [c]
                                     (when-let [parsed (parse-constraint c)]
                                       [(:name parsed) parsed]))
                                   constraint-locs))

        ;; Extract types (direct children, not nested in :types wrapper)
        type-locs (zx/xml-> loc :types :type)
        types (map #(parse-type % constraints) type-locs)

        ;; Extract aspects (direct children, not nested in :aspects wrapper)
        aspect-locs (zx/xml-> loc :aspects :aspect)
        aspects (map #(parse-aspect % constraints) aspect-locs)

        ;; Debug output
        _ (println "   DEBUG: Found" (count constraint-locs) "constraint locs")
        _ (println "   DEBUG: Found" (count type-locs) "type locs")
        _ (println "   DEBUG: Found" (count aspect-locs) "aspect locs")]

    {:model-name model-name
     :constraints constraints
     :types (into {} (map (juxt :type-id identity) types))
     :aspects (into {} (map (juxt :aspect-id identity) aspects))
     :malli-schemas {:types (into {} (map (juxt :type-id :malli-schema) types))
                     :aspects (into {} (map (juxt :aspect-id :malli-schema) aspects))}}))

;; --- OUTPUT GENERATION ---

(defn generate-malli-namespace
  "Generate Clojure namespace with Malli schemas"
  [parsed-model]
  (let [{:keys [types aspects]} (:malli-schemas parsed-model)]
    (str
     ";; AUTO-GENERATED - DO NOT EDIT MANUALLY\n"
     ";; Generated: " (java.time.Instant/now) "\n"
     ";; From: Alfresco Content Model XML\n"
     ";; Run: bb workbench/bb/xml-to-malli.clj to regenerate\n\n"

     "(ns mtz-cms.validation.content-model-schemas\n"
     "  \"Auto-generated Malli schemas from Alfresco Content Model\"\n"
     "  (:require [malli.core :as m]\n"
     "            [malli.util :as mu]))\n\n"

     ";; ========================================\n"
     ";; TYPE SCHEMAS\n"
     ";; ========================================\n\n"

     (str/join "\n\n"
               (for [[type-id schema] types]
                 (let [type-name (last (str/split (name type-id) #":"))]
                   (str "(def " type-name "-schema\n"
                        "  \"Schema for " type-id "\"\n"
                        "  " (pr-str schema) ")"))))
     "\n\n"

     ";; ========================================\n"
     ";; ASPECT SCHEMAS\n"
     ";; ========================================\n\n"

     (str/join "\n\n"
               (for [[aspect-id schema] aspects]
                 (let [aspect-name (last (str/split (name aspect-id) #":"))]
                   (str "(def " aspect-name "-aspect-schema\n"
                        "  \"Schema for " aspect-id "\"\n"
                        "  " (pr-str schema) ")"))))
     "\n\n"

     ";; ========================================\n"
     ";; SCHEMA REGISTRY\n"
     ";; ========================================\n\n"

     "(def schema-registry\n"
     "  \"Complete schema registry\"\n"
     "  {:types " (pr-str types) "\n"
     "   :aspects " (pr-str aspects) "})\n\n"

     "(defn get-type-schema [type-id]\n"
     "  (get-in schema-registry [:types type-id]))\n\n"

     "(defn get-aspect-schema [aspect-id]\n"
     "  (get-in schema-registry [:aspects aspect-id]))\n")))

;; --- CLI ---

(defn -main [& args]
  (let [input-file (or (first args) "../src/mtz_cms/mtz.xml")
        output-edn (or (second args) "../generated-model/malli-schemas.edn")
        output-clj (or (nth args 2 nil) "../src/mtz_cms/validation/content_model_schemas.clj")]

    (println "🔄 XML → Malli Converter")
    (println "   Input:  " input-file)
    (println "   Output: " output-edn)
    (when output-clj
      (println "   Code:   " output-clj))

    (try
      ;; Parse XML
      (println "\n📥 Parsing XML...")
      (let [parsed (parse-alfresco-model input-file)]

        (println "   ✓ Model:" (:model-name parsed))
        (println "   ✓ Types:" (count (:types parsed)))
        (println "   ✓ Aspects:" (count (:aspects parsed)))
        (println "   ✓ Constraints:" (count (:constraints parsed)))

        ;; Save EDN
        (println "\n💾 Saving EDN schema...")
        (io/make-parents output-edn)
        (with-open [writer (io/writer output-edn)]
          (pprint/pprint parsed writer))
        (println "   ✓" output-edn)

        ;; Generate Clojure code
        (when output-clj
          (println "\n💾 Generating Clojure namespace...")
          (io/make-parents output-clj)
          (spit output-clj (generate-malli-namespace parsed))
          (println "   ✓" output-clj))

        (println "\n✅ Success!")
        (println "\n📊 Schema Summary:")
        (doseq [[type-id type-data] (:types parsed)]
          (println "   •" type-id "(" (count (:properties type-data)) "properties)"))
        (doseq [[aspect-id aspect-data] (:aspects parsed)]
          (println "   •" aspect-id "(" (count (:properties aspect-data)) "properties)")))

      (catch Exception e
        (println "\n❌ Error:" (.getMessage e))
        (.printStackTrace e)
        (System/exit 1)))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
