# 🏗️ Alfresco Content Model Extensions for Component System

## 📋 **Planned Aspects for Component Selection**

### **1. Component Selection Aspect** (`mtz:componentSelection`)

**Purpose**: Allow users to select which component type to use for folders

**Properties**:
```xml
<aspect name="mtz:componentSelection">
  <title>Component Selection</title>
  <properties>
    <property name="mtz:componentType">
      <title>Component Type</title>
      <type>d:text</type>
      <default>auto</default>
      <constraints>
        <constraint name="mtz:componentTypeOptions" type="LIST">
          <parameter name="allowedValues">
            <list>
              <value>auto</value>
              <value>hero</value>
              <value>feature-with-image</value>
              <value>feature-text-only</value>
              <value>feature-card</value>
              <value>card</value>
              <value>text-block</value>
            </list>
          </parameter>
        </constraint>
      </constraints>
    </property>
    
    <property name="mtz:componentVariant">
      <title>Component Variant</title>
      <type>d:text</type>
      <default>default</default>
    </property>
    
    <property name="mtz:displayOrder">
      <title>Display Order</title>
      <type>d:int</type>
      <default>100</default>
    </property>
  </properties>
</aspect>
```

### **2. Page Layout Aspect** (`mtz:pageLayout`)

**Purpose**: Allow users to select page layout for folders

**Properties**:
```xml
<aspect name="mtz:pageLayout">
  <title>Page Layout Selection</title>
  <properties>
    <property name="mtz:layoutType">
      <title>Layout Type</title>
      <type>d:text</type>
      <default>auto</default>
      <constraints>
        <constraint name="mtz:layoutOptions" type="LIST">
          <parameter name="allowedValues">
            <list>
              <value>auto</value>
              <value>hero-features-layout</value>
              <value>simple-content-layout</value>
              <value>cards-grid-layout</value>
              <value>two-column-layout</value>
              <value>full-width-layout</value>
            </list>
          </parameter>
        </constraint>
      </constraints>
    </property>
    
    <property name="mtz:maxComponents">
      <title>Max Components</title>
      <type>d:int</type>
      <default>10</default>
    </property>
  </properties>
</aspect>
```

### **3. Content Metadata Aspect** (`mtz:contentMetadata`)

**Purpose**: Enhanced metadata for better component rendering

**Properties**:
```xml
<aspect name="mtz:contentMetadata">
  <title>Content Metadata</title>
  <properties>
    <property name="mtz:subtitle">
      <title>Subtitle</title>
      <type>d:text</type>
    </property>
    
    <property name="mtz:callToActionText">
      <title>Call to Action Text</title>
      <type>d:text</type>
      <default>Learn More</default>
    </property>
    
    <property name="mtz:callToActionUrl">
      <title>Call to Action URL</title>
      <type>d:text</type>
    </property>
    
    <property name="mtz:backgroundColor">
      <title>Background Color</title>
      <type>d:text</type>
      <default>white</default>
    </property>
    
    <property name="mtz:isPublished">
      <title>Published</title>
      <type>d:boolean</type>
      <default>true</default>
    </property>
  </properties>
</aspect>
```

## 🔄 **Integration Plan**

### **Phase 1: Manual Component Selection**
- ✅ Component resolvers created
- ✅ HyperUI templates created  
- ✅ Layout system created
- 🚧 Test with existing content

### **Phase 2: Alfresco Aspects**
- 📋 Deploy content model extensions
- 📋 Update Alfresco Share forms
- 📋 Create aspect-aware resolvers

### **Phase 3: User Interface**
- 📋 Component selection UI in Alfresco Share
- 📋 Layout preview system
- 📋 Drag-and-drop component ordering

## 🎯 **Current Status**

**Working Now**:
- Hero component renders from Alfresco content
- Feature components with text + images
- HyperUI-based responsive templates
- Layout composition system

**Next Steps**:
1. Test component system with real data
2. Create aspect-aware resolvers
3. Deploy Alfresco content model

## 🧪 **Test Commands**

```clojure
;; Test hero component
(user/test-hero-component)

;; Test individual features  
(user/test-feature-component 1)  ; Feature 1 (Welcome)
(user/test-feature-component 2)  ; Feature 2 (Blood Drive)

;; Test complete home page
(user/test-home-components)
```

## 🎨 **Available Components**

1. **Hero Components**:
   - `hero-with-image` - Full-screen hero with background image
   - `hero-text-only` - Text-focused hero with gradient

2. **Feature Components**:
   - `feature-with-image` - Side-by-side content and image
   - `feature-text-only` - Centered text content
   - `feature-card` - Card with hover effects

3. **Layouts**:
   - `hero-features-layout` - Hero + feature grid
   - `simple-content-layout` - Content + sidebar
   - `cards-grid-layout` - Grid of cards

The foundation is ready for user-selectable components and layouts! 🚀