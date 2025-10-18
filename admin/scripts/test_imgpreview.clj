#!/usr/bin/env bb
;; Test script for Alfresco imgpreview renditions
;; Tests that images are loading with imgpreview rendition (960px width)

(require '[babashka.curl :as curl]
         '[cheshire.core :as json])

(def base-url "http://localhost:8080")
(def auth {:user "admin" :pass "admin"})

(println "\n🔍 Testing Alfresco imgpreview Rendition Implementation")
(println "=" (apply str (repeat 60 "=")))

;; Test node IDs - these should be from Hero or Feature folders
;; You'll need to replace these with actual node IDs from your Alfresco
(def test-nodes
  [;; Add your Hero/Feature image node IDs here
   ;; Example: "39985c5c-201a-42f6-985c-5c201a62f6d8"
   ])

(defn test-rendition-available
  "Test if a rendition is available for a node"
  [node-id rendition-name]
  (let [url (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                 node-id "/renditions/" rendition-name "/content")
        response (curl/get url
                          {:basic-auth [(:user auth) (:pass auth)]
                           :throw false
                           :as :bytes})]
    {:node-id node-id
     :rendition rendition-name
     :status (:status response)
     :success (< (:status response) 400)
     :size (when (< (:status response) 400)
             (count (:body response)))}))

(defn get-image-nodes-from-hero
  "Get image node IDs from the Hero folder"
  []
  (let [;; Hero folder node ID - update this with your actual Hero folder ID
        hero-node-id "39985c5c-201a-42f6-985c-5c201a62f6d8"
        url (str base-url "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                 hero-node-id "/children")
        response (curl/get url
                          {:basic-auth [(:user auth) (:pass auth)]
                           :throw false})]
    (when (< (:status response) 400)
      (let [data (json/parse-string (:body response) true)
            entries (get-in data [:list :entries])]
        (->> entries
             (filter #(= "cm:content" (get-in % [:entry :nodeType])))
             (filter #(clojure.string/starts-with?
                       (get-in % [:entry :content :mimeType] "") "image/"))
             (map #(get-in % [:entry :id])))))))

(println "\n📋 Step 1: Finding image nodes in Hero folder...")
(let [image-nodes (get-image-nodes-from-hero)]
  (if (seq image-nodes)
    (do
      (println "✅ Found" (count image-nodes) "image(s):")
      (doseq [node-id image-nodes]
        (println "   -" node-id)))
    (println "⚠️  No images found in Hero folder"))

  (when (seq image-nodes)
    (println "\n📋 Step 2: Testing imgpreview rendition availability...")
    (doseq [node-id (take 2 image-nodes)]
      (println "\n🖼️  Testing node:" node-id)

      ;; Test original
      (let [original (test-rendition-available node-id "original")]
        (if (:success original)
          (println "   ✅ Original: Available -"
                   (format "%.2f KB" (/ (:size original) 1024.0)))
          (println "   ❌ Original: Not available")))

      ;; Test imgpreview
      (let [preview (test-rendition-available node-id "imgpreview")]
        (if (:success preview)
          (println "   ✅ imgpreview: Available -"
                   (format "%.2f KB" (/ (:size preview) 1024.0)))
          (println "   ⚠️  imgpreview: Not available (Alfresco may need to generate it)")))

      ;; Test doclib
      (let [doclib (test-rendition-available node-id "doclib")]
        (if (:success doclib)
          (println "   ✅ doclib: Available -"
                   (format "%.2f KB" (/ (:size doclib) 1024.0)))
          (println "   ⚠️  doclib: Not available"))))))

(println "\n📋 Step 3: Testing CMS image proxy endpoints...")
(println "   Once your server is running, test these URLs in browser:")
(println "   - http://localhost:3000/proxy/image/[NODE-ID]")
(println "   - http://localhost:3000/proxy/image/[NODE-ID]/imgpreview")

(println "\n✅ Test Complete!")
(println "\nNext steps:")
(println "1. Start your CMS server: clj -M:dev")
(println "2. Visit homepage: http://localhost:3000")
(println "3. Check browser Network tab - images should load from /proxy/image/.../imgpreview")
(println "4. Compare file sizes: imgpreview should be much smaller than original")
