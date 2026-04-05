package com.theblood.springfood.chat.web.rest;

import static com.theblood.springfood.chat.domain.BlockedUserAsserts.*;
import static com.theblood.springfood.chat.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.BlockedUser;
import com.theblood.springfood.chat.repository.BlockedUserRepository;
import com.theblood.springfood.chat.service.dto.BlockedUserDTO;
import com.theblood.springfood.chat.service.mapper.BlockedUserMapper;
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
 * Integration tests for the {@link BlockedUserResource} REST resources.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BlockedUserResourceIT {

    private static final String DEFAULT_BLOCKER_ID = "AAAAAAAAAA";
    private static final String UPDATED_BLOCKER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_BLOCKED_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_BLOCKED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REASON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/blocked-users";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{blockId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BlockedUserRepository blockedUserRepository;

    @Autowired
    private BlockedUserMapper blockedUserMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBlockedUserMockMvc;

    private BlockedUser blockedUser;

    private BlockedUser insertedBlockedUser;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BlockedUser createEntity() {
        return new BlockedUser()
            .blockId(UUID.randomUUID().toString())
            .blockerId(DEFAULT_BLOCKER_ID)
            .blockedUserId(DEFAULT_BLOCKED_USER_ID)
            .reason(DEFAULT_REASON);
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BlockedUser createUpdatedEntity() {
        return new BlockedUser()
            .blockId(UUID.randomUUID().toString())
            .blockerId(UPDATED_BLOCKER_ID)
            .blockedUserId(UPDATED_BLOCKED_USER_ID)
            .reason(UPDATED_REASON);
    }

    @BeforeEach
    void initTest() {
        blockedUser = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedBlockedUser != null) {
            blockedUserRepository.delete(insertedBlockedUser);
            insertedBlockedUser = null;
        }
    }

    @Test
    @Transactional
    void createBlockedUser() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the BlockedUser
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);
        var returnedBlockedUserDTO = om.readValue(
            restBlockedUserMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(blockedUserDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BlockedUserDTO.class
        );

        // Validate the BlockedUser in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBlockedUser = blockedUserMapper.toEntity(returnedBlockedUserDTO);
        assertBlockedUserUpdatableFieldsEquals(returnedBlockedUser, getPersistedBlockedUser(returnedBlockedUser));

        insertedBlockedUser = returnedBlockedUser;
    }

    @Test
    @Transactional
    void createBlockedUserWithExistingId() throws Exception {
        // Create the BlockedUser with an existing ID
        insertedBlockedUser = blockedUserRepository.saveAndFlush(blockedUser);
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBlockedUserMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(blockedUserDTO)))
            .andExpect(status().isBadRequest());

        // Validate the BlockedUser in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkBlockerIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        blockedUser.setBlockerId(null);

        // Create the BlockedUser, which fails.
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);

        restBlockedUserMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(blockedUserDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBlockedUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        blockedUser.setBlockedUserId(null);

        // Create the BlockedUser, which fails.
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);

        restBlockedUserMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(blockedUserDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBlockedUsers() throws Exception {
        // Initialize the database
        blockedUser.setBlockId(UUID.randomUUID().toString());
        insertedBlockedUser = blockedUserRepository.saveAndFlush(blockedUser);

        // Get all the blockedUserList
        restBlockedUserMockMvc
            .perform(get(ENTITY_API_URL + "?sort=blockId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].blockId").value(hasItem(blockedUser.getBlockId())))
            .andExpect(jsonPath("$.[*].blockerId").value(hasItem(DEFAULT_BLOCKER_ID)))
            .andExpect(jsonPath("$.[*].blockedUserId").value(hasItem(DEFAULT_BLOCKED_USER_ID)))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)));
    }

    @Test
    @Transactional
    void getBlockedUser() throws Exception {
        // Initialize the database
        blockedUser.setBlockId(UUID.randomUUID().toString());
        insertedBlockedUser = blockedUserRepository.saveAndFlush(blockedUser);

        // Get the blockedUser
        restBlockedUserMockMvc
            .perform(get(ENTITY_API_URL_ID, blockedUser.getBlockId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.blockId").value(blockedUser.getBlockId()))
            .andExpect(jsonPath("$.blockerId").value(DEFAULT_BLOCKER_ID))
            .andExpect(jsonPath("$.blockedUserId").value(DEFAULT_BLOCKED_USER_ID))
            .andExpect(jsonPath("$.reason").value(DEFAULT_REASON));
    }

    @Test
    @Transactional
    void getNonExistingBlockedUser() throws Exception {
        // Get the blockedUser
        restBlockedUserMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBlockedUser() throws Exception {
        // Initialize the database
        blockedUser.setBlockId(UUID.randomUUID().toString());
        insertedBlockedUser = blockedUserRepository.saveAndFlush(blockedUser);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the blockedUser
        BlockedUser updatedBlockedUser = blockedUserRepository.findById(blockedUser.getBlockId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBlockedUser are not directly saved in db
        em.detach(updatedBlockedUser);
        updatedBlockedUser.blockerId(UPDATED_BLOCKER_ID).blockedUserId(UPDATED_BLOCKED_USER_ID).reason(UPDATED_REASON);
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(updatedBlockedUser);

        restBlockedUserMockMvc
            .perform(
                put(ENTITY_API_URL_ID, blockedUserDTO.getBlockId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(blockedUserDTO))
            )
            .andExpect(status().isOk());

        // Validate the BlockedUser in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBlockedUserToMatchAllProperties(updatedBlockedUser);
    }

    @Test
    @Transactional
    void putNonExistingBlockedUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        blockedUser.setBlockId(UUID.randomUUID().toString());

        // Create the BlockedUser
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBlockedUserMockMvc
            .perform(
                put(ENTITY_API_URL_ID, blockedUserDTO.getBlockId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(blockedUserDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BlockedUser in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBlockedUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        blockedUser.setBlockId(UUID.randomUUID().toString());

        // Create the BlockedUser
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBlockedUserMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(blockedUserDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BlockedUser in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBlockedUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        blockedUser.setBlockId(UUID.randomUUID().toString());

        // Create the BlockedUser
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBlockedUserMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(blockedUserDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BlockedUser in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBlockedUserWithPatch() throws Exception {
        // Initialize the database
        blockedUser.setBlockId(UUID.randomUUID().toString());
        insertedBlockedUser = blockedUserRepository.saveAndFlush(blockedUser);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the blockedUser using partial update
        BlockedUser partialUpdatedBlockedUser = new BlockedUser();
        partialUpdatedBlockedUser.setBlockId(blockedUser.getBlockId());

        restBlockedUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBlockedUser.getBlockId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBlockedUser))
            )
            .andExpect(status().isOk());

        // Validate the BlockedUser in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBlockedUserUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedBlockedUser, blockedUser),
            getPersistedBlockedUser(blockedUser)
        );
    }

    @Test
    @Transactional
    void fullUpdateBlockedUserWithPatch() throws Exception {
        // Initialize the database
        blockedUser.setBlockId(UUID.randomUUID().toString());
        insertedBlockedUser = blockedUserRepository.saveAndFlush(blockedUser);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the blockedUser using partial update
        BlockedUser partialUpdatedBlockedUser = new BlockedUser();
        partialUpdatedBlockedUser.setBlockId(blockedUser.getBlockId());

        partialUpdatedBlockedUser.blockerId(UPDATED_BLOCKER_ID).blockedUserId(UPDATED_BLOCKED_USER_ID).reason(UPDATED_REASON);

        restBlockedUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBlockedUser.getBlockId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBlockedUser))
            )
            .andExpect(status().isOk());

        // Validate the BlockedUser in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBlockedUserUpdatableFieldsEquals(partialUpdatedBlockedUser, getPersistedBlockedUser(partialUpdatedBlockedUser));
    }

    @Test
    @Transactional
    void patchNonExistingBlockedUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        blockedUser.setBlockId(UUID.randomUUID().toString());

        // Create the BlockedUser
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBlockedUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, blockedUserDTO.getBlockId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(blockedUserDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BlockedUser in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBlockedUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        blockedUser.setBlockId(UUID.randomUUID().toString());

        // Create the BlockedUser
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBlockedUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(blockedUserDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BlockedUser in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBlockedUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        blockedUser.setBlockId(UUID.randomUUID().toString());

        // Create the BlockedUser
        BlockedUserDTO blockedUserDTO = blockedUserMapper.toDto(blockedUser);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBlockedUserMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(blockedUserDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BlockedUser in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBlockedUser() throws Exception {
        // Initialize the database
        blockedUser.setBlockId(UUID.randomUUID().toString());
        insertedBlockedUser = blockedUserRepository.saveAndFlush(blockedUser);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the blockedUser
        restBlockedUserMockMvc
            .perform(delete(ENTITY_API_URL_ID, blockedUser.getBlockId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return blockedUserRepository.count();
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

    protected BlockedUser getPersistedBlockedUser(BlockedUser blockedUser) {
        return blockedUserRepository.findById(blockedUser.getBlockId()).orElseThrow();
    }

    protected void assertPersistedBlockedUserToMatchAllProperties(BlockedUser expectedBlockedUser) {
        assertBlockedUserAllPropertiesEquals(expectedBlockedUser, getPersistedBlockedUser(expectedBlockedUser));
    }

    protected void assertPersistedBlockedUserToMatchUpdatableProperties(BlockedUser expectedBlockedUser) {
        assertBlockedUserAllUpdatablePropertiesEquals(expectedBlockedUser, getPersistedBlockedUser(expectedBlockedUser));
    }
}
