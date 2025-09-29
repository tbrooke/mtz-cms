(ns mtz-cms.components.resolvers
  "Component-based Pathom resolvers for Mount Zion CMS with Malli validation"
  (:require
   [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.alfresco.content-processor :as processor]
   [mtz-cms.config.core :as config]
   [mtz-cms.validation.schemas :as schemas]
   [mtz-cms.validation.middleware :as validation]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- COMPONENT DATA RESOLVERS ---

(defresolver hero-component
  "Resolve Hero component data from Alfresco using Content Model with validation"
  [ctx {:hero/keys [node-id]}]
  {::pco/input [:hero/node-id]
   ::pco/output [:hero/title :hero/image :hero/content]}
  (try
    ;; Validate input
    (log/debug "🔍 Validating hero component input")
    (schemas/validate! :hero/input {:hero/node-id node-id})

    ;; Step 1: Get the hero folder node to extract cm:title
    (let [node-result (alfresco/get-node ctx node-id)
          ;; Validate Alfresco response
          _ (validation/validate-alfresco-response node-result "get-node")]

      (if (:success node-result)
        (let [node-data (get-in node-result [:data :entry])
              ;; Extract title from Content Model properties
              hero-title (or (get-in node-data [:properties :cm:title])
                            (:name node-data)
                            "Welcome to Mount Zion UCC")

              ;; Step 2: Get children to find content based on MIME types
              children-result (alfresco/get-node-children ctx node-id)]

          (if (:success children-result)
            (let [children (get-in children-result [:data :list :entries])
                  ;; Find image content by MIME type
                  image-file (first (filter #(str/includes?
                                              (get-in % [:entry :content :mimeType] "") "image") children))
                  ;; Find HTML/text content by MIME type
                  text-files (filter #(or (str/includes?
                                           (get-in % [:entry :content :mimeType] "") "text")
                                          (str/includes?
                                           (get-in % [:entry :content :mimeType] "") "html")) children)

                  ;; Extract content from Content Model
                  hero-content (if (seq text-files)
                                ;; Get content from first text/html file
                                (let [content-result (alfresco/get-node-content ctx (get-in (first text-files) [:entry :id]))]
                                  (if (:success content-result)
                                    ;; Process HTML content to convert image URLs
                                    (processor/process-html-content (:data content-result))
                                    "A progressive Christian community welcoming all people."))
                                "A progressive Christian community welcoming all people.")

                  result {:hero/title hero-title
                         :hero/image (when image-file
                                       {:id (get-in image-file [:entry :id])
                                        :name (get-in image-file [:entry :name])
                                        :url (str "/api/image/" (get-in image-file [:entry :id]))})
                         :hero/content hero-content}]

              ;; Validate output
              (log/debug "🔍 Validating hero component output")
              (schemas/validate! :hero/output result)

              (log/info "✅ Hero component resolved using Content Model for node:" node-id)
              result)

            (let [fallback {:hero/title hero-title
                           :hero/image nil
                           :hero/content "A progressive Christian community welcoming all people."}]
              (log/warn "Using partial fallback hero data - node found but children failed")
              (schemas/validate! :hero/output fallback)
              fallback)))

        (let [fallback {:hero/title "Welcome to Mount Zion UCC"
                       :hero/image nil
                       :hero/content "A progressive Christian community welcoming all people."}]
          (log/warn "Using fallback hero data due to Alfresco node error")
          (schemas/validate! :hero/output fallback)
          fallback)))

    (catch Exception e
      (log/error "❌ Error in hero component resolver:" (.getMessage e))
      ;; Return validated fallback
      (let [fallback {:hero/title "Welcome to Mount Zion UCC"
                     :hero/image nil
                     :hero/content "A progressive Christian community welcoming all people."}]
        (schemas/validate! :hero/output fallback)
        fallback))))

