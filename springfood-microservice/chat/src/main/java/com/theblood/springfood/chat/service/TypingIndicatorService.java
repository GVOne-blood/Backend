package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.TypingIndicator;
import com.theblood.springfood.chat.repository.TypingIndicatorRepository;
import com.theblood.springfood.chat.service.dto.TypingIndicatorDTO;
import com.theblood.springfood.chat.service.mapper.TypingIndicatorMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.TypingIndicator}.
 */
@Service
@Transactional
public class TypingIndicatorService {

    private static final Logger LOG = LoggerFactory.getLogger(TypingIndicatorService.class);

    private final TypingIndicatorRepository typingIndicatorRepository;

    private final TypingIndicatorMapper typingIndicatorMapper;

    public TypingIndicatorService(TypingIndicatorRepository typingIndicatorRepository, TypingIndicatorMapper typingIndicatorMapper) {
        this.typingIndicatorRepository = typingIndicatorRepository;
        this.typingIndicatorMapper = typingIndicatorMapper;
    }

    /**
     * Save a typingIndicator.
     *
     * @param typingIndicatorDTO the entity to save.
     * @return the persisted entity.
     */
    public TypingIndicatorDTO save(TypingIndicatorDTO typingIndicatorDTO) {
        LOG.debug("Request to save TypingIndicator : {}", typingIndicatorDTO);
        TypingIndicator typingIndicator = typingIndicatorMapper.toEntity(typingIndicatorDTO);
        typingIndicator = typingIndicatorRepository.save(typingIndicator);
        return typingIndicatorMapper.toDto(typingIndicator);
    }

    /**
     * Update a typingIndicator.
     *
     * @param typingIndicatorDTO the entity to save.
     * @return the persisted entity.
     */
    public TypingIndicatorDTO update(TypingIndicatorDTO typingIndicatorDTO) {
        LOG.debug("Request to update TypingIndicator : {}", typingIndicatorDTO);
        TypingIndicator typingIndicator = typingIndicatorMapper.toEntity(typingIndicatorDTO);
        typingIndicator.setIsPersisted();
        typingIndicator = typingIndicatorRepository.save(typingIndicator);
        return typingIndicatorMapper.toDto(typingIndicator);
    }

    /**
     * Partially update a typingIndicator.
     *
     * @param typingIndicatorDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TypingIndicatorDTO> partialUpdate(TypingIndicatorDTO typingIndicatorDTO) {
        LOG.debug("Request to partially update TypingIndicator : {}", typingIndicatorDTO);

        return typingIndicatorRepository
            .findById(typingIndicatorDTO.getIndicatorId())
            .map(existingTypingIndicator -> {
                typingIndicatorMapper.partialUpdate(existingTypingIndicator, typingIndicatorDTO);

                return existingTypingIndicator;
            })
            .map(typingIndicatorRepository::save)
            .map(typingIndicatorMapper::toDto);
    }

    /**
     * Get all the typingIndicators.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<TypingIndicatorDTO> findAll() {
        LOG.debug("Request to get all TypingIndicators");
        return typingIndicatorRepository
            .findAll()
            .stream()
            .map(typingIndicatorMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one typingIndicator by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TypingIndicatorDTO> findOne(String id) {
        LOG.debug("Request to get TypingIndicator : {}", id);
        return typingIndicatorRepository.findById(id).map(typingIndicatorMapper::toDto);
    }

    /**
     * Delete the typingIndicator by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete TypingIndicator : {}", id);
        typingIndicatorRepository.deleteById(id);
    }
}
