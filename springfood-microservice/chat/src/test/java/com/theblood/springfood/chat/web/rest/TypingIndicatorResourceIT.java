package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.TypingIndicatorAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.TypingIndicator;
import com.theblood.springfood.chat.repository.TypingIndicatorRepository;
import com.theblood.springfood.chat.service.dto.TypingIndicatorDTO;
import com.theblood.springfood.chat.service.mapper.TypingIndicatorMapper;
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
 * Integration tests for the {@link TypingIndicatorResource} REST resources.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TypingIndicatorResourceIT {

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_USER_NAME = "AAAAAAAAAA";
    private static final String UPDATED_USER_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CONVERSATION_ID = "AAAAAAAAAA";
    private static final String UPDATED_CONVERSATION_ID = "BBBBBBBBBB";

    private static final Instant DEFAULT_STARTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_STARTED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_EXPIRES_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXPIRES_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/typing-indicators";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{indicatorId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TypingIndicatorRepository typingIndicatorRepository;

    @Autowired
    private TypingIndicatorMapper typingIndicatorMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTypingIndicatorMockMvc;

    private TypingIndicator typingIndicator;

    private TypingIndicator insertedTypingIndicator;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TypingIndicator createEntity() {
        return new TypingIndicator()
            .indicatorId(UUID.randomUUID().toString())
            .userId(DEFAULT_USER_ID)
            .userName(DEFAULT_USER_NAME)
            .conversationId(DEFAULT_CONVERSATION_ID)
            .startedAt(DEFAULT_STARTED_AT)
            .expiresAt(DEFAULT_EXPIRES_AT);
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TypingIndicator createUpdatedEntity() {
        return new TypingIndicator()
            .indicatorId(UUID.randomUUID().toString())
            .userId(UPDATED_USER_ID)
            .userName(UPDATED_USER_NAME)
            .conversationId(UPDATED_CONVERSATION_ID)
            .startedAt(UPDATED_STARTED_AT)
            .expiresAt(UPDATED_EXPIRES_AT);
    }

    @BeforeEach
    void initTest() {
        typingIndicator = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTypingIndicator != null) {
            typingIndicatorRepository.delete(insertedTypingIndicator);
            insertedTypingIndicator = null;
        }
    }

    @Test
    @Transactional
    void createTypingIndicator() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TypingIndicator
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);
        var returnedTypingIndicatorDTO = om.readValue(
            restTypingIndicatorMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typingIndicatorDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TypingIndicatorDTO.class
        );

        // Validate the TypingIndicator in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTypingIndicator = typingIndicatorMapper.toEntity(returnedTypingIndicatorDTO);
        assertTypingIndicatorUpdatableFieldsEquals(returnedTypingIndicator, getPersistedTypingIndicator(returnedTypingIndicator));

        insertedTypingIndicator = returnedTypingIndicator;
    }

    @Test
    @Transactional
    void createTypingIndicatorWithExistingId() throws Exception {
        // Create the TypingIndicator with an existing ID
        insertedTypingIndicator = typingIndicatorRepository.saveAndFlush(typingIndicator);
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTypingIndicatorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typingIndicatorDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TypingIndicator in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        typingIndicator.setUserId(null);

        // Create the TypingIndicator, which fails.
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        restTypingIndicatorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typingIndicatorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkConversationIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        typingIndicator.setConversationId(null);

        // Create the TypingIndicator, which fails.
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        restTypingIndicatorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typingIndicatorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        typingIndicator.setStartedAt(null);

        // Create the TypingIndicator, which fails.
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        restTypingIndicatorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typingIndicatorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkExpiresAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        typingIndicator.setExpiresAt(null);

        // Create the TypingIndicator, which fails.
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        restTypingIndicatorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typingIndicatorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTypingIndicators() throws Exception {
        // Initialize the database
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());
        insertedTypingIndicator = typingIndicatorRepository.saveAndFlush(typingIndicator);

        // Get all the typingIndicatorList
        restTypingIndicatorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=indicatorId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].indicatorId").value(hasItem(typingIndicator.getIndicatorId())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].userName").value(hasItem(DEFAULT_USER_NAME)))
            .andExpect(jsonPath("$.[*].conversationId").value(hasItem(DEFAULT_CONVERSATION_ID)))
            .andExpect(jsonPath("$.[*].startedAt").value(hasItem(DEFAULT_STARTED_AT.toString())))
            .andExpect(jsonPath("$.[*].expiresAt").value(hasItem(DEFAULT_EXPIRES_AT.toString())));
    }

    @Test
    @Transactional
    void getTypingIndicator() throws Exception {
        // Initialize the database
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());
        insertedTypingIndicator = typingIndicatorRepository.saveAndFlush(typingIndicator);

        // Get the typingIndicator
        restTypingIndicatorMockMvc
            .perform(get(ENTITY_API_URL_ID, typingIndicator.getIndicatorId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.indicatorId").value(typingIndicator.getIndicatorId()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.userName").value(DEFAULT_USER_NAME))
            .andExpect(jsonPath("$.conversationId").value(DEFAULT_CONVERSATION_ID))
            .andExpect(jsonPath("$.startedAt").value(DEFAULT_STARTED_AT.toString()))
            .andExpect(jsonPath("$.expiresAt").value(DEFAULT_EXPIRES_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingTypingIndicator() throws Exception {
        // Get the typingIndicator
        restTypingIndicatorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTypingIndicator() throws Exception {
        // Initialize the database
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());
        insertedTypingIndicator = typingIndicatorRepository.saveAndFlush(typingIndicator);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the typingIndicator
        TypingIndicator updatedTypingIndicator = typingIndicatorRepository.findById(typingIndicator.getIndicatorId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTypingIndicator are not directly saved in db
        em.detach(updatedTypingIndicator);
        updatedTypingIndicator
            .userId(UPDATED_USER_ID)
            .userName(UPDATED_USER_NAME)
            .conversationId(UPDATED_CONVERSATION_ID)
            .startedAt(UPDATED_STARTED_AT)
            .expiresAt(UPDATED_EXPIRES_AT);
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(updatedTypingIndicator);

        restTypingIndicatorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, typingIndicatorDTO.getIndicatorId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(typingIndicatorDTO))
            )
            .andExpect(status().isOk());

        // Validate the TypingIndicator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTypingIndicatorToMatchAllProperties(updatedTypingIndicator);
    }

    @Test
    @Transactional
    void putNonExistingTypingIndicator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());

        // Create the TypingIndicator
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTypingIndicatorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, typingIndicatorDTO.getIndicatorId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(typingIndicatorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypingIndicator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTypingIndicator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());

        // Create the TypingIndicator
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypingIndicatorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(typingIndicatorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypingIndicator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTypingIndicator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());

        // Create the TypingIndicator
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypingIndicatorMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typingIndicatorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TypingIndicator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTypingIndicatorWithPatch() throws Exception {
        // Initialize the database
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());
        insertedTypingIndicator = typingIndicatorRepository.saveAndFlush(typingIndicator);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the typingIndicator using partial update
        TypingIndicator partialUpdatedTypingIndicator = new TypingIndicator();
        partialUpdatedTypingIndicator.setIndicatorId(typingIndicator.getIndicatorId());

        partialUpdatedTypingIndicator.userId(UPDATED_USER_ID).userName(UPDATED_USER_NAME).expiresAt(UPDATED_EXPIRES_AT);

        restTypingIndicatorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTypingIndicator.getIndicatorId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTypingIndicator))
            )
            .andExpect(status().isOk());

        // Validate the TypingIndicator in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTypingIndicatorUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTypingIndicator, typingIndicator),
            getPersistedTypingIndicator(typingIndicator)
        );
    }

    @Test
    @Transactional
    void fullUpdateTypingIndicatorWithPatch() throws Exception {
        // Initialize the database
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());
        insertedTypingIndicator = typingIndicatorRepository.saveAndFlush(typingIndicator);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the typingIndicator using partial update
        TypingIndicator partialUpdatedTypingIndicator = new TypingIndicator();
        partialUpdatedTypingIndicator.setIndicatorId(typingIndicator.getIndicatorId());

        partialUpdatedTypingIndicator
            .userId(UPDATED_USER_ID)
            .userName(UPDATED_USER_NAME)
            .conversationId(UPDATED_CONVERSATION_ID)
            .startedAt(UPDATED_STARTED_AT)
            .expiresAt(UPDATED_EXPIRES_AT);

        restTypingIndicatorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTypingIndicator.getIndicatorId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTypingIndicator))
            )
            .andExpect(status().isOk());

        // Validate the TypingIndicator in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTypingIndicatorUpdatableFieldsEquals(
            partialUpdatedTypingIndicator,
            getPersistedTypingIndicator(partialUpdatedTypingIndicator)
        );
    }

    @Test
    @Transactional
    void patchNonExistingTypingIndicator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());

        // Create the TypingIndicator
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTypingIndicatorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, typingIndicatorDTO.getIndicatorId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(typingIndicatorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypingIndicator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTypingIndicator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());

        // Create the TypingIndicator
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypingIndicatorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(typingIndicatorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypingIndicator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTypingIndicator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());

        // Create the TypingIndicator
        TypingIndicatorDTO typingIndicatorDTO = typingIndicatorMapper.toDto(typingIndicator);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypingIndicatorMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(typingIndicatorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TypingIndicator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTypingIndicator() throws Exception {
        // Initialize the database
        typingIndicator.setIndicatorId(UUID.randomUUID().toString());
        insertedTypingIndicator = typingIndicatorRepository.saveAndFlush(typingIndicator);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the typingIndicator
        restTypingIndicatorMockMvc
            .perform(delete(ENTITY_API_URL_ID, typingIndicator.getIndicatorId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return typingIndicatorRepository.count();
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

    protected TypingIndicator getPersistedTypingIndicator(TypingIndicator typingIndicator) {
        return typingIndicatorRepository.findById(typingIndicator.getIndicatorId()).orElseThrow();
    }

    protected void assertPersistedTypingIndicatorToMatchAllProperties(TypingIndicator expectedTypingIndicator) {
        assertTypingIndicatorAllPropertiesEquals(expectedTypingIndicator, getPersistedTypingIndicator(expectedTypingIndicator));
    }

    protected void assertPersistedTypingIndicatorToMatchUpdatableProperties(TypingIndicator expectedTypingIndicator) {
        assertTypingIndicatorAllUpdatablePropertiesEquals(expectedTypingIndicator, getPersistedTypingIndicator(expectedTypingIndicator));
    }
}
