# Mount Zion CMS - Quick Start

Essential commands for daily development and administration.

## SSH Tunnel (Required for Local Dev)

```bash
# Start tunnel to Alfresco
tunnel

# Check if running
tunnel-check

# Stop tunnel
tunnel-kill
```

## Development Server

```bash
# Start local dev server (starts tunnel automatically)
./start-server.sh

# In REPL once started:
(require 'user)
(user/start)      # Start server
(user/stop)       # Stop server
(user/restart)    # Restart with code reload
```

## Static Content Sync

```bash
# 1. Make sure tunnel is active
tunnel

# 2. Sync static pages from Alfresco (like Pickle Ball)
clojure -M -e "(load-file \"src/sync_content.clj\") (sync-content/sync-all!)"

# 3. Review generated files
ls -la resources/content/static/

# 4. Commit and deploy
git add resources/content/static/
git commit -m "Sync static content"
./deploy.sh
```

## Cache Management

```bash
# In REPL
(require '[mtz-cms.cache.simple :as cache])

(cache/cache-stats)        # View stats
(cache/clear-cache!)       # Clear all cache

# For video backgrounds
(cache/set-max-entry-size! 50)   # 50MB max
(cache/set-max-entries! 50)      # 50 entries
```

## Deployment

```bash
# Deploy to production
./deploy.sh

# View production logs
mtz-logs

# SSH to production server
mtz-ssh
```

## Remote REPL (Production)

```bash
# Option 1: Direct access
mtz-ssh
docker exec -it mtz-cms clojure -M:dev
# Then: (require 'user) (user/start)

# Option 2: One-liner commands
ssh tmb@trust "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.cache.simple :as cache]) (cache/clear-cache!)\""
```

## Common Tasks

### Create New Static Page
1. In Alfresco: Add `web:pageType` aspect
2. Set properties:
   - `web:kind = "page"`
   - `web:pagetype = "Static"`
   - `web:menuItem = true`
   - `web:menuLabel = "Page Name"`
3. Add HTML document to folder
4. Run sync: `clojure -M -e "(load-file \"src/sync_content.clj\") (sync-content/sync-all!)"`
5. Deploy: `./deploy.sh`

### Debug Navigation Issues
```clojure
(require '[mtz-cms.pathom.resolvers :as pathom])
(pathom/query {} [:site/navigation])   # View nav structure
(cache/clear-cache!)                    # Clear nav cache
```

### Check Alfresco Connection
```clojure
(require '[mtz-cms.alfresco.client :as alfresco])
(alfresco/get-root-node {})
```

### Search for Pages
```clojure
(alfresco/search-nodes {} "web:pagetype:'Static'")
(alfresco/search-nodes {} "web:kind:'page' AND web:menuItem:true")
```

## File Locations

```
admin/
├── docs/              # Documentation
│   ├── CONTENT_MODEL.md          # Aspect reference
│   ├── STATIC_CONTENT_GUIDE.md   # Static pages guide
│   ├── CACHE_MANAGEMENT.md       # Cache config
│   └── REMOTE_REPL.md            # Server access
└── scripts/
    ├── Babashka/      # Sync scripts
    ├── Bash/          # Deployment scripts
    └── Repl/          # REPL commands

src/
├── mtz_cms/           # Main source
└── sync_content.clj   # Static content sync

resources/content/static/   # Generated static pages
```

## Troubleshooting

**Connection refused**
- Run `tunnel` to start SSH tunnel

**Cache not clearing**
- Check REPL is connected: `(user/start)`
- Verify cache namespace loaded: `(require '[mtz-cms.cache.simple :as cache])`

**Static page not found**
- Check file exists: `ls resources/content/static/`
- Verify slug matches URL
- Check static loader: `(require '[mtz-cms.content.static-loader :as static]) (static/load-static-page "slug")`

**Navigation not updating**
- Clear cache: `(cache/clear-cache!)`
- Restart server: `(user/restart)`
- Check Alfresco properties: `web:menuItem` and `web:menuLabel`

## Documentation

- **Full docs:** `admin/docs/INDEX.md`
- **Content model:** `admin/docs/CONTENT_MODEL.md`
- **Static pages:** `admin/docs/STATIC_CONTENT_GUIDE.md`
- **Scripts:** `admin/scripts/README.md`
- **Remote access:** `admin/docs/REMOTE_REPL.md`
- **Pickle Ball setup:** `RUN_PICKLE_BALL_SYNC.md`

## Useful Aliases

Add to your `~/.bashrc` or `~/.zshrc`:

```bash
alias tunnel='ssh -L 8080:localhost:8080 -N -f tmb@trust'
alias tunnel-check='pgrep -f "ssh.*trust" && echo "✅ Tunnel active" || echo "❌ Tunnel not running"'
alias tunnel-kill='pkill -f "ssh.*8080.*trust" && echo "✅ Tunnel killed"'
alias mtz-ssh='ssh tmb@trust'
alias mtz-logs='ssh tmb@trust "docker logs mtz-cms --tail 50 -f"'
```
