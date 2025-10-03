;; Test file to verify syntax
(ns test-syntax
  (:require [clojure.edn :as edn]))

;; Test the map that was causing issues
(def test-map-1
  {:class "bg-gray-800 text-white px-2 py-1 rounded text-xs"
   :hx-get "test"
   :hx-target "closest .content"
   :hx-swap "innerHTML"})

(println "Map 1 valid:" (map? test-map-1))
(println "Map 1 count:" (count test-map-1))

;; Test the other potentially problematic maps
(def test-map-2
  {:layout :hero-features-layout
   :components {:hero {:node-id "39985c5c-201a-42f6-985c-5c201a62f6d8"}
                :features [{:node-id "264ab06c-984e-4f64-8ab0-6c984eaf6440"}
                           {:node-id "fe3c64bf-bb1b-456f-bc64-bfbb1b656f89"}
                           {:node-id "6737d1b1-5465-4625-b7d1-b15465b62530"}]}})

(println "Map 2 valid:" (map? test-map-2))
(println "Components valid:" (map? (:components test-map-2)))

(println "All syntax tests passed!")
