#!/usr/bin/env bb
;; Sunday Worship Discovery Script
;; Explores Sunday Worship folder structure in Alfresco

(require '[babashka.http-client :as http]
         '[cheshire.core :as json]
         '[clojure.pprint :as pprint])

;; Configuration
(def config
  {:base-url "http://localhost:8080"
   :username "admin"
   :password "admin"})

;; HTTP Client
(defn make-request [method path]
  (let [url (str (:base-url config) path)
        auth {:user (:username config)
              :pass (:password config)}]
    (try
      (println "  → Request:" method url)
      (let [response (http/request
                      {:method method
                       :uri url
                       :basic-auth auth
                       :headers {"Accept" "application/json"
                                "Content-Type" "application/json"}})]
        (println "  ← Status:" (:status response))
        {:success true
         :status (:status response)
         :data (json/parse-string (:body response) true)})
      (catch Exception e
        (println "  ✗ Error:" (ex-message e))
        {:success false
         :error (ex-message e)}))))

;; Search for Sunday Worship folder
(defn search-nodes [query-string]
  (let [url (str (:base-url config) "/alfresco/api/-default-/public/search/versions/1/search")
        auth {:user (:username config)
              :pass (:password config)}
        body {:query {:query query-string
                     :language "afts"}
              :include ["properties" "aspectNames"]
              :paging {:maxItems 100}}]
    (try
      (let [response (http/post url
                               {:basic-auth auth
                                :headers {"Content-Type" "application/json"
                                         "Accept" "application/json"}
                                :body (json/generate-string body)})]
        {:success true
         :status (:status response)
         :data (json/parse-string (:body response) true)})
      (catch Exception e
        {:success false
         :error (ex-message e)}))))

(defn get-node [node-id]
  (make-request :get (str "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                         node-id
                         "?include=properties,aspectNames")))

(defn get-node-children [node-id]
  (make-request :get (str "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                         node-id
                         "/children?include=properties,aspectNames")))

;; Find Sunday Worship folder
(defn find-sunday-worship []
  (println "🔍 Searching for Sunday Worship folder...")
  (let [search-result (search-nodes "PATH:\"/app:company_home/st:sites/cm:swsdp//*\" AND cm:name:\"Sunday Worship\"")]
    (if (:success search-result)
      (let [entries (get-in search-result [:data :list :entries])]
        (if (seq entries)
          (let [worship-node (first entries)
                worship-id (get-in worship-node [:entry :id])]
            (println "✅ Found Sunday Worship folder:" worship-id)
            {:success true :node-id worship-id})
          {:error "Sunday Worship folder not found"}))
      search-result)))

;; Explore date subfolders
(defn explore-date-folders [worship-node-id]
  (println "\n📅 Exploring date subfolders...")
  (let [children-result (get-node-children worship-node-id)]
    (if (:success children-result)
      (let [entries (get-in children-result [:data :list :entries])
            folders (filter #(get-in % [:entry :isFolder]) entries)]
        (println "  Found" (count folders) "date folders\n")

        (doseq [folder folders]
          (let [folder-name (get-in folder [:entry :name])
                folder-id (get-in folder [:entry :id])]
            (println "📁" folder-name)
            (println "  ID:" folder-id)

            ;; Get PDFs in this date folder
            (let [pdfs-result (get-node-children folder-id)]
              (when (:success pdfs-result)
                (let [pdfs (filter #(and (get-in % [:entry :isFile])
                                        (= (get-in % [:entry :content :mimeType]) "application/pdf"))
                                  (get-in pdfs-result [:data :list :entries]))]
                  (println "  PDFs:" (count pdfs))
                  (doseq [pdf pdfs]
                    (let [pdf-name (get-in pdf [:entry :name])
                          pdf-id (get-in pdf [:entry :id])
                          tags (get-in pdf [:entry :properties :cm:taggable] [])]
                      (println "    📄" pdf-name)
                      (println "       ID:" pdf-id)
                      (println "       Tags:" tags)
                      (println "       Has bulletin tag?" (some #(= "bulletin" %) tags)))))))))

        {:success true :folders folders})
      children-result)))

;; Main execution
(defn -main []
  (println "🚀 Sunday Worship Discovery Script")
  (println (apply str (repeat 80 "=")) "\n")

  (let [worship-result (find-sunday-worship)]
    (when (:success worship-result)
      (let [node-id (:node-id worship-result)
            exploration (explore-date-folders node-id)]

        (when (:success exploration)
          (println "\n" (apply str (repeat 80 "=")) "\n")
          (println "💾 Summary:")
          (println "  Sunday Worship ID:" node-id)
          (println "  Date folders:" (count (:folders exploration)))
          (println "\n✅ Discovery complete!"))))))

;; Run main
(-main)
