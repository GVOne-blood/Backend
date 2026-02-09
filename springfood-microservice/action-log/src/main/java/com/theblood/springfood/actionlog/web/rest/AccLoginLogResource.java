package com.theblood.springfood.actionlog.web.rest;

import com.theblood.springfood.actionlog.domain.AccLoginLog;
import com.theblood.springfood.actionlog.repository.AccLoginLogRepository;
import com.theblood.springfood.actionlog.service.AccLoginLogService;
import com.theblood.springfood.actionlog.service.dto.AccLoginLogDTO;
import com.theblood.springfood.actionlog.web.rest.errors.BadRequestAlertException;
import com.theblood.springfood.client.api.AccLoginLogClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * REST resources for managing {@link AccLoginLog}.
 */
@RestController
@RequestMapping("/api/acc-login-logs")
public class AccLoginLogResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccLoginLogResource.class);

    private static final String ENTITY_NAME = "authenticationAccLoginLog";
    private final AccLoginLogService accLoginLogService;
    private final AccLoginLogRepository accLoginLogRepository;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public AccLoginLogResource(
            AccLoginLogService accLoginLogService,
            AccLoginLogRepository accLoginLogRepository
    ) {
        this.accLoginLogService = accLoginLogService;
        this.accLoginLogRepository = accLoginLogRepository;
    }

    /**
     * {@code POST  /acc-login-logs} : Create a new accLoginLog.
     *
     * @param accLoginLogClientDto the accLoginLogDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new accLoginLogDTO, or with status {@code 400 (Bad Request)} if the accLoginLog has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/create-log")
    public ResponseEntity<AccLoginLogDTO> createAccLoginLog(@Valid @RequestBody AccLoginLogClient.AccLoginLogDto accLoginLogClientDto) throws URISyntaxException {
        LOG.debug("REST request to save AccLoginLog : {}", accLoginLogClientDto);
        AccLoginLogDTO accLoginLogDTO = new AccLoginLogDTO();
        BeanUtils.copyProperties(accLoginLogClientDto, accLoginLogDTO);
        if (accLoginLogDTO.getId() != null) {
            throw new BadRequestAlertException("accLoginLog already exists", ENTITY_NAME, "idexists");
        }
        accLoginLogDTO = accLoginLogService.save(accLoginLogDTO);
        return ResponseEntity.created(new URI("/api/acc-login-logs/" + accLoginLogDTO.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, accLoginLogDTO.getId()))
                .body(accLoginLogDTO);
    }

    /**
     * {@code PUT  /acc-login-logs/:accAuditLogId} : Updates an existing accLoginLog.
     *
     * @param accAuditLogId  the id of the accLoginLogDTO to save.
     * @param accLoginLogDTO the accLoginLogDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated accLoginLogDTO,
     * or with status {@code 400 (Bad Request)} if the accLoginLogDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the accLoginLogDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{accAuditLogId}")
    public ResponseEntity<AccLoginLogDTO> updateAccLoginLog(
            @PathVariable(value = "accAuditLogId", required = false) final String accAuditLogId,
            @Valid @RequestBody AccLoginLogDTO accLoginLogDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AccLoginLog : {}, {}", accAuditLogId, accLoginLogDTO);
        if (accLoginLogDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(accAuditLogId, accLoginLogDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!accLoginLogRepository.existsById(accAuditLogId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        accLoginLogDTO = accLoginLogService.update(accLoginLogDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, accLoginLogDTO.getId()))
                .body(accLoginLogDTO);
    }

    /**
     * {@code PATCH  /acc-login-logs/:accAuditLogId} : Partial updates given fields of an existing accLoginLog, field will ignore if it is null
     *
     * @param accAuditLogId  the id of the accLoginLogDTO to save.
     * @param accLoginLogDTO the accLoginLogDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated accLoginLogDTO,
     * or with status {@code 400 (Bad Request)} if the accLoginLogDTO is not valid,
     * or with status {@code 404 (Not Found)} if the accLoginLogDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the accLoginLogDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{accAuditLogId}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<AccLoginLogDTO> partialUpdateAccLoginLog(
            @PathVariable(value = "accAuditLogId", required = false) final String accAuditLogId,
            @NotNull @RequestBody AccLoginLogDTO accLoginLogDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AccLoginLog partially : {}, {}", accAuditLogId, accLoginLogDTO);
        if (accLoginLogDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(accAuditLogId, accLoginLogDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!accLoginLogRepository.existsById(accAuditLogId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AccLoginLogDTO> result = accLoginLogService.partialUpdate(accLoginLogDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, accLoginLogDTO.getId())
        );
    }

    /**
     * {@code GET  /acc-login-logs/:id} : get the "id" accLoginLog.
     *
     * @param id the id of the accLoginLogDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the accLoginLogDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccLoginLogDTO> getAccLoginLog(@PathVariable("id") String id) {
        LOG.debug("REST request to get AccLoginLog : {}", id);
        Optional<AccLoginLogDTO> accLoginLogDTO = accLoginLogService.findOne(id);
        return ResponseUtil.wrapOrNotFound(accLoginLogDTO);
    }

    /**
     * {@code DELETE  /acc-login-logs/:id} : delete the "id" accLoginLog.
     *
     * @param id the id of the accLoginLogDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccLoginLog(@PathVariable("id") String id) {
        LOG.debug("REST request to delete AccLoginLog : {}", id);
        accLoginLogService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
