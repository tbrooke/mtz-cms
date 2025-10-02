# Claude Code Prompt: Add Simple Caching to Mount Zion CMS

## Context
This is a Clojure web application using Pathom resolvers to fetch content from Alfresco CMS. The home page loads slowly because navigation is built from Alfresco on every request. We've created a simple in-memory cache and need to apply it to the navigation building.

## Files Involved
- `src/mtz_cms/cache/simple.clj` - Already created, provides caching functions
- `src/mtz_cms/ui/pages.clj` - Needs updating to use cache

## Goal
Wrap the navigation building call with caching so it's only built once per hour instead of on every page request.

## Changes Needed

### 1. Update src/mtz_cms/ui/pages.clj

**Add cache require** (around line 5):
```clojure
(ns mtz-cms.ui.pages
  "Page templates for Mount Zion CMS"
  (:require
   [mtz-cms.ui.components :as ui]
   [mtz-cms.layouts.templates :as layouts]
   [mtz-cms.cache.simple :as cache]  ; ADD THIS LINE
   [mtz-cms.navigation.menu :as menu]))
```

**Update base-layout function** (around line 12-30):

Find this code:
```clojure
(defn base-layout
  "Base page layout with dynamic navigation.
   Accepts optional ctx (Pathom context) to build navigation from Alfresco."
  ([title content] (base-layout title content nil))
  ([title content ctx]
   (let [nav (when ctx
               (try
                 (let [result (menu/build-navigation ctx)]
                   (println "Navigation built successfully:" (count result) "items")
                   result)
                 (catch Exception e
                   ;; Log error but don't break the page
                   (println "ERROR building navigation:" (.getMessage e))
                   (println "Stack trace:")
                   (.printStackTrace e)
                   nil)))]
     (when nav
       (println "Rendering navigation with" (count nav) "items"))
     [:html {:class "h-full"}
      ...rest of function...
```

Replace with:
```clojure
(defn base-layout
  "Base page layout with dynamic navigation.
   Accepts optional ctx (Pathom context) to build navigation from Alfresco."
  ([title content] (base-layout title content nil))
  ([title content ctx]
   (let [nav (when ctx
               (cache/cached 
                 :navigation 
                 3600  ; Cache for 1 hour (3600 seconds)
                 #(try
                    (let [result (menu/build-navigation ctx)]
                      (println "Navigation built successfully:" (count result) "items")
                      result)
                    (catch Exception e
                      ;; Log error but don't break the page
                      (println "ERROR building navigation:" (.getMessage e))
                      (println "Stack trace:")
                      (.printStackTrace e)
                      nil))))]
     (when nav
       (println "Using navigation with" (count nav) "items"))
     [:html {:class "h-full"}
      ...rest of function...
```

**Key change**: Wrap the entire try/catch block in `(cache/cached :navigation 3600 #(...))` 

This means:
- First request: Builds navigation, caches it with key `:navigation` for 3600 seconds
- Subsequent requests (within 1 hour): Returns cached navigation immediately
- After 1 hour: Cache expires, navigation is rebuilt and re-cached

### 2. Add Admin Cache Control Route

**File**: `src/mtz_cms/routes/api.clj`

Add this endpoint (look for existing route definitions and add near the end):

```clojure
(require '[mtz-cms.cache.simple :as cache])

;; Add this route
(GET "/admin/clear-cache" []
  (cache/clear-cache!)
  {:status 200 
   :headers {"Content-Type" "text/plain"}
   :body "Cache cleared successfully. Navigation will be rebuilt on next request."})

;; Optional: Cache statistics endpoint
(GET "/admin/cache-stats" []
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (pr-str {:cache-size (count @cache/cache)
                  :cached-keys (keys @cache/cache)})})
```

## Testing Instructions

After making the changes:

1. **Start the application**:
   ```bash
   cd /Users/tombrooke/code/trust-server/mtzion/mtz-cms
   clj -M:dev
   ```

2. **Test caching behavior**:
   - Visit home page - Should see "Navigation built successfully: X items" in console
   - Refresh page - Should see "Using navigation with X items" but NOT "Navigation built"
   - This confirms navigation is being served from cache

3. **Test cache clearing**:
   - Visit: http://localhost:3000/admin/clear-cache
   - Should see "Cache cleared successfully"
   - Visit home page again - Should see "Navigation built" again (rebuilding)

4. **Test cache expiration**:
   - Wait 1 hour (or temporarily change 3600 to 10 for 10 seconds in testing)
   - Navigation should rebuild automatically after TTL expires

## Expected Behavior

**Before caching**:
- Every page load: "Navigation built successfully: 5 items"
- Slow page loads (querying Alfresco every time)

**After caching**:
- First load: "Navigation built successfully: 5 items"
- Subsequent loads: "Using navigation with 5 items"
- Fast page loads (serving from memory)
- After 1 hour or cache clear: Rebuilds automatically

## Important Notes

1. **Cache is in-memory**: Cache will be lost when the application restarts. This is acceptable for a church website.

2. **Manual cache clearing**: Visit /admin/clear-cache after updating content in Alfresco to see changes immediately.

3. **No external dependencies**: This uses a simple Clojure atom - no Redis, no external cache service.

4. **Thread-safe**: The cache uses Clojure's atom which is thread-safe.

## Git Commit Message

After changes are working:
```
feat: Add simple in-memory caching for navigation

- Created cache/simple.clj with TTL-based caching
- Wrapped navigation building in 1-hour cache
- Added admin endpoints to clear cache and view stats
- Significantly improves page load performance

Navigation now cached for 1 hour instead of being rebuilt
on every request. Cache can be manually cleared via
/admin/clear-cache endpoint.
```

## Future Enhancements (Not Now)

These are documented for future reference but NOT part of this task:
- Blog support for Pastor Jim Reflects
- Events page with listing
- Calendar widget
- Preschool section
- Image caching proxy

Focus only on adding the navigation caching as described above.
