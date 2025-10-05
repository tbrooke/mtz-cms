(ns mtz-cms.alfresco.sunday-worship-resolvers
  "Pathom resolvers for Sunday Worship functionality

   Handles:
   - Sunday Worship service list retrieval
   - PDF identification (bulletin vs presentation)
   - Date formatting and sorting"
  (:require
   [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.validation.schemas :as schemas]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- CONFIGURATION ---

(def sunday-worship-folder-id
  "Sunday Worship folder ID"
  "a2e853fa-28f5-42d9-a853-fa28f522d918")

(def bulletin-tag-id
  "Bulletin tag category node ID"
  "559084bc-1b4b-467c-9084-bc1b4b067c69")

;; --- HELPER FUNCTIONS ---

(defn has-bulletin-tag?
  "Check if PDF has the bulletin tag"
  [pdf-node]
  (let [tags (get-in pdf-node [:properties :cm:taggable] [])]
    (some #(= bulletin-tag-id %) tags)))

(defn format-date
  "Format date from folder name to human-readable
   Input: '09-21-25'
   Output: 'September 21, 2025'"
  [date-str]
  (try
    (let [[month day year] (str/split date-str #"-")
          month-num (Integer/parseInt month)
          month-names ["" "January" "February" "March" "April" "May" "June"
                      "July" "August" "September" "October" "November" "December"]
          month-name (get month-names month-num "")
          full-year (str "20" year)]
      (str month-name " " (Integer/parseInt day) ", " full-year))
    (catch Exception e
      (log/warn "Could not format date:" date-str)
      date-str)))

(defn identify-bulletin
  "Identify which PDF is the bulletin
   Priority:
   1. Has bulletin tag
   2. Shorter filename (fallback)"
  [pdfs]
  (or
   ;; Try to find by tag first
   (first (filter has-bulletin-tag? pdfs))
   ;; Fallback: assume shorter filename is bulletin
   (first (sort-by #(count (:name %)) pdfs))))

(defn identify-presentation
  "Get the other PDF (not bulletin)"
  [pdfs bulletin-pdf]
  (first (filter #(not= (:id %) (:id bulletin-pdf)) pdfs)))

(defn pdf-to-map
  "Convert PDF node to our data structure"
  [pdf-node]
  (when pdf-node
    {:pdf/id (:id pdf-node)
     :pdf/name (:name pdf-node)
     :pdf/url (str "/api/pdf/" (:id pdf-node))
     :pdf/thumbnail (str "/api/image/" (:id pdf-node))}))

(defn video-to-map
  "Convert video node to our data structure"
  [video-node]
  (when video-node
    {:video/id (:id video-node)
     :video/name (:name video-node)
     :video/url (str "/api/media/" (:id video-node))
     :video/mime-type (get-in video-node [:content :mimeType])}))

(defn process-date-folder
  "Process a single date folder into worship service data"
  [ctx folder-entry]
  (let [folder-node (:entry folder-entry)
        folder-id (:id folder-node)
        date-str (:name folder-node)]

    (log/debug "Processing date folder:" date-str)

    ;; Get children in this folder
    (let [children-result (alfresco/get-node-children ctx folder-id
                                                        {:include "properties,aspectNames"})
          children (when (:success children-result)
                    (get-in children-result [:data :list :entries]))

          ;; Filter to PDFs
          pdf-nodes (filter #(and (get-in % [:entry :isFile])
                                 (= (get-in % [:entry :content :mimeType]) "application/pdf"))
                           children)
          pdf-entries (map :entry pdf-nodes)

          ;; Filter to video files (optional)
          video-nodes (filter #(and (get-in % [:entry :isFile])
                                   (let [mime (get-in % [:entry :content :mimeType])]
                                     (and mime (str/starts-with? mime "video/"))))
                             children)
          video-entry (first (map :entry video-nodes))]

      (log/debug "Found" (count children) "children," (count pdf-entries) "PDFs," (count video-nodes) "videos")
      (when video-entry
        (log/info "Video found:" (:name video-entry) "ID:" (:id video-entry)))

      (if (seq pdf-entries)
        (let [bulletin-pdf (identify-bulletin pdf-entries)
              presentation-pdf (identify-presentation pdf-entries bulletin-pdf)]

          (log/info "  ✅" date-str "-"
                   "Bulletin:" (:name bulletin-pdf)
                   "Presentation:" (:name presentation-pdf)
                   (when video-entry (str "Video: " (:name video-entry))))

          {:worship/date date-str
           :worship/date-formatted (format-date date-str)
           :worship/folder-id folder-id
           :worship/bulletin (pdf-to-map bulletin-pdf)
           :worship/presentation (pdf-to-map presentation-pdf)
           :worship/video (when video-entry (video-to-map video-entry))})

        ;; No PDFs in this folder
        (do
          (log/warn "  ⚠️ No PDFs in folder:" date-str)
          {:worship/date date-str
           :worship/date-formatted (format-date date-str)
           :worship/folder-id folder-id
           :worship/bulletin nil
           :worship/presentation nil})))))

;; --- PATHOM RESOLVERS ---

(defresolver sunday-worship-list-resolver
  "Get list of Sunday Worship services

   Returns sorted list (newest first) of worship services with PDFs"
  [{:keys [ctx]} _]
  {::pco/output [{:worship/list [:worship/date
                                 :worship/date-formatted
                                 :worship/folder-id
                                 :worship/bulletin
                                 :worship/presentation]}]}
  (try
    (log/info "📅 Fetching Sunday Worship services...")

    (let [children-result (alfresco/get-node-children ctx sunday-worship-folder-id)]

      (if (:success children-result)
        (let [entries (get-in children-result [:data :list :entries])
              ;; Filter to folders only (date folders)
              date-folders (filter #(get-in % [:entry :isFolder]) entries)

              ;; Process each date folder
              services (map #(process-date-folder ctx %) date-folders)

              ;; Filter out folders with no PDFs
              valid-services (filter #(or (:worship/bulletin %)
                                         (:worship/presentation %))
                                    services)

              ;; Sort by date (newest first)
              sorted-services (sort-by :worship/date
                                      (fn [a b] (compare b a))
                                      valid-services)]

          (log/info "✅ Retrieved" (count sorted-services) "worship services")

          {:worship/list sorted-services})

        (do
          (log/error "❌ Failed to fetch Sunday Worship folders")
          {:worship/list []})))

    (catch Exception e
      (log/error "❌ Sunday Worship list resolver error:" (.getMessage e))
      {:worship/list []})))

(defresolver sunday-worship-detail-resolver
  "Get Sunday Worship service by date

   Input: {:worship/date \"09-21-25\"}
   Output: Full service details"
  [{:keys [ctx]} {:worship/keys [date]}]
  {::pco/input [:worship/date]
   ::pco/output [:worship/date-formatted
                 :worship/folder-id
                 :worship/bulletin
                 :worship/presentation
                 :worship/video]}
  (try
    (log/info "📄 Fetching Sunday Worship service for date:" date)

    ;; Get all services and find matching one
    (let [list-result (sunday-worship-list-resolver {:ctx ctx} {})
          services (:worship/list list-result)
          matching-service (first (filter #(= (:worship/date %) date) services))]

      (if matching-service
        (do
          (log/info "✅ Found worship service:" (:worship/date-formatted matching-service))
          matching-service)

        (do
          (log/warn "⚠️ No worship service found for date:" date)
          {:worship/date-formatted ""
           :worship/folder-id nil
           :worship/bulletin nil
           :worship/presentation nil})))

    (catch Exception e
      (log/error "❌ Sunday Worship detail resolver error:" (.getMessage e))
      {:worship/date-formatted ""
       :worship/folder-id nil
       :worship/bulletin nil
       :worship/presentation nil})))

;; --- RESOLVER COLLECTION ---

(def sunday-worship-resolvers
  "Collection of all Sunday Worship resolvers"
  [sunday-worship-list-resolver
   sunday-worship-detail-resolver])
