package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.repository.TypingIndicatorRepository;
import com.theblood.springfood.chat.service.TypingIndicatorService;
import com.theblood.springfood.chat.service.dto.TypingIndicatorDTO;
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
 * REST controller for managing {@link com.theblood.springfood.chat.domain.TypingIndicator}.
 */
@RestController
@RequestMapping("/api/typing-indicators")
public class TypingIndicatorResource {

    private static final Logger LOG = LoggerFactory.getLogger(TypingIndicatorResource.class);

    private static final String ENTITY_NAME = "chatTypingIndicator";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TypingIndicatorService typingIndicatorService;

    private final TypingIndicatorRepository typingIndicatorRepository;

    public TypingIndicatorResource(TypingIndicatorService typingIndicatorService, TypingIndicatorRepository typingIndicatorRepository) {
        this.typingIndicatorService = typingIndicatorService;
        this.typingIndicatorRepository = typingIndicatorRepository;
    }

    /**
     * {@code POST  /typing-indicators} : Create a new typingIndicator.
     *
     * @param typingIndicatorDTO the typingIndicatorDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new typingIndicatorDTO, or with status {@code 400 (Bad Request)} if the typingIndicator has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TypingIndicatorDTO> createTypingIndicator(@Valid @RequestBody TypingIndicatorDTO typingIndicatorDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TypingIndicator : {}", typingIndicatorDTO);
        if (typingIndicatorRepository.existsById(typingIndicatorDTO.getIndicatorId())) {
            throw new BadRequestAlertException("typingIndicator already exists", ENTITY_NAME, "idexists");
        }
        typingIndicatorDTO = typingIndicatorService.save(typingIndicatorDTO);
        return ResponseEntity.created(new URI("/api/typing-indicators/" + typingIndicatorDTO.getIndicatorId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, typingIndicatorDTO.getIndicatorId()))
            .body(typingIndicatorDTO);
    }

    /**
     * {@code PUT  /typing-indicators/:indicatorId} : Updates an existing typingIndicator.
     *
     * @param indicatorId the id of the typingIndicatorDTO to save.
     * @param typingIndicatorDTO the typingIndicatorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated typingIndicatorDTO,
     * or with status {@code 400 (Bad Request)} if the typingIndicatorDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the typingIndicatorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{indicatorId}")
    public ResponseEntity<TypingIndicatorDTO> updateTypingIndicator(
        @PathVariable(value = "indicatorId", required = false) final String indicatorId,
        @Valid @RequestBody TypingIndicatorDTO typingIndicatorDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TypingIndicator : {}, {}", indicatorId, typingIndicatorDTO);
        if (typingIndicatorDTO.getIndicatorId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(indicatorId, typingIndicatorDTO.getIndicatorId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!typingIndicatorRepository.existsById(indicatorId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        typingIndicatorDTO = typingIndicatorService.update(typingIndicatorDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, typingIndicatorDTO.getIndicatorId()))
            .body(typingIndicatorDTO);
    }

    /**
     * {@code PATCH  /typing-indicators/:indicatorId} : Partial updates given fields of an existing typingIndicator, field will ignore if it is null
     *
     * @param indicatorId the id of the typingIndicatorDTO to save.
     * @param typingIndicatorDTO the typingIndicatorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated typingIndicatorDTO,
     * or with status {@code 400 (Bad Request)} if the typingIndicatorDTO is not valid,
     * or with status {@code 404 (Not Found)} if the typingIndicatorDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the typingIndicatorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{indicatorId}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TypingIndicatorDTO> partialUpdateTypingIndicator(
        @PathVariable(value = "indicatorId", required = false) final String indicatorId,
        @NotNull @RequestBody TypingIndicatorDTO typingIndicatorDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TypingIndicator partially : {}, {}", indicatorId, typingIndicatorDTO);
        if (typingIndicatorDTO.getIndicatorId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(indicatorId, typingIndicatorDTO.getIndicatorId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!typingIndicatorRepository.existsById(indicatorId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TypingIndicatorDTO> result = typingIndicatorService.partialUpdate(typingIndicatorDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, typingIndicatorDTO.getIndicatorId())
        );
    }

    /**
     * {@code GET  /typing-indicators} : get all the typingIndicators.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of typingIndicators in body.
     */
    @GetMapping("")
    public List<TypingIndicatorDTO> getAllTypingIndicators() {
        LOG.debug("REST request to get all TypingIndicators");
        return typingIndicatorService.findAll();
    }

    /**
     * {@code GET  /typing-indicators/:id} : get the "id" typingIndicator.
     *
     * @param id the id of the typingIndicatorDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the typingIndicatorDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TypingIndicatorDTO> getTypingIndicator(@PathVariable("id") String id) {
        LOG.debug("REST request to get TypingIndicator : {}", id);
        Optional<TypingIndicatorDTO> typingIndicatorDTO = typingIndicatorService.findOne(id);
        return ResponseUtil.wrapOrNotFound(typingIndicatorDTO);
    }

    /**
     * {@code DELETE  /typing-indicators/:id} : delete the "id" typingIndicator.
     *
     * @param id the id of the typingIndicatorDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTypingIndicator(@PathVariable("id") String id) {
        LOG.debug("REST request to delete TypingIndicator : {}", id);
        typingIndicatorService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
