
;; Mount Zion CMS - Admin REPL Commands
;;
;; Load this file in your REPL for administrative tasks:
;; (load-file "admin/scripts/Repl/admin.clj")

(comment
  ;;; =======================================================================
  ;;; SERVER MANAGEMENT
  ;;; =======================================================================

  ;; Start the development server
  (require 'user)
  (user/start)

  ;; Stop the server
  (user/stop)

  ;; Restart with code reload
  (user/restart)


  ;;; =======================================================================
  ;;; CACHE MANAGEMENT
  ;;; =======================================================================

  (require '[mtz-cms.cache.simple :as cache])

  ;; View cache statistics
  (cache/cache-stats)
  ;; => {:total-entries 15
  ;;     :active-entries 15
  ;;     :expired-entries 0
  ;;     :total-size-mb 5.2
  ;;     :max-entry-size-mb 10.0
  ;;     :utilization-pct 15
  ;;     ...}

  ;; Clear all cache (force refresh from Alfresco)
  (cache/clear-cache!)

  ;; Configure cache for video backgrounds
  (cache/set-max-entry-size! 50)   ;; Allow 50MB videos
  (cache/set-max-entries! 50)      ;; Reduce total entries

  ;; Default configuration (for text/images)
  (cache/set-max-entry-size! 10)   ;; 10MB max
  (cache/set-max-entries! 100)     ;; 100 entries max

  ;; Memory Usage Guide:
  ;; - Default: ~100 MB RAM typical
  ;; - With videos (50MB): ~340 MB RAM typical, 2.5GB worst case


  ;;; =======================================================================
  ;;; CONTENT MANAGEMENT
  ;;; =======================================================================

  (require '[mtz-cms.pathom.resolvers :as pathom])
  (require '[mtz-cms.content.static-loader :as static])

  ;; View all discovered pages
  (pathom/query {} [:site/pages])

  ;; Get navigation structure
  (pathom/query {} [:site/navigation])

  ;; Test a specific page by slug
  (pathom/query {} [{[:page/slug "about"] [:page/title :page/content :page/exists]}])

  ;; Load static page (from resources/content/static/)
  (static/load-static-page "about")

  ;; Test dynamic page content
  (pathom/query {} [{[:page/key :home] [:page/title :page/content]}])


  ;;; =======================================================================
  ;;; ALFRESCO CONNECTION
  ;;; =======================================================================

  (require '[mtz-cms.alfresco.client :as alfresco])

  ;; Test Alfresco connection
  (alfresco/test-connection {})

  ;; Explore Alfresco structure
  (alfresco/explore-structure {})

  ;; Discover nodes for page mapping
  (pathom/discover-page-nodes {})


  ;;; =======================================================================
  ;;; COMPONENT TESTING
  ;;; =======================================================================

  ;; Test hero component with real data
  (pathom/query {} [{:hero/node-id "39985c5c-201a-42f6-985c-5c201a62f6d8"}
                    [:hero/title :hero/image :hero/content]])

  ;; Test feature components
  (pathom/query {} [{:feature/node-id "264ab06c-984e-4f64-8ab0-6c984eaf6440"}
                    [:feature/title :feature/content :feature/image :feature/type]])

  ;; Test complete home page composition
  (pathom/query {} [:home/hero :home/features :home/layout])


  ;;; =======================================================================
  ;;; DEBUGGING
  ;;; =======================================================================

  ;; View all routes
  (require 'mtz-cms.routes.main :reload)
  @(resolve 'mtz-cms.routes.main/all-routes)

  ;; Reload specific namespace
  (require 'mtz-cms.ui.pages :reload)

  ;; Check environment
  (System/getProperty "user.dir")
  (System/getenv "ALFRESCO_URL")


  ;;; =======================================================================
  ;;; DEPLOYMENT
  ;;; =======================================================================

  ;; After syncing static content with admin/scripts/Babashka/sync-content.clj:
  ;; 1. Verify static files exist
  (clojure.java.io/file "resources/content/static")
  (seq (.listFiles (clojure.java.io/file "resources/content/static")))

  ;; 2. Test loading them
  (static/load-static-page "about")

  ;; 3. Clear cache before deploying
  (cache/clear-cache!)

  ;; 4. Deploy using wrapper script from project root:
  ;; ./deploy.sh


  ;;; =======================================================================

)
