(ns mtz-cms.admin.blog
  (:require
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.admin.layout :as layout]
   [mtz-cms.config.core :as config]
   [ring.util.response :as response]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

(def ^:private blog-folder (config/get-node-id :blog))
(defn- ctx [] {})

(defn- fmt-date [iso]
  (when (and iso (> (count iso) 9)) (subs iso 0 10)))

;; --- LIST ---

(defn- post-row [entry]
  (let [id    (get-in entry [:entry :id])
        props (get-in entry [:entry :properties])
        title (or (:cm:title props) (get-in entry [:entry :name]) "Untitled")
        author (or (:cm:author props) "—")
        date   (fmt-date (get-in entry [:entry :modifiedAt]))]
    [:tr {:class "border-b border-slate-100 hover:bg-slate-50"}
     [:td {:class "py-3 px-4"}
      [:div {:class "font-medium text-slate-800 text-sm"} title]
      (when-let [d (:cm:description props)]
        [:div {:class "text-xs text-slate-400 mt-0.5 truncate max-w-xs"} d])]
     [:td {:class "py-3 px-4 text-sm text-slate-500"} author]
     [:td {:class "py-3 px-4 text-sm text-slate-500"} date]
     [:td {:class "py-3 px-4"}
      [:div {:class "flex gap-2"}
       [:a {:href  (str "/admin/blog/edit/" id)
            :class "text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 px-3 py-1 rounded-md"}
        "Edit"]
       [:button {:hx-delete   (str "/admin/blog/delete/" id)
                 :hx-confirm  (str "Delete " " title " "? This moves it to trash.")
                 :hx-target   "closest tr"
                 :hx-swap     "outerHTML swap:300ms"
                 :class       "text-xs bg-red-50 hover:bg-red-100 text-red-600 px-3 py-1 rounded-md"}
        "Delete"]]]]))

(defn blog-list-handler [request]
  (let [result (alfresco/get-node-children
                (ctx) blog-folder
                {:include "properties" :maxItems 100 :where "(isFile=true)"
                 :orderBy "modifiedAt DESC"})]
    (layout/html-response
     (layout/admin-page
      "Blog Posts"
      (if (:success result)
        (let [posts (get-in result [:data :list :entries])]
          [:div
           [:div {:class "flex items-center justify-between mb-6"}
            [:div
             [:h1 {:class "text-xl font-bold text-slate-800"} "Blog Posts"]
             [:p {:class "text-sm text-slate-500 mt-0.5"}
              (str (count posts) " post" (when (not= 1 (count posts)) "s"))]]
            [:a {:href  "/admin/blog/new"
                 :class "bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg"}
             "+ New Post"]]
           [:div {:class "bg-white rounded-xl shadow-sm overflow-hidden"}
            [:table {:class "w-full"}
             [:thead
              [:tr {:class "border-b border-slate-200 bg-slate-50"}
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Title"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Author"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} "Modified"]
               [:th {:class "text-left py-3 px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider"} ""]]]
             [:tbody
              (if (seq posts)
                (map post-row posts)
                [:tr [:td {:colspan 4 :class "py-10 text-center text-slate-400 text-sm"}
                      "No posts yet."]])]]]])
        [:p {:class "text-red-600 text-sm"} "Could not load posts from Alfresco."])
      :current-path "/admin/blog"))))

;; --- FORM ---

(defn- toolbar-btn [action label]
  [:button {:type "button" :data-tiptap-action action
            :class "px-2 py-1 text-xs border border-slate-300 rounded hover:bg-slate-100 text-slate-700"}
   label])

