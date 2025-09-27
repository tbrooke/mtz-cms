(ns mtz-cms.core
  "Mount Zion UCC CMS - Main application entry point"
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.params :as params]
   [ring.middleware.keyword-params :as keyword-params]
   [reitit.ring :as reitit-ring]
   [mtz-cms.routes.main :as routes]
   [clojure.tools.logging :as log])
  (:gen-class))

(defn create-handler []
  (reitit-ring/ring-handler
    (reitit-ring/router routes/all-routes)
    (reitit-ring/create-default-handler)))

(defn create-app []
  (-> (create-handler)
      keyword-params/wrap-keyword-params
      params/wrap-params))

(defn start-server [& [port]]
  (let [port (or port 3000)]
    (log/info "Starting Mount Zion CMS on port" port)
    (jetty/run-jetty (create-app) 
                     {:port port 
                      :join? false})))

(defn -main [& args]
  (start-server))

(comment
  ;; Development
  (def server (start-server 3000))
  (.stop server))