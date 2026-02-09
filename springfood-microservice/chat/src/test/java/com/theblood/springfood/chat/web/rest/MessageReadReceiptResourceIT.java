package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.MessageReadReceiptAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReadReceipt;
import com.theblood.springfood.chat.repository.MessageReadReceiptRepository;
import com.theblood.springfood.chat.service.dto.MessageReadReceiptDTO;
import com.theblood.springfood.chat.service.mapper.MessageReadReceiptMapper;
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
 * Integration tests for the {@link MessageReadReceiptResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MessageReadReceiptResourceIT {

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final Instant DEFAULT_READ_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_READ_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_DEVICE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_TYPE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/message-read-receipts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{receiptId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MessageReadReceiptRepository messageReadReceiptRepository;

    @Autowired
    private MessageReadReceiptMapper messageReadReceiptMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMessageReadReceiptMockMvc;

    private MessageReadReceipt messageReadReceipt;

    private MessageReadReceipt insertedMessageReadReceipt;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MessageReadReceipt createEntity(EntityManager em) {
        MessageReadReceipt messageReadReceipt = new MessageReadReceipt()
            .receiptId(UUID.randomUUID().toString())
            .userId(DEFAULT_USER_ID)
            .readAt(DEFAULT_READ_AT)
            .deviceType(DEFAULT_DEVICE_TYPE);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        messageReadReceipt.setMessage(message);
        return messageReadReceipt;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MessageReadReceipt createUpdatedEntity(EntityManager em) {
        MessageReadReceipt updatedMessageReadReceipt = new MessageReadReceipt()
            .receiptId(UUID.randomUUID().toString())
            .userId(UPDATED_USER_ID)
            .readAt(UPDATED_READ_AT)
            .deviceType(UPDATED_DEVICE_TYPE);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createUpdatedEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        updatedMessageReadReceipt.setMessage(message);
        return updatedMessageReadReceipt;
    }

    @BeforeEach
    void initTest() {
        messageReadReceipt = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedMessageReadReceipt != null) {
            messageReadReceiptRepository.delete(insertedMessageReadReceipt);
            insertedMessageReadReceipt = null;
        }
    }

    @Test
    @Transactional
    void createMessageReadReceipt() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MessageReadReceipt
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);
        var returnedMessageReadReceiptDTO = om.readValue(
            restMessageReadReceiptMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReadReceiptDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MessageReadReceiptDTO.class
        );

        // Validate the MessageReadReceipt in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMessageReadReceipt = messageReadReceiptMapper.toEntity(returnedMessageReadReceiptDTO);
        assertMessageReadReceiptUpdatableFieldsEquals(
            returnedMessageReadReceipt,
            getPersistedMessageReadReceipt(returnedMessageReadReceipt)
        );

        insertedMessageReadReceipt = returnedMessageReadReceipt;
    }

    @Test
    @Transactional
    void createMessageReadReceiptWithExistingId() throws Exception {
        // Create the MessageReadReceipt with an existing ID
        insertedMessageReadReceipt = messageReadReceiptRepository.saveAndFlush(messageReadReceipt);
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMessageReadReceiptMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReadReceiptDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MessageReadReceipt in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageReadReceipt.setUserId(null);

        // Create the MessageReadReceipt, which fails.
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);

        restMessageReadReceiptMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReadReceiptDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkReadAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageReadReceipt.setReadAt(null);

        // Create the MessageReadReceipt, which fails.
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);

        restMessageReadReceiptMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReadReceiptDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMessageReadReceipts() throws Exception {
        // Initialize the database
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());
        insertedMessageReadReceipt = messageReadReceiptRepository.saveAndFlush(messageReadReceipt);

        // Get all the messageReadReceiptList
        restMessageReadReceiptMockMvc
            .perform(get(ENTITY_API_URL + "?sort=receiptId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].receiptId").value(hasItem(messageReadReceipt.getReceiptId())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].readAt").value(hasItem(DEFAULT_READ_AT.toString())))
            .andExpect(jsonPath("$.[*].deviceType").value(hasItem(DEFAULT_DEVICE_TYPE)));
    }

    @Test
    @Transactional
    void getMessageReadReceipt() throws Exception {
        // Initialize the database
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());
        insertedMessageReadReceipt = messageReadReceiptRepository.saveAndFlush(messageReadReceipt);

        // Get the messageReadReceipt
        restMessageReadReceiptMockMvc
            .perform(get(ENTITY_API_URL_ID, messageReadReceipt.getReceiptId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.receiptId").value(messageReadReceipt.getReceiptId()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.readAt").value(DEFAULT_READ_AT.toString()))
            .andExpect(jsonPath("$.deviceType").value(DEFAULT_DEVICE_TYPE));
    }

    @Test
    @Transactional
    void getNonExistingMessageReadReceipt() throws Exception {
        // Get the messageReadReceipt
        restMessageReadReceiptMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMessageReadReceipt() throws Exception {
        // Initialize the database
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());
        insertedMessageReadReceipt = messageReadReceiptRepository.saveAndFlush(messageReadReceipt);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageReadReceipt
        MessageReadReceipt updatedMessageReadReceipt = messageReadReceiptRepository
            .findById(messageReadReceipt.getReceiptId())
            .orElseThrow();
        // Disconnect from session so that the updates on updatedMessageReadReceipt are not directly saved in db
        em.detach(updatedMessageReadReceipt);
        updatedMessageReadReceipt.userId(UPDATED_USER_ID).readAt(UPDATED_READ_AT).deviceType(UPDATED_DEVICE_TYPE);
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(updatedMessageReadReceipt);

        restMessageReadReceiptMockMvc
            .perform(
                put(ENTITY_API_URL_ID, messageReadReceiptDTO.getReceiptId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageReadReceiptDTO))
            )
            .andExpect(status().isOk());

        // Validate the MessageReadReceipt in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMessageReadReceiptToMatchAllProperties(updatedMessageReadReceipt);
    }

    @Test
    @Transactional
    void putNonExistingMessageReadReceipt() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());

        // Create the MessageReadReceipt
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageReadReceiptMockMvc
            .perform(
                put(ENTITY_API_URL_ID, messageReadReceiptDTO.getReceiptId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageReadReceiptDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReadReceipt in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMessageReadReceipt() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());

        // Create the MessageReadReceipt
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReadReceiptMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageReadReceiptDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReadReceipt in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMessageReadReceipt() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());

        // Create the MessageReadReceipt
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReadReceiptMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReadReceiptDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MessageReadReceipt in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMessageReadReceiptWithPatch() throws Exception {
        // Initialize the database
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());
        insertedMessageReadReceipt = messageReadReceiptRepository.saveAndFlush(messageReadReceipt);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageReadReceipt using partial update
        MessageReadReceipt partialUpdatedMessageReadReceipt = new MessageReadReceipt();
        partialUpdatedMessageReadReceipt.setReceiptId(messageReadReceipt.getReceiptId());

        restMessageReadReceiptMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessageReadReceipt.getReceiptId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMessageReadReceipt))
            )
            .andExpect(status().isOk());

        // Validate the MessageReadReceipt in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMessageReadReceiptUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMessageReadReceipt, messageReadReceipt),
            getPersistedMessageReadReceipt(messageReadReceipt)
        );
    }

    @Test
    @Transactional
    void fullUpdateMessageReadReceiptWithPatch() throws Exception {
        // Initialize the database
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());
        insertedMessageReadReceipt = messageReadReceiptRepository.saveAndFlush(messageReadReceipt);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageReadReceipt using partial update
        MessageReadReceipt partialUpdatedMessageReadReceipt = new MessageReadReceipt();
        partialUpdatedMessageReadReceipt.setReceiptId(messageReadReceipt.getReceiptId());

        partialUpdatedMessageReadReceipt.userId(UPDATED_USER_ID).readAt(UPDATED_READ_AT).deviceType(UPDATED_DEVICE_TYPE);

        restMessageReadReceiptMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessageReadReceipt.getReceiptId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMessageReadReceipt))
            )
            .andExpect(status().isOk());

        // Validate the MessageReadReceipt in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMessageReadReceiptUpdatableFieldsEquals(
            partialUpdatedMessageReadReceipt,
            getPersistedMessageReadReceipt(partialUpdatedMessageReadReceipt)
        );
    }

    @Test
    @Transactional
    void patchNonExistingMessageReadReceipt() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());

        // Create the MessageReadReceipt
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageReadReceiptMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, messageReadReceiptDTO.getReceiptId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(messageReadReceiptDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReadReceipt in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMessageReadReceipt() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());

        // Create the MessageReadReceipt
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReadReceiptMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(messageReadReceiptDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReadReceipt in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMessageReadReceipt() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());

        // Create the MessageReadReceipt
        MessageReadReceiptDTO messageReadReceiptDTO = messageReadReceiptMapper.toDto(messageReadReceipt);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReadReceiptMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(messageReadReceiptDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MessageReadReceipt in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMessageReadReceipt() throws Exception {
        // Initialize the database
        messageReadReceipt.setReceiptId(UUID.randomUUID().toString());
        insertedMessageReadReceipt = messageReadReceiptRepository.saveAndFlush(messageReadReceipt);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the messageReadReceipt
        restMessageReadReceiptMockMvc
            .perform(delete(ENTITY_API_URL_ID, messageReadReceipt.getReceiptId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return messageReadReceiptRepository.count();
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

    protected MessageReadReceipt getPersistedMessageReadReceipt(MessageReadReceipt messageReadReceipt) {
        return messageReadReceiptRepository.findById(messageReadReceipt.getReceiptId()).orElseThrow();
    }

    protected void assertPersistedMessageReadReceiptToMatchAllProperties(MessageReadReceipt expectedMessageReadReceipt) {
        assertMessageReadReceiptAllPropertiesEquals(expectedMessageReadReceipt, getPersistedMessageReadReceipt(expectedMessageReadReceipt));
    }

    protected void assertPersistedMessageReadReceiptToMatchUpdatableProperties(MessageReadReceipt expectedMessageReadReceipt) {
        assertMessageReadReceiptAllUpdatablePropertiesEquals(
            expectedMessageReadReceipt,
            getPersistedMessageReadReceipt(expectedMessageReadReceipt)
        );
    }
}
