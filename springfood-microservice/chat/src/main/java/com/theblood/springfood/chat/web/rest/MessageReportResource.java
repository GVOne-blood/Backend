package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.MessageReportRepository;
import com.theblood.springfood.chat.service.MessageReportService;
import com.theblood.springfood.chat.service.dto.MessageReportDTO;
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
 * REST resources for managing {@link com.theblood.springfood.chat.domain.MessageReport}.
 */
@RestController
@RequestMapping("/api/message-reports")
public class MessageReportResource {

    private static final Logger LOG = LoggerFactory.getLogger(MessageReportResource.class);

    private static final String ENTITY_NAME = "chatMessageReport";
    private final MessageReportService messageReportService;
    private final MessageReportRepository messageReportRepository;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public MessageReportResource(MessageReportService messageReportService, MessageReportRepository messageReportRepository) {
        this.messageReportService = messageReportService;
        this.messageReportRepository = messageReportRepository;
    }

    /**
     * {@code POST  /message-reports} : Create a new messageReport.
     *
     * @param messageReportDTO the messageReportDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new messageReportDTO, or with status {@code 400 (Bad Request)} if the messageReport has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MessageReportDTO> createMessageReport(@Valid @RequestBody MessageReportDTO messageReportDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save MessageReport : {}", messageReportDTO);
        if (messageReportRepository.existsById(messageReportDTO.getReportId())) {
            throw new BadRequestAlertException("messageReport already exists", ENTITY_NAME, "idexists");
        }
        messageReportDTO = messageReportService.save(messageReportDTO);
        return ResponseEntity.created(new URI("/api/message-reports/" + messageReportDTO.getReportId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, messageReportDTO.getReportId()))
            .body(messageReportDTO);
    }

    /**
     * {@code PUT  /message-reports/:reportId} : Updates an existing messageReport.
     *
     * @param reportId         the id of the messageReportDTO to save.
     * @param messageReportDTO the messageReportDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageReportDTO,
     * or with status {@code 400 (Bad Request)} if the messageReportDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the messageReportDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{reportId}")
    public ResponseEntity<MessageReportDTO> updateMessageReport(
        @PathVariable(value = "reportId", required = false) final String reportId,
        @Valid @RequestBody MessageReportDTO messageReportDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MessageReport : {}, {}", reportId, messageReportDTO);
        if (messageReportDTO.getReportId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(reportId, messageReportDTO.getReportId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageReportRepository.existsById(reportId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        messageReportDTO = messageReportService.update(messageReportDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, messageReportDTO.getReportId()))
            .body(messageReportDTO);
    }

    /**
     * {@code PATCH  /message-reports/:reportId} : Partial updates given fields of an existing messageReport, field will ignore if it is null
     *
     * @param reportId         the id of the messageReportDTO to save.
     * @param messageReportDTO the messageReportDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageReportDTO,
     * or with status {@code 400 (Bad Request)} if the messageReportDTO is not valid,
     * or with status {@code 404 (Not Found)} if the messageReportDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the messageReportDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{reportId}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<MessageReportDTO> partialUpdateMessageReport(
        @PathVariable(value = "reportId", required = false) final String reportId,
        @NotNull @RequestBody MessageReportDTO messageReportDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MessageReport partially : {}, {}", reportId, messageReportDTO);
        if (messageReportDTO.getReportId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(reportId, messageReportDTO.getReportId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageReportRepository.existsById(reportId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MessageReportDTO> result = messageReportService.partialUpdate(messageReportDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, messageReportDTO.getReportId())
        );
    }

    /**
     * {@code GET  /message-reports} : get all the messageReports.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of messageReports in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MessageReportDTO>> getAllMessageReports(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of MessageReports");
        Page<MessageReportDTO> page = messageReportService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /message-reports/:id} : get the "id" messageReport.
     *
     * @param id the id of the messageReportDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the messageReportDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageReportDTO> getMessageReport(@PathVariable("id") String id) {
        LOG.debug("REST request to get MessageReport : {}", id);
        Optional<MessageReportDTO> messageReportDTO = messageReportService.findOne(id);
        return ResponseUtil.wrapOrNotFound(messageReportDTO);
    }

    /**
     * {@code DELETE  /message-reports/:id} : delete the "id" messageReport.
     *
     * @param id the id of the messageReportDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessageReport(@PathVariable("id") String id) {
        LOG.debug("REST request to delete MessageReport : {}", id);
        messageReportService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
