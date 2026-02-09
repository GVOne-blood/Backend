package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.MessageAttachmentRepository;
import com.theblood.springfood.chat.service.MessageAttachmentService;
import com.theblood.springfood.chat.service.dto.MessageAttachmentDTO;
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
 * REST controller for managing {@link com.theblood.springfood.chat.domain.MessageAttachment}.
 */
@RestController
@RequestMapping("/api/message-attachments")
public class MessageAttachmentResource {

    private static final Logger LOG = LoggerFactory.getLogger(MessageAttachmentResource.class);

    private static final String ENTITY_NAME = "chatMessageAttachment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MessageAttachmentService messageAttachmentService;

    private final MessageAttachmentRepository messageAttachmentRepository;

    public MessageAttachmentResource(
        MessageAttachmentService messageAttachmentService,
        MessageAttachmentRepository messageAttachmentRepository
    ) {
        this.messageAttachmentService = messageAttachmentService;
        this.messageAttachmentRepository = messageAttachmentRepository;
    }

    /**
     * {@code POST  /message-attachments} : Create a new messageAttachment.
     *
     * @param messageAttachmentDTO the messageAttachmentDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new messageAttachmentDTO, or with status {@code 400 (Bad Request)} if the messageAttachment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MessageAttachmentDTO> createMessageAttachment(@Valid @RequestBody MessageAttachmentDTO messageAttachmentDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save MessageAttachment : {}", messageAttachmentDTO);
        if (messageAttachmentRepository.existsById(messageAttachmentDTO.getAttachmentId())) {
            throw new BadRequestAlertException("messageAttachment already exists", ENTITY_NAME, "idexists");
        }
        messageAttachmentDTO = messageAttachmentService.save(messageAttachmentDTO);
        return ResponseEntity.created(new URI("/api/message-attachments/" + messageAttachmentDTO.getAttachmentId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, messageAttachmentDTO.getAttachmentId()))
            .body(messageAttachmentDTO);
    }

    /**
     * {@code PUT  /message-attachments/:attachmentId} : Updates an existing messageAttachment.
     *
     * @param attachmentId the id of the messageAttachmentDTO to save.
     * @param messageAttachmentDTO the messageAttachmentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageAttachmentDTO,
     * or with status {@code 400 (Bad Request)} if the messageAttachmentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the messageAttachmentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{attachmentId}")
    public ResponseEntity<MessageAttachmentDTO> updateMessageAttachment(
        @PathVariable(value = "attachmentId", required = false) final String attachmentId,
        @Valid @RequestBody MessageAttachmentDTO messageAttachmentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MessageAttachment : {}, {}", attachmentId, messageAttachmentDTO);
        if (messageAttachmentDTO.getAttachmentId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(attachmentId, messageAttachmentDTO.getAttachmentId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageAttachmentRepository.existsById(attachmentId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        messageAttachmentDTO = messageAttachmentService.update(messageAttachmentDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, messageAttachmentDTO.getAttachmentId()))
            .body(messageAttachmentDTO);
    }

    /**
     * {@code PATCH  /message-attachments/:attachmentId} : Partial updates given fields of an existing messageAttachment, field will ignore if it is null
     *
     * @param attachmentId the id of the messageAttachmentDTO to save.
     * @param messageAttachmentDTO the messageAttachmentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageAttachmentDTO,
     * or with status {@code 400 (Bad Request)} if the messageAttachmentDTO is not valid,
     * or with status {@code 404 (Not Found)} if the messageAttachmentDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the messageAttachmentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{attachmentId}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MessageAttachmentDTO> partialUpdateMessageAttachment(
        @PathVariable(value = "attachmentId", required = false) final String attachmentId,
        @NotNull @RequestBody MessageAttachmentDTO messageAttachmentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MessageAttachment partially : {}, {}", attachmentId, messageAttachmentDTO);
        if (messageAttachmentDTO.getAttachmentId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(attachmentId, messageAttachmentDTO.getAttachmentId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageAttachmentRepository.existsById(attachmentId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MessageAttachmentDTO> result = messageAttachmentService.partialUpdate(messageAttachmentDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, messageAttachmentDTO.getAttachmentId())
        );
    }

    /**
     * {@code GET  /message-attachments} : get all the messageAttachments.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of messageAttachments in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MessageAttachmentDTO>> getAllMessageAttachments(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of MessageAttachments");
        Page<MessageAttachmentDTO> page = messageAttachmentService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /message-attachments/:id} : get the "id" messageAttachment.
     *
     * @param id the id of the messageAttachmentDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the messageAttachmentDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageAttachmentDTO> getMessageAttachment(@PathVariable("id") String id) {
        LOG.debug("REST request to get MessageAttachment : {}", id);
        Optional<MessageAttachmentDTO> messageAttachmentDTO = messageAttachmentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(messageAttachmentDTO);
    }

    /**
     * {@code DELETE  /message-attachments/:id} : delete the "id" messageAttachment.
     *
     * @param id the id of the messageAttachmentDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessageAttachment(@PathVariable("id") String id) {
        LOG.debug("REST request to delete MessageAttachment : {}", id);
        messageAttachmentService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
