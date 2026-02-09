package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.UserPresenceAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.UserPresence;
import com.theblood.springfood.chat.repository.UserPresenceRepository;
import com.theblood.springfood.chat.service.dto.UserPresenceDTO;
import com.theblood.springfood.chat.service.mapper.UserPresenceMapper;
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
 * Integration tests for the {@link UserPresenceResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class UserPresenceResourceIT {

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_STATUS_MESSAGE = "BBBBBBBBBB";

    private static final Instant DEFAULT_LAST_SEEN_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_SEEN_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_ACTIVE_CONVERSATION_ID = "AAAAAAAAAA";
    private static final String UPDATED_ACTIVE_CONVERSATION_ID = "BBBBBBBBBB";

    private static final String DEFAULT_DEVICE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_DEVICE_ID = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_SESSION_ID = "AAAAAAAAAA";
    private static final String UPDATED_SESSION_ID = "BBBBBBBBBB";

    private static final Instant DEFAULT_LAST_ACTIVITY_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_ACTIVITY_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/user-presences";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{presenceId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserPresenceRepository userPresenceRepository;

    @Autowired
    private UserPresenceMapper userPresenceMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserPresenceMockMvc;

    private UserPresence userPresence;

    private UserPresence insertedUserPresence;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserPresence createEntity() {
        return new UserPresence()
            .presenceId(UUID.randomUUID().toString())
            .userId(DEFAULT_USER_ID)
            .status(DEFAULT_STATUS)
            .statusMessage(DEFAULT_STATUS_MESSAGE)
            .lastSeenAt(DEFAULT_LAST_SEEN_AT)
            .activeConversationId(DEFAULT_ACTIVE_CONVERSATION_ID)
            .deviceType(DEFAULT_DEVICE_TYPE)
            .deviceId(DEFAULT_DEVICE_ID)
            .sessionId(DEFAULT_SESSION_ID)
            .lastActivityAt(DEFAULT_LAST_ACTIVITY_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserPresence createUpdatedEntity() {
        return new UserPresence()
            .presenceId(UUID.randomUUID().toString())
            .userId(UPDATED_USER_ID)
            .status(UPDATED_STATUS)
            .statusMessage(UPDATED_STATUS_MESSAGE)
            .lastSeenAt(UPDATED_LAST_SEEN_AT)
            .activeConversationId(UPDATED_ACTIVE_CONVERSATION_ID)
            .deviceType(UPDATED_DEVICE_TYPE)
            .deviceId(UPDATED_DEVICE_ID)
            .sessionId(UPDATED_SESSION_ID)
            .lastActivityAt(UPDATED_LAST_ACTIVITY_AT);
    }

    @BeforeEach
    void initTest() {
        userPresence = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedUserPresence != null) {
            userPresenceRepository.delete(insertedUserPresence);
            insertedUserPresence = null;
        }
    }

    @Test
    @Transactional
    void createUserPresence() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the UserPresence
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);
        var returnedUserPresenceDTO = om.readValue(
            restUserPresenceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userPresenceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            UserPresenceDTO.class
        );

        // Validate the UserPresence in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedUserPresence = userPresenceMapper.toEntity(returnedUserPresenceDTO);
        assertUserPresenceUpdatableFieldsEquals(returnedUserPresence, getPersistedUserPresence(returnedUserPresence));

        insertedUserPresence = returnedUserPresence;
    }

    @Test
    @Transactional
    void createUserPresenceWithExistingId() throws Exception {
        // Create the UserPresence with an existing ID
        insertedUserPresence = userPresenceRepository.saveAndFlush(userPresence);
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserPresenceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userPresenceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the UserPresence in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        userPresence.setUserId(null);

        // Create the UserPresence, which fails.
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        restUserPresenceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userPresenceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        userPresence.setStatus(null);

        // Create the UserPresence, which fails.
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        restUserPresenceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userPresenceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastSeenAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        userPresence.setLastSeenAt(null);

        // Create the UserPresence, which fails.
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        restUserPresenceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userPresenceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllUserPresences() throws Exception {
        // Initialize the database
        userPresence.setPresenceId(UUID.randomUUID().toString());
        insertedUserPresence = userPresenceRepository.saveAndFlush(userPresence);

        // Get all the userPresenceList
        restUserPresenceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=presenceId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].presenceId").value(hasItem(userPresence.getPresenceId())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].statusMessage").value(hasItem(DEFAULT_STATUS_MESSAGE)))
            .andExpect(jsonPath("$.[*].lastSeenAt").value(hasItem(DEFAULT_LAST_SEEN_AT.toString())))
            .andExpect(jsonPath("$.[*].activeConversationId").value(hasItem(DEFAULT_ACTIVE_CONVERSATION_ID)))
            .andExpect(jsonPath("$.[*].deviceType").value(hasItem(DEFAULT_DEVICE_TYPE)))
            .andExpect(jsonPath("$.[*].deviceId").value(hasItem(DEFAULT_DEVICE_ID)))
            .andExpect(jsonPath("$.[*].sessionId").value(hasItem(DEFAULT_SESSION_ID)))
            .andExpect(jsonPath("$.[*].lastActivityAt").value(hasItem(DEFAULT_LAST_ACTIVITY_AT.toString())));
    }

    @Test
    @Transactional
    void getUserPresence() throws Exception {
        // Initialize the database
        userPresence.setPresenceId(UUID.randomUUID().toString());
        insertedUserPresence = userPresenceRepository.saveAndFlush(userPresence);

        // Get the userPresence
        restUserPresenceMockMvc
            .perform(get(ENTITY_API_URL_ID, userPresence.getPresenceId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.presenceId").value(userPresence.getPresenceId()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
            .andExpect(jsonPath("$.statusMessage").value(DEFAULT_STATUS_MESSAGE))
            .andExpect(jsonPath("$.lastSeenAt").value(DEFAULT_LAST_SEEN_AT.toString()))
            .andExpect(jsonPath("$.activeConversationId").value(DEFAULT_ACTIVE_CONVERSATION_ID))
            .andExpect(jsonPath("$.deviceType").value(DEFAULT_DEVICE_TYPE))
            .andExpect(jsonPath("$.deviceId").value(DEFAULT_DEVICE_ID))
            .andExpect(jsonPath("$.sessionId").value(DEFAULT_SESSION_ID))
            .andExpect(jsonPath("$.lastActivityAt").value(DEFAULT_LAST_ACTIVITY_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingUserPresence() throws Exception {
        // Get the userPresence
        restUserPresenceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingUserPresence() throws Exception {
        // Initialize the database
        userPresence.setPresenceId(UUID.randomUUID().toString());
        insertedUserPresence = userPresenceRepository.saveAndFlush(userPresence);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userPresence
        UserPresence updatedUserPresence = userPresenceRepository.findById(userPresence.getPresenceId()).orElseThrow();
        // Disconnect from session so that the updates on updatedUserPresence are not directly saved in db
        em.detach(updatedUserPresence);
        updatedUserPresence
            .userId(UPDATED_USER_ID)
            .status(UPDATED_STATUS)
            .statusMessage(UPDATED_STATUS_MESSAGE)
            .lastSeenAt(UPDATED_LAST_SEEN_AT)
            .activeConversationId(UPDATED_ACTIVE_CONVERSATION_ID)
            .deviceType(UPDATED_DEVICE_TYPE)
            .deviceId(UPDATED_DEVICE_ID)
            .sessionId(UPDATED_SESSION_ID)
            .lastActivityAt(UPDATED_LAST_ACTIVITY_AT);
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(updatedUserPresence);

        restUserPresenceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, userPresenceDTO.getPresenceId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(userPresenceDTO))
            )
            .andExpect(status().isOk());

        // Validate the UserPresence in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedUserPresenceToMatchAllProperties(updatedUserPresence);
    }

    @Test
    @Transactional
    void putNonExistingUserPresence() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userPresence.setPresenceId(UUID.randomUUID().toString());

        // Create the UserPresence
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserPresenceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, userPresenceDTO.getPresenceId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(userPresenceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserPresence in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchUserPresence() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userPresence.setPresenceId(UUID.randomUUID().toString());

        // Create the UserPresence
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserPresenceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(userPresenceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserPresence in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamUserPresence() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userPresence.setPresenceId(UUID.randomUUID().toString());

        // Create the UserPresence
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserPresenceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userPresenceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserPresence in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateUserPresenceWithPatch() throws Exception {
        // Initialize the database
        userPresence.setPresenceId(UUID.randomUUID().toString());
        insertedUserPresence = userPresenceRepository.saveAndFlush(userPresence);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userPresence using partial update
        UserPresence partialUpdatedUserPresence = new UserPresence();
        partialUpdatedUserPresence.setPresenceId(userPresence.getPresenceId());

        partialUpdatedUserPresence.activeConversationId(UPDATED_ACTIVE_CONVERSATION_ID);

        restUserPresenceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserPresence.getPresenceId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUserPresence))
            )
            .andExpect(status().isOk());

        // Validate the UserPresence in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserPresenceUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedUserPresence, userPresence),
            getPersistedUserPresence(userPresence)
        );
    }

    @Test
    @Transactional
    void fullUpdateUserPresenceWithPatch() throws Exception {
        // Initialize the database
        userPresence.setPresenceId(UUID.randomUUID().toString());
        insertedUserPresence = userPresenceRepository.saveAndFlush(userPresence);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userPresence using partial update
        UserPresence partialUpdatedUserPresence = new UserPresence();
        partialUpdatedUserPresence.setPresenceId(userPresence.getPresenceId());

        partialUpdatedUserPresence
            .userId(UPDATED_USER_ID)
            .status(UPDATED_STATUS)
            .statusMessage(UPDATED_STATUS_MESSAGE)
            .lastSeenAt(UPDATED_LAST_SEEN_AT)
            .activeConversationId(UPDATED_ACTIVE_CONVERSATION_ID)
            .deviceType(UPDATED_DEVICE_TYPE)
            .deviceId(UPDATED_DEVICE_ID)
            .sessionId(UPDATED_SESSION_ID)
            .lastActivityAt(UPDATED_LAST_ACTIVITY_AT);

        restUserPresenceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserPresence.getPresenceId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUserPresence))
            )
            .andExpect(status().isOk());

        // Validate the UserPresence in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserPresenceUpdatableFieldsEquals(partialUpdatedUserPresence, getPersistedUserPresence(partialUpdatedUserPresence));
    }

    @Test
    @Transactional
    void patchNonExistingUserPresence() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userPresence.setPresenceId(UUID.randomUUID().toString());

        // Create the UserPresence
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserPresenceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, userPresenceDTO.getPresenceId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(userPresenceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserPresence in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchUserPresence() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userPresence.setPresenceId(UUID.randomUUID().toString());

        // Create the UserPresence
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserPresenceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(userPresenceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserPresence in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamUserPresence() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userPresence.setPresenceId(UUID.randomUUID().toString());

        // Create the UserPresence
        UserPresenceDTO userPresenceDTO = userPresenceMapper.toDto(userPresence);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserPresenceMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(userPresenceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserPresence in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteUserPresence() throws Exception {
        // Initialize the database
        userPresence.setPresenceId(UUID.randomUUID().toString());
        insertedUserPresence = userPresenceRepository.saveAndFlush(userPresence);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the userPresence
        restUserPresenceMockMvc
            .perform(delete(ENTITY_API_URL_ID, userPresence.getPresenceId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return userPresenceRepository.count();
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

    protected UserPresence getPersistedUserPresence(UserPresence userPresence) {
        return userPresenceRepository.findById(userPresence.getPresenceId()).orElseThrow();
    }

    protected void assertPersistedUserPresenceToMatchAllProperties(UserPresence expectedUserPresence) {
        assertUserPresenceAllPropertiesEquals(expectedUserPresence, getPersistedUserPresence(expectedUserPresence));
    }

    protected void assertPersistedUserPresenceToMatchUpdatableProperties(UserPresence expectedUserPresence) {
        assertUserPresenceAllUpdatablePropertiesEquals(expectedUserPresence, getPersistedUserPresence(expectedUserPresence));
    }
}
