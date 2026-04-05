package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.ConversationSettingsAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.ConversationSettings;
import com.theblood.springfood.chat.repository.ConversationSettingsRepository;
import com.theblood.springfood.chat.service.dto.ConversationSettingsDTO;
import com.theblood.springfood.chat.service.mapper.ConversationSettingsMapper;
import jakarta.persistence.EntityManager;

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
 * Integration tests for the {@link ConversationSettingsResource} REST resources.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ConversationSettingsResourceIT {

    private static final Integer DEFAULT_ONLY_ADMIN_CAN_SEND = 1;
    private static final Integer UPDATED_ONLY_ADMIN_CAN_SEND = 2;

    private static final Integer DEFAULT_ONLY_ADMIN_CAN_ADD_MEMBERS = 1;
    private static final Integer UPDATED_ONLY_ADMIN_CAN_ADD_MEMBERS = 2;

    private static final Integer DEFAULT_AUTO_DELETE_DAYS = 1;
    private static final Integer UPDATED_AUTO_DELETE_DAYS = 2;

    private static final Integer DEFAULT_ALLOW_REACTIONS = 1;
    private static final Integer UPDATED_ALLOW_REACTIONS = 2;

    private static final Integer DEFAULT_ALLOW_REPLIES = 1;
    private static final Integer UPDATED_ALLOW_REPLIES = 2;

    private static final Integer DEFAULT_ALLOW_ATTACHMENTS = 1;
    private static final Integer UPDATED_ALLOW_ATTACHMENTS = 2;

    private static final Integer DEFAULT_MAX_ATTACHMENT_SIZE_MB = 1;
    private static final Integer UPDATED_MAX_ATTACHMENT_SIZE_MB = 2;

    private static final String DEFAULT_ALLOWED_FILE_TYPES = "AAAAAAAAAA";
    private static final String UPDATED_ALLOWED_FILE_TYPES = "BBBBBBBBBB";

    private static final Integer DEFAULT_SHOW_READ_RECEIPTS = 1;
    private static final Integer UPDATED_SHOW_READ_RECEIPTS = 2;

    private static final Integer DEFAULT_SHOW_TYPING_INDICATORS = 1;
    private static final Integer UPDATED_SHOW_TYPING_INDICATORS = 2;

    private static final String ENTITY_API_URL = "/api/conversation-settings";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{settingsId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ConversationSettingsRepository conversationSettingsRepository;

    @Autowired
    private ConversationSettingsMapper conversationSettingsMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restConversationSettingsMockMvc;

    private ConversationSettings conversationSettings;

    private ConversationSettings insertedConversationSettings;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ConversationSettings createEntity() {
        return new ConversationSettings()
            .settingsId(UUID.randomUUID().toString())
            .onlyAdminCanSend(DEFAULT_ONLY_ADMIN_CAN_SEND)
            .onlyAdminCanAddMembers(DEFAULT_ONLY_ADMIN_CAN_ADD_MEMBERS)
            .autoDeleteDays(DEFAULT_AUTO_DELETE_DAYS)
            .allowReactions(DEFAULT_ALLOW_REACTIONS)
            .allowReplies(DEFAULT_ALLOW_REPLIES)
            .allowAttachments(DEFAULT_ALLOW_ATTACHMENTS)
            .maxAttachmentSizeMb(DEFAULT_MAX_ATTACHMENT_SIZE_MB)
            .allowedFileTypes(DEFAULT_ALLOWED_FILE_TYPES)
            .showReadReceipts(DEFAULT_SHOW_READ_RECEIPTS)
            .showTypingIndicators(DEFAULT_SHOW_TYPING_INDICATORS);
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ConversationSettings createUpdatedEntity() {
        return new ConversationSettings()
            .settingsId(UUID.randomUUID().toString())
            .onlyAdminCanSend(UPDATED_ONLY_ADMIN_CAN_SEND)
            .onlyAdminCanAddMembers(UPDATED_ONLY_ADMIN_CAN_ADD_MEMBERS)
            .autoDeleteDays(UPDATED_AUTO_DELETE_DAYS)
            .allowReactions(UPDATED_ALLOW_REACTIONS)
            .allowReplies(UPDATED_ALLOW_REPLIES)
            .allowAttachments(UPDATED_ALLOW_ATTACHMENTS)
            .maxAttachmentSizeMb(UPDATED_MAX_ATTACHMENT_SIZE_MB)
            .allowedFileTypes(UPDATED_ALLOWED_FILE_TYPES)
            .showReadReceipts(UPDATED_SHOW_READ_RECEIPTS)
            .showTypingIndicators(UPDATED_SHOW_TYPING_INDICATORS);
    }

    @BeforeEach
    void initTest() {
        conversationSettings = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedConversationSettings != null) {
            conversationSettingsRepository.delete(insertedConversationSettings);
            insertedConversationSettings = null;
        }
    }

    @Test
    @Transactional
    void createConversationSettings() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ConversationSettings
        ConversationSettingsDTO conversationSettingsDTO = conversationSettingsMapper.toDto(conversationSettings);
        var returnedConversationSettingsDTO = om.readValue(
            restConversationSettingsMockMvc
                .perform(
                    post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationSettingsDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ConversationSettingsDTO.class
        );

        // Validate the ConversationSettings in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedConversationSettings = conversationSettingsMapper.toEntity(returnedConversationSettingsDTO);
        assertConversationSettingsUpdatableFieldsEquals(
            returnedConversationSettings,
            getPersistedConversationSettings(returnedConversationSettings)
        );

        insertedConversationSettings = returnedConversationSettings;
    }

    @Test
    @Transactional
    void createConversationSettingsWithExistingId() throws Exception {
        // Create the ConversationSettings with an existing ID
        insertedConversationSettings = conversationSettingsRepository.saveAndFlush(conversationSettings);
        ConversationSettingsDTO conversationSettingsDTO = conversationSettingsMapper.toDto(conversationSettings);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restConversationSettingsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationSettingsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ConversationSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllConversationSettings() throws Exception {
        // Initialize the database
        conversationSettings.setSettingsId(UUID.randomUUID().toString());
        insertedConversationSettings = conversationSettingsRepository.saveAndFlush(conversationSettings);

        // Get all the conversationSettingsList
        restConversationSettingsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=settingsId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].settingsId").value(hasItem(conversationSettings.getSettingsId())))
            .andExpect(jsonPath("$.[*].onlyAdminCanSend").value(hasItem(DEFAULT_ONLY_ADMIN_CAN_SEND)))
            .andExpect(jsonPath("$.[*].onlyAdminCanAddMembers").value(hasItem(DEFAULT_ONLY_ADMIN_CAN_ADD_MEMBERS)))
            .andExpect(jsonPath("$.[*].autoDeleteDays").value(hasItem(DEFAULT_AUTO_DELETE_DAYS)))
            .andExpect(jsonPath("$.[*].allowReactions").value(hasItem(DEFAULT_ALLOW_REACTIONS)))
            .andExpect(jsonPath("$.[*].allowReplies").value(hasItem(DEFAULT_ALLOW_REPLIES)))
            .andExpect(jsonPath("$.[*].allowAttachments").value(hasItem(DEFAULT_ALLOW_ATTACHMENTS)))
            .andExpect(jsonPath("$.[*].maxAttachmentSizeMb").value(hasItem(DEFAULT_MAX_ATTACHMENT_SIZE_MB)))
            .andExpect(jsonPath("$.[*].allowedFileTypes").value(hasItem(DEFAULT_ALLOWED_FILE_TYPES)))
            .andExpect(jsonPath("$.[*].showReadReceipts").value(hasItem(DEFAULT_SHOW_READ_RECEIPTS)))
            .andExpect(jsonPath("$.[*].showTypingIndicators").value(hasItem(DEFAULT_SHOW_TYPING_INDICATORS)));
    }

    @Test
    @Transactional
    void getConversationSettings() throws Exception {
        // Initialize the database
        conversationSettings.setSettingsId(UUID.randomUUID().toString());
        insertedConversationSettings = conversationSettingsRepository.saveAndFlush(conversationSettings);

        // Get the conversationSettings
        restConversationSettingsMockMvc
            .perform(get(ENTITY_API_URL_ID, conversationSettings.getSettingsId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.settingsId").value(conversationSettings.getSettingsId()))
            .andExpect(jsonPath("$.onlyAdminCanSend").value(DEFAULT_ONLY_ADMIN_CAN_SEND))
            .andExpect(jsonPath("$.onlyAdminCanAddMembers").value(DEFAULT_ONLY_ADMIN_CAN_ADD_MEMBERS))
            .andExpect(jsonPath("$.autoDeleteDays").value(DEFAULT_AUTO_DELETE_DAYS))
            .andExpect(jsonPath("$.allowReactions").value(DEFAULT_ALLOW_REACTIONS))
            .andExpect(jsonPath("$.allowReplies").value(DEFAULT_ALLOW_REPLIES))
            .andExpect(jsonPath("$.allowAttachments").value(DEFAULT_ALLOW_ATTACHMENTS))
            .andExpect(jsonPath("$.maxAttachmentSizeMb").value(DEFAULT_MAX_ATTACHMENT_SIZE_MB))
            .andExpect(jsonPath("$.allowedFileTypes").value(DEFAULT_ALLOWED_FILE_TYPES))
            .andExpect(jsonPath("$.showReadReceipts").value(DEFAULT_SHOW_READ_RECEIPTS))
            .andExpect(jsonPath("$.showTypingIndicators").value(DEFAULT_SHOW_TYPING_INDICATORS));
    }

    @Test
    @Transactional
    void getNonExistingConversationSettings() throws Exception {
        // Get the conversationSettings
        restConversationSettingsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingConversationSettings() throws Exception {
        // Initialize the database
        conversationSettings.setSettingsId(UUID.randomUUID().toString());
        insertedConversationSettings = conversationSettingsRepository.saveAndFlush(conversationSettings);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the conversationSettings
        ConversationSettings updatedConversationSettings = conversationSettingsRepository
            .findById(conversationSettings.getSettingsId())
            .orElseThrow();
        // Disconnect from session so that the updates on updatedConversationSettings are not directly saved in db
        em.detach(updatedConversationSettings);
        updatedConversationSettings
            .onlyAdminCanSend(UPDATED_ONLY_ADMIN_CAN_SEND)
            .onlyAdminCanAddMembers(UPDATED_ONLY_ADMIN_CAN_ADD_MEMBERS)
            .autoDeleteDays(UPDATED_AUTO_DELETE_DAYS)
            .allowReactions(UPDATED_ALLOW_REACTIONS)
            .allowReplies(UPDATED_ALLOW_REPLIES)
            .allowAttachments(UPDATED_ALLOW_ATTACHMENTS)
            .maxAttachmentSizeMb(UPDATED_MAX_ATTACHMENT_SIZE_MB)
            .allowedFileTypes(UPDATED_ALLOWED_FILE_TYPES)
            .showReadReceipts(UPDATED_SHOW_READ_RECEIPTS)
            .showTypingIndicators(UPDATED_SHOW_TYPING_INDICATORS);
        ConversationSettingsDTO conversationSettingsDTO = conversationSettingsMapper.toDto(updatedConversationSettings);

        restConversationSettingsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, conversationSettingsDTO.getSettingsId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(conversationSettingsDTO))
            )
            .andExpect(status().isOk());

        // Validate the ConversationSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedConversationSettingsToMatchAllProperties(updatedConversationSettings);
    }

    @Test
    @Transactional
    void putNonExistingConversationSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationSettings.setSettingsId(UUID.randomUUID().toString());

        // Create the ConversationSettings
        ConversationSettingsDTO conversationSettingsDTO = conversationSettingsMapper.toDto(conversationSettings);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConversationSettingsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, conversationSettingsDTO.getSettingsId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(conversationSettingsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConversationSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchConversationSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationSettings.setSettingsId(UUID.randomUUID().toString());

        // Create the ConversationSettings
        ConversationSettingsDTO conversationSettingsDTO = conversationSettingsMapper.toDto(conversationSettings);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationSettingsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(conversationSettingsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConversationSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamConversationSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationSettings.setSettingsId(UUID.randomUUID().toString());

        // Create the ConversationSettings
        ConversationSettingsDTO conversationSettingsDTO = conversationSettingsMapper.toDto(conversationSettings);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationSettingsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conversationSettingsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ConversationSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateConversationSettingsWithPatch() throws Exception {
        // Initialize the database
        conversationSettings.setSettingsId(UUID.randomUUID().toString());
        insertedConversationSettings = conversationSettingsRepository.saveAndFlush(conversationSettings);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the conversationSettings using partial update
        ConversationSettings partialUpdatedConversationSettings = new ConversationSettings();
        partialUpdatedConversationSettings.setSettingsId(conversationSettings.getSettingsId());

        partialUpdatedConversationSettings.onlyAdminCanSend(UPDATED_ONLY_ADMIN_CAN_SEND).allowReplies(UPDATED_ALLOW_REPLIES);

        restConversationSettingsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConversationSettings.getSettingsId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedConversationSettings))
            )
            .andExpect(status().isOk());

        // Validate the ConversationSettings in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConversationSettingsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedConversationSettings, conversationSettings),
            getPersistedConversationSettings(conversationSettings)
        );
    }

    @Test
    @Transactional
    void fullUpdateConversationSettingsWithPatch() throws Exception {
        // Initialize the database
        conversationSettings.setSettingsId(UUID.randomUUID().toString());
        insertedConversationSettings = conversationSettingsRepository.saveAndFlush(conversationSettings);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the conversationSettings using partial update
        ConversationSettings partialUpdatedConversationSettings = new ConversationSettings();
        partialUpdatedConversationSettings.setSettingsId(conversationSettings.getSettingsId());

        partialUpdatedConversationSettings
            .onlyAdminCanSend(UPDATED_ONLY_ADMIN_CAN_SEND)
            .onlyAdminCanAddMembers(UPDATED_ONLY_ADMIN_CAN_ADD_MEMBERS)
            .autoDeleteDays(UPDATED_AUTO_DELETE_DAYS)
            .allowReactions(UPDATED_ALLOW_REACTIONS)
            .allowReplies(UPDATED_ALLOW_REPLIES)
            .allowAttachments(UPDATED_ALLOW_ATTACHMENTS)
            .maxAttachmentSizeMb(UPDATED_MAX_ATTACHMENT_SIZE_MB)
            .allowedFileTypes(UPDATED_ALLOWED_FILE_TYPES)
            .showReadReceipts(UPDATED_SHOW_READ_RECEIPTS)
            .showTypingIndicators(UPDATED_SHOW_TYPING_INDICATORS);

        restConversationSettingsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConversationSettings.getSettingsId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedConversationSettings))
            )
            .andExpect(status().isOk());

        // Validate the ConversationSettings in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConversationSettingsUpdatableFieldsEquals(
            partialUpdatedConversationSettings,
            getPersistedConversationSettings(partialUpdatedConversationSettings)
        );
    }

    @Test
    @Transactional
    void patchNonExistingConversationSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationSettings.setSettingsId(UUID.randomUUID().toString());

        // Create the ConversationSettings
        ConversationSettingsDTO conversationSettingsDTO = conversationSettingsMapper.toDto(conversationSettings);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConversationSettingsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, conversationSettingsDTO.getSettingsId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(conversationSettingsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConversationSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchConversationSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationSettings.setSettingsId(UUID.randomUUID().toString());

        // Create the ConversationSettings
        ConversationSettingsDTO conversationSettingsDTO = conversationSettingsMapper.toDto(conversationSettings);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationSettingsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(conversationSettingsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConversationSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamConversationSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        conversationSettings.setSettingsId(UUID.randomUUID().toString());

        // Create the ConversationSettings
        ConversationSettingsDTO conversationSettingsDTO = conversationSettingsMapper.toDto(conversationSettings);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConversationSettingsMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(conversationSettingsDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ConversationSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteConversationSettings() throws Exception {
        // Initialize the database
        conversationSettings.setSettingsId(UUID.randomUUID().toString());
        insertedConversationSettings = conversationSettingsRepository.saveAndFlush(conversationSettings);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the conversationSettings
        restConversationSettingsMockMvc
            .perform(delete(ENTITY_API_URL_ID, conversationSettings.getSettingsId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return conversationSettingsRepository.count();
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

    protected ConversationSettings getPersistedConversationSettings(ConversationSettings conversationSettings) {
        return conversationSettingsRepository.findById(conversationSettings.getSettingsId()).orElseThrow();
    }

    protected void assertPersistedConversationSettingsToMatchAllProperties(ConversationSettings expectedConversationSettings) {
        assertConversationSettingsAllPropertiesEquals(
            expectedConversationSettings,
            getPersistedConversationSettings(expectedConversationSettings)
        );
    }

    protected void assertPersistedConversationSettingsToMatchUpdatableProperties(ConversationSettings expectedConversationSettings) {
        assertConversationSettingsAllUpdatablePropertiesEquals(
            expectedConversationSettings,
            getPersistedConversationSettings(expectedConversationSettings)
        );
    }
}
