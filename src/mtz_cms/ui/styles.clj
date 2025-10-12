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

(defn custom-styles
  "Custom CSS to complement Tailwind.
   
   Currently includes:
   - HTMX loading indicator animations
   - Smooth transitions for loading states
   
   Returns: Hiccup style tag with CSS"
  []
  [:style
   "
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