(defresolver feature-component
  "Resolve Feature component data from Alfresco using Content Model"
  [ctx {:feature/keys [node-id]}]
  {::pco/input [:feature/node-id]
   ::pco/output [:feature/title :feature/content :feature/image :feature/type]}
  (let [node-result (alfresco/get-node ctx node-id)
        children-result (alfresco/get-node-children ctx node-id)]
    (if (and (:success node-result) (:success children-result))
      (let [node-data (get-in node-result [:data :entry])
            ;; Extract title from Content Model properties (cm:title preferred over name)
            feature-title (or (get-in node-data [:properties :cm:title])
                             (:name node-data)
                             "Feature")
            children (get-in children-result [:data :list :entries])
            ;; Filter by MIME type for proper Content Model handling
            image-files (filter #(str/includes?
                                  (get-in % [:entry :content :mimeType] "") "image") children)
            text-files (filter #(or (str/includes?
                                     (get-in % [:entry :content :mimeType] "") "text")
                                    (str/includes?
                                     (get-in % [:entry :content :mimeType] "") "html")) children)

            ;; Get content from first text/html file and process images
            feature-content (if (seq text-files)
                             (let [content-result (alfresco/get-node-content ctx (get-in (first text-files) [:entry :id]))]
                               (if (:success content-result)
                                 ;; Convert binary data to string and process HTML content
                                 (let [content-str (if (bytes? (:data content-result))
                                                    (String. (:data content-result) "UTF-8")
                                                    (str (:data content-result)))]
                                   (processor/process-html-content content-str))
                                 ""))
                             "")

            ;; Determine component type based on Content Model structure
            component-type (cond
                             (and (seq image-files) (seq text-files)) :feature-with-image
                             (seq text-files) :feature-text-only
                             (seq image-files) :feature-image-only
                             :else :feature-placeholder)]

        (log/info "✅ Feature component resolved using Content Model for node:" node-id "title:" feature-title)
        {:feature/title feature-title
         :feature/content feature-content
         :feature/image (when (seq image-files)
                          (let [image-file (first image-files)]
                            {:id (get-in image-file [:entry :id])
                             :name (get-in image-file [:entry :name])
                             :url (str "/api/image/" (get-in image-file [:entry :id]))}))
         :feature/type component-type})
      (do
        (log/warn "Feature component fallback for node:" node-id)
        {:feature/title "Feature"
         :feature/content ""
         :feature/image nil
         :feature/type :feature-placeholder}))))

(defresolver card-component
  "Resolve Card component data from Alfresco"
  [ctx {:card/keys [node-id]}]
  {::pco/input [:card/node-id]
   ::pco/output [:card/title :card/content :card/image :card/link]}
  (let [node-result (alfresco/get-node ctx node-id)]
    (if (:success node-result)
      (let [node-data (get-in node-result [:data :entry])
            title (or (get-in node-data [:properties :cm:title])
                      (:name node-data))]
        {:card/title title
         :card/content (str "Learn more about " title)
         :card/image nil
         :card/link (str "/page/" (str/lower-case (str/replace title #" " "-")))})
      {:card/title "Card"
       :card/content ""
       :card/image nil
       :card/link "#"})))

;; --- PAGE COMPONENT COMPOSITION RESOLVERS ---

(defresolver home-page-components
  "Compose components for the home page"
  [ctx input]
  {::pco/output [:home/hero :home/features :home/layout]}
  (let [hero-node-id (config/get-node-id :home-hero)
        feature1-node-id (config/get-node-id :home-feature1)
        feature2-node-id (config/get-node-id :home-feature2)
        feature3-node-id (config/get-node-id :home-feature3)]

    {:home/hero {:hero/node-id hero-node-id}
     :home/features [{:feature/node-id feature1-node-id}
                     {:feature/node-id feature2-node-id}
                     {:feature/node-id feature3-node-id}]
     :home/layout :hero-features-layout}))

;; --- COMPONENT LIST ---

(def component-resolvers
  "All component resolvers"
  [hero-component
   feature-component
   card-component
   home-page-components])