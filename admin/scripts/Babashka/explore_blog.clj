#!/usr/bin/env bb
;; Blog Discovery Script
;; Explores Alfresco blog structure and generates schema information

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
        (println "  Stack:" (ex-data e))
        {:success false
         :error (ex-message e)
         :exception (str e)}))))

;; Search API
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

;; Node operations
(defn get-node [node-id]
  (make-request :get (str "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" node-id)))

(defn get-node-children [node-id]
  (make-request :get (str "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                         node-id
                         "/children?include=properties,aspectNames")))

;; Blog discovery
(defn get-site-containers [site-id]
  (make-request :get (str "/alfresco/api/-default-/public/alfresco/versions/1/sites/" site-id "/containers")))

(defn find-blog-folder []
  (println "🔍 Looking for blog folder via Sites API...")
  ;; First try to get the blog container from the swsdp site
  (let [containers-result (get-site-containers "swsdp")]
    (if (:success containers-result)
      (let [entries (get-in containers-result [:data :list :entries])
            blog-container (first (filter #(= "blog" (get-in % [:entry :folderId])) entries))]
        (if blog-container
          (let [blog-id (get-in blog-container [:entry :id])]
            (println "✅ Found blog container:" blog-id)
            {:success true :data {:list {:entries [{:entry {:id blog-id}}]}}})
          ;; Try PATH search as fallback
          (do
            (println "🔍 Trying PATH search...")
            (search-nodes "PATH:\"/app:company_home/st:sites/cm:swsdp/cm:blog/*\""))))
      containers-result)))

(defn explore-blog []
  (let [blog-result (find-blog-folder)]
    (if (:success blog-result)
      (let [entries (get-in blog-result [:data :list :entries])]
        (if (seq entries)
          (let [blog-node (first entries)
                blog-id (get-in blog-node [:entry :id])]
            (println "✅ Found blog folder:" blog-id)
            (get-node-children blog-id))
          {:error "Blog folder not found"}))
      blog-result)))

(defn extract-blog-post-info [entry]
  (let [node-id (get-in entry [:entry :id])
        node-result (get-node node-id)]
    (when (:success node-result)
      (let [node (get-in node-result [:data :entry])]
        {:node-id node-id
         :name (:name node)
         :title (get-in node [:properties :cm:title])
         :description (get-in node [:properties :cm:description])
         :created (:createdAt node)
         :modified (:modifiedAt node)
         :author (get-in node [:createdByUser :displayName])
         :aspects (:aspectNames node)
         :properties (:properties node)}))))

(defn analyze-blog []
  (println "\n📊 Analyzing blog structure...\n")
  (let [blog-result (explore-blog)]
    (if (:success blog-result)
      (let [entries (get-in blog-result [:data :list :entries])
            posts (map extract-blog-post-info entries)]
        (println "📁 Total blog posts:" (count posts))
        (println "\n" (apply str (repeat 80 "=")) "\n")

        (doseq [post posts]
          (println "📝 Blog Post:")
          (println "  Title:" (:title post))
          (println "  Description:" (:description post))
          (println "  Node ID:" (:node-id post))
          (println "  Author:" (:author post))
          (println "  Created:" (:created post))
          (println "  Modified:" (:modified post))
          (println "  Aspects:" (:aspects post))
          (println "\n  Properties:")
          (doseq [[k v] (:properties post)]
            (println "    " k "=" v))
          (println "\n" (apply str (repeat 80 "-")) "\n"))

        ;; Return structured data
        {:total (count posts)
         :posts posts})
      (do
        (println "❌ Error:" (:error blog-result))
        blog-result))))

(defn generate-malli-schema [posts]
  (println "\n📋 Generating Malli Schema...\n")

  ;; Extract all unique properties from blog posts
  (let [all-props (into #{} (mapcat #(keys (:properties %)) posts))
        all-aspects (into #{} (mapcat :aspects posts))]

    (println ";; Blog Post Malli Schema")
    (println "(def blog-post-schema")
    (println "  [:map")
    (println "   [:node-id :string]")
    (println "   [:name :string]")
    (println "   [:title {:optional true} [:maybe :string]]")
    (println "   [:description {:optional true} [:maybe :string]]")
    (println "   [:created :string]")
    (println "   [:modified :string]")
    (println "   [:author {:optional true} [:maybe :string]]")
    (println "   [:aspects [:sequential :keyword]]")
    (println "   [:properties [:map")
    (doseq [prop (sort all-props)]
      (println (str "                 [" prop " {:optional true} [:maybe :string]]")))
    (println "                 ]]])")

    (println "\n;; Blog List Schema")
    (println "(def blog-list-schema")
    (println "  [:sequential blog-post-schema])")

    (println "\n;; Discovered Aspects:")
    (doseq [aspect (sort all-aspects)]
      (println "  -" aspect))

    {:schema {:properties (vec (sort all-props))
              :aspects (vec (sort all-aspects))}
     :all-props all-props
     :all-aspects all-aspects}))

;; Main execution
(defn -main []
  (println "🚀 Mount Zion CMS - Blog Discovery Script")
  (println (apply str (repeat 80 "=")) "\n")

  (let [analysis (analyze-blog)]
    (when (:posts analysis)
      (let [schema (generate-malli-schema (:posts analysis))]

        ;; Save results to file
        (let [output-file "admin/docs/BLOG_SCHEMA.edn"
              output-data {:analyzed-at (str (java.time.Instant/now))
                          :total-posts (:total analysis)
                          :posts (:posts analysis)
                          :schema (:schema schema)}]
          (spit output-file (with-out-str (pprint/pprint output-data)))
          (println "\n💾 Results saved to:" output-file))

        (println "\n✅ Blog discovery complete!")))))

;; Run main
(-main)
