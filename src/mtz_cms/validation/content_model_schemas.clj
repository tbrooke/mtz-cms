;; AUTO-GENERATED - DO NOT EDIT MANUALLY
;; Generated: 2025-09-30T22:25:55.639176Z
;; From: Alfresco Content Model XML
;; Run: bb workbench/bb/xml-to-malli.clj to regenerate

(ns mtz-cms.validation.content-model-schemas
  "Auto-generated Malli schemas from Alfresco Content Model"
  (:require [malli.core :as m]
            [malli.util :as mu]))

;; ========================================
;; TYPE SCHEMAS
;; ========================================



;; ========================================
;; ASPECT SCHEMAS
;; ========================================



;; ========================================
;; SCHEMA REGISTRY
;; ========================================

(def schema-registry
  "Complete schema registry"
  {:types {}
   :aspects {}})

(defn get-type-schema [type-id]
  (get-in schema-registry [:types type-id]))

(defn get-aspect-schema [aspect-id]
  (get-in schema-registry [:aspects aspect-id]))
