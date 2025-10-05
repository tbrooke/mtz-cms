#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json])

(def base-url "http://localhost:8080")
(def auth {:user "admin" :pass "admin"})

;; 10-05-25 folder - today's date
(def sunday-worship-id "a2e853fa-28f5-42d9-a853-fa28f522d918")

(println "\n🔍 Checking for media files in Sunday Worship folders...")

;; Get date folders
(let [response (curl/get (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" sunday-worship-id "/children")
                         {:basic-auth [(:user auth) (:pass auth)]
                          :headers {"Accept" "application/json"}
                          :throw false})]
  (when (= 200 (:status response))
    (let [data (json/parse-string (:body response) true)
          folders (get-in data [:list :entries])
          target-folder (first (filter #(= "10-05-25" (get-in % [:entry :name])) folders))]
      
      (if target-folder
        (let [folder-id (get-in target-folder [:entry :id])
              folder-name (get-in target-folder [:entry :name])]
          
          (println "\n✅ Found folder:" folder-name "(" folder-id ")")
          
          ;; Get all children
          (let [children-response (curl/get (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" folder-id "/children")
                                            {:basic-auth [(:user auth) (:pass auth)]
                                             :headers {"Accept" "application/json"}
                                             :throw false})]
            (when (= 200 (:status children-response))
              (let [children-data (json/parse-string (:body children-response) true)
                    children (get-in children-data [:list :entries])]
                
                (println "\nFiles in folder:")
                (doseq [child children]
                  (let [entry (:entry child)
                        name (:name entry)
                        mime (get-in entry [:content :mimeType])
                        id (:id entry)
                        size (get-in entry [:content :sizeInBytes])]
                    (println "\n  Name:" name)
                    (println "  ID:" id)
                    (println "  MIME:" mime)
                    (println "  Size:" (when size (str (int (/ size 1024 1024)) " MB")))))))))
        
        (println "\n❌ Folder 10-05-25 not found")))))

