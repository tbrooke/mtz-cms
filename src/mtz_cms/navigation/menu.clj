(ns mtz-cms.navigation.menu
  "Hierarchical navigation menu system for Mount Zion CMS

   Handles both:
   - Top-level pages (defined in config with node IDs)
   - Dynamic sub-menus (discovered via web:menuItem aspect on child pages)"
  (:require
   [mtz-cms.config.core :as config]
   [mtz-cms.alfresco.client :as alfresco]
   [mtz-cms.components.aspect-discovery :as aspect]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- TOP-LEVEL MENU CONFIGURATION ---

(def top-level-menu
  "Top-level menu items - these are the 9 main pages.
   Order matters - will display in this sequence.
   Each has a :key that maps to config/page-node-mapping."
  [{:key :home
    :label "Home"
    :path "/"
    :icon "home"}

   {:key :about
    :label "About"
    :path "/about"
    :icon "info"}

   {:key :worship
    :label "Worship"
    :path "/worship"
    :icon "church"}

   {:key :events
    :label "Events"
    :path "/events"
    :icon "calendar"}

   {:key :activities
    :label "Activities"
    :path "/activities"
    :icon "activity"
    :has-submenu? true}  ;; This one will have sub-items like "Pickle Ball"

   {:key :news
    :label "News"
    :path "/news"
    :icon "newspaper"}

   {:key :outreach
    :label "Outreach"
    :path "/outreach"
    :icon "heart"
    :has-submenu? true}  ;; May have sub-items

   {:key :preschool
    :label "Preschool"
    :path "/preschool"
    :icon "school"}

   {:key :contact
    :label "Contact"
    :path "/contact"
    :icon "mail"}])

;; --- SUBMENU DISCOVERY ---

(defn get-submenu-items
  "Get child pages that have web:menuItem=true aspect.

   These will appear as dropdown items under the parent menu.

   Example:
   - Parent: Activities (node-id from config)
   - Children with web:menuItem=true:
     - Pickle Ball
     - Youth Group
     - Bible Study

   Returns list of maps with :label, :path, :node-id"
  [ctx parent-node-id]
  (try
    ;; IMPORTANT: Must include aspectNames and properties to check web:menuItem
    (let [children-result (alfresco/get-node-children ctx parent-node-id
                                                       {:include "aspectNames,properties"})]
      (if (:success children-result)
        (let [children (get-in children-result [:data :list :entries])
              ;; Filter to pages with menuItem=true and published
              menu-items (filter (fn [child]
                                  (let [node (:entry child)]
                                    (and (aspect/is-page? node)
                                         (aspect/is-published? node)
                                         (aspect/should-show-in-menu? node))))
                                children)]

          ;; Transform to menu structure
          (map (fn [item]
                 (let [node (:entry item)
                       label (aspect/get-menu-label node)]
                   {:label label
                    :path (str "/page/" (:id node))
                    :node-id (:id node)
                    :name (:name node)}))
               menu-items))

        ;; Failed to get children
        (do
          (log/warn "Failed to get children for submenu:" parent-node-id)
          [])))

    (catch Exception e
      (log/error e "Error fetching submenu items for:" parent-node-id)
      [])))

;; --- MENU BUILDING ---

(defn build-top-level-item
  "Build a single top-level menu item with optional submenu.

   If the item has :has-submenu? true, fetches child pages with web:menuItem=true."
  [ctx menu-item]
  (let [node-id (config/get-node-id (:key menu-item))
        base-item (assoc menu-item :node-id node-id)]

    (if (:has-submenu? menu-item)
      ;; Fetch submenu items dynamically
      (let [submenu (get-submenu-items ctx node-id)]
        (assoc base-item
               :submenu submenu
               :has-children? (boolean (seq submenu))))

      ;; No submenu
      base-item)))

(defn build-navigation
  "Build complete hierarchical navigation structure.

   Returns:
   [{:key :home
     :label \"Home\"
     :path \"/\"
     :node-id \"9faac48b-6c77-4266-aac4-8b6c7752668a\"}

    {:key :activities
     :label \"Activities\"
     :path \"/activities\"
     :node-id \"bb44a590-1c61-416b-84a5-901c61716b5e\"
     :has-children? true
     :submenu [{:label \"Pickle Ball\"
                :path \"/page/12345...\"
                :node-id \"12345...\"}
               {:label \"Youth Group\"
                :path \"/page/67890...\"
                :node-id \"67890...\"}]}
    ...]"
  [ctx]
  (log/info "Building navigation menu...")

  (let [menu (mapv (partial build-top-level-item ctx) top-level-menu)
        submenu-count (reduce + (map #(count (:submenu %)) menu))]

    (log/info "Navigation built:" (count menu) "top-level items,"
              submenu-count "submenu items")
    menu))

;; --- MENU HELPERS ---

(defn find-menu-item
  "Find a menu item by key or node-id in the navigation structure"
  [nav item-key-or-id]
  (letfn [(search [items]
            (some (fn [item]
                    (cond
                      ;; Match by key
                      (= (:key item) item-key-or-id) item

                      ;; Match by node-id
                      (= (:node-id item) item-key-or-id) item

                      ;; Search submenu
                      (:submenu item)
                      (some #(when (= (:node-id %) item-key-or-id) %)
                            (:submenu item))

                      :else nil))
                  items))]
    (search nav)))

(defn get-breadcrumbs
  "Get breadcrumb trail for a page.

   Examples:
   - Home → [Home]
   - Activities → [Home, Activities]
   - Pickle Ball → [Home, Activities, Pickle Ball]"
  [nav current-node-id]
  (let [;; Find if it's a top-level item
        top-level (find-menu-item nav current-node-id)]

    (if top-level
      ;; It's a top-level page
      [{:label "Home" :path "/"}
       {:label (:label top-level) :path (:path top-level)}]

      ;; Check if it's a submenu item
      (let [parent (some (fn [item]
                          (when (:submenu item)
                            (when (some #(= (:node-id %) current-node-id)
                                       (:submenu item))
                              item)))
                        nav)
            child (when parent
                   (some #(when (= (:node-id %) current-node-id) %)
                        (:submenu parent)))]

        (if (and parent child)
          ;; Found as submenu item
          [{:label "Home" :path "/"}
           {:label (:label parent) :path (:path parent)}
           {:label (:label child) :path (:path child)}]

          ;; Not found - default to just Home
          [{:label "Home" :path "/"}])))))

(defn active-item?
  "Check if a menu item should be marked as active based on current path"
  [item current-path]
  (or (= (:path item) current-path)
      (and (not= "/" (:path item))
           (str/starts-with? current-path (:path item)))))

;; --- MENU VALIDATION ---

(defn validate-menu-structure
  "Validate that all top-level menu items have valid node IDs configured.

   Returns {:valid? boolean :errors [...] :warnings [...]}"
  []
  (let [errors (atom [])
        warnings (atom [])]

    (doseq [item top-level-menu]
      (let [node-id (config/get-node-id (:key item))]
        (when-not node-id
          (swap! errors conj (str "Missing node ID for menu item: " (:key item))))

        (when (and (:has-submenu? item) (not node-id))
          (swap! errors conj (str "Cannot fetch submenu for " (:key item) " - no node ID")))))

    {:valid? (empty? @errors)
     :errors @errors
     :warnings @warnings
     :menu-items (count top-level-menu)
     :configured-nodes (count (filter #(config/get-node-id (:key %)) top-level-menu))}))

;; --- EXPORT FOR TESTING ---

(defn menu-summary
  "Get a summary of the menu structure (useful for debugging)"
  [nav]
  {:top-level-count (count nav)
   :items-with-submenus (count (filter :has-children? nav))
   :total-submenu-items (reduce + (map #(count (:submenu % [])) nav))
   :top-level-keys (mapv :key nav)
   :items-with-children (mapv (fn [item]
                                {:key (:key item)
                                 :submenu-count (count (:submenu item []))})
                             (filter :has-children? nav))})
