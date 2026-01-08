# Sync Static Pages - Quick Guide

## Easy Way (Recommended)

```bash
# From anywhere in the project
./sync-static-content.sh
```

This script will:
- Check if SSH tunnel is running (prompt to start if not)
- Navigate to project root
- Run the sync
- Show results

## Manual Way

```bash
# Must be in project root (where deps.edn is)
cd /Users/tombrooke/Code/trust-server/mtzion/mtz-cms

# Make sure tunnel is active
tunnel

# Run sync
clojure -M -e "(load-file \"src/sync_content.clj\") (sync-content/sync-all!)"
```

## What Gets Synced

All pages in Alfresco with:
- `web:pagetype = "Static"`
- `web:kind = "page"`

Currently:
- ✅ Pickle Ball page

## After Sync

```bash
# View generated files
ls -la resources/content/static/

# Check a specific file
cat "resources/content/static/Pickle Ball.edn"

# Commit changes
git add resources/content/static/
git commit -m "Sync static pages from Alfresco"

# Deploy
./deploy.sh
```

## Troubleshooting

### "Could not locate sync_content"
- You're not in the project root
- Use the wrapper script: `./sync-static-content.sh`
- Or manually: `cd /Users/tombrooke/Code/trust-server/mtzion/mtz-cms`

### "Connection refused"
- SSH tunnel not running
- Run: `tunnel` or let the script start it for you

### "Found 0 static pages"
- Check Alfresco page has `web:pagetype="Static"` property
- Verify the aspect is applied: `web:pageType`
- Check the page's properties in Alfresco

## Quick Checklist

- [ ] In project root (or using `./sync-static-content.sh`)
- [ ] SSH tunnel active (`tunnel` or script will ask)
- [ ] Page marked as Static in Alfresco
- [ ] HTML file in page folder
- [ ] Run sync
- [ ] Review `.edn` file
- [ ] Commit and deploy
