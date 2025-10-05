#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json])

(def base-url "http://localhost:8080")
(def auth {:user "admin" :pass "admin"})

;; Test the exact folder we're working with
(def folder-id "e90280d1-da84-4eba-8280-d1da84beba26")

(println "\n🔍 Testing 10-05-25 folder contents...")

;; Get children with properties
(let [response (curl/get (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" folder-id "/children?include=properties,aspectNames")
                         {:basic-auth [(:user auth) (:pass auth)]
                          :headers {"Accept" "application/json"}
                          :throw false})]
  (when (= 200 (:status response))
    (let [data (json/parse-string (:body response) true)
          children (get-in data [:list :entries])]
      
      (println "\nAll files:")
      (doseq [child children]
        (let [entry (:entry child)
              name (:name entry)
              mime (get-in entry [:content :mimeType])
              id (:id entry)]
          (println "\n  Name:" name)
          (println "  MIME:" mime)
          (println "  ID:" id)
          (println "  Is video?" (and mime (clojure.string/starts-with? mime "video/"))))))))

