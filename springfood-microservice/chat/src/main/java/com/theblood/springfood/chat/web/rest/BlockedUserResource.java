package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.BlockedUserRepository;
import com.theblood.springfood.chat.service.BlockedUserService;
import com.theblood.springfood.chat.service.dto.BlockedUserDTO;
import com.theblood.springfood.chat.web.rest.errors.BadRequestAlertException;
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
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST resources for managing {@link com.theblood.springfood.chat.domain.BlockedUser}.
 */
@RestController
@RequestMapping("/api/blocked-users")
public class BlockedUserResource {

    private static final Logger LOG = LoggerFactory.getLogger(BlockedUserResource.class);

    private static final String ENTITY_NAME = "chatBlockedUser";
    private final BlockedUserService blockedUserService;
    private final BlockedUserRepository blockedUserRepository;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public BlockedUserResource(BlockedUserService blockedUserService, BlockedUserRepository blockedUserRepository) {
        this.blockedUserService = blockedUserService;
        this.blockedUserRepository = blockedUserRepository;
    }

    /**
     * {@code POST  /blocked-users} : Create a new blockedUser.
     *
     * @param blockedUserDTO the blockedUserDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new blockedUserDTO, or with status {@code 400 (Bad Request)} if the blockedUser has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BlockedUserDTO> createBlockedUser(@Valid @RequestBody BlockedUserDTO blockedUserDTO) throws URISyntaxException {
        LOG.debug("REST request to save BlockedUser : {}", blockedUserDTO);
        if (blockedUserRepository.existsById(blockedUserDTO.getBlockId())) {
            throw new BadRequestAlertException("blockedUser already exists", ENTITY_NAME, "idexists");
        }
        blockedUserDTO = blockedUserService.save(blockedUserDTO);
        return ResponseEntity.created(new URI("/api/blocked-users/" + blockedUserDTO.getBlockId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, blockedUserDTO.getBlockId()))
            .body(blockedUserDTO);
    }

    /**
     * {@code PUT  /blocked-users/:blockId} : Updates an existing blockedUser.
     *
     * @param blockId        the id of the blockedUserDTO to save.
     * @param blockedUserDTO the blockedUserDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated blockedUserDTO,
     * or with status {@code 400 (Bad Request)} if the blockedUserDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the blockedUserDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{blockId}")
    public ResponseEntity<BlockedUserDTO> updateBlockedUser(
        @PathVariable(value = "blockId", required = false) final String blockId,
        @Valid @RequestBody BlockedUserDTO blockedUserDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update BlockedUser : {}, {}", blockId, blockedUserDTO);
        if (blockedUserDTO.getBlockId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(blockId, blockedUserDTO.getBlockId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!blockedUserRepository.existsById(blockId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        blockedUserDTO = blockedUserService.update(blockedUserDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, blockedUserDTO.getBlockId()))
            .body(blockedUserDTO);
    }

    /**
     * {@code PATCH  /blocked-users/:blockId} : Partial updates given fields of an existing blockedUser, field will ignore if it is null
     *
     * @param blockId        the id of the blockedUserDTO to save.
     * @param blockedUserDTO the blockedUserDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated blockedUserDTO,
     * or with status {@code 400 (Bad Request)} if the blockedUserDTO is not valid,
     * or with status {@code 404 (Not Found)} if the blockedUserDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the blockedUserDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{blockId}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<BlockedUserDTO> partialUpdateBlockedUser(
        @PathVariable(value = "blockId", required = false) final String blockId,
        @NotNull @RequestBody BlockedUserDTO blockedUserDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BlockedUser partially : {}, {}", blockId, blockedUserDTO);
        if (blockedUserDTO.getBlockId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(blockId, blockedUserDTO.getBlockId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!blockedUserRepository.existsById(blockId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BlockedUserDTO> result = blockedUserService.partialUpdate(blockedUserDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, blockedUserDTO.getBlockId())
        );
    }

    /**
     * {@code GET  /blocked-users} : get all the blockedUsers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of blockedUsers in body.
     */
    @GetMapping("")
    public List<BlockedUserDTO> getAllBlockedUsers() {
        LOG.debug("REST request to get all BlockedUsers");
        return blockedUserService.findAll();
    }

    /**
     * {@code GET  /blocked-users/:id} : get the "id" blockedUser.
     *
     * @param id the id of the blockedUserDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the blockedUserDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BlockedUserDTO> getBlockedUser(@PathVariable("id") String id) {
        LOG.debug("REST request to get BlockedUser : {}", id);
        Optional<BlockedUserDTO> blockedUserDTO = blockedUserService.findOne(id);
        return ResponseUtil.wrapOrNotFound(blockedUserDTO);
    }

    /**
     * {@code DELETE  /blocked-users/:id} : delete the "id" blockedUser.
     *
     * @param id the id of the blockedUserDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlockedUser(@PathVariable("id") String id) {
        LOG.debug("REST request to delete BlockedUser : {}", id);
        blockedUserService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
