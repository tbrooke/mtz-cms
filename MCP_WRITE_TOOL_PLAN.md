# MCP Tool: Write Content to Alfresco

**Repository**: https://github.com/aborroy/alfresco-agents-lab-clive
**Purpose**: Add MCP tool to write Markdown/HTML content to Alfresco from Claude Desktop
**Status**: Planning Phase - Awaiting Server Access
**Date**: October 13, 2025

---

## 📊 Feasibility Assessment: ✅ HIGHLY FEASIBLE (9/10)

### Why It's Feasible:

1. **✅ Framework Already Exists**
   - MCP server already running in `alfresco-mcp-server/`
   - Existing summarization tool to use as template
   - Python-based (Python 3.11+) with clear structure

2. **✅ Alfresco REST API Support**
   - `POST /api/-default-/public/alfresco/versions/1/nodes/{parentNodeId}/children` - Create nodes
   - `PUT /api/-default-/public/alfresco/versions/1/nodes/{nodeId}/content` - Update content
   - Supports HTML and Markdown content types
   - Full aspect and property management

3. **✅ You Have Working Alfresco Client Code**
   - Your mtz-cms repo has mature Alfresco client (`src/mtz_cms/alfresco/client.clj`)
   - Can reference authentication, error handling, API patterns
   - Already handles aspects (web:siteMeta, web:publishable)

4. **✅ MCP Protocol Perfect Match**
   - Tools can accept complex inputs (content, metadata, options)
   - Returns structured responses (success/error, node ID, URL)
   - Claude Desktop can call with natural language prompts

---

## 🎯 General Plan

### **Tool Specification**

#### Tool Name: `alfresco_create_content`

#### Description:
```
Create a new page or document in Alfresco CMS with Markdown or HTML content.
Perfect for creating web pages, blog posts, or documentation directly from Claude Desktop.
```

#### Input Schema:
```python
{
    "type": "object",
    "properties": {
        "parent_path": {
            "type": "string",
            "description": "Parent folder path in Alfresco (e.g., '/Sites/mtzion/documentLibrary/Pages/Outreach')"
        },
        "name": {
            "type": "string",
            "description": "Name of the new page/document (will become folder name)"
        },
        "title": {
            "type": "string",
            "description": "Display title (optional, defaults to name)"
        },
        "content": {
            "type": "string",
            "description": "Page content in HTML or Markdown format"
        },
        "content_type": {
            "type": "string",
            "enum": ["html", "markdown"],
            "description": "Content format - will convert Markdown to HTML if needed"
        },
        "page_type": {
            "type": "string",
            "enum": ["page", "component"],
            "default": "page",
            "description": "Whether this is a full page or a component"
        },
        "menu_item": {
            "type": "boolean",
            "default": false,
            "description": "Should this page appear in the navigation menu?"
        },
        "menu_label": {
            "type": "string",
            "description": "Custom menu label (optional, uses title if not set)"
        },
        "publish_state": {
            "type": "string",
            "enum": ["Draft", "Publish"],
            "default": "Draft",
            "description": "Publishing state - Draft or Publish"
        },
        "component_type": {
            "type": "string",
            "enum": ["Plain HTML", "Hero", "Feature", "Section"],
            "default": "Plain HTML",
            "description": "Component type (if page_type is 'component')"
        }
    },
    "required": ["parent_path", "name", "content", "content_type"]
}
```

---

## 🏗️ Implementation Steps

### **Phase 1: Core Functionality (3-4 hours)**

#### 1. Add Tool Handler Function
**File**: `alfresco-mcp-server/src/alfresco_mcp/tools.py` (or similar)

