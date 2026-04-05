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

/**
 * REST resources for managing conversations in the chat system.
 * Provides endpoints for conversation management, message history, participant management,
 * and unread count tracking.
 * <p>
 * All endpoints require JWT authentication via @PreAuthorize.
 */
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

    /**
     * GET /api/conversations : Get user's conversations with pagination.
     * Returns all conversations where the authenticated user is an ACTIVE participant,
     * ordered by last_message_at descending.
     *
     * @param pageable  the pagination information
     * @param principal the authenticated user
     * @return the ResponseEntity with status 200 (OK) and the list of conversations in body
     */
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
            ServletUriComponentsBuilder.fromCurrentRequest(),
            page
        );

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * GET /api/conversations/search : Search conversations by keyword.
     * Searches in conversation name and last_message_preview fields.
     * Only returns conversations where the user is an ACTIVE participant.
     *
     * @param keyword   the search keyword
     * @param pageable  the pagination information
     * @param principal the authenticated user
     * @return the ResponseEntity with status 200 (OK) and the list of matching conversations
     */
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
            ServletUriComponentsBuilder.fromCurrentRequest(),
            page
        );

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * GET /api/conversations/{id} : Get conversation details.
     * Returns conversation details if the user is an ACTIVE participant.
     *
     * @param id        the conversation ID
     * @param principal the authenticated user
     * @return the ResponseEntity with status 200 (OK) and the conversation in body,
     * or status 403 (Forbidden) if user is not a participant,
     * or status 404 (Not Found) if conversation doesn't exist
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationDTO> getConversation(
        @PathVariable("id") String id,
        Principal principal
    ) {
        String userId = principal.getName();
        LOG.debug("REST request to get Conversation: {}, user: {}", id, userId);

        try {
            ConversationDTO conversation = conversationService.getConversationById(id, userId)
                .orElseThrow(() -> new BadRequestAlertException("Conversation not found", ENTITY_NAME, "notfound"));

            return ResponseEntity.ok().body(conversation);
        } catch (IllegalArgumentException e) {
            // User is not a participant
            LOG.warn("User {} attempted to access conversation {} without permission", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * POST /api/conversations : Create a new conversation.
     * Creates a conversation with the specified type and participants.
     * The creator becomes the OWNER, other participants become MEMBERs.
     *
     * @param request   the conversation creation request
     * @param principal the authenticated user (creator)
     * @return the ResponseEntity with status 201 (Created) and the new conversation in body,
     * or status 400 (Bad Request) if validation fails
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
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
                .headers(HeaderUtil.createEntityCreationAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    conversation.getConversationId()
                ))
                .body(conversation);
        } catch (IllegalArgumentException e) {
            LOG.error("Failed to create conversation: {}", e.getMessage());
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "validationfailed");
        }
    }

    /**
     * GET /api/conversations/{id}/messages : Get message history with pagination.
     * Returns non-deleted messages ordered by created_at descending.
     * Maximum 50 messages per page.
     *
     * @param id        the conversation ID
     * @param pageable  the pagination information
     * @param principal the authenticated user
     * @return the ResponseEntity with status 200 (OK) and the message list in body,
     * or status 403 (Forbidden) if user is not a participant
     */
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
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
            );

            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (IllegalArgumentException e) {
            // User is not a participant
            LOG.warn("User {} attempted to access messages for conversation {} without permission", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * POST /api/conversations/{id}/participants : Add a participant to a conversation.
     * Requires the requesting user to have OWNER or ADMIN role.
     * The new participant will have MEMBER role.
     *
     * @param id        the conversation ID
     * @param request   the add participant request containing the user ID
     * @param principal the authenticated user (must be OWNER or ADMIN)
     * @return the ResponseEntity with status 200 (OK) if successful,
     * or status 400 (Bad Request) if validation fails,
     * or status 403 (Forbidden) if user doesn't have permission
     */
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
                .headers(HeaderUtil.createAlert(
                    applicationName,
                    "Participant added successfully",
                    request.getUserId()
                ))
                .build();
        } catch (IllegalArgumentException e) {
            LOG.error("Failed to add participant: {}", e.getMessage());

            // Check if it's an authorization error
            if (e.getMessage().contains("OWNER or ADMIN") || e.getMessage().contains("not a participant")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "participantaddfailed");
        }
    }

    /**
     * GET /api/conversations/{id}/unread-count : Get unread message count.
     * Returns the number of unread messages for the authenticated user in the conversation.
     *
     * @param id        the conversation ID
     * @param principal the authenticated user
     * @return the ResponseEntity with status 200 (OK) and the unread count in body,
     * or status 403 (Forbidden) if user is not a participant,
     * or status 404 (Not Found) if conversation doesn't exist
     */
    @GetMapping("/{id}/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
        @PathVariable("id") String id,
        Principal principal
    ) {
        String userId = principal.getName();
        LOG.debug("REST request to get unread count for conversation: {}, user: {}", id, userId);

        try {
            Integer unreadCount = conversationService.getUnreadCount(id, userId);
            UnreadCountResponse response = new UnreadCountResponse(id, unreadCount);

            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            // User is not a participant or conversation doesn't exist
            LOG.warn("User {} attempted to get unread count for conversation {} without permission", userId, id);

            if (e.getMessage().contains("not a participant")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
