(ns mtz-cms.alfresco.resolvers
  "Pathom resolvers for Alfresco content with Malli validation

   Handles data resolution from Alfresco CMS to application data structures.
   Moved from components/ to alfresco/ as part of architecture refactoring."
  (:require
   [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.alfresco.content-processor :as processor]
   [mtz-cms.config.core :as config]
   [mtz-cms.validation.schemas :as schemas]
   [mtz-cms.validation.middleware :as validation]
   [mtz-cms.cache.simple :as cache]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- GENERIC HELPER FUNCTION ---

(defn- resolve-content-component-impl
  "Generic resolver for cm:folder with typed children (images + HTML content).
   Used by hero, feature, section, and other components following this pattern.

   Returns a map with:
   - :title (from folder cm:title or node name)
   - :content (HTML content from text/html children, processed for images)
   - :image (map with :id, :name, :url for first image found)
   - :description (from HTML node cm:description if available)
   - :type (component type keyword for rendering logic)"
  [ctx node-id component-type]
  (try
    (let [node-result (alfresco/get-node ctx node-id)]
      (if (:success node-result)
        (let [node-data (get-in node-result [:data :entry])
              ;; Extract title from Content Model (cm:title preferred over name)
              title (or (get-in node-data [:properties :cm:title])
                       (:name node-data)
                       "")

              ;; Get children to find content
              children-result (alfresco/get-node-children ctx node-id)]

          (if (:success children-result)
            (let [children (get-in children-result [:data :list :entries])
                  ;; Filter by MIME type
                  image-files (filter #(str/includes?
                                       (get-in % [:entry :content :mimeType] "") "image") children)
                  text-files (filter #(or (str/includes?
                                          (get-in % [:entry :content :mimeType] "") "text")
                                         (str/includes?
                                          (get-in % [:entry :content :mimeType] "") "html")) children)

                  ;; Get HTML content from first text/html file
                  html-node (first text-files)
                  content (if html-node
                           (let [content-result (alfresco/get-node-content ctx (get-in html-node [:entry :id]))]
                             (if (:success content-result)
                               ;; Convert binary to string and process HTML
                               (let [content-str (if (bytes? (:data content-result))
                                                  (String. (:data content-result) "UTF-8")
                                                  (str (:data content-result)))]
                                 (processor/process-html-content content-str))
                               ""))
                           "")

                  ;; Get metadata from HTML node if available
                  html-node-data (when html-node
                                  (get-in (alfresco/get-node ctx (get-in html-node [:entry :id]))
                                          [:data :entry]))
                  description (when html-node-data
                               (get-in html-node-data [:properties :cm:description]))

                  ;; Determine type based on what content exists
                  content-type (cond
                                (and (seq image-files) (seq text-files)) :with-image
                                (seq text-files) :text-only
                                (seq image-files) :image-only
                                :else :placeholder)]

              (log/info "✅ Content component resolved:" component-type "for node:" node-id "title:" title)
              {:title title
               :content content
               :description description
               :image (when (seq image-files)
                       (let [img (first image-files)]
                         {:id (get-in img [:entry :id])
                          :name (get-in img [:entry :name])
                          :url (str "/proxy/image/" (get-in img [:entry :id]) "/imgpreview")}))
               ;; Return images with metadata (hero uses this)
               ;; NOTE: Hero component only supports 1-2 images, limit and warn if more
               :images (when (seq image-files)
                        (let [image-count (count image-files)]
                          (when (> image-count 2)
                            (log/warn "⚠️ Hero folder has" image-count "images. Only first 2 will be used. Please remove extra images."))
                          (mapv (fn [img]
                                  (let [img-id (get-in img [:entry :id])
                                        ;; Fetch image node to get title and description
                                        img-node-result (alfresco/get-node ctx img-id)
                                        img-props (when (:success img-node-result)
                                                   (get-in img-node-result [:data :entry :properties]))]
                                    {:id img-id
                                     :name (get-in img [:entry :name])
                                     :url (str "/proxy/image/" img-id "/imgpreview")
                                     :title (get img-props :cm:title)
                                     :description (get img-props :cm:description)
                                     :link (str "/hero/" img-id)}))
                                ;; Take only first 2 images for hero
                                (take 2 image-files))))
               :type content-type
               :component-type component-type})

            ;; Children fetch failed - return partial data
            (do
              (log/warn "Children fetch failed for node:" node-id)
              {:title title
               :content ""
               :description nil
               :image nil
               :type :placeholder
               :component-type component-type})))

        ;; Node fetch failed
        (do
          (log/error "Node fetch failed for:" node-id)
          {:title ""
           :content ""
           :description nil
           :image nil
           :type :placeholder
           :component-type component-type})))

    (catch Exception e
      (log/error "❌ Error in resolve-content-component:" (.getMessage e))
      {:title ""
       :content ""
       :description nil
       :image nil
       :type :error
       :component-type component-type})))

(defn resolve-content-component
  "Cached wrapper for resolve-content-component-impl.
   Caches component data for 1 hour (3600 seconds)."
  [ctx node-id component-type]
  (cache/cached
   (keyword (str (name component-type) "-" node-id))
   3600  ;; 1 hour
   (fn [] (resolve-content-component-impl ctx node-id component-type))))

;; --- COMPONENT DATA RESOLVERS ---

(defresolver hero-component
  "Resolve Hero component data from Alfresco with validation & transformation"
  [ctx {:hero/keys [node-id]}]
  {::pco/input [:hero/node-id]
   ::pco/output [:hero/title :hero/image :hero/images :hero/content]}
  ;; Use the cached generic resolver
  (let [raw-data (resolve-content-component ctx node-id :hero)
        result {:hero/title (:title raw-data)
                :hero/image (:image raw-data)
                :hero/images (:images raw-data)
                :hero/content (:content raw-data)}
        ;; Validate and transform
        validated (schemas/validate-and-transform :hero/output result schemas/component-transformer)]
    (when-not (:valid? validated)
      (log/warn "Hero component validation failed:" (:errors validated)))
    ;; Return cleaned data (or original if validation failed)
    (or (:data validated) result)))

(defresolver feature-component
  "Resolve Feature component data from Alfresco with validation & transformation"
  [ctx {:feature/keys [node-id]}]
  {::pco/input [:feature/node-id]
   ::pco/output [:feature/title :feature/content :feature/image :feature/type]}
  ;; Use the cached generic resolver
  (let [raw-data (resolve-content-component ctx node-id :feature)
        result {:feature/title (:title raw-data)
                :feature/content (:content raw-data)
                :feature/image (:image raw-data)
                :feature/type (:type raw-data)}
        ;; Validate and transform
        validated (schemas/validate-and-transform :feature/output result schemas/component-transformer)]
    (when-not (:valid? validated)
      (log/warn "Feature component validation failed:" (:errors validated)))
    ;; Return cleaned data (or original if validation failed)
    (or (:data validated) result)))

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

(defresolver section-component
  "Resolve Section component data from Alfresco using generic helper.
   Sections follow the folder pattern: cm:folder with HTML content + optional image."
  [ctx {:section/keys [node-id]}]
  {::pco/input [:section/node-id]
   ::pco/output [:section/title :section/subtitle :section/body
                 :section/description :section/image :section/type]}
  (let [data (resolve-content-component ctx node-id :section)]
    {:section/title (:title data)
     :section/subtitle (:title data)  ; Use title as subtitle for now
     :section/body (:content data)
     :section/description (:description data)
     :section/image (:image data)
     :section/type (:type data)}))

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
   section-component
   home-page-components])