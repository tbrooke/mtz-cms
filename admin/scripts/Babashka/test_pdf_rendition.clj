#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json])

(def base-url "http://localhost:8080")
(def auth {:user "admin" :pass "admin"})

;; Test PDF node ID (one of the bulletin PDFs)
(def test-pdf-id "e7e4e3aa-2f9b-42b9-a4e3-aa2f9b02b917")

(println "\n🔍 Testing PDF renditions for node:" test-pdf-id)

;; First, get the node info
(println "\n1. Getting node info...")
(let [response (curl/get (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" test-pdf-id)
                         {:basic-auth [(:user auth) (:pass auth)]
                          :headers {"Accept" "application/json"}
                          :throw false})]
  (println "Status:" (:status response))
  (when (= 200 (:status response))
    (let [data (json/parse-string (:body response) true)]
      (println "Name:" (get-in data [:entry :name]))
      (println "MIME:" (get-in data [:entry :content :mimeType])))))

;; Get available renditions
(println "\n2. Getting available renditions...")
(let [response (curl/get (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" test-pdf-id "/renditions")
                         {:basic-auth [(:user auth) (:pass auth)]
                          :headers {"Accept" "application/json"}
                          :throw false})]
  (println "Status:" (:status response))
  (when (= 200 (:status response))
    (let [data (json/parse-string (:body response) true)
          renditions (get-in data [:list :entries])]
      (println "\nAvailable renditions:")
      (doseq [r renditions]
        (let [entry (:entry r)]
          (println "  -" (:id entry) 
                   (str "(status: " (:status entry) ")")))))))

;; Try different rendition names
(println "\n3. Testing common rendition names...")
(doseq [rendition-name ["pdf" "doclib" "imgpreview" "medium" "avatar"]]
  (println "\nTrying rendition:" rendition-name)
  (let [response (curl/get (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" test-pdf-id "/renditions/" rendition-name "/content")
                           {:basic-auth [(:user auth) (:pass auth)]
                            :throw false
                            :as :bytes})]
    (println "  Status:" (:status response))
    (when (= 200 (:status response))
      (println "  ✅ SUCCESS! Content-Type:" (get-in response [:headers "content-type"])))))

(println "\n✅ Done!")
