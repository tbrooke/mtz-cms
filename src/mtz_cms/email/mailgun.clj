(ns mtz-cms.email.mailgun
  "Mailgun email client for Mount Zion CMS using Postal

   Handles sending emails via Mailgun SMTP for contact form submissions.
   Much simpler than SendGrid - uses standard SMTP via Postal library."
  (:require
   [postal.core :as postal]
   [clojure.tools.logging :as log]))

;; --- CONFIGURATION ---

(def mailgun-config
  "Mailgun SMTP configuration

   IMPORTANT: Set these environment variables before using:
   - MAILGUN_SMTP_HOST: e.g. smtp.mailgun.org
   - MAILGUN_SMTP_USER: Your Mailgun SMTP username (postmaster@your-domain.mailgun.org)
   - MAILGUN_SMTP_PASSWORD: Your Mailgun SMTP password
   - MAILGUN_FROM_EMAIL: Email to send FROM (default: noreply@mtzcg.com)
   - MAILGUN_ADMIN_EMAIL: Email to receive notifications (default: office@mtzcg.com)

   Get credentials from: https://app.mailgun.com/app/sending/domains"
  {:smtp-host (or (System/getenv "MAILGUN_SMTP_HOST") "smtp.mailgun.org")
   :smtp-port (or (some-> (System/getenv "MAILGUN_SMTP_PORT") Integer/parseInt) 587)
   :smtp-user (System/getenv "MAILGUN_SMTP_USER")
   :smtp-password (System/getenv "MAILGUN_SMTP_PASSWORD")
   :from-email (or (System/getenv "MAILGUN_FROM_EMAIL") "noreply@mtzcg.com")
   :from-name "Mount Zion UCC"
   :admin-email (or (System/getenv "MAILGUN_ADMIN_EMAIL") "office@mtzcg.com")
   :admin-name "Mount Zion Church Office"})

;; --- EMAIL SENDING ---

(defn send-email
  "Send an email via Mailgun SMTP using Postal

   Parameters (map):
   - :to-email - Recipient email address
   - :to-name - Recipient name
   - :subject - Email subject
   - :html-body - HTML email body
   - :text-body - Plain text email body (optional)

   Returns: {:success true/false, :message string}"
  [{:keys [to-email to-name subject html-body text-body]}]
  (try
    (let [{:keys [smtp-host smtp-port smtp-user smtp-password from-email from-name]} mailgun-config]

      ;; Validate configuration
      (when-not smtp-user
        (log/error "❌ MAILGUN_SMTP_USER environment variable not set!")
        (throw (ex-info "Mailgun SMTP user not configured" {})))

      (when-not smtp-password
        (log/error "❌ MAILGUN_SMTP_PASSWORD environment variable not set!")
        (throw (ex-info "Mailgun SMTP password not configured" {})))

      ;; Build email message
      (let [message {:from (str from-name " <" from-email ">")
                     :to (if to-name
                           (str to-name " <" to-email ">")
                           to-email)
                     :subject subject
                     :body (cond
                             ;; Both HTML and text
                             (and html-body text-body)
                             [{:type "text/plain" :content text-body}
                              {:type "text/html" :content html-body}]

                             ;; HTML only
                             html-body
                             [{:type "text/html" :content html-body}]

                             ;; Text only
                             text-body
                             text-body

                             ;; No body provided
                             :else
                             "")}

            ;; SMTP connection configuration
            connection {:host smtp-host
                       :port smtp-port
                       :user smtp-user
                       :pass smtp-password
                       :tls true}]

        ;; Send email via Postal
        (log/info "📧 Sending email to:" to-email)
        (let [result (postal/send-message connection message)]

          (if (= :SUCCESS (:error result))
            (do
              (log/info "✅ Email sent successfully to:" to-email)
              {:success true
               :message "Email sent successfully"})
            (do
              (log/error "❌ Email failed:" (:message result))
              {:success false
               :message (str "Email failed: " (:message result))})))))

    (catch Exception e
      (log/error "❌ Error sending email:" (.getMessage e))
      {:success false
       :message (str "Error sending email: " (.getMessage e))})))

;; --- CONTACT FORM EMAILS ---