(defn- blog-form [post-id props content]
  (layout/admin-page
   (if post-id "Edit Post" "New Post")
   [:div
    [:div {:class "flex items-center gap-2 mb-6 text-sm"}
     [:a {:href "/admin/blog" :class "text-slate-400 hover:text-slate-600"} "Blog Posts"]
     [:span {:class "text-slate-300"} "/"]
     [:span {:class "text-slate-600"} (if post-id "Edit Post" "New Post")]]

    [:form {:method "post"
            :action (if post-id (str "/admin/blog/save/" post-id) "/admin/blog/create")
            :class  "space-y-5"}

     [:div
      [:label {:for "title" :class "block text-sm font-medium text-slate-700 mb-1"} "Title *"]
      [:input {:type "text" :name "title" :id "title" :required true
               :value (or (:cm:title props) "")
               :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]

     [:div
      [:label {:for "description" :class "block text-sm font-medium text-slate-700 mb-1"} "Excerpt"]
      [:input {:type "text" :name "description" :id "description"
               :value (or (:cm:description props) "")
               :placeholder "Short summary shown in post listings"
               :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]

     [:div
      [:label {:for "author" :class "block text-sm font-medium text-slate-700 mb-1"} "Author"]
      [:input {:type "text" :name "author" :id "author"
               :value (or (:cm:author props) "Jim Simonds")
               :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]

     [:div
      [:label {:class "block text-sm font-medium text-slate-700 mb-1"} "Post Content"]
      ;; Toolbar
      [:div {:class "flex gap-1 mb-1 flex-wrap"}
       (toolbar-btn "bold" "B")
       (toolbar-btn "italic" "I")
       (toolbar-btn "heading2" "H2")
       (toolbar-btn "heading3" "H3")
       (toolbar-btn "bulletList" "• List")
       (toolbar-btn "orderedList" "1. List")
       (toolbar-btn "blockquote" "❝")]
      ;; Hidden input carries the HTML value on form submit
      [:input {:type "hidden" :name "body" :id "body-content" :value (or content "")}]
      ;; TipTap mount point
      [:div {:id           "tiptap-editor"
             :data-tiptap-input "body-content"
             :class        "border border-slate-300 rounded-lg p-3 bg-white min-h-64"}]]

     [:div {:class "flex items-center gap-3 pt-2"}
      [:button {:type  "submit"
                :class "bg-blue-600 hover:bg-blue-700 text-white font-medium px-5 py-2 rounded-lg text-sm"}
       (if post-id "Save Changes" "Create Post")]
      [:a {:href "/admin/blog" :class "text-slate-500 hover:text-slate-700 text-sm px-4 py-2"}
       "Cancel"]]]]

   :current-path "/admin/blog"
   :with-editor? true))

;; --- HANDLERS ---

(defn blog-new-handler [_request]
  (layout/html-response (blog-form nil {} "")))

(defn blog-edit-handler [request]
  (let [id             (get-in request [:path-params :id])
        node-result    (alfresco/get-node (ctx) id)
        content-result (alfresco/get-node-content (ctx) id)]
    (if (:success node-result)
      (let [props   (get-in node-result [:data :entry :properties])
            content (when (:success content-result)
                      (String. ^bytes (:data content-result) "UTF-8"))]
        (layout/html-response (blog-form id props (or content ""))))
      {:status 404 :headers {"Content-Type" "text/plain"} :body "Post not found"})))

(defn blog-create-handler [request]
  (let [{:keys [title description author body]} (:params request)
        node-name (str "post-" (System/currentTimeMillis) ".html")
        result    (alfresco/create-node
                   (ctx) blog-folder
                   {:name       node-name
                    :nodeType   "cm:content"
                    :properties {"cm:title"       title
                                 "cm:description" description
                                 "cm:author"      (or author "Jim Simonds")}})]
    (if (:success result)
      (let [node-id (get-in result [:data :entry :id])]
        (when (not (str/blank? body))
          (alfresco/upload-text-content (ctx) node-id body "text/html"))
        (response/redirect (str "/admin/blog/edit/" node-id) :see-other))
      (do
        (log/error "Failed to create blog post:" (:error result))
        {:status 500 :headers {"Content-Type" "text/plain"} :body "Failed to create post"}))))

(defn blog-save-handler [request]
  (let [id                    (get-in request [:path-params :id])
        {:keys [title description author body]} (:params request)
        update-result         (alfresco/update-node
                               (ctx) id
                               {:properties {"cm:title"       title
                                             "cm:description" description
                                             "cm:author"      author}})]
    (if (:success update-result)
      (do
        (when (not (str/blank? body))
          (alfresco/upload-text-content (ctx) id body "text/html"))
        (response/redirect (str "/admin/blog/edit/" id) :see-other))
      (do
        (log/error "Failed to save blog post:" (:error update-result))
        {:status 500 :headers {"Content-Type" "text/plain"} :body "Failed to save post"}))))

(defn blog-delete-handler [request]
  (let [id (get-in request [:path-params :id])]
    (alfresco/delete-node (ctx) id)
    {:status 200 :body ""}))
