(ns mtz-cms.alfresco.content-processor
  "Content processor for handling Alfresco images and links in HTML content"
  (:require
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; --- IMAGE URL PATTERNS ---

(def share-document-details-pattern
  "Pattern for Share document details URLs"
  #"http://[^/]+/share/page/site/[^/]+/document-details\?nodeRef=workspace://SpacesStore/([a-f0-9-]+)")

(def share-proxy-api-pattern  
  "Pattern for Share proxy API URLs"
  #"http://[^/]+/share/proxy/alfresco/api/node/content/workspace/SpacesStore/([a-f0-9-]+)")

(def alfresco-api-pattern
  "Pattern for direct Alfresco API URLs"
  #"http://[^/]+/alfresco/api/.*/nodes/([a-f0-9-]+)/content")

(def img-src-pattern
  "Pattern to find all img src attributes"
  #"<img[^>]+src=\"([^\"]+)\"[^>]*>")

;; --- NODE ID EXTRACTION ---

(defn extract-node-id-from-url
  "Extract node ID from various Alfresco URL patterns with fallback"
  [url]
  (cond
    ;; Share document details link
    (re-find share-document-details-pattern url)
    (second (re-find share-document-details-pattern url))
    
    ;; Share proxy API link  
    (re-find share-proxy-api-pattern url)
    (second (re-find share-proxy-api-pattern url))
    
    ;; Direct Alfresco API link
    (re-find alfresco-api-pattern url)
    (second (re-find alfresco-api-pattern url))
    
    ;; Fallback: try to extract any UUID-like pattern
    :else
    (let [uuid-pattern #"([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})"
          match (re-find uuid-pattern url)]
      (when match
        (second match)))))

(defn find-image-urls
  "Find all image URLs in HTML content"
  [html-content]
  (when html-content
    (->> html-content
         (re-seq img-src-pattern)
         (map second)
         (filter some?))))

;; --- URL CONVERSION ---

(defn convert-image-url-to-proxy
  "Convert Alfresco image URL to local proxy URL"
  [url]
  (if-let [node-id (extract-node-id-from-url url)]
    (do
      (log/debug "Converting image URL to proxy:" url "→" (str "/proxy/image/" node-id))
      (str "/proxy/image/" node-id))
    (do
      (log/warn "Could not extract node ID from image URL:" url)
      url))) ; Return original URL as fallback

(defn process-html-content
  "Process HTML content to convert Alfresco image URLs to proxy URLs"
  [html-content]
  (if (str/blank? html-content)
    html-content
    (try
      (let [image-urls (find-image-urls html-content)
            processed-html (reduce (fn [html url]
                                     (let [proxy-url (convert-image-url-to-proxy url)]
                                       (str/replace html url proxy-url)))
                                   html-content
                                   image-urls)]
        (log/debug "Processed HTML content:" 
                   (count image-urls) "images found,"
                   (count (find-image-urls processed-html)) "proxy URLs created")
        processed-html)
      (catch Exception e
        (log/error "Error processing HTML content:" (.getMessage e))
        html-content)))) ; Return original content on error

;; --- CONTENT ITEM PROCESSING ---

(defn process-content-item
  "Process a complete content item with image URL conversion and metadata"
  [content-item]
  (if-not (:success content-item)
    content-item
    (let [original-html (:content content-item)
          processed-html (process-html-content original-html)
          image-urls (find-image-urls processed-html)
          has-images (seq image-urls)]
      
      (assoc content-item
             :processed-html processed-html
             :image-urls image-urls
             :has-images has-images
             :processed-at (java.time.Instant/now)))))

;; --- FEATURE COMPONENT ENHANCEMENT ---

(defn enhance-feature-content
  "Enhance feature component content with processed HTML"
  [feature-data]
  (if-let [content (:feature/content feature-data)]
    (let [processed-content (process-html-content content)
          image-urls (find-image-urls processed-content)]
      (assoc feature-data
             :feature/content processed-content
             :feature/processed true
             :feature/image-urls image-urls
             :feature/has-embedded-images (seq image-urls)))
    feature-data))

;; --- DEBUGGING HELPERS ---

(defn analyze-image-urls
  "Analyze and debug image URLs in content"
  [html-content]
  (let [urls (find-image-urls html-content)]
    (mapv (fn [url]
            {:original-url url
             :extracted-node-id (extract-node-id-from-url url)
             :proxy-url (convert-image-url-to-proxy url)
             :pattern-matches {:share-details (boolean (re-find share-document-details-pattern url))
                               :share-proxy (boolean (re-find share-proxy-api-pattern url))
                               :api-direct (boolean (re-find alfresco-api-pattern url))}})
          urls)))

(comment
  ;; Test the processor with Feature 2 content
  (def test-html "<p><img src=\"http://admin.mtzcg.com/share/page/site/swsdp/document-details?nodeRef=workspace://SpacesStore/fad117b4-b182-494e-9117-b4b182994ed8\" alt=\"Blood Drive\" width=\"600\" height=\"400\" /></p>")
  
  (analyze-image-urls test-html)
  (process-html-content test-html)
  
  ;; Test with different URL patterns
  (extract-node-id-from-url "http://admin.mtzcg.com/share/page/site/swsdp/document-details?nodeRef=workspace://SpacesStore/fad117b4-b182-494e-9117-b4b182994ed8")
  (extract-node-id-from-url "http://admin.mtzcg.com/share/proxy/alfresco/api/node/content/workspace/SpacesStore/fad117b4-b182-494e-9117-b4b182994ed8/image.jpg"))