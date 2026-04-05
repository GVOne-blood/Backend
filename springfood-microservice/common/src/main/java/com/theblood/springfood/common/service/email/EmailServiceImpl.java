package com.theblood.springfood.common.service.email;

import com.google.gson.Gson;
import com.theblood.springfood.common.config.email.EmailProperties;
import com.theblood.springfood.common.service.email.dto.EmailDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of EmailService using Brevo API.
 * 
 * Features:
 * - Async email sending for better performance
 * - Support for plain text and HTML emails
 * - Thymeleaf template integration
 * - Comprehensive error logging
 * 
 * Only active when springfood.email.enabled=true
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "springfood.email", name = "enabled", havingValue = "true")
public class EmailServiceImpl implements EmailService {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final EmailProperties emailProperties;
    private final SpringTemplateEngine templateEngine;
    private final Gson gson;
    
    public EmailServiceImpl(
        OkHttpClient httpClient,
        EmailProperties emailProperties,
        SpringTemplateEngine templateEngine
    ) {
        this.httpClient = httpClient;
        this.emailProperties = emailProperties;
        this.templateEngine = templateEngine;
        this.gson = new Gson();
    }
    
    @Override
    @Async
    public void sendEmail(EmailDTO emailDTO) {
        try {
            Map<String, Object> payload = buildBrevoPayload(emailDTO, false);
            sendToBrevo(payload);
            log.info("Email sent successfully to: {}", emailDTO.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", emailDTO.getTo(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    @Override
    @Async
    public void sendHtmlEmail(EmailDTO emailDTO) {
        try {
            Map<String, Object> payload = buildBrevoPayload(emailDTO, true);
            sendToBrevo(payload);
            log.info("HTML email sent successfully to: {}", emailDTO.getTo());
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", emailDTO.getTo(), e.getMessage(), e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }
    
    @Override
    @Async
    public void sendTemplateEmail(String templateName, EmailDTO emailDTO, Object context) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("data", context);
            thymeleafContext.setVariable("fromName", emailProperties.getFromName());
            
            String htmlContent = templateEngine.process(templateName, thymeleafContext);
            emailDTO.setContent(htmlContent);
            emailDTO.setHtml(true);
            
            sendHtmlEmail(emailDTO);
            log.info("Template email '{}' sent successfully to: {}", templateName, emailDTO.getTo());
        } catch (Exception e) {
            log.error("Failed to send template email '{}' to {}: {}", templateName, emailDTO.getTo(), e.getMessage(), e);
            throw new RuntimeException("Failed to send template email", e);
        }
    }
    
    /**
     * Build Brevo API payload
     */
    private Map<String, Object> buildBrevoPayload(EmailDTO emailDTO, boolean isHtml) {
        Map<String, Object> payload = new HashMap<>();
        
        // Sender
        Map<String, String> sender = new HashMap<>();
        sender.put("email", emailProperties.getFrom());
        sender.put("name", emailProperties.getFromName());
        payload.put("sender", sender);
        
        // Recipients
        Map<String, String> recipient = new HashMap<>();
        recipient.put("email", emailDTO.getTo());
        payload.put("to", List.of(recipient));
        
        // CC recipients
        if (emailDTO.getCc() != null && emailDTO.getCc().length > 0) {
            List<Map<String, String>> ccList = new java.util.ArrayList<>();
            for (String cc : emailDTO.getCc()) {
                Map<String, String> ccRecipient = new HashMap<>();
                ccRecipient.put("email", cc);
                ccList.add(ccRecipient);
            }
            payload.put("cc", ccList);
        }
        
        // Reply-to
        if (emailProperties.getReplyTo() != null) {
            Map<String, String> replyTo = new HashMap<>();
            replyTo.put("email", emailProperties.getReplyTo());
            payload.put("replyTo", replyTo);
        }
        
        // Subject
        payload.put("subject", emailDTO.getSubject());
        
        // Content
        if (isHtml || emailDTO.isHtml()) {
            payload.put("htmlContent", emailDTO.getContent());
        } else {
            payload.put("textContent", emailDTO.getContent());
        }
        
        return payload;
    }
    
    /**
     * Send email via Brevo API
     */
    private void sendToBrevo(Map<String, Object> payload) throws Exception {
        String url = emailProperties.getBrevo().getApiUrl() + "/smtp/email";
        String json = gson.toJson(payload);
        
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
            .url(url)
            .addHeader("api-key", emailProperties.getBrevo().getApiKey())
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Brevo API error: {} - {}", response.code(), errorBody);
                throw new RuntimeException("Brevo API error: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("Brevo API response: {}", responseBody);
        }
    }
}
