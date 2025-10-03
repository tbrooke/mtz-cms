# Static Content Generation Guide

## Overview

Mount Zion CMS now supports **hybrid content**: some pages static (fast), some dynamic (always fresh).

## How It Works

### 1. Mark Pages in Alfresco

In Alfresco, set the `web:pagetype` property to "Static" for pages you want to generate statically:

```
Aspects to add:
- web:pageType

Properties:
- web:kind = "page"
- web:pagetype = "Static"  (← Use this instead of web:generateStatic aspect)
- web:menuItem = true/false
- web:menuLabel = "About" (if menuItem = true)
```

**Recommended for static:**
- About page (rarely changes)
- Worship info (stable content)
- Contact info (rarely updates)
- Preschool info (stable)

**Keep dynamic:**
- News/Blog posts (frequent updates)
- Events (time-sensitive)
- Hero banners (marketing changes often)

### 2. Generate Static Files

Run the sync script:

```bash
clojure -M -m sync-content
```

This will:
- Query Alfresco for pages with `web:pagetype="Static"`
- Fetch their content
- Generate `.edn` files in `resources/content/static/`
- You can then edit these files manually if needed

### 3. Commit and Deploy

```bash
git add resources/content/static/
git commit -m "Update static content from Alfresco"
./deploy.sh
```

## Runtime Behavior

When a user requests `/page/about`:

1. Router checks `resources/content/static/about.edn`
2. **If found** → Serves static content (⚡ fast, no Alfresco call)
3. **If not found** → Fetches from Alfresco dynamically (🔄 cached for 1 hour)

You'll see in logs:
```
📄 Serving STATIC page: about
🔄 Serving DYNAMIC page: events
```

## Example Static Content File

`resources/content/static/about.edn`:

```clojure
{:title "About Mount Zion UCC"
 :slug "about"
 :node-id "8158a6aa-dbd7-4f5b-98a6-aadbd72f5b3b"
 :content "<div class=\"prose\">...</div>"
 :generated-at "2025-10-01T22:00:00Z"
 :static true}
```

You can edit the `:content` HTML directly if needed!

## Benefits

### Static Pages
✅ **Fast** - No Alfresco API calls
✅ **Reliable** - Works even if Alfresco is down
✅ **Editable** - Can tweak HTML in editor
✅ **Version controlled** - Content changes tracked in git

### Dynamic Pages
✅ **Always fresh** - Real-time from Alfresco
✅ **Cached** - Still fast (1 hour cache)
✅ **CMS-managed** - Content editors use Alfresco

## Workflow

**For content editors:**
1. Edit content in Alfresco
2. For static pages: Tell developer "sync content"
3. For dynamic pages: Changes appear within 1 hour (or clear cache)

**For developers:**
```bash
# When content editors update static pages:
clojure -M -m sync-content
git add resources/content/static/
git commit -m "Sync static content"
./deploy.sh

# To force refresh dynamic pages:
ssh server "docker exec mtz-cms clj -M -e \"(require '[mtz-cms.cache.simple :as cache]) (cache/clear-cache!)\""
```

## Files

- `sync-content.clj` - Sync script (pulls from Alfresco)
- `src/mtz_cms/content/static_loader.clj` - Static content loader
- `resources/content/static/*.edn` - Static content files (committed to git)
- `src/mtz_cms/routes/main.clj` - Router (checks static-first)

## Future Enhancements

- [ ] Implement Alfresco search query in sync script
- [ ] Add `--page` flag to sync specific pages
- [ ] Auto-sync on deploy via CI/CD
- [ ] Preview static pages before committing
