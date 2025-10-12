# 🎨 START HERE - Color Experimentation Session

**Quick Start**: You're ready to try different colors for Mount Zion CMS!

---

## ✅ Current Status

**System Status**: ✅ Complete Design System Built
**Server Status**: Should be running on port 3000
**Current Color**: Blue (`blue-600`)
**Ready For**: Trying different color schemes

---

## 🚀 Three Simple Steps

### 1. Open These Now:
- **Browser Tab 1**: http://localhost:3000/admin/style-guide (View your design system)
- **Browser Tab 2**: https://tailwindcss.com/docs/customizing-colors (Pick colors)
- **Editor**: `src/mtz_cms/ui/design_system.clj` (Edit colors)
- **Keep Open**: `QUICK_COLOR_WORKFLOW.md` (Quick reference)

### 2. The Workflow (30 seconds per color):
```
Pick color → Edit file (line 86) → Reload REPL → View style guide → Repeat!
```

### 3. Try These Colors First:
- `purple` - Traditional/Royal
- `emerald` - Fresh/Modern
- `indigo` - Deep/Professional
- `sky` - Light/Welcoming

---

## 📍 Exact File to Edit

**File**: `src/mtz_cms/ui/design_system.clj`
**Line**: 86-90 (the `:primary` colors)

**Currently says**:
```clojure
:primary :blue-600
```

**Change to** (example):
```clojure
:primary :purple-600
```

Don't forget to update lines 87-90 too (light, lighter, dark, darker)!

---

## 🔄 REPL Commands

After each color change:
```clojure
(require 'mtz-cms.ui.design-system :reload)
(user/restart)
```

Then refresh: http://localhost:3000/admin/style-guide

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| `START_HERE.md` | This file - Quick overview |
| `QUICK_COLOR_WORKFLOW.md` | Step-by-step color changes |
| `CURRENT_SESSION_STATUS.md` | Full session status & plan |
| `COLOR_CUSTOMIZATION.md` | Detailed color guide |
| `DESIGN_SYSTEM.md` | Complete system documentation |

---

## 🎯 Goal for This Session

**Primary Goal**: Pick a primary color scheme that feels right for Mount Zion

**How You'll Know**:
- ✅ Tried 3-5 different colors
- ✅ Found one that feels right
- ✅ Colors look good in style guide
- ✅ Both of you agree on it

**Time Estimate**: 20-30 minutes of experimentation

---

## 🚨 Quick Troubleshooting

**Server not running?**
```clojure
(require 'user)
(user/start)
```

**Need to reset?**
```clojure
;; Change line 86 back to:
:primary :blue-600

;; Then:
(require 'mtz-cms.ui.design-system :reload)
(user/restart)
```

**Can't find file?**
```bash
# You should be here:
/Users/tombrooke/Code/trust-server/mtzion/mtz-cms

# Check with:
pwd
```

---

## ⚡ Super Quick Reference

**1. File to edit**: `src/mtz_cms/ui/design_system.clj` line 86
**2. Colors to try**: https://tailwindcss.com/docs/customizing-colors
**3. View results**: http://localhost:3000/admin/style-guide
**4. Reload command**: `(require 'mtz-cms.ui.design-system :reload)` then `(user/restart)`

---

## 🎨 Ready? Let's Go!

1. ✅ Open the 3 browser tabs above
2. ✅ Open `design_system.clj` in your editor
3. ✅ Keep `QUICK_COLOR_WORKFLOW.md` visible for reference
4. ✅ Start trying colors!

**Have fun experimenting! The workflow is fast - try lots of colors!**

---

*Questions? See `CURRENT_SESSION_STATUS.md` for detailed information*