```python
import markdown
import requests
from typing import Dict, Any

class AlfrescoContentWriter:
    """Handles creating content in Alfresco"""

    def __init__(self, alfresco_url: str, username: str, password: str):
        self.base_url = alfresco_url
        self.auth = (username, password)
        self.api_base = f"{alfresco_url}/alfresco/api/-default-/public/alfresco/versions/1"

    def markdown_to_html(self, markdown_text: str) -> str:
        """Convert Markdown to HTML"""
        return markdown.markdown(
            markdown_text,
            extensions=['extra', 'codehilite', 'tables']
        )

    def create_page_folder(self, parent_id: str, name: str, title: str) -> Dict[str, Any]:
        """Create a folder node for the page"""
        url = f"{self.api_base}/nodes/{parent_id}/children"

        payload = {
            "name": name,
            "nodeType": "cm:folder",
            "properties": {
                "cm:title": title or name
            }
        }

        response = requests.post(url, json=payload, auth=self.auth)
        response.raise_for_status()

        return response.json()['entry']

    def add_aspects_to_node(self, node_id: str, aspects: list):
        """Add aspects to a node"""
        url = f"{self.api_base}/nodes/{node_id}"

        payload = {
            "aspectNames": aspects
        }

        response = requests.put(url, json=payload, auth=self.auth)
        response.raise_for_status()

    def set_web_properties(self, node_id: str, properties: Dict[str, Any]):
        """Set web:siteMeta properties on a node"""
        url = f"{self.api_base}/nodes/{node_id}"

        payload = {
            "properties": properties
        }

        response = requests.put(url, json=payload, auth=self.auth)
        response.raise_for_status()

    def create_html_file(self, parent_id: str, name: str, html_content: str) -> Dict[str, Any]:
        """Create HTML file inside the page folder"""
        url = f"{self.api_base}/nodes/{parent_id}/children"

        # First create the node
        payload = {
            "name": f"{name}.html",
            "nodeType": "cm:content",
            "properties": {
                "cm:title": name
            }
        }

        response = requests.post(url, json=payload, auth=self.auth)
        response.raise_for_status()
        file_node = response.json()['entry']

        # Then upload content
        content_url = f"{self.api_base}/nodes/{file_node['id']}/content"
        files = {'filedata': (f"{name}.html", html_content, 'text/html')}

        response = requests.put(content_url, files=files, auth=self.auth)
        response.raise_for_status()

        return file_node

    def create_content(self,
                      parent_path: str,
                      name: str,
                      content: str,
                      content_type: str,
                      title: str = None,
                      page_type: str = "page",
                      menu_item: bool = False,
                      menu_label: str = None,
                      publish_state: str = "Draft",
                      component_type: str = "Plain HTML") -> Dict[str, Any]:
        """
        Main function to create content in Alfresco

        Returns:
            Dict with success status, node_id, url, and message
        """
        try:
            # Step 1: Convert Markdown to HTML if needed
            html_content = content if content_type == "html" else self.markdown_to_html(content)

            # Step 2: Resolve parent path to node ID
            parent_id = self.resolve_path_to_node_id(parent_path)
            if not parent_id:
                return {
                    "success": False,
                    "error": f"Parent path not found: {parent_path}"
                }

            # Step 3: Create folder node
            folder = self.create_page_folder(parent_id, name, title or name)
            node_id = folder['id']

            # Step 4: Add required aspects
            self.add_aspects_to_node(node_id, ["web:siteMeta", "web:publishable"])

            # Step 5: Set web properties
            web_props = {
                "web:kind": page_type,
                "web:componentType": component_type,
                "web:menuItem": menu_item,
                "web:publishState": publish_state
            }

            if menu_label:
                web_props["web:menuLabel"] = menu_label

            self.set_web_properties(node_id, web_props)

            # Step 6: Create HTML file inside folder
            self.create_html_file(node_id, name, html_content)

            # Step 7: Return success
            return {
                "success": True,
                "node_id": node_id,
                "url": f"{self.base_url}/share/page/document-details?nodeRef=workspace://SpacesStore/{node_id}",
                "message": f"Successfully created {'page' if page_type == 'page' else 'component'}: {name}",
                "publish_state": publish_state,
                "menu_item": menu_item
            }

        except requests.HTTPError as e:
            return {
                "success": False,
                "error": f"Alfresco API error: {e.response.status_code} - {e.response.text}"
            }
        except Exception as e:
            return {
                "success": False,
                "error": f"Unexpected error: {str(e)}"
            }

    def resolve_path_to_node_id(self, path: str) -> str:
        """Convert Alfresco path to node ID"""
        # TODO: Implement path resolution using search or traversal
        # For now, could use a mapping or search API
        # Example: GET /nodes/-root-?relativePath={path}
        url = f"{self.api_base}/nodes/-root-"
        params = {"relativePath": path}

        response = requests.get(url, params=params, auth=self.auth)
        if response.status_code == 200:
            return response.json()['entry']['id']
        return None
```

