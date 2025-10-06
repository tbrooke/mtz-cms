(ns mtz-cms.components.htmx
  "HTMX-powered dynamic components for Mount Zion CMS"
  (:require
   [mtz-cms.components.templates :as templates]
   [clojure.string :as str]))

;; --- HTMX COMPONENT CONTAINERS ---

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
    [:div {:class "bg-white text-gray-900 py-32 min-h-screen flex items-center"}
     [:div {:class "mx-auto max-w-screen-xl px-4 text-center"}
      [:div {:class "animate-pulse"}
       [:div {:class "h-8 bg-gray-300 rounded w-3/4 mx-auto mb-4"}]
       [:div {:class "h-4 bg-gray-400 rounded w-1/2 mx-auto mb-8"}]
       [:div {:class "h-10 bg-gray-500 rounded w-32 mx-auto"}]]]]]])

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
    [:div {:class "bg-white p-8 rounded-lg shadow-sm"}
     [:div {:class "animate-pulse"}
      [:div {:class "h-6 bg-gray-300 rounded w-1/3 mb-4"}]
      [:div {:class "h-4 bg-gray-200 rounded w-full mb-2"}]
      [:div {:class "h-4 bg-gray-200 rounded w-2/3"}]]]]])

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
    [:div {:class "bg-gray-50 p-6 rounded-lg border-2 border-dashed border-gray-300"}
     [:div {:class "animate-pulse text-center"}
      [:div {:class "h-32 bg-gray-200 rounded mb-4"}]
      [:div {:class "h-4 bg-gray-300 rounded w-2/3 mx-auto"}]]]]])

;; --- HTMX DYNAMIC LAYOUTS ---

(defn htmx-hero-features-layout
  "HTMX-powered layout that loads components dynamically

   NOTE: This wraps HTMX loading containers with the standard layout structure.
   The actual layout logic comes from ui/layouts.clj to avoid duplication."
  [page-config]
  (let [hero-node-id (get-in page-config [:components :hero :node-id])
        feature-nodes (get-in page-config [:components :features])]
    [:div
     ;; Hero section - loads dynamically
     (when hero-node-id
       (htmx-hero-container hero-node-id))

     ;; Features section - loads dynamically
     (when (seq feature-nodes)
       [:section {:class "py-12 bg-white"}
        [:div {:class "mx-auto max-w-7xl px-6 lg:px-8"}
         [:div {:class "mx-auto max-w-2xl lg:text-center mb-12"}
          [:h2 {:class "text-base font-semibold leading-7 text-blue-600"} "Our Community"]
          [:p {:class "mt-2 text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl"}
           "What's Happening at Mount Zion"]]

         ;; Dynamic features grid with HTMX loading
         [:div {:class "mx-auto mt-16 max-w-2xl sm:mt-20 lg:mt-24 lg:max-w-none"}
          [:dl {:class "grid max-w-xl grid-cols-1 gap-x-8 gap-y-16 lg:max-w-none lg:grid-cols-3"}
           (for [feature-node feature-nodes]
             [:div {:key (:node-id feature-node)}
              (htmx-feature-container (:node-id feature-node))])]]]])

     ;; Call to action section (static, same as regular layout)
     [:section {:class "bg-blue-600"}
      [:div {:class "mx-auto max-w-7xl py-12 px-6 lg:px-8 lg:py-24"}
       [:div {:class "lg:grid lg:grid-cols-2 lg:gap-8 lg:items-center"}
        [:div
         [:h2 {:class "text-3xl font-bold tracking-tight text-white sm:text-4xl"}
          "Join Our Community"]
         [:p {:class "mt-3 max-w-3xl text-lg text-blue-100"}
          "Experience the warmth and fellowship of Mount Zion UCC. All are welcome in our progressive Christian community."]]
        [:div {:class "mt-8 lg:mt-0"}
         [:div {:class "inline-flex rounded-md shadow"}
          [:a {:href "/worship"
               :class "inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-blue-600 bg-white hover:bg-blue-50"}
           "Plan Your Visit"]]
         [:div {:class "ml-3 inline-flex"}
          [:a {:href "/contact"
               :class "inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-500 hover:bg-blue-400"}
           "Contact Us"]]]]]]

     ;; Refresh button for content editors
     [:div {:class "fixed bottom-4 right-4 z-50"}
      [:button {:class "bg-blue-600 text-white px-4 py-2 rounded-full shadow-lg hover:bg-blue-700 transition-colors"
                :onclick "window.location.reload()"
                :title "Refresh content from Alfresco"}
       "🔄 Refresh"]]]))

;; --- HTMX INTERACTIVE FEATURES ---

(defn htmx-editable-content
  "Make content editable via HTMX (for content editors)"
  [content node-id]
  [:div {:class "relative group"}
   [:div {:class "content"} content]
   ;; Edit overlay (only visible to editors)
   [:div {:class "absolute top-0 right-0 opacity-0 group-hover:opacity-100 transition-opacity"}
    [:button {:class "bg-gray-800 text-white px-2 py-1 rounded text-xs"
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
  [:div {:class "component-selector bg-gray-100 p-4 rounded-lg mb-4"}
   [:h4 {:class "font-semibold text-gray-900 mb-2"} "Component Type"]
   [:select {:class "w-full p-2 border border-gray-300 rounded"
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
   [:div {:id "component-preview" :class "mt-4 border-2 border-dashed border-gray-300 rounded p-4 min-h-32"}
    "Select a component type to see preview"]])

;; --- PAGE CONFIGURATION ---

(defn get-page-component-config
  "Get component configuration for a page (future: from Alfresco aspects)"
  [page-key]
  (case page-key
    :home {:layout :hero-features-layout
           :components {:hero {:node-id "39985c5c-201a-42f6-985c-5c201a62f6d8"} ; Hero folder
                        :features [{:node-id "264ab06c-984e-4f64-8ab0-6c984eaf6440"} ; Feature 1
                                   {:node-id "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89"} ; Feature 2
                                   {:node-id "6737d1b1-5465-4625-b7d1-b15465b62530"}]}} ; Feature 3
    ;; Default configuration
    {:layout :simple-content-layout
     :components {}}))