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
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.mapper.ConversationMapper;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Unit tests for {@link ConversationService}.
 * Tests participant management functionality including adding participants
 * with proper authorization and validation.
 * 
 * Requirements: 14.4, 14.5, 14.6
 */
@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationParticipantRepository participantRepository;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ConversationService conversationService;

    private static final String TEST_CONVERSATION_ID = "conv-123";
    private static final String TEST_OWNER_ID = "owner-123";
    private static final String TEST_ADMIN_ID = "admin-123";
    private static final String TEST_MEMBER_ID = "member-123";
    private static final String TEST_TARGET_USER_ID = "target-456";

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    /**
     * Test: addParticipant as MEMBER returns authorization error
     * Requirement: 14.5
     * 
     * Validates that when a user with MEMBER role attempts to add a participant,
     * an IllegalArgumentException is thrown with appropriate error message.
     */
    @Test
    void testAddParticipant_AsMember_ThrowsAuthorizationError() {
        // Given: Conversation exists
        Conversation conversation = createTestConversation();
        when(conversationRepository.findById(TEST_CONVERSATION_ID))
            .thenReturn(Optional.of(conversation));

        // Given: Requesting user is a MEMBER (not OWNER or ADMIN)
        ConversationParticipant memberParticipant = createTestParticipant(TEST_MEMBER_ID, "MEMBER");
        when(participantRepository.findByConversation_ConversationIdAndUserId(
            TEST_CONVERSATION_ID, TEST_MEMBER_ID))
            .thenReturn(Optional.of(memberParticipant));

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> 
            conversationService.addParticipant(TEST_CONVERSATION_ID, TEST_TARGET_USER_ID, TEST_MEMBER_ID)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Only OWNER or ADMIN can add participants");

        // Verify that no participant was saved
        verify(participantRepository, never()).save(any(ConversationParticipant.class));
        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * Test: addParticipant as ADMIN succeeds
     * Requirement: 14.5
     * 
     * Validates that when a user with ADMIN role adds a participant,
     * the operation succeeds and creates the participant with correct attributes.
     */
    @Test
    void testAddParticipant_AsAdmin_Succeeds() {
        // Given: Conversation exists
        Conversation conversation = createTestConversation();
        when(conversationRepository.findById(TEST_CONVERSATION_ID))
            .thenReturn(Optional.of(conversation));

        // Given: Requesting user is an ADMIN
        ConversationParticipant adminParticipant = createTestParticipant(TEST_ADMIN_ID, "ADMIN");
        adminParticipant.setDisplayName("Admin User");
        when(participantRepository.findByConversation_ConversationIdAndUserId(
            TEST_CONVERSATION_ID, TEST_ADMIN_ID))
            .thenReturn(Optional.of(adminParticipant));

        // Given: Target user is not already a participant
        when(participantRepository.existsByConversation_ConversationIdAndUserIdAndStatus(
            TEST_CONVERSATION_ID, TEST_TARGET_USER_ID, "ACTIVE"))
            .thenReturn(false);

        // When: Add participant as ADMIN
        conversationService.addParticipant(TEST_CONVERSATION_ID, TEST_TARGET_USER_ID, TEST_ADMIN_ID);

        // Then: New participant is created with correct attributes
        ArgumentCaptor<ConversationParticipant> participantCaptor = 
            ArgumentCaptor.forClass(ConversationParticipant.class);
        verify(participantRepository).save(participantCaptor.capture());
        
        ConversationParticipant savedParticipant = participantCaptor.getValue();
        assertThat(savedParticipant.getUserId()).isEqualTo(TEST_TARGET_USER_ID);
        assertThat(savedParticipant.getStatus()).isEqualTo("ACTIVE");
        assertThat(savedParticipant.getRole()).isEqualTo("MEMBER");
        assertThat(savedParticipant.getUnreadCount()).isEqualTo(0);
        assertThat(savedParticipant.getJoinedAt()).isNotNull();
        assertThat(savedParticipant.getAddedBy()).isEqualTo(TEST_ADMIN_ID);

        // Then: System message is created
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        
        Message systemMessage = messageCaptor.getValue();
        assertThat(systemMessage.getSenderId()).isEqualTo("SYSTEM");
        assertThat(systemMessage.getMessageType()).isEqualTo("SYSTEM");
        assertThat(systemMessage.getContent()).contains(TEST_TARGET_USER_ID);
        assertThat(systemMessage.getContent()).contains("Admin User");

        // Then: System message is broadcast to conversation
        verify(messagingTemplate).convertAndSend(
            eq("/topic/conversation." + TEST_CONVERSATION_ID),
            any(Message.class)
        );
    }

    /**
     * Test: addParticipant for duplicate user returns validation error
     * Requirement: 14.6
     * 
     * Validates that attempting to add a user who is already an ACTIVE participant
     * results in an IllegalArgumentException.
     */
    @Test
    void testAddParticipant_DuplicateUser_ThrowsValidationError() {
        // Given: Conversation exists
        Conversation conversation = createTestConversation();
        when(conversationRepository.findById(TEST_CONVERSATION_ID))
            .thenReturn(Optional.of(conversation));

        // Given: Requesting user is an OWNER
        ConversationParticipant ownerParticipant = createTestParticipant(TEST_OWNER_ID, "OWNER");
        when(participantRepository.findByConversation_ConversationIdAndUserId(
            TEST_CONVERSATION_ID, TEST_OWNER_ID))
            .thenReturn(Optional.of(ownerParticipant));

        // Given: Target user is already an ACTIVE participant
        when(participantRepository.existsByConversation_ConversationIdAndUserIdAndStatus(
            TEST_CONVERSATION_ID, TEST_TARGET_USER_ID, "ACTIVE"))
            .thenReturn(true);

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> 
            conversationService.addParticipant(TEST_CONVERSATION_ID, TEST_TARGET_USER_ID, TEST_OWNER_ID)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already a participant");

        // Verify that no participant was saved
        verify(participantRepository, never()).save(any(ConversationParticipant.class));
        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * Test: system message created when participant added
     * Requirement: 14.4
     * 
     * Validates that when a participant is successfully added, a system message
     * is created and broadcast to notify all participants in the conversation.
     */
    @Test
    void testAddParticipant_CreatesSystemMessage() {
        // Given: Conversation exists
        Conversation conversation = createTestConversation();
        when(conversationRepository.findById(TEST_CONVERSATION_ID))
            .thenReturn(Optional.of(conversation));

        // Given: Requesting user is an OWNER
        ConversationParticipant ownerParticipant = createTestParticipant(TEST_OWNER_ID, "OWNER");
        ownerParticipant.setDisplayName("Owner User");
        when(participantRepository.findByConversation_ConversationIdAndUserId(
            TEST_CONVERSATION_ID, TEST_OWNER_ID))
            .thenReturn(Optional.of(ownerParticipant));

        // Given: Target user is not already a participant
        when(participantRepository.existsByConversation_ConversationIdAndUserIdAndStatus(
            TEST_CONVERSATION_ID, TEST_TARGET_USER_ID, "ACTIVE"))
            .thenReturn(false);

        // When: Add participant
        conversationService.addParticipant(TEST_CONVERSATION_ID, TEST_TARGET_USER_ID, TEST_OWNER_ID);

        // Then: System message is created with correct attributes
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        
        Message systemMessage = messageCaptor.getValue();
        assertThat(systemMessage.getMessageId()).isNotNull();
        assertThat(systemMessage.getSenderId()).isEqualTo("SYSTEM");
        assertThat(systemMessage.getSenderName()).isEqualTo("System");
        assertThat(systemMessage.getMessageType()).isEqualTo("SYSTEM");
        assertThat(systemMessage.getStatus()).isEqualTo("SENT");
        assertThat(systemMessage.getContent())
            .contains(TEST_TARGET_USER_ID)
            .contains("Owner User");
        assertThat(systemMessage.getIsRead()).isFalse();
        assertThat(systemMessage.getIsEdited()).isEqualTo(0);
        assertThat(systemMessage.getIsDeleted()).isEqualTo(0);
        assertThat(systemMessage.getReactionCount()).isEqualTo(0);

        // Then: System message is broadcast to all participants
        verify(messagingTemplate).convertAndSend(
            eq("/topic/conversation." + TEST_CONVERSATION_ID),
            eq(systemMessage)
        );
    }

    // Helper methods to create test data

    private Conversation createTestConversation() {
        Conversation conversation = new Conversation();
        conversation.setConversationId(TEST_CONVERSATION_ID);
        conversation.setConversationType("GROUP");
        conversation.setName("Test Conversation");
        conversation.setMessageCount(0L);
        return conversation;
    }

    private ConversationParticipant createTestParticipant(String userId, String role) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setParticipantId("participant-" + userId);
        participant.setUserId(userId);
        participant.setRole(role);
        participant.setStatus("ACTIVE");
        participant.setUnreadCount(0);
        participant.setJoinedAt(Instant.now());
        
        Conversation conversation = new Conversation();
        conversation.setConversationId(TEST_CONVERSATION_ID);
        participant.setConversation(conversation);
        
        return participant;
    }
}