#### 2. Register Tool in MCP Server
**File**: `alfresco-mcp-server/src/alfresco_mcp/server.py`

```python
from mcp.server import Server
from alfresco_mcp.tools import AlfrescoContentWriter

# Initialize
server = Server("alfresco-mcp")
content_writer = AlfrescoContentWriter(
    alfresco_url=os.getenv("ALFRESCO_URL"),
    username=os.getenv("ALFRESCO_USERNAME"),
    password=os.getenv("ALFRESCO_PASSWORD")
)

@server.list_tools()
async def handle_list_tools() -> list[Tool]:
    """List available MCP tools"""
    return [
        # Existing summarize tool...

        # NEW: Create content tool
        Tool(
            name="alfresco_create_content",
            description="Create a new page or document in Alfresco CMS with Markdown or HTML content",
            inputSchema={
                "type": "object",
                "properties": {
                    # ... (input schema from above)
                },
                "required": ["parent_path", "name", "content", "content_type"]
            }
        )
    ]

@server.call_tool()
async def handle_call_tool(name: str, arguments: dict) -> list[TextContent]:
    """Handle tool execution"""

    if name == "alfresco_create_content":
        result = content_writer.create_content(**arguments)

        if result['success']:
            return [
                TextContent(
                    type="text",
                    text=f"✅ {result['message']}\n\n"
                         f"Node ID: {result['node_id']}\n"
                         f"URL: {result['url']}\n"
                         f"Status: {result['publish_state']}\n"
                         f"In Menu: {'Yes' if result['menu_item'] else 'No'}"
                )
            ]
        else:
            return [
                TextContent(
                    type="text",
                    text=f"❌ Failed to create content: {result['error']}"
                )
            ]

    # ... other tools
```

#### 3. Add Dependencies
**File**: `alfresco-mcp-server/requirements.txt`

```txt
# Existing dependencies...
markdown>=3.5.0
```

---

### **Phase 2: Enhanced Features (2-3 hours)**

1. **Content Validation**
   - HTML sanitization (prevent XSS)
   - Content size limits
   - Name validation (no special chars)

2. **Error Handling**
   - Check if page already exists
   - Validate parent path exists
   - Handle permission errors
   - Rollback on partial failure

3. **Path Resolution**
   - Support both absolute paths and node IDs
   - Path autocomplete/suggestions
   - Common parent paths as shortcuts

4. **Markdown Enhancements**
   - Support front matter (YAML metadata)
   - Custom CSS classes
   - Image handling (upload referenced images)

---

### **Phase 3: Testing (1-2 hours)**

1. **Unit Tests**
```python
def test_create_simple_page():
    writer = AlfrescoContentWriter(url, user, pass)
    result = writer.create_content(
        parent_path="/Sites/test/documentLibrary/Pages",
        name="Test Page",
        content="# Hello World\n\nThis is a test.",
        content_type="markdown"
    )
    assert result['success'] == True
    assert result['node_id'] is not None
```

2. **Integration Tests**
   - Create page with menu item
   - Create component
   - Create with publish state
   - Error cases (bad path, duplicate name)

3. **Claude Desktop Testing**
   - Natural language prompts
   - Complex content formatting
   - Multiple pages in sequence

---

## 💡 Usage Examples

### Example 1: Simple Page
**User to Claude Desktop:**
> "Create a new page called 'Community Garden' under /Sites/mtzion/documentLibrary/Pages/Outreach with this content:
>
> # Community Garden Program
>
> Join us every Saturday at 10am to tend our community garden.
>
> ## What to Bring:
> - Gloves
> - Water bottle
> - Enthusiasm!"

**Claude calls:**
```json
{
  "tool": "alfresco_create_content",
  "arguments": {
    "parent_path": "/Sites/mtzion/documentLibrary/Pages/Outreach",
    "name": "Community Garden",
    "title": "Community Garden Program",
    "content": "# Community Garden Program\n\nJoin us...",
    "content_type": "markdown",
    "page_type": "page",
    "menu_item": true,
    "publish_state": "Draft"
  }
}
```

**Response:**
> ✅ Successfully created page: Community Garden
>
> Node ID: abc-123-def-456
> URL: http://localhost:8080/share/page/document-details?nodeRef=...
> Status: Draft
> In Menu: Yes
>
> The page has been created as a draft. Would you like me to publish it?

