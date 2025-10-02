(ns mtz-cms.cache.simple
  "Ultra-simple in-memory cache with TTL and size limits

  Purpose: Cache heavy Alfresco content (images, pages) to speed up load times.
  No external services needed - just a Clojure atom.

  Features:
  - TTL expiration
  - Size-based eviction (LRU)
  - Configurable max entries and max size per entry")

;; --- CONFIGURATION ---

(def max-cache-entries
  "Maximum number of entries in cache (default: 100)"
  (atom 100))

(def max-entry-size
  "Maximum size of a single cache entry in bytes (default: 10MB for videos/large images)"
  (atom (* 10 1024 1024)))  ;; 10 MB

;; The cache - map with timestamps and metadata
(def cache (atom {}))

(defn expired? 
  "Check if a cache entry is expired"
  [{:keys [expires-at]}]
  (> (System/currentTimeMillis) expires-at))

(defn get-cached
  "Get value from cache if not expired, updating access time (LRU)"
  [key]
  (when-let [entry (get @cache key)]
    (when-not (expired? entry)
      ;; Update access time for LRU
      (swap! cache assoc-in [key :accessed-at] (System/currentTimeMillis))
      (:value entry))))

(defn estimate-size
  "Estimate size of a value in bytes (rough approximation)"
  [value]
  (cond
    (bytes? value) (alength value)
    (string? value) (* 2 (count value))  ;; Java strings are UTF-16
    (map? value) (reduce + (map #(+ (estimate-size (key %)) (estimate-size (val %))) value))
    (coll? value) (reduce + (map estimate-size value))
    :else 100))  ;; Default small size for primitives

(defn evict-oldest!
  "Evict the oldest cache entry (LRU)"
  []
  (when-let [oldest-key (first (sort-by #(get-in @cache [% :accessed-at]) (keys @cache)))]
    (swap! cache dissoc oldest-key)
    (println "⚠️  CACHE EVICTED (size limit):" oldest-key)))

(defn put-cached!
  "Store value in cache with TTL in seconds and size checking"
  [key value ttl-seconds]
  (let [size (estimate-size value)
        expires-at (+ (System/currentTimeMillis)
                     (* ttl-seconds 1000))
        now (System/currentTimeMillis)]

    ;; Check if entry is too large
    (if (> size @max-entry-size)
      (do
        (println "❌ CACHE SKIP (too large):" key "- size:" (int (/ size 1024)) "KB")
        value)  ;; Don't cache, just return value

      ;; Store in cache
      (do
        ;; Evict oldest if cache is full
        (when (>= (count @cache) @max-cache-entries)
          (evict-oldest!))

        (swap! cache assoc key {:value value
                                :expires-at expires-at
                                :accessed-at now
                                :size size})
        value))))

(defn cached
  "Get from cache or compute and cache

  Usage:
    (cached :home-page 3600
      #(render-home-page data))
  "
  [key ttl-seconds compute-fn]
  (if-let [cached-value (get-cached key)]
    (do
      (println "✅ CACHE HIT:" key)
      cached-value)
    (do
      (println "❌ CACHE MISS:" key "- computing...")
      (put-cached! key (compute-fn) ttl-seconds))))

(defn clear-cache! 
  "Clear all cache (useful for development or admin action)"
  []
  (reset! cache {}))

(defn clear-key!
  "Clear specific cache entry"
  [key]
  (swap! cache dissoc key))

(defn set-max-entries!
  "Set maximum number of cache entries (default: 100)"
  [n]
  (reset! max-cache-entries n))

(defn set-max-entry-size!
  "Set maximum size per entry in MB (default: 10MB)"
  [mb]
  (reset! max-entry-size (* mb 1024 1024)))

(defn cache-stats
  "Get cache statistics with size information"
  []
  (let [entries @cache
        now (System/currentTimeMillis)
        active (filter #(> (:expires-at (val %)) now) entries)
        expired (filter #(<= (:expires-at (val %)) now) entries)
        total-size (reduce + (map #(get-in (val %) [:size] 0) entries))
        max-entries @max-cache-entries
        max-size @max-entry-size]
    {:total-entries (count entries)
     :active-entries (count active)
     :expired-entries (count expired)
     :total-size-bytes total-size
     :total-size-mb (/ total-size 1024 1024.0)
     :max-entries max-entries
     :max-entry-size-mb (/ max-size 1024 1024.0)
     :utilization-pct (int (* 100 (/ (count entries) max-entries)))
     :keys (keys entries)}))

(comment
  ;; Test cache
  (cached :test 5 #(do (println "Computing...") "result"))
  (cached :test 5 #(do (println "Computing...") "result")) ; Should not print

  ;; Clear and test again
  (clear-key! :test)
  (cached :test 5 #(do (println "Computing...") "result")) ; Should print again

  ;; Check cache stats
  (cache-stats)
  ;; => {:total-entries 5
  ;;     :active-entries 5
  ;;     :total-size-mb 0.5
  ;;     :max-entries 100
  ;;     :max-entry-size-mb 10.0
  ;;     :utilization-pct 5}

  ;; Configure for video backgrounds (increase max size)
  (set-max-entry-size! 50)  ;; Allow 50MB entries for videos
  (set-max-entries! 50)     ;; Reduce max entries to conserve memory

  ;; Clear everything
  (clear-cache!)
  )
