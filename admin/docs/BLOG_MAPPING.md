# Blog Data Mapping

## Discovered Alfresco Blog Structure

### Blog Location
- **Path**: Sites/swsdp/blog
- **Container ID**: `f5e1e5ed-ba6f-471d-a1e5-edba6fe71db1`

### Sample Blog Post Data

```edn
{:node-id "83b5ad4e-a60f-440d-b5ad-4ea60f940d5b"
 :name "post-1759344630358"
 :title "New Blog Post"
 :description nil
 :created "2025-10-01T18:50:30.359+0000"
 :modified "2025-10-04T02:21:07.996+0000"
 :author "Tom Brooke"
 :aspects ["rn:renditioned" "cm:titled" "cm:auditable" "cm:taggable"
           "cm:syndication" "cm:thumbnailModification"]
 :properties
 {:cm:title "New Blog Post"
  :cm:published "2025-10-04T02:13:53.260+0000"
  :cm:updated "2025-10-04T02:21:08.004+0000"
  :cm:lastThumbnailModification ["doclib:1759583245482" "pdf:1759583260776"]
  :cm:taggable []}}
```

## Alfresco → CMS Mapping

### Properties Mapping

| Alfresco Property | CMS Field | Transformation | Notes |
|------------------|-----------|----------------|-------|
| `node-id` | `:blog/id` | Direct | Unique identifier |
| `name` | `:blog/slug` | Direct | URL-friendly slug |
| `:cm:title` | `:blog/title` | Direct | Blog post title |
| `:cm:description` | `:blog/description` | Direct or generate | May be nil, generate from content |
| `:cm:description` | `:blog/excerpt` | Truncate | First 200 chars |
| `:cm:published` | `:blog/published-at` | Direct | Publication date |
| `:cm:updated` | `:blog/updated-at` | Direct | Last update date |
| `author` | `:blog/author` | Direct | Author display name |
| `:cm:taggable` | `:blog/tags` | Direct | Array of tags |
| `:cm:lastThumbnailModification` | `:blog/thumbnail` | Extract ID → URL | Get first thumbnail |

### Aspects Available

1. **`cm:titled`** - Has title property ✅
2. **`cm:auditable`** - Creation/modification tracking ✅
3. **`cm:taggable`** - Tagging support ✅
4. **`cm:syndication`** - Publishing/syndication ✅
5. **`cm:thumbnailModification`** - Thumbnail support ✅
6. **`rn:renditioned`** - Has renditions (PDF, etc.) ✅

## Display Mappings

### Blog List Item (for /blog page)

```clojure
{:blog/id "83b5ad4e-a60f-440d-b5ad-4ea60f940d5b"
 :blog/slug "post-1759344630358"
 :blog/title "New Blog Post"
 :blog/description nil  ; or excerpt from content
 :blog/excerpt "First 200 characters..."
 :blog/published-at "2025-10-04T02:13:53.260+0000"
 :blog/updated-at "2025-10-04T02:21:08.004+0000"
 :blog/author "Tom Brooke"
 :blog/tags []
 :blog/thumbnail "/api/image/thumbnail-node-id"}
```

### Blog Detail (for /blog/:slug page)

```clojure
{:blog/id "83b5ad4e-a60f-440d-b5ad-4ea60f940d5b"
 :blog/slug "post-1759344630358"
 :blog/title "New Blog Post"
 :blog/content "<html content from Alfresco>"
 :blog/description "Description or excerpt"
 :blog/published-at "2025-10-04T02:13:53.260+0000"
 :blog/updated-at "2025-10-04T02:21:08.004+0000"
 :blog/author "Tom Brooke"
 :blog/tags []
 :blog/thumbnail "/api/image/thumbnail-node-id"}
```

## Content Fetching Strategy

### Blog List Query

```clojure
;; Get blog folder children (all posts)
(alfresco/get-node-children ctx "f5e1e5ed-ba6f-471d-a1e5-edba6fe71db1"
  {:include "properties,aspectNames"
   :where "(cm:published IS NOT NULL)"  ; Only published posts
   :orderBy "cm:published DESC"})       ; Newest first
```

### Blog Content Fetching

```clojure
;; Get HTML content for blog post
(alfresco/get-node-content ctx node-id)
;; Returns binary data, convert to UTF-8 string
```

### Thumbnail Handling

```clojure
;; From cm:lastThumbnailModification: ["doclib:1759583245482" "pdf:1759583260776"]
;; Extract first: "doclib:1759583245482"
;; Format: "rendition-type:node-id"
;; Extract node-id: "1759583245482"
;; Build URL: "/api/image/1759583245482"
```

## Transformation Pipeline

```
Alfresco Raw Data
       ↓
[extract-blog-post-data]  ← Maps properties, extracts aspects
       ↓
Validated with :blog/post-alfresco schema
       ↓
[transform-for-display]   ← Formats dates, builds URLs, creates excerpt
       ↓
Validated with :blog/post-display schema
       ↓
Display-ready blog post
```

## Publishing Filter

Only show posts where:
- `cm:published` is not null
- `cm:published` <= current date/time (not future-dated)
- (Optional) `publishStatus` = "published" if using custom aspect

## Sorting & Pagination

- **Default Sort**: `cm:published DESC` (newest first)
- **Pagination**: Use Alfresco's `skipCount` and `maxItems`
- **Items Per Page**: 10-20 posts

## Missing Content Handling

### If Description is Null
1. Try to fetch content
2. Strip HTML tags
3. Take first 200 characters
4. Add "..." if truncated

### If Thumbnail is Missing
1. Use default blog post image
2. Or extract first image from content
3. Or use site/category default

### If Tags are Empty
- Display nothing or "Uncategorized"

## Future Enhancements

### Categories
- Could use `cm:categories` aspect
- Or custom `blog:category` property

### SEO
- Add `seo:title`, `seo:description`, `seo:keywords`
- Extract from content if not set

### Featured Posts
- Add custom `blog:featured` boolean
- Display differently on list page

### Draft vs Published
- Add `blog:publishStatus` with values: draft, published, scheduled
- Filter list by status
