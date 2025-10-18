(ns mtz-cms.components.htmx
  "HTMX-powered dynamic components for Mount Zion CMS"
  (:require
   [mtz-cms.components.templates :as templates]
   [mtz-cms.components.home-features :as home-features]
   [mtz-cms.ui.design-system :as ds]
   [mtz-cms.alfresco.client :as alfresco]
   [clojure.tools.logging :as log]
   [clojure.string :as str]))

;; --- HTMX COMPONENT CONTAINERS ---

;; Hero Loading Skeleton
(defn- hero-loading-skeleton
  "Loading skeleton for hero component"
  []
  [:div {:class (ds/classes [(ds/bg :bg-page)
                             (ds/text :text-primary)
                             "py-32 min-h-screen flex items-center"])}
   [:div {:class (ds/classes [(ds/container :xl)
                              "text-center"])}
    [:div {:class "animate-pulse"}
     [:div {:class "h-8 bg-gray-300 rounded w-3/4 mx-auto mb-4"}]
     [:div {:class "h-4 bg-gray-400 rounded w-1/2 mx-auto mb-8"}]
     [:div {:class "h-10 bg-gray-500 rounded w-32 mx-auto"}]]]])

(defn htmx-hero-container
  "HTMX container that dynamically loads hero content from Alfresco"
  [node-id]
  [:section {:class "relative"
             :hx-get (str "/api/components/hero/" node-id)
             :hx-trigger "load"
             :hx-swap "innerHTML"
             :hx-indicator "#hero-loading"}
   ;; Loading state
   [:div {:id "hero-loading" :class "htmx-indicator"}
    (hero-loading-skeleton)]])

;; Feature Loading Skeleton
(defn- feature-loading-skeleton
  "Loading skeleton for feature component"
  []
  [:div {:class (ds/classes [(ds/bg :bg-card)
                             (ds/p :xl)
                             (ds/rounded :lg)
                             (ds/shadow :sm)])}
   [:div {:class "animate-pulse"}
    [:div {:class "h-6 bg-gray-300 rounded w-1/3 mb-4"}]
    [:div {:class "h-4 bg-gray-200 rounded w-full mb-2"}]
    [:div {:class "h-4 bg-gray-200 rounded w-2/3"}]]])

(defn htmx-feature-container
  "HTMX container that dynamically loads feature content from Alfresco"
  [node-id]
  [:div {:class "feature-component"
         :hx-get (str "/api/components/feature/" node-id)
         :hx-trigger "load"
         :hx-swap "innerHTML"
         :hx-indicator (str "#feature-loading-" node-id)}
   ;; Loading state
   [:div {:id (str "feature-loading-" node-id) :class "htmx-indicator"}
    (feature-loading-skeleton)]])

;; Card Loading Skeleton
(defn- card-loading-skeleton
  "Loading skeleton for card component"
  []
  [:div {:class (ds/classes [(ds/bg :secondary-lighter)
                             (ds/p :lg)
                             (ds/rounded :lg)
                             "border-2 border-dashed"
                             (ds/border-color :border-default)])}
   [:div {:class "animate-pulse text-center"}
    [:div {:class "h-32 bg-gray-200 rounded mb-4"}]
    [:div {:class "h-4 bg-gray-300 rounded w-2/3 mx-auto"}]]])

(defn htmx-card-container
  "HTMX container that dynamically loads card content from Alfresco"
  [node-id]
  [:div {:class "card-component"
         :hx-get (str "/api/components/card/" node-id)
         :hx-trigger "load"
         :hx-swap "innerHTML"
         :hx-indicator (str "#card-loading-" node-id)}
   ;; Loading state
   [:div {:id (str "card-loading-" node-id) :class "htmx-indicator"}
    (card-loading-skeleton)]])

;; --- HTMX DYNAMIC LAYOUTS ---

(defn htmx-hero-features-layout
  "HTMX-powered layout that loads components dynamically

   NOTE: This wraps HTMX loading containers with the standard layout structure.
   Uses the new card-based feature grid for better UX."
  [page-config]
  (let [hero-node-id (get-in page-config [:components :hero :node-id])
        feature-nodes (get-in page-config [:components :features])]
    [:div
     ;; Hero section - loads dynamically
     (when hero-node-id
       (htmx-hero-container hero-node-id))

     ;; Features section - NEW CARD GRID LAYOUT
     (when (seq feature-nodes)
       (home-features/htmx-features-grid
        (map-indexed
         (fn [idx feature-node]
           {:node-id (:node-id feature-node)
            :slug (str "feature" (inc idx))})
         feature-nodes)))

     ;; Refresh button for content editors - clears cache and reloads
     [:div {:class "fixed bottom-4 right-4 z-50"}
      [:button {:class (ds/classes [(ds/bg :primary)
                                    (ds/text :text-on-primary)
                                    (ds/px :md)
                                    (ds/py :sm)
                                    (ds/rounded :full)
                                    (ds/shadow :lg)
                                    (ds/hover-bg :primary-dark)
                                    (ds/transition :colors)])
                :hx-post "/api/cache/clear"
                :hx-target "body"
                :hx-swap "none"
                :title "Clear cache and refresh content from Alfresco"}
       "🔄 Refresh Content"]]]))

;; --- HTMX INTERACTIVE FEATURES ---

