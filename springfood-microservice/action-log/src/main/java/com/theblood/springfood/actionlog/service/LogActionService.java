package com.theblood.springfood.actionlog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.actionlog.domain.LogAction;
import com.theblood.springfood.actionlog.repository.LogActionRepository;
import com.theblood.springfood.actionlog.service.dto.LogActionDTO;
import com.theblood.springfood.actionlog.service.dto.LogActionExportDTO;
import com.theblood.springfood.actionlog.carbone.CarboneService;
import com.theblood.springfood.actionlog.carbone.dto.CarboneResponseData;
import com.theblood.springfood.actionlog.service.mapper.LogActionClientMapper;
import com.theblood.springfood.actionlog.service.mapper.LogActionMapper;
import com.theblood.springfood.actionlog.service.util.FileUtil;
import com.theblood.springfood.client.api.LogActionsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Service Implementation for managing {@link LogAction}.
 */
@Service
@Transactional
public class LogActionService {

    private static final Logger LOG = LoggerFactory.getLogger(LogActionService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final CarboneService carboneService;

    private final LogActionRepository logActionRepository;

    private final LogActionMapper logActionMapper;

    private final LogActionClientMapper logActionClientMapper;

    public LogActionService(CarboneService carboneService, LogActionRepository logActionRepository, LogActionMapper logActionMapper, LogActionClientMapper logActionClientMapper) {
        this.carboneService = carboneService;
        this.logActionRepository = logActionRepository;
        this.logActionMapper = logActionMapper;
        this.logActionClientMapper = logActionClientMapper;
    }

    /**
     * Save a logAction.
     *
     * @param logActionDTO the entity to save.
     * @return the persisted entity.
     */
    public LogActionDTO save(LogActionsClient.LogActionsDto logActionDTO) {
        LOG.debug("Request to save LogAction : {}", logActionDTO);
        LogAction logAction = logActionClientMapper.toEntity(logActionDTO);
        logAction = logActionRepository.save(logAction);
        return logActionMapper.toDto(logAction);
    }

    /**
     * Update a logAction.
     *
     * @param logActionDTO the entity to save.
     * @return the persisted entity.
     */
    public LogActionDTO update(LogActionDTO logActionDTO) {
        LOG.debug("Request to update LogAction : {}", logActionDTO);
        LogAction logAction = logActionMapper.toEntity(logActionDTO);
        logAction = logActionRepository.save(logAction);
        return logActionMapper.toDto(logAction);
    }

    /**
     * Partially update a logAction.
     *
     * @param logActionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<LogActionDTO> partialUpdate(LogActionDTO logActionDTO) {
        LOG.debug("Request to partially update LogAction : {}", logActionDTO);

        return logActionRepository
                .findById(logActionDTO.getId())
                .map(existingLogAction -> {
                    logActionMapper.partialUpdate(existingLogAction, logActionDTO);

                    return existingLogAction;
                })
                .map(logActionRepository::save)
                .map(logActionMapper::toDto);
    }

    /**
     * Get one logAction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<LogActionDTO> findOne(String id) {
        LOG.debug("Request to get LogAction : {}", id);
        return logActionRepository.findById(id).map(logActionMapper::toDto);
    }

    /**
     * Delete the logAction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete LogAction : {}", id);
        logActionRepository.deleteById(id);
    }

    public void importData(String fileUrl) {
        String savePath = "downloaded_file.xls";
        FileUtil.downloadFile(fileUrl, savePath);
        var file = Paths.get(savePath).toFile();
        try {
            var rowDatas = FileUtil.readRowAsString(file);
            var dtos = rowDatas.stream().map(this::convertJsonToDTO).toList();
            var entities = logActionMapper.toEntity(dtos);
            logActionRepository.saveAllAndFlush(entities);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            FileUtil.cleanUpFile(file);
        }
    }

    public CarboneResponseData exportData() {
        var entities = logActionRepository.findAll();
        var dtos = logActionMapper.toDto(entities);
        var output = new LogActionExportDTO(dtos);
        var templateFilename = "logAction-export.xlsx";
        return carboneService.renderReport(output, templateFilename, "xlsx");
    }

    private LogActionDTO convertJsonToDTO(String json) {
        try {
            return objectMapper.readValue(json, LogActionDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
