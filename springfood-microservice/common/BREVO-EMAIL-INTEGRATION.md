# Brevo Email Integration Guide

## Overview

SpringFood sử dụng **Brevo API** (trước đây là Sendinblue) để gửi email thay vì SMTP. Brevo cung cấp API đơn giản, nhanh, và có free tier hào phóng (300 emails/day).

---

## 1. Configuration

### 1.1. Environment Variables

File `.env` đã được config với Brevo API key:

```bash
# Email Configuration - Brevo API
MAIL_ENABLED=true
BREVO_API_KEY=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
MAIL_FROM=noreply@springfood.com
MAIL_FROM_NAME=SpringFood
MAIL_REPLY_TO=support@springfood.com
```

### 1.2. Application Configuration

Trong service của bạn (e.g., notification-service), config trong `application.yml`:

```yaml
springfood:
  email:
    enabled: ${MAIL_ENABLED:true}
    from: ${MAIL_FROM:noreply@springfood.com}
    from-name: ${MAIL_FROM_NAME:SpringFood}
    reply-to: ${MAIL_REPLY_TO:support@springfood.com}
    brevo:
      api-key: ${BREVO_API_KEY}
      api-url: https://api.brevo.com/v3
```

---

## 2. Usage Examples

### 2.1. Plain Text Email

```java
@Service
public class NotificationService {
    
    @Autowired
    private EmailService emailService;
    
    public void sendWelcomeEmail(String userEmail, String userName) {
        EmailDTO email = EmailDTO.builder()
            .to(userEmail)
            .subject("Chào mừng đến với SpringFood")
            .content("Xin chào " + userName + "! Cảm ơn bạn đã đăng ký.")
            .build();
        
        emailService.sendEmail(email);
    }
}
```

### 2.2. HTML Email

```java
public void sendOrderConfirmation(String userEmail, Order order) {
    String htmlContent = String.format("""
        <html>
        <body>
            <h2>Đơn hàng #%s đã được xác nhận</h2>
            <p>Tổng tiền: <strong>%,d VNĐ</strong></p>
            <p>Trạng thái: <span style="color: green;">Đã xác nhận</span></p>
            <p>Cảm ơn bạn đã mua hàng tại SpringFood!</p>
        </body>
        </html>
        """, order.getId(), order.getTotalAmount());
    
    EmailDTO email = EmailDTO.builder()
        .to(userEmail)
        .subject("Xác nhận đơn hàng #" + order.getId())
        .content(htmlContent)
        .isHtml(true)
        .build();
    
    emailService.sendHtmlEmail(email);
}
```

### 2.3. Template Email (Thymeleaf)

```java
public void sendNotificationEmail(String userEmail, Notification notification) {
    // Data cho template
    Map<String, Object> data = Map.of(
        "title", notification.getTitle(),
        "body", notification.getBody(),
        "actionUrl", "https://springfood.com/notifications/" + notification.getId(),
        "actionText", "Xem chi tiết"
    );
    
    EmailDTO email = EmailDTO.builder()
        .to(userEmail)
        .subject(notification.getTitle())
        .build();
    
    // Template: common/src/main/resources/templates/email/notification-email.html
    emailService.sendTemplateEmail("notification-email", email, data);
}
```

### 2.4. Email with CC

```java
public void sendReportEmail(String to, String[] ccList, Report report) {
    EmailDTO email = EmailDTO.builder()
        .to(to)
        .cc(ccList)  // CC recipients
        .subject("Báo cáo " + report.getName())
        .content(report.getContent())
        .isHtml(true)
        .build();
    
    emailService.sendHtmlEmail(email);
}
```

---

## 3. Brevo API Details

### 3.1. API Endpoint

```
POST https://api.brevo.com/v3/smtp/email
```

### 3.2. Request Headers

```
api-key: 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
Content-Type: application/json
Accept: application/json
```

### 3.3. Request Body Example

```json
{
  "sender": {
    "email": "noreply@springfood.com",
    "name": "SpringFood"
  },
  "to": [
    {
      "email": "user@example.com"
    }
  ],
  "cc": [
    {
      "email": "manager@springfood.com"
    }
  ],
  "replyTo": {
    "email": "support@springfood.com"
  },
  "subject": "Welcome to SpringFood",
  "htmlContent": "<html><body><h1>Welcome!</h1></body></html>"
}
```

### 3.4. Response

**Success (201 Created):**
```json
{
  "messageId": "<202301011200.12345@smtp-relay.mailin.fr>"
}
```

**Error (400 Bad Request):**
```json
{
  "code": "invalid_parameter",
  "message": "Invalid email address"
}
```

---

## 4. Features

### 4.1. Async Sending

Tất cả email được gửi async (không block thread):

```java
@Async
public void sendEmail(EmailDTO emailDTO) {
    // Send email asynchronously
}
```

### 4.2. Error Handling

```java
try {
    emailService.sendEmail(email);
} catch (RuntimeException e) {
    log.error("Failed to send email: {}", e.getMessage());
    // Handle error (retry, notify admin, etc.)
}
```

### 4.3. Logging

```
INFO  - Email sent successfully to: user@example.com
ERROR - Failed to send email to user@example.com: Brevo API error: 400 - Invalid email
DEBUG - Brevo API response: {"messageId":"<...>"}
```

---

## 5. Brevo Dashboard

### 5.1. Access

- URL: https://app.brevo.com
- Login với account đã tạo API key

### 5.2. Features