(defn htmx-editable-content
  "Make content editable via HTMX (for content editors)"
  [content node-id]
  [:div {:class "relative group"}
   [:div {:class "content"} content]
   ;; Edit overlay (only visible to editors)
   [:div {:class (ds/classes ["absolute top-0 right-0"
                              "opacity-0 group-hover:opacity-100"
                              (ds/transition :opacity)])}
    [:button {:class (ds/classes [(ds/bg :secondary)
                                  (ds/text :text-on-primary)
                                  (ds/px :sm)
                                  "py-1"
                                  (ds/rounded :md)
                                  (ds/text-size :xs)])
              :hx-get (str "/api/edit/" node-id)
              :hx-target "closest .content"
              :hx-swap "innerHTML"}
     "✏️ Edit"]]])

(defn htmx-live-preview
  "Live preview that updates as content changes in Alfresco"
  [component-type node-id]
  [:div {:class "live-component"
         :hx-get (str "/api/components/" component-type "/" node-id)
         :hx-trigger "every 30s" ; Poll for changes every 30 seconds
         :hx-swap "innerHTML"}])

;; --- HTMX COMPONENT BUILDER ---

(defn htmx-component-selector
  "HTMX interface for selecting component types"
  [node-id current-type]
  [:div {:class (ds/classes ["component-selector"
                             (ds/bg :secondary-lighter)
                             (ds/p :md)
                             (ds/rounded :lg)
                             (ds/mb :md)])}
   [:h4 {:class (ds/classes [(ds/font-weight :semibold)
                             (ds/text :text-primary)
                             (ds/mb :sm)])}
    "Component Type"]
   [:select {:class (ds/classes ["w-full"
                                 (ds/p :sm)
                                 (ds/border)
                                 (ds/border-color :border-default)
                                 (ds/rounded :md)])
             :hx-get "/api/components/preview"
             :hx-target "#component-preview"
             :hx-include "[name='node-id']"
             :name "component-type"}
    [:option {:value "auto" :selected (= current-type "auto")} "Auto-detect"]
    [:option {:value "hero" :selected (= current-type "hero")} "Hero Banner"]
    [:option {:value "feature-with-image" :selected (= current-type "feature-with-image")} "Feature with Image"]
    [:option {:value "feature-text-only" :selected (= current-type "feature-text-only")} "Text Feature"]
    [:option {:value "feature-card" :selected (= current-type "feature-card")} "Feature Card"]
    [:option {:value "card" :selected (= current-type "card")} "Simple Card"]]
   [:input {:type "hidden" :name "node-id" :value node-id}]
   [:div {:id "component-preview"
          :class (ds/classes [(ds/mt :md)
                              "border-2 border-dashed"
                              (ds/border-color :border-default)
                              (ds/rounded :md)
                              (ds/p :md)
                              "min-h-32"])}
    "Select a component type to see preview"]])

;; --- DYNAMIC FEATURE DISCOVERY ---

(def home-page-node-id "9faac48b-6c77-4266-aac4-8b6c7752668a")
(def hero-node-id "39985c5c-201a-42f6-985c-5c201a62f6d8")

(defn discover-home-features
  "Dynamically discover all feature folders in the Home Page directory.

   Returns all children of Home Page except the Hero folder.
   This allows features to be added/removed in Alfresco without code changes."
  [ctx]
  (try
    (let [children-result (alfresco/get-node-children ctx home-page-node-id)]
      (if (:success children-result)
        (let [all-children (get-in children-result [:data :list :entries])
              ;; Filter out Hero folder and keep only folders (not files)
              feature-folders (->> all-children
                                   (filter (fn [entry]
                                             (let [node-id (get-in entry [:entry :id])
                                                   is-folder (get-in entry [:entry :isFolder])]
                                               (and is-folder
                                                    (not= node-id hero-node-id)))))
                                   ;; Sort by name to maintain consistent order (Feature 1, Feature 2, Feature 3)
                                   (sort-by (fn [entry] (get-in entry [:entry :name])))
                                   (map-indexed (fn [idx entry]
                                                  {:node-id (get-in entry [:entry :id])
                                                   :name (get-in entry [:entry :name])
                                                   :slug (str "feature" (inc idx))})))]
          (log/info "📦 Discovered" (count feature-folders) "home page features:"
                    (str/join ", " (map :name feature-folders)))
          feature-folders)
        (do
          (log/error "❌ Failed to discover home features:" (:error children-result))
          [])))
    (catch Exception e
      (log/error "❌ Error discovering home features:" (.getMessage e))
      [])))

(defn get-feature-node-id-by-slug
  "Look up a feature node-id by its slug (e.g., 'feature1', 'feature2').

   Dynamically discovers features and returns the matching node-id, or nil if not found."
  [ctx slug]
  (let [features (discover-home-features ctx)
        feature (first (filter #(= (:slug %) slug) features))]
    (:node-id feature)))

;; --- PAGE CONFIGURATION ---

(defn get-page-component-config
  "Get component configuration for a page.

   For the home page, dynamically discovers all features from Alfresco.
   This means adding/removing Feature folders in Alfresco will automatically
   update the homepage without code changes."
  [page-key ctx]
  (case page-key
    :home (let [features (discover-home-features ctx)]
            {:layout :hero-features-layout
             :components {:hero {:node-id hero-node-id}
                          ;; Try dynamic discovery, fall back to hardcoded if it fails
                          :features (if (seq features)
                                      features
                                      ;; Fallback to hardcoded features (includes all 3)
                                      [{:node-id "264ab06c-984e-4f64-8ab0-6c984eaf6440" :slug "feature1"}
                                       {:node-id "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89" :slug "feature2"}
                                       {:node-id "6737d1b1-5465-4625-b7d1-b15465b62530" :slug "feature3"}])}})
    ;; Default configuration
    {:layout :simple-content-layout
     :components {}}))