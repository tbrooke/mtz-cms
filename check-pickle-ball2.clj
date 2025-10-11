(require '[mtz-cms.alfresco.client :as alfresco])

(def ctx {})
(def pickle-ball-node-id "2a2aca9f-2285-4563-aaca-9f2285c563e9")

(println "\n=== Checking Pickle Ball Folder Contents ===")
(let [result (alfresco/get-node-children ctx pickle-ball-node-id)]
  (if (:success result)
    (let [entries (get-in result [:data :list :entries])]
      (println "Found" (count entries) "items:")
      (doseq [entry entries]
        (let [node (:entry entry)
              name (:name node)
              is-file (:isFile node)
              node-id (:id node)]
          (println "  -" name (if is-file "(file)" "(folder)") "ID:" node-id)

          ;; Try to read content of any file
          (when is-file
            (let [content-result (alfresco/get-node-content ctx node-id)]
              (if (:success content-result)
                (let [content-str (String. (:data content-result) "UTF-8")]
                  (println "    ✅ Successfully read content (" (count content-str) "bytes)")
                  (println "    Content:")
                  (println content-str))
                (println "    ❌ Failed to read content")))))))
    (println "Failed to fetch folder contents")))

(System/exit 0)
