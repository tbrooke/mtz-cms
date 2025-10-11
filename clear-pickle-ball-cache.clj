(require '[mtz-cms.cache.simple :as cache])

(println "\n=== Clearing Pickle Ball Page Cache ===")

;; The node ID from the page
(def node-id "2a2aca9f-2285-4563-aaca-9f2285c563e9")

;; Clear the specific cache entry
(cache/clear-key! (keyword (str "page/" node-id)))

;; Also clear by slug
(cache/clear-key! :page/pickle-ball)

;; Clear the navigation cache (in case the page title changed)
(cache/clear-key! :site-navigation)

(println "✅ Cache cleared for Pickle Ball page")
(println "Refresh the page at http://localhost:3000/page/pickle-ball to see your updates!")

(System/exit 0)