(defn send-thank-you-email
  "Send thank you email to person who submitted contact form

   Parameters:
   - name: Person's name
   - email: Person's email address"
  [{:keys [name email]}]
  (send-email
   {:to-email email
    :to-name name
    :subject "Thank you for contacting Mount Zion UCC"
    :html-body
    (str "<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
         "<div style='background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: white; padding: 30px; text-align: center;'>"
         "<h1 style='margin: 0;'>Mount Zion UCC</h1>"
         "<p style='margin: 10px 0 0 0; opacity: 0.9;'>United Church of Christ</p>"
         "</div>"
         "<div style='padding: 30px; background: #f9fafb;'>"
         "<h2 style='color: #1f2937; margin-top: 0;'>Thank You for Reaching Out!</h2>"
         "<p style='color: #4b5563; line-height: 1.6;'>Dear " name ",</p>"
         "<p style='color: #4b5563; line-height: 1.6;'>"
         "Thank you for contacting Mount Zion United Church of Christ. "
         "We have received your message and will respond as soon as possible, typically within 1-2 business days."
         "</p>"
         "<p style='color: #4b5563; line-height: 1.6;'>"
         "We look forward to connecting with you!"
         "</p>"
         "<p style='color: #4b5563; line-height: 1.6;'>Blessings,<br>"
         "<strong>Mount Zion UCC</strong></p>"
         "</div>"
         "<div style='background: #e5e7eb; padding: 20px; text-align: center; font-size: 12px; color: #6b7280;'>"
         "<p style='margin: 0;'>Mount Zion United Church of Christ</p>"
         "<p style='margin: 5px 0 0 0;'>A Progressive Christian Community</p>"
         "</div>"
         "</body></html>")
    :text-body
    (str "Dear " name ",\n\n"
         "Thank you for contacting Mount Zion United Church of Christ. "
         "We have received your message and will respond as soon as possible, typically within 1-2 business days.\n\n"
         "We look forward to connecting with you!\n\n"
         "Blessings,\n"
         "Mount Zion UCC")}))

(defn send-admin-notification
  "Send notification email to church staff about new contact form submission

   Parameters:
   - name: Person's name
   - email: Person's email
   - phone: Person's phone (optional)
   - subject: Message subject
   - message: Message content"
  [{:keys [name email phone subject message]}]
  (send-email
   {:to-email (:admin-email mailgun-config)
    :to-name (:admin-name mailgun-config)
    :subject (str "New Contact Form Submission: " subject)
    :html-body
    (str "<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
         "<div style='background: #1f2937; color: white; padding: 20px;'>"
         "<h1 style='margin: 0; font-size: 24px;'>📬 New Contact Form Submission</h1>"
         "</div>"
         "<div style='padding: 30px; background: #ffffff; border: 1px solid #e5e7eb;'>"
         "<h2 style='color: #2563eb; margin-top: 0;'>Contact Information</h2>"
         "<table style='width: 100%; border-collapse: collapse;'>"
         "<tr><td style='padding: 10px; background: #f9fafb; font-weight: bold; width: 120px;'>Name:</td>"
         "<td style='padding: 10px;'>" name "</td></tr>"
         "<tr><td style='padding: 10px; background: #f9fafb; font-weight: bold;'>Email:</td>"
         "<td style='padding: 10px;'><a href='mailto:" email "'>" email "</a></td></tr>"
         (if phone
           (str "<tr><td style='padding: 10px; background: #f9fafb; font-weight: bold;'>Phone:</td>"
                "<td style='padding: 10px;'>" phone "</td></tr>")
           "")
         "<tr><td style='padding: 10px; background: #f9fafb; font-weight: bold;'>Subject:</td>"
         "<td style='padding: 10px;'>" subject "</td></tr>"
         "</table>"
         "<h2 style='color: #2563eb; margin-top: 30px;'>Message</h2>"
         "<div style='background: #f9fafb; padding: 20px; border-left: 4px solid #2563eb; white-space: pre-wrap;'>"
         message
         "</div>"
         "<div style='margin-top: 30px; padding: 20px; background: #eff6ff; border-radius: 8px;'>"
         "<p style='margin: 0; color: #1e40af;'><strong>💡 Tip:</strong> Reply directly to this email to respond to " name ".</p>"
         "</div>"
         "</div>"
         "</body></html>")
    :text-body
    (str "New Contact Form Submission\n"
         "============================\n\n"
         "Name: " name "\n"
         "Email: " email "\n"
         (if phone (str "Phone: " phone "\n") "")
         "Subject: " subject "\n\n"
         "Message:\n"
         "--------\n"
         message "\n\n"
         "Reply to this email to respond to " name ".")}))

;; --- REPL TESTING ---

(comment
  ;; Test thank you email
  (send-thank-you-email {:name "John Smith"
                         :email "john@example.com"})

  ;; Test admin notification
  (send-admin-notification {:name "Jane Doe"
                           :email "jane@example.com"
                           :phone "555-1234"
                           :subject "Question about worship times"
                           :message "What time is your Sunday service?"})

  ;; Check configuration
  mailgun-config)
