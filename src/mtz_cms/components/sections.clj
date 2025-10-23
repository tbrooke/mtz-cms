(ns mtz-cms.components.sections
  "Section component templates - HyperUI marketing/sections converted to Hiccup"
  (:require [hiccup.core :refer [html]]))

(defn section-component
  "Two-column section with text and image.
   Data expected:
   {:section/subtitle 'Title text'
    :section/body 'HTML content'
    :section/image {:url '/api/image/...' :name 'alt text'}
    :section/type :with-image | :text-only}"
  [{:section/keys [subtitle body image type] :as data}]
  [:section
   {:class "py-8"}
   [:div
    {:class "mx-auto max-w-screen-xl px-4 py-8 sm:px-6 lg:px-8"}
    [:div
     {:class "grid grid-cols-1 gap-4 md:grid-cols-2 md:items-center md:gap-8"}

     ;; Text content column
     [:div
      [:div
       {:class "max-w-prose md:max-w-none"}
       [:h2
        {:class "text-2xl font-semibold text-gray-900 sm:text-3xl"}
        (or subtitle "Section Title")]
       [:div
        {:class "mt-4 text-gray-700 prose prose-2xl max-w-none"}
        ;; body is already processed HTML
        [:div {:dangerouslySetInnerHTML {:__html (or body "")}}]]]]

     ;; Image column (only show if image exists)
     (when (and image (:url image))
       [:div
        [:img
         {:src (:url image)
          :class "rounded shadow-lg w-full h-auto object-cover"
          :alt (or (:name image) subtitle "")}]])]]])

(defn section-component-text-only
  "Section with just text, no image - full width"
  [{:section/keys [subtitle body]}]
  [:section
   {:class "py-8"}
   [:div
    {:class "mx-auto max-w-screen-xl px-4 py-8 sm:px-6 lg:px-8"}
    [:div
     {:class "max-w-3xl mx-auto"}
     [:h2
      {:class "text-2xl font-semibold text-gray-900 sm:text-3xl mb-6"}
      (or subtitle "Section Title")]
     [:div
      {:class "text-gray-700 prose prose-2xl max-w-none"}
      [:div {:dangerouslySetInnerHTML {:__html (or body "")}}]]]]])

(defn render-section
  "Smart renderer that picks the right section layout based on type"
  [data]
  (case (:section/type data)
    :text-only (section-component-text-only data)
    :with-image (section-component data)
    :placeholder [:div {:class "p-8 text-gray-400"} "Section content loading..."]
    ;; Default: show with image if available
    (if (:section/image data)
      (section-component data)
      (section-component-text-only data))))