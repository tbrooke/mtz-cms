(ns user
  "Development namespace for Mount Zion CMS"
  (:require
   [clojure.tools.namespace.repl :as repl]
   [mtz-cms.core :as core]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.pathom.resolvers :as pathom]))

;; --- DEVELOPMENT SERVER ---

(def server (atom nil))

(defn start []
  (when @server
    (.stop @server))
  (reset! server (core/start-server 3000))
  (println "🚀 Mount Zion CMS started on http://localhost:3000"))

(defn stop []
  (when @server
    (.stop @server)
    (reset! server nil))
  (println "🛑 Mount Zion CMS stopped"))

(defn restart []
  (stop)
  (repl/refresh :after 'user/start))

;; --- DEVELOPMENT HELPERS ---

(defn test-alfresco []
  (alfresco/test-connection {}))

(defn test-pathom []
  (pathom/query {} [{[:test/name "Development"] [:test/greeting]}]))

(defn routes []
  (require 'mtz-cms.routes.main :reload)
  @(resolve 'mtz-cms.routes.main/all-routes))

;; --- REPL HELPERS ---

(comment
  ;; Start the server
  (start)
  
  ;; Stop the server
  (stop)
  
  ;; Restart with code reload
  (restart)
  
  ;; Test connections
  (test-alfresco)
  (test-pathom)
  
  ;; View routes
  (routes))