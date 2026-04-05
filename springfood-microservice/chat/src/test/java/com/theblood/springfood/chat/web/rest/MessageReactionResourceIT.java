package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.MessageReactionAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReaction;
import com.theblood.springfood.chat.repository.MessageReactionRepository;
import com.theblood.springfood.chat.service.dto.MessageReactionDTO;
import com.theblood.springfood.chat.service.mapper.MessageReactionMapper;
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
 * Integration tests for the {@link MessageReactionResource} REST resources.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MessageReactionResourceIT {

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_EMOJI = "AAAAAAAAAA";
    private static final String UPDATED_EMOJI = "BBBBBBBBBB";

    private static final String DEFAULT_EMOJI_DISPLAY = "AAAAAAAAAA";
    private static final String UPDATED_EMOJI_DISPLAY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/message-reactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{reactionId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MessageReactionRepository messageReactionRepository;

    @Autowired
    private MessageReactionMapper messageReactionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMessageReactionMockMvc;

    private MessageReaction messageReaction;

    private MessageReaction insertedMessageReaction;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MessageReaction createEntity(EntityManager em) {
        MessageReaction messageReaction = new MessageReaction()
            .reactionId(UUID.randomUUID().toString())
            .userId(DEFAULT_USER_ID)
            .emoji(DEFAULT_EMOJI)
            .emojiDisplay(DEFAULT_EMOJI_DISPLAY);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        messageReaction.setMessage(message);
        return messageReaction;
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MessageReaction createUpdatedEntity(EntityManager em) {
        MessageReaction updatedMessageReaction = new MessageReaction()
            .reactionId(UUID.randomUUID().toString())
            .userId(UPDATED_USER_ID)
            .emoji(UPDATED_EMOJI)
            .emojiDisplay(UPDATED_EMOJI_DISPLAY);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createUpdatedEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        updatedMessageReaction.setMessage(message);
        return updatedMessageReaction;
    }

    @BeforeEach
    void initTest() {
        messageReaction = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedMessageReaction != null) {
            messageReactionRepository.delete(insertedMessageReaction);
            insertedMessageReaction = null;
        }
    }

    @Test
    @Transactional
    void createMessageReaction() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MessageReaction
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);
        var returnedMessageReactionDTO = om.readValue(
            restMessageReactionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReactionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MessageReactionDTO.class
        );

        // Validate the MessageReaction in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMessageReaction = messageReactionMapper.toEntity(returnedMessageReactionDTO);
        assertMessageReactionUpdatableFieldsEquals(returnedMessageReaction, getPersistedMessageReaction(returnedMessageReaction));

        insertedMessageReaction = returnedMessageReaction;
    }

    @Test
    @Transactional
    void createMessageReactionWithExistingId() throws Exception {
        // Create the MessageReaction with an existing ID
        insertedMessageReaction = messageReactionRepository.saveAndFlush(messageReaction);
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMessageReactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReactionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MessageReaction in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageReaction.setUserId(null);

        // Create the MessageReaction, which fails.
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);

        restMessageReactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEmojiIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        messageReaction.setEmoji(null);

        // Create the MessageReaction, which fails.
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);

        restMessageReactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMessageReactions() throws Exception {
        // Initialize the database
        messageReaction.setReactionId(UUID.randomUUID().toString());
        insertedMessageReaction = messageReactionRepository.saveAndFlush(messageReaction);

        // Get all the messageReactionList
        restMessageReactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=reactionId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].reactionId").value(hasItem(messageReaction.getReactionId())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].emoji").value(hasItem(DEFAULT_EMOJI)))
            .andExpect(jsonPath("$.[*].emojiDisplay").value(hasItem(DEFAULT_EMOJI_DISPLAY)));
    }

    @Test
    @Transactional
    void getMessageReaction() throws Exception {
        // Initialize the database
        messageReaction.setReactionId(UUID.randomUUID().toString());
        insertedMessageReaction = messageReactionRepository.saveAndFlush(messageReaction);

        // Get the messageReaction
        restMessageReactionMockMvc
            .perform(get(ENTITY_API_URL_ID, messageReaction.getReactionId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.reactionId").value(messageReaction.getReactionId()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.emoji").value(DEFAULT_EMOJI))
            .andExpect(jsonPath("$.emojiDisplay").value(DEFAULT_EMOJI_DISPLAY));
    }

    @Test
    @Transactional
    void getNonExistingMessageReaction() throws Exception {
        // Get the messageReaction
        restMessageReactionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMessageReaction() throws Exception {
        // Initialize the database
        messageReaction.setReactionId(UUID.randomUUID().toString());
        insertedMessageReaction = messageReactionRepository.saveAndFlush(messageReaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageReaction
        MessageReaction updatedMessageReaction = messageReactionRepository.findById(messageReaction.getReactionId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMessageReaction are not directly saved in db
        em.detach(updatedMessageReaction);
        updatedMessageReaction.userId(UPDATED_USER_ID).emoji(UPDATED_EMOJI).emojiDisplay(UPDATED_EMOJI_DISPLAY);
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(updatedMessageReaction);

        restMessageReactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, messageReactionDTO.getReactionId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageReactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the MessageReaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMessageReactionToMatchAllProperties(updatedMessageReaction);
    }

    @Test
    @Transactional
    void putNonExistingMessageReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReaction.setReactionId(UUID.randomUUID().toString());

        // Create the MessageReaction
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageReactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, messageReactionDTO.getReactionId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageReactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMessageReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReaction.setReactionId(UUID.randomUUID().toString());

        // Create the MessageReaction
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(messageReactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMessageReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReaction.setReactionId(UUID.randomUUID().toString());

        // Create the MessageReaction
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReactionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(messageReactionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MessageReaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMessageReactionWithPatch() throws Exception {
        // Initialize the database
        messageReaction.setReactionId(UUID.randomUUID().toString());
        insertedMessageReaction = messageReactionRepository.saveAndFlush(messageReaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageReaction using partial update
        MessageReaction partialUpdatedMessageReaction = new MessageReaction();
        partialUpdatedMessageReaction.setReactionId(messageReaction.getReactionId());

        partialUpdatedMessageReaction.userId(UPDATED_USER_ID);

        restMessageReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessageReaction.getReactionId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMessageReaction))
            )
            .andExpect(status().isOk());

        // Validate the MessageReaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMessageReactionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMessageReaction, messageReaction),
            getPersistedMessageReaction(messageReaction)
        );
    }

    @Test
    @Transactional
    void fullUpdateMessageReactionWithPatch() throws Exception {
        // Initialize the database
        messageReaction.setReactionId(UUID.randomUUID().toString());
        insertedMessageReaction = messageReactionRepository.saveAndFlush(messageReaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the messageReaction using partial update
        MessageReaction partialUpdatedMessageReaction = new MessageReaction();
        partialUpdatedMessageReaction.setReactionId(messageReaction.getReactionId());

        partialUpdatedMessageReaction.userId(UPDATED_USER_ID).emoji(UPDATED_EMOJI).emojiDisplay(UPDATED_EMOJI_DISPLAY);

        restMessageReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessageReaction.getReactionId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMessageReaction))
            )
            .andExpect(status().isOk());

        // Validate the MessageReaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMessageReactionUpdatableFieldsEquals(
            partialUpdatedMessageReaction,
            getPersistedMessageReaction(partialUpdatedMessageReaction)
        );
    }

    @Test
    @Transactional
    void patchNonExistingMessageReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReaction.setReactionId(UUID.randomUUID().toString());

        // Create the MessageReaction
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, messageReactionDTO.getReactionId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(messageReactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMessageReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReaction.setReactionId(UUID.randomUUID().toString());

        // Create the MessageReaction
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(messageReactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MessageReaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMessageReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        messageReaction.setReactionId(UUID.randomUUID().toString());

        // Create the MessageReaction
        MessageReactionDTO messageReactionDTO = messageReactionMapper.toDto(messageReaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageReactionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(messageReactionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MessageReaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMessageReaction() throws Exception {
        // Initialize the database
        messageReaction.setReactionId(UUID.randomUUID().toString());
        insertedMessageReaction = messageReactionRepository.saveAndFlush(messageReaction);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the messageReaction
        restMessageReactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, messageReaction.getReactionId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return messageReactionRepository.count();
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

    protected MessageReaction getPersistedMessageReaction(MessageReaction messageReaction) {
        return messageReactionRepository.findById(messageReaction.getReactionId()).orElseThrow();
    }

    protected void assertPersistedMessageReactionToMatchAllProperties(MessageReaction expectedMessageReaction) {
        assertMessageReactionAllPropertiesEquals(expectedMessageReaction, getPersistedMessageReaction(expectedMessageReaction));
    }

    protected void assertPersistedMessageReactionToMatchUpdatableProperties(MessageReaction expectedMessageReaction) {
        assertMessageReactionAllUpdatablePropertiesEquals(expectedMessageReaction, getPersistedMessageReaction(expectedMessageReaction));
    }
}
