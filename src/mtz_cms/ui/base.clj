(ns mtz-cms.ui.base
  "Base HTML page template for Mount Zion CMS

   This is the foundation of EVERY page - the HTML shell that wraps all content.

   Philosophy:
   - ONE base template for the entire site
   - Handles HTML structure, head, navigation, footer
   - Navigation built once and cached
   - All pages use this - no exceptions

   Architecture:
   base-page → [header + navigation] + content + [footer]

   Created as part of architecture refactoring to eliminate duplication."
  (:require
   [mtz-cms.ui.styles :as styles]
   [mtz-cms.components.navigation :as nav]
   [mtz-cms.navigation.menu :as menu]
   [mtz-cms.cache.simple :as cache]
   [clojure.tools.logging :as log]))

;; --- BASE PAGE TEMPLATE ---

(defn base-page
  "The ONE base page template for the entire site.

   This function renders the complete HTML structure that wraps all content:
   - HTML head with meta tags, CSS, scripts
   - Header with dynamic navigation (cached)
   - Main content area
   - Footer

   Args:
     title - Page title (string)
     content - Page content (Hiccup vector)
     ctx - Pathom context (map, optional but recommended)
     site-context - :church or :preschool (optional, defaults to :church)

   Returns:
     Complete HTML page as Hiccup vector

   Example:
     (base-page \"Home\" [:div \"Welcome!\"] {} :church)
     (base-page \"Preschool Home\" [:div \"Preschool!\"] {} :preschool)

   Note: Navigation is built from Alfresco and cached for 1 hour.
         If ctx is nil, falls back to static navigation."
  ([title content]
   (base-page title content nil :church))

  ([title content ctx]
   (base-page title content ctx :church))

  ([title content ctx site-context]
   (let [;; Build navigation from Alfresco (cached for performance)
         ;; Use different cache key for church vs preschool
         cache-key (keyword "site-navigation" (name site-context))
         nav (when ctx
               (try
                 (let [result (cache/cached
                               cache-key
                               3600  ;; 1 hour cache
                               #(menu/build-navigation ctx site-context))]
                   (log/debug "Navigation ready for" site-context ":" (count result) "items")
                   result)
                 (catch Exception e
                   (log/error e "Failed to build navigation for" site-context)
                   nil)))]

     [:html {:class "h-full bg-white"}
      ;; HEAD - Meta tags, CSS, Scripts
      [:head
       [:meta {:charset "utf-8"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
       [:title title]

       ;; Google Fonts
       (styles/google-fonts)

       ;; Tailwind CSS (CDN for dev, compiled for production)
       (styles/tailwind-styles)

       ;; HTMX for dynamic interactions
       [:script {:src "/js/htmx.min.js"}]

       ;; Custom site styles
       (styles/custom-styles)]

      ;; BODY - Header, Main, Footer
      [:body {:class "h-full bg-white"}
       ;; Header with navigation
       (nav/site-header nav site-context)

       ;; Main content area
       [:main {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"}
        content]

       ;; Footer
       (nav/site-footer)]])))

;; --- REPL TESTING ---

(comment
  ;; Test base page
  (base-page
   "Test Page"
   [:div "Hello World"]
   {})

  ;; Test without context (should use fallback navigation)
  (base-page
   "Test Page"
   [:div "Hello World"])

  ;; All pages should use this function - no direct HTML rendering elsewhere
  )
