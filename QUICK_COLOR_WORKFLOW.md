# 🎨 Quick Color Change Workflow

**Keep this open while experimenting!**

---

## The 4-Step Loop (Takes 30 seconds per color)

### 1️⃣ Pick a Color
Browse: https://tailwindcss.com/docs/customizing-colors
Choose: `purple`, `emerald`, `indigo`, `sky`, `amber`, etc.

### 2️⃣ Edit File
```bash
# Open in editor:
src/mtz_cms/ui/design_system.clj

# Go to line 86 and change:
:primary :blue-600    # ← Change to your color
:primary :purple-600  # Example
```

Update lines 86-90:
```clojure
:primary           :purple-600   ; Main color
:primary-light     :purple-100   ; Light version
:primary-lighter   :purple-50    ; Lightest
:primary-dark      :purple-700   ; Dark version
:primary-darker    :purple-800   ; Darkest
```

### 3️⃣ Reload in REPL
```clojure
(require 'mtz-cms.ui.design-system :reload)
(user/restart)
```

### 4️⃣ View Results
```
Open: http://localhost:3000/admin/style-guide
Check: Colors, buttons, header
```

**Repeat!** Don't like it? Try another color!

---

## 🎨 Quick Pick List

Just copy-paste these into lines 86-90:

### Purple (Traditional/Royal)
```clojure
:primary :purple-600
:primary-light :purple-100
:primary-lighter :purple-50
:primary-dark :purple-700
:primary-darker :purple-800
```

### Emerald (Fresh/Modern)
```clojure
:primary :emerald-600
:primary-light :emerald-100
:primary-lighter :emerald-50
:primary-dark :emerald-700
:primary-darker :emerald-800
```

### Indigo (Deep/Professional)
```clojure
:primary :indigo-600
:primary-light :indigo-100
:primary-lighter :indigo-50
:primary-dark :indigo-700
:primary-darker :indigo-800
```

### Sky (Light/Welcoming)
```clojure
:primary :sky-600
:primary-light :sky-100
:primary-lighter :sky-50
:primary-dark :sky-700
:primary-darker :sky-800
```

### Amber (Warm/Gold)
```clojure
:primary :amber-600
:primary-light :amber-100
:primary-lighter :amber-50
:primary-dark :amber-700
:primary-darker :amber-800
```

### Teal (Calm/Balanced)
```clojure
:primary :teal-600
:primary-light :teal-100
:primary-lighter :teal-50
:primary-dark :teal-700
:primary-darker :teal-800
```

### Rose (Soft/Compassionate)
```clojure
:primary :rose-600
:primary-light :rose-100
:primary-lighter :rose-50
:primary-dark :rose-700
:primary-darker :rose-800
```

### Slate (Sophisticated)
```clojure
:primary :slate-700
:primary-light :slate-200
:primary-lighter :slate-100
:primary-dark :slate-800
:primary-darker :slate-900
```

---

## ✅ Checklist for Each Color

When viewing in style guide, check:
- [ ] Color swatches look good
- [ ] Primary button looks clickable
- [ ] Header background works
- [ ] Text is readable
- [ ] Overall page feels cohesive
- [ ] Feels right for Mount Zion

---

## 🚨 Emergency Reset

If something breaks:
```clojure
;; Change line 86 back to:
:primary :blue-600

;; In REPL:
(require 'mtz-cms.ui.design-system :reload)
(user/restart)
```

---

## 📝 Track Your Favorites

```
✅ purple - Looks royal, good for traditional feel
🤔 emerald - Nice but maybe too modern?
❌ amber - Too bright, hard to read
⭐ indigo - FAVORITE! Deep and professional

Final choice: _____________
```

---

**Experiment fast, try many colors, have fun!**
