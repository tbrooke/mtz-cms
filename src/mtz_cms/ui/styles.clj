(ns mtz-cms.ui.styles
  "CSS and Tailwind configuration for Mount Zion CMS

   This namespace handles all styling concerns:
   - Tailwind CSS (CDN for dev, compiled for production)
   - Custom CSS styles
   - HTMX loading indicators

   No business logic or rendering - just pure CSS configuration.")

;; --- CONFIGURATION ---

(def ^:dynamic *use-cdn*
  "Use Tailwind CDN (development) or compiled CSS (production).

   Set to true for development (faster, no build step)
   Set to false for production (smaller bundle, custom config)

   Can be overridden with system property:
   -Dtailwind.cdn=false"
  (if-let [cdn-prop (System/getProperty "tailwind.cdn")]
    (Boolean/parseBoolean cdn-prop)
    true)) ; Default to CDN for development

;; --- TAILWIND CSS ---

(defn tailwind-cdn
  "Tailwind CSS CDN script tag for development.

   Pros: No build step, faster development
   Cons: Larger file size, no custom config

   Returns: Hiccup script tag"
  []
  [:script {:src "https://cdn.tailwindcss.com"}])

(defn tailwind-compiled
  "Compiled Tailwind CSS link tag for production.

   Pros: Smaller file size, custom config, purged unused CSS
   Cons: Requires build step (npm run build:css)

   Returns: Hiccup link tag"
  []
  [:link {:rel "stylesheet"
          :href "/css/styles.css"}])

(defn tailwind-styles
  "Returns appropriate Tailwind CSS tag based on environment.

   Uses *use-cdn* dynamic var to determine mode:
   - true: CDN script tag (development)
   - false: Compiled CSS link (production)

   Can override with system property:
   java -Dtailwind.cdn=false -jar app.jar

   Returns: Hiccup tag (script or link)"
  []
  (if *use-cdn*
    (tailwind-cdn)
    (tailwind-compiled)))

;; --- CUSTOM STYLES ---

(defn google-fonts
  "Google Fonts import for EB Garamond, IBM Plex Sans, and Source Serif 4.

   Returns: Hiccup link tags for Google Fonts"
  []
  (list
   [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
   [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin "anonymous"}]
   [:link {:rel "stylesheet"
           :href "https://fonts.googleapis.com/css2?family=EB+Garamond:wght@400;500;600;700;800&family=IBM+Plex+Sans:wght@400;500;600&family=Source+Serif+4:ital,opsz,wght@0,8..60,200..900;1,8..60,200..900&display=swap"}]))

(defn custom-styles
  "Custom CSS to complement Tailwind.

   Currently includes:
   - HTMX loading indicator animations
   - Smooth transitions for loading states
   - Custom OKLCH colors
   - Custom fonts

   Returns: Hiccup style tag with CSS"
  []
  [:style
   "
   /* Custom OKLCH colors - Coordinated mint/teal palette */

   /* Mint backgrounds - very light */
   .bg-mint-light {
     background-color: oklch(97.9% 0.021 166.113);
   }

   .text-mint-light {
     color: oklch(97.9% 0.021 166.113);
   }

   /* Mint primary - medium saturation for buttons/links */
   .bg-mint-primary {
     background-color: oklch(65% 0.12 166.113);
   }

   .text-mint-primary {
     color: oklch(65% 0.12 166.113);
   }

   .border-mint-primary {
     border-color: oklch(65% 0.12 166.113);
   }

   /* Mint dark - for hover states */
   .bg-mint-dark {
     background-color: oklch(55% 0.10 166.113);
   }

   .text-mint-dark {
     color: oklch(55% 0.10 166.113);
   }

   .hover\\:bg-mint-dark:hover {
     background-color: oklch(55% 0.10 166.113);
   }

   .hover\\:text-mint-dark:hover {
     color: oklch(55% 0.10 166.113);
   }

   /* Mint accent - lighter for highlights */
   .bg-mint-accent {
     background-color: oklch(88% 0.06 166.113);
   }

   .text-mint-accent {
     color: oklch(88% 0.06 166.113);
   }

   /* Complementary warm accent - for variety */
   .bg-warm-accent {
     background-color: oklch(75% 0.10 40);
   }

   .text-warm-accent {
     color: oklch(75% 0.10 40);
   }

   /* Custom fonts */
   .font-garamond {
     font-family: 'EB Garamond', serif;
   }

   .font-menu {
     font-family: 'IBM Plex Sans', system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
     font-weight: 500;
     letter-spacing: 0.04em;
     text-transform: uppercase;
   }

   .font-ibm-plex {
     font-family: 'IBM Plex Sans', system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
     font-weight: 400;
   }

   /* Source Serif 4 for body text */
   body {
     font-family: 'Source Serif 4', Georgia, 'Times New Roman', serif;
   }

   /* Decorative laurel wreath */
   .logo-with-laurel {
     position: relative;
     display: inline-block;
     padding-bottom: 35px;
   }

   .logo-laurel-svg {
     position: absolute;
     bottom: 0;
     left: 50%;
     transform: translateX(-50%);
     width: 100%;
     max-width: 280px;
     height: 32px;
     opacity: 0.7;
   }

   /* HTMX loading indicators */
   .htmx-indicator {
     opacity: 0;
     transition: opacity 200ms ease-in;
   }

   .htmx-request .htmx-indicator {
     opacity: 1;
   }

   .htmx-request.htmx-indicator {
     opacity: 1;
   }
   "])

;; --- REPL TESTING ---

(comment
  ;; Test the functions
  (tailwind-cdn)
  ;; => [:script {:src "https://cdn.tailwindcss.com"}]

  (tailwind-compiled)
  ;; => [:link {:rel "stylesheet" :href "/css/styles.css"}]

  (tailwind-styles)
  ;; => Uses *use-cdn* to determine which to use

  ;; Override mode temporarily
  (binding [*use-cdn* false]
    (tailwind-styles))
  ;; => [:link {:rel "stylesheet" :href "/css/styles.css"}]

  (custom-styles)
  ;; => [:style "...CSS..."]

  ;; Check current mode
  *use-cdn*
  ;; => true (default)

  ;; All functions return Hiccup vectors ready for page inclusion
  )
