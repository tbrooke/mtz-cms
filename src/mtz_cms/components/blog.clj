(ns mtz-cms.components.blog
  "Blog components for Pastor Jim Reflects blog section"
  (:require
   [clojure.string :as str]
   [mtz-cms.ui.pages :as pages]))

;; --- BLOG LIST ITEM ---

(defn blog-list-item
  "Blog list item with thumbnail on left

   Design:
   - Thumbnail image on left (fixed width)
   - Content on right (title, excerpt, metadata)
   - Full-width clickable area
   - Horizontal borders top/bottom

   Data structure:
   {:blog/id \"node-id\"
    :blog/slug \"post-slug\"
    :blog/title \"Blog Post Title\"
    :blog/excerpt \"Post excerpt...\"
    :blog/published-at \"2025-10-04...\"
    :blog/author \"Tom Brooke\"
    :blog/thumbnail \"/api/image/...\" or nil}"
  [post]
  (let [thumbnail-url (or (:blog/thumbnail post) "/images/blog-default.svg")
        post-url (str "/blog/" (:blog/slug post))
        published-date (:blog/published-at post)
        ;; Format date - for now keep ISO, can enhance later
        formatted-date (when published-date
                        (-> published-date
                            (str/split #"T")
                            first))]

    [:a {:href post-url
         :class "block border-t border-gray-200 hover:bg-gray-50 transition-colors"}
     [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6"}
      [:div {:class "flex gap-6"}

       ;; Thumbnail on left
       [:div {:class "flex-shrink-0"}
        [:img {:src thumbnail-url
               :alt (:blog/title post)
               :class "w-32 h-32 object-cover rounded-lg"}]]

       ;; Content on right
       [:div {:class "flex-1 min-w-0"}
        ;; Title
        [:h3 {:class "text-xl font-semibold text-gray-900 mb-2"}
         (:blog/title post)]

        ;; Date and Author
        [:div {:class "flex items-center text-sm text-gray-500 mb-3"}
         (when formatted-date
           [:time {:datetime (:blog/published-at post)
                   :class ""}
            (str formatted-date)])

         (when (:blog/author post)
           [:span {:class "mx-2"} " • "]
           [:span (:blog/author post)])]

        ;; Excerpt
        (when (:blog/excerpt post)
          [:p {:class "text-gray-600 line-clamp-3"}
           (:blog/excerpt post)])]]]]))

;; --- BLOG LIST PAGE ---

(defn blog-list-page
  "Blog list page with 'Pastor Jim Reflects' heading

   Shows list of all blog posts with thumbnails"
  [posts]
  [:div {:class "bg-white min-h-screen"}
   ;; Page header
   [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 border-b border-gray-200"}
    [:h1 {:class "text-4xl font-bold text-gray-900 mb-2"}
     "Pastor Jim Reflects"]
    [:p {:class "text-lg text-gray-600"}
     "Thoughts and reflections from Pastor Jim"]]

   ;; Blog list
   [:div {:class "divide-y divide-gray-200"}
    (if (seq posts)
      (for [post posts]
        [:div {:key (:blog/id post)}
         (blog-list-item post)])

      ;; Empty state
      [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center"}
       [:p {:class "text-gray-500"}
        "No blog posts yet. Check back soon!"]])]

   ;; Bottom border
   [:div {:class "border-b border-gray-200"}]])

;; --- BLOG DETAIL PAGE ---

(defn blog-detail-page
  "Blog detail page with image at top left

   Design:
   - 'Pastor Jim Reflects' heading
   - Image on top left, title on right
   - Full blog content below
   - Metadata (date, author) under title"
  [post]
  [:div {:class "bg-white min-h-screen"}
   ;; Page header
   [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 border-b border-gray-200"}
    [:h1 {:class "text-3xl font-bold text-gray-900"}
     "Pastor Jim Reflects"]]

   ;; Post header with image
   [:div {:class "max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8"}
    [:div {:class "flex gap-8 mb-8"}

     ;; Image on top left
     (when (:blog/thumbnail post)
       [:div {:class "flex-shrink-0"}
        [:img {:src (:blog/thumbnail post)
               :alt (:blog/title post)
               :class "w-48 h-48 object-cover rounded-lg"}]])

     ;; Title and metadata on right
     [:div {:class "flex-1 min-w-0"}
      [:h2 {:class "text-3xl font-bold text-gray-900 mb-4"}
       (:blog/title post)]

      ;; Date and author
      [:div {:class "flex items-center text-gray-600 mb-4"}
       (when (:blog/published-at post)
         [:time {:datetime (:blog/published-at post)}
          (str (-> (:blog/published-at post)
                   (str/split #"T")
                   first))])

       (when (:blog/author post)
         [:span {:class "mx-2"} " • "]
         [:span (str "By " (:blog/author post))])]

      ;; Tags if available
      (when (seq (:blog/tags post))
        [:div {:class "flex flex-wrap gap-2"}
         (for [tag (:blog/tags post)]
           [:span {:key tag
                   :class "inline-block bg-blue-100 text-blue-800 text-sm px-3 py-1 rounded-full"}
            tag])])]]

    ;; Blog content
    [:article {:class "prose max-w-none mt-8"}
     ;; Content is raw HTML from Alfresco
     (when-let [content (:blog/content post)]
       (pages/raw-html content))]

    ;; Back to blog link
    [:div {:class "mt-12 pt-8 border-t border-gray-200"}
     [:a {:href "/blog"
          :class "text-blue-600 hover:text-blue-800 font-medium"}
      "← Back to Pastor Jim Reflects"]]]])

;; --- UTILITY FUNCTIONS ---

(defn extract-first-image-from-content
  "Extract first image URL from blog HTML content
   Used as fallback if thumbnail not available"
  [html-content]
  (when html-content
    (let [img-match (re-find #"<img[^>]+src=[\"']([^\"']+)[\"']" html-content)]
      (second img-match))))

(defn get-thumbnail-or-default
  "Get thumbnail URL with fallback to content image or default"
  [post]
  (or (:blog/thumbnail post)
      (extract-first-image-from-content (:blog/content post))
      "/images/blog-default.svg"))
