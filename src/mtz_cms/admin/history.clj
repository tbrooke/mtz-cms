(ns mtz-cms.admin.history
  (:require
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.admin.layout :as layout]
   [mtz-cms.config.core :as config]
   [ring.util.response :as response]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

(def ^:private sections-folder (config/get-node-id :history-sections))
(defn- ctx [] {})

(def ^:private era-options
  ["" "Pre-Civil War" "Post-Civil War" "Early 20th Century"
   "Mid-Century" "Contemporary"])

(def ^:private research-status-options
  ["Raw" "In Progress" "Needs Review" "Complete"])

(def ^:private transcription-status-options
  ["Not Started" "In Progress" "Complete" "Verified"])

(defn- select-input [id name options current]
  [:select {:id id :name name
            :class "border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"}
   (map (fn [opt]
          [:option {:value opt :selected (= opt current)} opt])
        options)])

;; --- LIST ---

(defn- status-badge [status]
  (let [color (case status
                "Complete"    "bg-green-100 text-green-700"
                "In Progress" "bg-blue-100 text-blue-700"
                "Needs Review" "bg-yellow-100 text-yellow-700"
                "bg-slate-100 text-slate-600")]
    [:span {:class (str "inline-block text-xs px-2 py-0.5 rounded-full " color)} (or status "Raw")]))

(defn- section-row [entry]
  (let [id    (get-in entry [:entry :id])
        props (get-in entry [:entry :properties])
        title (or (:cm:title props) (get-in entry [:entry :name]))
        era   (or (:mtzion:era props) "—")
        decade (or (:mtzion:decade props) "—")
        rs    (:mtzion:researchStatus props)
        ts    (:mtzion:transcriptionStatus props)]
    [:tr {:class "border-b border-slate-100 hover:bg-slate-50"}
     [:td {:class "py-3 px-4"}
      [:div {:class "font-medium text-slate-800 text-sm"} title]]
     [:td {:class "py-3 px-4 text-sm text-slate-500"} era]
     [:td {:class "py-3 px-4 text-sm text-slate-500"} decade]
     [:td {:class "py-3 px-4"} (status-badge rs)]
     [:td {:class "py-3 px-4 text-xs text-slate-400"} (or ts "—")]
     [:td {:class "py-3 px-4"}
      [:a {:href  (str "/admin/history/edit/" id)
           :class "text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 px-3 py-1 rounded-md"}
       "Edit"]]]))

(defn history-list-handler [request]
  (let [result (alfresco/get-node-children
                (ctx) sections-folder
                {:include "properties" :maxItems 200 :orderBy "cm:name ASC"})]
    (layout/html-response
     (layout/admin-page
      "History Archive"
      (if (:success result)
        (let [items (get-in result [:data :list :entries])]
          [:div
           [:div {:class "flex items-center justify-between mb-6"}
            [:div
             [:h1 {:class "text-xl font-bold text-slate-800"} "History Archive"]
             [:p {:class "text-sm text-slate-500 mt-0.5"} "Handbook Sections"]]
            [:a {:href  "/admin/history/upload"
                 :class "bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg"}
             "+ Upload Document"]]
           [:div {:class "bg-white rounded-xl shadow-sm overflow-hidden"}
            [:table {:class "w-full"}
             [:thead
              [:tr {:class "border-b border-slate-200 bg-slate-50"}
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider w-64"} "Title"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Era"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Decade"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Research"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Transcription"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} ""]]]
             [:tbody
              (if (seq items)
                (map section-row items)
                [:tr [:td {:colspan 6 :class "py-10 text-center text-slate-400 text-sm"}
                      "No documents found."]])]]]])
        [:p {:class "text-red-600 text-sm"} "Could not load documents from Alfresco."])
      :current-path "/admin/history"))))

;; --- EDIT FORM ---

(defn- history-edit-form [node-id props content]
  (layout/admin-page
   "Edit Document"
   [:div
    [:div {:class "flex items-center gap-2 mb-6 text-sm"}
     [:a {:href "/admin/history" :class "text-slate-400 hover:text-slate-600"} "History"]
     [:span {:class "text-slate-300"} "/"]
     [:span {:class "text-slate-600"} "Edit Document"]]

    [:form {:method "post" :action (str "/admin/history/save/" node-id) :class "space-y-6"}

     ;; Title
     [:div
      [:label {:for "title" :class "block text-sm font-medium text-slate-700 mb-1"} "Title"]
      [:input {:type "text" :id "title" :name "title" :value (or (:cm:title props) "")
               :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]

     ;; Archival metadata section
     [:div {:class "bg-slate-50 rounded-xl p-5 space-y-4"}
      [:h3 {:class "text-sm font-semibold text-slate-700 mb-3"} "Archival Metadata"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div
        [:label {:for "era" :class "block text-xs font-medium text-slate-600 mb-1"} "Era"]
        (select-input "era" "era" era-options (or (:mtzion:era props) ""))]
       [:div
        [:label {:for "decade" :class "block text-xs font-medium text-slate-600 mb-1"} "Decade"]
        [:input {:type "text" :id "decade" :name "decade"
                 :value (or (:mtzion:decade props) "")
                 :placeholder "e.g. 1980s"
                 :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div
        [:label {:for "date-range" :class "block text-xs font-medium text-slate-600 mb-1"} "Date Range"]
        [:input {:type "text" :id "date-range" :name "date-range"
                 :value (or (:mtzion:dateRange props) "")
                 :placeholder "e.g. 1920–1935"
                 :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]
       [:div
        [:label {:for "research-status" :class "block text-xs font-medium text-slate-600 mb-1"} "Research Status"]
        (select-input "research-status" "research-status"
                      research-status-options
                      (or (:mtzion:researchStatus props) "Raw"))]]
      [:div
       [:label {:for "significance" :class "block text-xs font-medium text-slate-600 mb-1"} "Significance / Notes"]
       [:textarea {:id "significance" :name "significance" :rows 2
                   :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}
        (or (:mtzion:significance props) "")]]
      [:div {:class "flex items-center gap-2"}
       [:input {:type "checkbox" :id "circa" :name "circa" :value "true"
                :checked (= true (:mtzion:circa props))}]
       [:label {:for "circa" :class "text-xs text-slate-600"} "Date is approximate (circa)"]]]

     ;; Provenance section
     [:div {:class "bg-slate-50 rounded-xl p-5 space-y-4"}
      [:h3 {:class "text-sm font-semibold text-slate-700 mb-3"} "Provenance"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div
        [:label {:for "original-format" :class "block text-xs font-medium text-slate-600 mb-1"} "Original Format"]
        [:input {:type "text" :id "original-format" :name "original-format"
                 :value (or (:mtzion:originalFormat props) "")
                 :placeholder "e.g. Paper Document, Photograph"
                 :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]
       [:div
        [:label {:for "condition" :class "block text-xs font-medium text-slate-600 mb-1"} "Condition"]
        [:input {:type "text" :id "condition" :name "condition"
                 :value (or (:mtzion:condition props) "")
                 :placeholder "e.g. Good, Fair, Fragile"
                 :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div
        [:label {:for "donor-name" :class "block text-xs font-medium text-slate-600 mb-1"} "Donor Name"]
        [:input {:type "text" :id "donor-name" :name "donor-name"
                 :value (or (:mtzion:donorName props) "")
                 :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]
       [:div
        [:label {:for "storage-location" :class "block text-xs font-medium text-slate-600 mb-1"} "Storage Location"]
        [:input {:type "text" :id "storage-location" :name "storage-location"
                 :value (or (:mtzion:storageLocation props) "")
                 :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]]]

     ;; Transcription section
     [:div {:class "bg-slate-50 rounded-xl p-5 space-y-4"}
      [:h3 {:class "text-sm font-semibold text-slate-700 mb-3"} "Transcription"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div
        [:label {:for "transcription-status" :class "block text-xs font-medium text-slate-600 mb-1"} "Status"]
        (select-input "transcription-status" "transcription-status"
                      transcription-status-options
                      (or (:mtzion:transcriptionStatus props) "Not Started"))]
       [:div
        [:label {:for "transcription-notes" :class "block text-xs font-medium text-slate-600 mb-1"} "Notes"]
        [:input {:type "text" :id "transcription-notes" :name "transcription-notes"
                 :value (or (:mtzion:transcriptionNotes props) "")
                 :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]]

      ;; Document content (TipTap)
      [:div
       [:label {:class "block text-xs font-medium text-slate-600 mb-1"} "Document Text"]
       [:input {:type "hidden" :name "body" :id "body-content" :value (or content "")}]
       [:div {:id                "tiptap-editor"
              :data-tiptap-input "body-content"
              :class             "border border-slate-300 rounded-lg p-3 bg-white min-h-64 text-sm"}]]]

     [:div {:class "flex items-center gap-3 pt-2"}
      [:button {:type  "submit"
                :class "bg-blue-600 hover:bg-blue-700 text-white font-medium px-5 py-2 rounded-lg text-sm"}
       "Save Changes"]
      [:a {:href "/admin/history" :class "text-slate-500 hover:text-slate-700 text-sm px-4 py-2"}
       "Cancel"]]]]

   :current-path "/admin/history"
   :with-editor? true))

;; --- UPLOAD FORM ---

(defn history-upload-handler [_request]
  (layout/html-response
   (layout/admin-page
    "Upload Document"
    [:div
     [:div {:class "flex items-center gap-2 mb-6 text-sm"}
      [:a {:href "/admin/history" :class "text-slate-400 hover:text-slate-600"} "History"]
      [:span {:class "text-slate-300"} "/"]
      [:span {:class "text-slate-600"} "Upload Document"]]

     [:form {:method "post" :action "/admin/history/upload-submit"
             :enctype "multipart/form-data" :class "space-y-5"}

      [:div
       [:label {:for "title" :class "block text-sm font-medium text-slate-700 mb-1"} "Title *"]
       [:input {:type "text" :id "title" :name "title" :required true
                :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]

      [:div
       [:label {:for "file" :class "block text-sm font-medium text-slate-700 mb-1"} "File *"]
       [:input {:type "file" :id "file" :name "file" :required true
                :class "block w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-medium file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"}]
       [:p {:class "text-xs text-slate-400 mt-1"} "PDF, images, Word documents, or text files"]]

      [:div {:class "grid grid-cols-2 gap-4"}
       [:div
        [:label {:for "era" :class "block text-sm font-medium text-slate-700 mb-1"} "Era"]
        (select-input "era" "era" era-options "")]
       [:div
        [:label {:for "decade" :class "block text-sm font-medium text-slate-700 mb-1"} "Decade"]
        [:input {:type "text" :id "decade" :name "decade" :placeholder "e.g. 1940s"
                 :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]]

      [:div
       [:label {:for "original-format" :class "block text-sm font-medium text-slate-700 mb-1"} "Original Format"]
       [:input {:type "text" :id "original-format" :name "original-format"
                :placeholder "e.g. Photograph, Letter, Minutes"
                :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]

      [:div {:class "flex items-center gap-3 pt-2"}
       [:button {:type  "submit"
                 :class "bg-blue-600 hover:bg-blue-700 text-white font-medium px-5 py-2 rounded-lg text-sm"}
        "Upload"]
       [:a {:href "/admin/history" :class "text-slate-500 hover:text-slate-700 text-sm px-4 py-2"}
        "Cancel"]]]]
    :current-path "/admin/history")))

;; --- HANDLERS ---

(defn history-edit-handler [request]
  (let [id             (get-in request [:path-params :id])
        node-result    (alfresco/get-node (ctx) id)
        content-result (alfresco/get-node-content (ctx) id)]
    (if (:success node-result)
      (let [props   (get-in node-result [:data :entry :properties])
            content (when (:success content-result)
                      (String. ^bytes (:data content-result) "UTF-8"))]
        (layout/html-response (history-edit-form id props (or content ""))))
      {:status 404 :headers {"Content-Type" "text/plain"} :body "Document not found"})))

(defn history-save-handler [request]
  (let [id     (get-in request [:path-params :id])
        p      (:params request)
        circa? (= "true" (:circa p))
        result (alfresco/update-node
                (ctx) id
                {:properties
                 (cond-> {"cm:title"                    (:title p)
                          "mtzion:era"                 (:era p)
                          "mtzion:decade"              (:decade p)
                          "mtzion:dateRange"           (:date-range p)
                          "mtzion:circa"               circa?
                          "mtzion:significance"        (:significance p)
                          "mtzion:researchStatus"      (:research-status p)
                          "mtzion:transcriptionStatus" (:transcription-status p)
                          "mtzion:transcriptionNotes"  (:transcription-notes p)
                          "mtzion:originalFormat"      (:original-format p)
                          "mtzion:condition"           (:condition p)
                          "mtzion:donorName"           (:donor-name p)
                          "mtzion:storageLocation"     (:storage-location p)})})]
    (when (and (:success result) (not (str/blank? (:body p))))
      (alfresco/upload-text-content (ctx) id (:body p) "text/markdown"))
    (if (:success result)
      (response/redirect (str "/admin/history/edit/" id) :see-other)
      (do
        (log/error "Failed to save history document:" (:error result))
        {:status 500 :headers {"Content-Type" "text/plain"} :body "Failed to save"}))))

(defn history-upload-submit-handler [request]
  (let [p           (:params request)
        file-param  (:file p)
        title       (:title p)
        era         (:era p)
        decade      (:decade p)
        orig-format (:original-format p)]
    (if (nil? file-param)
      {:status 400 :headers {"Content-Type" "text/plain"} :body "No file uploaded"}
      (let [filename     (:filename file-param)
            content-type (:content-type file-param)
            tempfile     (:tempfile file-param)
            file-bytes   (when tempfile (java.nio.file.Files/readAllBytes (.toPath tempfile)))
            result       (alfresco/upload-file
                          (ctx) sections-folder filename file-bytes content-type)]
        (if (:success result)
          (let [node-id (get-in result [:data :entry :id])]
            ;; Ensure mtzion aspects and set initial properties
            (alfresco/add-aspects (ctx) node-id
                                  ["mtzion:archival" "mtzion:transcription" "mtzion:provenance"])
            (alfresco/update-node
             (ctx) node-id
             {:properties (cond-> {"cm:title"               title
                                   "mtzion:researchStatus"  "Raw"
                                   "mtzion:transcriptionStatus" "Not Started"
                                   "mtzion:originalFormat"  orig-format}
                            era    (assoc "mtzion:era" era)
                            decade (assoc "mtzion:decade" decade))})
            (response/redirect (str "/admin/history/edit/" node-id) :see-other))
          (do
            (log/error "Upload failed:" (:error result))
            {:status 500 :headers {"Content-Type" "text/plain"} :body "Upload failed"}))))))
