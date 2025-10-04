(ns mtz-cms.components.templates
  "Component templates - DEPRECATED, use individual component files

   This file is kept for backwards compatibility during refactoring.
   New code should import from:
   - mtz-cms.components.hero
   - mtz-cms.components.feature
   - mtz-cms.components.card
   - mtz-cms.components.section"
  (:require
   [mtz-cms.components.hero :as hero]
   [mtz-cms.components.feature :as feature]
   [mtz-cms.components.card :as card]))

;; --- BACKWARDS COMPATIBILITY WRAPPERS ---

(defn extract-text-from-html
  "DEPRECATED: Use hero/extract-text-from-html instead"
  [html-content]
  (hero/extract-text-from-html html-content))

(defn simple-hero
  "DEPRECATED: Use hero/hero instead"
  [hero-data]
  (hero/hero hero-data))

(defn simple-feature
  "DEPRECATED: Use feature/feature instead"
  [feature-data]
  (feature/feature feature-data))

(defn render-hero
  "DEPRECATED: Use hero/hero instead"
  [hero-data]
  (hero/hero hero-data))

(defn render-feature
  "DEPRECATED: Use feature/feature instead"
  [feature-data]
  (feature/feature feature-data))

(defn feature-card
  "DEPRECATED: Use card/card instead"
  [item-data]
  (card/card item-data))