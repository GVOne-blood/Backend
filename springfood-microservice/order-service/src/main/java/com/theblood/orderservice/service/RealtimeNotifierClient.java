package com.theblood.orderservice.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client gọi tới chat-service /api/realtime/notify để push WebSocket message
 * tới browser của user / shop owner.
 *
 * <p>Tất cả method swallow exception để KHÔNG làm fail luồng business chính
 * (ví dụ create order). Push notify là best-effort.
 */
@Slf4j
@Service
public class RealtimeNotifierClient {

    /** Eureka service-id của chat-service. */
    private static final String CHAT_SERVICE_URL = "http://CHAT/api/realtime/notify";

    private final RestTemplate restTemplate;

    public RealtimeNotifierClient(
            @Qualifier("realtimeNotifierRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void notifyUser(String userId, String type, Object payload) {
        send("USER", userId, type, payload);
    }

    public void notifyShop(String shopId, String type, Object payload) {
        send("SHOP", shopId, type, payload);
    }

    private void send(String targetType, String targetId, String type, Object payload) {
        if (targetId == null || targetId.isBlank()) {
            log.debug("Skip realtime notify: empty targetId (type={})", targetType);
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            NotifyRequest body = new NotifyRequest(targetType, targetId, type, payload);
            HttpEntity<NotifyRequest> entity = new HttpEntity<>(body, headers);

            restTemplate.postForObject(CHAT_SERVICE_URL, entity, Map.class);
            log.debug("Realtime notify sent: target={}/{}, type={}", targetType, targetId, type);
        } catch (Exception ex) {
            // Đừng để chat-service down làm fail order/payment flow.
            log.warn("Realtime notify failed (target={}/{}, type={}): {}",
                    targetType, targetId, type, ex.getMessage());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record NotifyRequest(String targetType, String targetId, String type, Object payload) {}
}
