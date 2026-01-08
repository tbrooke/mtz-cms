# 📊 Alfresco Content Model Analysis

**Generated:** 2025-09-30
**Source:** Live Alfresco API via SSH tunnel

---

## 🎯 Key Findings

### **Current Reality: Simple `cm:folder` Structure**

Your Hero and Feature components are currently:
- **Type:** `cm:folder` (standard Alfresco folder)
- **Aspects:** `cm:titled` + `cm:auditable`
- **Properties:** Only `cm:title` + standard audit fields

### **What This Means:**

✅ **It's working!** Your current pathom resolvers correctly treat these as folders with children
❌ **No custom content model deployed yet** - `mtz.xml` hasn't been applied to Alfresco
✅ **You have a clean slate** - Can design the ideal model based on actual needs

---

## 📋 Current Node Structure

### **Hero Node** (`39985c5c-201a-42f6-985c-5c201a62f6d8`)
```clojure
{:node-type "cm:folder"
 :name "Hero"
 :aspects ["cm:titled" "cm:auditable"]
 :properties {:cm:title "The Hero Graphic Dafault large image or images"}
 :children 1  ; Contains buildingC.png
}
```

**What's Actually Stored:**
- Folder with title
- Child node: `buildingC.png` (image file)
- Your resolver extracts the image from children

### **Feature 1 Node** (`264ab06c-984e-4f64-8ab0-6c984eaf6440`)
```clojure
{:node-type "cm:folder"
 :name "Feature 1"
 :aspects ["cm:titled" "cm:auditable"]
 :properties {:cm:title "first feature on home page"}
}
```

###  **Feature 2 Node** (`fe3c64bf-bb1b-456f-bc64-bfbb1b656f89`)
```clojure
{:node-type "cm:folder"
 :name "Feature 2"
 :aspects ["cm:titled" "cm:auditable"]
 :properties {:cm:title "Second feature on Home page"}
}
```

---

## 🤔 Design Decision: Folder-Based vs Type-Based

### **Option A: Keep Current Folder-Based Approach** ✅ **Recommended for Now**

**How it works:**
- Component = Folder with conventional structure
- Title → `cm:title` property
- Body content → Child text/HTML file
- Images → Child image files
- Ordering → Folder name or creation date

**Pros:**
- ✅ Already working
- ✅ No custom model deployment needed
- ✅ Familiar to content editors (just folders!)
- ✅ Flexible - any content type can be a child
- ✅ Easy to manage in Alfresco Share UI

**Cons:**
- ❌ No validation (editors could add wrong content)
- ❌ No dropdown for component type selection
- ❌ Requires convention-based naming

**Example Structure:**
```
Home Page/
├── Hero/
│   └── buildingC.png
├── Feature 1/
│   ├── content.html
│   └── image.jpg
├── Feature 2/
│   └── content.html
└── Feature 3/
    ├── content.html
    └── gallery/
        ├── img1.jpg
        └── img2.jpg
```

---

### **Option B: Deploy Custom `cms:item` Type** ⏭️ **Future Enhancement**

**How it would work:**
- Component = `cms:item` node with typed properties
- Title → `cms:title` property
- Body → `cms:body` property (inline HTML)
- Image → `cms:imageRef` (node reference)
- Component type → `cms:componentType` (hero/section/feature)
- Placement → `cms:placeable` aspect (sitePath, region, slot)
- Publishing → `cms:publishable` aspect (status, dates)

**Pros:**
- ✅ Type safety and validation
- ✅ Dropdown selection for component types
- ✅ Query by type: `TYPE:'cms:item' AND cms:componentType:'hero'`
- ✅ Workflow integration via aspects
- ✅ Clear schema for editors

**Cons:**
- ❌ Requires AMP deployment
- ❌ More complex for editors initially
- ❌ Harder to evolve once deployed
- ❌ Need to migrate existing content

---

## 💡 Recommended Approach: **Hybrid Strategy**

### **Phase 1: NOW** (Folder-Based with Conventions)
1. ✅ Keep current `cm:folder` structure
2. ✅ Use workbench to document conventions
3. ✅ Build components that work with folders
4. ✅ Create editor documentation

**Convention Example:**
```
Component Folder Structure:
- Folder name = Component type (Hero, Section, Feature, etc.)
- cm:title = Component title/heading
- content.html = Body content (if exists)
- *.jpg, *.png = Images
- metadata.json = Additional component settings (optional)
```

### **Phase 2: LATER** (Custom Type Migration)
1. Deploy `mtz.xml` to Alfresco
2. Create migration script (folder → `cms:item`)
3. Build admin UI for typed content
4. Gradually migrate high-value components

---

## 📦 What Alfresco Has Today

### **50 Built-in Content Types**
Including:
- `cm:content` - Base content type
- `cm:folder` - Base folder type
- `cm:link` - Link objects
- `bpm:workflowTask` - Workflow tasks
- Many more...

### **50 Built-in Aspects**
Including:
- `cm:titled` - Adds title property ✅ (you're using this!)
- `cm:auditable` - Audit trail ✅ (you're using this!)
- `cm:versionable` - Version history
- `cm:taggable` - Tagging support
- `cm:geographic` - Location data
- Many more...

**Key Insight:** You can leverage existing aspects instead of creating everything custom!

---

## 🎯 Immediate Action Items

### **1. Document Current Conventions**
Create `CONTENT_CONVENTIONS.md`:
```markdown
# Hero Component
- Folder name: "Hero"
- cm:title: Hero heading text
- Children: One large image (1920x1080 recommended)

# Feature Component
- Folder name: "Feature 1", "Feature 2", etc.
- cm:title: Feature heading
- Children:
  - content.html: Feature body (optional)
  - feature-image.*: Feature image (optional)
```

### **2. Enhance Current Resolvers**
Add better error handling and validation:
```clojure
(defn validate-hero-structure [node children]
  "Ensure hero has expected structure"
  (when-not (= 1 (count children))
    (log/warn "Hero should have exactly 1 image child"))
  ...)
```

### **3. Build Workbench UI**
- Panel 1: Current folder structure from Alfresco
- Panel 2: Convention documentation
- Panel 3: Component preview

### **4. Create Content Editor Guide**
Simple docs for pastor/staff:
- How to add new component folder
- Naming conventions
- Image size recommendations
- Where to put HTML content

---

## 🚀 Next Steps for Workbench

1. ✅ **DONE:** Fetch live Alfresco model
2. ✅ **DONE:** Analyze current node structure
3. **TODO:** Build workbench UI showing:
   - Current Alfresco structure
   - Proposed conventions
   - Side-by-side comparison
4. **TODO:** Add "Convention Validator"
   - Scan existing nodes
   - Report violations
   - Suggest fixes

---

## 📝 Summary

**Current State:**
- Simple folder-based structure
- Working with existing Alfresco types
- No custom model deployed

**Recommendation:**
- **Stay with folder-based approach** for now
- Document clear conventions
- Build workbench to help manage conventions
- Defer custom type deployment until proven necessary

**Why This Works:**
- Simpler for content editors
- Faster to iterate
- Less deployment complexity
- Can always add custom types later

---

## Files Generated

- `generated-model/alfresco-live-model.edn` - Full Alfresco model data
- `workbench/bb/fetch-alfresco-model.clj` - Fetcher script
- This analysis document

**Run fetcher again:** `bb workbench/bb/fetch-alfresco-model.clj`
