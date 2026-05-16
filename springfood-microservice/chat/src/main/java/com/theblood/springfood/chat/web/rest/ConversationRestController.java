package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.service.ConversationService;
import com.theblood.springfood.chat.service.MessageService;
import com.theblood.springfood.chat.service.dto.AddParticipantRequest;
import com.theblood.springfood.chat.service.dto.ConversationDTO;
import com.theblood.springfood.chat.service.dto.CreateConversationRequest;
import com.theblood.springfood.chat.service.dto.MessageDTO;
import com.theblood.springfood.chat.service.dto.UnreadCountResponse;
import com.theblood.springfood.chat.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/conversations")
public class ConversationRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationRestController.class);
    private static final String ENTITY_NAME = "chatConversation";
    private final ConversationService conversationService;
    private final MessageService messageService;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public ConversationRestController(
        ConversationService conversationService,
        MessageService messageService
    ) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        Principal principal
    ) {
        String userId = principal.getName();
        LOG.debug("REST request to get conversations for user: {}", userId);
        Page<ConversationDTO> page = conversationService.getUserConversations(userId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/direct")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationDTO> getOrCreateDirectConversation(
        @RequestParam("withUserId") String withUserId,
        Principal principal
    ) {
        String currentUserId = principal.getName();
        LOG.debug("REST request to get or create direct conversation with user: {}", withUserId);
        ConversationDTO conversation = conversationService.findOrCreateDirectConversation(currentUserId, withUserId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationDTO>> searchConversations(
        @RequestParam("keyword") String keyword,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        Principal principal
    ) {
        String userId = principal.getName();
        LOG.debug("REST request to search conversations for user: {}, keyword: {}", userId, keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestAlertException("Search keyword cannot be empty", ENTITY_NAME, "keywordempty");
        }
        Page<ConversationDTO> page = conversationService.searchUserConversations(userId, keyword, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationDTO> getConversation(
        @PathVariable("id") String id, Principal principal
    ) {
        String userId = principal.getName();
        LOG.debug("REST request to get Conversation: {}, user: {}", id, userId);
        try {
            ConversationDTO conversation = conversationService.getConversationById(id, userId)
                .orElseThrow(() -> new BadRequestAlertException("Conversation not found", ENTITY_NAME, "notfound"));
            return ResponseEntity.ok().body(conversation);
        } catch (IllegalArgumentException e) {
            LOG.warn("User {} attempted to access conversation {} without permission", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationDTO> createConversation(
        @Valid @RequestBody CreateConversationRequest request,
        Principal principal
    ) throws URISyntaxException {
        String creatorId = principal.getName();
        LOG.debug("REST request to create Conversation: {}, creator: {}", request, creatorId);
        try {
            ConversationDTO conversation = conversationService.createConversation(request, creatorId);
            return ResponseEntity
                .created(new URI("/api/conversations/" + conversation.getConversationId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, conversation.getConversationId()))
                .body(conversation);
        } catch (IllegalArgumentException e) {
            LOG.error("Failed to create conversation: {}", e.getMessage());
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "validationfailed");
        }
    }

    @GetMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageDTO>> getMessages(
        @PathVariable("id") String id,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        Principal principal
    ) {
        String userId = principal.getName();
        LOG.debug("REST request to get messages for conversation: {}, user: {}", id, userId);
        try {
            Page<MessageDTO> page = messageService.getMessageHistory(id, userId, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (IllegalArgumentException e) {
            LOG.warn("User {} attempted to access messages for conversation {} without permission", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{id}/participants")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addParticipant(
        @PathVariable("id") String id,
        @Valid @RequestBody AddParticipantRequest request,
        Principal principal
    ) {
        String requestingUserId = principal.getName();
        LOG.debug("REST request to add participant to conversation: {}, target: {}, requester: {}",
            id, request.getUserId(), requestingUserId);
        try {
            conversationService.addParticipant(id, request.getUserId(), requestingUserId);
            return ResponseEntity.ok()
                .headers(HeaderUtil.createAlert(applicationName, "Participant added successfully", request.getUserId()))
                .build();
        } catch (IllegalArgumentException e) {
            LOG.error("Failed to add participant: {}", e.getMessage());
            if (e.getMessage().contains("OWNER or ADMIN") || e.getMessage().contains("not a participant")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "participantaddfailed");
        }
    }

    @GetMapping("/{id}/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
        @PathVariable("id") String id, Principal principal
    ) {
        String userId = principal.getName();
        LOG.debug("REST request to get unread count for conversation: {}, user: {}", id, userId);
        try {
            Integer unreadCount = conversationService.getUnreadCount(id, userId);
            return ResponseEntity.ok().body(new UnreadCountResponse(id, unreadCount));
        } catch (IllegalArgumentException e) {
            LOG.warn("User {} attempted to get unread count for conversation {} without permission", userId, id);
            if (e.getMessage().contains("not a participant")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
