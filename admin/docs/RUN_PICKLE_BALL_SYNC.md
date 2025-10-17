# Generate Pickle Ball Static Page

## Prerequisites

1. **SSH Tunnel Must Be Running**
   ```bash
   # Check if tunnel is active
   pgrep -f "ssh.*trust"

   # If not running, start it with your alias
   tunnel

   # Or manually:
   # ssh -L 8080:localhost:8080 -N -f tmb@trust
   ```

2. **Verify Alfresco is accessible**
   ```bash
   curl -s -u admin:admin http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root- | head -5
   ```

## Run the Sync

```bash
# From project root
clojure -M -e "(load-file \"src/sync_content.clj\") (sync-content/sync-all!)"
```

## What It Does

1. **Searches Alfresco** for pages with:
   - `web:pagetype = "Static"`
   - `web:kind = "page"`
   
2. **Finds Pickle Ball** (and any other static pages)

3. **Fetches Content**:
   - Gets the Pickle Ball folder
   - Finds the HTML document inside
   - Reads the HTML content

4. **Generates Static File**:
   - Creates `resources/content/static/Pickle Ball.edn`
   - (or uses web:menuLabel as filename)

## Expected Output

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔄 Static Content Sync - Mount Zion CMS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🔍 Searching for pages marked as static...
   Query: web:pagetype:'Static' AND web:kind:'page'
   ✅ Found 1 static pages

📄 Pickle Ball (Pickle Ball)
  📥 Fetching content for node: abc-123-node-id
  ✅ Generated: resources/content/static/Pickle Ball.edn

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ Sync Complete!
   Generated: 1 / 1 pages
   Location: resources/content/static
```

## Generated File

`resources/content/static/Pickle Ball.edn`:
```clojure
{:title "Pickle Ball",
 :slug "Pickle Ball",
 :node-id "abc-123-node-id",
 :content "<div class=\"pickle-ball-content\">...HTML from Alfresco...</div>",
 :menu-label "Pickle Ball",
 :menu-item true,
 :static true,
 :generated-at "2025-10-03T12:34:56.789Z"}
```

## View the Page

After sync:
```bash
# Check static file was created
ls -la resources/content/static/

# View content
cat "resources/content/static/Pickle Ball.edn"
```

Website will serve it at:
- `/page/pickle-ball` (or whatever the slug is)
- Loads from static file (fast, no Alfresco call)
- Shows in navigation menu (menuItem=true)

## Troubleshooting

### "Connection refused"
- SSH tunnel not running
- Start with: `tunnel` (or `ssh -L 8080:localhost:8080 -N -f tmb@trust`)

### "Found 0 static pages"
- Pickle Ball page doesn't have `web:pagetype="Static"`
- Check in Alfresco that the property is set
- Verify with: Check admin/docs/CONTENT_MODEL.md

### "No HTML file found in folder"
- The Pickle Ball folder doesn't contain an .html file
- Check folder contents in Alfresco
- HTML file must have .html extension

## Next Steps After Sync

1. **Review the generated file**
   ```bash
   cat "resources/content/static/Pickle Ball.edn"
   ```

2. **Edit if needed** (optional)
   - You can manually edit the .edn file
   - Update content, title, etc.

3. **Commit to git**
   ```bash
   git add resources/content/static/
   git commit -m "Add Pickle Ball static page"
   ```

4. **Deploy**
   ```bash
   ./deploy.sh
   ```

5. **Visit page**
   - https://mtzcg.com/page/pickle-ball (or appropriate slug)
