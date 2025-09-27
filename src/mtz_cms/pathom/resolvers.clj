(ns mtz-cms.pathom.resolvers
  "Pathom resolvers for Mount Zion CMS"
  (:require
   [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
   [com.wsscode.pathom3.connect.runner :as pcr]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [mtz-cms.alfresco.client :as alfresco]))

;; Simple test resolver
(defresolver hello-content
  "Simple test resolver"
  [ctx {:test/keys [name]}]
  {::pco/input [:test/name]
   ::pco/output [:test/greeting]}
  {:test/greeting (str "Hello " name " from Mount Zion CMS!")})

;; Alfresco content resolver
(defresolver alfresco-content
  "Fetch content from Alfresco"
  [ctx {:alfresco/keys [node-id]}]
  {::pco/input [:alfresco/node-id]
   ::pco/output [:alfresco/content]}
  {:alfresco/content (alfresco/get-node-content ctx node-id)})

;; Page content resolver  
(defresolver page-content
  "Get page content by page key"
  [ctx {:page/keys [key]}]
  {::pco/input [:page/key]
   ::pco/output [:page/content :page/title]}
  (let [content (alfresco/get-page-content ctx key)]
    {:page/content content
     :page/title (str "Mount Zion UCC - " (name key))}))

(def all-resolvers
  "All Pathom resolvers"
  [hello-content
   alfresco-content
   page-content])

;; Temporary simple mock until Pathom is properly configured
(defn query 
  "Execute a Pathom query (mock implementation for now)"
  [ctx eql-query]
  (cond
    ;; Mock test greeting
    (= eql-query [{[:test/name "Mount Zion CMS"] [:test/greeting]}])
    {[:test/name "Mount Zion CMS"] {:test/greeting "Hello Mount Zion CMS from Pathom!"}}
    
    ;; Mock page content  
    (some #(and (map? %) (contains? % [:page/key :home])) (flatten eql-query))
    {[:page/key :home] {:page/title "Welcome Home" :page/content "Dynamic content from Alfresco will go here."}}
    
    ;; Default mock
    :else
    {:mock true :message "Pathom query received" :query eql-query}))

(comment
  ;; Test queries:
  (query {} [{[:test/name "World"] [:test/greeting]}])
  (query {} [{[:page/key :home] [:page/title :page/content]}]))