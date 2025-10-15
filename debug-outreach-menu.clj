;; Debug script for Outreach submenu issue
;; Run this in your REPL to see what's happening

(require '[mtz-cms.config.core :as config])
(require '[mtz-cms.alfresco.client :as alfresco])
(require '[mtz-cms.alfresco.discovery :as aspect])
(require '[mtz-cms.navigation.menu :as menu])
(require '[mtz-cms.cache.simple :as cache])

(println "\n=== OUTREACH SUBMENU DEBUG ===\n")

;; 1. Clear cache
(println "1. Clearing navigation cache...")
(cache/clear-key! :site-navigation)
(println "   ✓ Cache cleared\n")

;; 2. Setup context
(def ctx {:alfresco/base-url "http://localhost:8090"
          :alfresco/username "admin"
          :alfresco/password "admin"})

;; 3. Get Outreach node ID
(def outreach-node-id (config/get-node-id :outreach))
(println "2. Outreach Node ID:" outreach-node-id "\n")

;; 4. Fetch children directly from Alfresco
(println "3. Fetching Outreach children from Alfresco...")
(def children-result (alfresco/get-node-children ctx outreach-node-id
                                                   {:include "aspectNames,properties"}))

(if-not (:success children-result)
  (println "   ❌ ERROR fetching children:" children-result)
  (let [children (get-in children-result [:data :list :entries])]
    (println "   ✓ Found" (count children) "children\n")

    (println "4. Analyzing each child:\n")
    (doseq [child children]
      (let [node (:entry child)
            node-name (:name node)
            node-id (:id node)
            aspects (:aspectNames node)
            props (:properties node)]

        (println "   📄" node-name)
        (println "      Node ID:" node-id)
        (println "      Node Type:" (:nodeType node))
        (println "      Aspects:" aspects)
        (println "      web:kind:" (get props "web:kind"))
        (println "      web:menuItem:" (get props "web:menuItem"))
        (println "      web:publishState:" (get props "web:publishState"))
        (println "      web:menuLabel:" (get props "web:menuLabel"))
        (println "      cm:title:" (get props "cm:title"))
        (println)

        ;; Use aspect discovery functions
        (println "      🔍 Aspect Discovery Results:")
        (println "         has-web-site-meta?:" (aspect/has-web-site-meta? node))
        (println "         has-web-publishable?:" (aspect/has-web-publishable? node))
        (println "         is-page?:" (aspect/is-page? node))
        (println "         is-published?:" (aspect/is-published? node))
        (println "         get-web-menu-item?:" (aspect/get-web-menu-item? node))
        (println "         should-show-in-menu?:" (aspect/should-show-in-menu? node))
        (println "         menu-label:" (aspect/get-menu-label node))
        (println)))))

;; 5. Test menu building
(println "5. Building navigation menu...")
(def nav (menu/build-navigation ctx))
(def outreach (first (filter #(= (:key %) :outreach) nav)))

(println "   Outreach menu item:")
(println "      has-children?:" (:has-children? outreach))
(println "      submenu count:" (count (:submenu outreach [])))

(if (seq (:submenu outreach))
  (do
    (println "\n   ✓ Submenu items found:")
    (doseq [sub (:submenu outreach)]
      (println "      -" (:label sub) "->" (:path sub))))
  (println "\n   ❌ No submenu items found!"))

(println "\n6. Full navigation summary:")
(doseq [item nav]
  (when (:has-children? item)
    (println "   ▸" (:label item) "(" (count (:submenu item)) "submenu items)")))

(println "\n=== DEBUG COMPLETE ===")
