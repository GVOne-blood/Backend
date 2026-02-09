package com.theblood.springfood.actionlog.web.rest;

import com.theblood.springfood.actionlog.domain.LogActionAnnualUpdate;
import com.theblood.springfood.actionlog.repository.LogActionAnnualUpdateRepository;
import com.theblood.springfood.actionlog.service.CarboneService;
import com.theblood.springfood.actionlog.service.LogActionAnnualUpdateQueryService;
import com.theblood.springfood.actionlog.service.LogActionAnnualUpdateService;
import com.theblood.springfood.actionlog.service.criteria.LogActionAnnualUpdateCriteria;
import com.theblood.springfood.actionlog.service.dto.LogActionAnnualUpdateDTO;
import com.theblood.springfood.actionlog.service.dto.LogActionRequest;
import com.theblood.springfood.actionlog.service.dto.carbone.CarboneResponseData;
import com.theblood.springfood.actionlog.web.rest.errors.BadRequestAlertException;
import com.theblood.springfood.client.api.LogActionAnnualUpdateClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST resources for managing {@link LogActionAnnualUpdate}.
 */
@RestController
@RequestMapping("/api/log-action-annual-updates")
@Slf4j
@RequiredArgsConstructor
public class LogActionAnnualUpdateResource {

    private static final String ENTITY_NAME = "customerLogActionAnnualUpdate";
    private final CarboneService carboneService;
    private final LogActionAnnualUpdateService logActionAnnualUpdateService;
    private final LogActionAnnualUpdateRepository logActionAnnualUpdateRepository;
    private final LogActionAnnualUpdateQueryService logActionAnnualUpdateQueryService;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    /**
     * {@code POST  /log-action-annual-updates} : Create a new logActionAnnualUpdate.
     *
     * @param logActionAnnualUpdateDTO the logActionAnnualUpdateDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new logActionAnnualUpdateDTO, or with status {@code 400 (Bad Request)} if the logActionAnnualUpdate has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Boolean> createLogActionAnnualUpdate(@RequestBody LogActionAnnualUpdateClient.LogActionAnnualUpdateDto logActionAnnualUpdateDTO) throws URISyntaxException {
        LOG.debug("REST request to save LogAction : {}", logActionAnnualUpdateDTO);
        if (logActionAnnualUpdateDTO.getId() != null) {
            throw new BadRequestAlertException("A new logAction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        try {
            logActionAnnualUpdateService.save(logActionAnnualUpdateDTO);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            LOG.error("Failed to save LogAction", e);
            return ResponseEntity.ok(false);
        }
    }

    /**
     * {@code PUT  /log-action-annual-updates/:id} : Updates an existing logActionAnnualUpdate.
     *
     * @param id                       the id of the logActionAnnualUpdateDTO to save.
     * @param logActionAnnualUpdateDTO the logActionAnnualUpdateDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated logActionAnnualUpdateDTO,
     * or with status {@code 400 (Bad Request)} if the logActionAnnualUpdateDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the logActionAnnualUpdateDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LogActionAnnualUpdateDTO> updateLogActionAnnualUpdate(
            @PathVariable(value = "id", required = false) final String id,
            @Valid @RequestBody LogActionAnnualUpdateDTO logActionAnnualUpdateDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update LogActionAnnualUpdate : {}, {}", id, logActionAnnualUpdateDTO);
        if (logActionAnnualUpdateDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, logActionAnnualUpdateDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!logActionAnnualUpdateRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        logActionAnnualUpdateDTO = logActionAnnualUpdateService.update(logActionAnnualUpdateDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, logActionAnnualUpdateDTO.getId()))
                .body(logActionAnnualUpdateDTO);
    }

    /**
     * {@code PATCH  /log-action-annual-updates/:id} : Partial updates given fields of an existing logActionAnnualUpdate, field will ignore if it is null
     *
     * @param id                       the id of the logActionAnnualUpdateDTO to save.
     * @param logActionAnnualUpdateDTO the logActionAnnualUpdateDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated logActionAnnualUpdateDTO,
     * or with status {@code 400 (Bad Request)} if the logActionAnnualUpdateDTO is not valid,
     * or with status {@code 404 (Not Found)} if the logActionAnnualUpdateDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the logActionAnnualUpdateDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<LogActionAnnualUpdateDTO> partialUpdateLogActionAnnualUpdate(
            @PathVariable(value = "id", required = false) final String id,
            @NotNull @RequestBody LogActionAnnualUpdateDTO logActionAnnualUpdateDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update LogActionAnnualUpdate partially : {}, {}", id, logActionAnnualUpdateDTO);
        if (logActionAnnualUpdateDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, logActionAnnualUpdateDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!logActionAnnualUpdateRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<LogActionAnnualUpdateDTO> result = logActionAnnualUpdateService.partialUpdate(logActionAnnualUpdateDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, logActionAnnualUpdateDTO.getId())
        );
    }

    /**
     * {@code GET  /log-action-annual-updates} : get all the logActionAnnualUpdates.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of logActionAnnualUpdates in body.
     */
    @GetMapping("")
    public ResponseEntity<List<LogActionAnnualUpdateDTO>> getAllLogActionAnnualUpdates(
            LogActionAnnualUpdateCriteria criteria,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get LogActionAnnualUpdates by criteria: {}", criteria);

        Page<LogActionAnnualUpdateDTO> page = logActionAnnualUpdateQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /log-action-annual-updates/count} : count all the logActionAnnualUpdates.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countLogActionAnnualUpdates(LogActionAnnualUpdateCriteria criteria) {
        LOG.debug("REST request to count LogActionAnnualUpdates by criteria: {}", criteria);
        return ResponseEntity.ok().body(logActionAnnualUpdateQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /log-action-annual-updates/:id} : get the "id" logActionAnnualUpdate.
     *
     * @param id the id of the logActionAnnualUpdateDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the logActionAnnualUpdateDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LogActionAnnualUpdateDTO> getLogActionAnnualUpdate(@PathVariable("id") String id) {
        LOG.debug("REST request to get LogActionAnnualUpdate : {}", id);
        Optional<LogActionAnnualUpdateDTO> logActionAnnualUpdateDTO = logActionAnnualUpdateService.findOne(id);
        return ResponseUtil.wrapOrNotFound(logActionAnnualUpdateDTO);
    }

    /**
     * {@code DELETE  /log-action-annual-updates/:id} : delete the "id" logActionAnnualUpdate.
     *
     * @param id the id of the logActionAnnualUpdateDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLogActionAnnualUpdate(@PathVariable("id") String id) {
        LOG.debug("REST request to delete LogActionAnnualUpdate : {}", id);
        logActionAnnualUpdateService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importLogActionAnnualUpdates(@RequestParam String fileUrl) {
        LOG.debug("REST request to import LogActionAnnualUpdates");
        logActionAnnualUpdateService.importData(fileUrl);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/export")
    public ResponseEntity<CarboneResponseData> exportLogActionAnnualUpdates() {
        LOG.debug("REST request to export LogActionAnnualUpdates");
        var data = logActionAnnualUpdateService.exportData();
        return ResponseEntity.ok().body(data);
    }

    /**
     * {@code POST  /log-actions/search} : Search all log actions by table name and object ID.
     *
     * @param logActionRequest the request containing tableName and objectId with optional sorting info
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the full list of log actions in body.
     */
    @PostMapping("/search")
    public ResponseEntity<List<LogActionAnnualUpdateDTO>> findByTableNameAndObjectId(
            @Valid @RequestBody LogActionRequest logActionRequest
    ) {
        LOG.debug("REST request to find all LogActionAnnualUpdates by tableName: {} and objectId: {}",
                logActionRequest.getTableName(),
                logActionRequest.getObjectId());

        List<LogActionAnnualUpdateDTO> result = logActionAnnualUpdateQueryService.findByTableNameAndObjectId(logActionRequest);

        return ResponseEntity.ok().body(result);
    }
}
