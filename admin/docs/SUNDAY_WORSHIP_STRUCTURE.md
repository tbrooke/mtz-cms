# Sunday Worship Structure

## Discovered Structure

### Folder Hierarchy
```
Sunday Worship (a2e853fa-28f5-42d9-a853-fa28f522d918)
├── 09-07-25 (51a081a4-e404-4ee1-a081-a4e4046ee19e) - Empty
├── 09-14-25 (bbf027a4-b6a0-46d8-b027-a4b6a096d85e)
│   ├── 9-14-25.pdf (bulletin - tagged)
│   └── September 14.pdf (presentation - no tag)
├── 09-21-25 (fecde9be-36d3-4ea5-8de9-be36d3dea599)
│   ├── Copy of 9-21-25.pdf (bulletin - tagged)
│   └── September 21,2025.pdf (presentation - no tag)
└── 09-28-25 (447129c5-b85b-4d5b-b129-c5b85b2d5b78)
    ├── 9-28-25.pdf (bulletin - no tag)
    └── September 28.pdf (presentation - no tag)
```

### Tag Structure

- **Tags** are stored as `cm:category` nodes
- **Bulletin tag ID**: `559084bc-1b4b-467c-9084-bc1b4b067c69`
- **Tag name**: "bulletin"
- **Tag storage**: In `cm:taggable` property as array of node IDs

Example:
```json
{
  "properties": {
    "cm:taggable": ["559084bc-1b4b-467c-9084-bc1b4b067c69"]
  }
}
```

## Data Model

### Sunday Worship Service

```clojure
{:worship/date "09-21-25"                    ; Folder name (date)
 :worship/date-formatted "September 21, 2025" ; Human-readable
 :worship/folder-id "fecde9be-..."           ; Date folder ID
 :worship/bulletin {:pdf/id "490b0c96-..."   ; Bulletin PDF
                    :pdf/name "Copy of 9-21-25.pdf"
                    :pdf/url "/api/pdf/490b0c96-..."
                    :pdf/thumbnail "/api/image/490b0c96-..."}
 :worship/presentation {:pdf/id "5fb78fca-..." ; Presentation PDF
                       :pdf/name "September 21,2025.pdf"
                       :pdf/url "/api/pdf/5fb78fca-..."
                       :pdf/thumbnail "/api/image/5fb78fca-..."}}
```

## Identification Strategy

### Bulletin PDF
1. Check for `cm:taggable` containing bulletin tag ID (`559084bc-1b4b-467c-9084-bc1b4b067c69`)
2. Fallback: Look for filename patterns (shorter filename, contains date numbers)

### Presentation PDF
1. The "other" PDF (not tagged as bulletin)
2. Usually has longer, descriptive filename

## Page Design

### List Page (`/worship/sunday`)

**Layout**:
```
┌─────────────────────────────────────────────────────┐
│  Sunday Worship                                     │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────┐  September 28, 2025                       │
│  │      │  Bulletin: 9-28-25.pdf                    │
│  │ PDF  │  Presentation: September 28.pdf           │
│  │THUMB │                                            │
│  └──────┘                                            │
│  ───────────────────────────────────────────────────│
│  ┌──────┐  September 21, 2025                       │
│  │      │  Bulletin: Copy of 9-21-25.pdf            │
│  │ PDF  │  Presentation: September 21,2025.pdf      │
│  │THUMB │                                            │
│  └──────┘                                            │
└─────────────────────────────────────────────────────┘
```

### Detail Page (`/worship/sunday/09-21-25`)

**Layout**:
```
┌─────────────────────────────────────────────────────┐
│  Sunday Worship                                     │
├─────────────────────────────────────────────────────┤
│                                                      │
│  September 21, 2025                                 │
│                                                      │
│  ┌──────────────────┐  ┌──────────────────┐        │
│  │                  │  │                  │        │
│  │   Bulletin PDF   │  │ Presentation PDF │        │
│  │   Thumbnail      │  │   Thumbnail      │        │
│  │                  │  │                  │        │
│  │  [View/Download] │  │  [View/Download] │        │
│  └──────────────────┘  └──────────────────┘        │
│                                                      │
│  ← Back to Sunday Worship                           │
└─────────────────────────────────────────────────────┘
```

## API Endpoints Needed

- `GET /worship/sunday` - List page
- `GET /worship/sunday/:date` - Detail page (e.g., `/worship/sunday/09-21-25`)
- `GET /api/pdf/:node-id` - Serve PDF with proper headers
- PDF thumbnails use existing `/api/image/:node-id` (Alfresco generates PDF thumbnails)

## Implementation Status

✅ **COMPLETE** - All features implemented and deployed

### Completed Components

1. ✅ **Malli Schemas** (`validation/schemas.clj`)
   - `worship-pdf-schema` - PDF document structure
   - `worship-service-schema` - Single service data
   - `worship-list-schema` - List of services

2. ✅ **Pathom Resolver** (`alfresco/sunday_worship_resolvers.clj`)
   - `sunday-worship-list-resolver` - Fetches all services
   - `sunday-worship-detail-resolver` - Fetches single service by date
   - Helper functions for bulletin identification and date formatting

3. ✅ **UI Components** (`components/sunday_worship.clj`)
   - `worship-list-item` - List item with pulpit thumbnail
   - `sunday-worship-list-page` - Full list page
   - `pdf-card` - PDF viewer with inline iframe
   - `sunday-worship-detail-page` - Detail page with both PDFs

4. ✅ **Routes** (`routes/main.clj`)
   - `/worship/sunday` - List page
   - `/worship/sunday/:date` - Detail page
   - `/api/pdf/:node-id` - PDF serving with caching

5. ✅ **Navigation** (`navigation/menu.clj`)
   - Added "Sunday Worship" to Worship submenu
   - Set `web:menuItem=false` in Alfresco to prevent duplicate menu items

### Implementation Details

**Thumbnail Strategy**:
- **List page**: Uses static pulpit image (`/images/pulpit.jpg`) for all thumbnails
- **Detail page**: Embeds PDFs inline using `<iframe>` elements
- Alfresco PDF renditions not used due to availability issues

**PDF Identification**:
- Primary: Check for bulletin tag ID in `cm:taggable` property
- Fallback: Assume shorter filename is bulletin

**Date Formatting**:
- Input: "09-21-25" (folder name)
- Output: "September 21, 2025" (human-readable)

**Caching**:
- PDFs cached for 24 hours via `cache/cached`
- Image proxy handles both regular images and PDF thumbnails

## Future Enhancements

### Phase 2: Metadata Enhancement
**Status**: Planned (not started)

Add additional metadata to Sunday Worship services:

1. **Sermon Information**
   - Sermon title
   - Scripture references
   - Preacher name

2. **Hymn Information**
   - Opening hymn
   - Closing hymn
   - Special music

3. **Media**
   - Video recording of sermon
   - Audio recording
   - Embed video player in detail page

4. **Implementation Approach**
   - Add custom aspects to Alfresco content model
   - Update schemas to include new fields
   - Enhance resolver to extract metadata
   - Update UI components to display new fields

**Storage Options**:
- Option A: Store as Alfresco properties on date folder
- Option B: Create separate metadata file in each date folder
- Option C: Use aspect properties on bulletin PDF

## Notes

- Date folders may be empty (like 09-07-25) - gracefully filtered out
- PDFs may not always have tags - fallback identification works well
- Sort by date (newest first) for list view
- Menu item set to static to avoid conflicts with dynamic discovery
