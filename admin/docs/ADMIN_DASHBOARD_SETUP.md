# Admin Dashboard Setup Guide

## Quick Setup

### 1. Add Admin Routes

Edit `src/mtz_cms/routes/main.clj`:

```clojure
(ns mtz-cms.routes.main
  (:require
   ;; ... existing requires
   [mtz-cms.admin.dashboard :as admin]))

;; Add to your routes
(def app-routes
  [["/"] ;; ... existing routes

   ;; Admin Dashboard Routes
   ["/admin" {:get admin/admin-home-handler}]
   ["/admin/calendar" {:get admin/admin-calendar-handler}]
   ["/admin/settings" {:get admin/admin-settings-handler}]
   ["/admin/api/stats/events-count" {:get admin/api-events-count-handler}]
   ["/admin/api/system/alfresco-status" {:get admin/api-alfresco-status-handler}]
   ["/admin/api/calendar/upcoming" {:get admin/api-calendar-upcoming-handler}]])
```

### 2. Restart Server

```bash
# In REPL
(user/restart)
```

### 3. Access Dashboard

Visit: **http://localhost:3000/admin**

---

## Authentication Setup

The dashboard currently has **basic authentication placeholder**. To add proper authentication:

### Option 1: Environment Variable Auth (Simple)

Set environment variables:

```bash
# Add to .env
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your-secure-password
```

Update `src/mtz_cms/admin/dashboard.clj`:

```clojure
(defn check-auth
  "Check basic auth credentials"
  [request]
  (let [auth-header (get-in request [:headers "authorization"])
        [username password] (parse-basic-auth auth-header)]
    (and (= username (:username admin-credentials))
         (= password (:password admin-credentials)))))

(defn require-auth
  "Middleware to require authentication"
  [handler]
  (fn [request]
    (if (check-auth request)
      (handler request)
      {:status 401
       :headers {"WWW-Authenticate" "Basic realm=\"Admin Dashboard\""}
       :body "Unauthorized"})))

;; Wrap admin routes with auth
(def admin-routes
  [["/admin" {:middleware [require-auth]
              :get admin-home-handler}]
   ;; ... other admin routes with middleware
   ])
```

### Option 2: Session-Based Auth (Recommended)

Use `ring-session` for proper session management:

```clojure
;; Add to deps.edn
ring/ring-defaults {:mvn/version "0.4.0"}

;; In dashboard.clj
(require '[ring.middleware.session :refer [wrap-session]]
         '[ring.middleware.defaults :refer [wrap-defaults site-defaults]])

(defn login-page []
  [:html
   [:head [:title "Admin Login"]]
   [:body
    [:form {:method "post" :action "/admin/login"}
     [:input {:type "text" :name "username" :placeholder "Username"}]
     [:input {:type "password" :name "password" :placeholder "Password"}]
     [:button "Login"]]]])

(defn login-handler [request]
  (let [params (:form-params request)
        username (get params "username")
        password (get params "password")]
    (if (valid-credentials? username password)
      {:status 302
       :headers {"Location" "/admin"}
       :session {:user username :authenticated true}}
      {:status 401
       :body "Invalid credentials"})))

(defn require-session-auth [handler]
  (fn [request]
    (if (get-in request [:session :authenticated])
      (handler request)
      {:status 302
       :headers {"Location" "/admin/login"}})))
```

---

## Customizing the Dashboard

### Adding New Pages

1. **Create page function** in `dashboard.clj`:

```clojure
(defn my-custom-page []
  (admin-layout
   "Custom Page Title"
   [:div {:class "space-y-6"}
    [:h1 "My Custom Admin Page"]
    ;; Your content here
    ]))
```

2. **Add route handler**:

```clojure
(defn custom-page-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html (my-custom-page))})
```

3. **Add to routes**:

```clojure
["/admin/custom" {:get custom-page-handler}]
```

4. **Add to navigation** (in `admin-layout` function):

```clojure
[:a {:href "/admin/custom"
     :class "py-4 px-2 ..."}
 "Custom"]
```

### Adding HTMX Endpoints

