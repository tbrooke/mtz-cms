(ns mtz-cms.validation.dashboard
  "Validation dashboard for monitoring Mount Zion CMS data pipeline"
  (:require
   [mtz-cms.validation.schemas :as schemas]
   [mtz-cms.validation.middleware :as validation]
   [hiccup.core :as hiccup]
   [clojure.tools.logging :as log]))

;; --- VALIDATION METRICS ---

(def validation-metrics
  "Atom to track validation metrics"
  (atom {:total-validations 0
         :successful-validations 0
         :failed-validations 0
         :by-schema {}
         :recent-errors []}))

(defn record-validation
  "Record a validation attempt"
  [schema-key success? error]
  (swap! validation-metrics
         (fn [metrics]
           (-> metrics
               (update :total-validations inc)
               (update (if success? :successful-validations :failed-validations) inc)
               (update-in [:by-schema schema-key] 
                         (fn [schema-stats]
                           (-> (or schema-stats {:total 0 :success 0 :failed 0})
                               (update :total inc)
                               (update (if success? :success :failed) inc))))
               (update :recent-errors 
                      (fn [errors]
                        (if error
                          (take 10 (conj errors {:timestamp (java.util.Date.)
                                                 :schema schema-key
                                                 :error error}))
                          errors)))))))

;; --- DASHBOARD HTML ---

(defn validation-dashboard-page []
  "Generate HTML for validation dashboard"
  (let [metrics @validation-metrics
        success-rate (if (> (:total-validations metrics) 0)
                      (/ (:successful-validations metrics) 
                         (:total-validations metrics))
                      0)]
    [:html
     [:head
      [:title "Mount Zion CMS - Validation Dashboard"]
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:script {:src "https://cdn.tailwindcss.com"}]
      [:script {:src "https://unpkg.com/htmx.org@1.9.6"}]
      [:style "
        .metric-card { @apply bg-white rounded-lg shadow p-6 border-l-4; }
        .metric-success { @apply border-green-500; }
        .metric-error { @apply border-red-500; }
        .metric-info { @apply border-blue-500; }
      "]]
     
     [:body {:class "bg-gray-50 min-h-screen"}
      [:header {:class "bg-blue-600 text-white shadow"}
       [:div {:class "max-w-7xl mx-auto px-4 py-6"}
        [:h1 {:class "text-2xl font-bold"} "Mount Zion CMS - Validation Dashboard"]
        [:p {:class "text-blue-100"} "Monitor data pipeline health and validation metrics"]]]
      
      [:main {:class "max-w-7xl mx-auto px-4 py-8"}
       
       ;; Overall metrics
       [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-6 mb-8"}
        [:div {:class "metric-card metric-info"}
         [:h3 {:class "text-lg font-semibold text-gray-900"} "Total Validations"]
         [:p {:class "text-3xl font-bold text-blue-600"} (:total-validations metrics)]]
        
        [:div {:class "metric-card metric-success"}
         [:h3 {:class "text-lg font-semibold text-gray-900"} "Successful"]
         [:p {:class "text-3xl font-bold text-green-600"} (:successful-validations metrics)]]
        
        [:div {:class "metric-card metric-error"}
         [:h3 {:class "text-lg font-semibold text-gray-900"} "Failed"]
         [:p {:class "text-3xl font-bold text-red-600"} (:failed-validations metrics)]]
        
        [:div {:class "metric-card metric-info"}
         [:h3 {:class "text-lg font-semibold text-gray-900"} "Success Rate"]
         [:p {:class "text-3xl font-bold text-blue-600"} 
          (str (int (* success-rate 100)) "%")]]]
       
       ;; Schema registry
       [:div {:class "bg-white rounded-lg shadow mb-8"}
        [:div {:class "px-6 py-4 border-b border-gray-200"}
         [:h2 {:class "text-xl font-semibold text-gray-900"} "Schema Registry"]
         [:p {:class "text-gray-600"} "Available schemas for validation"]]
        
        [:div {:class "p-6"}
         [:div {:class "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"}
          (for [schema-key (schemas/list-schemas)]
            [:div {:key schema-key :class "bg-gray-50 rounded p-4"}
             [:h4 {:class "font-medium text-gray-900"} (str schema-key)]
             [:p {:class "text-sm text-gray-600 mt-1"} 
              "Fields: " (count (rest (schemas/explain-schema schema-key)))]
             (when-let [stats (get-in metrics [:by-schema schema-key])]
               [:div {:class "mt-2 text-xs"}
                [:span {:class "text-green-600"} "✓ " (:success stats 0)]
                [:span {:class "text-red-600 ml-2"} "✗ " (:failed stats 0)]])])]]]
       
       ;; Recent errors
       (when (seq (:recent-errors metrics))
         [:div {:class "bg-white rounded-lg shadow"}
          [:div {:class "px-6 py-4 border-b border-gray-200"}
           [:h2 {:class "text-xl font-semibold text-gray-900"} "Recent Validation Errors"]
           [:p {:class "text-gray-600"} "Latest validation failures for debugging"]]
          
          [:div {:class "p-6"}
           [:div {:class "space-y-4"}
            (for [error (:recent-errors metrics)]
              [:div {:key (str (:timestamp error)) :class "bg-red-50 border border-red-200 rounded p-4"}
               [:div {:class "flex justify-between items-start"}
                [:div
                 [:h4 {:class "font-medium text-red-900"} (str (:schema error))]
                 [:p {:class "text-sm text-red-700 mt-1"} (str (:error error))]]
                [:span {:class "text-xs text-red-600"} (str (:timestamp error))]]])]]])
       
       ;; Actions
       [:div {:class "bg-white rounded-lg shadow"}
        [:div {:class "px-6 py-4 border-b border-gray-200"}
         [:h2 {:class "text-xl font-semibold text-gray-900"} "Actions"]]
        
        [:div {:class "p-6 space-y-4"}
         [:button {:class "bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                   :hx-post "/api/validation/refresh-schemas"
                   :hx-target "#notification-area"}
          "🔄 Refresh Schemas from Alfresco"]
         
         [:button {:class "bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 ml-4"
                   :hx-post "/api/validation/test-pipeline"
                   :hx-target "#notification-area"}
          "🧪 Test Full Pipeline"]
         
         [:button {:class "bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 ml-4"
                   :hx-post "/api/validation/clear-metrics"
                   :hx-target "#notification-area"}
          "🗑️ Clear Metrics"]]]
       
       [:div {:id "notification-area" :class "fixed top-4 right-4 z-50"}]]]]))

;; --- API HANDLERS ---

(defn dashboard-handler [request]
  "Serve validation dashboard"
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html (validation-dashboard-page))})

