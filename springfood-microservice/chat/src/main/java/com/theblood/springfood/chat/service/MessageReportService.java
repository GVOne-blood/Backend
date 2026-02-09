package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.MessageReport;
import com.theblood.springfood.chat.repository.MessageReportRepository;
import com.theblood.springfood.chat.service.dto.MessageReportDTO;
import com.theblood.springfood.chat.service.mapper.MessageReportMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.MessageReport}.
 */
@Service
@Transactional
public class MessageReportService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageReportService.class);

    private final MessageReportRepository messageReportRepository;

    private final MessageReportMapper messageReportMapper;

    public MessageReportService(MessageReportRepository messageReportRepository, MessageReportMapper messageReportMapper) {
        this.messageReportRepository = messageReportRepository;
        this.messageReportMapper = messageReportMapper;
    }

    /**
     * Save a messageReport.
     *
     * @param messageReportDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageReportDTO save(MessageReportDTO messageReportDTO) {
        LOG.debug("Request to save MessageReport : {}", messageReportDTO);
        MessageReport messageReport = messageReportMapper.toEntity(messageReportDTO);
        messageReport = messageReportRepository.save(messageReport);
        return messageReportMapper.toDto(messageReport);
    }

    /**
     * Update a messageReport.
     *
     * @param messageReportDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageReportDTO update(MessageReportDTO messageReportDTO) {
        LOG.debug("Request to update MessageReport : {}", messageReportDTO);
        MessageReport messageReport = messageReportMapper.toEntity(messageReportDTO);
        messageReport.setIsPersisted();
        messageReport = messageReportRepository.save(messageReport);
        return messageReportMapper.toDto(messageReport);
    }

    /**
     * Partially update a messageReport.
     *
     * @param messageReportDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MessageReportDTO> partialUpdate(MessageReportDTO messageReportDTO) {
        LOG.debug("Request to partially update MessageReport : {}", messageReportDTO);

        return messageReportRepository
            .findById(messageReportDTO.getReportId())
            .map(existingMessageReport -> {
                messageReportMapper.partialUpdate(existingMessageReport, messageReportDTO);

                return existingMessageReport;
            })
            .map(messageReportRepository::save)
            .map(messageReportMapper::toDto);
    }

    /**
     * Get all the messageReports.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MessageReportDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all MessageReports");
        return messageReportRepository.findAll(pageable).map(messageReportMapper::toDto);
    }

    /**
     * Get one messageReport by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MessageReportDTO> findOne(String id) {
        LOG.debug("Request to get MessageReport : {}", id);
        return messageReportRepository.findById(id).map(messageReportMapper::toDto);
    }

    /**
     * Delete the messageReport by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete MessageReport : {}", id);
        messageReportRepository.deleteById(id);
    }
}