```clojure
(defn api-my-data-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html
          [:div "Dynamic content here"])})

;; In page, trigger with:
[:div {:hx-get "/admin/api/my-data"
       :hx-trigger "load"
       :hx-swap "innerHTML"}]
```

---

## Security Best Practices

### 1. Use HTTPS in Production

```clojure
;; Configure Jetty for HTTPS
(defn start-server [port]
  (jetty/run-jetty app
    {:port port
     :ssl? true
     :ssl-port 8443
     :keystore "path/to/keystore.jks"
     :key-password "password"}))
```

### 2. Add CSRF Protection

```clojure
(require '[ring.middleware.anti-forgery :refer [wrap-anti-forgery]])

(def app
  (-> handler
      wrap-anti-forgery
      wrap-session))
```

### 3. Rate Limiting

```clojure
;; Simple rate limiter
(def rate-limits (atom {}))

(defn rate-limit-middleware [handler]
  (fn [request]
    (let [ip (get-in request [:headers "x-forwarded-for"])
          now (System/currentTimeMillis)
          requests (get @rate-limits ip [])]
      ;; Allow 100 requests per minute
      (if (< (count (filter #(> (- now %) 60000) requests)) 100)
        (do
          (swap! rate-limits update ip conj now)
          (handler request))
        {:status 429
         :body "Too many requests"}))))
```

### 4. Audit Logging

```clojure
(defn audit-log-middleware [handler]
  (fn [request]
    (let [user (get-in request [:session :user])
          path (:uri request)
          method (:request-method request)]
      (log/info "Admin action:"
               {:user user
                :path path
                :method method
                :timestamp (System/currentTimeMillis)})
      (handler request))))
```

---

## Production Deployment

### Environment Variables

```bash
# .env for production
ADMIN_USERNAME=admin
ADMIN_PASSWORD=secure-random-password
SESSION_SECRET=random-32-char-string
ENABLE_ADMIN_DASHBOARD=true
```

### Reverse Proxy Configuration

#### Nginx

```nginx
location /admin {
    auth_basic "Admin Access";
    auth_basic_user_file /etc/nginx/.htpasswd;

    proxy_pass http://localhost:3000;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

#### Apache

```apache
<Location /admin>
    AuthType Basic
    AuthName "Admin Dashboard"
    AuthUserFile /etc/apache2/.htpasswd
    Require valid-user

    ProxyPass http://localhost:3000/admin
    ProxyPassReverse http://localhost:3000/admin
</Location>
```

---

## Monitoring & Maintenance

### Health Check Endpoint

```clojure
(defn health-check-handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string
          {:status "ok"
           :timestamp (System/currentTimeMillis)
           :alfresco-connected (check-alfresco-connection)
           :cache-size (cache/get-cache-size)})})

["/admin/api/health" {:get health-check-handler}]
```

### Metrics Dashboard

```clojure
(defn metrics-page []
  (admin-layout
   "Metrics"
   [:div
    [:h2 "System Metrics"]
    [:div {:hx-get "/admin/api/metrics"
           :hx-trigger "every 5s"
           :hx-swap "innerHTML"}
     "Loading..."]]))

(defn api-metrics-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html
          [:div
           [:p "Requests: " (get-request-count)]
           [:p "Cache Hit Rate: " (get-cache-hit-rate)]
           [:p "Uptime: " (get-uptime)]])})
```

---

## Troubleshooting

### Admin page returns 404

- Check routes are registered in `main.clj`
- Restart server: `(user/restart)`

### Authentication not working

- Verify environment variables are set
- Check `admin-credentials` map in `dashboard.clj`
- Clear browser cookies

### HTMX not loading

- Check HTMX script tag in `admin-layout`
- Open browser console for errors
- Verify `/admin/api/*` endpoints are registered

### Styles not applying

- Tailwind CDN script in `<head>`
- Check class names are valid Tailwind classes
- Clear browser cache

---

## Next Steps

1. **Add authentication** - Implement proper auth system
2. **Customize pages** - Add pages for your specific needs
3. **Add metrics** - Track system performance
4. **Deploy securely** - Use HTTPS and proper authentication

---

## Resources

- [HTMX Documentation](https://htmx.org/docs/)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [Ring Middleware](https://github.com/ring-clojure/ring/wiki/Middleware)
