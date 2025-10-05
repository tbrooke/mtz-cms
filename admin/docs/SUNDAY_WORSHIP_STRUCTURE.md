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

## Implementation Plan

1. **Malli Schemas** - Validate worship service data
2. **Pathom Resolver** - Fetch and transform date folders + PDFs
3. **Helper Functions** - Identify bulletin vs presentation
4. **List Component** - Display services with bulletin thumbnails
5. **Detail Component** - Display both PDFs side-by-side
6. **Routes** - `/worship/sunday` and `/worship/sunday/:date`

## Notes

- Date folders may be empty (like 09-07-25) - handle gracefully
- PDFs may not always have tags - use fallback identification
- Thumbnails come from Alfresco's PDF rendition system
- Sort by date (newest first) for list view
