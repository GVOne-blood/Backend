package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.MessageAttachmentAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageAttachment;
import com.theblood.springfood.chat.repository.MessageAttachmentRepository;
import com.theblood.springfood.chat.service.dto.MessageAttachmentDTO;
import com.theblood.springfood.chat.service.mapper.MessageAttachmentMapper;
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
 * Integration tests for the {@link MessageAttachmentResource} REST resources.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MessageAttachmentResourceIT {

    private static final String DEFAULT_MEDIA_ID = "AAAAAAAAAA";
    private static final String UPDATED_MEDIA_ID = "BBBBBBBBBB";

    private static final String DEFAULT_ATTACHMENT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_ATTACHMENT_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_FILE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FILE_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_FILE_SIZE = 1L;
    private static final Long UPDATED_FILE_SIZE = 2L;

    private static final String DEFAULT_MIME_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_MIME_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final String DEFAULT_THUMBNAIL_URL = "AAAAAAAAAA";
    private static final String UPDATED_THUMBNAIL_URL = "BBBBBBBBBB";

    private static final Integer DEFAULT_WIDTH = 1;
    private static final Integer UPDATED_WIDTH = 2;

    private static final Integer DEFAULT_HEIGHT = 1;
    private static final Integer UPDATED_HEIGHT = 2;

    private static final Integer DEFAULT_DURATION = 1;
    private static final Integer UPDATED_DURATION = 2;

    private static final Integer DEFAULT_DISPLAY_ORDER = 1;
    private static final Integer UPDATED_DISPLAY_ORDER = 2;

    private static final String ENTITY_API_URL = "/api/message-attachments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{attachmentId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MessageAttachmentRepository messageAttachmentRepository;

    @Autowired
    private MessageAttachmentMapper messageAttachmentMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMessageAttachmentMockMvc;

    private MessageAttachment messageAttachment;

    private MessageAttachment insertedMessageAttachment;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MessageAttachment createEntity(EntityManager em) {
        MessageAttachment messageAttachment = new MessageAttachment()
            .attachmentId(UUID.randomUUID().toString())
            .mediaId(DEFAULT_MEDIA_ID)
            .attachmentType(DEFAULT_ATTACHMENT_TYPE)
            .fileName(DEFAULT_FILE_NAME)
            .fileSize(DEFAULT_FILE_SIZE)
            .mimeType(DEFAULT_MIME_TYPE)
            .url(DEFAULT_URL)
            .thumbnailUrl(DEFAULT_THUMBNAIL_URL)
            .width(DEFAULT_WIDTH)
            .height(DEFAULT_HEIGHT)
            .duration(DEFAULT_DURATION)
            .displayOrder(DEFAULT_DISPLAY_ORDER);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        messageAttachment.setMessage(message);
        return messageAttachment;
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MessageAttachment createUpdatedEntity(EntityManager em) {
        MessageAttachment updatedMessageAttachment = new MessageAttachment()
            .attachmentId(UUID.randomUUID().toString())
            .mediaId(UPDATED_MEDIA_ID)
            .attachmentType(UPDATED_ATTACHMENT_TYPE)
            .fileName(UPDATED_FILE_NAME)
            .fileSize(UPDATED_FILE_SIZE)
            .mimeType(UPDATED_MIME_TYPE)
            .url(UPDATED_URL)
            .thumbnailUrl(UPDATED_THUMBNAIL_URL)
            .width(UPDATED_WIDTH)
            .height(UPDATED_HEIGHT)
            .duration(UPDATED_DURATION)
            .displayOrder(UPDATED_DISPLAY_ORDER);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createUpdatedEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        updatedMessageAttachment.setMessage(message);
        return updatedMessageAttachment;
    }

    @BeforeEach
    void initTest() {
        messageAttachment = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedMessageAttachment != null) {
            messageAttachmentRepository.delete(insertedMessageAttachment);
            insertedMessageAttachment = null;
        }
    }

    @Test
    @Transactional
    void createMessageAttachment() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MessageAttachment
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);
        var returnedMessageAttachmentDTO = om.readValue(
            restMessageAttachmentMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageAttachmentDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MessageAttachmentDTO.class
        );

        // Validate the MessageAttachment in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMessageAttachment = messageAttachmentMapper.toEntity(returnedMessageAttachmentDTO);
        assertMessageAttachmentUpdatableFieldsEquals(returnedMessageAttachment, getPersistedMessageAttachment(returnedMessageAttachment));

        insertedMessageAttachment = returnedMessageAttachment;
    }

    @Test
    @Transactional
    void createMessageAttachmentWithExistingId() throws Exception {
        // Create the MessageAttachment with an existing ID
        insertedMessageAttachment = messageAttachmentRepository.saveAndFlush(messageAttachment);
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMessageAttachmentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageAttachmentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MessageAttachment in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkMediaIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageAttachment.setMediaId(null);

        // Create the MessageAttachment, which fails.
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);

        restMessageAttachmentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageAttachmentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAttachmentTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageAttachment.setAttachmentType(null);

        // Create the MessageAttachment, which fails.
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);

        restMessageAttachmentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageAttachmentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMessageAttachments() throws Exception {
        // Initialize the database
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());
        insertedMessageAttachment = messageAttachmentRepository.saveAndFlush(messageAttachment);

        // Get all the messageAttachmentList
        restMessageAttachmentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=attachmentId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].attachmentId").value(hasItem(messageAttachment.getAttachmentId())))
            .andExpect(jsonPath("$.[*].mediaId").value(hasItem(DEFAULT_MEDIA_ID)))
            .andExpect(jsonPath("$.[*].attachmentType").value(hasItem(DEFAULT_ATTACHMENT_TYPE)))
            .andExpect(jsonPath("$.[*].fileName").value(hasItem(DEFAULT_FILE_NAME)))
            .andExpect(jsonPath("$.[*].fileSize").value(hasItem(DEFAULT_FILE_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].mimeType").value(hasItem(DEFAULT_MIME_TYPE)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].thumbnailUrl").value(hasItem(DEFAULT_THUMBNAIL_URL)))
            .andExpect(jsonPath("$.[*].width").value(hasItem(DEFAULT_WIDTH)))
            .andExpect(jsonPath("$.[*].height").value(hasItem(DEFAULT_HEIGHT)))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)))
            .andExpect(jsonPath("$.[*].displayOrder").value(hasItem(DEFAULT_DISPLAY_ORDER)));
    }

    @Test
    @Transactional
    void getMessageAttachment() throws Exception {
        // Initialize the database
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());
        insertedMessageAttachment = messageAttachmentRepository.saveAndFlush(messageAttachment);

        // Get the messageAttachment
        restMessageAttachmentMockMvc
            .perform(get(ENTITY_API_URL_ID, messageAttachment.getAttachmentId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.attachmentId").value(messageAttachment.getAttachmentId()))
            .andExpect(jsonPath("$.mediaId").value(DEFAULT_MEDIA_ID))
            .andExpect(jsonPath("$.attachmentType").value(DEFAULT_ATTACHMENT_TYPE))
            .andExpect(jsonPath("$.fileName").value(DEFAULT_FILE_NAME))
            .andExpect(jsonPath("$.fileSize").value(DEFAULT_FILE_SIZE.intValue()))
            .andExpect(jsonPath("$.mimeType").value(DEFAULT_MIME_TYPE))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL))
            .andExpect(jsonPath("$.thumbnailUrl").value(DEFAULT_THUMBNAIL_URL))
            .andExpect(jsonPath("$.width").value(DEFAULT_WIDTH))
            .andExpect(jsonPath("$.height").value(DEFAULT_HEIGHT))
            .andExpect(jsonPath("$.duration").value(DEFAULT_DURATION))
            .andExpect(jsonPath("$.displayOrder").value(DEFAULT_DISPLAY_ORDER));
    }

    @Test
    @Transactional
    void getNonExistingMessageAttachment() throws Exception {
        // Get the messageAttachment
        restMessageAttachmentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMessageAttachment() throws Exception {
        // Initialize the database
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());
        insertedMessageAttachment = messageAttachmentRepository.saveAndFlush(messageAttachment);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageAttachment
        MessageAttachment updatedMessageAttachment = messageAttachmentRepository
            .findById(messageAttachment.getAttachmentId())
            .orElseThrow();
        // Disconnect from session so that the updates on updatedMessageAttachment are not directly saved in db
        em.detach(updatedMessageAttachment);
        updatedMessageAttachment
            .mediaId(UPDATED_MEDIA_ID)
            .attachmentType(UPDATED_ATTACHMENT_TYPE)
            .fileName(UPDATED_FILE_NAME)
            .fileSize(UPDATED_FILE_SIZE)
            .mimeType(UPDATED_MIME_TYPE)
            .url(UPDATED_URL)
            .thumbnailUrl(UPDATED_THUMBNAIL_URL)
            .width(UPDATED_WIDTH)
            .height(UPDATED_HEIGHT)
            .duration(UPDATED_DURATION)
            .displayOrder(UPDATED_DISPLAY_ORDER);
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(updatedMessageAttachment);

        restMessageAttachmentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, messageAttachmentDTO.getAttachmentId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageAttachmentDTO))
            )
            .andExpect(status().isOk());

        // Validate the MessageAttachment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMessageAttachmentToMatchAllProperties(updatedMessageAttachment);
    }

    @Test
    @Transactional
    void putNonExistingMessageAttachment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());

        // Create the MessageAttachment
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageAttachmentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, messageAttachmentDTO.getAttachmentId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageAttachmentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageAttachment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMessageAttachment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());

        // Create the MessageAttachment
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageAttachmentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageAttachmentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageAttachment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMessageAttachment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());

        // Create the MessageAttachment
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageAttachmentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageAttachmentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MessageAttachment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMessageAttachmentWithPatch() throws Exception {
        // Initialize the database
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());
        insertedMessageAttachment = messageAttachmentRepository.saveAndFlush(messageAttachment);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageAttachment using partial update
        MessageAttachment partialUpdatedMessageAttachment = new MessageAttachment();
        partialUpdatedMessageAttachment.setAttachmentId(messageAttachment.getAttachmentId());

        partialUpdatedMessageAttachment
            .attachmentType(UPDATED_ATTACHMENT_TYPE)
            .fileName(UPDATED_FILE_NAME)
            .fileSize(UPDATED_FILE_SIZE)
            .displayOrder(UPDATED_DISPLAY_ORDER);

        restMessageAttachmentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessageAttachment.getAttachmentId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMessageAttachment))
            )
            .andExpect(status().isOk());

        // Validate the MessageAttachment in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMessageAttachmentUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMessageAttachment, messageAttachment),
            getPersistedMessageAttachment(messageAttachment)
        );
    }

    @Test
    @Transactional
    void fullUpdateMessageAttachmentWithPatch() throws Exception {
        // Initialize the database
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());
        insertedMessageAttachment = messageAttachmentRepository.saveAndFlush(messageAttachment);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageAttachment using partial update
        MessageAttachment partialUpdatedMessageAttachment = new MessageAttachment();
        partialUpdatedMessageAttachment.setAttachmentId(messageAttachment.getAttachmentId());

        partialUpdatedMessageAttachment
            .mediaId(UPDATED_MEDIA_ID)
            .attachmentType(UPDATED_ATTACHMENT_TYPE)
            .fileName(UPDATED_FILE_NAME)
            .fileSize(UPDATED_FILE_SIZE)
            .mimeType(UPDATED_MIME_TYPE)
            .url(UPDATED_URL)
            .thumbnailUrl(UPDATED_THUMBNAIL_URL)
            .width(UPDATED_WIDTH)
            .height(UPDATED_HEIGHT)
            .duration(UPDATED_DURATION)
            .displayOrder(UPDATED_DISPLAY_ORDER);

        restMessageAttachmentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessageAttachment.getAttachmentId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMessageAttachment))
            )
            .andExpect(status().isOk());

        // Validate the MessageAttachment in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMessageAttachmentUpdatableFieldsEquals(
            partialUpdatedMessageAttachment,
            getPersistedMessageAttachment(partialUpdatedMessageAttachment)
        );
    }

    @Test
    @Transactional
    void patchNonExistingMessageAttachment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());

        // Create the MessageAttachment
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageAttachmentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, messageAttachmentDTO.getAttachmentId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(messageAttachmentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageAttachment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMessageAttachment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());

        // Create the MessageAttachment
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageAttachmentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(messageAttachmentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageAttachment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMessageAttachment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());

        // Create the MessageAttachment
        MessageAttachmentDTO messageAttachmentDTO = messageAttachmentMapper.toDto(messageAttachment);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageAttachmentMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(messageAttachmentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MessageAttachment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMessageAttachment() throws Exception {
        // Initialize the database
        messageAttachment.setAttachmentId(UUID.randomUUID().toString());
        insertedMessageAttachment = messageAttachmentRepository.saveAndFlush(messageAttachment);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the messageAttachment
        restMessageAttachmentMockMvc
            .perform(delete(ENTITY_API_URL_ID, messageAttachment.getAttachmentId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return messageAttachmentRepository.count();
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

    protected MessageAttachment getPersistedMessageAttachment(MessageAttachment messageAttachment) {
        return messageAttachmentRepository.findById(messageAttachment.getAttachmentId()).orElseThrow();
    }

    protected void assertPersistedMessageAttachmentToMatchAllProperties(MessageAttachment expectedMessageAttachment) {
        assertMessageAttachmentAllPropertiesEquals(expectedMessageAttachment, getPersistedMessageAttachment(expectedMessageAttachment));
    }

    protected void assertPersistedMessageAttachmentToMatchUpdatableProperties(MessageAttachment expectedMessageAttachment) {
        assertMessageAttachmentAllUpdatablePropertiesEquals(
            expectedMessageAttachment,
            getPersistedMessageAttachment(expectedMessageAttachment)
        );
    }
}
