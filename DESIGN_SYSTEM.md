# Mount Zion CMS Design System

Complete guide to using the Mount Zion CMS design system for consistent, maintainable styling.

## Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [Development vs Production](#development-vs-production)
- [Design Tokens](#design-tokens)
- [Component Builders](#component-builders)
- [Tailwind Build System](#tailwind-build-system)
- [Living Style Guide](#living-style-guide)
- [Examples](#examples)
- [Migration Guide](#migration-guide)

---

## Overview

The Mount Zion CMS design system provides:

1. **Centralized Design Tokens** - Colors, typography, spacing defined in one place
2. **Component Class Builders** - Functions to generate consistent component styles
3. **Living Style Guide** - Interactive documentation at `/admin/style-guide`
4. **Tailwind Configuration** - Custom config matching design tokens
5. **Production Build** - Optimized, purged CSS for production

**Philosophy:**
- Single source of truth for design decisions
- Semantic naming (use purpose, not appearance)
- Composable, functional approach
- Works with existing Tailwind classes

---

## Quick Start

### Using the Design System

```clojure
(ns my-namespace
  (:require [mtz-cms.ui.design-system :as ds]))

;; Generate component classes
(ds/button :primary)
;; => "font-medium transition-colors text-center inline-block px-6 py-4 bg-blue-600 text-white hover:bg-blue-700 rounded-md"

;; Use semantic colors
[:div {:class (ds/classes [(ds/bg :white)
                           (ds/shadow :md)
                           (ds/rounded :lg)
                           (ds/p :lg)])}
 "Card content"]

;; Build custom components
[:button {:class (ds/classes [(ds/button :primary)
                              (when loading? "opacity-50")])}
 "Submit"]
```

### Key Functions

```clojure
;; Colors (coordinated OKLCH mint palette)
(ds/text :primary)            ; => "text-mint-primary"
(ds/bg :primary)              ; => "bg-mint-primary"
(ds/bg :bg-header)            ; => "bg-mint-light"
(ds/hover-bg :primary-dark)   ; => "hover:bg-mint-dark"
(ds/bg :error-bg)             ; => "bg-red-50"

;; Typography
(ds/heading 1)                ; => "text-4xl font-extrabold text-gray-900"
(ds/text-size :lg)            ; => "text-lg"
(ds/font-weight :bold)        ; => "font-bold"
(ds/font-family :garamond)    ; => "font-garamond"
(ds/font-family :menu)        ; => "font-menu"

;; Spacing
(ds/px :lg)                ; => "px-6"
(ds/py :md)                ; => "py-4"
(ds/gap :md)               ; => "gap-4"

;; Layout
(ds/container :7xl)        ; => "max-w-7xl mx-auto px-4"

;; Components
(ds/button :secondary {:size :sm})
(ds/card {:hover? true :shadow :lg})
(ds/alert :success)
(ds/input {:error? true})
```

---

## Development vs Production

### Development Mode (Default)

Uses Tailwind CDN - fast, no build step:

```clojure
;; In ui/styles.clj
(def *use-cdn* true) ; Default

;; Automatic in templates
(styles/tailwind-styles) ; Uses CDN
```

**Pros:**
- No build step required
- Instant development
- Full Tailwind available

**Cons:**
- Larger file size (~3MB)
- No custom configuration
- Slower page loads

### Production Mode

Uses compiled, purged CSS:

```bash
# Build CSS once
npm install
npm run build:css

# Or watch for changes during development
npm run watch:css
```

```clojure
;; Enable production mode
;; Option 1: System property
java -Dtailwind.cdn=false -jar app.jar

;; Option 2: Binding in code
(binding [styles/*use-cdn* false]
  (styles/tailwind-styles)) ; Uses compiled CSS
```

**Pros:**
- Small file size (~10-50KB)
- Custom design tokens
- Faster page loads
- Only includes used classes

**Cons:**
- Requires build step
- Need to rebuild after adding new classes

---

## Design Tokens

### Semantic Colors

Use semantic names instead of raw colors:

```clojure
;; ❌ Don't do this
"text-blue-600"

;; ✅ Do this
(ds/text :primary)

;; ✅ Or this
(ds/text :text-primary)
```

**Available Semantic Colors:**

**Brand (OKLCH Mint Palette):**
- `:primary` → `:mint-primary` - Main brand mint/teal (hue 166.113°)
- `:primary-light` → `:mint-accent` - Lighter mint for highlights (hue 166.113°)
- `:primary-lighter` → `:mint-light` - Very light cyan backgrounds (hue 180.72°)
- `:primary-dark` → `:mint-dark` - Darker mint for hover states
- `:secondary` - Gray for secondary elements
- `:secondary-warm` → `:warm-accent` - Complementary warm accent

**Status:**
- `:success` - Green for success states
- `:error` - Red for errors
- `:warning` - Yellow for warnings
- `:info` → `:mint-primary` - Coordinated mint for informational (matches brand)

**Text:**
- `:text-primary` - Main content text (gray-900)
- `:text-secondary` - Secondary text (gray-600)
- `:text-muted` - Subtle text (gray-500)
- `:text-on-dark` - Text on dark backgrounds (white)

**Background:**
- `:bg-page` - Page background (white)
- `:bg-card` - Card background (white)
- `:bg-hover` → `:mint-light` - Subtle mint hover (coordinated)
- `:bg-header` → `:mint-light` - Navigation header (custom OKLCH)

**Border:**
- `:border-default` - Standard borders (gray-200)

### Custom Fonts

Mount Zion uses custom Google Fonts for a refined, professional look:

```clojure
;; Font families
(ds/font-family :garamond)  ; EB Garamond - elegant serif for headings
(ds/font-family :menu)      ; IBM Plex Sans - uppercase menu font
(ds/font-family :ibm-plex)  ; IBM Plex Sans - clean sans-serif
;; Source Serif 4 is the default body font (applied globally)

;; Example usage
[:h1 {:class (ds/classes [(ds/heading 1)
                          (ds/font-family :garamond)])}
 "Elegant Heading"]

[:nav {:class (ds/font-family :menu)}
 "HOME • ABOUT • SERVICES"]
```

**Font Guidelines:**
- **EB Garamond**: Use for elegant headings and decorative text
- **IBM Plex Sans (Menu)**: Navigation and menu items (auto-uppercase)
- **IBM Plex Sans**: UI elements and modern headings
- **Source Serif 4**: Body text (default, no function call needed)

### Typography Scale

```clojure
;; Sizes
(ds/text-size :xs)   ; 12px
(ds/text-size :sm)   ; 14px
(ds/text-size :base) ; 16px
(ds/text-size :lg)   ; 18px
(ds/text-size :xl)   ; 20px
(ds/text-size :2xl)  ; 24px
(ds/text-size :3xl)  ; 30px
(ds/text-size :4xl)  ; 36px
(ds/text-size :5xl)  ; 48px
(ds/text-size :6xl)  ; 60px

;; Weights
(ds/font-weight :normal)    ; 400
(ds/font-weight :medium)    ; 500
(ds/font-weight :semibold)  ; 600
(ds/font-weight :bold)      ; 700
(ds/font-weight :extrabold) ; 800

;; Complete heading styles
(ds/heading 1)  ; H1 with size and weight
(ds/heading 2)  ; H2 with size and weight
;; ... etc
```

### Spacing Scale

```clojure
:xs   ; 4px
:sm   ; 8px
:md   ; 16px
:lg   ; 24px
:xl   ; 32px
:2xl  ; 48px
:3xl  ; 64px
:4xl  ; 96px

;; Usage
(ds/px :md)  ; Horizontal padding
(ds/py :lg)  ; Vertical padding
(ds/p :md)   ; All sides padding
(ds/mb :xl)  ; Bottom margin
(ds/gap :md) ; Gap in flex/grid
```

---

## Component Builders

### Button

```clojure
;; Basic variants
(ds/button :primary)    ; Mint primary color
(ds/button :secondary)  ; White with mint border
(ds/button :warm)       ; Warm complementary accent
(ds/button :danger)     ; Red for destructive actions
(ds/button :link)       ; Text-only link style

;; With options
(ds/button :primary {:size :sm})
(ds/button :secondary {:size :lg :disabled? true})
(ds/button :danger {:class "extra-classes"})

;; In Hiccup
[:button {:class (ds/button :primary)}
 "Click Me"]

[:a {:href "/about"
     :class (ds/button :secondary)}
 "Learn More"]
```

### Card

```clojure
;; Basic card
(ds/card)
;; => "bg-white rounded-lg shadow-md overflow-hidden"

;; With options
(ds/card {:hover? true})
(ds/card {:shadow :lg :border? true})
(ds/card {:padding :xl :class "custom-class"})

;; In Hiccup
[:div {:class (ds/card {:hover? true})}
 [:h3 "Card Title"]
 [:p "Card content"]]
```

### Alert/Message

```clojure
;; Alert variants
(ds/alert :success)
(ds/alert :error)
(ds/alert :warning)
(ds/alert :info)

;; In Hiccup
[:div {:class (ds/alert :success)}
 [:p "✓ Changes saved successfully!"]]

[:div {:class (ds/alert :error)}
 [:p "✗ An error occurred."]]
```

### Input

```clojure
;; Standard input
(ds/input)

;; With states
(ds/input {:error? true})
(ds/input {:disabled? true})

;; In Hiccup
[:input {:type "text"
         :placeholder "Enter your name"
         :class (ds/input)}]

[:input {:type "email"
         :class (ds/input {:error? (not valid?)})
         :aria-invalid (not valid?)}]
```

### Combining Classes

```clojure
;; Use `classes` to combine multiple utilities
(ds/classes [(ds/bg :white)
             (ds/shadow :md)
             (ds/p :lg)
             (ds/rounded :lg)
             (ds/mb :xl)])

;; With conditional classes
(ds/classes [(ds/text :primary)
             (when error? (ds/text :error))
             (when disabled? "opacity-50 cursor-not-allowed")])

;; nil values are automatically filtered
```

---

## Tailwind Build System

### Setup

```bash
# Install dependencies
npm install

# Build CSS for production (one-time)
npm run build:css

# Watch for changes during development
npm run watch:css
```

### Files

```
mtz-cms/
├── package.json              # npm configuration
├── tailwind.config.js        # Tailwind config with custom tokens
├── src/css/
│   └── main.css             # Source CSS with @tailwind directives
└── resources/public/css/
    └── styles.css           # Generated output (gitignored)
```

### Customization

Edit `tailwind.config.js` to customize:

```js
module.exports = {
  theme: {
    extend: {
      colors: {
        'mtz-primary': {
          DEFAULT: '#2563eb',
          light: '#dbeafe',
          // ...
        },
      },
    },
  },
}
```

Any changes here should also be reflected in `design-system.clj` for consistency.

### Safelist

Classes generated dynamically in Clojure need to be safelisted:

```js
// tailwind.config.js
safelist: [
  'text-blue-600',
  'bg-white',
  'hover:shadow-lg',
  // ...
]
```

---

## Living Style Guide

Visit `/admin/style-guide` to see:

- All design tokens with visual examples
- Color swatches with usage guidelines
- Typography scale
- Spacing examples
- Interactive component examples
- Code snippets for every component

The style guide is automatically generated from the design system, ensuring it's always up-to-date.

---

## Examples

### Building a Custom Component

```clojure
(ns my-namespace
  (:require [mtz-cms.ui.design-system :as ds]))

(defn custom-panel
  "A reusable panel component with consistent styling"
  [{:keys [title content variant]
    :or {variant :default}}]
  (let [header-bg (case variant
                    :primary (ds/bg :primary)
                    :success (ds/bg :success-bg)
                    :default (ds/bg :bg-card))]
    [:div {:class (ds/card {:shadow :lg})}
     ;; Header
     [:div {:class (ds/classes [header-bg
                                (ds/px :lg)
                                (ds/py :md)
                                (ds/border-color :border-default)
                                "border-b"])}
      [:h3 {:class (ds/heading 3)} title]]

     ;; Content
     [:div {:class (ds/classes [(ds/p :lg)])}
      content]]))

;; Usage
(custom-panel {:title "Welcome"
               :content [:p "Hello world!"]
               :variant :primary})
```

### Refactoring Existing Code

**Before:**
```clojure
[:button {:class "px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 font-medium"}
 "Submit"]
```

**After:**
```clojure
[:button {:class (ds/button :primary)}
 "Submit"]
```

**Before:**
```clojure
[:div {:class "bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md"}
 "Error message"]
```

**After:**
```clojure
[:div {:class (ds/alert :error)}
 "Error message"]
```

### Responsive Design

Mix design system utilities with Tailwind responsive classes:

```clojure
[:div {:class (ds/classes [(ds/container :7xl)
                           (ds/py :xl)
                           "grid"
                           "grid-cols-1"
                           "md:grid-cols-2"
                           "lg:grid-cols-3"
                           (ds/gap :lg)])}
 ;; Grid items...
 ]
```

---

## Migration Guide

### Step 1: Identify Patterns

Look for repeated class strings in your components:

```clojure
;; Pattern: Button styles
"px-6 py-3 bg-blue-600 text-white hover:bg-blue-700 rounded-md"

;; Pattern: Card styles
"bg-white rounded-lg shadow-md overflow-hidden"
```

### Step 2: Replace with Design System

```clojure
;; Add require
(ns my-namespace
  (:require [mtz-cms.ui.design-system :as ds]))

;; Replace patterns
(ds/button :primary)
(ds/card)
```

### Step 3: Handle Custom Cases

For unique cases, combine design system with custom classes:

```clojure
[:div {:class (ds/classes [(ds/card {:hover? true})
                           "custom-animation"
                           "special-border"])}
 ;; content
 ]
```

### Step 4: Test

1. Visual regression testing
2. Check responsive behavior
3. Verify hover/active states
4. Test in different browsers

---

## Best Practices

### DO

✅ Use semantic color names
```clojure
(ds/text :primary)
(ds/bg :error-bg)
```

✅ Use component builders
```clojure
(ds/button :primary)
(ds/card {:hover? true})
```

✅ Combine with `classes` for complex components
```clojure
(ds/classes [(ds/bg :white) (ds/p :lg) "custom-class"])
```

✅ Use the style guide as reference
```
Visit /admin/style-guide
```

### DON'T

❌ Hardcode color values
```clojure
"text-blue-600" ; Use (ds/text :primary) instead
```

❌ Duplicate class patterns
```clojure
;; Multiple buttons with same classes - use design system
[:button {:class "px-6 py-3 bg-blue-600..."} "One"]
[:button {:class "px-6 py-3 bg-blue-600..."} "Two"]
```

❌ Mix raw Tailwind colors with semantic ones
```clojure
;; Inconsistent
(ds/classes [(ds/bg :primary) "text-blue-600"])

;; Better
(ds/classes [(ds/bg :primary) (ds/text :text-on-primary)])
```

---

## Troubleshooting

### Classes not appearing in production

**Problem:** Classes work in development (CDN) but disappear in production build.

**Solution:** Add dynamic classes to safelist in `tailwind.config.js`:

```js
safelist: [
  'your-dynamic-class',
]
```

### Styling looks different between dev and prod

**Problem:** CDN and compiled CSS have different behaviors.

**Cause:** Custom configuration in `tailwind.config.js` not applied to CDN.

**Solution:** Always test with compiled CSS before deploying:

```bash
npm run build:css
# Set use-cdn to false and test
```

### Design system function not found

**Problem:** `Unable to resolve symbol: ds/button`

**Solution:** Check namespace require:

```clojure
(ns my-namespace
  (:require [mtz-cms.ui.design-system :as ds])) ; Add this
```

---

## Resources

- **Living Style Guide**: `/admin/style-guide`
- **Design System Source**: `src/mtz_cms/ui/design_system.clj`
- **Tailwind Config**: `tailwind.config.js`
- **Source CSS**: `src/css/main.css`
- **Tailwind Docs**: https://tailwindcss.com/docs

---

## Support

For questions or issues:

1. Check the [Living Style Guide](/admin/style-guide)
2. Review this documentation
3. Check `design-system.clj` source code
4. Consult Tailwind CSS documentation

---

*Last updated: October 2025*
