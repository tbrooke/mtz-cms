(require 'mtz-cms.core)

(defn -main []
  (println "🚀 Starting Mount Zion CMS...")
  (mtz-cms.core/start-server 3000)
  (println "✅ Server started on http://localhost:3000")
  (println "Press Ctrl+C to stop")
  @(promise))

(-main)
