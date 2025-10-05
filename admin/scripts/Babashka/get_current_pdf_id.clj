#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json])

(def base-url "http://localhost:8080")
(def auth {:user "admin" :pass "admin"})

;; Sunday Worship folder
(def sunday-worship-id "a2e853fa-28f5-42d9-a853-fa28f522d918")

(println "\n🔍 Getting a PDF from Sunday Worship...")

;; Get date folders
(let [response (curl/get (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" sunday-worship-id "/children")
                         {:basic-auth [(:user auth) (:pass auth)]
                          :headers {"Accept" "application/json"}
                          :throw false})]
  (when (= 200 (:status response))
    (let [data (json/parse-string (:body response) true)
          folders (get-in data [:list :entries])
          first-folder (first folders)
          folder-id (get-in first-folder [:entry :id])
          folder-name (get-in first-folder [:entry :name])]
      
      (println "First date folder:" folder-name "(" folder-id ")")
      
      ;; Get PDFs in that folder
      (let [pdf-response (curl/get (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" folder-id "/children")
                                   {:basic-auth [(:user auth) (:pass auth)]
                                    :headers {"Accept" "application/json"}
                                    :throw false})]
        (when (= 200 (:status pdf-response))
          (let [pdf-data (json/parse-string (:body pdf-response) true)
                pdfs (get-in pdf-data [:list :entries])
                first-pdf (first pdfs)
                pdf-id (get-in first-pdf [:entry :id])
                pdf-name (get-in first-pdf [:entry :name])]
            
            (println "First PDF:" pdf-name)
            (println "PDF ID:" pdf-id)))))))

