# Malli Validation Analysis

**Date:** 2025-10-03
**Status:** ⚠️ PARTIALLY IMPLEMENTED - NOT INTEGRATED

## Current State

### What Exists

1. **Schema Definitions** (`src/mtz_cms/validation/schemas.clj`):
   - ✅ Alfresco node schemas
   - ✅ Pathom component schemas (hero, feature)
   - ✅ Page composition schemas
   - ✅ HTMX response schemas
   - ✅ Schema registry with all schemas
   - ✅ Validation functions (`validate`, `validate!`, `transform`)
   - ✅ Helper functions for debugging

2. **Middleware** (`src/mtz_cms/validation/middleware.clj`):
   - ✅ Alfresco response validation wrapper
   - ✅ Pathom resolver validation decorator
   - ✅ HTMX response validation
   - ✅ Component pipeline validation
   - ⚠️ Schema refresh stub (not implemented)

3. **Content Model Schemas** (`src/mtz_cms/validation/content_model_schemas.clj`):
   - ⚠️ **AUTO-GENERATED FILE - EMPTY**
   - Schema registry is empty: `{:types {} :aspects {}}`
   - Needs to be regenerated from Alfresco model

4. **Generation Scripts**:
   - ✅ `workbench/bb/xml-to-malli.clj` - Convert Alfresco XML model to Malli
   - ✅ Babashka scripts for model sync

### Where It's Used

**VERY LIMITED USAGE:**

1. **routes/api.clj** - Only 1 place:
   ```clojure
   (validation/validate-htmx-response response "hero-component")
   ```
   - Only validates HTMX responses
   - No input validation
   - No Pathom validation
   - No Alfresco validation

2. **NOT USED:**
   - ❌ Alfresco resolvers (`alfresco/resolvers.clj`) - NO VALIDATION
   - ❌ Pathom resolvers (`pathom/resolvers.clj`) - NO VALIDATION
   - ❌ Navigation menu building - NO VALIDATION
   - ❌ Component rendering - NO VALIDATION
   - ❌ Static content loading - NO VALIDATION

## Problems

### 1. **Content Model Schemas Are Empty**
The auto-generated file has empty registries:
```clojure
(def schema-registry
  "Complete schema registry"
  {:types {}
   :aspects {}})
```

Need to run: `bb workbench/bb/xml-to-malli.clj`

### 2. **No Integration with Pathom**
Pathom resolvers don't use validation at all. The `validate-pathom-resolver` middleware exists but is never used.

### 3. **No Pipeline Validation**
Data flows through the system without validation:
```
Alfresco → Pathom → Components → HTMX → HTML
   ❌         ❌         ❌          ✅
```

Only the final HTMX response is validated (and only in one handler).

### 4. **Transformation Not Used**
Malli can do transformations (coercion, defaults, etc.) but we don't use it:
- No string trimming
- No date parsing
- No HTML sanitization
- No default values

## Your Original Intent

From your message:
> "we have some Malli stuff but it is not integrated - I am thinking the validation may be useful to insure consistency from start to finish - Maybe we put in an inconsistent selection in Alfresco - malli can do some transformations for us"

**You wanted:**
1. ✅ Validation throughout pipeline
2. ✅ Data transformation/coercion
3. ✅ Catch inconsistent Alfresco data early
4. ❌ **NONE OF THIS IS ACTUALLY HAPPENING**

## Options

### Option 1: Remove Malli Entirely
**Pros:**
- Reduces complexity
- One less dependency
- Less code to maintain
- We're not using it anyway

**Cons:**
- No validation safety net
- Hard to debug bad Alfresco data
- No transformation capabilities

### Option 2: Fully Integrate Malli
**Pros:**
- Catch errors early in pipeline
- Transform/coerce data automatically
- Self-documenting schemas
- Great for debugging

**Cons:**
- More work to integrate
- Performance overhead (minimal)
- Need to maintain schemas

**Integration Plan:**
1. Regenerate content model schemas from Alfresco
2. Add validation to Pathom resolvers
3. Add validation to Alfresco client
4. Add transformations (trim strings, parse dates, etc.)
5. Use schemas for development debugging

### Option 3: Minimal Validation (Hybrid)
**Pros:**
- Validate only critical paths
- Less overhead
- Catch major issues

**Cons:**
- Partial coverage

**Critical Points to Validate:**
1. ✅ Alfresco API responses (check for required fields)
2. ✅ Pathom component outputs (ensure components have required data)
3. ✅ Navigation menu structure (prevent broken menus)
4. ❌ HTML responses (already doing this)

## Recommendation

**I recommend Option 2 - Fully Integrate**, because:

1. **You have the infrastructure** - schemas are written, just not used
2. **Alfresco data is unpredictable** - content editors can create inconsistent data
3. **Transformations are useful** - trim HTML, normalize text, set defaults
4. **Self-documenting** - schemas show what data structure is expected
5. **Debugging** - when something breaks, schemas help find why

**Quick wins:**
- Wrap Pathom resolvers with validation
- Validate Alfresco responses
- Add transformers for HTML cleaning
- Use schemas in REPL for development

## Next Steps (If Integrating)

1. **Regenerate content model schemas:**
   ```bash
   bb workbench/bb/xml-to-malli.clj
   ```

2. **Add validation to Alfresco client** (in `alfresco/client.clj`):
   ```clojure
   (defn get-node [ctx node-id]
     (let [response (http/get ...)
           validated (validation/validate-alfresco-response response "get-node")]
       validated))
   ```

3. **Add validation to Pathom resolvers** (in `alfresco/resolvers.clj`):
   ```clojure
   (defresolver hero-component
     [ctx input]
     {::pco/input [:hero/node-id]
      ::pco/output [:hero/title :hero/content :hero/image]}
     (let [_ (schemas/validate! :hero/input input)
           result (fetch-and-process-hero ctx input)
           _ (schemas/validate! :hero/output result)]
       result))
   ```

4. **Add transformations** (string trimming, HTML cleaning):
   ```clojure
   (def hero-output-transformer
     (mt/transformer
       mt/strip-extra-keys-transformer
       mt/string-transformer))

   (schemas/transform :hero/output raw-data hero-output-transformer)
   ```

## Decision Needed

**Question for you:** What do you want to do?

- **A) Remove Malli** - Clean up, remove unused code
- **B) Fully integrate Malli** - Add validation throughout pipeline
- **C) Minimal validation** - Just validate critical paths
- **D) Something else** - Your idea?
