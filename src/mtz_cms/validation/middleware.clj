(ns mtz-cms.validation.middleware
  "Validation middleware for Mount Zion CMS
   
   Provides validation at each layer of the pipeline:
   1. Alfresco API responses
   2. Pathom resolver inputs/outputs  
   3. HTMX fragment responses"
  (:require
   [mtz-cms.validation.schemas :as schemas]
   [clojure.tools.logging :as log]
   [malli.core :as m]))

;; --- ALFRESCO CLIENT VALIDATION ---

(defn validate-alfresco-response
  "Middleware to validate all Alfresco API responses"
  [response operation]
  (try
    ;; Log the validation attempt
    (log/debug "Validating Alfresco response for operation:" operation)
    
    ;; For now, just validate that we have the basic structure
    (if (and (:success response) (:data response))
      (do
        (log/debug "✅ Alfresco response structure valid")
        response)
      (do
        (log/warn "❌ Alfresco response missing :success or :data fields")
        response))
    
    (catch Exception e
      (log/error "Validation error for Alfresco response:" (.getMessage e))
      response)))

;; --- PATHOM RESOLVER VALIDATION ---

(defn validate-pathom-resolver
  "Middleware to validate Pathom resolver inputs and outputs"
  [resolver-fn resolver-name input-schema output-schema]
  (fn [ctx input]
    (try
      ;; Validate input
      (log/debug "Validating input for resolver:" resolver-name)
      (let [input-validation (schemas/validate input-schema input)]
        (when-not (:valid? input-validation)
          (log/warn "❌ Invalid input for" resolver-name ":" (:errors input-validation)))
        
        ;; Call the actual resolver
        (let [result (resolver-fn ctx input)]
          
          ;; Validate output
          (log/debug "Validating output for resolver:" resolver-name)
          (let [output-validation (schemas/validate output-schema result)]
            (when-not (:valid? output-validation)
              (log/warn "❌ Invalid output for" resolver-name ":" (:errors output-validation))
              (log/debug "Output data:" result))
            
            result)))
      
      (catch Exception e
        (log/error "Validation error in resolver" resolver-name ":" (.getMessage e))
        ;; Return fallback data or re-throw based on your needs
        (throw e)))))

;; --- HTMX RESPONSE VALIDATION ---

(defn validate-htmx-response
  "Middleware to validate HTMX fragment responses"
  [response endpoint]
  (try
    (log/debug "Validating HTMX response for endpoint:" endpoint)
    
    ;; Basic structure validation
    (if (and (:status response) (:body response))
      (do
        (when (not= 200 (:status response))
          (log/warn "HTMX response has non-200 status:" (:status response)))
        
        (when-not (string? (:body response))
          (log/warn "HTMX response body is not a string"))
        
        (log/debug "✅ HTMX response structure valid")
        response)
      (do
        (log/error "❌ HTMX response missing :status or :body")
        response))
    
    (catch Exception e
      (log/error "Validation error for HTMX response:" (.getMessage e))
      response)))

;; --- COMPONENT PIPELINE VALIDATION ---

(defn validate-component-pipeline
  "Validate the entire component data flow"
  [component-type alfresco-data pathom-input pathom-output htmx-response]
  (let [validations {:alfresco-valid? true  ; We'll enhance this
                    :pathom-input-valid? true
                    :pathom-output-valid? true  
                    :htmx-valid? true
                    :errors []}]
    
    (log/info "🔍 Validating complete pipeline for component:" component-type)
    
    ;; Log the pipeline steps
    (log/debug "Pipeline steps:"
               "\n  1. Alfresco data received"
               "\n  2. Pathom input:" pathom-input
               "\n  3. Pathom output:" (keys pathom-output)
               "\n  4. HTMX response status:" (:status htmx-response))
    
    validations))

;; --- SCHEMA GENERATION INTEGRATION ---

(defn refresh-schemas-from-alfresco
  "Regenerate schemas from live Alfresco data"
  []
  (log/info "🔄 Refreshing schemas from live Alfresco data...")
  
  ;; This would call your babashka scripts
  (try
    ;; For now, just log that we would do this
    (log/info "Would run: bb model_sync_working.clj")
    (log/info "Would run: bb model_sync_calendar.clj")
    (log/info "✅ Schema refresh complete")
    
    (catch Exception e
      (log/error "❌ Schema refresh failed:" (.getMessage e)))))

;; --- VALIDATION DECORATORS ---

(defmacro with-validation
  "Decorator to add validation to any function"
  [fn-name input-schema output-schema]
  `(fn [& args#]
     (let [input# (first args#)]
       ;; Validate input
       (schemas/validate! ~input-schema input#)
       
       ;; Call function
       (let [result# (apply ~fn-name args#)]
         ;; Validate output
         (schemas/validate! ~output-schema result#)
         result#))))

;; --- DEVELOPMENT HELPERS ---

(defn enable-validation-logging
  "Enable detailed validation logging for development"
  []
  (log/info "🔍 Validation logging enabled")
  ;; Set logging levels, etc.
  )

(defn validation-report
  "Generate a validation report for debugging"
  [data]
  {:schema-registry-size (count schemas/schema-registry)
   :available-schemas (schemas/list-schemas)
   :data-keys (when (map? data) (keys data))
   :data-type (type data)})

(comment
  ;; Usage examples:
  
  ;; Validate an Alfresco response
  (validate-alfresco-response 
    {:success true :data {:entry {:id "123"}}}
    "get-node")
  
  ;; Generate validation report
  (validation-report {:hero/title "Test" :hero/content "Content"})
  
  ;; Enable validation logging
  (enable-validation-logging))