(defn refresh-schemas-handler [request]
  "Refresh schemas from Alfresco"
  (try
    (validation/refresh-schemas-from-alfresco)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (hiccup/html
            [:div {:class "bg-green-50 border border-green-200 rounded p-4"}
             [:p {:class "text-green-800"} "✅ Schemas refreshed successfully"]])}
    (catch Exception e
      {:status 500
       :headers {"Content-Type" "text/html"}
       :body (hiccup/html
              [:div {:class "bg-red-50 border border-red-200 rounded p-4"}
               [:p {:class "text-red-800"} "❌ Schema refresh failed: " (.getMessage e)]])})))

(defn test-pipeline-handler [request]
  "Test the full validation pipeline"
  (try
    (log/info "🧪 Testing validation pipeline")
    
    ;; Test hero component pipeline
    (let [test-node-id "39985c5c-201a-42f6-985c-5c201a62f6d8"
          test-result (schemas/validate :hero/input {:hero/node-id test-node-id})]
      
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (hiccup/html
              [:div {:class "bg-blue-50 border border-blue-200 rounded p-4"}
               [:p {:class "text-blue-800"} 
                (if (:valid? test-result)
                  "✅ Pipeline test successful"
                  "❌ Pipeline test failed")]])})
    
    (catch Exception e
      {:status 500
       :headers {"Content-Type" "text/html"}
       :body (hiccup/html
              [:div {:class "bg-red-50 border border-red-200 rounded p-4"}
               [:p {:class "text-red-800"} "❌ Pipeline test failed: " (.getMessage e)]])})))

(defn clear-metrics-handler [request]
  "Clear validation metrics"
  (reset! validation-metrics {:total-validations 0
                             :successful-validations 0
                             :failed-validations 0
                             :by-schema {}
                             :recent-errors []})
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html
          [:div {:class "bg-blue-50 border border-blue-200 rounded p-4"}
           [:p {:class "text-blue-800"} "✅ Metrics cleared"]])})

;; --- DASHBOARD ROUTES ---

(def dashboard-routes
  "Routes for validation dashboard"
  [["/validation"
    ["/dashboard" {:get dashboard-handler}]
    ["/refresh-schemas" {:post refresh-schemas-handler}]
    ["/test-pipeline" {:post test-pipeline-handler}]
    ["/clear-metrics" {:post clear-metrics-handler}]]])

(comment
  ;; Usage:
  
  ;; Record a validation result
  (record-validation :hero/input true nil)
  (record-validation :hero/output false "Missing required field")
  
  ;; View current metrics
  @validation-metrics)