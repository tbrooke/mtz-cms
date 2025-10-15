;; Connect to nREPL and run diagnostic commands
(require '[clojure.java.io :as io])

;; Step 1: Check cache status
(println "\n=== 1. Cache Status ===")
(require '[mtz-cms.cache.simple :as cache])
(println (cache/cache-stats))

;; Step 2: Clear navigation cache
(println "\n=== 2. Clearing navigation cache ===")
(cache/clear-key! :site-navigation)
(println "Cache cleared!")

;; Step 3: Build fresh navigation
(println "\n=== 3. Building fresh navigation ===")
(require '[mtz-cms.navigation.menu :as menu])
(require '[mtz-cms.config.core :as config])

(def ctx {:alfresco/base-url "http://localhost:8090"
          :alfresco/username "admin"
          :alfresco/password "admin"})

(def nav (menu/build-navigation ctx))
(println "Navigation built with" (count nav) "top-level items")

;; Step 4: Check navigation structure
(println "\n=== 4. Navigation Structure ===")
(doseq [item nav]
  (println (str "- " (:label item)
                " | has-children? " (:has-children? item)
                " | submenu count: " (count (:submenu item [])))))

;; Step 5: Check Outreach specifically
(println "\n=== 5. Outreach Submenu ===")
(def outreach (first (filter #(= (:key %) :outreach) nav)))
(println "Outreach submenu items:")
(if outreach
  (doseq [sub (:submenu outreach)]
    (println (str "  - " (:label sub) " -> " (:path sub))))
  (println "ERROR: No Outreach item found!"))

;; Step 6: Check Alfresco folder contents
(println "\n=== 6. Alfresco Outreach Folder Contents ===")
(require '[mtz-cms.alfresco.client :as alfresco])
(def outreach-node-id (config/get-node-id :outreach))
(println "Outreach node ID:" outreach-node-id)

(def children (alfresco/get-node-children ctx outreach-node-id {:include "aspectNames,properties"}))

(if (:success children)
  (do
    (println "\nFound" (count (get-in children [:data :list :entries])) "children:")
    (doseq [child (get-in children [:data :list :entries])]
      (let [node (:entry child)
            aspects (:aspectNames node)
            props (:properties node)]
        (println "\n------------------------------")
        (println "Child:" (:name node))
        (println "  Type:" (:nodeType node))
        (println "  Is Folder:" (:isFolder node))
        (println "  Aspects:" aspects)
        (println "  web:menuItem:" (get props "web:menuItem"))
        (println "  web:published:" (get props "web:published"))
        (println "  web:publishState:" (get props "web:publishState"))
        (println "  web:kind:" (get props "web:kind"))
        (println "  web:menuLabel:" (get props "web:menuLabel"))
        (println "  cm:title:" (get props "cm:title")))))
  (println "ERROR getting children:" children))

(println "\n=== DIAGNOSTIC COMPLETE ===")
