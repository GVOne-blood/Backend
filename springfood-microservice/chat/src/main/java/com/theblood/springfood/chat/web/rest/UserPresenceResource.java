package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.UserPresenceRepository;
import com.theblood.springfood.chat.service.UserPresenceService;
import com.theblood.springfood.chat.service.dto.UserPresenceDTO;
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
 * REST controller for managing {@link com.theblood.springfood.chat.domain.UserPresence}.
 */
@RestController
@RequestMapping("/api/user-presences")
public class UserPresenceResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserPresenceResource.class);

    private static final String ENTITY_NAME = "chatUserPresence";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserPresenceService userPresenceService;

    private final UserPresenceRepository userPresenceRepository;

    public UserPresenceResource(UserPresenceService userPresenceService, UserPresenceRepository userPresenceRepository) {
        this.userPresenceService = userPresenceService;
        this.userPresenceRepository = userPresenceRepository;
    }

    /**
     * {@code POST  /user-presences} : Create a new userPresence.
     *
     * @param userPresenceDTO the userPresenceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new userPresenceDTO, or with status {@code 400 (Bad Request)} if the userPresence has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<UserPresenceDTO> createUserPresence(@Valid @RequestBody UserPresenceDTO userPresenceDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save UserPresence : {}", userPresenceDTO);
        if (userPresenceRepository.existsById(userPresenceDTO.getPresenceId())) {
            throw new BadRequestAlertException("userPresence already exists", ENTITY_NAME, "idexists");
        }
        userPresenceDTO = userPresenceService.save(userPresenceDTO);
        return ResponseEntity.created(new URI("/api/user-presences/" + userPresenceDTO.getPresenceId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, userPresenceDTO.getPresenceId()))
            .body(userPresenceDTO);
    }

    /**
     * {@code PUT  /user-presences/:presenceId} : Updates an existing userPresence.
     *
     * @param presenceId the id of the userPresenceDTO to save.
     * @param userPresenceDTO the userPresenceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userPresenceDTO,
     * or with status {@code 400 (Bad Request)} if the userPresenceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the userPresenceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{presenceId}")
    public ResponseEntity<UserPresenceDTO> updateUserPresence(
        @PathVariable(value = "presenceId", required = false) final String presenceId,
        @Valid @RequestBody UserPresenceDTO userPresenceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update UserPresence : {}, {}", presenceId, userPresenceDTO);
        if (userPresenceDTO.getPresenceId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(presenceId, userPresenceDTO.getPresenceId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!userPresenceRepository.existsById(presenceId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        userPresenceDTO = userPresenceService.update(userPresenceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userPresenceDTO.getPresenceId()))
            .body(userPresenceDTO);
    }

    /**
     * {@code PATCH  /user-presences/:presenceId} : Partial updates given fields of an existing userPresence, field will ignore if it is null
     *
     * @param presenceId the id of the userPresenceDTO to save.
     * @param userPresenceDTO the userPresenceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userPresenceDTO,
     * or with status {@code 400 (Bad Request)} if the userPresenceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the userPresenceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the userPresenceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{presenceId}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<UserPresenceDTO> partialUpdateUserPresence(
        @PathVariable(value = "presenceId", required = false) final String presenceId,
        @NotNull @RequestBody UserPresenceDTO userPresenceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update UserPresence partially : {}, {}", presenceId, userPresenceDTO);
        if (userPresenceDTO.getPresenceId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(presenceId, userPresenceDTO.getPresenceId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!userPresenceRepository.existsById(presenceId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<UserPresenceDTO> result = userPresenceService.partialUpdate(userPresenceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userPresenceDTO.getPresenceId())
        );
    }

    /**
     * {@code GET  /user-presences} : get all the userPresences.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of userPresences in body.
     */
    @GetMapping("")
    public List<UserPresenceDTO> getAllUserPresences() {
        LOG.debug("REST request to get all UserPresences");
        return userPresenceService.findAll();
    }

    /**
     * {@code GET  /user-presences/:id} : get the "id" userPresence.
     *
     * @param id the id of the userPresenceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the userPresenceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserPresenceDTO> getUserPresence(@PathVariable("id") String id) {
        LOG.debug("REST request to get UserPresence : {}", id);
        Optional<UserPresenceDTO> userPresenceDTO = userPresenceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(userPresenceDTO);
    }

    /**
     * {@code DELETE  /user-presences/:id} : delete the "id" userPresence.
     *
     * @param id the id of the userPresenceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserPresence(@PathVariable("id") String id) {
        LOG.debug("REST request to delete UserPresence : {}", id);
        userPresenceService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
