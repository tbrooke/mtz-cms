# Remote REPL Access

Guide for accessing the REPL on the production server.

## Option 1: SSH + Direct REPL (Recommended)

Connect directly to the production server and attach to the Docker container's REPL.

```bash
# SSH to server
ssh tmb@trust

# Access container REPL
cd /home/tmb/mtz-cms
docker exec -it mtz-cms clojure -M:dev
```

In the REPL:
```clojure
;; Start user namespace
(require 'user)
(user/start)

;; Load admin commands
(load-file "admin/scripts/Repl/admin.clj")

;; Now use any admin commands
(require '[mtz-cms.cache.simple :as cache])
(cache/cache-stats)
(cache/clear-cache!)
```

**Use this when:** You need full interactive REPL access to production

## Option 2: SSH + One-liner Commands

Execute single commands remotely without entering interactive REPL.

```bash
# Clear cache
ssh tmb@trust "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.cache.simple :as cache]) (cache/clear-cache!)\""

# Check cache stats
ssh tmb@trust "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.cache.simple :as cache]) (cache/cache-stats)\""

# View all pages
ssh tmb@trust "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.pathom.resolvers :as pathom]) (pathom/query {} [:site/pages])\""

# Configure cache for videos
ssh tmb@trust "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.cache.simple :as cache]) (cache/set-max-entry-size! 50)\""
```

**Use this when:** You need to run a quick command without full REPL session

## Option 3: SSH Tunnel + Local Development (Current Setup)

Connect to production Alfresco from local development environment.

```bash
# Start local dev server with tunnel (your current setup)
./start-server.sh
```

This creates an SSH tunnel:
```bash
# Use your alias
tunnel

# Or manually
ssh -L 8080:localhost:8080 -N -f tmb@trust
```

Then develop locally:
```clojure
;; In local REPL
(require 'user)
(user/start)
;; Connects to production Alfresco via tunnel
```

**Use this when:** Developing locally but testing against production Alfresco data

## Common Remote Tasks

### Clear Cache After Deployment
```bash
ssh tmb@trust "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.cache.simple :as cache]) (cache/clear-cache!)\""
```

### Check Server Status
```bash
# View running containers
ssh tmb@trust "docker ps | grep mtz-cms"

# View logs
ssh tmb@trust "docker logs mtz-cms --tail 50"

# Check cache statistics
ssh tmb@trust "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.cache.simple :as cache]) (cache/cache-stats)\""
```

### Update Cache Configuration
```bash
# For video backgrounds
ssh tmb@trust "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.cache.simple :as cache]) (cache/set-max-entry-size! 50) (cache/set-max-entries! 50)\""
```

### View Navigation Structure
```bash
ssh tmb@trust "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.pathom.resolvers :as pathom]) (clojure.pprint/pprint (pathom/query {} [:site/navigation]))\""
```

## Troubleshooting

### Container not running
```bash
ssh tmb@trust "cd /home/tmb/mtz-cms && docker compose up -d"
```

### REPL won't start
```bash
# Check container logs
ssh tmb@trust "docker logs mtz-cms"

# Restart container
ssh tmb@trust "cd /home/tmb/mtz-cms && docker compose restart"
```

### Can't connect to Alfresco
```bash
# From inside container
ssh tmb@trust "docker exec mtz-cms curl -v http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-"
```

## Notes

- The production Docker container has Clojure installed
- All admin scripts and REPL commands work in production
- Use Option 1 for exploratory work
- Use Option 2 for automated tasks
- Use Option 3 for local development

## Bash Aliases

Useful aliases for working with the server:

```bash
# Add to your ~/.bashrc or ~/.zshrc

# SSH Tunnel to Alfresco
alias tunnel='ssh -L 8080:localhost:8080 -N -f tmb@trust'

# Check if tunnel is active
alias tunnel-check='pgrep -f "ssh.*trust" && echo "✅ Tunnel active" || echo "❌ Tunnel not running"'

# Kill tunnel
alias tunnel-kill='pkill -f "ssh.*8080.*trust" && echo "✅ Tunnel killed"'

# Connect to production
alias mtz-ssh='ssh tmb@trust'

# View production logs
alias mtz-logs='ssh tmb@trust "docker logs mtz-cms --tail 50 -f"'
```

Then use:
```bash
tunnel              # Start tunnel
tunnel-check        # Check if running
tunnel-kill         # Stop tunnel
mtz-ssh            # SSH to server
mtz-logs           # View live logs
```
