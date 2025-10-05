# Blog Discovery Guide

## Prerequisites

### 1. Start SSH Tunnel (if connecting remotely)

```bash
ssh -L 8080:localhost:8080 -N -f tmb@trust
```

Verify tunnel is active:
```bash
ps aux | grep "ssh.*trust" | grep -v grep
```

### 2. Test Alfresco Connection

```bash
curl -u admin:admin http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-
```

## Method 1: Using Babashka Script

Once tunnel is active:

```bash
cd /Users/tombrooke/code/trust-server/mtzion/mtz-cms
bb admin/scripts/Babashka/explore_blog.clj
```

This will:
- Find the blog folder in Sites/swsdp/blog
- Extract all blog post properties
- Generate Malli schema
- Save results to `admin/docs/BLOG_SCHEMA.edn`

## Method 2: Using Clojure REPL

```bash
cd /Users/tombrooke/code/trust-server/mtzion/mtz-cms
clojure -M:dev
```

In the REPL:

```clojure
(require 'user)

;; Test connection
(user/test-alfresco)

;; Find blog folder
(user/find-blog-folder)

;; Explore blog structure
(user/explore-blog)

;; Analyze and display all blog posts
(user/analyze-blog-structure)
```

## Method 3: Direct curl Commands

### Find Site Containers

```bash
curl -u admin:admin \
  "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/sites/swsdp/containers"
```

### Search for Blog Folder

```bash
curl -u admin:admin \
  -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8080/alfresco/api/-default-/public/search/versions/1/search" \
  -d '{
    "query": {
      "query": "PATH:\"/app:company_home/st:sites/cm:swsdp/cm:blog/*\"",
      "language": "afts"
    },
    "include": ["properties", "aspectNames"]
  }'
```

### Get Blog Posts (once you have blog folder ID)

```bash
# Replace BLOG_FOLDER_ID with actual ID
curl -u admin:admin \
  "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/BLOG_FOLDER_ID/children?include=properties,aspectNames"
```

### Get Specific Blog Post Details

```bash
# Replace POST_NODE_ID with actual ID
curl -u admin:admin \
  "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/POST_NODE_ID?include=properties,aspectNames"
```

## Expected Blog Post Properties

Based on Alfresco's standard blog model, we expect:

### Core Properties
- `cm:title` - Blog post title
- `cm:description` - Blog post excerpt/summary
- `cm:content` - Full blog post content (HTML)
- `cm:name` - File/node name
- `cm:created` - Creation date
- `cm:modified` - Last modification date
- `cm:creator` - Author username
- `cm:modifier` - Last modifier

### Blog-Specific Aspects
- `blg:blogPost` - Blog post aspect
  - `blg:posted` - Publication date
  - `blg:released` - Release date
  - `blg:lastUpdate` - Last update timestamp

### Possible Additional Properties
- `blg:thumbnail` - Thumbnail image reference
- `blg:tags` - Post tags
- `blg:category` - Post category
- Publishing status (draft/published)
- SEO metadata

## Troubleshooting

### SSH Tunnel Issues

If tunnel connection fails:

```bash
# Kill existing tunnel
pkill -f "ssh.*trust"

# Restart tunnel
ssh -L 8080:localhost:8080 -N -f tmb@trust

# Verify
curl http://localhost:8080/alfresco
```

### Permission Issues

Ensure admin credentials are correct:
- Username: admin
- Password: admin

### Site Not Found

The blog might be in a different site. List all sites:

```bash
curl -u admin:admin \
  "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/sites"
```

## Next Steps

Once blog data is discovered:

1. **Review Properties** - Identify which properties to use for list display
2. **Create Malli Schema** - Validate blog post data structure
3. **Map to CMS Model** - Decide how to map Alfresco blog properties to CMS
4. **Build Pathom Resolver** - Create resolver to fetch and transform blog data
5. **Create List Component** - Build UI component for blog list page
6. **Create Detail Page** - Build UI for individual blog post view

## Output Files

The discovery process will generate:

- `admin/docs/BLOG_SCHEMA.edn` - Discovered schema and sample data
- Malli validation schemas in `src/mtz_cms/validation/schemas.clj`
- Blog resolver in `src/mtz_cms/alfresco/blog_resolvers.clj` (to be created)
