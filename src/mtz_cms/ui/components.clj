(ns mtz-cms.ui.components
  "UI components for Mount Zion CMS - REFACTORED

   This namespace is now a compatibility layer that re-exports from specialized namespaces:

   - mtz-cms.ui.styles (CSS, Tailwind)
   - mtz-cms.components.navigation (Header, Footer, Breadcrumbs)
   - mtz-cms.components.primitives (Loading, Errors, Messages, Buttons)

   ARCHITECTURE NOTE:
   This file maintains backward compatibility while the codebase transitions
   to the new structure. Eventually, imports should be updated to use the
   specialized namespaces directly, and this file can be deprecated.

   Most functionality has been moved to specialized namespaces for better organization."
  (:require
   [mtz-cms.ui.styles :as styles]
   [mtz-cms.components.navigation :as nav]
   [mtz-cms.components.primitives :as primitives]))

;; --- RE-EXPORTS FROM ui/styles.clj ---

(def tailwind-cdn
  "Tailwind CSS CDN script tag.
   Re-exported from mtz-cms.ui.styles"
  styles/tailwind-cdn)

(def custom-styles
  "Custom CSS styles.
   Re-exported from mtz-cms.ui.styles"
  styles/custom-styles)

;; --- RE-EXPORTS FROM components/navigation.clj ---

(def site-header
  "Site header with navigation.
   Re-exported from mtz-cms.components.navigation"
  nav/site-header)

(def site-footer
  "Site footer.
   Re-exported from mtz-cms.components.navigation"
  nav/site-footer)

;; --- RE-EXPORTS FROM components/primitives.clj ---

(def loading-spinner
  "Loading spinner component.
   Re-exported from mtz-cms.components.primitives"
  primitives/loading-spinner)

(def error-message
  "Error message component.
   Re-exported from mtz-cms.components.primitives"
  primitives/error-message)

(def success-message
  "Success message component.
   Re-exported from mtz-cms.components.primitives"
  primitives/success-message)

;; --- REPL TESTING ---

(comment
  ;; Verify all re-exports work
  (tailwind-cdn)
  (custom-styles)
  (site-header [])
  (site-footer)
  (loading-spinner)
  (error-message "test error")
  (success-message "test success")

  ;; All functions should work exactly as before
  ;; External code using mtz-cms.ui.components still works unchanged
  )
