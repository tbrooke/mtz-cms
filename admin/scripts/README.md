# Mount Zion CMS - Admin Scripts

Organized scripts for CMS administration and development.

## Directory Structure

```
admin/scripts/
├── Babashka/         # Babashka/Clojure scripts for data sync and processing
├── Bash/             # Bash scripts for server management and deployment
├── Repl/             # REPL commands for interactive development
└── README.md         # This file
```

## Babashka Scripts

**Location:** `admin/scripts/Babashka/`

### sync-content.clj
Syncs pages marked with `web:generateStatic=true` from Alfresco to static `.edn` files.

```bash
# Run from project root
admin/scripts/Babashka/sync-content.clj
# or with Clojure
clojure -M -m sync-content
```

### model_sync_working.clj, model_sync_calendar.clj
Legacy model sync scripts for Alfresco content model synchronization.

### test_syntax.clj
Quick syntax testing script.

## Bash Scripts

**Location:** `admin/scripts/Bash/`

All scripts navigate to project root automatically, so they work from any location.

### deploy.sh
Deploys the CMS to the production server.

```bash
# Run from anywhere
admin/scripts/Bash/deploy.sh
# or use wrapper
./deploy.sh
```

Steps:
1. Syncs files to trust server
2. Updates nginx configuration
3. Builds and runs Docker container
4. Verifies deployment

### start-server.sh
Starts the development server with SSH tunnel to Alfresco.

```bash
admin/scripts/Bash/start-server.sh
# or use wrapper
./start-server.sh
```

Once started:
```clojure
;; In REPL
(require 'user)
(user/start)
```

### start-ezuno.sh, launch-ezuno.sh
Scripts for starting the Ezuno subsystem.

## REPL Commands

**Location:** `admin/scripts/Repl/admin.clj`

Load in your REPL for administrative functions:

```clojure
(load-file "admin/scripts/Repl/admin.clj")
```

### Key Commands

**Server Management:**
```clojure
(user/start)        ; Start server
(user/stop)         ; Stop server
(user/restart)      ; Restart with code reload
```

**Cache Management:**
```clojure
(require '[mtz-cms.cache.simple :as cache])
(cache/cache-stats)              ; View stats
(cache/clear-cache!)             ; Clear all
(cache/set-max-entry-size! 50)   ; Configure for videos
```

**Content:**
```clojure
(require '[mtz-cms.pathom.resolvers :as pathom])
(pathom/query {} [:site/pages])        ; All pages
(pathom/query {} [:site/navigation])   ; Navigation
```

See `admin/scripts/Repl/admin.clj` for complete command reference.

## Workflow Examples

### Update Static Content

1. Mark pages in Alfresco with `web:generateStatic=true` aspect
2. Sync content:
   ```bash
   admin/scripts/Babashka/sync-content.clj
   ```
3. Verify files:
   ```bash
   ls -la resources/content/static/
   ```
4. Test locally:
   ```clojure
   (require '[mtz-cms.content.static-loader :as static])
   (static/load-static-page "about")
   ```
5. Deploy:
   ```bash
   ./deploy.sh
   ```

### Cache Management for Videos

1. Configure larger cache:
   ```clojure
   (require '[mtz-cms.cache.simple :as cache])
   (cache/set-max-entry-size! 50)  ; 50MB per video
   (cache/set-max-entries! 50)     ; Fewer total items
   ```
2. Monitor:
   ```clojure
   (cache/cache-stats)
   ```
3. Clear if needed:
   ```clojure
   (cache/clear-cache!)
   ```

### Debugging Routes

1. View all routes:
   ```clojure
   (require 'mtz-cms.routes.main :reload)
   @(resolve 'mtz-cms.routes.main/all-routes)
   ```
2. Test specific page:
   ```clojure
   (pathom/query {} [{[:page/slug "about"] [:page/title :page/content]}])
   ```

## Notes

- All Bash scripts use relative paths and navigate to project root
- Wrapper scripts exist in project root for convenience (`./deploy.sh`, `./start-server.sh`)
- Babashka scripts are executable (`chmod +x`)
- REPL commands are in comment blocks for easy evaluation
