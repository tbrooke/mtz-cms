#!/usr/bin/env bb
;; Test the hierarchical menu system
;; This script demonstrates how the menu discovers subpages

(require '[babashka.curl :as curl]
         '[cheshire.core :as json]
         '[clojure.pprint :as pprint])

(def alfresco-host "http://localhost:8080")
(def api-base (str alfresco-host "/alfresco/api/-default-/public/alfresco/versions/1"))
(def auth ["admin" "admin"])

;; Top-level page node IDs (from config)
(def pages
  {:home       "9faac48b-6c77-4266-aac4-8b6c7752668a"
   :about      "8158a6aa-dbd7-4f5b-98a6-aadbd72f5b3b"
   :worship    "2cf1aac5-8577-499e-b1aa-c58577a99ea0"
   :events     "7c1da411-886a-4009-9da4-11886a6009c0"
   :activities "bb44a590-1c61-416b-84a5-901c61716b5e"
   :news       "fd02c48b-3d27-4df7-82c4-8b3d27adf701"
   :outreach   "b0774f12-4ea4-4851-b74f-124ea4f851a7"
   :preschool  "915ea06b-4d65-4d5c-9ea0-6b4d65bd5cba"
   :contact    "acfd9bd1-1e61-4c3b-bd9b-d11e611c3bc0"})

(defn get-json [path]
  (try
    (let [url (str api-base path)
          resp (curl/get url {:basic-auth auth :throw false})]
      (when (= 200 (:status resp))
        (json/parse-string (:body resp) true)))
    (catch Exception e
      (println "Error:" (.getMessage e))
      nil)))

(defn has-aspect? [node aspect]
  (some #(= aspect %) (:aspectNames node [])))

(defn is-menu-item? [node]
  (and (has-aspect? node "web:siteMeta")
       (= "page" (get-in node [:properties :web:kind]))
       (true? (get-in node [:properties :web:menuItem]))))

(defn get-children [node-id]
  (when-let [data (get-json (str "/nodes/" node-id "/children?include=aspectNames,properties"))]
    (get-in data [:list :entries])))

(defn build-menu-item [page-key node-id label]
  (let [children (get-children node-id)
        _ (when (= page-key :activities)
            (println "DEBUG Activities children:" (count children))
            (doseq [child children]
              (let [node (:entry child)]
                (println "  - " (:name node)
                         "aspects:" (:aspectNames node)
                         "web:menuItem:" (get-in node [:properties :web:menuItem])))))
        menu-children (filter #(is-menu-item? (:entry %)) children)
        _ (when (= page-key :activities)
            (println "DEBUG Menu children count:" (count menu-children)))
        submenu (map (fn [child]
                      (let [node (:entry child)]
                        {:label (or (get-in node [:properties :web:menuLabel])
                                   (get-in node [:properties :cm:title])
                                   (:name node))
                         :node-id (:id node)}))
                    menu-children)]
    {:key page-key
     :label label
     :node-id node-id
     :has-submenu? (seq submenu)
     :submenu submenu}))

(defn -main []
  (println "🔍 Mount Zion Menu Discovery Test\n")
  (println "Testing hierarchical menu with subpages...\n")

  (let [menu [{:key :home :label "Home" :node-id (:home pages)}
              (build-menu-item :about (:about pages) "About")
              (build-menu-item :worship (:worship pages) "Worship")
              (build-menu-item :events (:events pages) "Events")
              (build-menu-item :activities (:activities pages) "Activities")
              (build-menu-item :news (:news pages) "News")
              (build-menu-item :outreach (:outreach pages) "Outreach")
              (build-menu-item :preschool (:preschool pages) "Preschool")
              (build-menu-item :contact (:contact pages) "Contact")]]

    (println "📊 Menu Structure:\n")
    (doseq [item menu]
      (println "▸" (:label item))
      (when (:has-submenu? item)
        (doseq [sub (:submenu item)]
          (println "  └─" (:label sub)))))

    (println "\n📈 Summary:")
    (println "  Top-level items:" (count menu))
    (println "  Items with submenus:" (count (filter :has-submenu? menu)))
    (println "  Total submenu items:" (reduce + (map #(count (:submenu % [])) menu)))

    (println "\n💾 Full structure:")
    (pprint/pprint menu)))

(when (= *file* (System/getProperty "babashka.file"))
  (-main))
