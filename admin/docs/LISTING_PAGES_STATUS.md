# Listing Pages Implementation Status

## Overview

Comprehensive listing pages for dynamic content types in Mount Zion CMS.

## Implementation Status

### ✅ Blog - "Pastor Jim Reflects"
**Status**: COMPLETE - Deployed

**Features**:
- List page at `/blog`
- Detail page at `/blog/:slug`
- Thumbnails (with default icon fallback)
- Full HTML content rendering
- Excerpt generation
- Menu integration (under News dropdown)

**Details**: See [BLOG_COMPLETE_GUIDE.md](BLOG_COMPLETE_GUIDE.md)

---

### ✅ Sunday Worship
**Status**: COMPLETE - Deployed

**Features**:
- List page at `/worship/sunday`
- Detail page at `/worship/sunday/:date`
- Pulpit thumbnail on list items
- Inline PDF viewers (bulletin + presentation)
- Automatic bulletin identification via tags
- Date formatting (09-21-25 → September 21, 2025)
- Menu integration (under Worship dropdown)

**Details**: See [SUNDAY_WORSHIP_STRUCTURE.md](SUNDAY_WORSHIP_STRUCTURE.md)

**Future Enhancements** (Phase 2):
- Add sermon title metadata
- Add hymn information
- Add video/audio recordings
- Embed media player in detail page

---

### ⏸️ Events Calendar
**Status**: NOT STARTED

**Planned Features**:
- List page at `/events`
- Detail page at `/events/:event-id`
- Calendar view integration
- Parse ICS files from Alfresco
- Display upcoming events
- Past events archive

**Data Source**:
- ICS calendar files stored in Alfresco
- May need custom parser for ICS format

**Implementation Notes**:
- Requires ICS file parsing library
- Consider using clj-icalendar or similar
- Design calendar UI component
- Add event filtering (upcoming/past)

---

## Architecture Pattern

All listing pages follow this consistent pattern:

```
1. Data Layer (Alfresco)
   └─> Pathom Resolver (alfresco/*_resolvers.clj)
       └─> Malli Schema Validation (validation/schemas.clj)
           └─> UI Component (components/*.clj)
               └─> Route Handler (routes/main.clj)
                   └─> Navigation Menu (navigation/menu.clj)
```

### Common Components

**Resolvers**:
- List resolver: Fetch all items
- Detail resolver: Fetch single item by ID/slug
- Helper functions: Transform Alfresco data

**Schemas**:
- Alfresco raw data schema
- Display data schema
- List schema
- Detail schema

**UI Components**:
- List item component
- List page component
- Detail page component

**Routes**:
- `GET /[resource]` - List page
- `GET /[resource]/:id` - Detail page
- `GET /api/[resource]/:id` - API endpoints if needed

## Menu Integration

Two approaches used:

1. **Static Submenu** (Blog, Sunday Worship):
   - Defined in `navigation/menu.clj` as `:static-submenu`
   - Combined with dynamic items if parent has children
   - Prevents conflicts with dynamic discovery

2. **Dynamic Discovery** (Other pages):
   - Automatically discovered from Alfresco folders
   - Requires `web:menuItem=true` aspect
   - Flexible but requires Alfresco configuration

## Next Steps

1. **Events Calendar** - Implement when ready
   - Research ICS parsing options
   - Design calendar UI
   - Decide on data structure

2. **Sunday Worship Phase 2** - Add metadata
   - Sermon title, scriptures, preacher
   - Hymn information
   - Video/audio media
   - Enhanced detail page layout

3. **Performance Optimization**
   - Review caching strategy
   - Consider pagination for large lists
   - Optimize Alfresco queries

## Related Documentation

- [LIST_PAGES_PLAN.md](LIST_PAGES_PLAN.md) - Original planning document
- [BLOG_COMPLETE_GUIDE.md](BLOG_COMPLETE_GUIDE.md) - Blog implementation
- [SUNDAY_WORSHIP_STRUCTURE.md](SUNDAY_WORSHIP_STRUCTURE.md) - Sunday Worship implementation
- [MENU_SYSTEM.md](MENU_SYSTEM.md) - Navigation menu system
- [ARCHITECTURE_REFACTOR.md](ARCHITECTURE_REFACTOR.md) - Overall architecture
