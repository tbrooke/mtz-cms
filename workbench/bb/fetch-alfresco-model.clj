#!/usr/bin/env bb
;; Fetch Alfresco Content Model via API
;; Retrieves actual deployed types, aspects, and sample node data

(require '[babashka.curl :as curl]
         '[cheshire.core :as json]
         '[clojure.java.io :as io]
         '[clojure.pprint :as pprint]
         '[clojure.string :as str])

;; --- CONFIGURATION ---
(def alfresco-host "http://localhost:8080")
(def alfresco-user "admin")
(def alfresco-pass "admin")
(def api-base (str alfresco-host "/alfresco/api/-default-/public/alfresco/versions/1"))

;; Known node IDs from AI_CONTEXT.md
(def known-nodes
  {:home-hero "39985c5c-201a-42f6-985c-5c201a62f6d8"
   :home-feature1 "264ab06c-984e-4f64-8ab0-6c984eaf6440"
   :home-feature2 "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89"})

;; --- HTTP CLIENT ---

(defn get-json
  "Fetch JSON from Alfresco API"
  [path]
  (try
    (let [url (str api-base path)
          _ (println "   GET" url)
          resp (curl/get url {:basic-auth [alfresco-user alfresco-pass]
                             :throw false})]
      (if (= 200 (:status resp))
        (json/parse-string (:body resp) true)
        (do
          (println "   ❌ Status:" (:status resp))
          nil)))
    (catch Exception e
      (println "   ❌ Error:" (.getMessage e))
      nil)))

;; --- FETCH MODEL DEFINITIONS ---

(defn fetch-types
  "Fetch all content types from Alfresco"
  [& {:keys [max-items] :or {max-items 100}}]
  (println "\n📥 Fetching content types...")
  (when-let [data (get-json (str "/types?maxItems=" max-items))]
    (let [entries (get-in data [:list :entries])]
      (println "   ✓ Found" (count entries) "types")
      entries)))

(defn fetch-type-details
  "Fetch detailed information about a specific type"
  [type-id]
  (println "\n📥 Fetching type details:" type-id)
  (get-json (str "/types/" type-id)))

(defn fetch-aspects
  "Fetch all aspects from Alfresco"
  [& {:keys [max-items] :or {max-items 100}}]
  (println "\n📥 Fetching aspects...")
  (when-let [data (get-json (str "/aspects?maxItems=" max-items))]
    (let [entries (get-in data [:list :entries])]
      (println "   ✓ Found" (count entries) "aspects")
      entries)))

(defn fetch-aspect-details
  "Fetch detailed information about a specific aspect"
  [aspect-id]
  (println "\n📥 Fetching aspect details:" aspect-id)
  (get-json (str "/aspects/" aspect-id)))

;; --- FETCH NODE DATA ---

(defn fetch-node
  "Fetch node data including properties"
  [node-id]
  (println "\n📥 Fetching node:" node-id)
  (get-json (str "/nodes/" node-id)))

(defn fetch-node-children
  "Fetch children of a node"
  [node-id]
  (println "\n📥 Fetching children of:" node-id)
  (when-let [data (get-json (str "/nodes/" node-id "/children"))]
    (get-in data [:list :entries])))

;; --- ANALYSIS ---

(defn analyze-node
  "Analyze a node to understand its structure"
  [node-data]
  (when node-data
    (let [entry (:entry node-data)
          node-type (:nodeType entry)
          aspects (:aspectNames entry)
          properties (:properties entry)]
      {:node-id (:id entry)
       :name (:name entry)
       :node-type node-type
       :aspects aspects
       :properties properties
       :property-keys (keys properties)})))

(defn summarize-types
  "Create summary of types with their properties"
  [types]
  (for [type-entry types]
    (let [type-data (:entry type-entry)
          type-id (:id type-data)
          properties (:properties type-data)]
      {:type-id type-id
       :title (:title type-data)
       :parent (:parentId type-data)
       :property-count (count properties)
       :property-names (map :id properties)})))

(defn summarize-aspects
  "Create summary of aspects with their properties"
  [aspects]
  (for [aspect-entry aspects]
    (let [aspect-data (:entry aspect-entry)
          aspect-id (:id aspect-data)
          properties (:properties aspect-data)]
      {:aspect-id aspect-id
       :title (:title aspect-data)
       :property-count (count properties)
       :property-names (map :id properties)})))

;; --- OUTPUT ---

(defn save-model-data
  "Save fetched model data to file"
  [data output-file]
  (println "\n💾 Saving to" output-file)
  (io/make-parents output-file)
  (with-open [writer (io/writer output-file)]
    (pprint/pprint data writer))
  (println "   ✓ Saved"))

;; --- MAIN ---

(defn -main [& args]
  (println "🔄 Alfresco Model Fetcher")
  (println "   Connecting to" alfresco-host)
  (println)

  (try
    ;; Fetch types
    (let [types (fetch-types :max-items 50)
          type-summary (summarize-types types)

          ;; Fetch aspects
          aspects (fetch-aspects :max-items 50)
          aspect-summary (summarize-aspects aspects)

          ;; Fetch known nodes for analysis
          hero-node (fetch-node (:home-hero known-nodes))
          hero-analysis (analyze-node hero-node)

          feature1-node (fetch-node (:home-feature1 known-nodes))
          feature1-analysis (analyze-node feature1-node)

          feature2-node (fetch-node (:home-feature2 known-nodes))
          feature2-analysis (analyze-node feature2-node)

          ;; Get children to understand structure
          hero-children (fetch-node-children (:home-hero known-nodes))

          ;; Compile results
          results {:fetched-at (str (java.time.Instant/now))
                   :alfresco-host alfresco-host
                   :types {:count (count types)
                           :summary type-summary
                           :full-data types}
                   :aspects {:count (count aspects)
                             :summary aspect-summary
                             :full-data aspects}
                   :sample-nodes {:hero {:node-data hero-node
                                        :analysis hero-analysis
                                        :children hero-children}
                                  :feature1 {:node-data feature1-node
                                            :analysis feature1-analysis}
                                  :feature2 {:node-data feature2-node
                                            :analysis feature2-analysis}}}]

      ;; Save results
      (save-model-data results "../generated-model/alfresco-live-model.edn")

      ;; Print summary
      (println "\n✅ Success!")
      (println "\n📊 Model Summary:")
      (println "   Types:" (count types))
      (println "   Aspects:" (count aspects))
      (println)
      (println "📊 Sample Node Analysis:")
      (println)
      (println "   Hero Node:")
      (println "     Type:" (:node-type hero-analysis))
      (println "     Aspects:" (:aspects hero-analysis))
      (println "     Properties:" (count (:property-keys hero-analysis)))
      (println "     Children:" (count hero-children))
      (println)
      (println "   Feature 1 Node:")
      (println "     Type:" (:node-type feature1-analysis))
      (println "     Aspects:" (:aspects feature1-analysis))
      (println "     Properties:" (count (:property-keys feature1-analysis)))
      (println)
      (println "   Feature 2 Node:")
      (println "     Type:" (:node-type feature2-analysis))
      (println "     Aspects:" (:aspects feature2-analysis))
      (println "     Properties:" (count (:property-keys feature2-analysis)))
      (println)
      (println "📄 Full data saved to: generated-model/alfresco-live-model.edn"))

    (catch Exception e
      (println "\n❌ Error:" (.getMessage e))
      (.printStackTrace e)
      (System/exit 1))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
