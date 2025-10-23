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
     background-color: oklch(98.4% 0.014 180.72);
   }

   .text-mint-light {
     color: oklch(98.4% 0.014 180.72);
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

   /* Prose Styling - Custom typography for content areas */
   /* Tailwind CDN doesn't include @tailwindcss/typography, so we define it */

   .prose {
     color: #374151; /* gray-700 */
     max-width: 65ch;
   }

   .prose p,
   .prose div {
     margin-top: 1.25em;
     margin-bottom: 1.25em;
     font-size: 1.5rem; /* 24px - larger, more readable */
     line-height: 1.75; /* increased from 18px */
   }

   /* Remove excessive margins between consecutive divs */
   .prose div + div {
     margin-top: 0;
   }

   .prose h1 {
     color: #111827; /* gray-900 */
     font-weight: 800;
     font-size: 2.25em;
     margin-top: 0;
     margin-bottom: 0.8888889em;
     line-height: 1.1111111;
   }

   .prose h2 {
     color: #111827; /* gray-900 */
     font-weight: 700;
     font-size: 1.5em;
     margin-top: 2em;
     margin-bottom: 1em;
     line-height: 1.3333333;
   }

   .prose h3 {
     color: #111827; /* gray-900 */
     font-weight: 600;
     font-size: 1.25em;
     margin-top: 1.6em;
     margin-bottom: 0.6em;
     line-height: 1.6;
   }

   .prose ul, .prose ol {
     margin-top: 1.25em;
     margin-bottom: 1.25em;
     padding-left: 1.625em;
   }

   .prose li {
     margin-top: 0.5em;
     margin-bottom: 0.5em;
     font-size: 1.5rem; /* 24px - matches paragraph size */
     line-height: 1.75;
   }

   .prose strong {
     color: #111827;
     font-weight: 600;
   }

   .prose a {
     color: oklch(65% 0.12 166.113); /* mint-primary */
     text-decoration: underline;
     font-weight: 500;
   }

   .prose a:hover {
     color: oklch(55% 0.10 166.113); /* mint-dark */
   }

   /* Alfresco Content Styles - for static pages with custom classes */

   /* Schedule container */
   .schedule {
     background-color: oklch(98.4% 0.014 180.72); /* bg-mint-light */
     border-left: 4px solid oklch(65% 0.12 166.113); /* border-mint-primary */
     padding: 1rem;
     margin: 1.25rem 0;
     border-radius: 0.375rem;
   }

   .schedule h2 {
     font-size: 1.5rem;
     font-weight: 700;
     margin-top: 0.5rem;
     margin-bottom: 1rem;
   }

   /* Individual day blocks */
   .day {
     margin: 0.625rem 0;
     padding: 0.75rem;
     background-color: white;
     border-radius: 0.375rem;
     border: 1px solid #e5e7eb; /* gray-200 */
   }

   .day h3 {
     font-size: 1.25rem;
     font-weight: 600;
     margin-bottom: 0.5rem;
   }

   .day p {
     font-size: 1.125rem; /* 18px */
     line-height: 1.75;
     margin: 0.5rem 0;
   }

   /* Time information */
   .time {
     font-weight: 600;
     color: oklch(55% 0.10 166.113); /* text-mint-dark */
     margin: 0.5rem 0;
   }

   /* Level/skill information */
   .level {
     font-style: italic;
     color: #6b7280; /* gray-500 */
     margin: 0.5rem 0;
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
