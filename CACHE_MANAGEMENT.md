# Cache Management Guide

## Overview

The cache system manages in-memory storage of Alfresco content with automatic eviction and size limits.

## Default Settings

- **Max entries:** 100 items
- **Max entry size:** 10 MB
- **Eviction strategy:** LRU (Least Recently Used)

## Configuration

### For Video Backgrounds

If you want to cache large assets like video backgrounds:

```clojure
(require '[mtz-cms.cache.simple :as cache])

;; Increase max entry size to 50MB
(cache/set-max-entry-size! 50)

;; Optionally reduce max entries to conserve memory
(cache/set-max-entries! 50)
```

### Recommended Settings by Use Case

**Small site (mostly text/images):**
```clojure
(cache/set-max-entries! 100)
(cache/set-max-entry-size! 10)  ;; 10 MB
```

**Site with video backgrounds:**
```clojure
(cache/set-max-entries! 50)
(cache/set-max-entry-size! 50)  ;; 50 MB for videos
```

**Large site with lots of content:**
```clojure
(cache/set-max-entries! 200)
(cache/set-max-entry-size! 10)  ;; 10 MB
```

## Monitoring

### Check Cache Stats

```clojure
(cache/cache-stats)
```

Returns:
```clojure
{:total-entries 15          ;; Total cached items
 :active-entries 15         ;; Non-expired items
 :expired-entries 0         ;; Expired (will be cleaned)
 :total-size-mb 5.2        ;; Total memory used
 :max-entries 100          ;; Configured max
 :max-entry-size-mb 10.0   ;; Configured max per entry
 :utilization-pct 15       ;; Percentage of max entries used
 :keys (...)}              ;; List of cache keys
```

### Cache Logging

The system logs cache activity:

```
✅ CACHE HIT: :hero-abc-123       # Served from cache
❌ CACHE MISS: :image/xyz-789     # Fetched from Alfresco
❌ CACHE SKIP (too large): :video-bg - size: 55000 KB  # Entry too large
⚠️  CACHE EVICTED (size limit): :old-image  # Removed due to max entries
```

## Manual Management

### Clear Entire Cache

```clojure
(cache/clear-cache!)
```

### Clear Specific Entry

```clojure
(cache/clear-key! :site-navigation)
(cache/clear-key! :image/abc-123)
```

## How It Works

### Automatic Eviction

When cache reaches max entries:
1. System identifies least recently accessed item
2. Removes it to make room
3. Logs eviction message

### Size Checking

Before caching:
1. System estimates entry size
2. If > max-entry-size, skips caching
3. Item is fetched fresh each time

### TTL Expiration

Each entry has a time-to-live:
- **Navigation:** 1 hour
- **Components:** 1 hour
- **Images:** 24 hours

Expired entries are automatically skipped.

## Performance Impact

### Memory Usage

Approximate memory per cached item:
- Navigation menu: ~50 KB
- Hero component: ~100 KB
- Feature component: ~80 KB
- Small image (< 1 MB): ~1 MB
- Large image (5 MB): ~5 MB
- Video background (50 MB): ~50 MB

### Example Calculation

**Default settings (100 entries, 10 MB max):**
- Worst case: 100 × 10 MB = **1 GB RAM**
- Typical: ~50 entries × 2 MB avg = **100 MB RAM**

**Video settings (50 entries, 50 MB max):**
- Worst case: 50 × 50 MB = **2.5 GB RAM**
- Typical: ~10 videos × 30 MB + 40 items × 1 MB = **340 MB RAM**

## Best Practices

1. **Monitor utilization** - Check `cache-stats` periodically
2. **Adjust for content** - Larger max-entry-size if using videos
3. **Server restart clears cache** - First load after restart will be slow
4. **Development** - Clear cache when content changes: `(clear-cache!)`
5. **Production** - Let TTL handle expiration automatically

## Troubleshooting

### "CACHE SKIP (too large)" Messages

**Problem:** Asset exceeds max-entry-size
**Solution:** Increase the limit:
```clojure
(cache/set-max-entry-size! 50)  ;; Or higher
```

### "CACHE EVICTED" Messages

**Problem:** Cache is full (hit max entries)
**Solution:** Increase max entries:
```clojure
(cache/set-max-entries! 200)
```

### Page Still Slow After Restart

**Expected:** Cache is in-memory, lost on restart
**Solution:** First load rebuilds cache, subsequent loads are fast

### High Memory Usage

**Problem:** Cache consuming too much RAM
**Solution:** Reduce limits:
```clojure
(cache/set-max-entries! 50)
(cache/set-max-entry-size! 5)
```

## Configuration File

For permanent configuration, add to your startup code:

```clojure
;; In your main namespace or config
(ns mtz-cms.core
  (:require [mtz-cms.cache.simple :as cache]))

(defn configure-cache! []
  "Configure cache for production"
  (cache/set-max-entries! 100)
  (cache/set-max-entry-size! 10))

;; Call on startup
(configure-cache!)
```
