(ns mtz-cms.config.core
  "Configuration for Mount Zion CMS")

;; --- ALFRESCO CONFIGURATION ---

(def alfresco-config
  {:base-url (or (System/getenv "ALFRESCO_URL") "http://localhost:8080")
   :username (or (System/getenv "ALFRESCO_USERNAME") "admin")
   :password (or (System/getenv "ALFRESCO_PASSWORD") "admin")})

;; --- SERVER CONFIGURATION ---

(def server-config
  {:port (Integer/parseInt (or (System/getenv "PORT") "3000"))
   :host (or (System/getenv "HOST") "localhost")})

;; --- APPLICATION CONFIGURATION ---

(def app-config
  {:name "Mount Zion UCC"
   :version "1.0.0"
   :environment (keyword (or (System/getenv "ENV") "development"))})

;; --- CONTEXT HELPER ---

(defn make-context
  "Create application context with configuration"
  []
  (merge alfresco-config
         {:app app-config
          :server server-config}))

(comment
  ;; View configuration
  (make-context))