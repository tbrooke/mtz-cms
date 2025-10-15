# Color System Update - Coordinated OKLCH Mint Palette

## Summary

The Mount Zion CMS design system has been updated to use a fully coordinated color palette based on your custom OKLCH mint header color. All brand colors, buttons, alerts, and UI elements now use harmonious mint/teal tones.

---

## New OKLCH Color Palette

All colors use the same hue (166.113°) with varying lightness and chroma for perfect coordination:

### Custom OKLCH Colors (defined in `ui/styles.clj`)

```css
/* Mint Light - Very subtle background */
oklch(97.9% 0.021 166.113)  /* Extremely light, low saturation */

/* Mint Accent - Light highlights */
oklch(88% 0.06 166.113)     /* Light, subtle saturation */

/* Mint Primary - Main brand color */
oklch(65% 0.12 166.113)     /* Medium lightness, good saturation */

/* Mint Dark - Hover states */
oklch(55% 0.10 166.113)     /* Darker, slightly lower saturation */

/* Warm Accent - Complementary */
oklch(75% 0.10 40)          /* Warm orange/peach tone */
```

**Why OKLCH?**
- Perceptually uniform (looks consistent across the palette)
- Same hue (166.113°) ensures perfect color harmony
- Varying lightness creates natural hierarchy
- Better than RGB/HSL for color coordination

---

## Semantic Color Mappings

### Brand Colors (Updated)

| Semantic Name | OKLCH Color | Usage |
|--------------|-------------|-------|
| `:primary` | `:mint-primary` | Buttons, links, primary actions |
| `:primary-light` | `:mint-accent` | Highlights, light backgrounds |
| `:primary-lighter` | `:mint-light` | Very subtle backgrounds |
| `:primary-dark` | `:mint-dark` | Hover states, active elements |
| `:secondary-warm` | `:warm-accent` | Complementary warm tone |

### Updated Mappings

| Element | Old Color | New Color |
|---------|-----------|-----------|
| Primary Button | `blue-600` | `mint-primary` |
| Secondary Button Hover | `blue-50` | `mint-light` |
| Info Alerts | `blue-600` | `mint-primary` |
| Header Background | `blue-600` | `mint-light` |
| Hover Backgrounds | `gray-100` | `mint-light` |

### Status Colors (Unchanged)

These remain the same for clear communication:
- `:success` → Green
- `:error` → Red
- `:warning` → Yellow

---

## Component Updates

### Buttons

```clojure
;; Primary - Mint primary background
(ds/button :primary)
;; => "bg-mint-primary text-white hover:bg-mint-dark"

;; Secondary - White with mint border
(ds/button :secondary)
;; => "bg-white text-mint-primary border-mint-primary hover:bg-mint-light"

;; Warm Accent - NEW! Complementary color
(ds/button :warm)
;; => "bg-warm-accent text-white"

;; Danger - Red (unchanged)
(ds/button :danger)
;; => "bg-red-600 text-white"
```

### Alerts

```clojure
;; Info alerts now use mint (coordinated theme)
(ds/alert :info)
;; => Uses mint-primary, mint-accent, mint-light

;; Other alerts unchanged
(ds/alert :success)  ; Green
(ds/alert :error)    ; Red
(ds/alert :warning)  ; Yellow
```

### Cards & UI Elements

```clojure
;; Hover states use subtle mint
(ds/card {:hover? true})
;; => Hovers to mint-light background

;; Headers use mint
(ds/bg :bg-header)
;; => "bg-mint-light"
```

---

## Font System (Already in Place)

### Custom Google Fonts

| Font | Usage | Function |
|------|-------|----------|
| **EB Garamond** | Elegant headings, decorative text | `(ds/font-family :garamond)` |
| **IBM Plex Sans (Menu)** | Navigation (uppercase, medium weight) | `(ds/font-family :menu)` |
| **IBM Plex Sans** | UI elements, clean headings | `(ds/font-family :ibm-plex)` |
| **Source Serif 4** | Body text (default) | Applied globally |

