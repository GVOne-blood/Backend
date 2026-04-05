package com.theblood.springfood.actionlog.web.rest;

import com.theblood.springfood.actionlog.domain.LogAction;
import com.theblood.springfood.actionlog.repository.LogActionRepository;
import com.theblood.springfood.actionlog.carbone.CarboneService;
import com.theblood.springfood.actionlog.service.LogActionService;
import com.theblood.springfood.actionlog.service.dto.LogActionDTO;
import com.theblood.springfood.actionlog.carbone.dto.CarboneResponseData;
import com.theblood.springfood.actionlog.web.rest.errors.BadRequestAlertException;
import com.theblood.springfood.client.api.LogActionsClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * REST resources for managing {@link LogAction}.
 */
@RestController
@RequestMapping("/api/log-actions")
@Slf4j
public class LogActionResource {

    private static final String ENTITY_NAME = "customerLogAction";
    private final CarboneService carboneService;
    private final LogActionService logActionService;
    private final LogActionRepository logActionRepository;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public LogActionResource(
            CarboneService carboneService,
            LogActionService logActionService,
            LogActionRepository logActionRepository
    ) {
        this.carboneService = carboneService;
        this.logActionService = logActionService;
        this.logActionRepository = logActionRepository;
    }

    /**
     * {@code POST  /log-actions} : Create a new logAction.
     *
     * @param logActionDTO the logActionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new logActionDTO, or with status {@code 400 (Bad Request)} if the logAction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Boolean> createLogAction(@RequestBody LogActionsClient.LogActionsDto logActionDTO) throws URISyntaxException {
        LOG.debug("REST request to save LogAction : {}", logActionDTO);
        if (logActionDTO.getId() != null) {
            throw new BadRequestAlertException("A new logAction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        try {
            logActionService.save(logActionDTO);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            LOG.error("Failed to save LogAction", e);
            return ResponseEntity.ok(false);
        }
    }

    /**
     * {@code PUT  /log-actions/:id} : Updates an existing logAction.
     *
     * @param id           the id of the logActionDTO to save.
     * @param logActionDTO the logActionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated logActionDTO,
     * or with status {@code 400 (Bad Request)} if the logActionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the logActionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LogActionDTO> updateLogAction(
            @PathVariable(value = "id", required = false) final String id,
            @Valid @RequestBody LogActionDTO logActionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update LogAction : {}, {}", id, logActionDTO);
        if (logActionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, logActionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!logActionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        logActionDTO = logActionService.update(logActionDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, logActionDTO.getId()))
                .body(logActionDTO);
    }

    /**
     * {@code PATCH  /log-actions/:id} : Partial updates given fields of an existing logAction, field will ignore if it is null
     *
     * @param id           the id of the logActionDTO to save.
     * @param logActionDTO the logActionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated logActionDTO,
     * or with status {@code 400 (Bad Request)} if the logActionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the logActionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the logActionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<LogActionDTO> partialUpdateLogAction(
            @PathVariable(value = "id", required = false) final String id,
            @NotNull @RequestBody LogActionDTO logActionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update LogAction partially : {}, {}", id, logActionDTO);
        if (logActionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, logActionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!logActionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<LogActionDTO> result = logActionService.partialUpdate(logActionDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, logActionDTO.getId())
        );
    }

    /**
     * {@code GET  /log-actions/:id} : get the "id" logAction.
     *
     * @param id the id of the logActionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the logActionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LogActionDTO> getLogAction(@PathVariable("id") String id) {
        LOG.debug("REST request to get LogAction : {}", id);
        Optional<LogActionDTO> logActionDTO = logActionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(logActionDTO);
    }

    /**
     * {@code DELETE  /log-actions/:id} : delete the "id" logAction.
     *
     * @param id the id of the logActionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLogAction(@PathVariable("id") String id) {
        LOG.debug("REST request to delete LogAction : {}", id);
        logActionService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importLogActions(@RequestParam String fileUrl) {
        LOG.debug("REST request to import LogActions");
        logActionService.importData(fileUrl);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/export")
    public ResponseEntity<CarboneResponseData> exportLogActions() {
        LOG.debug("REST request to export LogActions");
        var data = logActionService.exportData();
        return ResponseEntity.ok().body(data);
    }
}
