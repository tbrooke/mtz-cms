(ns mtz-cms.components.resolvers
  "Component-based Pathom resolvers for Mount Zion CMS"
  (:require
   [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.config.core :as config]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- COMPONENT DATA RESOLVERS ---

(defresolver hero-component
  "Resolve Hero component data from Alfresco"
  [ctx {:hero/keys [node-id]}]
  {::pco/input [:hero/node-id]
   ::pco/output [:hero/title :hero/image :hero/content]}
  (let [children-result (alfresco/get-node-children ctx node-id)]
    (if (:success children-result)
      (let [children (get-in children-result [:data :list :entries])
            image-file (first (filter #(str/includes? 
                                       (get-in % [:entry :content :mimeType] "") "image") children))
            text-files (filter #(str/includes? 
                                (get-in % [:entry :content :mimeType] "") "text") children)]
        {:hero/title "Welcome to Mount Zion UCC"
         :hero/image (when image-file
                      {:id (get-in image-file [:entry :id])
                       :name (get-in image-file [:entry :name])
                       :url (str "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" 
                                (get-in image-file [:entry :id]) "/content")})
         :hero/content (if (seq text-files)
                        ;; Get content from first text file
                        (let [content-result (alfresco/get-node-content ctx (get-in (first text-files) [:entry :id]))]
                          (if (:success content-result)
                            (:data content-result)
                            "Welcome to our community of faith."))
                        "Welcome to our community of faith.")})
      {:hero/title "Welcome to Mount Zion UCC"
       :hero/image nil
       :hero/content "Welcome to our community of faith."})))

(defresolver feature-component
  "Resolve Feature component data from Alfresco"
  [ctx {:feature/keys [node-id]}]
  {::pco/input [:feature/node-id]
   ::pco/output [:feature/title :feature/content :feature/image :feature/type]}
  (let [node-result (alfresco/get-node ctx node-id)
        children-result (alfresco/get-node-children ctx node-id)]
    (if (and (:success node-result) (:success children-result))
      (let [node-data (get-in node-result [:data :entry])
            folder-name (:name node-data)
            children (get-in children-result [:data :list :entries])
            image-files (filter #(str/includes? 
                                 (get-in % [:entry :content :mimeType] "") "image") children)
            text-files (filter #(or (str/includes? 
                                    (get-in % [:entry :content :mimeType] "") "text")
                                   (str/includes? 
                                    (get-in % [:entry :content :mimeType] "") "html")) children)
            
            ;; Get content from first text/html file
            content (if (seq text-files)
                     (let [content-result (alfresco/get-node-content ctx (get-in (first text-files) [:entry :id]))]
                       (if (:success content-result)
                         (:data content-result)
                         ""))
                     "")
            
            ;; Determine component type based on content
            component-type (cond
                           (and (seq image-files) (seq text-files)) :feature-with-image
                           (seq text-files) :feature-text-only
                           (seq image-files) :feature-image-only
                           :else :feature-placeholder)]
        
        {:feature/title folder-name
         :feature/content content
         :feature/image (when (seq image-files)
                         (let [image-file (first image-files)]
                           {:id (get-in image-file [:entry :id])
                            :name (get-in image-file [:entry :name])
                            :url (str "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" 
                                     (get-in image-file [:entry :id]) "/content")}))
         :feature/type component-type})
      {:feature/title "Feature"
       :feature/content ""
       :feature/image nil
       :feature/type :feature-placeholder})))

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