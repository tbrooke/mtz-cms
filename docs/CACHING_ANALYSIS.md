# MTZ-CMS Caching Analysis & Optimization

## Current Setup (Before Cloudflare)

### Local Cache (Clojure In-Memory)
**Location**: `/home/tmb/mtz-cms/src/mtz_cms/cache/simple.clj`

**What's Being Cached:**
1. **Site Navigation** - 1 hour TTL (3600s)
   - Built from Alfresco folder structure
   - Cached in `ui/base.clj`
   - Reduces Alfresco API calls

2. **Images from Alfresco** - 24 hour TTL (86400s)
   - Cached in `routes/main.clj`
   - PDF thumbnails included
   - Reduces bandwidth to Alfresco

3. **PDFs** - 24 hour TTL (86400s)
   - Full PDF content
   - Served from cache

**Cache Configuration:**
- Max entries: 100
- Max size per entry: 10 MB
- Eviction: LRU (Least Recently Used)
- Type: In-memory (lost on restart)

**Cache Flow:**
```
User Request → Clojure App → Check Local Cache
                           ↓ miss
                           → Alfresco → Store in Cache → Return
                           ↓ hit
                           → Return from Cache
```

## New Setup (With Cloudflare)

### Three-Layer Caching

```
User → Cloudflare CDN → Clojure App → Alfresco
       (Edge Cache)     (Local Cache)   (Source)
```

**Layer 1: Browser Cache (4 hours)**
- User's browser caches static assets
- Set by Cloudflare

**Layer 2: Cloudflare CDN (Automatic)**
- Caches at edge servers worldwide
- Standard caching level
- Only proxied records: mtzcg.com, monitor, web
- NOT cached: admin.mtzcg.com (DNS only)

**Layer 3: Local Cache (Current)**
- In-memory Clojure cache
- Still valuable for reducing Alfresco calls
- Acts as origin cache

## Optimization Recommendations

### ✅ KEEP Current Local Cache Settings
Your current settings are still good:

**Why Keep Local Cache?**
1. **Reduces Alfresco load** - Even with Cloudflare, first request still hits your server
2. **Faster than Alfresco** - Local memory is faster than Alfresco API calls
3. **No duplicate work** - Cloudflare caches the OUTPUT, local cache prevents duplicate PROCESSING
4. **Dynamic content** - Navigation building is dynamic, worth caching

### 🎥 OPTIMIZE for Video Backgrounds

**Current Limitation:** Max 10MB per entry won't handle most videos

**Recommended Changes:**

1. **Increase max entry size** for videos (50MB):
   ```clojure
   ;; In your REPL or startup code:
   (require '[mtz-cms.cache.simple :as cache])
   (cache/set-max-entry-size! 50)  ; 50MB for video backgrounds
   ```

2. **Adjust max entries** to conserve memory:
   ```clojure
   (cache/set-max-entries! 50)  ; Reduce from 100 since entries are larger
   ```

3. **Add video-specific caching** in routes:
   ```clojure
   ;; Cache videos for 7 days (they don't change often)
   (cache/cached video-cache-key 604800 #(get-video ...))
   ```

### 📊 Optimal TTL Settings

| Content Type | Current TTL | Recommended | Reason |
|--------------|-------------|-------------|---------|
| Navigation | 1 hour | **1 hour** ✓ | Changes occasionally |
| Images | 24 hours | **24 hours** ✓ | Rarely change |
| PDFs | 24 hours | **24 hours** ✓ | Static documents |
| Videos | N/A | **7 days** | Large, rarely change |

### 🚀 Additional Optimizations

**1. Add Cache Stats Endpoint** (For monitoring)
Already available in cache/simple.clj:
```clojure
(cache/cache-stats)
;; Returns:
;; {:total-entries 25
;;  :active-entries 25
;;  :total-size-mb 45.2
;;  :utilization-pct 50}
```

**2. Add Admin Cache Clear** (Already planned in docs)
```clojure
;; In routes
(GET "/admin/clear-cache" []
  (cache/clear-cache!)
  {:status 200 :body "Cache cleared"})
```

**3. Cache Headers for Cloudflare**
Your app should send appropriate Cache-Control headers:
```clojure
;; For static assets served from Alfresco:
{:headers {"Cache-Control" "public, max-age=86400"}}  ; 24 hours

;; For dynamic pages:
{:headers {"Cache-Control" "public, max-age=3600"}}   ; 1 hour
```

## Memory Considerations

**Current Memory Usage:**
- 100 entries × 10MB max = ~1GB potential
- Actual usage likely much less (only if full)

**With Video Support:**
- 50 entries × 50MB max = ~2.5GB potential
- More realistic: 10-15 videos = ~500MB

**Recommendation:** Monitor with cache-stats and adjust as needed.

## When Cloudflare Helps Most

**Best for Cloudflare CDN:**
- Static images
- CSS/JavaScript files
- Video backgrounds (after first load)
- Assets served to repeat/multiple visitors

**Best for Local Cache:**
- Dynamic content (navigation)
- Content processing (thumbnails)
- Alfresco API responses
- First-time content requests

## Summary

### What to Change NOW for Videos:

1. **In your Clojure REPL** (connect to running app):
   ```clojure
   (require '[mtz-cms.cache.simple :as cache])
   (cache/set-max-entry-size! 50)  ; Support 50MB videos
   (cache/set-max-entries! 50)     ; Reduce total entries
   ```

2. **When adding video routes**, use long TTL:
   ```clojure
   (cache/cached :video-background-home 604800 #(get-video ...))  ; 7 days
   ```

### What to Keep:
- ✓ Current navigation cache (1 hour)
- ✓ Current image cache (24 hours)
- ✓ Current PDF cache (24 hours)
- ✓ LRU eviction strategy

### Benefits of Current Setup + Cloudflare:
1. **Reduced Alfresco load** - Local cache handles processing
2. **Fast global delivery** - Cloudflare serves cached content worldwide
3. **Bandwidth savings** - Most traffic served by Cloudflare
4. **Quick updates** - Clear local cache to refresh content
5. **Video support** - After simple config change

## Monitoring

Check cache health:
```bash
# SSH to server
ssh tmb@trust

# Connect to REPL (if available) or check logs
# Run: (cache/cache-stats)
```

Good indicators:
- Utilization 30-70% (not too empty, not too full)
- Total size < 1GB (adjust if needed)
- Active entries > 0 (cache is working)
