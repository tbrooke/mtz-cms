# Fix for MCP write_content Tool - HTML Upload

**Issue**: mtz-cms-17
**Date**: 2025-10-18
**Status**: Solution Designed

## Problem

The `write_content` tool in `python-alfresco-mcp-server` successfully creates:
- ✅ Folder node
- ✅ web:siteMeta and web:publishable aspects
- ✅ Web properties (menuItem, publishState, etc.)
- ❌ **HTML content file** - FAILS at Step 4

**Root Cause**: `content_utils.upload_file()` call fails to upload HTML content.

## Solution: Replace with Direct httpx Upload

Following the **alfresco-agents-lab-clive** pattern (uses `httpx` for all REST API calls), replace the problematic Step 4 with direct REST API calls.

### Alfresco REST API for Content Upload

```
POST /nodes/{parentId}/children  - Create cm:content node
PUT /nodes/{nodeId}/content      - Upload actual content stream
```

### Fixed Code (Step 4)

Replace lines 186-224 in `write_content.py` with:

```python
# Step 4: Upload HTML file using direct httpx calls
if ctx:
    await ctx.info(f"Step 4/4: Uploading HTML content...")
    await ctx.report_progress(0.8)

logger.info(f"Creating HTML file in folder {folder_id}")

html_filename = f"{name}.html"

try:
    import httpx

    # Get Alfresco config
    alfresco_url = os.getenv('ALFRESCO_URL', 'http://localhost:8080').rstrip('/')
    alfresco_user = os.getenv('ALFRESCO_USERNAME', 'admin')
    alfresco_pass = os.getenv('ALFRESCO_PASSWORD', 'admin')
    verify_ssl = os.getenv('ALFRESCO_VERIFY_SSL', 'false').lower() == 'true'

    # Step 4a: Create cm:content node
    create_url = f"{alfresco_url}/alfresco/api/-default-/public/alfresco/versions/1/nodes/{folder_id}/children"

    node_body = {
        "name": html_filename,
        "nodeType": "cm:content",
        "properties": {
            "cm:title": display_title,
            "cm:description": f"Content for {display_title}"
        }
    }

    async with httpx.AsyncClient(
        verify=verify_ssl,
        timeout=30.0,
        auth=(alfresco_user, alfresco_pass)
    ) as client:
        # Create the node
        create_response = await client.post(
            create_url,
            json=node_body,
            headers={"Content-Type": "application/json"}
        )

        if create_response.status_code not in [200, 201]:
            raise Exception(f"Failed to create HTML node: {create_response.status_code} - {create_response.text}")

        node_data = create_response.json()
        html_file_id = node_data['entry']['id']

        logger.info(f"✅ HTML node created: {html_file_id}")

        # Step 4b: Upload HTML content stream
        content_url = f"{alfresco_url}/alfresco/api/-default-/public/alfresco/versions/1/nodes/{html_file_id}/content"

        upload_response = await client.put(
            content_url,
            content=html_content.encode('utf-8'),
            headers={"Content-Type": "text/html; charset=utf-8"}
        )

        if upload_response.status_code not in [200, 201]:
            raise Exception(f"Failed to upload HTML content: {upload_response.status_code} - {upload_response.text}")

        logger.info(f"✅ HTML content uploaded: {len(html_content)} bytes")

except Exception as e:
    raise Exception(f"HTML upload failed: {str(e)}")
```

## Benefits

1. ✅ **Direct REST API** - No dependency on broken `content_utils`
2. ✅ **Follows lab pattern** - Uses `httpx` like `get_markdown_content`
3. ✅ **Better error handling** - Clear status codes and messages
4. ✅ **Two-step process** - Create node, then upload content (Alfresco best practice)
5. ✅ **Async/await** - Matches FastMCP async patterns

## Testing Plan

1. **Update the code** in python-alfresco-mcp-server
2. **Restart MCP server** (if running via Claude Desktop)
3. **Test from Claude Desktop**:
   ```
   Create a test page in Alfresco:
   - parent_id: [your Pages folder ID]
   - name: Test Article
   - content: <h1>Hello World</h1><p>This is a test.</p>
   - content_type: html
   - publish_state: Draft
   ```
4. **Verify** in Alfresco Share that folder + HTML file exist
5. **Check** in mtz-cms that page displays correctly

## Alternative: CMS API Endpoint

If the fix above still has issues, create a Clojure API endpoint:

```clojure
;; src/mtz_cms/routes/api.clj
(POST "/api/content/create" request
  ;; Use existing alfresco/client.clj functions
  ;; Returns: {:success true :node-id "..." :url "..."}
)
```

MCP tool calls this endpoint instead of direct Alfresco REST API.

## Next Steps

1. ✅ Apply fix to `write_content.py` - DONE
2. ✅ Fix API method names (get_node → get, update_node → update, delete_node → delete) - DONE
3. ✅ Install Python dependencies in virtual environment - DONE
4. ✅ Update Claude Desktop config to use venv Python - DONE
5. Test from Claude Desktop
6. Document workflow
7. Close mtz-cms-17

---

## Additional Fix Applied - 2025-10-18

**Issue Found**: `'NodesClient' object has no attribute 'get_node'`

**Root Cause**: Incorrect method names used in write_content.py

**Fixed Method Names**:
- Line 145: `get_node()` → `get()`
- Line 151: `update_node()` → `update()`
- Line 177: `update_node()` → `update()`
- Line 295: `delete_node()` → `delete()`

**Reference**: Other tools (delete_node.py, checkin_document.py, etc.) use the correct API method names.

**Python Dependencies**:
- Created virtual environment at `/Users/tombrooke/Code/trust-server/mtzion/python-alfresco-mcp-server/venv`
- Installed all dependencies via `pip install -e .`
- Updated Claude Desktop config to use `venv/bin/python3` instead of system `python3`

**Status**: Server starts successfully, ready for end-to-end testing

---

**Created**: 2025-10-18
**Author**: Claude Code
**Status**: Ready for testing in Claude Desktop
