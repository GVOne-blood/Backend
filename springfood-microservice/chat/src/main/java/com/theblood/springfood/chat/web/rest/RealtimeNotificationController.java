package com.theblood.springfood.chat.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint nội bộ cho các service khác (order-service, payment-service…) push
 * realtime notification tới browser của user đang đăng nhập, qua kết nối STOMP
 * sẵn có của chat-service.
 *
 * <p>Thiết kế đơn giản, tránh thêm notification-service riêng:
 * <ul>
 *   <li>POST /api/realtime/notify với payload {targetType, targetId, type, payload}</li>
 *   <li>chat-service forward tới destination STOMP tương ứng</li>
 *   <li>FE đã connect /ws với JWT, subscribe các queue/topic dưới đây để nhận realtime</li>
 * </ul>
 *
 * <p>STOMP destinations:
 * <ul>
 *   <li>USER  → /user/queue/order-events  (per-user inbox)</li>
 *   <li>SHOP  → /topic/shop.{shopId}.orders (broadcast cho shop dashboard)</li>
 * </ul>
 *
 * <p>Để demo, endpoint này KHÔNG verify identity (giống AIAssistantController hiện tại
 * khi gọi từ inter-service). Khi production cần thêm internal-token check.
 */
@RestController
@RequestMapping("/api/realtime")
public class RealtimeNotificationController {

    private static final Logger LOG = LoggerFactory.getLogger(RealtimeNotificationController.class);

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeNotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/notify")
    public Map<String, Object> notify(@RequestBody NotifyRequest request) {
        if (request == null || request.getTargetType() == null || request.getTargetId() == null) {
            throw new IllegalArgumentException("targetType and targetId are required");
        }

        // Bổ sung timestamp cho payload nếu thiếu, để FE không cần tự generate
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("type", request.getType());
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("payload", request.getPayload());

        switch (request.getTargetType().toUpperCase()) {
            case "USER" -> {
                String dest = "/queue/order-events";
                LOG.info("Pushing realtime notify to USER {} dest=/user/{}{}", request.getTargetId(), request.getTargetId(), dest);
                messagingTemplate.convertAndSendToUser(request.getTargetId(), dest, envelope);
            }
            case "SHOP" -> {
                String topic = "/topic/shop." + request.getTargetId() + ".orders";
                LOG.info("Pushing realtime notify to SHOP topic={}", topic);
                messagingTemplate.convertAndSend(topic, envelope);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported targetType: " + request.getTargetType() + " (expect USER or SHOP)"
            );
        }

        return Map.of("status", "OK");
    }

    /**
     * Body schema:
     * <pre>
     * {
     *   "targetType": "USER" | "SHOP",
     *   "targetId":   "<uuid|userId>",
     *   "type":       "ORDER_CREATED" | "ORDER_APPROVED" | ...,
     *   "payload":    { ... domain payload ... }
     * }
     * </pre>
     */
    public static class NotifyRequest {
        private String targetType;
        private String targetId;
        private String type;
        private Object payload;

        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Object getPayload() { return payload; }
        public void setPayload(Object payload) { this.payload = payload; }
    }
}
