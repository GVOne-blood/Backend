package com.theblood.springfood.chat.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.service.ConversationService;
import com.theblood.springfood.chat.service.MessageService;
import com.theblood.springfood.chat.service.dto.*;
import java.security.Principal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for {@link ConversationRestController}.
 * Tests all REST endpoints for conversation management.
 */
@WebMvcTest(ConversationRestController.class)
class ConversationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private MessageService messageService;

    private ConversationDTO testConversation;
    private MessageDTO testMessage;

    @BeforeEach
    void setUp() {
        testConversation = new ConversationDTO();
        testConversation.setConversationId("conv-123");
        testConversation.setConversationType("DIRECT");
        testConversation.setName("Test Conversation");
        testConversation.setLastMessagePreview("Hello world");
        testConversation.setLastMessageAt(Instant.now());
        testConversation.setMessageCount(5L);

        testMessage = new MessageDTO();
        testMessage.setMessageId("msg-123");
        testMessage.setConversationId("conv-123");
        testMessage.setSenderId("user1");
        testMessage.setContent("Test message");
        testMessage.setCreatedAt(Instant.now());
    }

    @Test
    @WithMockUser(username = "user1")
    void getUserConversations_shouldReturnConversationsOrderedByLastMessageAt() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ConversationDTO> page = new PageImpl<>(Arrays.asList(testConversation), pageable, 1);
        when(conversationService.getUserConversations(eq("user1"), any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc
            .perform(get("/api/conversations").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].conversationId").value("conv-123"))
            .andExpect(jsonPath("$[0].name").value("Test Conversation"));

        verify(conversationService).getUserConversations(eq("user1"), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void searchConversations_shouldReturnMatchingConversations() throws Exception {
        // Given
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 20);
        Page<ConversationDTO> page = new PageImpl<>(Arrays.asList(testConversation), pageable, 1);
        when(conversationService.searchUserConversations(eq("user1"), eq(keyword), any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc
            .perform(get("/api/conversations/search").param("keyword", keyword).param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].conversationId").value("conv-123"));

        verify(conversationService).searchUserConversations(eq("user1"), eq(keyword), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void searchConversations_withEmptyKeyword_shouldReturnBadRequest() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/conversations/search").param("keyword", "")).andExpect(status().isBadRequest());

        verify(conversationService, never()).searchUserConversations(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void getConversation_whenUserIsParticipant_shouldReturnConversation() throws Exception {
        // Given
        when(conversationService.getConversationById("conv-123", "user1")).thenReturn(Optional.of(testConversation));

        // When/Then
        mockMvc
            .perform(get("/api/conversations/conv-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.conversationId").value("conv-123"))
            .andExpect(jsonPath("$.name").value("Test Conversation"));

        verify(conversationService).getConversationById("conv-123", "user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void getConversation_whenUserIsNotParticipant_shouldReturnForbidden() throws Exception {
        // Given
        when(conversationService.getConversationById("conv-123", "user1")).thenThrow(
            new IllegalArgumentException("User is not a participant of this conversation")
        );

        // When/Then
        mockMvc.perform(get("/api/conversations/conv-123")).andExpect(status().isForbidden());

        verify(conversationService).getConversationById("conv-123", "user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void createConversation_withValidData_shouldReturnCreated() throws Exception {
        // Given
        CreateConversationRequest request = new CreateConversationRequest();
        request.setConversationType("DIRECT");
        request.setParticipantIds(Arrays.asList("user1", "user2"));

        when(conversationService.createConversation(any(CreateConversationRequest.class), eq("user1"))).thenReturn(testConversation);

        // When/Then
        mockMvc
            .perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.conversationId").value("conv-123"));

        verify(conversationService).createConversation(any(CreateConversationRequest.class), eq("user1"));
    }

    @Test
    @WithMockUser(username = "user1")
    void createConversation_withInvalidParticipantCount_shouldReturnBadRequest() throws Exception {
        // Given
        CreateConversationRequest request = new CreateConversationRequest();
        request.setConversationType("DIRECT");
        request.setParticipantIds(Arrays.asList("user1", "user2", "user3")); // 3 participants for DIRECT

        when(conversationService.createConversation(any(CreateConversationRequest.class), eq("user1"))).thenThrow(
            new IllegalArgumentException("DIRECT conversation must have exactly 2 participants")
        );

        // When/Then
        mockMvc
            .perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(conversationService).createConversation(any(CreateConversationRequest.class), eq("user1"));
    }

    @Test
    @WithMockUser(username = "user1")
    void getMessages_whenUserIsParticipant_shouldReturnMessages() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 50);
        Page<MessageDTO> page = new PageImpl<>(Arrays.asList(testMessage), pageable, 1);
        when(messageService.getMessageHistory(eq("conv-123"), eq("user1"), any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc
            .perform(get("/api/conversations/conv-123/messages").param("page", "0").param("size", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].messageId").value("msg-123"))
            .andExpect(jsonPath("$[0].content").value("Test message"));

        verify(messageService).getMessageHistory(eq("conv-123"), eq("user1"), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void getMessages_whenUserIsNotParticipant_shouldReturnForbidden() throws Exception {
        // Given
        when(messageService.getMessageHistory(eq("conv-123"), eq("user1"), any(Pageable.class))).thenThrow(
            new IllegalArgumentException("User is not an ACTIVE participant of this conversation")
        );

        // When/Then
        mockMvc.perform(get("/api/conversations/conv-123/messages")).andExpect(status().isForbidden());

        verify(messageService).getMessageHistory(eq("conv-123"), eq("user1"), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void addParticipant_asAdmin_shouldSucceed() throws Exception {
        // Given
        AddParticipantRequest request = new AddParticipantRequest("user3");
        doNothing().when(conversationService).addParticipant("conv-123", "user3", "user1");

        // When/Then
        mockMvc
            .perform(
                post("/api/conversations/conv-123/participants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());

        verify(conversationService).addParticipant("conv-123", "user3", "user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void addParticipant_asMember_shouldReturnForbidden() throws Exception {
        // Given
        AddParticipantRequest request = new AddParticipantRequest("user3");
        doThrow(new IllegalArgumentException("Only OWNER or ADMIN can add participants"))
            .when(conversationService)
            .addParticipant("conv-123", "user3", "user1");

        // When/Then
        mockMvc
            .perform(
                post("/api/conversations/conv-123/participants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isForbidden());

        verify(conversationService).addParticipant("conv-123", "user3", "user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void addParticipant_duplicateUser_shouldReturnBadRequest() throws Exception {
        // Given
        AddParticipantRequest request = new AddParticipantRequest("user2");
        doThrow(new IllegalArgumentException("User is already a participant of this conversation"))
            .when(conversationService)
            .addParticipant("conv-123", "user2", "user1");

        // When/Then
        mockMvc
            .perform(
                post("/api/conversations/conv-123/participants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());

        verify(conversationService).addParticipant("conv-123", "user2", "user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void getUnreadCount_shouldReturnCount() throws Exception {
        // Given
        when(conversationService.getUnreadCount("conv-123", "user1")).thenReturn(5);

        // When/Then
        mockMvc
            .perform(get("/api/conversations/conv-123/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.conversationId").value("conv-123"))
            .andExpect(jsonPath("$.unreadCount").value(5));

        verify(conversationService).getUnreadCount("conv-123", "user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void getUnreadCount_whenUserIsNotParticipant_shouldReturnForbidden() throws Exception {
        // Given
        when(conversationService.getUnreadCount("conv-123", "user1")).thenThrow(
            new IllegalArgumentException("User is not a participant of this conversation")
        );

        // When/Then
        mockMvc.perform(get("/api/conversations/conv-123/unread-count")).andExpect(status().isForbidden());

        verify(conversationService).getUnreadCount("conv-123", "user1");
    }

    @Test
    void getUserConversations_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/conversations")).andExpect(status().isUnauthorized());

        verify(conversationService, never()).getUserConversations(anyString(), any(Pageable.class));
    }
}
