(ns mtz-cms.alfresco.blog-resolvers
  "Pathom resolvers for blog functionality with Malli validation

   Handles:
   - Blog post list retrieval from Alfresco Sites/swsdp/blog
   - Individual blog post details with content
   - Transformation from Alfresco structure to CMS display format"
  (:require
   [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.alfresco.content-processor :as processor]
   [mtz-cms.validation.schemas :as schemas]
   [mtz-cms.cache.simple :as cache]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- CONFIGURATION ---

(def blog-folder-id
  "Blog container ID from Sites/swsdp/blog"
  "f5e1e5ed-ba6f-471d-a1e5-edba6fe71db1")

;; --- TRANSFORMATION HELPERS ---

(defn extract-thumbnail-id
  "Extract thumbnail node ID from cm:lastThumbnailModification
   Format: [\"doclib:node-id\" \"pdf:node-id\"]
   Returns first node ID or nil"
  [thumbnail-data]
  (when (and thumbnail-data (sequential? thumbnail-data) (seq thumbnail-data))
    (let [first-thumb (first thumbnail-data)]
      (when (string? first-thumb)
        (-> first-thumb
            (str/split #":")
            second)))))

(defn format-published-date
  "Format published date for display
   Input: \"2025-10-04T02:13:53.260+0000\"
   Output: Keep ISO format for now, can enhance later"
  [date-str]
  date-str)

(defn generate-excerpt
  "Generate excerpt from content or description
   Takes first 200 characters and adds ellipsis if truncated"
  [content description]
  (let [source (or description content "")
        ;; Strip HTML tags
        clean (-> source
                 (str/replace #"<[^>]+>" " ")
                 (str/replace #"\s+" " ")
                 str/trim)
        max-length 200]
    (if (> (count clean) max-length)
      (str (subs clean 0 max-length) "...")
      clean)))

(defn transform-alfresco-post-to-display
  "Transform Alfresco blog post data to CMS display format

   Input: Raw Alfresco node data
   Output: Display-ready blog post map with :blog/ namespaced keys"
  [post-data]
  (let [thumbnail-id (extract-thumbnail-id
                      (get-in post-data [:properties :cm:lastThumbnailModification]))
        excerpt (generate-excerpt
                 nil ; content not yet loaded for list view
                 (:description post-data))
        tags (let [tag-data (get-in post-data [:properties :cm:taggable])]
               (cond
                 (sequential? tag-data) (vec tag-data)
                 (string? tag-data) [tag-data]
                 :else []))]

    {:blog/id (:node-id post-data)
     :blog/slug (:name post-data)
     :blog/title (or (:title post-data)
                    (get-in post-data [:properties :cm:title])
                    "Untitled Post")
     :blog/description (:description post-data)
     :blog/excerpt excerpt
     :blog/published-at (format-published-date
                         (get-in post-data [:properties :cm:published]))
     :blog/updated-at (format-published-date
                       (get-in post-data [:properties :cm:updated]))
     :blog/author (:author post-data)
     :blog/tags tags
     :blog/thumbnail (when thumbnail-id
                      (str "/api/image/" thumbnail-id))}))

(defn extract-blog-post-from-alfresco
  "Extract blog post data from Alfresco node entry

   Input: Alfresco API node entry
   Output: Intermediate blog post map for validation"
  [entry]
  (let [node (:entry entry)]
    {:node-id (:id node)
     :name (:name node)
     :title (get-in node [:properties :cm:title])
     :description (get-in node [:properties :cm:description])
     :created (:createdAt node)
     :modified (:modifiedAt node)
     :author (get-in node [:createdByUser :displayName])
     :aspects (vec (:aspectNames node))
     :properties (:properties node)}))

;; --- PATHOM RESOLVERS ---

(defresolver blog-list-resolver
  "Get list of blog posts from Alfresco

   Returns sorted list of published blog posts ready for display"
  [{:keys [ctx]} _]
  {::pco/output [{:blog/list [:blog/id
                              :blog/slug
                              :blog/title
                              :blog/description
                              :blog/excerpt
                              :blog/published-at
                              :blog/updated-at
                              :blog/author
                              :blog/tags
                              :blog/thumbnail]}]}
  (try
    (log/info "📚 Fetching blog list from Alfresco...")

    (let [;; Fetch blog posts from Alfresco
          children-result (alfresco/get-node-children
                           ctx
                           blog-folder-id
                           {:include "properties,aspectNames"})

          success? (:success children-result)]

      (if success?
        (let [entries (get-in children-result [:data :list :entries])

              ;; Extract and transform blog posts
              raw-posts (map extract-blog-post-from-alfresco entries)

              ;; Validate raw data
              _ (doseq [post raw-posts]
                  (let [validation (schemas/validate :blog/post-alfresco post)]
                    (when-not (:valid? validation)
                      (log/warn "Blog post validation failed for" (:node-id post)
                               ":" (:errors validation)))))

              ;; Transform to display format
              display-posts (map transform-alfresco-post-to-display raw-posts)

              ;; Filter published posts only
              published-posts (filter #(some? (:blog/published-at %)) display-posts)

              ;; Sort by published date (newest first)
              sorted-posts (sort-by :blog/published-at
                                   (fn [a b] (compare b a))
                                   published-posts)]

          (log/info "✅ Retrieved" (count sorted-posts) "published blog posts")

          {:blog/list sorted-posts})

        (do
          (log/error "❌ Failed to fetch blog posts:" (:error children-result))
          {:blog/list []})))

    (catch Exception e
      (log/error "❌ Blog list resolver error:" (.getMessage e))
      {:blog/list []})))

(defresolver blog-detail-by-id-resolver
  "Get blog post details by node ID

   Includes full content from Alfresco"
  [{:keys [ctx]} {:blog/keys [id]}]
  {::pco/input [:blog/id]
   ::pco/output [:blog/slug
                 :blog/title
                 :blog/content
                 :blog/description
                 :blog/published-at
                 :blog/updated-at
                 :blog/author
                 :blog/tags
                 :blog/thumbnail]}
  (try
    (log/info "📄 Fetching blog post details for ID:" id)

    (let [;; Fetch node data
          node-result (alfresco/get-node ctx id)
          success? (:success node-result)]

      (if success?
        (let [node-data (get-in node-result [:data :entry])

              ;; Extract blog post data
              raw-post {:node-id (:id node-data)
                       :name (:name node-data)
                       :title (get-in node-data [:properties :cm:title])
                       :description (get-in node-data [:properties :cm:description])
                       :created (:createdAt node-data)
                       :modified (:modifiedAt node-data)
                       :author (get-in node-data [:createdByUser :displayName])
                       :aspects (vec (:aspectNames node-data))
                       :properties (:properties node-data)}

              ;; Fetch content
              content-result (alfresco/get-node-content ctx id)
              content (if (:success content-result)
                       (let [content-str (if (bytes? (:data content-result))
                                          (String. (:data content-result) "UTF-8")
                                          (str (:data content-result)))]
                         (processor/process-html-content content-str))
                       "")

              ;; Transform to display format
              display-post (transform-alfresco-post-to-display raw-post)

              ;; Add content
              result (assoc display-post :blog/content content)]

          (log/info "✅ Retrieved blog post:" (:blog/title result))
          result)

        (do
          (log/error "❌ Failed to fetch blog post:" (:error node-result))
          {:blog/slug ""
           :blog/title "Post Not Found"
           :blog/content ""
           :blog/description nil
           :blog/published-at nil
           :blog/updated-at nil
           :blog/author nil
           :blog/tags []
           :blog/thumbnail nil})))

    (catch Exception e
      (log/error "❌ Blog detail resolver error:" (.getMessage e))
      {:blog/slug ""
       :blog/title "Error Loading Post"
       :blog/content ""
       :blog/description nil
       :blog/published-at nil
       :blog/updated-at nil
       :blog/author nil
       :blog/tags []
       :blog/thumbnail nil})))

(defresolver blog-detail-by-slug-resolver
  "Get blog post by slug

   First finds post by slug in blog list, then fetches full details"
  [env {:blog/keys [slug]}]
  {::pco/input [:blog/slug]
   ::pco/output [:blog/id]}
  (try
    (log/info "🔍 Looking up blog post by slug:" slug)

    ;; Get blog list to find post with matching slug
    (let [blog-list-result (blog-list-resolver env {})
          posts (:blog/list blog-list-result)
          matching-post (first (filter #(= (:blog/slug %) slug) posts))]

      (if matching-post
        (do
          (log/info "✅ Found post with slug:" slug "ID:" (:blog/id matching-post))
          {:blog/id (:blog/id matching-post)})
        (do
          (log/warn "⚠️ No post found with slug:" slug)
          {:blog/id nil})))

    (catch Exception e
      (log/error "❌ Blog slug lookup error:" (.getMessage e))
      {:blog/id nil})))

;; --- RESOLVER COLLECTION ---

(def blog-resolvers
  "Collection of all blog resolvers"
  [blog-list-resolver
   blog-detail-by-id-resolver
   blog-detail-by-slug-resolver])
