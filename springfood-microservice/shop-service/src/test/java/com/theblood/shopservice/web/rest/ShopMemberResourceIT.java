package com.theblood.shopservice.web.rest;

import static com.theblood.shopservice.domain.ShopMemberAsserts.*;
import static com.theblood.shopservice.web.rest.TestUtil.createUpdateProxyForBean;
import static com.theblood.shopservice.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.shopservice.IntegrationTest;
import com.theblood.shopservice.domain.ShopMember;
import com.theblood.shopservice.repository.ShopMemberRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link ShopMemberResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ShopMemberResourceIT {

    private static final String DEFAULT_SHOP_ID = "AAAAAAAAAA";
    private static final String UPDATED_SHOP_ID = "BBBBBBBBBB";

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_ROLE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ROLE_NAME = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_DEPARTMENT = "AAAAAAAAAA";
    private static final String UPDATED_DEPARTMENT = "BBBBBBBBBB";

    private static final String DEFAULT_JOIN_DATE = "AAAAAAAAAA";
    private static final String UPDATED_JOIN_DATE = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String DEFAULT_END_DATE = "AAAAAAAAAA";
    private static final String UPDATED_END_DATE = "BBBBBBBBBB";

    private static final String DEFAULT_WORK_SCHEDULE = "AAAAAAAAAA";
    private static final String UPDATED_WORK_SCHEDULE = "BBBBBBBBBB";

    private static final String DEFAULT_SALARY_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_SALARY_TYPE = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_BASE_SALARY = new BigDecimal(1);
    private static final BigDecimal UPDATED_BASE_SALARY = new BigDecimal(2);

    private static final BigDecimal DEFAULT_COMMISSION = new BigDecimal(1);
    private static final BigDecimal UPDATED_COMMISSION = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/shop-members";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{shopMemberId}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ShopMemberRepository shopMemberRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restShopMemberMockMvc;

    private ShopMember shopMember;

    private ShopMember insertedShopMember;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ShopMember createEntity() {
        return new ShopMember()
            .shopMemberId(UUID.randomUUID().toString())
            .shopId(DEFAULT_SHOP_ID)
            .userId(DEFAULT_USER_ID)
            .roleName(DEFAULT_ROLE_NAME)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT)
            .department(DEFAULT_DEPARTMENT)
            .joinDate(DEFAULT_JOIN_DATE)
            .status(DEFAULT_STATUS)
            .endDate(DEFAULT_END_DATE)
            .workSchedule(DEFAULT_WORK_SCHEDULE)
            .salaryType(DEFAULT_SALARY_TYPE)
            .baseSalary(DEFAULT_BASE_SALARY)
            .commission(DEFAULT_COMMISSION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ShopMember createUpdatedEntity() {
        return new ShopMember()
            .shopMemberId(UUID.randomUUID().toString())
            .shopId(UPDATED_SHOP_ID)
            .userId(UPDATED_USER_ID)
            .roleName(UPDATED_ROLE_NAME)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .department(UPDATED_DEPARTMENT)
            .joinDate(UPDATED_JOIN_DATE)
            .status(UPDATED_STATUS)
            .endDate(UPDATED_END_DATE)
            .workSchedule(UPDATED_WORK_SCHEDULE)
            .salaryType(UPDATED_SALARY_TYPE)
            .baseSalary(UPDATED_BASE_SALARY)
            .commission(UPDATED_COMMISSION);
    }

    @BeforeEach
    void initTest() {
        shopMember = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedShopMember != null) {
            shopMemberRepository.delete(insertedShopMember);
            insertedShopMember = null;
        }
    }

    @Test
    @Transactional
    void createShopMember() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ShopMember
        var returnedShopMember = om.readValue(
            restShopMemberMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shopMember)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ShopMember.class
        );

        // Validate the ShopMember in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertShopMemberUpdatableFieldsEquals(returnedShopMember, getPersistedShopMember(returnedShopMember));

        insertedShopMember = returnedShopMember;
    }

    @Test
    @Transactional
    void createShopMemberWithExistingId() throws Exception {
        // Create the ShopMember with an existing ID
        insertedShopMember = shopMemberRepository.saveAndFlush(shopMember);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restShopMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shopMember)))
            .andExpect(status().isBadRequest());

        // Validate the ShopMember in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllShopMembers() throws Exception {
        // Initialize the database
        shopMember.setShopMemberId(UUID.randomUUID().toString());
        insertedShopMember = shopMemberRepository.saveAndFlush(shopMember);

        // Get all the shopMemberList
        restShopMemberMockMvc
            .perform(get(ENTITY_API_URL + "?sort=shopMemberId,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].shopMemberId").value(hasItem(shopMember.getShopMemberId())))
            .andExpect(jsonPath("$.[*].shopId").value(hasItem(DEFAULT_SHOP_ID)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].roleName").value(hasItem(DEFAULT_ROLE_NAME)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].department").value(hasItem(DEFAULT_DEPARTMENT)))
            .andExpect(jsonPath("$.[*].joinDate").value(hasItem(DEFAULT_JOIN_DATE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE)))
            .andExpect(jsonPath("$.[*].workSchedule").value(hasItem(DEFAULT_WORK_SCHEDULE)))
            .andExpect(jsonPath("$.[*].salaryType").value(hasItem(DEFAULT_SALARY_TYPE)))
            .andExpect(jsonPath("$.[*].baseSalary").value(hasItem(sameNumber(DEFAULT_BASE_SALARY))))
            .andExpect(jsonPath("$.[*].commission").value(hasItem(sameNumber(DEFAULT_COMMISSION))));
    }

    @Test
    @Transactional
    void getShopMember() throws Exception {
        // Initialize the database
        shopMember.setShopMemberId(UUID.randomUUID().toString());
        insertedShopMember = shopMemberRepository.saveAndFlush(shopMember);

        // Get the shopMember
        restShopMemberMockMvc
            .perform(get(ENTITY_API_URL_ID, shopMember.getShopMemberId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.shopMemberId").value(shopMember.getShopMemberId()))
            .andExpect(jsonPath("$.shopId").value(DEFAULT_SHOP_ID))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.roleName").value(DEFAULT_ROLE_NAME))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()))
            .andExpect(jsonPath("$.department").value(DEFAULT_DEPARTMENT))
            .andExpect(jsonPath("$.joinDate").value(DEFAULT_JOIN_DATE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE))
            .andExpect(jsonPath("$.workSchedule").value(DEFAULT_WORK_SCHEDULE))
            .andExpect(jsonPath("$.salaryType").value(DEFAULT_SALARY_TYPE))
            .andExpect(jsonPath("$.baseSalary").value(sameNumber(DEFAULT_BASE_SALARY)))
            .andExpect(jsonPath("$.commission").value(sameNumber(DEFAULT_COMMISSION)));
    }

    @Test
    @Transactional
    void getNonExistingShopMember() throws Exception {
        // Get the shopMember
        restShopMemberMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingShopMember() throws Exception {
        // Initialize the database
        shopMember.setShopMemberId(UUID.randomUUID().toString());
        insertedShopMember = shopMemberRepository.saveAndFlush(shopMember);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the shopMember
        ShopMember updatedShopMember = shopMemberRepository.findById(shopMember.getShopMemberId()).orElseThrow();
        // Disconnect from session so that the updates on updatedShopMember are not directly saved in db
        em.detach(updatedShopMember);
        updatedShopMember
            .shopId(UPDATED_SHOP_ID)
            .userId(UPDATED_USER_ID)
            .roleName(UPDATED_ROLE_NAME)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .department(UPDATED_DEPARTMENT)
            .joinDate(UPDATED_JOIN_DATE)
            .status(UPDATED_STATUS)
            .endDate(UPDATED_END_DATE)
            .workSchedule(UPDATED_WORK_SCHEDULE)
            .salaryType(UPDATED_SALARY_TYPE)
            .baseSalary(UPDATED_BASE_SALARY)
            .commission(UPDATED_COMMISSION);

        restShopMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedShopMember.getShopMemberId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedShopMember))
            )
            .andExpect(status().isOk());

        // Validate the ShopMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedShopMemberToMatchAllProperties(updatedShopMember);
    }

    @Test
    @Transactional
    void putNonExistingShopMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopMember.setShopMemberId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShopMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shopMember.getShopMemberId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shopMember))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchShopMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopMember.setShopMemberId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shopMember))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamShopMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopMember.setShopMemberId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopMemberMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shopMember)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ShopMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateShopMemberWithPatch() throws Exception {
        // Initialize the database
        shopMember.setShopMemberId(UUID.randomUUID().toString());
        insertedShopMember = shopMemberRepository.saveAndFlush(shopMember);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the shopMember using partial update
        ShopMember partialUpdatedShopMember = new ShopMember();
        partialUpdatedShopMember.setShopMemberId(shopMember.getShopMemberId());

        partialUpdatedShopMember
            .updatedAt(UPDATED_UPDATED_AT)
            .salaryType(UPDATED_SALARY_TYPE)
            .baseSalary(UPDATED_BASE_SALARY)
            .commission(UPDATED_COMMISSION);

        restShopMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShopMember.getShopMemberId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedShopMember))
            )
            .andExpect(status().isOk());

        // Validate the ShopMember in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertShopMemberUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedShopMember, shopMember),
            getPersistedShopMember(shopMember)
        );
    }

    @Test
    @Transactional
    void fullUpdateShopMemberWithPatch() throws Exception {
        // Initialize the database
        shopMember.setShopMemberId(UUID.randomUUID().toString());
        insertedShopMember = shopMemberRepository.saveAndFlush(shopMember);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the shopMember using partial update
        ShopMember partialUpdatedShopMember = new ShopMember();
        partialUpdatedShopMember.setShopMemberId(shopMember.getShopMemberId());

        partialUpdatedShopMember
            .shopId(UPDATED_SHOP_ID)
            .userId(UPDATED_USER_ID)
            .roleName(UPDATED_ROLE_NAME)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .department(UPDATED_DEPARTMENT)
            .joinDate(UPDATED_JOIN_DATE)
            .status(UPDATED_STATUS)
            .endDate(UPDATED_END_DATE)
            .workSchedule(UPDATED_WORK_SCHEDULE)
            .salaryType(UPDATED_SALARY_TYPE)
            .baseSalary(UPDATED_BASE_SALARY)
            .commission(UPDATED_COMMISSION);

        restShopMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShopMember.getShopMemberId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedShopMember))
            )
            .andExpect(status().isOk());

        // Validate the ShopMember in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertShopMemberUpdatableFieldsEquals(partialUpdatedShopMember, getPersistedShopMember(partialUpdatedShopMember));
    }

    @Test
    @Transactional
    void patchNonExistingShopMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopMember.setShopMemberId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShopMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, shopMember.getShopMemberId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shopMember))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchShopMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopMember.setShopMemberId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shopMember))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamShopMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shopMember.setShopMemberId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopMemberMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(shopMember)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ShopMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteShopMember() throws Exception {
        // Initialize the database
        shopMember.setShopMemberId(UUID.randomUUID().toString());
        insertedShopMember = shopMemberRepository.saveAndFlush(shopMember);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the shopMember
        restShopMemberMockMvc
            .perform(delete(ENTITY_API_URL_ID, shopMember.getShopMemberId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return shopMemberRepository.count();
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

    protected ShopMember getPersistedShopMember(ShopMember shopMember) {
        return shopMemberRepository.findById(shopMember.getShopMemberId()).orElseThrow();
    }

    protected void assertPersistedShopMemberToMatchAllProperties(ShopMember expectedShopMember) {
        assertShopMemberAllPropertiesEquals(expectedShopMember, getPersistedShopMember(expectedShopMember));
    }

    protected void assertPersistedShopMemberToMatchUpdatableProperties(ShopMember expectedShopMember) {
        assertShopMemberAllUpdatablePropertiesEquals(expectedShopMember, getPersistedShopMember(expectedShopMember));
    }
}
