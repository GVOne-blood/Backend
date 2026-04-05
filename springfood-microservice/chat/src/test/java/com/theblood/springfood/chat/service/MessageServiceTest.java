package com.theblood.springfood.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReaction;
import com.theblood.springfood.chat.domain.MessageReadReceipt;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.MessageReactionRepository;
import com.theblood.springfood.chat.repository.MessageReadReceiptRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.dto.MessageDTO;
import com.theblood.springfood.chat.service.mapper.MessageMapper;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

/**
 * Unit tests for {@link MessageService}.
 * Tests core message service functionality including message history retrieval,
 * reactions, and read receipts.
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private MessageReactionRepository reactionRepository;

    @Mock
    private MessageReadReceiptRepository readReceiptRepository;

    @Mock
    private ConversationParticipantRepository participantRepository;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private MessageService messageService;

    private static final String TEST_CONVERSATION_ID = "conv-123";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_MESSAGE_ID = "msg-123";
    private static final String TEST_EMOJI = "thumbs_up";

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    /**
     * Test: getMessageHistory for non-participant returns 403
     * Requirement: 6.1, 6.6
     * 
     * Validates that when a user who is not an ACTIVE participant of a conversation
     * attempts to retrieve message history, an IllegalArgumentException is thrown.
     */
    @Test
    void testGetMessageHistory_NonParticipant_ThrowsException() {
        // Given: User is not a participant
        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, TEST_USER_ID))
            .thenReturn(false);

        Pageable pageable = PageRequest.of(0, 20);

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> 
            messageService.getMessageHistory(TEST_CONVERSATION_ID, TEST_USER_ID, pageable)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not an ACTIVE participant");

        // Verify that repository was never called
        verify(messageRepository, never()).findMessageHistory(anyString(), any(Pageable.class));
    }

    /**
     * Test: addReaction to non-existent message throws exception
     * Requirement: 8.1
     * 
     * Validates that attempting to add a reaction to a message that doesn't exist
     * results in an IllegalArgumentException.
     */
    @Test
    void testAddReaction_NonExistentMessage_ThrowsException() {
        // Given: Message does not exist
        when(messageRepository.findById(TEST_MESSAGE_ID))
            .thenReturn(Optional.empty());

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> 
            messageService.addReaction(TEST_MESSAGE_ID, TEST_USER_ID, TEST_EMOJI)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Message not found");

        // Verify that no reaction was saved
        verify(reactionRepository, never()).save(any(MessageReaction.class));
    }

    /**
     * Test: removeReaction that doesn't exist is idempotent (no error)
     * Requirement: 8.1
     * 
     * Validates that removing a reaction that doesn't exist does not throw an error
     * and the operation completes successfully (idempotent behavior).
     */
    @Test
    void testRemoveReaction_NonExistentReaction_IsIdempotent() {
        // Given: Message exists and user is participant
        Message message = createTestMessage();
        when(messageRepository.findById(TEST_MESSAGE_ID))
            .thenReturn(Optional.of(message));
        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, TEST_USER_ID))
            .thenReturn(true);

        // Given: Reaction does not exist
        when(reactionRepository.findByMessage_MessageIdAndUserIdAndEmoji(
            TEST_MESSAGE_ID, TEST_USER_ID, TEST_EMOJI))
            .thenReturn(Optional.empty());

        // When: Remove non-existent reaction (should not throw)
        messageService.removeReaction(TEST_MESSAGE_ID, TEST_USER_ID, TEST_EMOJI);

        // Then: No exception thrown, operation is idempotent
        verify(reactionRepository, never()).delete(any(MessageReaction.class));
        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * Test: processReadReceipt resets unread_count to 0
     * Requirement: 7.5
     * 
     * Validates that when a read receipt is processed, the participant's unread_count
     * is reset to 0, last_read_message_id is updated, and last_read_at is set.
     */
    @Test
    void testProcessReadReceipt_ResetsUnreadCount() {
        // Given: User is participant
        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, TEST_USER_ID))
            .thenReturn(true);

        // Given: Message exists and belongs to conversation
        Message message = createTestMessage();
        when(messageRepository.findById(TEST_MESSAGE_ID))
            .thenReturn(Optional.of(message));

        // Given: Participant exists with unread count > 0
        ConversationParticipant participant = createTestParticipant();
        participant.setUnreadCount(5); // Has unread messages
        when(participantRepository.findByConversation_ConversationIdAndUserId(
            TEST_CONVERSATION_ID, TEST_USER_ID))
            .thenReturn(Optional.of(participant));

        // Given: No existing read receipt
        when(readReceiptRepository.findByMessage_MessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID))
            .thenReturn(Optional.empty());

        // When: Process read receipt
        messageService.processReadReceipt(TEST_CONVERSATION_ID, TEST_USER_ID, TEST_MESSAGE_ID);

        // Then: Participant unread count is reset to 0
        ArgumentCaptor<ConversationParticipant> participantCaptor = 
            ArgumentCaptor.forClass(ConversationParticipant.class);
        verify(participantRepository).save(participantCaptor.capture());
        
        ConversationParticipant savedParticipant = participantCaptor.getValue();
        assertThat(savedParticipant.getUnreadCount()).isEqualTo(0);
        assertThat(savedParticipant.getLastReadMessageId()).isEqualTo(TEST_MESSAGE_ID);
        assertThat(savedParticipant.getLastReadAt()).isNotNull();

        // Then: Read receipt is created
        ArgumentCaptor<MessageReadReceipt> receiptCaptor = 
            ArgumentCaptor.forClass(MessageReadReceipt.class);
        verify(readReceiptRepository).save(receiptCaptor.capture());
        
        MessageReadReceipt savedReceipt = receiptCaptor.getValue();
        assertThat(savedReceipt.getMessage()).isEqualTo(message);
        assertThat(savedReceipt.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(savedReceipt.getReadAt()).isNotNull();
    }

    // Helper methods to create test data

    private Message createTestMessage() {
        Message message = new Message();
        message.setMessageId(TEST_MESSAGE_ID);
        message.setSenderId("sender-123");
        message.setContent("Test message content");
        message.setReactionCount(0);
        
        Conversation conversation = new Conversation();
        conversation.setConversationId(TEST_CONVERSATION_ID);
        message.setConversation(conversation);
        
        return message;
    }

    private ConversationParticipant createTestParticipant() {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setParticipantId("participant-123");
        participant.setUserId(TEST_USER_ID);
        participant.setUnreadCount(0);
        
        Conversation conversation = new Conversation();
        conversation.setConversationId(TEST_CONVERSATION_ID);
        participant.setConversation(conversation);
        
        return participant;
    }
}
