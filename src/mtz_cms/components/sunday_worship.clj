(ns mtz-cms.components.sunday-worship
  "Sunday Worship components"
  (:require
   [clojure.string :as str]))

;; --- SUNDAY WORSHIP LIST ITEM ---

(defn worship-list-item
  "Worship service list item with bulletin thumbnail on left

   Data structure:
   {:worship/date \"09-21-25\"
    :worship/date-formatted \"September 21, 2025\"
    :worship/folder-id \"...\"
    :worship/bulletin {:pdf/id \"...\" :pdf/name \"...\" :pdf/thumbnail \"...\"}
    :worship/presentation {:pdf/id \"...\" :pdf/name \"...\" :pdf/thumbnail \"...\"}}"
  [service]
  (let [bulletin (:worship/bulletin service)
        presentation (:worship/presentation service)
        service-url (str "/worship/sunday/" (:worship/date service))
        ;; Use static pulpit image for all list thumbnails
        thumbnail-url "/images/pulpit.jpg"]

    [:a {:href service-url
         :class "block border-t border-gray-200 hover:bg-gray-50 transition-colors"}
     [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6"}
      [:div {:class "flex gap-6"}

       ;; Bulletin thumbnail on left
       [:div {:class "flex-shrink-0"}
        [:img {:src thumbnail-url
               :alt (str "Bulletin for " (:worship/date-formatted service))
               :class "w-32 h-32 object-cover rounded-lg border border-gray-300"}]]

       ;; Content on right
       [:div {:class "flex-1 min-w-0"}
        ;; Date
        [:h3 {:class "text-xl font-semibold text-gray-900 mb-3"}
         (:worship/date-formatted service)]

        ;; PDF info
        [:div {:class "space-y-2 text-sm text-gray-600"}
         (when bulletin
           [:div {:class "flex items-center"}
            [:span {:class "font-medium text-gray-700 w-24"} "Bulletin:"]
            [:span (:pdf/name bulletin)]])

         (when presentation
           [:div {:class "flex items-center"}
            [:span {:class "font-medium text-gray-700 w-24"} "Presentation:"]
            [:span (:pdf/name presentation)]])]]]]]))

;; --- SUNDAY WORSHIP LIST PAGE ---

(defn sunday-worship-list-page
  "Sunday Worship list page

   Shows list of worship services with bulletin thumbnails"
  [services]
  [:div {:class "bg-white min-h-screen"}
   ;; Page header
   [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 border-b border-gray-200"}
    [:h1 {:class "text-4xl font-bold text-gray-900 mb-2"}
     "Sunday Worship"]
    [:p {:class "text-lg text-gray-600"}
     "Bulletins and worship resources"]]

   ;; Worship services list
   [:div {:class "divide-y divide-gray-200"}
    (if (seq services)
      (for [service services]
        [:div {:key (:worship/folder-id service)}
         (worship-list-item service)])

      ;; Empty state
      [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center"}
       [:p {:class "text-gray-500"}
        "No worship services available yet."]])]

   ;; Bottom border
   [:div {:class "border-b border-gray-200"}]])

;; --- SUNDAY WORSHIP DETAIL PAGE ---

(defn pdf-card
  "Card displaying a PDF with inline viewer and download link"
  [pdf title]
  (when pdf
    [:div {:class "bg-white rounded-lg border border-gray-200 p-6"}
     ;; Title
     [:h3 {:class "text-lg font-semibold text-gray-900 mb-4 text-center"}
      title]

     ;; Inline PDF viewer
     [:div {:class "mb-4"}
      [:iframe {:src (:pdf/url pdf)
                :class "w-full h-96 border border-gray-300 rounded-lg"
                :title (str title " - " (:pdf/name pdf))}]]

     ;; Filename
     [:p {:class "text-sm text-gray-600 mb-4 text-center"}
      (:pdf/name pdf)]

     ;; Buttons
     [:div {:class "space-y-2"}
      [:a {:href (:pdf/url pdf)
           :target "_blank"
           :class "block w-full bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors text-center"}
       "Open in New Tab"]
      [:a {:href (:pdf/url pdf)
           :download (:pdf/name pdf)
           :class "block w-full bg-gray-100 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-200 transition-colors text-center"}
       "Download"]]]))

(defn sunday-worship-detail-page
  "Sunday Worship detail page showing both PDFs

   Data structure same as worship-list-item"
  [service]
  [:div {:class "bg-white min-h-screen"}
   ;; Page header
   [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 border-b border-gray-200"}
    [:h1 {:class "text-3xl font-bold text-gray-900"}
     "Sunday Worship"]]

   ;; Service content
   [:div {:class "max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8"}
    ;; Date heading
    [:h2 {:class "text-2xl font-bold text-gray-900 mb-8 text-center"}
     (:worship/date-formatted service)]

    ;; Video player (if video exists)
    (when (:worship/video service)
      (let [video (:worship/video service)]
        [:div {:class "mb-8"}
         [:h3 {:class "text-lg font-semibold text-gray-900 mb-4"}
          "Sermon Video"]
         [:video {:controls true
                  :class "w-full rounded-lg border border-gray-300"
                  :preload "auto"
                  :poster "/images/pulpit.jpg"}
          [:source {:src (:video/url video)
                    :type (:video/mime-type video)}]
          "Your browser does not support the video tag."]]))

    ;; PDFs side by side
    [:div {:class "grid grid-cols-1 md:grid-cols-2 gap-8 mb-8"}
     (pdf-card (:worship/bulletin service) "Bulletin")
     (pdf-card (:worship/presentation service) "Presentation")]

    ;; Back link
    [:div {:class "mt-12 pt-8 border-t border-gray-200 text-center"}
     [:a {:href "/worship/sunday"
          :class "text-blue-600 hover:text-blue-800 font-medium"}
      "← Back to Sunday Worship"]]]])
