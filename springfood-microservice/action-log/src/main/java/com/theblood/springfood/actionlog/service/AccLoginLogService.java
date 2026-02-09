package com.theblood.springfood.actionlog.service;

import com.theblood.springfood.actionlog.domain.AccLoginLog;
import com.theblood.springfood.actionlog.repository.AccLoginLogRepository;
import com.theblood.springfood.actionlog.service.dto.AccLoginLogDTO;
import com.theblood.springfood.actionlog.service.mapper.AccLoginLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link AccLoginLog}.
 */
@Service
@Transactional
public class AccLoginLogService {

    private static final Logger LOG = LoggerFactory.getLogger(AccLoginLogService.class);

    private final AccLoginLogRepository accLoginLogRepository;

    private final AccLoginLogMapper accLoginLogMapper;

    public AccLoginLogService(AccLoginLogRepository accLoginLogRepository, AccLoginLogMapper accLoginLogMapper) {
        this.accLoginLogRepository = accLoginLogRepository;
        this.accLoginLogMapper = accLoginLogMapper;
    }

    /**
     * Save a accLoginLog.
     *
     * @param accLoginLogDTO the entity to save.
     * @return the persisted entity.
     */
    public AccLoginLogDTO save(AccLoginLogDTO accLoginLogDTO) {
        LOG.debug("Request to save AccLoginLog : {}", accLoginLogDTO);
        AccLoginLog accLoginLog = accLoginLogMapper.toEntity(accLoginLogDTO);
        accLoginLog = accLoginLogRepository.save(accLoginLog);
        return accLoginLogMapper.toDto(accLoginLog);
    }

    /**
     * Update a accLoginLog.
     *
     * @param accLoginLogDTO the entity to save.
     * @return the persisted entity.
     */
    public AccLoginLogDTO update(AccLoginLogDTO accLoginLogDTO) {
        LOG.debug("Request to update AccLoginLog : {}", accLoginLogDTO);
        AccLoginLog accLoginLog = accLoginLogMapper.toEntity(accLoginLogDTO);
        accLoginLog.setIsPersisted();
        accLoginLog = accLoginLogRepository.save(accLoginLog);
        return accLoginLogMapper.toDto(accLoginLog);
    }

    /**
     * Partially update a accLoginLog.
     *
     * @param accLoginLogDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AccLoginLogDTO> partialUpdate(AccLoginLogDTO accLoginLogDTO) {
        LOG.debug("Request to partially update AccLoginLog : {}", accLoginLogDTO);

        return accLoginLogRepository
                .findById(accLoginLogDTO.getId())
                .map(existingAccLoginLog -> {
                    accLoginLogMapper.partialUpdate(existingAccLoginLog, accLoginLogDTO);

                    return existingAccLoginLog;
                })
                .map(accLoginLogRepository::save)
                .map(accLoginLogMapper::toDto);
    }

    /**
     * Get one accLoginLog by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AccLoginLogDTO> findOne(String id) {
        LOG.debug("Request to get AccLoginLog : {}", id);
        return accLoginLogRepository.findById(id).map(accLoginLogMapper::toDto);
    }

    /**
     * Delete the accLoginLog by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete AccLoginLog : {}", id);
        accLoginLogRepository.deleteById(id);
    }
}
