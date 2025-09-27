# 🤖 AI Context: Mount Zion CMS Development

## 📋 **Project Overview**

**Goal**: Create a clean, modern CMS for Mount Zion United Church of Christ using Alfresco as the content repository.

**Architecture**: Alfresco → Pathom → HTMX → Tailwind CSS

---

## 🎯 **Current Status: WORKING**

### **✅ What's Successfully Implemented:**

1. **Clean Repository Structure** (`mtz-cms-new/`)
   - Extracted from complex Yakread codebase
   - Minimal dependencies (no Biff/Yakread complexity)
   - Professional Tailwind CSS styling
   - HTMX interactivity ready

2. **Infrastructure Working:**
   - **SSH Tunnel**: `ssh -L 8080:localhost:8080 -N -f tmb@trust` (ACTIVE)
   - **Alfresco API**: Responding on localhost:8080 (TESTED ✅)
   - **Server Framework**: Ring/Jetty + Reitit routing (TESTED ✅)
   - **UI Components**: Beautiful Tailwind CSS components (TESTED ✅)

3. **Core Components:**
   ```
   mtz-cms-new/
   ├── deps.edn                    # Minimal dependencies 
   ├── src/mtz_cms/
   │   ├── core.clj               # Main server (✅ Working)
   │   ├── routes/main.clj        # HTTP routes (✅ Working)
   │   ├── pathom/resolvers.clj   # Data resolution (🔄 Mock working)
   │   ├── alfresco/client.clj    # Alfresco API client (✅ Working)
   │   ├── ui/pages.clj           # Beautiful Tailwind pages (✅ Working)
   │   └── ui/components.clj      # Tailwind components (✅ Working)
   └── dev/user.clj              # REPL helpers (✅ Working)
   ```

### **🧪 Test Results:**
```bash
# ALL WORKING:
(user/test-alfresco)  # ✅ {:success true, :message "Connected to Alfresco successfully!"}
(user/test-pathom)    # ✅ Mock responses working
clojure -M:dev        # ✅ REPL loads correctly
```

---

## 🔧 **How We Got Here**

### **Phase 1: Analysis**
- Started with complex Yakread codebase (too many dependencies)
- Identified we only needed: Alfresco + Pathom + HTMX + Tailwind
- SSH tunnel to Alfresco server already working

### **Phase 2: Clean Extraction**
- Created new `mtz-cms-new` directory
- Extracted working components:
  - Alfresco client (`com.yakread.alfresco.client` → `mtz-cms.alfresco.client`)
  - Basic Pathom setup (simplified)
  - Clean routing structure
- **Dropped**: Biff, Yakread complexity, XTDB, MinIO

### **Phase 3: Tailwind Integration**
- Added Tailwind CSS CDN for beautiful styling
- Created professional UI components
- Built responsive page layouts
- Added HTMX loading indicators

### **Phase 4: Testing & Validation**
- Fixed syntax errors in page templates
- Verified all core components load
- Confirmed Alfresco connection working
- Set up proper REPL workflow

---

## 🎯 **NEXT PHASE: Get Pathom Working with Alfresco**

### **Current Issue:**
Pathom is using **mock responses**. Need to connect it to **real Alfresco data**.

### **Immediate Tasks:**

#### **1. Fix Pathom 3 Setup**
**Problem**: Currently using mock implementation
```clojure
;; Current mock in src/mtz_cms/pathom/resolvers.clj
(defn query [ctx eql-query]
  ;; Returns mock data instead of real Pathom processing
```

**Solution**: Implement proper Pathom 3 processor
```clojure
;; Need to fix this pattern:
(def pathom-processor
  (p.eql/boundary-interface 
    (pco/registry all-resolvers)))
```

#### **2. Create Alfresco → Pathom Resolvers**
**Files to update:**
- `src/mtz_cms/pathom/resolvers.clj`

**Resolvers needed:**
```clojure
;; Page content resolver
(defresolver page-content-resolver
  [{:page/keys [path]}]
  {::pco/input [:page/path]
   ::pco/output [:page/title :page/content :page/modified]}
  (alfresco/get-page-content ctx path))

;; Node children resolver  
(defresolver node-children-resolver
  [{:alfresco/keys [node-id]}]
  {::pco/input [:alfresco/node-id]
   ::pco/output [:alfresco/children]}
  (alfresco/get-node-children ctx node-id))
```

