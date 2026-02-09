package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.MessageReadReceiptRepository;
import com.theblood.springfood.chat.service.MessageReadReceiptService;
import com.theblood.springfood.chat.service.dto.MessageReadReceiptDTO;
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
 * REST controller for managing {@link com.theblood.springfood.chat.domain.MessageReadReceipt}.
 */
@RestController
@RequestMapping("/api/message-read-receipts")
public class MessageReadReceiptResource {

    private static final Logger LOG = LoggerFactory.getLogger(MessageReadReceiptResource.class);

    private static final String ENTITY_NAME = "chatMessageReadReceipt";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MessageReadReceiptService messageReadReceiptService;

    private final MessageReadReceiptRepository messageReadReceiptRepository;

    public MessageReadReceiptResource(
        MessageReadReceiptService messageReadReceiptService,
        MessageReadReceiptRepository messageReadReceiptRepository
    ) {
        this.messageReadReceiptService = messageReadReceiptService;
        this.messageReadReceiptRepository = messageReadReceiptRepository;
    }

    /**
     * {@code POST  /message-read-receipts} : Create a new messageReadReceipt.
     *
     * @param messageReadReceiptDTO the messageReadReceiptDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new messageReadReceiptDTO, or with status {@code 400 (Bad Request)} if the messageReadReceipt has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MessageReadReceiptDTO> createMessageReadReceipt(@Valid @RequestBody MessageReadReceiptDTO messageReadReceiptDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save MessageReadReceipt : {}", messageReadReceiptDTO);
        if (messageReadReceiptRepository.existsById(messageReadReceiptDTO.getReceiptId())) {
            throw new BadRequestAlertException("messageReadReceipt already exists", ENTITY_NAME, "idexists");
        }
        messageReadReceiptDTO = messageReadReceiptService.save(messageReadReceiptDTO);
        return ResponseEntity.created(new URI("/api/message-read-receipts/" + messageReadReceiptDTO.getReceiptId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, messageReadReceiptDTO.getReceiptId()))
            .body(messageReadReceiptDTO);
    }

    /**
     * {@code PUT  /message-read-receipts/:receiptId} : Updates an existing messageReadReceipt.
     *
     * @param receiptId the id of the messageReadReceiptDTO to save.
     * @param messageReadReceiptDTO the messageReadReceiptDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageReadReceiptDTO,
     * or with status {@code 400 (Bad Request)} if the messageReadReceiptDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the messageReadReceiptDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{receiptId}")
    public ResponseEntity<MessageReadReceiptDTO> updateMessageReadReceipt(
        @PathVariable(value = "receiptId", required = false) final String receiptId,
        @Valid @RequestBody MessageReadReceiptDTO messageReadReceiptDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MessageReadReceipt : {}, {}", receiptId, messageReadReceiptDTO);
        if (messageReadReceiptDTO.getReceiptId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(receiptId, messageReadReceiptDTO.getReceiptId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageReadReceiptRepository.existsById(receiptId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        messageReadReceiptDTO = messageReadReceiptService.update(messageReadReceiptDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, messageReadReceiptDTO.getReceiptId()))
            .body(messageReadReceiptDTO);
    }

    /**
     * {@code PATCH  /message-read-receipts/:receiptId} : Partial updates given fields of an existing messageReadReceipt, field will ignore if it is null
     *
     * @param receiptId the id of the messageReadReceiptDTO to save.
     * @param messageReadReceiptDTO the messageReadReceiptDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageReadReceiptDTO,
     * or with status {@code 400 (Bad Request)} if the messageReadReceiptDTO is not valid,
     * or with status {@code 404 (Not Found)} if the messageReadReceiptDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the messageReadReceiptDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{receiptId}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MessageReadReceiptDTO> partialUpdateMessageReadReceipt(
        @PathVariable(value = "receiptId", required = false) final String receiptId,
        @NotNull @RequestBody MessageReadReceiptDTO messageReadReceiptDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MessageReadReceipt partially : {}, {}", receiptId, messageReadReceiptDTO);
        if (messageReadReceiptDTO.getReceiptId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(receiptId, messageReadReceiptDTO.getReceiptId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageReadReceiptRepository.existsById(receiptId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MessageReadReceiptDTO> result = messageReadReceiptService.partialUpdate(messageReadReceiptDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, messageReadReceiptDTO.getReceiptId())
        );
    }

    /**
     * {@code GET  /message-read-receipts} : get all the messageReadReceipts.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of messageReadReceipts in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MessageReadReceiptDTO>> getAllMessageReadReceipts(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of MessageReadReceipts");
        Page<MessageReadReceiptDTO> page = messageReadReceiptService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /message-read-receipts/:id} : get the "id" messageReadReceipt.
     *
     * @param id the id of the messageReadReceiptDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the messageReadReceiptDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageReadReceiptDTO> getMessageReadReceipt(@PathVariable("id") String id) {
        LOG.debug("REST request to get MessageReadReceipt : {}", id);
        Optional<MessageReadReceiptDTO> messageReadReceiptDTO = messageReadReceiptService.findOne(id);
        return ResponseUtil.wrapOrNotFound(messageReadReceiptDTO);
    }

    /**
     * {@code DELETE  /message-read-receipts/:id} : delete the "id" messageReadReceipt.
     *
     * @param id the id of the messageReadReceiptDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessageReadReceipt(@PathVariable("id") String id) {
        LOG.debug("REST request to delete MessageReadReceipt : {}", id);
        messageReadReceiptService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
