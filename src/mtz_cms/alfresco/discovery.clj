(ns mtz-cms.alfresco.discovery
  "Aspect-based content discovery for Alfresco CMS

   Handles discovery and classification of content based on Alfresco aspects.
   Moved from components/ to alfresco/ as part of architecture refactoring."
  (:require
   [mtz-cms.alfresco.client :as alfresco]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- ASPECT CHECKING ---

(defn has-aspect?
  "Check if a node has a specific aspect applied"
  [node aspect-name]
  (boolean (some #(= aspect-name %) (:aspectNames node []))))

(defn has-web-site-meta? [node] (has-aspect? node "web:siteMeta"))
(defn has-web-publishable? [node] (has-aspect? node "web:publishable"))

;; --- PROPERTY EXTRACTION ---

(defn get-web-kind [node] (get-in node [:properties :web:kind]))
(defn get-web-component-type [node] (get-in node [:properties :web:componentType]))
(defn get-web-publish-state [node] (get-in node [:properties :web:publishState]))
(defn get-web-menu-item? [node] (boolean (get-in node [:properties :web:menuItem])))
(defn get-web-menu-label [node] (get-in node [:properties :web:menuLabel]))

;; --- TYPE DETECTION ---

(defn is-component? [node]
  (if (has-web-site-meta? node)
    (= "component" (get-web-kind node))
    ;; Fallback: name-based detection
    (let [name (str/lower-case (:name node ""))]
      (or (str/includes? name "hero")
          (str/includes? name "feature")
          (str/includes? name "section")))))

(defn is-page? [node]
  (if (has-web-site-meta? node)
    (= "page" (get-web-kind node))
    (and (:isFolder node) (not (is-component? node)))))

(defn is-published? [node]
  (if (has-web-publishable? node)
    (= "Publish" (get-web-publish-state node))
    true)) ;; Fallback: assume published

(defn component-type-keyword [node]
  "Returns :hero, :feature, :section, etc."
  (when (is-component? node)
    (case (get-web-component-type node)
      "Hero" :hero
      "Feature" :feature
      "Section" :section
      "Blog Post" :blog-post
      "Gallery" :gallery
      "Plain HTML" :html
      ;; Fallback to name-based
      (let [name (str/lower-case (:name node ""))]
        (cond
          (str/includes? name "hero") :hero
          (str/includes? name "feature") :feature
          (str/includes? name "section") :section
          :else :unknown)))))

;; --- NAVIGATION ---

(defn should-show-in-menu?
  "Components should NEVER show in menu, only pages can.
   Returns true only if it's a page AND menuItem is true."
  [node]
  (and (is-page? node)
       (get-web-menu-item? node)))

(defn get-menu-label
  "Get menu label, but only if node should show in menu.
   Components return nil since they can't be menu items."
  [node]
  (when (should-show-in-menu? node)
    (or (get-web-menu-label node)
        (get-in node [:properties :cm:title])
        (:name node))))

;; --- VALIDATION ---

(defn validate-component-structure
  "Validate node structure with specific rules:
   - Components CANNOT be menu items (menuItem/menuLabel ignored)
   - Pages CAN be menu items
   - Components should have web:componentType
   - Published content needs web:publishable aspect"
  [node]
  (let [errors (atom [])
        warnings (atom [])]

    ;; Check for web:siteMeta
    (when-not (has-web-site-meta? node)
      (swap! warnings conj "Missing web:siteMeta aspect (will use fallback detection)"))

    (when (has-web-site-meta? node)
      ;; Check required properties
      (when-not (get-web-kind node)
        (swap! errors conj "Has web:siteMeta but missing web:kind property"))

      ;; Component-specific validation
      (when (is-component? node)
        (when-not (get-web-component-type node)
          (swap! errors conj "Component missing web:componentType"))

        ;; Components should NOT have menu properties set
        (when (get-web-menu-item? node)
          (swap! warnings conj "Component has web:menuItem=true (will be ignored - only pages can be menu items)"))

        (when (get-web-menu-label node)
          (swap! warnings conj "Component has web:menuLabel set (will be ignored - only pages can have menu labels)")))

      ;; Page-specific validation
      (when (is-page? node)
        (when (get-web-component-type node)
          (swap! warnings conj "Page has web:componentType set (typically only for components)"))

        (when (and (get-web-menu-item? node)
                  (not (get-web-menu-label node))
                  (not (get-in node [:properties :cm:title])))
          (swap! warnings conj "Menu item page should have web:menuLabel or cm:title"))))

    ;; Publishable validation
    (when-not (has-web-publishable? node)
      (swap! warnings conj "Consider adding web:publishable aspect for workflow"))

    (when (and (has-web-publishable? node)
              (not (get-web-publish-state node)))
      (swap! errors conj "Has web:publishable but missing web:publishState"))

    {:valid? (empty? @errors)
     :errors @errors
     :warnings @warnings}))

;; --- SEARCH ---

(defn search-components
  "Search for published components of a specific type"
  [ctx & {:keys [component-type published-only? parent-path]
          :or {published-only? true}}]
  (let [type-str (case component-type
                  :hero "Hero"
                  :feature "Feature"
                  :section "Section"
                  :blog-post "Blog Post"
                  :gallery "Gallery"
                  nil)
        query-parts (filter some? [
                                  "ASPECT:'web:siteMeta'"
                                  "web:kind:'component'"
                                  (when type-str (str "web:componentType:'" type-str "'"))
                                  (when published-only? "ASPECT:'web:publishable'")
                                  (when published-only? "web:publishState:'Publish'")
                                  (when parent-path (str "PATH:'" parent-path "/*'"))])
        query (str/join " AND " query-parts)]

    (log/info "Component search:" query)
    (let [result (alfresco/search-nodes ctx {:query query})]
      (if (:success result)
        (get-in result [:data :list :entries])
        []))))

(defn build-navigation
  "Build navigation menu from published pages with menuItem=true.
   Components are automatically excluded."
  [ctx]
  (let [query "ASPECT:'web:siteMeta' AND web:kind:'page' AND web:menuItem:true AND ASPECT:'web:publishable' AND web:publishState:'Publish'"
        result (alfresco/search-nodes ctx {:query query
                                           :orderBy [{:type "FIELD"
                                                     :field "web:menuLabel"
                                                     :ascending true}]})]
    (when (:success result)
      (map (fn [entry]
             (let [node (:entry entry)]
               {:id (:id node)
                :label (get-menu-label node) ;; Will extract proper label
                :path (str "/page/" (:id node))
                :name (:name node)}))
           (get-in result [:data :list :entries])))))

;; --- SUMMARY ---

(defn node-summary
  "Summary of node's aspect properties"
  [node]
  {:id (:id node)
   :name (:name node)
   :has-web-meta? (has-web-site-meta? node)
   :has-web-publishable? (has-web-publishable? node)
   :kind (get-web-kind node)
   :component-type (component-type-keyword node)
   :publish-state (get-web-publish-state node)
   :is-published? (is-published? node)
   :should-show-in-menu? (should-show-in-menu? node)
   :menu-label (get-menu-label node) ;; nil for components
   :validation (validate-component-structure node)})
