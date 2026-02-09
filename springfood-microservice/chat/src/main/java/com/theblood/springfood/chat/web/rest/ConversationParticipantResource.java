package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.service.ConversationParticipantService;
import com.theblood.springfood.chat.service.dto.ConversationParticipantDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.theblood.springfood.chat.domain.ConversationParticipant}.
 */
@RestController
@RequestMapping("/api/conversation-participants")
public class ConversationParticipantResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationParticipantResource.class);

    private static final String ENTITY_NAME = "chatConversationParticipant";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ConversationParticipantService conversationParticipantService;

    private final ConversationParticipantRepository conversationParticipantRepository;

    public ConversationParticipantResource(
        ConversationParticipantService conversationParticipantService,
        ConversationParticipantRepository conversationParticipantRepository
    ) {
        this.conversationParticipantService = conversationParticipantService;
        this.conversationParticipantRepository = conversationParticipantRepository;
    }

    /**
     * {@code POST  /conversation-participants} : Create a new conversationParticipant.
     *
     * @param conversationParticipantDTO the conversationParticipantDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new conversationParticipantDTO, or with status {@code 400 (Bad Request)} if the conversationParticipant has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ConversationParticipantDTO> createConversationParticipant(
        @Valid @RequestBody ConversationParticipantDTO conversationParticipantDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save ConversationParticipant : {}", conversationParticipantDTO);
        if (conversationParticipantRepository.existsById(conversationParticipantDTO.getParticipantId())) {
            throw new BadRequestAlertException("conversationParticipant already exists", ENTITY_NAME, "idexists");
        }
        conversationParticipantDTO = conversationParticipantService.save(conversationParticipantDTO);
        return ResponseEntity.created(new URI("/api/conversation-participants/" + conversationParticipantDTO.getParticipantId()))
            .headers(
                HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, conversationParticipantDTO.getParticipantId())
            )
            .body(conversationParticipantDTO);
    }

    /**
     * {@code PUT  /conversation-participants/:participantId} : Updates an existing conversationParticipant.
     *
     * @param participantId the id of the conversationParticipantDTO to save.
     * @param conversationParticipantDTO the conversationParticipantDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated conversationParticipantDTO,
     * or with status {@code 400 (Bad Request)} if the conversationParticipantDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the conversationParticipantDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{participantId}")
    public ResponseEntity<ConversationParticipantDTO> updateConversationParticipant(
        @PathVariable(value = "participantId", required = false) final String participantId,
        @Valid @RequestBody ConversationParticipantDTO conversationParticipantDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ConversationParticipant : {}, {}", participantId, conversationParticipantDTO);
        if (conversationParticipantDTO.getParticipantId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(participantId, conversationParticipantDTO.getParticipantId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!conversationParticipantRepository.existsById(participantId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        conversationParticipantDTO = conversationParticipantService.update(conversationParticipantDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, conversationParticipantDTO.getParticipantId()))
            .body(conversationParticipantDTO);
    }

    /**
     * {@code PATCH  /conversation-participants/:participantId} : Partial updates given fields of an existing conversationParticipant, field will ignore if it is null
     *
     * @param participantId the id of the conversationParticipantDTO to save.
     * @param conversationParticipantDTO the conversationParticipantDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated conversationParticipantDTO,
     * or with status {@code 400 (Bad Request)} if the conversationParticipantDTO is not valid,
     * or with status {@code 404 (Not Found)} if the conversationParticipantDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the conversationParticipantDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{participantId}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ConversationParticipantDTO> partialUpdateConversationParticipant(
        @PathVariable(value = "participantId", required = false) final String participantId,
        @NotNull @RequestBody ConversationParticipantDTO conversationParticipantDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ConversationParticipant partially : {}, {}", participantId, conversationParticipantDTO);
        if (conversationParticipantDTO.getParticipantId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(participantId, conversationParticipantDTO.getParticipantId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!conversationParticipantRepository.existsById(participantId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ConversationParticipantDTO> result = conversationParticipantService.partialUpdate(conversationParticipantDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, conversationParticipantDTO.getParticipantId())
        );
    }

    /**
     * {@code GET  /conversation-participants} : get all the conversationParticipants.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of conversationParticipants in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ConversationParticipantDTO>> getAllConversationParticipants(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of ConversationParticipants");
        Page<ConversationParticipantDTO> page = conversationParticipantService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /conversation-participants/:id} : get the "id" conversationParticipant.
     *
     * @param id the id of the conversationParticipantDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the conversationParticipantDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConversationParticipantDTO> getConversationParticipant(@PathVariable("id") String id) {
        LOG.debug("REST request to get ConversationParticipant : {}", id);
        Optional<ConversationParticipantDTO> conversationParticipantDTO = conversationParticipantService.findOne(id);
        return ResponseUtil.wrapOrNotFound(conversationParticipantDTO);
    }

    /**
     * {@code DELETE  /conversation-participants/:id} : delete the "id" conversationParticipant.
     *
     * @param id the id of the conversationParticipantDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversationParticipant(@PathVariable("id") String id) {
        LOG.debug("REST request to delete ConversationParticipant : {}", id);
        conversationParticipantService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