- **Statistics**: Xem số email đã gửi, open rate, click rate
- **Logs**: Xem chi tiết từng email (sent, delivered, bounced, opened)
- **Templates**: Tạo email templates trực tiếp trên Brevo
- **Contacts**: Quản lý danh sách email
- **Automation**: Tạo email automation workflows

### 5.3. Limits (Free Plan)

- **300 emails/day**
- Unlimited contacts
- Email support
- Basic reporting

### 5.4. Upgrade Plans

Nếu cần gửi nhiều hơn 300 emails/day:
- **Lite**: $25/month - 10,000 emails/month
- **Premium**: $65/month - 20,000 emails/month
- **Enterprise**: Custom pricing

---

## 6. Email Templates

### 6.1. Template Location

```
common/src/main/resources/templates/email/
├── notification-email.html
├── order-confirmation.html
├── password-reset.html
└── welcome-email.html
```

### 6.2. Template Example

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${data.title}">Title</title>
    <style>
        body { font-family: Arial, sans-serif; }
        .container { max-width: 600px; margin: 0 auto; }
        .button { 
            background-color: #4CAF50; 
            color: white; 
            padding: 12px 30px; 
            text-decoration: none; 
        }
    </style>
</head>
<body>
    <div class="container">
        <h1 th:text="${data.title}">Title</h1>
        <p th:text="${data.body}">Body</p>
        <a th:href="${data.actionUrl}" class="button">
            <span th:text="${data.actionText}">Action</span>
        </a>
    </div>
</body>
</html>
```

### 6.3. Template Variables

Templates có access đến:
- `${data}` - Object được pass vào từ code
- `${fromName}` - Sender name từ config

---

## 7. Testing

### 7.1. Unit Test

```java
@SpringBootTest
class EmailServiceTest {
    
    @Autowired
    private EmailService emailService;
    
    @Test
    void testSendEmail() {
        EmailDTO email = EmailDTO.builder()
            .to("test@example.com")
            .subject("Test Email")
            .content("This is a test")
            .build();
        
        assertDoesNotThrow(() -> emailService.sendEmail(email));
    }
}
```

### 7.2. Manual Test

```bash
# Test với real email
curl -X POST https://api.brevo.com/v3/smtp/email \
  -H "api-key: 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" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": {"email": "noreply@springfood.com", "name": "SpringFood"},
    "to": [{"email": "your-email@example.com"}],
    "subject": "Test Email",
    "htmlContent": "<h1>Hello from SpringFood!</h1>"
  }'
```

---

## 8. Troubleshooting

### Issue 1: API Key Invalid

```
Error: Brevo API error: 401 - Unauthorized
```

**Solution:**
- Check BREVO_API_KEY trong .env
- Verify API key trên Brevo dashboard
- Regenerate API key nếu cần

### Issue 2: Email Not Delivered

```
Error: Brevo API error: 400 - Invalid email address
```

**Solution:**
- Check email format (phải valid)
- Verify sender email đã được verify trên Brevo
- Check email không bị blacklist

### Issue 3: Rate Limit Exceeded

```
Error: Brevo API error: 429 - Too Many Requests
```

**Solution:**
- Free plan: 300 emails/day
- Upgrade plan hoặc implement queue/retry logic
- Spread emails over time

### Issue 4: Template Not Found

```
Error: Failed to send template email: Template 'xyz' not found
```

**Solution:**
- Check template file exists trong `common/src/main/resources/templates/email/`
- Check template name (không cần .html extension)
- Rebuild common module

---

## 9. Best Practices

### 9.1. Email Content

✅ Use clear, concise subject lines
✅ Include unsubscribe link (for marketing emails)
✅ Use responsive HTML templates
✅ Test emails trên nhiều email clients (Gmail, Outlook, etc.)
✅ Avoid spam trigger words

### 9.2. Performance

✅ Always send emails async
✅ Implement retry logic cho failed emails
✅ Use queue (Kafka) cho bulk emails
✅ Monitor email delivery rates
✅ Cache templates nếu có thể

### 9.3. Security

✅ Never expose API key trong code
✅ Use environment variables
✅ Validate email addresses trước khi gửi
✅ Sanitize user input trong email content
✅ Use HTTPS cho action URLs

---

## 10. Migration from SMTP

Nếu bạn đang dùng SMTP và muốn chuyển sang Brevo:

### Before (SMTP):
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

### After (Brevo):
```yaml
springfood:
  email:
    enabled: true
    brevo:
      api-key: ${BREVO_API_KEY}
```

**Advantages:**
- ✅ Không cần manage SMTP credentials
- ✅ Better deliverability
- ✅ Built-in analytics
- ✅ No Gmail "less secure apps" issues
- ✅ Higher sending limits

---

## 11. Summary

### What We Have

✅ Brevo API integration trong common module
✅ Support plain text, HTML, và template emails
✅ Async sending
✅ Error handling và logging
✅ Environment-based configuration
✅ Sample templates

### Configuration

- **API Key**: Stored in `.env`
- **Sender**: noreply@springfood.com
- **API URL**: https://api.brevo.com/v3
- **Free Limit**: 300 emails/day

### Usage

```java
@Autowired
private EmailService emailService;

emailService.sendEmail(EmailDTO.builder()
    .to("user@example.com")
    .subject("Hello")
    .content("Welcome!")
    .build());
```

---

**Date:** 2024-02-24
**Status:** ✅ Brevo Integration Complete
**API Key:** Active and configured
