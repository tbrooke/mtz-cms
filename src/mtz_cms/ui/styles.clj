(ns mtz-cms.ui.styles
  "CSS and Tailwind configuration for Mount Zion CMS
   
   This namespace handles all styling concerns:
   - Tailwind CSS CDN inclusion
   - Custom CSS styles
   - HTMX loading indicators
   
   No business logic or rendering - just pure CSS configuration.")

;; --- TAILWIND CSS CDN ---

(defn tailwind-cdn
  "Tailwind CSS CDN script tag for development.
   
   In production, you may want to switch to a build process
   with proper purging for smaller file sizes.
   
   Returns: Hiccup script tag"
  []
  [:script {:src "https://cdn.tailwindcss.com"}])

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

  (custom-styles)
  ;; => [:style "...CSS..."]

  ;; Both functions return Hiccup vectors ready for page inclusion
  )
