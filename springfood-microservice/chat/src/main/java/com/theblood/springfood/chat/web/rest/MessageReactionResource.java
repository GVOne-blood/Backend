package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.MessageReactionRepository;
import com.theblood.springfood.chat.service.MessageReactionService;
import com.theblood.springfood.chat.service.dto.MessageReactionDTO;
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
 * REST controller for managing {@link com.theblood.springfood.chat.domain.MessageReaction}.
 */
@RestController
@RequestMapping("/api/message-reactions")
public class MessageReactionResource {

    private static final Logger LOG = LoggerFactory.getLogger(MessageReactionResource.class);

    private static final String ENTITY_NAME = "chatMessageReaction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MessageReactionService messageReactionService;

    private final MessageReactionRepository messageReactionRepository;

    public MessageReactionResource(MessageReactionService messageReactionService, MessageReactionRepository messageReactionRepository) {
        this.messageReactionService = messageReactionService;
        this.messageReactionRepository = messageReactionRepository;
    }

    /**
     * {@code POST  /message-reactions} : Create a new messageReaction.
     *
     * @param messageReactionDTO the messageReactionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new messageReactionDTO, or with status {@code 400 (Bad Request)} if the messageReaction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MessageReactionDTO> createMessageReaction(@Valid @RequestBody MessageReactionDTO messageReactionDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save MessageReaction : {}", messageReactionDTO);
        if (messageReactionRepository.existsById(messageReactionDTO.getReactionId())) {
            throw new BadRequestAlertException("messageReaction already exists", ENTITY_NAME, "idexists");
        }
        messageReactionDTO = messageReactionService.save(messageReactionDTO);
        return ResponseEntity.created(new URI("/api/message-reactions/" + messageReactionDTO.getReactionId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, messageReactionDTO.getReactionId()))
            .body(messageReactionDTO);
    }

    /**
     * {@code PUT  /message-reactions/:reactionId} : Updates an existing messageReaction.
     *
     * @param reactionId the id of the messageReactionDTO to save.
     * @param messageReactionDTO the messageReactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageReactionDTO,
     * or with status {@code 400 (Bad Request)} if the messageReactionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the messageReactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{reactionId}")
    public ResponseEntity<MessageReactionDTO> updateMessageReaction(
        @PathVariable(value = "reactionId", required = false) final String reactionId,
        @Valid @RequestBody MessageReactionDTO messageReactionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MessageReaction : {}, {}", reactionId, messageReactionDTO);
        if (messageReactionDTO.getReactionId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(reactionId, messageReactionDTO.getReactionId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageReactionRepository.existsById(reactionId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        messageReactionDTO = messageReactionService.update(messageReactionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, messageReactionDTO.getReactionId()))
            .body(messageReactionDTO);
    }

    /**
     * {@code PATCH  /message-reactions/:reactionId} : Partial updates given fields of an existing messageReaction, field will ignore if it is null
     *
     * @param reactionId the id of the messageReactionDTO to save.
     * @param messageReactionDTO the messageReactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageReactionDTO,
     * or with status {@code 400 (Bad Request)} if the messageReactionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the messageReactionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the messageReactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{reactionId}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MessageReactionDTO> partialUpdateMessageReaction(
        @PathVariable(value = "reactionId", required = false) final String reactionId,
        @NotNull @RequestBody MessageReactionDTO messageReactionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MessageReaction partially : {}, {}", reactionId, messageReactionDTO);
        if (messageReactionDTO.getReactionId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(reactionId, messageReactionDTO.getReactionId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageReactionRepository.existsById(reactionId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MessageReactionDTO> result = messageReactionService.partialUpdate(messageReactionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, messageReactionDTO.getReactionId())
        );
    }

    /**
     * {@code GET  /message-reactions} : get all the messageReactions.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of messageReactions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MessageReactionDTO>> getAllMessageReactions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of MessageReactions");
        Page<MessageReactionDTO> page = messageReactionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /message-reactions/:id} : get the "id" messageReaction.
     *
     * @param id the id of the messageReactionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the messageReactionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageReactionDTO> getMessageReaction(@PathVariable("id") String id) {
        LOG.debug("REST request to get MessageReaction : {}", id);
        Optional<MessageReactionDTO> messageReactionDTO = messageReactionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(messageReactionDTO);
    }

    /**
     * {@code DELETE  /message-reactions/:id} : delete the "id" messageReaction.
     *
     * @param id the id of the messageReactionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessageReaction(@PathVariable("id") String id) {
        LOG.debug("REST request to delete MessageReaction : {}", id);
        messageReactionService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
