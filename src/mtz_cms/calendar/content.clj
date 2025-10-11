(ns mtz-cms.calendar.content
  "Fetch .ics files directly from Alfresco node content endpoints"
  (:require
   [clj-http.client :as http]
   [mtz-cms.alfresco.client :as alfresco]
   [clojure.tools.logging :as log]))

(defn fetch-node-content-v1
  "Fetch node content via v1 API: /alfresco/api/-default-/public/alfresco/versions/1/nodes/{id}/content"
  [base-url node-id username password]
  (let [url (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" node-id "/content")]
    (log/info "Trying v1 API content endpoint:" url)
    (try
      (let [response (http/get url
                              {:basic-auth [username password]
                               :throw-exceptions false
                               :as :text})]
        (when (= 200 (:status response))
          (log/info "✅ v1 API success -" (count (:body response)) "bytes")
          (:body response)))
      (catch Exception e
        (log/warn "v1 API failed:" (.getMessage e))
        nil))))

(defn fetch-node-content-legacy
  "Fetch node content via legacy servlet: /alfresco/service/api/node/content/workspace/SpacesStore/{id}"
  [base-url node-id username password]
  (let [url (str base-url "/alfresco/service/api/node/content/workspace/SpacesStore/" node-id)]
    (log/info "Trying legacy content servlet:" url)
    (try
      (let [response (http/get url
                              {:basic-auth [username password]
                               :throw-exceptions false
                               :as :text})]
        (when (= 200 (:status response))
          (log/info "✅ Legacy servlet success -" (count (:body response)) "bytes")
          (:body response)))
      (catch Exception e
        (log/warn "Legacy servlet failed:" (.getMessage e))
        nil))))

(defn fetch-node-content-direct
  "Fetch node content via direct content URL: /alfresco/d/d/workspace/SpacesStore/{id}/{filename}"
  [base-url node-id filename username password]
  (let [url (str base-url "/alfresco/d/d/workspace/SpacesStore/" node-id "/" filename)]
    (log/info "Trying direct content URL:" url)
    (try
      (let [response (http/get url
                              {:basic-auth [username password]
                               :throw-exceptions false
                               :as :text})]
        (when (= 200 (:status response))
          (log/info "✅ Direct URL success -" (count (:body response)) "bytes")
          (:body response)))
      (catch Exception e
        (log/warn "Direct URL failed:" (.getMessage e))
        nil))))

(defn fetch-ics-content
  "Fetch .ics content from Alfresco node - tries multiple endpoints

   Attempts (in order):
   1. v1 API content endpoint (via tunnel)
   2. v1 API content endpoint (via direct server)
   3. Legacy content servlet (via direct server)
   4. Direct content URL (via direct server)

   Parameters:
   - ctx: Alfresco context
   - node-id: Node ID
   - filename: Filename (optional, for direct URL)

   Returns: String content or nil"
  [ctx node-id & {:keys [filename]}]
  (let [config (alfresco/get-config ctx)
        tunnel-url (:base-url config)  ; http://localhost:8080
        direct-url (or (System/getenv "ALFRESCO_SERVER_URL") "http://admin.mtzcg.com")
        username (:username config)
        password (:password config)]

    (log/info "Fetching .ics content for node:" node-id)

    ;; Try multiple approaches
    (or
     ;; Try v1 API via tunnel
     (fetch-node-content-v1 tunnel-url node-id username password)

     ;; Try v1 API via direct server
     (fetch-node-content-v1 direct-url node-id username password)

     ;; Try legacy servlet via direct server
     (fetch-node-content-legacy direct-url node-id username password)

     ;; Try direct URL if filename provided
     (when filename
       (fetch-node-content-direct direct-url node-id filename username password))

     ;; All failed
     (do
       (log/error "❌ All content fetch methods failed for node:" node-id)
       nil))))

(comment
  ;; Test fetching
  (require '[mtz-cms.calendar.content :as content])

  (def ctx {})
  (def node-id "a950f75a-248b-4e80-90f7-5a248b6e80b9")

  ;; Try fetching
  (def ics-content (content/fetch-ics-content ctx node-id))

  ;; Or with filename
  (def ics-content (content/fetch-ics-content ctx node-id :filename "1760122710043-4983.ics"))

  ;; Check content
  (when ics-content
    (println "Got" (count ics-content) "bytes")
    (println (subs ics-content 0 (min 200 (count ics-content))))))
