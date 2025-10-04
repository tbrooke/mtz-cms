;; AUTO-GENERATED - DO NOT EDIT MANUALLY
;; Generated: 2025-10-04T01:43:36.691427Z
;; From: Alfresco Content Model XML
;; Run: bb workbench/bb/xml-to-malli.clj to regenerate

(ns mtz-cms.validation.content-model-schemas
  "Auto-generated Malli schemas from Alfresco Content Model"
  (:require [malli.core :as m]
            [malli.util :as mu]))

;; ========================================
;; TYPE SCHEMAS
;; ========================================

(def item-schema
  "Schema for cms:item"
  [:map {:description "CMS Content Item"} [:cms:title :string] [:cms:slug {:optional true} :string] [:cms:body {:optional true} :string] [:cms:componentType [:enum {:description "Allowed: #{\"hero\" \"custom\" \"cta\" \"feature\" \"section\" \"tabs\" \"gallery\" \"card\" \"accordion\" \"testimonial\"}"} ["accordion" "card" "cta" "custom" "feature" "gallery" "hero" "section" "tabs" "testimonial"]]] [:cms:ordering {:optional true} :int] [:cms:imageRef {:optional true} :string] [:cms:metadata {:optional true} :string]])

;; ========================================
;; ASPECT SCHEMAS
;; ========================================

(def placeable-aspect-schema
  "Schema for cms:placeable"
  [:map {:description "Placeable Content"} [:cms:sitePath {:optional true} :string] [:cms:pagePath {:optional true} :string] [:cms:region {:optional true} [:enum {:description "Allowed: #{\"hero\" \"menu\" \"sidebar\" \"footer\" \"main\" \"header\"}"} ["footer" "header" "hero" "main" "menu" "sidebar"]]] [:cms:slot {:optional true} :int]])

(def publishable-aspect-schema
  "Schema for cms:publishable"
  [:map {:description "Publishable Content"} [:cms:publishStatus {:optional true} :string] [:cms:publishedDate {:optional true} :string] [:cms:publishedBy {:optional true} :string] [:cms:embargoDate {:optional true} :string] [:cms:expiryDate {:optional true} :string]])

;; ========================================
;; SCHEMA REGISTRY
;; ========================================

(def schema-registry
  "Complete schema registry"
  {:types {"cms:item" [:map {:description "CMS Content Item"} [:cms:title :string] [:cms:slug {:optional true} :string] [:cms:body {:optional true} :string] [:cms:componentType [:enum {:description "Allowed: #{\"hero\" \"custom\" \"cta\" \"feature\" \"section\" \"tabs\" \"gallery\" \"card\" \"accordion\" \"testimonial\"}"} ["accordion" "card" "cta" "custom" "feature" "gallery" "hero" "section" "tabs" "testimonial"]]] [:cms:ordering {:optional true} :int] [:cms:imageRef {:optional true} :string] [:cms:metadata {:optional true} :string]]}
   :aspects {"cms:placeable" [:map {:description "Placeable Content"} [:cms:sitePath {:optional true} :string] [:cms:pagePath {:optional true} :string] [:cms:region {:optional true} [:enum {:description "Allowed: #{\"hero\" \"menu\" \"sidebar\" \"footer\" \"main\" \"header\"}"} ["footer" "header" "hero" "main" "menu" "sidebar"]]] [:cms:slot {:optional true} :int]], "cms:publishable" [:map {:description "Publishable Content"} [:cms:publishStatus {:optional true} :string] [:cms:publishedDate {:optional true} :string] [:cms:publishedBy {:optional true} :string] [:cms:embargoDate {:optional true} :string] [:cms:expiryDate {:optional true} :string]]}})

(defn get-type-schema [type-id]
  (get-in schema-registry [:types type-id]))

(defn get-aspect-schema [aspect-id]
  (get-in schema-registry [:aspects aspect-id]))
