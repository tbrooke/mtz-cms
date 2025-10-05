#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json])

(def base-url "http://localhost:8080")
(def auth {:user "admin" :pass "admin"})

;; Sunday Worship folder
(def sunday-worship-id "a2e853fa-28f5-42d9-a853-fa28f522d918")

(println "\n🔍 Getting PDF details from Sunday Worship...")

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
      
      (println "\nFirst date folder:" folder-name)
      
      ;; Get children with properties
      (let [pdf-response (curl/get (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" folder-id "/children?include=properties,aspectNames")
                                   {:basic-auth [(:user auth) (:pass auth)]
                                    :headers {"Accept" "application/json"}
                                    :throw false})]
        (when (= 200 (:status pdf-response))
          (let [pdf-data (json/parse-string (:body pdf-response) true)
                children (get-in pdf-data [:list :entries])]
            
            (println "\nChildren in folder:")
            (doseq [child children]
              (let [entry (:entry child)]
                (println "\n  Name:" (:name entry))
                (println "  ID:" (:id entry))
                (println "  MIME:" (get-in entry [:content :mimeType]))
                (println "  isFile:" (:isFile entry))))))))))

