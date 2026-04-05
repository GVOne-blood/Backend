package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.ConversationAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.service.dto.ConversationDTO;
import com.theblood.springfood.chat.service.mapper.ConversationMapper;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ConversationResource} REST resources.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ConversationResourceIT {

    private static final String DEFAULT_CONVERSATION_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_CONVERSATION_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_AVATAR_URL = "AAAAAAAAAA";
    private static final String UPDATED_AVATAR_URL = "BBBBBBBBBB";

    private static final String DEFAULT_REFERENCE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_REFERENCE_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_REFERENCE_ID = "AAAAAAAAAA";
    private static final String UPDATED_REFERENCE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_MESSAGE_PREVIEW = "AAAAAAAAAA";
    private static final String UPDATED_LAST_MESSAGE_PREVIEW = "BBBBBBBBBB";

    private static final Instant DEFAULT_LAST_MESSAGE_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_MESSAGE_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_LAST_MESSAGE_SENDER_ID = "AAAAAAAAAA";
    private static final String UPDATED_LAST_MESSAGE_SENDER_ID = "BBBBBBBBBB";

    private static final Long DEFAULT_MESSAGE_COUNT = 1L;
    private static final Long UPDATED_MESSAGE_COUNT = 2L;

    private static final Integer DEFAULT_IS_ARCHIVED = 1;
    private static final Integer UPDATED_IS_ARCHIVED = 2;

    private static final Integer DEFAULT_IS_PINNED = 1;
    private static final Integer UPDATED_IS_PINNED = 2;

    private static final String ENTITY_API_URL = "/api/conversations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{conversationId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restConversationMockMvc;

    private Conversation conversation;

    private Conversation insertedConversation;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Conversation createEntity() {
        return new Conversation()
            .conversationId(UUID.randomUUID().toString())
            .conversationType(DEFAULT_CONVERSATION_TYPE)
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .avatarUrl(DEFAULT_AVATAR_URL)
            .referenceType(DEFAULT_REFERENCE_TYPE)
            .referenceId(DEFAULT_REFERENCE_ID)
            .lastMessagePreview(DEFAULT_LAST_MESSAGE_PREVIEW)
            .lastMessageAt(DEFAULT_LAST_MESSAGE_AT)
            .lastMessageSenderId(DEFAULT_LAST_MESSAGE_SENDER_ID)
            .messageCount(DEFAULT_MESSAGE_COUNT)
            .isArchived(DEFAULT_IS_ARCHIVED)
            .isPinned(DEFAULT_IS_PINNED);
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Conversation createUpdatedEntity() {
        return new Conversation()
            .conversationId(UUID.randomUUID().toString())
            .conversationType(UPDATED_CONVERSATION_TYPE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .avatarUrl(UPDATED_AVATAR_URL)
            .referenceType(UPDATED_REFERENCE_TYPE)
            .referenceId(UPDATED_REFERENCE_ID)
            .lastMessagePreview(UPDATED_LAST_MESSAGE_PREVIEW)
            .lastMessageAt(UPDATED_LAST_MESSAGE_AT)
            .lastMessageSenderId(UPDATED_LAST_MESSAGE_SENDER_ID)
            .messageCount(UPDATED_MESSAGE_COUNT)
            .isArchived(UPDATED_IS_ARCHIVED)
            .isPinned(UPDATED_IS_PINNED);
    }

    @BeforeEach
    void initTest() {
        conversation = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedConversation != null) {
            conversationRepository.delete(insertedConversation);
            insertedConversation = null;
        }
    }

    @Test
    @Transactional
    void createConversation() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Conversation
        ConversationDTO conversationDTO = conversationMapper.toDto(conversation);
        var returnedConversationDTO = om.readValue(
            restConversationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ConversationDTO.class
        );

        // Validate the Conversation in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedConversation = conversationMapper.toEntity(returnedConversationDTO);
        assertConversationUpdatableFieldsEquals(returnedConversation, getPersistedConversation(returnedConversation));

        insertedConversation = returnedConversation;
    }

    @Test
    @Transactional
    void createConversationWithExistingId() throws Exception {
        // Create the Conversation with an existing ID
        insertedConversation = conversationRepository.saveAndFlush(conversation);
        ConversationDTO conversationDTO = conversationMapper.toDto(conversation);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restConversationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Conversation in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkConversationTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        conversation.setConversationType(null);

        // Create the Conversation, which fails.
        ConversationDTO conversationDTO = conversationMapper.toDto(conversation);

        restConversationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllConversations() throws Exception {
        // Initialize the database
        conversation.setConversationId(UUID.randomUUID().toString());
        insertedConversation = conversationRepository.saveAndFlush(conversation);

        // Get all the conversationList
        restConversationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=conversationId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].conversationId").value(hasItem(conversation.getConversationId())))
            .andExpect(jsonPath("$.[*].conversationType").value(hasItem(DEFAULT_CONVERSATION_TYPE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].avatarUrl").value(hasItem(DEFAULT_AVATAR_URL)))
            .andExpect(jsonPath("$.[*].referenceType").value(hasItem(DEFAULT_REFERENCE_TYPE)))
            .andExpect(jsonPath("$.[*].referenceId").value(hasItem(DEFAULT_REFERENCE_ID)))
            .andExpect(jsonPath("$.[*].lastMessagePreview").value(hasItem(DEFAULT_LAST_MESSAGE_PREVIEW)))
            .andExpect(jsonPath("$.[*].lastMessageAt").value(hasItem(DEFAULT_LAST_MESSAGE_AT.toString())))
            .andExpect(jsonPath("$.[*].lastMessageSenderId").value(hasItem(DEFAULT_LAST_MESSAGE_SENDER_ID)))
            .andExpect(jsonPath("$.[*].messageCount").value(hasItem(DEFAULT_MESSAGE_COUNT.intValue())))
            .andExpect(jsonPath("$.[*].isArchived").value(hasItem(DEFAULT_IS_ARCHIVED)))
            .andExpect(jsonPath("$.[*].isPinned").value(hasItem(DEFAULT_IS_PINNED)));
    }

    @Test
    @Transactional
    void getConversation() throws Exception {
        // Initialize the database
        conversation.setConversationId(UUID.randomUUID().toString());
        insertedConversation = conversationRepository.saveAndFlush(conversation);

        // Get the conversation
        restConversationMockMvc
            .perform(get(ENTITY_API_URL_ID, conversation.getConversationId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.conversationId").value(conversation.getConversationId()))
            .andExpect(jsonPath("$.conversationType").value(DEFAULT_CONVERSATION_TYPE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.avatarUrl").value(DEFAULT_AVATAR_URL))
            .andExpect(jsonPath("$.referenceType").value(DEFAULT_REFERENCE_TYPE))
            .andExpect(jsonPath("$.referenceId").value(DEFAULT_REFERENCE_ID))
            .andExpect(jsonPath("$.lastMessagePreview").value(DEFAULT_LAST_MESSAGE_PREVIEW))
            .andExpect(jsonPath("$.lastMessageAt").value(DEFAULT_LAST_MESSAGE_AT.toString()))
            .andExpect(jsonPath("$.lastMessageSenderId").value(DEFAULT_LAST_MESSAGE_SENDER_ID))
            .andExpect(jsonPath("$.messageCount").value(DEFAULT_MESSAGE_COUNT.intValue()))
            .andExpect(jsonPath("$.isArchived").value(DEFAULT_IS_ARCHIVED))
            .andExpect(jsonPath("$.isPinned").value(DEFAULT_IS_PINNED));
    }

    @Test
    @Transactional
    void getNonExistingConversation() throws Exception {
        // Get the conversation
        restConversationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingConversation() throws Exception {
        // Initialize the database
        conversation.setConversationId(UUID.randomUUID().toString());
        insertedConversation = conversationRepository.saveAndFlush(conversation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the conversation
        Conversation updatedConversation = conversationRepository.findById(conversation.getConversationId()).orElseThrow();
        // Disconnect from session so that the updates on updatedConversation are not directly saved in db
        em.detach(updatedConversation);
        updatedConversation
            .conversationType(UPDATED_CONVERSATION_TYPE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .avatarUrl(UPDATED_AVATAR_URL)
            .referenceType(UPDATED_REFERENCE_TYPE)
            .referenceId(UPDATED_REFERENCE_ID)
            .lastMessagePreview(UPDATED_LAST_MESSAGE_PREVIEW)
            .lastMessageAt(UPDATED_LAST_MESSAGE_AT)
            .lastMessageSenderId(UPDATED_LAST_MESSAGE_SENDER_ID)
            .messageCount(UPDATED_MESSAGE_COUNT)
            .isArchived(UPDATED_IS_ARCHIVED)
            .isPinned(UPDATED_IS_PINNED);
        ConversationDTO conversationDTO = conversationMapper.toDto(updatedConversation);

        restConversationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, conversationDTO.getConversationId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(conversationDTO))
            )
            .andExpect(status().isOk());

        // Validate the Conversation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedConversationToMatchAllProperties(updatedConversation);
    }

    @Test
    @Transactional
    void putNonExistingConversation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversation.setConversationId(UUID.randomUUID().toString());

        // Create the Conversation
        ConversationDTO conversationDTO = conversationMapper.toDto(conversation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConversationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, conversationDTO.getConversationId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(conversationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Conversation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchConversation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversation.setConversationId(UUID.randomUUID().toString());

        // Create the Conversation
        ConversationDTO conversationDTO = conversationMapper.toDto(conversation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(conversationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Conversation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamConversation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversation.setConversationId(UUID.randomUUID().toString());

        // Create the Conversation
        ConversationDTO conversationDTO = conversationMapper.toDto(conversation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Conversation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateConversationWithPatch() throws Exception {
        // Initialize the database
        conversation.setConversationId(UUID.randomUUID().toString());
        insertedConversation = conversationRepository.saveAndFlush(conversation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the conversation using partial update
        Conversation partialUpdatedConversation = new Conversation();
        partialUpdatedConversation.setConversationId(conversation.getConversationId());

        partialUpdatedConversation
            .referenceType(UPDATED_REFERENCE_TYPE)
            .referenceId(UPDATED_REFERENCE_ID)
            .lastMessageAt(UPDATED_LAST_MESSAGE_AT)
            .lastMessageSenderId(UPDATED_LAST_MESSAGE_SENDER_ID)
            .isPinned(UPDATED_IS_PINNED);

        restConversationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConversation.getConversationId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedConversation))
            )
            .andExpect(status().isOk());

        // Validate the Conversation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConversationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedConversation, conversation),
            getPersistedConversation(conversation)
        );
    }

    @Test
    @Transactional
    void fullUpdateConversationWithPatch() throws Exception {
        // Initialize the database
        conversation.setConversationId(UUID.randomUUID().toString());
        insertedConversation = conversationRepository.saveAndFlush(conversation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the conversation using partial update
        Conversation partialUpdatedConversation = new Conversation();
        partialUpdatedConversation.setConversationId(conversation.getConversationId());

        partialUpdatedConversation
            .conversationType(UPDATED_CONVERSATION_TYPE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .avatarUrl(UPDATED_AVATAR_URL)
            .referenceType(UPDATED_REFERENCE_TYPE)
            .referenceId(UPDATED_REFERENCE_ID)
            .lastMessagePreview(UPDATED_LAST_MESSAGE_PREVIEW)
            .lastMessageAt(UPDATED_LAST_MESSAGE_AT)
            .lastMessageSenderId(UPDATED_LAST_MESSAGE_SENDER_ID)
            .messageCount(UPDATED_MESSAGE_COUNT)
            .isArchived(UPDATED_IS_ARCHIVED)
            .isPinned(UPDATED_IS_PINNED);

        restConversationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConversation.getConversationId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedConversation))
            )
            .andExpect(status().isOk());

        // Validate the Conversation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConversationUpdatableFieldsEquals(partialUpdatedConversation, getPersistedConversation(partialUpdatedConversation));
    }

    @Test
    @Transactional
    void patchNonExistingConversation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversation.setConversationId(UUID.randomUUID().toString());

        // Create the Conversation
        ConversationDTO conversationDTO = conversationMapper.toDto(conversation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConversationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, conversationDTO.getConversationId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(conversationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Conversation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchConversation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversation.setConversationId(UUID.randomUUID().toString());

        // Create the Conversation
        ConversationDTO conversationDTO = conversationMapper.toDto(conversation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(conversationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Conversation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamConversation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversation.setConversationId(UUID.randomUUID().toString());

        // Create the Conversation
        ConversationDTO conversationDTO = conversationMapper.toDto(conversation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(conversationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Conversation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteConversation() throws Exception {
        // Initialize the database
        conversation.setConversationId(UUID.randomUUID().toString());
        insertedConversation = conversationRepository.saveAndFlush(conversation);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the conversation
        restConversationMockMvc
            .perform(delete(ENTITY_API_URL_ID, conversation.getConversationId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return conversationRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Conversation getPersistedConversation(Conversation conversation) {
        return conversationRepository.findById(conversation.getConversationId()).orElseThrow();
    }

    protected void assertPersistedConversationToMatchAllProperties(Conversation expectedConversation) {
        assertConversationAllPropertiesEquals(expectedConversation, getPersistedConversation(expectedConversation));
    }

    protected void assertPersistedConversationToMatchUpdatableProperties(Conversation expectedConversation) {
        assertConversationAllUpdatablePropertiesEquals(expectedConversation, getPersistedConversation(expectedConversation));
    }
}
