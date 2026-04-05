package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.ConversationParticipantAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.service.dto.ConversationParticipantDTO;
import com.theblood.springfood.chat.service.mapper.ConversationParticipantMapper;
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
 * Integration tests for the {@link ConversationParticipantResource} REST resources.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ConversationParticipantResourceIT {

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_AVATAR_URL = "AAAAAAAAAA";
    private static final String UPDATED_AVATAR_URL = "BBBBBBBBBB";

    private static final String DEFAULT_ROLE = "AAAAAAAAAA";
    private static final String UPDATED_ROLE = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String DEFAULT_NICKNAME = "AAAAAAAAAA";
    private static final String UPDATED_NICKNAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_READ_MESSAGE_ID = "AAAAAAAAAA";
    private static final String UPDATED_LAST_READ_MESSAGE_ID = "BBBBBBBBBB";

    private static final Instant DEFAULT_LAST_READ_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_READ_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Integer DEFAULT_UNREAD_COUNT = 1;
    private static final Integer UPDATED_UNREAD_COUNT = 2;

    private static final Integer DEFAULT_IS_MUTED = 1;
    private static final Integer UPDATED_IS_MUTED = 2;

    private static final Instant DEFAULT_MUTE_UNTIL = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_MUTE_UNTIL = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Integer DEFAULT_IS_PINNED = 1;
    private static final Integer UPDATED_IS_PINNED = 2;

    private static final Instant DEFAULT_PINNED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PINNED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_JOINED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_JOINED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_LEFT_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LEFT_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_ADDED_BY = "AAAAAAAAAA";
    private static final String UPDATED_ADDED_BY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/conversation-participants";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{participantId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ConversationParticipantRepository conversationParticipantRepository;

    @Autowired
    private ConversationParticipantMapper conversationParticipantMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restConversationParticipantMockMvc;

    private ConversationParticipant conversationParticipant;

    private ConversationParticipant insertedConversationParticipant;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ConversationParticipant createEntity(EntityManager em) {
        ConversationParticipant conversationParticipant = new ConversationParticipant()
            .participantId(UUID.randomUUID().toString())
            .userId(DEFAULT_USER_ID)
            .displayName(DEFAULT_DISPLAY_NAME)
            .avatarUrl(DEFAULT_AVATAR_URL)
            .role(DEFAULT_ROLE)
            .status(DEFAULT_STATUS)
            .nickname(DEFAULT_NICKNAME)
            .lastReadMessageId(DEFAULT_LAST_READ_MESSAGE_ID)
            .lastReadAt(DEFAULT_LAST_READ_AT)
            .unreadCount(DEFAULT_UNREAD_COUNT)
            .isMuted(DEFAULT_IS_MUTED)
            .muteUntil(DEFAULT_MUTE_UNTIL)
            .isPinned(DEFAULT_IS_PINNED)
            .pinnedAt(DEFAULT_PINNED_AT)
            .joinedAt(DEFAULT_JOINED_AT)
            .leftAt(DEFAULT_LEFT_AT)
            .addedBy(DEFAULT_ADDED_BY);
        // Add required entity
        Conversation conversation;
        if (TestUtil.findAll(em, Conversation.class).isEmpty()) {
            conversation = ConversationResourceIT.createEntity();
            em.persist(conversation);
            em.flush();
        } else {
            conversation = TestUtil.findAll(em, Conversation.class).get(0);
        }
        conversationParticipant.setConversation(conversation);
        return conversationParticipant;
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ConversationParticipant createUpdatedEntity(EntityManager em) {
        ConversationParticipant updatedConversationParticipant = new ConversationParticipant()
            .participantId(UUID.randomUUID().toString())
            .userId(UPDATED_USER_ID)
            .displayName(UPDATED_DISPLAY_NAME)
            .avatarUrl(UPDATED_AVATAR_URL)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS)
            .nickname(UPDATED_NICKNAME)
            .lastReadMessageId(UPDATED_LAST_READ_MESSAGE_ID)
            .lastReadAt(UPDATED_LAST_READ_AT)
            .unreadCount(UPDATED_UNREAD_COUNT)
            .isMuted(UPDATED_IS_MUTED)
            .muteUntil(UPDATED_MUTE_UNTIL)
            .isPinned(UPDATED_IS_PINNED)
            .pinnedAt(UPDATED_PINNED_AT)
            .joinedAt(UPDATED_JOINED_AT)
            .leftAt(UPDATED_LEFT_AT)
            .addedBy(UPDATED_ADDED_BY);
        // Add required entity
        Conversation conversation;
        if (TestUtil.findAll(em, Conversation.class).isEmpty()) {
            conversation = ConversationResourceIT.createUpdatedEntity();
            em.persist(conversation);
            em.flush();
        } else {
            conversation = TestUtil.findAll(em, Conversation.class).get(0);
        }
        updatedConversationParticipant.setConversation(conversation);
        return updatedConversationParticipant;
    }

    @BeforeEach
    void initTest() {
        conversationParticipant = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedConversationParticipant != null) {
            conversationParticipantRepository.delete(insertedConversationParticipant);
            insertedConversationParticipant = null;
        }
    }

    @Test
    @Transactional
    void createConversationParticipant() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ConversationParticipant
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);
        var returnedConversationParticipantDTO = om.readValue(
            restConversationParticipantMockMvc
                .perform(
                    post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationParticipantDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ConversationParticipantDTO.class
        );

        // Validate the ConversationParticipant in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedConversationParticipant = conversationParticipantMapper.toEntity(returnedConversationParticipantDTO);
        assertConversationParticipantUpdatableFieldsEquals(
            returnedConversationParticipant,
            getPersistedConversationParticipant(returnedConversationParticipant)
        );

        insertedConversationParticipant = returnedConversationParticipant;
    }

    @Test
    @Transactional
    void createConversationParticipantWithExistingId() throws Exception {
        // Create the ConversationParticipant with an existing ID
        insertedConversationParticipant = conversationParticipantRepository.saveAndFlush(conversationParticipant);
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restConversationParticipantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationParticipantDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ConversationParticipant in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        conversationParticipant.setUserId(null);

        // Create the ConversationParticipant, which fails.
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        restConversationParticipantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationParticipantDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRoleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        conversationParticipant.setRole(null);

        // Create the ConversationParticipant, which fails.
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        restConversationParticipantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationParticipantDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        conversationParticipant.setStatus(null);

        // Create the ConversationParticipant, which fails.
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        restConversationParticipantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationParticipantDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkJoinedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        conversationParticipant.setJoinedAt(null);

        // Create the ConversationParticipant, which fails.
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        restConversationParticipantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationParticipantDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllConversationParticipants() throws Exception {
        // Initialize the database
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());
        insertedConversationParticipant = conversationParticipantRepository.saveAndFlush(conversationParticipant);

        // Get all the conversationParticipantList
        restConversationParticipantMockMvc
            .perform(get(ENTITY_API_URL + "?sort=participantId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].participantId").value(hasItem(conversationParticipant.getParticipantId())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].avatarUrl").value(hasItem(DEFAULT_AVATAR_URL)))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ROLE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].nickname").value(hasItem(DEFAULT_NICKNAME)))
            .andExpect(jsonPath("$.[*].lastReadMessageId").value(hasItem(DEFAULT_LAST_READ_MESSAGE_ID)))
            .andExpect(jsonPath("$.[*].lastReadAt").value(hasItem(DEFAULT_LAST_READ_AT.toString())))
            .andExpect(jsonPath("$.[*].unreadCount").value(hasItem(DEFAULT_UNREAD_COUNT)))
            .andExpect(jsonPath("$.[*].isMuted").value(hasItem(DEFAULT_IS_MUTED)))
            .andExpect(jsonPath("$.[*].muteUntil").value(hasItem(DEFAULT_MUTE_UNTIL.toString())))
            .andExpect(jsonPath("$.[*].isPinned").value(hasItem(DEFAULT_IS_PINNED)))
            .andExpect(jsonPath("$.[*].pinnedAt").value(hasItem(DEFAULT_PINNED_AT.toString())))
            .andExpect(jsonPath("$.[*].joinedAt").value(hasItem(DEFAULT_JOINED_AT.toString())))
            .andExpect(jsonPath("$.[*].leftAt").value(hasItem(DEFAULT_LEFT_AT.toString())))
            .andExpect(jsonPath("$.[*].addedBy").value(hasItem(DEFAULT_ADDED_BY)));
    }

    @Test
    @Transactional
    void getConversationParticipant() throws Exception {
        // Initialize the database
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());
        insertedConversationParticipant = conversationParticipantRepository.saveAndFlush(conversationParticipant);

        // Get the conversationParticipant
        restConversationParticipantMockMvc
            .perform(get(ENTITY_API_URL_ID, conversationParticipant.getParticipantId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.participantId").value(conversationParticipant.getParticipantId()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME))
            .andExpect(jsonPath("$.avatarUrl").value(DEFAULT_AVATAR_URL))
            .andExpect(jsonPath("$.role").value(DEFAULT_ROLE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
            .andExpect(jsonPath("$.nickname").value(DEFAULT_NICKNAME))
            .andExpect(jsonPath("$.lastReadMessageId").value(DEFAULT_LAST_READ_MESSAGE_ID))
            .andExpect(jsonPath("$.lastReadAt").value(DEFAULT_LAST_READ_AT.toString()))
            .andExpect(jsonPath("$.unreadCount").value(DEFAULT_UNREAD_COUNT))
            .andExpect(jsonPath("$.isMuted").value(DEFAULT_IS_MUTED))
            .andExpect(jsonPath("$.muteUntil").value(DEFAULT_MUTE_UNTIL.toString()))
            .andExpect(jsonPath("$.isPinned").value(DEFAULT_IS_PINNED))
            .andExpect(jsonPath("$.pinnedAt").value(DEFAULT_PINNED_AT.toString()))
            .andExpect(jsonPath("$.joinedAt").value(DEFAULT_JOINED_AT.toString()))
            .andExpect(jsonPath("$.leftAt").value(DEFAULT_LEFT_AT.toString()))
            .andExpect(jsonPath("$.addedBy").value(DEFAULT_ADDED_BY));
    }

    @Test
    @Transactional
    void getNonExistingConversationParticipant() throws Exception {
        // Get the conversationParticipant
        restConversationParticipantMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingConversationParticipant() throws Exception {
        // Initialize the database
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());
        insertedConversationParticipant = conversationParticipantRepository.saveAndFlush(conversationParticipant);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the conversationParticipant
        ConversationParticipant updatedConversationParticipant = conversationParticipantRepository
            .findById(conversationParticipant.getParticipantId())
            .orElseThrow();
        // Disconnect from session so that the updates on updatedConversationParticipant are not directly saved in db
        em.detach(updatedConversationParticipant);
        updatedConversationParticipant
            .userId(UPDATED_USER_ID)
            .displayName(UPDATED_DISPLAY_NAME)
            .avatarUrl(UPDATED_AVATAR_URL)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS)
            .nickname(UPDATED_NICKNAME)
            .lastReadMessageId(UPDATED_LAST_READ_MESSAGE_ID)
            .lastReadAt(UPDATED_LAST_READ_AT)
            .unreadCount(UPDATED_UNREAD_COUNT)
            .isMuted(UPDATED_IS_MUTED)
            .muteUntil(UPDATED_MUTE_UNTIL)
            .isPinned(UPDATED_IS_PINNED)
            .pinnedAt(UPDATED_PINNED_AT)
            .joinedAt(UPDATED_JOINED_AT)
            .leftAt(UPDATED_LEFT_AT)
            .addedBy(UPDATED_ADDED_BY);
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(updatedConversationParticipant);

        restConversationParticipantMockMvc
            .perform(
                put(ENTITY_API_URL_ID, conversationParticipantDTO.getParticipantId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(conversationParticipantDTO))
            )
            .andExpect(status().isOk());

        // Validate the ConversationParticipant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedConversationParticipantToMatchAllProperties(updatedConversationParticipant);
    }

    @Test
    @Transactional
    void putNonExistingConversationParticipant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());

        // Create the ConversationParticipant
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConversationParticipantMockMvc
            .perform(
                put(ENTITY_API_URL_ID, conversationParticipantDTO.getParticipantId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(conversationParticipantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConversationParticipant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchConversationParticipant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());

        // Create the ConversationParticipant
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationParticipantMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(conversationParticipantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConversationParticipant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamConversationParticipant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());

        // Create the ConversationParticipant
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationParticipantMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationParticipantDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ConversationParticipant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateConversationParticipantWithPatch() throws Exception {
        // Initialize the database
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());
        insertedConversationParticipant = conversationParticipantRepository.saveAndFlush(conversationParticipant);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the conversationParticipant using partial update
        ConversationParticipant partialUpdatedConversationParticipant = new ConversationParticipant();
        partialUpdatedConversationParticipant.setParticipantId(conversationParticipant.getParticipantId());

        partialUpdatedConversationParticipant
            .userId(UPDATED_USER_ID)
            .displayName(UPDATED_DISPLAY_NAME)
            .avatarUrl(UPDATED_AVATAR_URL)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS)
            .nickname(UPDATED_NICKNAME)
            .unreadCount(UPDATED_UNREAD_COUNT)
            .muteUntil(UPDATED_MUTE_UNTIL)
            .pinnedAt(UPDATED_PINNED_AT)
            .joinedAt(UPDATED_JOINED_AT);

        restConversationParticipantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConversationParticipant.getParticipantId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedConversationParticipant))
            )
            .andExpect(status().isOk());

        // Validate the ConversationParticipant in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConversationParticipantUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedConversationParticipant, conversationParticipant),
            getPersistedConversationParticipant(conversationParticipant)
        );
    }

    @Test
    @Transactional
    void fullUpdateConversationParticipantWithPatch() throws Exception {
        // Initialize the database
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());
        insertedConversationParticipant = conversationParticipantRepository.saveAndFlush(conversationParticipant);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the conversationParticipant using partial update
        ConversationParticipant partialUpdatedConversationParticipant = new ConversationParticipant();
        partialUpdatedConversationParticipant.setParticipantId(conversationParticipant.getParticipantId());

        partialUpdatedConversationParticipant
            .userId(UPDATED_USER_ID)
            .displayName(UPDATED_DISPLAY_NAME)
            .avatarUrl(UPDATED_AVATAR_URL)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS)
            .nickname(UPDATED_NICKNAME)
            .lastReadMessageId(UPDATED_LAST_READ_MESSAGE_ID)
            .lastReadAt(UPDATED_LAST_READ_AT)
            .unreadCount(UPDATED_UNREAD_COUNT)
            .isMuted(UPDATED_IS_MUTED)
            .muteUntil(UPDATED_MUTE_UNTIL)
            .isPinned(UPDATED_IS_PINNED)
            .pinnedAt(UPDATED_PINNED_AT)
            .joinedAt(UPDATED_JOINED_AT)
            .leftAt(UPDATED_LEFT_AT)
            .addedBy(UPDATED_ADDED_BY);

        restConversationParticipantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConversationParticipant.getParticipantId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedConversationParticipant))
            )
            .andExpect(status().isOk());

        // Validate the ConversationParticipant in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConversationParticipantUpdatableFieldsEquals(
            partialUpdatedConversationParticipant,
            getPersistedConversationParticipant(partialUpdatedConversationParticipant)
        );
    }

    @Test
    @Transactional
    void patchNonExistingConversationParticipant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());

        // Create the ConversationParticipant
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConversationParticipantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, conversationParticipantDTO.getParticipantId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(conversationParticipantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConversationParticipant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchConversationParticipant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());

        // Create the ConversationParticipant
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationParticipantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(conversationParticipantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConversationParticipant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamConversationParticipant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());

        // Create the ConversationParticipant
        ConversationParticipantDTO conversationParticipantDTO = conversationParticipantMapper.toDto(conversationParticipant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationParticipantMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(conversationParticipantDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ConversationParticipant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteConversationParticipant() throws Exception {
        // Initialize the database
        conversationParticipant.setParticipantId(UUID.randomUUID().toString());
        insertedConversationParticipant = conversationParticipantRepository.saveAndFlush(conversationParticipant);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the conversationParticipant
        restConversationParticipantMockMvc
            .perform(delete(ENTITY_API_URL_ID, conversationParticipant.getParticipantId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return conversationParticipantRepository.count();
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

    protected ConversationParticipant getPersistedConversationParticipant(ConversationParticipant conversationParticipant) {
        return conversationParticipantRepository.findById(conversationParticipant.getParticipantId()).orElseThrow();
    }

    protected void assertPersistedConversationParticipantToMatchAllProperties(ConversationParticipant expectedConversationParticipant) {
        assertConversationParticipantAllPropertiesEquals(
            expectedConversationParticipant,
            getPersistedConversationParticipant(expectedConversationParticipant)
        );
    }

    protected void assertPersistedConversationParticipantToMatchUpdatableProperties(
        ConversationParticipant expectedConversationParticipant
    ) {
        assertConversationParticipantAllUpdatablePropertiesEquals(
            expectedConversationParticipant,
            getPersistedConversationParticipant(expectedConversationParticipant)
        );
    }
}
