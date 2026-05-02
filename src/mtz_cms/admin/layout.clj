(ns mtz-cms.admin.layout
  (:require
   [clojure.string :as str]
   [hiccup.core :as hiccup]))

(defn- nav-link [href label current-path]
  (let [active? (or (= current-path href)
                    (and (not= href "/admin")
                         (str/starts-with? current-path href)))
        cls (str "block px-3 py-2 rounded-lg text-sm transition-colors "
                 (if active?
                   "bg-slate-700 text-white font-medium"
                   "text-slate-300 hover:bg-slate-700 hover:text-white"))]
    [:a {:href href :class cls} label]))

(defn- nav-section [label]
  [:div {:class "pt-4 pb-1 px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider"}
   label])

(defn admin-page
  "Admin shell layout. Options: :current-path, :with-editor?"
  [title content & {:keys [current-path with-editor?]
                    :or   {current-path "" with-editor? false}}]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:title (str title " — Mt Zion Admin")]
    [:link {:rel "stylesheet" :href "/css/styles.css"}]
    (when with-editor?
      [:style ".ProseMirror{outline:none;min-height:200px;}
               .ProseMirror p{margin:.75em 0;}
               .ProseMirror h2{font-size:1.25em;font-weight:600;margin:1em 0 .5em;}
               .ProseMirror h3{font-size:1.1em;font-weight:600;margin:1em 0 .5em;}
               .ProseMirror ul,.ProseMirror ol{padding-left:1.5em;margin:.5em 0;}
               .ProseMirror strong{font-weight:600;}
               .ProseMirror em{font-style:italic;}"])]
   [:body {:class "bg-slate-100"}
    [:div {:class "flex min-h-screen"}

     ;; Sidebar
     [:aside {:class "w-56 bg-slate-800 text-white flex flex-col fixed inset-y-0 left-0 z-10"}
      [:div {:class "px-4 py-5 border-b border-slate-700"}
       [:a {:href "/admin"}
        [:div {:class "text-white font-bold text-base"} "Mt Zion"]
        [:div {:class "text-slate-400 text-xs mt-0.5"} "Content Admin"]]]
      [:nav {:class "flex-1 p-3 space-y-0.5 overflow-y-auto"}
       (nav-link "/admin" "Dashboard" current-path)
       (nav-section "Content")
       (nav-link "/admin/blog" "Blog Posts" current-path)
       (nav-link "/admin/calendar" "Calendar Events" current-path)
       (nav-section "Archive")
       (nav-link "/admin/history" "History" current-path)]
      [:div {:class "p-3 border-t border-slate-700 space-y-0.5"}
       [:a {:href "/" :target "_blank"
            :class "block px-3 py-2 text-xs text-slate-400 hover:text-white rounded-lg hover:bg-slate-700"}
        "↗ View Site"]
       [:a {:href "/admin/logout"
            :class "block px-3 py-2 text-xs text-slate-400 hover:text-white rounded-lg hover:bg-slate-700"}
        "Sign Out"]]]

     ;; Main area
     [:main {:class "ml-56 flex-1 min-h-screen"}
      [:div {:class "p-8 max-w-5xl mx-auto"}
       content]]]

    ;; Scripts at bottom
    [:script {:src "https://unpkg.com/htmx.org@1.9.11"}]
    (when with-editor?
      [:script {:src "/js/editor.bundle.js"}])]])

(defn html-response
  "Wrap an admin-page hiccup vector into an HTTP response map."
  [page-hiccup]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (str "<!DOCTYPE html>" (hiccup/html page-hiccup))})
