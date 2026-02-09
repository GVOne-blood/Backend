package com.theblood.springfood.actionlog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.actionlog.domain.LogActionAnnualUpdate;
import com.theblood.springfood.actionlog.repository.LogActionAnnualUpdateRepository;
import com.theblood.springfood.actionlog.service.dto.LogActionAnnualUpdateDTO;
import com.theblood.springfood.actionlog.service.dto.LogActionAnnualUpdateExportDTO;
import com.theblood.springfood.actionlog.service.dto.carbone.CarboneResponseData;
import com.theblood.springfood.actionlog.service.mapper.LogActionAnnualUpdateMapper;
import com.theblood.springfood.actionlog.service.util.FileUtil;
import com.theblood.springfood.client.api.LogActionAnnualUpdateClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Service Implementation for managing {@link LogActionAnnualUpdate}.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class LogActionAnnualUpdateService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final CarboneService carboneService;

    private final LogActionAnnualUpdateRepository logActionAnnualUpdateRepository;

    private final LogActionAnnualUpdateMapper logActionAnnualUpdateMapper;

    /**
     * Save a logActionAnnualUpdate.
     *
     * @param logActionAnnualUpdateDTO the entity to save.
     * @return the persisted entity.
     */
    public LogActionAnnualUpdateDTO save(LogActionAnnualUpdateClient.LogActionAnnualUpdateDto logActionAnnualUpdateDTO) {
        LOG.debug("Request to save LogAction : {}", logActionAnnualUpdateDTO);
        LogActionAnnualUpdate logActionAnnualUpdate = logActionAnnualUpdateMapper.toEntity(logActionAnnualUpdateDTO);
        logActionAnnualUpdate = logActionAnnualUpdateRepository.save(logActionAnnualUpdate);
        return logActionAnnualUpdateMapper.toDto(logActionAnnualUpdate);
    }

    /**
     * Update a logActionAnnualUpdate.
     *
     * @param logActionAnnualUpdateDTO the entity to save.
     * @return the persisted entity.
     */
    public LogActionAnnualUpdateDTO update(LogActionAnnualUpdateDTO logActionAnnualUpdateDTO) {
        LOG.debug("Request to update LogActionAnnualUpdate : {}", logActionAnnualUpdateDTO);
        LogActionAnnualUpdate logActionAnnualUpdate = logActionAnnualUpdateMapper.toEntity(logActionAnnualUpdateDTO);
        logActionAnnualUpdate = logActionAnnualUpdateRepository.save(logActionAnnualUpdate);
        return logActionAnnualUpdateMapper.toDto(logActionAnnualUpdate);
    }

    /**
     * Partially update a logActionAnnualUpdate.
     *
     * @param logActionAnnualUpdateDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<LogActionAnnualUpdateDTO> partialUpdate(LogActionAnnualUpdateDTO logActionAnnualUpdateDTO) {
        LOG.debug("Request to partially update LogActionAnnualUpdate : {}", logActionAnnualUpdateDTO);

        return logActionAnnualUpdateRepository
                .findById(logActionAnnualUpdateDTO.getId())
                .map(existingLogActionAnnualUpdate -> {
                    logActionAnnualUpdateMapper.partialUpdate(existingLogActionAnnualUpdate, logActionAnnualUpdateDTO);

                    return existingLogActionAnnualUpdate;
                })
                .map(logActionAnnualUpdateRepository::save)
                .map(logActionAnnualUpdateMapper::toDto);
    }

    /**
     * Get one logActionAnnualUpdate by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<LogActionAnnualUpdateDTO> findOne(String id) {
        LOG.debug("Request to get LogActionAnnualUpdate : {}", id);
        return logActionAnnualUpdateRepository.findById(id).map(logActionAnnualUpdateMapper::toDto);
    }

    /**
     * Delete the logActionAnnualUpdate by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete LogActionAnnualUpdate : {}", id);
        logActionAnnualUpdateRepository.deleteById(id);
    }

    public void importData(String fileUrl) {
        String savePath = "downloaded_file.xls";
        FileUtil.downloadFile(fileUrl, savePath);
        File file = Path.of(savePath).toFile();
        try {
            var rowDatas = FileUtil.readRowAsString(file);
            var dtos = rowDatas.stream().map(this::convertJsonToDTO).toList();
            var entities = logActionAnnualUpdateMapper.toEntity(dtos);
            logActionAnnualUpdateRepository.saveAllAndFlush(entities);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            FileUtil.cleanUpFile(file);
        }
    }

    public CarboneResponseData exportData() {
        var entities = logActionAnnualUpdateRepository.findAll();
        var dtos = logActionAnnualUpdateMapper.toDto(entities);
        var output = new LogActionAnnualUpdateExportDTO(dtos);
        var templateFilename = "logActionAnnualUpdate-export.xlsx";
        return carboneService.renderReport(output, templateFilename, "xlsx");
    }

    private LogActionAnnualUpdateDTO convertJsonToDTO(String json) {
        try {
            return objectMapper.readValue(json, LogActionAnnualUpdateDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
