(ns mtz-cms.config.core
  "Configuration for Mount Zion CMS")

;; --- ALFRESCO CONFIGURATION ---

(def alfresco-config
  "Default Alfresco configuration"
  {:base-url "http://localhost:8080"
   :username "admin"
   :password "admin"})

;; --- PAGE NODE MAPPING ---

;; Actual node IDs discovered from the Mount Zion Alfresco instance
(def page-node-mapping
  "Mapping from page keys to Alfresco node IDs"
  {;; Main pages - using actual node IDs from the Mt Zion site
   :home "9faac48b-6c77-4266-aac4-8b6c7752668a"        ; Home Page folder
   :about "8158a6aa-dbd7-4f5b-98a6-aadbd72f5b3b"       ; About folder
   :worship "2cf1aac5-8577-499e-b1aa-c58577a99ea0"     ; Worship folder
   :events "7c1da411-886a-4009-9da4-11886a6009c0"      ; Events folder
   :contact "acfd9bd1-1e61-4c3b-bd9b-d11e611c3bc0"     ; Contact folder
   :activities "bb44a590-1c61-416b-84a5-901c61716b5e"  ; Activities folder
   :news "fd02c48b-3d27-4df7-82c4-8b3d27adf701"        ; News folder
   :outreach "b0774f12-4ea4-4851-b74f-124ea4f851a7"    ; Outreach folder
   :preschool "915ea06b-4d65-4d5c-9ea0-6b4d65bd5cba"   ; Preschool folder
   
   ;; Special content areas
   :calendar "4f6972f5-9d50-4ff3-a972-f59d500ff3f4"    ; Calendar container
   :document-library "8f2105b4-daaf-4874-9e8a-2152569d109b"  ; Document Library
   :content "f9bffdb0-e21a-4853-bffd-b0e21a1853b6"     ; Content folder
   :website "21f2687f-7b6c-403a-b268-7f7b6c803a85"     ; Web Site folder
   
   ;; Home page features
   :home-hero "39985c5c-201a-42f6-985c-5c201a62f6d8"   ; Hero section
   :home-feature1 "264ab06c-984e-4f64-8ab0-6c984eaf6440" ; Feature 1
   :home-feature2 "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89" ; Feature 2
   :home-feature3 "6737d1b1-5465-4625-b7d1-b15465b62530" ; Feature 3
   })

;; --- SITE CONFIGURATION ---

(def site-config
  "Mount Zion site configuration"
  {:site-name "Mount Zion United Church of Christ"
   :site-tagline "A Progressive Christian Community"
   :default-page :home
   :navigation-items [{:key :home :label "Home" :path "/"}
                      {:key :about :label "About" :path "/about"}
                      {:key :worship :label "Worship" :path "/worship"}
                      {:key :events :label "Events" :path "/events"}
                      {:key :ministries :label "Ministries" :path "/ministries"}
                      {:key :contact :label "Contact" :path "/contact"}]})

;; --- HELPER FUNCTIONS ---

(defn get-node-id 
  "Get the node ID for a page key"
  [page-key]
  (get page-node-mapping page-key))

(defn update-node-mapping!
  "Update the node mapping with discovered node IDs"
  [new-mappings]
  (alter-var-root #'page-node-mapping merge new-mappings))

(defn get-page-config
  "Get configuration for a specific page"
  [page-key]
  (merge {:key page-key
          :node-id (get-node-id page-key)}
         (first (filter #(= (:key %) page-key) 
                       (:navigation-items site-config)))))