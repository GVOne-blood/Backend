package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.MessageReportAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.MessageReport;
import com.theblood.springfood.chat.repository.MessageReportRepository;
import com.theblood.springfood.chat.service.dto.MessageReportDTO;
import com.theblood.springfood.chat.service.mapper.MessageReportMapper;
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
 * Integration tests for the {@link MessageReportResource} REST resources.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MessageReportResourceIT {

    private static final String DEFAULT_REPORTER_ID = "AAAAAAAAAA";
    private static final String UPDATED_REPORTER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_MESSAGE_ID = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REASON = "BBBBBBBBBB";

    private static final String DEFAULT_DETAILS = "AAAAAAAAAA";
    private static final String UPDATED_DETAILS = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String DEFAULT_REVIEWED_BY = "AAAAAAAAAA";
    private static final String UPDATED_REVIEWED_BY = "BBBBBBBBBB";

    private static final Instant DEFAULT_REVIEWED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REVIEWED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_REVIEW_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_REVIEW_NOTES = "BBBBBBBBBB";

    private static final String DEFAULT_ACTION_TAKEN = "AAAAAAAAAA";
    private static final String UPDATED_ACTION_TAKEN = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/message-reports";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{reportId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MessageReportRepository messageReportRepository;

    @Autowired
    private MessageReportMapper messageReportMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMessageReportMockMvc;

    private MessageReport messageReport;

    private MessageReport insertedMessageReport;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MessageReport createEntity() {
        return new MessageReport()
            .reportId(UUID.randomUUID().toString())
            .reporterId(DEFAULT_REPORTER_ID)
            .messageId(DEFAULT_MESSAGE_ID)
            .reason(DEFAULT_REASON)
            .details(DEFAULT_DETAILS)
            .status(DEFAULT_STATUS)
            .reviewedBy(DEFAULT_REVIEWED_BY)
            .reviewedAt(DEFAULT_REVIEWED_AT)
            .reviewNotes(DEFAULT_REVIEW_NOTES)
            .actionTaken(DEFAULT_ACTION_TAKEN);
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MessageReport createUpdatedEntity() {
        return new MessageReport()
            .reportId(UUID.randomUUID().toString())
            .reporterId(UPDATED_REPORTER_ID)
            .messageId(UPDATED_MESSAGE_ID)
            .reason(UPDATED_REASON)
            .details(UPDATED_DETAILS)
            .status(UPDATED_STATUS)
            .reviewedBy(UPDATED_REVIEWED_BY)
            .reviewedAt(UPDATED_REVIEWED_AT)
            .reviewNotes(UPDATED_REVIEW_NOTES)
            .actionTaken(UPDATED_ACTION_TAKEN);
    }

    @BeforeEach
    void initTest() {
        messageReport = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMessageReport != null) {
            messageReportRepository.delete(insertedMessageReport);
            insertedMessageReport = null;
        }
    }

    @Test
    @Transactional
    void createMessageReport() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MessageReport
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);
        var returnedMessageReportDTO = om.readValue(
            restMessageReportMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReportDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MessageReportDTO.class
        );

        // Validate the MessageReport in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMessageReport = messageReportMapper.toEntity(returnedMessageReportDTO);
        assertMessageReportUpdatableFieldsEquals(returnedMessageReport, getPersistedMessageReport(returnedMessageReport));

        insertedMessageReport = returnedMessageReport;
    }

    @Test
    @Transactional
    void createMessageReportWithExistingId() throws Exception {
        // Create the MessageReport with an existing ID
        insertedMessageReport = messageReportRepository.saveAndFlush(messageReport);
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMessageReportMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReportDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MessageReport in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkReporterIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageReport.setReporterId(null);

        // Create the MessageReport, which fails.
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        restMessageReportMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReportDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMessageIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageReport.setMessageId(null);

        // Create the MessageReport, which fails.
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        restMessageReportMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReportDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkReasonIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageReport.setReason(null);

        // Create the MessageReport, which fails.
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        restMessageReportMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReportDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageReport.setStatus(null);

        // Create the MessageReport, which fails.
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        restMessageReportMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReportDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMessageReports() throws Exception {
        // Initialize the database
        messageReport.setReportId(UUID.randomUUID().toString());
        insertedMessageReport = messageReportRepository.saveAndFlush(messageReport);

        // Get all the messageReportList
        restMessageReportMockMvc
            .perform(get(ENTITY_API_URL + "?sort=reportId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].reportId").value(hasItem(messageReport.getReportId())))
            .andExpect(jsonPath("$.[*].reporterId").value(hasItem(DEFAULT_REPORTER_ID)))
            .andExpect(jsonPath("$.[*].messageId").value(hasItem(DEFAULT_MESSAGE_ID)))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)))
            .andExpect(jsonPath("$.[*].details").value(hasItem(DEFAULT_DETAILS)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].reviewedBy").value(hasItem(DEFAULT_REVIEWED_BY)))
            .andExpect(jsonPath("$.[*].reviewedAt").value(hasItem(DEFAULT_REVIEWED_AT.toString())))
            .andExpect(jsonPath("$.[*].reviewNotes").value(hasItem(DEFAULT_REVIEW_NOTES)))
            .andExpect(jsonPath("$.[*].actionTaken").value(hasItem(DEFAULT_ACTION_TAKEN)));
    }

    @Test
    @Transactional
    void getMessageReport() throws Exception {
        // Initialize the database
        messageReport.setReportId(UUID.randomUUID().toString());
        insertedMessageReport = messageReportRepository.saveAndFlush(messageReport);

        // Get the messageReport
        restMessageReportMockMvc
            .perform(get(ENTITY_API_URL_ID, messageReport.getReportId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.reportId").value(messageReport.getReportId()))
            .andExpect(jsonPath("$.reporterId").value(DEFAULT_REPORTER_ID))
            .andExpect(jsonPath("$.messageId").value(DEFAULT_MESSAGE_ID))
            .andExpect(jsonPath("$.reason").value(DEFAULT_REASON))
            .andExpect(jsonPath("$.details").value(DEFAULT_DETAILS))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
            .andExpect(jsonPath("$.reviewedBy").value(DEFAULT_REVIEWED_BY))
            .andExpect(jsonPath("$.reviewedAt").value(DEFAULT_REVIEWED_AT.toString()))
            .andExpect(jsonPath("$.reviewNotes").value(DEFAULT_REVIEW_NOTES))
            .andExpect(jsonPath("$.actionTaken").value(DEFAULT_ACTION_TAKEN));
    }

    @Test
    @Transactional
    void getNonExistingMessageReport() throws Exception {
        // Get the messageReport
        restMessageReportMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMessageReport() throws Exception {
        // Initialize the database
        messageReport.setReportId(UUID.randomUUID().toString());
        insertedMessageReport = messageReportRepository.saveAndFlush(messageReport);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageReport
        MessageReport updatedMessageReport = messageReportRepository.findById(messageReport.getReportId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMessageReport are not directly saved in db
        em.detach(updatedMessageReport);
        updatedMessageReport
            .reporterId(UPDATED_REPORTER_ID)
            .messageId(UPDATED_MESSAGE_ID)
            .reason(UPDATED_REASON)
            .details(UPDATED_DETAILS)
            .status(UPDATED_STATUS)
            .reviewedBy(UPDATED_REVIEWED_BY)
            .reviewedAt(UPDATED_REVIEWED_AT)
            .reviewNotes(UPDATED_REVIEW_NOTES)
            .actionTaken(UPDATED_ACTION_TAKEN);
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(updatedMessageReport);

        restMessageReportMockMvc
            .perform(
                put(ENTITY_API_URL_ID, messageReportDTO.getReportId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageReportDTO))
            )
            .andExpect(status().isOk());

        // Validate the MessageReport in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMessageReportToMatchAllProperties(updatedMessageReport);
    }

    @Test
    @Transactional
    void putNonExistingMessageReport() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReport.setReportId(UUID.randomUUID().toString());

        // Create the MessageReport
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageReportMockMvc
            .perform(
                put(ENTITY_API_URL_ID, messageReportDTO.getReportId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageReportDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReport in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMessageReport() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReport.setReportId(UUID.randomUUID().toString());

        // Create the MessageReport
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReportMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageReportDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReport in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMessageReport() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReport.setReportId(UUID.randomUUID().toString());

        // Create the MessageReport
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReportMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReportDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MessageReport in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMessageReportWithPatch() throws Exception {
        // Initialize the database
        messageReport.setReportId(UUID.randomUUID().toString());
        insertedMessageReport = messageReportRepository.saveAndFlush(messageReport);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageReport using partial update
        MessageReport partialUpdatedMessageReport = new MessageReport();
        partialUpdatedMessageReport.setReportId(messageReport.getReportId());

        partialUpdatedMessageReport.reason(UPDATED_REASON).status(UPDATED_STATUS);

        restMessageReportMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessageReport.getReportId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMessageReport))
            )
            .andExpect(status().isOk());

        // Validate the MessageReport in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMessageReportUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMessageReport, messageReport),
            getPersistedMessageReport(messageReport)
        );
    }

    @Test
    @Transactional
    void fullUpdateMessageReportWithPatch() throws Exception {
        // Initialize the database
        messageReport.setReportId(UUID.randomUUID().toString());
        insertedMessageReport = messageReportRepository.saveAndFlush(messageReport);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageReport using partial update
        MessageReport partialUpdatedMessageReport = new MessageReport();
        partialUpdatedMessageReport.setReportId(messageReport.getReportId());

        partialUpdatedMessageReport
            .reporterId(UPDATED_REPORTER_ID)
            .messageId(UPDATED_MESSAGE_ID)
            .reason(UPDATED_REASON)
            .details(UPDATED_DETAILS)
            .status(UPDATED_STATUS)
            .reviewedBy(UPDATED_REVIEWED_BY)
            .reviewedAt(UPDATED_REVIEWED_AT)
            .reviewNotes(UPDATED_REVIEW_NOTES)
            .actionTaken(UPDATED_ACTION_TAKEN);

        restMessageReportMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessageReport.getReportId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMessageReport))
            )
            .andExpect(status().isOk());

        // Validate the MessageReport in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMessageReportUpdatableFieldsEquals(partialUpdatedMessageReport, getPersistedMessageReport(partialUpdatedMessageReport));
    }

    @Test
    @Transactional
    void patchNonExistingMessageReport() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReport.setReportId(UUID.randomUUID().toString());

        // Create the MessageReport
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageReportMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, messageReportDTO.getReportId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(messageReportDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReport in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMessageReport() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReport.setReportId(UUID.randomUUID().toString());

        // Create the MessageReport
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReportMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(messageReportDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReport in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMessageReport() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReport.setReportId(UUID.randomUUID().toString());

        // Create the MessageReport
        MessageReportDTO messageReportDTO = messageReportMapper.toDto(messageReport);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReportMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(messageReportDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MessageReport in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMessageReport() throws Exception {
        // Initialize the database
        messageReport.setReportId(UUID.randomUUID().toString());
        insertedMessageReport = messageReportRepository.saveAndFlush(messageReport);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the messageReport
        restMessageReportMockMvc
            .perform(delete(ENTITY_API_URL_ID, messageReport.getReportId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return messageReportRepository.count();
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

    protected MessageReport getPersistedMessageReport(MessageReport messageReport) {
        return messageReportRepository.findById(messageReport.getReportId()).orElseThrow();
    }

    protected void assertPersistedMessageReportToMatchAllProperties(MessageReport expectedMessageReport) {
        assertMessageReportAllPropertiesEquals(expectedMessageReport, getPersistedMessageReport(expectedMessageReport));
    }

    protected void assertPersistedMessageReportToMatchUpdatableProperties(MessageReport expectedMessageReport) {
        assertMessageReportAllUpdatablePropertiesEquals(expectedMessageReport, getPersistedMessageReport(expectedMessageReport));
    }
}
