package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.service.ConversationService;
import com.theblood.springfood.chat.service.dto.ConversationDTO;
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
 * REST controller for managing {@link com.theblood.springfood.chat.domain.Conversation}.
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationResource.class);

    private static final String ENTITY_NAME = "chatConversation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ConversationService conversationService;

    private final ConversationRepository conversationRepository;

    public ConversationResource(ConversationService conversationService, ConversationRepository conversationRepository) {
        this.conversationService = conversationService;
        this.conversationRepository = conversationRepository;
    }

    /**
     * {@code POST  /conversations} : Create a new conversation.
     *
     * @param conversationDTO the conversationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new conversationDTO, or with status {@code 400 (Bad Request)} if the conversation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ConversationDTO> createConversation(@Valid @RequestBody ConversationDTO conversationDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save Conversation : {}", conversationDTO);
        if (conversationRepository.existsById(conversationDTO.getConversationId())) {
            throw new BadRequestAlertException("conversation already exists", ENTITY_NAME, "idexists");
        }
        conversationDTO = conversationService.save(conversationDTO);
        return ResponseEntity.created(new URI("/api/conversations/" + conversationDTO.getConversationId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, conversationDTO.getConversationId()))
            .body(conversationDTO);
    }

    /**
     * {@code PUT  /conversations/:conversationId} : Updates an existing conversation.
     *
     * @param conversationId the id of the conversationDTO to save.
     * @param conversationDTO the conversationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated conversationDTO,
     * or with status {@code 400 (Bad Request)} if the conversationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the conversationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> updateConversation(
        @PathVariable(value = "conversationId", required = false) final String conversationId,
        @Valid @RequestBody ConversationDTO conversationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Conversation : {}, {}", conversationId, conversationDTO);
        if (conversationDTO.getConversationId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(conversationId, conversationDTO.getConversationId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!conversationRepository.existsById(conversationId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        conversationDTO = conversationService.update(conversationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, conversationDTO.getConversationId()))
            .body(conversationDTO);
    }

    /**
     * {@code PATCH  /conversations/:conversationId} : Partial updates given fields of an existing conversation, field will ignore if it is null
     *
     * @param conversationId the id of the conversationDTO to save.
     * @param conversationDTO the conversationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated conversationDTO,
     * or with status {@code 400 (Bad Request)} if the conversationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the conversationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the conversationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{conversationId}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ConversationDTO> partialUpdateConversation(
        @PathVariable(value = "conversationId", required = false) final String conversationId,
        @NotNull @RequestBody ConversationDTO conversationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Conversation partially : {}, {}", conversationId, conversationDTO);
        if (conversationDTO.getConversationId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(conversationId, conversationDTO.getConversationId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!conversationRepository.existsById(conversationId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ConversationDTO> result = conversationService.partialUpdate(conversationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, conversationDTO.getConversationId())
        );
    }

    /**
     * {@code GET  /conversations} : get all the conversations.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of conversations in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ConversationDTO>> getAllConversations(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Conversations");
        Page<ConversationDTO> page = conversationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /conversations/:id} : get the "id" conversation.
     *
     * @param id the id of the conversationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the conversationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConversationDTO> getConversation(@PathVariable("id") String id) {
        LOG.debug("REST request to get Conversation : {}", id);
        Optional<ConversationDTO> conversationDTO = conversationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(conversationDTO);
    }

    /**
     * {@code DELETE  /conversations/:id} : delete the "id" conversation.
     *
     * @param id the id of the conversationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Conversation : {}", id);
        conversationService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
