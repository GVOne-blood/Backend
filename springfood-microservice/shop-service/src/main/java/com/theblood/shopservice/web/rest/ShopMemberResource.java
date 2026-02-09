package com.theblood.shopservice.web.rest;

import com.theblood.shopservice.domain.ShopMember;
import com.theblood.shopservice.repository.ShopMemberRepository;
import com.theblood.shopservice.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.theblood.shopservice.domain.ShopMember}.
 */
@RestController
@RequestMapping("/api/shop-members")
@Transactional
public class ShopMemberResource {

    private static final Logger LOG = LoggerFactory.getLogger(ShopMemberResource.class);

    private static final String ENTITY_NAME = "shopServiceShopMember";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ShopMemberRepository shopMemberRepository;

    public ShopMemberResource(ShopMemberRepository shopMemberRepository) {
        this.shopMemberRepository = shopMemberRepository;
    }

    /**
     * {@code POST  /shop-members} : Create a new shopMember.
     *
     * @param shopMember the shopMember to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new shopMember, or with status {@code 400 (Bad Request)} if the shopMember has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ShopMember> createShopMember(@Valid @RequestBody ShopMember shopMember) throws URISyntaxException {
        LOG.debug("REST request to save ShopMember : {}", shopMember);
        if (shopMemberRepository.existsById(shopMember.getShopMemberId())) {
            throw new BadRequestAlertException("shopMember already exists", ENTITY_NAME, "idexists");
        }
        shopMember = shopMemberRepository.save(shopMember);
        return ResponseEntity.created(new URI("/api/shop-members/" + shopMember.getShopMemberId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, shopMember.getShopMemberId()))
            .body(shopMember);
    }

    /**
     * {@code PUT  /shop-members/:shopMemberId} : Updates an existing shopMember.
     *
     * @param shopMemberId the id of the shopMember to save.
     * @param shopMember the shopMember to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shopMember,
     * or with status {@code 400 (Bad Request)} if the shopMember is not valid,
     * or with status {@code 500 (Internal Server Error)} if the shopMember couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{shopMemberId}")
    public ResponseEntity<ShopMember> updateShopMember(
        @PathVariable(value = "shopMemberId", required = false) final String shopMemberId,
        @Valid @RequestBody ShopMember shopMember
    ) throws URISyntaxException {
        LOG.debug("REST request to update ShopMember : {}, {}", shopMemberId, shopMember);
        if (shopMember.getShopMemberId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(shopMemberId, shopMember.getShopMemberId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shopMemberRepository.existsById(shopMemberId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        shopMember.setIsPersisted();
        shopMember = shopMemberRepository.save(shopMember);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, shopMember.getShopMemberId()))
            .body(shopMember);
    }

    /**
     * {@code PATCH  /shop-members/:shopMemberId} : Partial updates given fields of an existing shopMember, field will ignore if it is null
     *
     * @param shopMemberId the id of the shopMember to save.
     * @param shopMember the shopMember to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shopMember,
     * or with status {@code 400 (Bad Request)} if the shopMember is not valid,
     * or with status {@code 404 (Not Found)} if the shopMember is not found,
     * or with status {@code 500 (Internal Server Error)} if the shopMember couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{shopMemberId}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ShopMember> partialUpdateShopMember(
        @PathVariable(value = "shopMemberId", required = false) final String shopMemberId,
        @NotNull @RequestBody ShopMember shopMember
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ShopMember partially : {}, {}", shopMemberId, shopMember);
        if (shopMember.getShopMemberId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(shopMemberId, shopMember.getShopMemberId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shopMemberRepository.existsById(shopMemberId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ShopMember> result = shopMemberRepository
            .findById(shopMember.getShopMemberId())
            .map(existingShopMember -> {
                if (shopMember.getShopId() != null) {
                    existingShopMember.setShopId(shopMember.getShopId());
                }
                if (shopMember.getUserId() != null) {
                    existingShopMember.setUserId(shopMember.getUserId());
                }
                if (shopMember.getRoleName() != null) {
                    existingShopMember.setRoleName(shopMember.getRoleName());
                }
                if (shopMember.getCreatedAt() != null) {
                    existingShopMember.setCreatedAt(shopMember.getCreatedAt());
                }
                if (shopMember.getUpdatedAt() != null) {
                    existingShopMember.setUpdatedAt(shopMember.getUpdatedAt());
                }
                if (shopMember.getDepartment() != null) {
                    existingShopMember.setDepartment(shopMember.getDepartment());
                }
                if (shopMember.getJoinDate() != null) {
                    existingShopMember.setJoinDate(shopMember.getJoinDate());
                }
                if (shopMember.getStatus() != null) {
                    existingShopMember.setStatus(shopMember.getStatus());
                }
                if (shopMember.getEndDate() != null) {
                    existingShopMember.setEndDate(shopMember.getEndDate());
                }
                if (shopMember.getWorkSchedule() != null) {
                    existingShopMember.setWorkSchedule(shopMember.getWorkSchedule());
                }
                if (shopMember.getSalaryType() != null) {
                    existingShopMember.setSalaryType(shopMember.getSalaryType());
                }
                if (shopMember.getBaseSalary() != null) {
                    existingShopMember.setBaseSalary(shopMember.getBaseSalary());
                }
                if (shopMember.getCommission() != null) {
                    existingShopMember.setCommission(shopMember.getCommission());
                }

                return existingShopMember;
            })
            .map(shopMemberRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, shopMember.getShopMemberId())
        );
    }

    /**
     * {@code GET  /shop-members} : get all the shopMembers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of shopMembers in body.
     */
    @GetMapping("")
    public List<ShopMember> getAllShopMembers() {
        LOG.debug("REST request to get all ShopMembers");
        return shopMemberRepository.findAll();
    }

    /**
     * {@code GET  /shop-members/:id} : get the "id" shopMember.
     *
     * @param id the id of the shopMember to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the shopMember, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShopMember> getShopMember(@PathVariable("id") String id) {
        LOG.debug("REST request to get ShopMember : {}", id);
        Optional<ShopMember> shopMember = shopMemberRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(shopMember);
    }

    /**
     * {@code DELETE  /shop-members/:id} : delete the "id" shopMember.
     *
     * @param id the id of the shopMember to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShopMember(@PathVariable("id") String id) {
        LOG.debug("REST request to delete ShopMember : {}", id);
        shopMemberRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
