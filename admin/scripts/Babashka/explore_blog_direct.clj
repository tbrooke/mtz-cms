#!/usr/bin/env bb
;; Blog Discovery Script - Direct Access Version
;; Can be run directly on the trust server or via SSH tunnel

(require '[babashka.http-client :as http]
         '[cheshire.core :as json]
         '[clojure.pprint :as pprint])

;; Configuration - try tunnel first, then direct
(def configs
  [{:name "SSH Tunnel"
    :base-url "http://localhost:8080"
    :username "admin"
    :password "admin"}
   {:name "Direct (trust server)"
    :base-url "http://localhost:8080"
    :username "admin"
    :password "admin"}])

;; Test connection
(defn test-connection [config]
  (try
    (let [url (str (:base-url config) "/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-")
          response (http/get url
                            {:basic-auth {:user (:username config)
                                         :pass (:password config)}
                             :headers {"Accept" "application/json"}
                             :throw false})]
      (when (and response (< (:status response) 400))
        config))
    (catch Exception e
      nil)))

;; Find working configuration
(defn find-working-config []
  (println "🔍 Testing connections...")
  (or (some test-connection configs)
      (do
        (println "\n❌ No working connection found!")
        (println "\nPlease ensure:")
        (println "  1. SSH tunnel is active: ssh -L 8080:localhost:8080 -N -f tmb@trust")
        (println "  2. OR run this script directly on the trust server")
        (System/exit 1))))

;; Simple curl-based approach for direct server access
(defn curl-request [config method path]
  (let [url (str (:base-url config) path)
        auth (str (:username config) ":" (:password config))
        cmd (case method
              :get ["curl" "-s" "-u" auth "-H" "Accept: application/json" url]
              :post ["curl" "-s" "-u" auth "-X" "POST"
                    "-H" "Content-Type: application/json"
                    "-H" "Accept: application/json" url])]
    (try
      (let [result (apply shell/sh cmd)]
        (if (zero? (:exit result))
          {:success true
           :data (json/parse-string (:out result) true)}
          {:success false
           :error (:err result)}))
      (catch Exception e
        {:success false
         :error (ex-message e)}))))

(defn -main []
  (let [config (find-working-config)]
    (println "✅ Connected via:" (:name config))
    (println "\n📍 Next step: Run the full exploration")
    (println "\nTo explore blog manually, you can use:")
    (println "  curl -u admin:admin http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/sites/swsdp/containers")
    (println "\nOr via Clojure REPL:")
    (println "  (user/analyze-blog-structure)")))

(-main)
