(ns mtz-cms.routes.main
  "Main routing for Mount Zion CMS"
  (:require
   [hiccup.core :as hiccup]
   [mtz-cms.pathom.resolvers :as pathom]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.ui.pages :as pages]))

;; --- HANDLER HELPERS ---

(defn html-response [hiccup-content]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html hiccup-content)})

(defn json-response [data]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (pr-str data)})

;; --- ROUTE HANDLERS ---

(defn home-handler [request]
  (let [ctx {}
        result (pathom/query ctx [{[:page/key :home] [:page/title :page/content]}])
        page-data (get result [:page/key :home])]
    (html-response (pages/home-page page-data))))

(defn about-handler [request]
  (let [ctx {}
        result (pathom/query ctx [{[:page/key :about] [:page/title :page/content]}])
        page-data (get result [:page/key :about])]
    (html-response (pages/about-page page-data))))

(defn demo-handler [request]
  (let [ctx {}
        ;; Test Pathom
        greeting-result (pathom/query ctx [{[:test/name "Mount Zion CMS"] [:test/greeting]}])
        greeting (get-in greeting-result [[:test/name "Mount Zion CMS"] :test/greeting])
        
        ;; Test Alfresco
        alfresco-result (alfresco/test-connection ctx)]
    
    (html-response (pages/demo-page {:greeting greeting
                                     :alfresco alfresco-result}))))

(defn api-pathom-handler [request]
  (let [ctx {}
        query (get-in request [:params :query])
        result (pathom/query ctx query)]
    (json-response result)))

;; --- ROUTES ---

(def all-routes
  [["/" {:get home-handler}]
   
   ["/about" {:get about-handler}]
   
   ["/demo" {:get demo-handler}]
   
   ["/api/pathom" {:post api-pathom-handler}]
   
   ;; Static assets (basic)
   ["/assets/*" {:get (fn [request]
                        {:status 404
                         :body "Static assets not implemented"})}]])

(comment
  ;; Test routes
  (home-handler {})
  (demo-handler {}))