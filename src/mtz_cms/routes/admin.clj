(ns mtz-cms.routes.admin
  (:require
   [mtz-cms.admin.auth :as auth]
   [mtz-cms.admin.dashboard :as dashboard]
   [mtz-cms.admin.blog :as blog]
   [mtz-cms.admin.calendar :as cal]
   [mtz-cms.admin.history :as history]))

(defn- protected [handler]
  (fn [request]
    ((auth/wrap-admin-auth handler) request)))

(def admin-routes
  [;; Auth (no session required)
   ["/admin/login"  {:get auth/login-handler :post auth/login-submit-handler}]
   ["/admin/logout" {:get auth/logout-handler}]

   ;; Dashboard
   ["/admin" {:get (protected dashboard/dashboard-handler)}]

   ;; Blog
   ["/admin/blog"            {:get (protected blog/blog-list-handler)}]
   ["/admin/blog/new"        {:get (protected blog/blog-new-handler)}]
   ["/admin/blog/create"     {:post (protected blog/blog-create-handler)}]
   ["/admin/blog/edit/:id"   {:get (protected blog/blog-edit-handler)}]
   ["/admin/blog/save/:id"   {:post (protected blog/blog-save-handler)}]
   ["/admin/blog/delete/:id" {:delete (protected blog/blog-delete-handler)}]

   ;; Calendar
   ["/admin/calendar"            {:get (protected cal/calendar-list-handler)}]
   ["/admin/calendar/new"        {:get (protected cal/calendar-new-handler)}]
   ["/admin/calendar/create"     {:post (protected cal/calendar-create-handler)}]
   ["/admin/calendar/edit/:id"   {:get (protected cal/calendar-edit-handler)}]
   ["/admin/calendar/save/:id"   {:post (protected cal/calendar-save-handler)}]
   ["/admin/calendar/delete/:id" {:delete (protected cal/calendar-delete-handler)}]

   ;; History
   ["/admin/history"                {:get (protected history/history-list-handler)}]
   ["/admin/history/upload"         {:get (protected history/history-upload-handler)}]
   ["/admin/history/upload-submit"  {:post (protected history/history-upload-submit-handler)}]
   ["/admin/history/edit/:id"       {:get (protected history/history-edit-handler)}]
   ["/admin/history/save/:id"       {:post (protected history/history-save-handler)}]])