### Example 2: Create Component
**User:**
> "Create a hero component for the homepage with a welcome message"

**Claude calls:**
```json
{
  "tool": "alfresco_create_content",
  "arguments": {
    "parent_path": "/Sites/mtzion/documentLibrary/Components",
    "name": "Homepage Hero",
    "content": "<div class='hero'>Welcome to Mount Zion!</div>",
    "content_type": "html",
    "page_type": "component",
    "component_type": "Hero",
    "publish_state": "Publish"
  }
}
```

---

## 🔧 Technical Considerations

### **1. Alfresco Folder + HTML Structure**

Mount Zion CMS uses this pattern:
```
Page Folder (cm:folder)
├── aspects: web:siteMeta, web:publishable
├── properties:
│   ├── web:kind = "page"
│   ├── web:menuItem = true
│   └── web:publishState = "Publish"
└── content.html (cm:content)
    └── HTML content
```

Your tool must replicate this structure.

### **2. Markdown Processing**

Use Python's `markdown` library with extensions:
```python
import markdown

html = markdown.markdown(
    markdown_text,
    extensions=[
        'extra',       # Tables, footnotes, etc.
        'codehilite',  # Syntax highlighting
        'tables',      # Table support
        'nl2br',       # Newline to <br>
    ]
)
```

### **3. Authentication**

MCP server should read from environment:
```bash
ALFRESCO_URL=http://localhost:8080
ALFRESCO_USERNAME=admin
ALFRESCO_PASSWORD=admin
```

### **4. Error Recovery**

If folder created but HTML upload fails:
```python
try:
    folder = create_folder()
    add_aspects(folder['id'])
    set_properties(folder['id'])
    create_html_file(folder['id'])
except Exception as e:
    # Rollback: delete the folder
    delete_node(folder['id'])
    raise
```

---

## 📊 Estimated Effort

| Phase | Task | Hours |
|-------|------|-------|
| 1 | Core tool implementation | 3-4 |
| 2 | Enhanced features | 2-3 |
| 3 | Testing & debugging | 1-2 |
| 4 | Documentation | 1 |
| **Total** | | **7-10 hours** |

---

## 🚀 Next Steps (When Server Available)

### Preparation:
1. ✅ Clone alfresco-agents-lab-clive repo
2. ✅ Study existing summarize tool implementation
3. ✅ Set up local Alfresco environment with Docker Compose
4. ✅ Test MCP server with existing tools

### Implementation:
1. Create new branch: `feature/write-content-tool`
2. Implement `AlfrescoContentWriter` class
3. Register tool in MCP server
4. Add tests
5. Test with Claude Desktop
6. Create PR with documentation

### Future Enhancements:
- **Update Content Tool**: Modify existing pages
- **Delete Content Tool**: Remove pages
- **Bulk Import Tool**: Upload multiple pages from directory
- **Template Tool**: Create pages from templates
- **Image Upload Tool**: Upload images referenced in Markdown

---

## 🎯 Success Criteria

✅ Tool successfully creates pages in Alfresco
✅ Markdown converts to HTML correctly
✅ Aspects and properties set properly
✅ Pages appear in Alfresco Content App
✅ Menu items show in navigation (if requested)
✅ Claude Desktop can use tool with natural language
✅ Error messages are clear and actionable
✅ Tests pass with 90%+ coverage

---

## 📚 Reference Materials

### Alfresco REST API Docs:
- Create Node: `POST /nodes/{parentId}/children`
- Update Node: `PUT /nodes/{nodeId}`
- Upload Content: `PUT /nodes/{nodeId}/content`
- Path Resolution: `GET /nodes/-root-?relativePath={path}`

### MCP Protocol:
- Tool Schema: https://modelcontextprotocol.io/docs/tools
- Python SDK: https://github.com/modelcontextprotocol/python-sdk

### Your Code References:
- `mtz-cms/src/mtz_cms/alfresco/client.clj` - Alfresco API patterns
- `mtz-cms/src/mtz_cms/alfresco/discovery.clj` - Aspect handling
- `mtz-cms/admin/docs/MENU_SYSTEM.md` - Menu item requirements

---

**Created**: 2025-10-13
**Author**: Claude (via Claude Code)
**Status**: Ready for implementation when server access available