---

## Files Updated

### 1. `src/mtz_cms/ui/styles.clj`
- Added complete OKLCH color palette
- All mint shades with coordinated hue
- Text, background, border, and hover variants
- Warm complementary accent

### 2. `src/mtz_cms/ui/design_system.clj`
- Added `:mint-light`, `:mint-primary`, `:mint-dark`, `:mint-accent`, `:warm-accent` to color palette
- Updated semantic color mappings to use mint colors
- Added `font-families` map and `font-family` function
- Updated button component with `:warm` variant
- Fixed secondary button hover to use mint
- Updated namespace documentation

### 3. `src/mtz_cms/admin/style_guide.clj`
- Added "Custom OKLCH Mint Palette" section with visual swatches
- Added "Font Families" section with live previews
- Updated header to use Garamond font
- Added warm accent button example
- Updated color usage examples
- Enhanced documentation throughout

### 4. `DESIGN_SYSTEM.md`
- Updated color documentation with OKLCH details
- Added custom fonts section
- Updated button variants to include `:warm`
- Updated all code examples
- Documented semantic color mappings

---

## Usage Examples

### Building with Coordinated Colors

```clojure
;; Modern button with mint theme
[:button {:class (ds/button :primary)}
 "Get Started"]

;; Card with mint hover
[:div {:class (ds/card {:hover? true})}
 [:h3 "Title"]
 [:p "Content"]]

;; Info message with mint
[:div {:class (ds/alert :info)}
 "This now uses coordinated mint colors!"]

;; Elegant heading with custom font
[:h1 {:class (ds/classes [(ds/heading 1)
                          (ds/font-family :garamond)])}
 "Mount Zion"]

;; Navigation with menu font
[:nav {:class (ds/font-family :menu)}
 "HOME • ABOUT • SERVICES"]
```

### Mixing Colors

```clojure
;; Primary action
(ds/button :primary)

;; Warm accent for variety
(ds/button :warm)

;; Light background
(ds/bg :bg-hover)  ; => mint-light
```

---

## Visual Hierarchy

The coordinated palette creates natural visual hierarchy:

1. **Darkest** (`mint-dark`) - Hover states, emphasis
2. **Medium** (`mint-primary`) - Primary actions, links
3. **Light** (`mint-accent`) - Highlights, secondary backgrounds
4. **Lightest** (`mint-light`) - Subtle backgrounds, header

Plus **warm accent** for complementary variety.

---

## Before & After

### Before
- Mixed blue (`blue-600`, `blue-50`, `blue-700`)
- Info used different blue
- No coordination between header and UI elements
- Hardcoded Tailwind colors

### After
- Coordinated OKLCH mint palette (hue 166.113°)
- All brand colors harmonize
- Header and UI elements share color family
- Semantic names map to coordinated colors
- Complementary warm accent for variety

---

## Testing the Style Guide

To see all the updates:

```clojure
;; From REPL
(require '[mtz-cms.admin.style-guide :as sg])
(sg/style-guide-page)
```

Or visit: `/admin/style-guide`

The style guide now shows:
- ✅ Custom OKLCH Mint Palette section
- ✅ Brand color mappings
- ✅ All four custom fonts with previews
- ✅ Warm accent button
- ✅ Coordinated info alerts
- ✅ Updated code examples

---

## Benefits

1. **Visual Cohesion** - All colors from same family
2. **Perceptual Uniformity** - OKLCH ensures consistent appearance
3. **Easy Maintenance** - Change one hue, update entire palette
4. **Accessibility** - Carefully chosen lightness values for contrast
5. **Professional** - Coordinated colors + custom fonts = polished look

---

## Next Steps

1. **Test the style guide** - View all colors visually
2. **Review existing pages** - See how they look with new palette
3. **Adjust if needed** - Can tweak lightness/chroma values
4. **Build new components** - Use coordinated palette from the start

---

*Color system coordinated on October 15, 2025*