#### **3. Map Church Pages to Alfresco Nodes**
**Need**: Configuration mapping church pages to Alfresco node IDs
```clojure
;; In src/mtz_cms/config/core.clj
(def page-node-mapping
  {:home "some-alfresco-node-id"
   :about "another-node-id"
   :worship "worship-node-id"
   :events "events-node-id"})
```

#### **4. Update Routes to Use Real Pathom**
**Files to update:**
- `src/mtz_cms/routes/main.clj`

**Current**: Mock data
**Target**: Real Pathom queries
```clojure
(defn home-handler [request]
  (let [result (pathom/query {} [{[:page/path "/home"] 
                                  [:page/title :page/content]}])]
    (html-response (pages/home-page result))))
```

### **Configuration Needed:**

#### **Alfresco Node Discovery**
```bash
# Need to discover the actual node structure:
curl -u admin:admin "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-/children"
```

#### **Content Structure Mapping**
Map church sections to Alfresco folders:
- `/Company Home/Sites/church-site/documentLibrary/pages/`
- `/Company Home/Sites/church-site/documentLibrary/events/`
- etc.

---

## 🚀 **How to Continue Development**

### **Start Working:**
```bash
cd /Users/tombrooke/Code/trust-server/mtzion/yakread/mtz-cms-new

# Start REPL with dev environment
clojure -M:dev

# In REPL:
(require 'user)
(user/test-alfresco)   # Verify connection
(user/start)           # Start server (use port 3001 if 3000 busy)
```

### **Visit Working Pages:**
- `http://localhost:3001/` - Beautiful home page (Tailwind)
- `http://localhost:3001/demo` - Shows current status + architecture
- `http://localhost:3001/about` - Professional about page

### **Development Workflow:**
```clojure
;; Test changes
(user/restart)         # Reload code + restart server

;; Test components individually  
(user/test-alfresco)   # Test Alfresco connection
(user/test-pathom)     # Test Pathom (currently mock)
```

---

## 🎯 **Success Criteria**

### **Phase 1 Complete ✅:**
- [x] Clean architecture extracted
- [x] Tailwind CSS beautiful UI
- [x] Alfresco connection working
- [x] HTMX ready for interactivity
- [x] REPL development workflow

### **Phase 2 Targets:**
- [ ] Real Pathom 3 resolvers working
- [ ] Church pages pulling content from Alfresco
- [ ] Dynamic content rendering
- [ ] Page editing workflow
- [ ] Calendar integration

---

## 📁 **Key Files for Next Developer**

### **Must Understand:**
1. **`src/mtz_cms/pathom/resolvers.clj`** - Fix Pathom setup here
2. **`src/mtz_cms/alfresco/client.clj`** - Alfresco API working
3. **`src/mtz_cms/routes/main.clj`** - Connect routes to real data
4. **`dev/user.clj`** - Development helpers

### **Working Examples:**
- **Alfresco Client**: `(alfresco/test-connection {})` works perfectly
- **UI Components**: All Tailwind components render beautifully
- **Server Setup**: Ring/Jetty + Reitit routing solid

---

## 🚨 **Important Notes**

### **SSH Tunnel Must Be Active:**
```bash
# Check if running:
ps aux | grep "ssh.*trust"

# Start if needed:
ssh -L 8080:localhost:8080 -N -f tmb@trust
```

### **Port Management:**
- Port 3000 often busy (old Yakread server)
- Use port 3001 for development: `(core/start-server 3001)`

### **Pathom Version:**
- Using Pathom 3 (`com.wsscode/pathom3 2023.08.22-alpha`)
- Syntax different from Pathom 2 examples online

---

## 🎯 **Immediate Next Steps**

1. **Fix Pathom 3 setup** - Replace mock with real processor
2. **Discover Alfresco node structure** - Map church content
3. **Create page-to-node mapping** - Configuration file
4. **Test with real content** - Pull one page from Alfresco
5. **Verify end-to-end flow** - Browser → Pathom → Alfresco → HTML

**The foundation is solid. Ready to connect Pathom to Alfresco! 🚀**