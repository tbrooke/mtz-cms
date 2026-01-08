# Color Customization Guide

## Quick Start: Try Different Color Schemes

### Option 1: Update Semantic Mappings (Easiest - 2 Minutes)

Edit `src/mtz_cms/ui/design_system.clj` around **line 85**:

```clojure
(def semantic-colors
  {;; Brand colors - CHANGE THESE to experiment!
   :primary           :indigo-600    ; Try: :purple-600, :emerald-600, :sky-600
   :primary-light     :indigo-100
   :primary-lighter   :indigo-50
   :primary-dark      :indigo-700
   :primary-darker    :indigo-800

   :secondary         :slate-600     ; Try: :gray-600, :zinc-600
   ;; ... rest of config
   })
```

**After editing:**
```clojure
;; In REPL:
(require 'mtz-cms.ui.design-system :reload)
;; Refresh your browser to see changes
```

---

## Available Tailwind Color Palettes

### Blues/Purples (Traditional/Professional)
```clojure
:primary :blue-600      ; Classic professional blue
:primary :indigo-600    ; Rich deep blue
:primary :purple-600    ; Spiritual purple
:primary :violet-600    ; Modern purple
```

### Greens (Growth/Hope)
```clojure
:primary :emerald-600   ; Fresh modern green
:primary :teal-600      ; Calm teal
:primary :green-600     ; Traditional green
```

### Warm Colors (Welcoming)
```clojure
:primary :amber-600     ; Warm gold
:primary :orange-600    ; Energetic orange
:primary :rose-600      ; Soft rose
```

### Cool/Modern
```clojure
:primary :sky-600       ; Light modern blue
:primary :cyan-600      ; Tech cyan
:primary :slate-700     ; Sophisticated gray-blue
```

---

## Pre-Made Church Color Schemes

### 1. Traditional Catholic/Episcopal
```clojure
(def semantic-colors
  {:primary           :purple-700    ; Royal purple
   :primary-light     :purple-100
   :primary-lighter   :purple-50
   :primary-dark      :purple-800
   :primary-darker    :purple-900

   :secondary         :amber-600     ; Gold accents
   :secondary-light   :amber-200
   :secondary-lighter :amber-50
   ;; ... keep rest as-is
   })
```

### 2. Modern Evangelical/Non-Denominational
```clojure
(def semantic-colors
  {:primary           :sky-600       ; Bright welcoming blue
   :primary-light     :sky-100
   :primary-lighter   :sky-50
   :primary-dark      :sky-700
   :primary-darker    :sky-800

   :secondary         :orange-500    ; Energetic accent
   :secondary-light   :orange-200
   :secondary-lighter :orange-50
   ;; ... keep rest as-is
   })
```

### 3. Progressive/UCC Style (Current Setup)
```clojure
(def semantic-colors
  {:primary           :blue-600      ; Open, welcoming blue
   :primary-light     :blue-100
   :primary-lighter   :blue-50
   :primary-dark      :blue-700
   :primary-darker    :blue-800

   :secondary         :gray-600      ; Neutral, inclusive
   ;; ... keep rest as-is
   })
```

### 4. Nature/Creation Care Theme
```clojure
(def semantic-colors
  {:primary           :emerald-600   ; Living green
   :primary-light     :emerald-100
   :primary-lighter   :emerald-50
   :primary-dark      :emerald-700
   :primary-darker    :emerald-800

   :secondary         :teal-600      ; Water/sky
   :secondary-light   :teal-200
   :secondary-lighter :teal-50
   ;; ... keep rest as-is
   })
```

### 5. Elegant/Sophisticated
```clojure
(def semantic-colors
  {:primary           :slate-700     ; Refined dark blue-gray
   :primary-light     :slate-200
   :primary-lighter   :slate-100
   :primary-dark      :slate-800
   :primary-darker    :slate-900

   :secondary         :rose-600      ; Subtle accent
   :secondary-light   :rose-200
   :secondary-lighter :rose-50
   ;; ... keep rest as-is
   })
```

---

## Testing Your Color Changes

### In REPL:
```clojure
;; Reload the design system
(require 'mtz-cms.ui.design-system :reload)

;; Test new colors
(mtz-cms.ui.design-system/text :primary)
;; Should show your new color

;; Restart server to see changes
(user/restart)
```

### In Browser:
1. Visit: `http://localhost:3000/admin/style-guide`
2. Check the **Color Palette** section
3. Look at buttons, cards, and other components
4. Refresh after changes

---

## Using Online Color Tools

### Process:
1. **Pick a base color** from online tool
2. **Find closest Tailwind color** at https://tailwindcss.com/docs/customizing-colors
3. **Update semantic-colors map** in design_system.clj
4. **Test in style guide**

### Example: Using Coolors.co

1. Go to https://coolors.co/
2. Press spacebar until you like a palette
3. Export hex codes (e.g., `#8B5CF6`)
4. Find closest Tailwind match:
   - `#8B5CF6` ≈ `violet-500` or `purple-500`
5. Update in design_system.clj:
   ```clojure
   :primary :violet-500
   ```

---

## Custom Hex Colors (Advanced)

If you need exact brand colors not in Tailwind:

### Step 1: Add to color-palette map (line 36):
```clojure
(def ^:private color-palette
  {:blue-600  "blue-600"
   ;; ... existing colors ...

   ;; ADD YOUR CUSTOM COLORS:
   :brand-primary   "brand-primary"    ; Custom hex via config
   :brand-accent    "brand-accent"
   ;; ...
   })
```

### Step 2: Update tailwind.config.js:
```javascript
theme: {
  extend: {
    colors: {
      'brand-primary': '#8B5CF6',     // Your exact hex
      'brand-accent': '#F59E0B',
    },
  },
}
```

### Step 3: Map to semantic colors:
```clojure
(def semantic-colors
  {:primary :brand-primary    ; Uses your custom hex
   ;; ...
   })
```

### Step 4: Rebuild CSS (if using compiled mode):
```bash
npm run build:css
```

---

## Full Color Reference Chart

| Tailwind Color | Use Case | Feel |
|---------------|----------|------|
| `blue` | Professional, trustworthy | Classic |
| `indigo` | Deep, spiritual | Traditional |
| `purple/violet` | Royal, spiritual | Elegant |
| `emerald/teal` | Growth, peace | Natural |
| `sky/cyan` | Modern, open | Contemporary |
| `amber/orange` | Warm, welcoming | Friendly |
| `rose/pink` | Gentle, caring | Compassionate |
| `slate/gray` | Sophisticated, neutral | Modern |

---

## Tips

✅ **Start simple**: Just change `:primary` from `:blue-600` to `:purple-600`
✅ **Check contrast**: Ensure text is readable on backgrounds
✅ **Use style guide**: Visit `/admin/style-guide` to see all colors
✅ **Test on mobile**: Colors look different on phones
✅ **Keep status colors**: Usually keep green/red/yellow for success/error/warning

---

## Quick Experiment Script

```clojure
;; Try different colors quickly in REPL:

(def test-colors [:blue-600 :indigo-600 :purple-600 :emerald-600 :sky-600])

(doseq [color test-colors]
  (println "\nTesting" color)
  (println "Text:" (str "text-" (name color)))
  (println "BG:" (str "bg-" (name color))))

;; Then update semantic-colors with your favorite!
```

---

*Visit `/admin/style-guide` to see your changes in action!*
