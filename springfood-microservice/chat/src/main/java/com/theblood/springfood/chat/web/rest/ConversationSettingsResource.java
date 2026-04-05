package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.ConversationSettingsRepository;
import com.theblood.springfood.chat.service.ConversationSettingsService;
import com.theblood.springfood.chat.service.dto.ConversationSettingsDTO;
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
 * REST resources for managing {@link com.theblood.springfood.chat.domain.ConversationSettings}.
 */
@RestController
@RequestMapping("/api/conversation-settings")
public class ConversationSettingsResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationSettingsResource.class);

    private static final String ENTITY_NAME = "chatConversationSettings";
    private final ConversationSettingsService conversationSettingsService;
    private final ConversationSettingsRepository conversationSettingsRepository;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public ConversationSettingsResource(
        ConversationSettingsService conversationSettingsService,
        ConversationSettingsRepository conversationSettingsRepository
    ) {
        this.conversationSettingsService = conversationSettingsService;
        this.conversationSettingsRepository = conversationSettingsRepository;
    }

    /**
     * {@code POST  /conversation-settings} : Create a new conversationSettings.
     *
     * @param conversationSettingsDTO the conversationSettingsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new conversationSettingsDTO, or with status {@code 400 (Bad Request)} if the conversationSettings has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ConversationSettingsDTO> createConversationSettings(
        @Valid @RequestBody ConversationSettingsDTO conversationSettingsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save ConversationSettings : {}", conversationSettingsDTO);
        if (conversationSettingsRepository.existsById(conversationSettingsDTO.getSettingsId())) {
            throw new BadRequestAlertException("conversationSettings already exists", ENTITY_NAME, "idexists");
        }
        conversationSettingsDTO = conversationSettingsService.save(conversationSettingsDTO);
        return ResponseEntity.created(new URI("/api/conversation-settings/" + conversationSettingsDTO.getSettingsId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, conversationSettingsDTO.getSettingsId()))
            .body(conversationSettingsDTO);
    }

    /**
     * {@code PUT  /conversation-settings/:settingsId} : Updates an existing conversationSettings.
     *
     * @param settingsId              the id of the conversationSettingsDTO to save.
     * @param conversationSettingsDTO the conversationSettingsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated conversationSettingsDTO,
     * or with status {@code 400 (Bad Request)} if the conversationSettingsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the conversationSettingsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{settingsId}")
    public ResponseEntity<ConversationSettingsDTO> updateConversationSettings(
        @PathVariable(value = "settingsId", required = false) final String settingsId,
        @Valid @RequestBody ConversationSettingsDTO conversationSettingsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ConversationSettings : {}, {}", settingsId, conversationSettingsDTO);
        if (conversationSettingsDTO.getSettingsId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(settingsId, conversationSettingsDTO.getSettingsId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!conversationSettingsRepository.existsById(settingsId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        conversationSettingsDTO = conversationSettingsService.update(conversationSettingsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, conversationSettingsDTO.getSettingsId()))
            .body(conversationSettingsDTO);
    }

    /**
     * {@code PATCH  /conversation-settings/:settingsId} : Partial updates given fields of an existing conversationSettings, field will ignore if it is null
     *
     * @param settingsId              the id of the conversationSettingsDTO to save.
     * @param conversationSettingsDTO the conversationSettingsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated conversationSettingsDTO,
     * or with status {@code 400 (Bad Request)} if the conversationSettingsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the conversationSettingsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the conversationSettingsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{settingsId}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<ConversationSettingsDTO> partialUpdateConversationSettings(
        @PathVariable(value = "settingsId", required = false) final String settingsId,
        @NotNull @RequestBody ConversationSettingsDTO conversationSettingsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ConversationSettings partially : {}, {}", settingsId, conversationSettingsDTO);
        if (conversationSettingsDTO.getSettingsId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(settingsId, conversationSettingsDTO.getSettingsId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!conversationSettingsRepository.existsById(settingsId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ConversationSettingsDTO> result = conversationSettingsService.partialUpdate(conversationSettingsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, conversationSettingsDTO.getSettingsId())
        );
    }

    /**
     * {@code GET  /conversation-settings} : get all the conversationSettings.
     *
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of conversationSettings in body.
     */
    @GetMapping("")
    public List<ConversationSettingsDTO> getAllConversationSettings(@RequestParam(name = "filter", required = false) String filter) {
        if ("conversation-is-null".equals(filter)) {
            LOG.debug("REST request to get all ConversationSettingss where conversation is null");
            return conversationSettingsService.findAllWhereConversationIsNull();
        }
        LOG.debug("REST request to get all ConversationSettings");
        return conversationSettingsService.findAll();
    }

    /**
     * {@code GET  /conversation-settings/:id} : get the "id" conversationSettings.
     *
     * @param id the id of the conversationSettingsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the conversationSettingsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConversationSettingsDTO> getConversationSettings(@PathVariable("id") String id) {
        LOG.debug("REST request to get ConversationSettings : {}", id);
        Optional<ConversationSettingsDTO> conversationSettingsDTO = conversationSettingsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(conversationSettingsDTO);
    }

    /**
     * {@code DELETE  /conversation-settings/:id} : delete the "id" conversationSettings.
     *
     * @param id the id of the conversationSettingsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversationSettings(@PathVariable("id") String id) {
        LOG.debug("REST request to delete ConversationSettings : {}", id);
        conversationSettingsService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
