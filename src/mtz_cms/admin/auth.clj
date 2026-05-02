(ns mtz-cms.admin.auth
  (:require
   [mtz-cms.alfresco.client :as alfresco]
   [hiccup.core :as hiccup]
   [ring.util.response :as response]
   [clojure.tools.logging :as log]))

(defn get-session-user [request]
  (get-in request [:session :admin/user]))

(defn wrap-admin-auth
  "Ring middleware: redirect to /admin/login if no session."
  [handler]
  (fn [request]
    (if (get-session-user request)
      (handler request)
      (response/redirect "/admin/login"))))

(defn- login-page [& [error?]]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:title "Admin Login — Mt Zion"]
    [:link {:rel "stylesheet" :href "/css/styles.css"}]]
   [:body {:class "bg-slate-100 min-h-screen flex items-center justify-center"}
    [:div {:class "bg-white rounded-xl shadow-lg p-8 w-full max-w-sm"}
     [:div {:class "text-center mb-6"}
      [:h1 {:class "text-2xl font-bold text-slate-800"} "Mt Zion Admin"]
      [:p {:class "text-slate-500 text-sm mt-1"} "Church Content Management"]]
     (when error?
       [:div {:class "mb-4 bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-2 rounded-lg"}
        "Invalid username or password."])
     [:form {:method "post" :action "/admin/login" :class "space-y-4"}
      [:div
       [:label {:for "username" :class "block text-sm font-medium text-slate-700 mb-1"} "Username"]
       [:input {:type "text" :name "username" :id "username" :autofocus true :required true
                :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]
      [:div
       [:label {:for "password" :class "block text-sm font-medium text-slate-700 mb-1"} "Password"]
       [:input {:type "password" :name "password" :id "password" :required true
                :class "w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"}]]
      [:button {:type "submit"
                :class "w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg text-sm transition-colors"}
       "Sign In"]]]]])

(defn login-handler [request]
  (let [error? (= "1" (get-in request [:params :error]))]
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (str "<!DOCTYPE html>" (hiccup/html (login-page error?)))}))

(defn login-submit-handler [request]
  (let [{:keys [username password]} (:params request)
        result (alfresco/authenticate "http://localhost:8080" username password)]
    (if (:valid? result)
      (do
        (log/info "Admin login:" (:user-id result))
        (-> (response/redirect "/admin")
            (assoc :session {:admin/user (:user-id result)})))
      (response/redirect "/admin/login?error=1"))))

(defn logout-handler [_request]
  (-> (response/redirect "/admin/login")
      (assoc :session {})))
