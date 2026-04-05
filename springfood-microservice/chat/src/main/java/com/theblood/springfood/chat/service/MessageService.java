package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReaction;
import com.theblood.springfood.chat.domain.MessageReadReceipt;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.repository.MessageReactionRepository;
import com.theblood.springfood.chat.repository.MessageReadReceiptRepository;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.service.dto.MessageDTO;
import com.theblood.springfood.chat.service.mapper.MessageMapper;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.Message}.
 */
@Service
@Transactional
public class MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);
    private static final String PARTICIPANT_STATUS_ACTIVE = "ACTIVE";

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final MessageReactionRepository reactionRepository;
    private final MessageReadReceiptRepository readReceiptRepository;
    private final ConversationParticipantRepository participantRepository;
    private final ConversationService conversationService;

    public MessageService(
        MessageRepository messageRepository,
        MessageMapper messageMapper,
        MessageReactionRepository reactionRepository,
        MessageReadReceiptRepository readReceiptRepository,
        ConversationParticipantRepository participantRepository,
        ConversationService conversationService
    ) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.reactionRepository = reactionRepository;
        this.readReceiptRepository = readReceiptRepository;
        this.participantRepository = participantRepository;
        this.conversationService = conversationService;
    }

    /**
     * Get message history for a conversation with pagination.
     * Only returns non-deleted messages, ordered by created date descending.
     * Validates that the user is an ACTIVE participant of the conversation.
     *
     * @param conversationId the conversation ID
     * @param userId the user ID requesting the history
     * @param pageable pagination information (max 50 messages per page)
     * @return page of message DTOs
     * @throws IllegalArgumentException if user is not an ACTIVE participant
     */
    @Transactional(readOnly = true)
    public Page<MessageDTO> getMessageHistory(String conversationId, String userId, Pageable pageable) {
        LOG.debug("Request to get message history for conversation: {}, user: {}", conversationId, userId);
        
        // Validate user is an ACTIVE participant
        if (!conversationService.isUserParticipant(conversationId, userId)) {
            throw new IllegalArgumentException("User is not an ACTIVE participant of this conversation");
        }
        
        // Enforce maximum page size of 50
        int pageSize = Math.min(pageable.getPageSize(), 50);
        Pageable limitedPageable = Pageable.ofSize(pageSize).withPage(pageable.getPageNumber());
        
        // Query messages excluding deleted ones, ordered by created date descending
        return messageRepository.findMessageHistory(conversationId, limitedPageable)
            .map(messageMapper::toDto);
    }

    /**
     * Add a reaction to a message.
     * Implements toggle behavior: if the same reaction exists, it will be removed.
     * Updates the message reaction count accordingly.
     *
     * @param messageId the message ID
     * @param userId the user ID adding the reaction
     * @param emoji the emoji code (e.g., "thumbs_up", "heart")
     * @throws IllegalArgumentException if message doesn't exist or user is not a participant
     */
    public void addReaction(String messageId, String userId, String emoji) {
        LOG.debug("Request to add reaction to message: {}, user: {}, emoji: {}", messageId, userId, emoji);
        
        // Find the message
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        // Validate user is a participant of the conversation
        if (!conversationService.isUserParticipant(message.getConversation().getConversationId(), userId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }
        
        // Check if reaction already exists (toggle behavior)
        Optional<MessageReaction> existingReaction = reactionRepository
            .findByMessage_MessageIdAndUserIdAndEmoji(messageId, userId, emoji);
        
        if (existingReaction.isPresent()) {
            // Remove existing reaction (toggle off)
            reactionRepository.delete(existingReaction.get());
            
            // Decrement reaction count
            Integer currentCount = message.getReactionCount();
            message.setReactionCount(currentCount != null && currentCount > 0 ? currentCount - 1 : 0);
            messageRepository.save(message);
            
            LOG.debug("Removed reaction (toggle): messageId={}, userId={}, emoji={}", messageId, userId, emoji);
        } else {
            // Add new reaction
            MessageReaction reaction = new MessageReaction();
            reaction.setMessage(message);
            reaction.setUserId(userId);
            reaction.setEmoji(emoji);
            reactionRepository.save(reaction);
            
            // Increment reaction count
            Integer currentCount = message.getReactionCount();
            message.setReactionCount(currentCount != null ? currentCount + 1 : 1);
            messageRepository.save(message);
            
            LOG.debug("Added reaction: messageId={}, userId={}, emoji={}", messageId, userId, emoji);
        }
    }

    /**
     * Remove a reaction from a message.
     * This operation is idempotent - if the reaction doesn't exist, no error is thrown.
     *
     * @param messageId the message ID
     * @param userId the user ID removing the reaction
     * @param emoji the emoji code
     */
    public void removeReaction(String messageId, String userId, String emoji) {
        LOG.debug("Request to remove reaction from message: {}, user: {}, emoji: {}", messageId, userId, emoji);
        
        // Find the message
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        // Validate user is a participant of the conversation
        if (!conversationService.isUserParticipant(message.getConversation().getConversationId(), userId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }
        
        // Find and remove the reaction if it exists (idempotent)
        Optional<MessageReaction> reaction = reactionRepository
            .findByMessage_MessageIdAndUserIdAndEmoji(messageId, userId, emoji);
        
        if (reaction.isPresent()) {
            reactionRepository.delete(reaction.get());
            
            // Decrement reaction count
            Integer currentCount = message.getReactionCount();
            message.setReactionCount(currentCount != null && currentCount > 0 ? currentCount - 1 : 0);
            messageRepository.save(message);
            
            LOG.debug("Removed reaction: messageId={}, userId={}, emoji={}", messageId, userId, emoji);
        } else {
            LOG.debug("Reaction not found (idempotent): messageId={}, userId={}, emoji={}", messageId, userId, emoji);
        }
    }

    /**
     * Process a read receipt for a conversation.
     * Updates the participant's last_read_message_id, last_read_at, and resets unread_count to 0.
     * Also creates/updates message_read_receipt records.
     *
     * @param conversationId the conversation ID
     * @param userId the user ID marking messages as read
     * @param lastReadMessageId the ID of the last message read
     * @throws IllegalArgumentException if user is not a participant or message doesn't exist
     */
    public void processReadReceipt(String conversationId, String userId, String lastReadMessageId) {
        LOG.debug("Request to process read receipt: conversation={}, user={}, lastReadMessageId={}", 
            conversationId, userId, lastReadMessageId);
        
        // Validate user is a participant
        if (!conversationService.isUserParticipant(conversationId, userId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }
        
        // Validate message exists and belongs to the conversation
        Message message = messageRepository.findById(lastReadMessageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + lastReadMessageId));
        
        if (!message.getConversation().getConversationId().equals(conversationId)) {
            throw new IllegalArgumentException("Message does not belong to this conversation");
        }
        
        // Update conversation participant
        ConversationParticipant participant = participantRepository
            .findByConversation_ConversationIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
        
        Instant now = Instant.now();
        participant.setLastReadMessageId(lastReadMessageId);
        participant.setLastReadAt(now);
        participant.setUnreadCount(0);
        participantRepository.save(participant);
        
        // Create or update message read receipt
        Optional<MessageReadReceipt> existingReceipt = readReceiptRepository
            .findByMessage_MessageIdAndUserId(lastReadMessageId, userId);
        
        if (existingReceipt.isPresent()) {
            MessageReadReceipt receipt = existingReceipt.get();
            receipt.setReadAt(now);
            readReceiptRepository.save(receipt);
        } else {
            MessageReadReceipt receipt = new MessageReadReceipt();
            receipt.setMessage(message);
            receipt.setUserId(userId);
            receipt.setReadAt(now);
            readReceiptRepository.save(receipt);
        }
        
        LOG.debug("Processed read receipt: conversation={}, user={}, unreadCount reset to 0", conversationId, userId);
    }

    /**
     * Save a message.
     *
     * @param messageDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageDTO save(MessageDTO messageDTO) {
        LOG.debug("Request to save Message : {}", messageDTO);
        Message message = messageMapper.toEntity(messageDTO);
        message = messageRepository.save(message);
        return messageMapper.toDto(message);
    }

    /**
     * Update a message.
     *
     * @param messageDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageDTO update(MessageDTO messageDTO) {
        LOG.debug("Request to update Message : {}", messageDTO);
        Message message = messageMapper.toEntity(messageDTO);
        message.setIsPersisted();
        message = messageRepository.save(message);
        return messageMapper.toDto(message);
    }

    /**
     * Partially update a message.
     *
     * @param messageDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MessageDTO> partialUpdate(MessageDTO messageDTO) {
        LOG.debug("Request to partially update Message : {}", messageDTO);

        return messageRepository
            .findById(messageDTO.getMessageId())
            .map(existingMessage -> {
                messageMapper.partialUpdate(existingMessage, messageDTO);

                return existingMessage;
            })
            .map(messageRepository::save)
            .map(messageMapper::toDto);
    }

    /**
     * Get all the messages.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MessageDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Messages");
        return messageRepository.findAll(pageable).map(messageMapper::toDto);
    }

    /**
     * Get one message by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MessageDTO> findOne(String id) {
        LOG.debug("Request to get Message : {}", id);
        return messageRepository.findById(id).map(messageMapper::toDto);
    }

    /**
     * Delete the message by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete Message : {}", id);
        messageRepository.deleteById(id);
    }
}
