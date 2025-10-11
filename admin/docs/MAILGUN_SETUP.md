# Mailgun Setup & Alfresco IMAP Integration

## 🚀 Quick Start: Mailgun SMTP

### 1. Get Mailgun Credentials

1. Log in to [Mailgun Dashboard](https://app.mailgun.com)
2. Go to **Sending** → **Domains**
3. Select your domain (or create a new sandbox domain for testing)
4. Click **SMTP** tab
5. Copy:
   - **SMTP hostname**: `smtp.mailgun.org` (or `smtp.eu.mailgun.org` for EU)
   - **Default SMTP login**: `postmaster@your-domain.mailgun.org`
   - **Default password**: (click to reveal)

### 2. Run Setup Script

```bash
bash /tmp/setup-mailgun.sh
```

This will create a `.env` file with your credentials.

### 3. Restart REPL

The REPL needs to restart to load the new Postal library and environment variables:

```bash
# Stop current REPL (Ctrl+C or Ctrl+D)

# Start fresh REPL
clojure -M:dev
```

In the REPL:
```clojure
(require 'user)
(user/start)
```

### 4. Test Contact Form

Visit http://localhost:3000/contact and submit a test message.

You should receive:
1. **Thank you email** to the submitter
2. **Notification email** to your admin email with form details

---

## 📧 Mailgun vs. SendGrid

### Why Mailgun + Postal?

**Postal** is a pure Clojure SMTP library that works with **any SMTP service**:
- ✅ Simpler - just SMTP, no vendor-specific SDK
- ✅ Standard SMTP - works with Mailgun, Gmail, Office365, etc.
- ✅ Easy to test locally with tools like MailHog
- ✅ Better integration with Alfresco IMAP

**Mailgun** advantages:
- ✅ Excellent deliverability
- ✅ Detailed analytics and logs
- ✅ Inbound email routing (for Alfresco integration)
- ✅ Email validation API
- ✅ Generous free tier (5,000 emails/month)

---

## 🔧 Alfresco IMAP Integration

Alfresco has built-in IMAP support, allowing it to receive and store emails as documents. This enables powerful workflows like:

- **Contact form → Email → Alfresco** - Emails stored as documents with metadata
- **Email-to-folder** - Route emails to specific Alfresco folders
- **Document management** - Treat emails as first-class content

### Architecture Options

#### Option 1: Mailgun Inbound → Alfresco IMAP (Recommended)

```
Contact Form → Mailgun → Parse Email → Alfresco IMAP → Store as Document
```

**Benefits:**
- Emails stored in Alfresco for record-keeping
- Searchable email archive
- Workflow integration (e.g., "assign contact request to staff member")
- Content model tagging (e.g., tag emails with `mt:contactFormSubmission`)

#### Option 2: Direct Storage via API

```
Contact Form → API Handler → Both: Send Email + Store in Alfresco
```

**Benefits:**
- More control over document structure
- Can extract form fields as metadata
- Immediate storage (no IMAP delay)

---

## 🏗️ Alfresco IMAP Configuration

### Enable IMAP in Alfresco

Edit `alfresco-global.properties`:

```properties
# Enable IMAP
imap.server.enabled=true
imap.server.port=1143
imap.server.host=0.0.0.0

# IMAP authentication
imap.server.attachments.extraction.enabled=true
```

Restart Alfresco:
```bash
sudo systemctl restart alfresco
```

### Test IMAP Connection

```bash
# Connect via telnet
telnet localhost 1143

# Or use an email client (Thunderbird, Apple Mail, etc.)
# Server: your-alfresco-server.com
# Port: 1143
# Username: admin (or any Alfresco user)
# Password: admin (or user's password)
```

### Create IMAP Folder for Contact Forms

In Alfresco:
1. Create folder: **Document Library** → **Contact Submissions**
2. Note the folder path

IMAP folder appears as: `INBOX.Contact Submissions`

---

## 📨 Mailgun Inbound Routing → Alfresco

### 1. Configure Mailgun Routes

In Mailgun Dashboard:
1. Go to **Sending** → **Domains** → **Routes**
2. Click **Create Route**

**Route 1: Store all contact form emails in Alfresco**
```
Priority: 1
Expression Type: Match Recipient
Expression: contact@mtzcg.com
Actions:
  - Forward to: imap://admin:password@your-alfresco-server:1143/INBOX.Contact%20Submissions
  - Store: (optional - keep copy in Mailgun)
```

**Route 2: Forward to admin + store in Alfresco**
```
Priority: 1
Expression Type: Match Recipient
Expression: contact@mtzcg.com
Actions:
  - Forward to: office@mtzcg.com
  - Forward to: imap://admin:password@your-alfresco-server:1143/INBOX.Contact%20Submissions
```

### 2. Update Contact Form to Use Inbound Address

Edit `src/mtz_cms/email/mailgun.clj`:

```clojure
(def mailgun-config
  {:smtp-host "smtp.mailgun.org"
   :smtp-user (System/getenv "MAILGUN_SMTP_USER")
   :smtp-password (System/getenv "MAILGUN_SMTP_PASSWORD")
   :from-email "noreply@mtzcg.com"
   :from-name "Mount Zion UCC"
   ;; Send to Mailgun inbound address → routes to Alfresco IMAP
   :admin-email "contact@mtzcg.com"  ; This triggers Mailgun route
   :admin-name "Mount Zion Church Office"})
```

Now when someone submits the contact form:
1. Thank you email → Submitter
2. Notification email → `contact@mtzcg.com`
3. Mailgun route → Forwards to Alfresco IMAP
4. Alfresco → Stores email in "Contact Submissions" folder
5. Alfresco → (Optional) Triggers workflow or notification

---

## 🔄 Advanced: API-Based Alfresco Storage

For more control, store form submissions directly via Alfresco API:

### Create Alfresco Document from Form Submission

Add to `src/mtz_cms/email/mailgun.clj`:

```clojure
(ns mtz-cms.email.mailgun
  (:require
   [postal.core :as postal]
   [mtz-cms.alfresco.client :as alfresco]
   [clojure.tools.logging :as log]))

(defn store-contact-in-alfresco
  "Store contact form submission as Alfresco document"
  [{:keys [name email phone subject message]}]
  (try
    (let [ctx {}
          ;; ID of "Contact Submissions" folder
          folder-id (System/getenv "ALFRESCO_CONTACT_FOLDER_ID")

          ;; Create document content
          content (str "Contact Form Submission\n"
                      "======================\n\n"
                      "Name: " name "\n"
                      "Email: " email "\n"
                      (when phone (str "Phone: " phone "\n"))
                      "Subject: " subject "\n\n"
                      "Message:\n" message)

          ;; Create document in Alfresco
          result (alfresco/create-document
                  ctx
                  folder-id
                  (str "Contact-" name "-" (System/currentTimeMillis) ".txt")
                  content
                  {:cm:title subject
                   :cm:description (str "Contact form from " name " (" email ")")
                   :mt:contactName name
                   :mt:contactEmail email
                   :mt:contactPhone phone})]

      (if (:success result)
        (log/info "✅ Contact form stored in Alfresco:" (:id result))
        (log/error "❌ Failed to store in Alfresco:" (:message result))))

    (catch Exception e
      (log/error "❌ Error storing contact in Alfresco:" (.getMessage e)))))

(defn send-admin-notification
  "Send notification AND store in Alfresco"
  [form-data]
  ;; Send email notification
  (send-email {...})

  ;; Store in Alfresco
  (store-contact-in-alfresco form-data))
```

### Add Environment Variable

Add to `.env`:
```bash
ALFRESCO_CONTACT_FOLDER_ID=your-folder-node-id
```

---

## 🧪 Testing

### Test SMTP Locally with MailHog

For development, use [MailHog](https://github.com/mailhog/MailHog) as a local SMTP server:

```bash
# Install MailHog
brew install mailhog

# Start MailHog
mailhog
```

Update `.env` for local testing:
```bash
MAILGUN_SMTP_HOST=localhost
MAILGUN_SMTP_PORT=1025
MAILGUN_SMTP_USER=test
MAILGUN_SMTP_PASSWORD=test
```

View emails at: http://localhost:8025

### Test Alfresco IMAP

```clojure
;; In REPL
(require '[mtz-cms.alfresco.client :as alfresco])

;; Test IMAP folder access
(alfresco/get-node-by-path {} "/Sites/swsdp/documentLibrary/Contact Submissions")
```

---

## 📋 Environment Variables Summary

```bash
# Mailgun SMTP (Required)
MAILGUN_SMTP_HOST=smtp.mailgun.org
MAILGUN_SMTP_PORT=587
MAILGUN_SMTP_USER=postmaster@your-domain.mailgun.org
MAILGUN_SMTP_PASSWORD=your-password

# Email Addresses (Required)
MAILGUN_FROM_EMAIL=noreply@mtzcg.com
MAILGUN_ADMIN_EMAIL=office@mtzcg.com

# Alfresco IMAP Storage (Optional)
ALFRESCO_CONTACT_FOLDER_ID=folder-node-id
```

---

## 🎯 Recommended Setup

For Mount Zion CMS, I recommend:

1. **Start simple**: Use Mailgun SMTP for sending emails (current implementation)
2. **Add Alfresco storage**: Use API-based storage for structured data
3. **Later**: Add IMAP routing if you want email-based workflows

This gives you:
- ✅ Reliable email delivery via Mailgun
- ✅ Structured contact data in Alfresco
- ✅ Easy to search and report on submissions
- ✅ Integration with Alfresco workflows

---

## 🔗 Resources

- [Mailgun Documentation](https://documentation.mailgun.com/)
- [Postal Library](https://github.com/drewr/postal)
- [Alfresco IMAP Docs](https://docs.alfresco.com/content-services/latest/config/email/)
- [Alfresco Content Model](https://docs.alfresco.com/content-services/latest/develop/repo-ext-points/content-model/)
