package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.dto.ConversationDTO;
import com.theblood.springfood.chat.service.dto.CreateConversationRequest;
import com.theblood.springfood.chat.service.mapper.ConversationMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.Conversation}.
 */
@Service
@Transactional
public class ConversationService {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationService.class);
    private static final String PARTICIPANT_STATUS_ACTIVE = "ACTIVE";
    private static final String PARTICIPANT_ROLE_OWNER = "OWNER";
    private static final String PARTICIPANT_ROLE_MEMBER = "MEMBER";
    private static final String CONVERSATION_TYPE_DIRECT = "DIRECT";

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final ConversationMapper conversationMapper;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversationService(
        ConversationRepository conversationRepository,
        ConversationParticipantRepository participantRepository,
        ConversationMapper conversationMapper,
        MessageRepository messageRepository,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.conversationMapper = conversationMapper;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Create a new conversation with participants.
     * Validates conversation type constraints (e.g., DIRECT must have exactly 2 participants).
     *
     * @param request the conversation creation request
     * @param creatorId the ID of the user creating the conversation
     * @return the created conversation DTO
     * @throws IllegalArgumentException if validation fails
     */
    public ConversationDTO createConversation(CreateConversationRequest request, String creatorId) {
        LOG.debug("Request to create Conversation : {}, creator: {}", request, creatorId);

        // Validate DIRECT conversation constraint
        if (CONVERSATION_TYPE_DIRECT.equals(request.getConversationType())) {
            if (request.getParticipantIds() == null || request.getParticipantIds().size() != 2) {
                throw new IllegalArgumentException("DIRECT conversation must have exactly 2 participants");
            }
        }

        // Create conversation entity
        Conversation conversation = new Conversation();
        conversation.setConversationType(request.getConversationType());
        conversation.setName(request.getName());
        conversation.setDescription(request.getDescription());
        conversation.setAvatarUrl(request.getAvatarUrl());
        conversation.setReferenceType(request.getReferenceType());
        conversation.setReferenceId(request.getReferenceId());
        conversation.setMessageCount(0L);
        conversation.setIsArchived(0);
        conversation.setIsPinned(0);

        // Set participant IDs for DIRECT conversations (optimization)
        if (CONVERSATION_TYPE_DIRECT.equals(request.getConversationType())) {
            conversation.setParticipant1Id(request.getParticipantIds().get(0));
            conversation.setParticipant2Id(request.getParticipantIds().get(1));
        }

        // Save conversation first to get ID
        conversation = conversationRepository.save(conversation);

        // Create participants
        Instant now = Instant.now();
        for (int i = 0; i < request.getParticipantIds().size(); i++) {
            String participantUserId = request.getParticipantIds().get(i);
            
            ConversationParticipant participant = new ConversationParticipant();
            participant.setConversation(conversation);
            participant.setUserId(participantUserId);
            participant.setStatus(PARTICIPANT_STATUS_ACTIVE);
            participant.setUnreadCount(0);
            participant.setIsMuted(0);
            participant.setIsPinned(0);
            participant.setJoinedAt(now);
            participant.setAddedBy(creatorId);
            
            // First participant (creator) is OWNER, others are MEMBER
            if (participantUserId.equals(creatorId)) {
                participant.setRole(PARTICIPANT_ROLE_OWNER);
            } else {
                participant.setRole(PARTICIPANT_ROLE_MEMBER);
            }
            
            participantRepository.save(participant);
        }

        LOG.debug("Created conversation with ID: {}", conversation.getConversationId());
        return conversationMapper.toDto(conversation);
    }

    /**
     * Get all conversations for a user where they are an ACTIVE participant.
     * Results are ordered by last_message_at descending.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of conversation DTOs
     */
    @Transactional(readOnly = true)
    public Page<ConversationDTO> getUserConversations(String userId, Pageable pageable) {
        LOG.debug("Request to get conversations for user: {}", userId);
        return conversationRepository.findUserConversations(userId, pageable)
            .map(conversationMapper::toDto);
    }
    /**
     * Search conversations by keyword in name or last_message_preview.
     * Only returns conversations where the user is an ACTIVE participant.
     *
     * @param userId the user ID
     * @param keyword the search keyword
     * @param pageable pagination information
     * @return page of conversation DTOs matching the keyword
     */
    @Transactional(readOnly = true)
    public Page<ConversationDTO> searchUserConversations(String userId, String keyword, Pageable pageable) {
        LOG.debug("Request to search conversations for user: {}, keyword: {}", userId, keyword);
        return conversationRepository.searchUserConversations(userId, keyword, pageable)
            .map(conversationMapper::toDto);
    }

    /**
     * Get conversation by ID if the user is a participant.
     *
     * @param conversationId the conversation ID
     * @param userId the user ID requesting access
     * @return the conversation DTO if found and user is participant
     * @throws IllegalArgumentException if user is not a participant
     */
    @Transactional(readOnly = true)
    public Optional<ConversationDTO> getConversationById(String conversationId, String userId) {
        LOG.debug("Request to get Conversation : {}, user: {}", conversationId, userId);
        
        // Check if user is participant
        if (!isUserParticipant(conversationId, userId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }
        
        return conversationRepository.findById(conversationId)
            .map(conversationMapper::toDto);
    }

    /**
     * Check if a user is an ACTIVE participant of a conversation.
     *
     * @param conversationId the conversation ID
     * @param userId the user ID
     * @return true if user is an ACTIVE participant, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isUserParticipant(String conversationId, String userId) {
        return participantRepository.existsByConversation_ConversationIdAndUserIdAndStatus(
            conversationId, 
            userId, 
            PARTICIPANT_STATUS_ACTIVE
        );
    }
    
    /**
     * Add a participant to a conversation.
     * Validates that the requesting user has OWNER or ADMIN role.
     * Validates that the target user is not already a participant.
     * Sends a system message to notify all participants.
     *
     * @param conversationId the conversation ID
     * @param targetUserId the user ID to add as participant
     * @param requestingUserId the user ID making the request
     * @throws IllegalArgumentException if validation fails
     */
    public void addParticipant(String conversationId, String targetUserId, String requestingUserId) {
        LOG.debug("Request to add participant: conversation={}, target={}, requester={}", 
            conversationId, targetUserId, requestingUserId);
        
        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));
        
        // Validate requesting user is a participant with OWNER or ADMIN role
        ConversationParticipant requester = participantRepository
            .findByConversation_ConversationIdAndUserId(conversationId, requestingUserId)
            .orElseThrow(() -> new IllegalArgumentException("Requesting user is not a participant"));
        
        String requesterRole = requester.getRole();
        if (!PARTICIPANT_ROLE_OWNER.equals(requesterRole) && !"ADMIN".equals(requesterRole)) {
            throw new IllegalArgumentException("Only OWNER or ADMIN can add participants");
        }
        
        // Check if target user is already a participant
        boolean alreadyParticipant = participantRepository
            .existsByConversation_ConversationIdAndUserIdAndStatus(conversationId, targetUserId, PARTICIPANT_STATUS_ACTIVE);
        
        if (alreadyParticipant) {
            throw new IllegalArgumentException("User is already a participant of this conversation");
        }
        
        // Create new participant
        ConversationParticipant newParticipant = new ConversationParticipant();
        newParticipant.setConversation(conversation);
        newParticipant.setUserId(targetUserId);
        newParticipant.setStatus(PARTICIPANT_STATUS_ACTIVE);
        newParticipant.setRole(PARTICIPANT_ROLE_MEMBER);
        newParticipant.setUnreadCount(0);
        newParticipant.setIsMuted(0);
        newParticipant.setIsPinned(0);
        newParticipant.setJoinedAt(Instant.now());
        newParticipant.setAddedBy(requestingUserId);
        
        participantRepository.save(newParticipant);
        
        // Create and send system message to notify participants
        Message systemMessage = new Message();
        systemMessage.setMessageId(UUID.randomUUID().toString());
        systemMessage.setConversation(conversation);
        systemMessage.setSenderId("SYSTEM");
        systemMessage.setSenderName("System");
        systemMessage.setMessageType("SYSTEM");
        systemMessage.setContent(String.format("User %s was added to the conversation by %s", 
            targetUserId, requester.getDisplayName() != null ? requester.getDisplayName() : requestingUserId));
        systemMessage.setStatus("SENT");
        systemMessage.setIsRead(false);
        systemMessage.setIsEdited(0);
        systemMessage.setIsDeleted(0);
        systemMessage.setReactionCount(0);
        
        messageRepository.save(systemMessage);
        
        // Broadcast system message to all participants
        messagingTemplate.convertAndSend(
            "/topic/conversation." + conversationId,
            systemMessage
        );
        
        LOG.debug("Added participant: conversation={}, user={}", conversationId, targetUserId);
    }
    
    /**
     * Get unread message count for a user in a conversation.
     *
     * @param conversationId the conversation ID
     * @param userId the user ID
     * @return the unread count
     * @throws IllegalArgumentException if user is not a participant
     */
    @Transactional(readOnly = true)
    public Integer getUnreadCount(String conversationId, String userId) {
        LOG.debug("Request to get unread count: conversation={}, user={}", conversationId, userId);
        
        ConversationParticipant participant = participantRepository
            .findByConversation_ConversationIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new IllegalArgumentException("User is not a participant of this conversation"));
        
        return participant.getUnreadCount() != null ? participant.getUnreadCount() : 0;
    }

    /**
     * Update conversation metadata when a new message is sent.
     * Updates last_message_at, last_message_preview, last_message_sender_id, last_message_id,
     * and increments message_count.
     *
     * @param conversationId the conversation ID
     * @param messageId the new message ID
     * @param preview the message preview (first 200 chars)
     * @param senderId the sender user ID
     * @param timestamp the message timestamp
     */
    public void updateLastMessage(
        String conversationId,
        String messageId,
        String preview,
        String senderId,
        Instant timestamp
    ) {
        LOG.debug("Updating last message for conversation: {}", conversationId);
        
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setLastMessageId(messageId);
            conversation.setLastMessagePreview(preview != null && preview.length() > 200 
                ? preview.substring(0, 200) 
                : preview);
            conversation.setLastMessageSenderId(senderId);
            conversation.setLastMessageAt(timestamp);
            
            // Increment message count
            Long currentCount = conversation.getMessageCount();
            conversation.setMessageCount(currentCount != null ? currentCount + 1 : 1L);
            
            conversationRepository.save(conversation);
        });
    }

    /**
     * Save a conversation.
     *
     * @param conversationDTO the entity to save.
     * @return the persisted entity.
     */
    public ConversationDTO save(ConversationDTO conversationDTO) {
        LOG.debug("Request to save Conversation : {}", conversationDTO);
        Conversation conversation = conversationMapper.toEntity(conversationDTO);
        conversation = conversationRepository.save(conversation);
        return conversationMapper.toDto(conversation);
    }

    /**
     * Update a conversation.
     *
     * @param conversationDTO the entity to save.
     * @return the persisted entity.
     */
    public ConversationDTO update(ConversationDTO conversationDTO) {
        LOG.debug("Request to update Conversation : {}", conversationDTO);
        Conversation conversation = conversationMapper.toEntity(conversationDTO);
        conversation.setIsPersisted();
        conversation = conversationRepository.save(conversation);
        return conversationMapper.toDto(conversation);
    }

    /**
     * Partially update a conversation.
     *
     * @param conversationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ConversationDTO> partialUpdate(ConversationDTO conversationDTO) {
        LOG.debug("Request to partially update Conversation : {}", conversationDTO);

        return conversationRepository
            .findById(conversationDTO.getConversationId())
            .map(existingConversation -> {
                conversationMapper.partialUpdate(existingConversation, conversationDTO);

                return existingConversation;
            })
            .map(conversationRepository::save)
            .map(conversationMapper::toDto);
    }

    /**
     * Get all the conversations.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ConversationDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Conversations");
        return conversationRepository.findAll(pageable).map(conversationMapper::toDto);
    }

    /**
     * Get one conversation by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ConversationDTO> findOne(String id) {
        LOG.debug("Request to get Conversation : {}", id);
        return conversationRepository.findById(id).map(conversationMapper::toDto);
    }

    /**
     * Delete the conversation by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete Conversation : {}", id);
        conversationRepository.deleteById(id);
    }
}
