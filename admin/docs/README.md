# Mount Zion CMS

A clean, focused content management system for Mount Zion United Church of Christ, built with Clojure.

## Architecture

**Clean & Simple Stack:**
- **Alfresco** - Content repository and storage
- **Pathom** - GraphQL-like data resolution
- **Reitit** - HTTP routing
- **HTMX** - Frontend interactivity
- **Tailwind CSS** - Beautiful, utility-first styling
- **Hiccup** - HTML generation
- **Ring/Jetty** - HTTP server

## Features

- ✅ Direct Alfresco integration
- ✅ Pathom-powered data resolution
- ✅ HTMX-enhanced frontend
- ✅ Beautiful Tailwind CSS styling
- ✅ Clean, maintainable codebase
- ✅ Minimal dependencies
- ✅ Fast development cycle

## Prerequisites

1. **SSH Tunnel to Alfresco** (if remote):
   ```bash
   ssh -L 8080:localhost:8080 -N -f tmb@trust
   ```

2. **Clojure CLI tools** installed

## Quick Start

```bash
# Clone/navigate to project
cd mtz-cms

# Start development REPL
clj -M:repl

# In REPL:
(require 'user)
(user/start)
```

Visit: http://localhost:3000

## Development

### REPL Workflow

```clojure
;; Start server
(user/start)

;; Restart with code reload
(user/restart)

;; Test connections
(user/test-alfresco)
(user/test-pathom)

;; Stop server
(user/stop)
```

### Key URLs

- **Home**: http://localhost:3000/
- **Demo**: http://localhost:3000/demo
- **About**: http://localhost:3000/about

## Project Structure

```
mtz-cms/
├── deps.edn                 # Dependencies
├── src/mtz_cms/
│   ├── core.clj            # Main application
│   ├── routes/main.clj     # HTTP routes
│   ├── pathom/resolvers.clj # Data resolution
│   ├── alfresco/client.clj  # Alfresco API
│   ├── ui/                 # User interface
│   └── config/             # Configuration
└── dev/user.clj            # Development helpers
```

## Configuration

Set environment variables:

```bash
export ALFRESCO_URL=http://localhost:8080
export ALFRESCO_USERNAME=admin
export ALFRESCO_PASSWORD=admin
export PORT=3000
```

## Testing

```clojure
;; Test Alfresco connection
(user/test-alfresco)

;; Test Pathom resolvers  
(user/test-pathom)

;; Manual testing
curl http://localhost:3000/demo
```

## Adding Biff Later

If you need Biff functionality later, add to `deps.edn`:

```clojure
com.biffweb/biff {:git/url "https://github.com/jacobobryant/biff"
                  :git/sha "7836079fc64a6334ea5a43e5e92bcbd7faa1d300"}
```

## Deployment

TBD - Will add deployment instructions once basic functionality is complete.

---

**Built with ❤️ for Mount Zion UCC**