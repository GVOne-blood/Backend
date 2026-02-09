package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.domain.enumeration.EventType;
import com.theblood.springfood.chat.service.MessageService;
import com.theblood.springfood.chat.service.dto.ConversationDTO;
import com.theblood.springfood.chat.service.dto.MessageDTO;
import com.theblood.springfood.chat.service.dto.WebSocketMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;

@RestController("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService; // Reuse existing Service

    /**
     * Gửi tin nhắn và Lưu DB
     */
    @MessageMapping("/{conversationId}/sendMessage")
    public void sendMessage(
        @DestinationVariable String conversationId,
        @Payload WebSocketMessageDTO wsMessage,
        Principal principal
    ) {
        if (principal != null) {
            wsMessage.setSenderId(principal.getName());
        }
        wsMessage.setConversationId(conversationId);
        wsMessage.setTimestamp(Instant.now());
        wsMessage.setEventType(EventType.CHAT);

        // 1. Convert to MessageDTO để lưu DB (Tận dụng MessageService)
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setClientMessageId(wsMessage.getClientMessageId());
        messageDTO.setSenderId(wsMessage.getSenderId());
        messageDTO.setSenderName(wsMessage.getSenderName()); // Client gửi lên hoặc query từ User Service
        messageDTO.setSenderAvatar(wsMessage.getSenderAvatar());
        messageDTO.setContent(wsMessage.getContent());
        messageDTO.setMessageType(wsMessage.getMessageType() != null ? wsMessage.getMessageType() : "TEXT");
        messageDTO.setStatus("SENT");

        // Link với Conversation (Cần set ID cho ConversationDTO)
        ConversationDTO convoDTO = new ConversationDTO();
        convoDTO.setConversationId(conversationId);
        messageDTO.setConversation(convoDTO);

        try {
            // 2. Lưu vào DB thông qua Service (Đảm bảo transaction & audit)
            MessageDTO savedMessage = messageService.save(messageDTO);

            // Update lại ID thật từ DB vào message trả về
            wsMessage.setClientMessageId(savedMessage.getClientMessageId()); // Giữ client ID để map
            // wsMessage.setId(savedMessage.getMessageId()); // Nếu muốn trả về Server ID

        } catch (Exception e) {
            log.error("Failed to save message", e);
            wsMessage.setEventType(EventType.CHAT); // Hoặc ERROR
            wsMessage.setContent("Error saving message: " + e.getMessage());
            // Có thể gửi lại riêng cho sender user queue thay vì broadcast topic
        }

        // 3. Broadcast tin nhắn (đã lưu thành công)
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, wsMessage);
    }

    /**
     * Typing Indicator (Không cần lưu DB)
     */
    @MessageMapping("/{conversationId}/typing")
    public void typing(
        @DestinationVariable String conversationId,
        @Payload WebSocketMessageDTO event,
        Principal principal
    ) {
        if (principal != null) {
            event.setSenderId(principal.getName());
        }
        event.setConversationId(conversationId);
        event.setEventType(EventType.TYPING);

        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, event);
    }
}
