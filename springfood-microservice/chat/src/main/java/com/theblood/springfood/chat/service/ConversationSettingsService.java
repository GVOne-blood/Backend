package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.ConversationSettings;
import com.theblood.springfood.chat.repository.ConversationSettingsRepository;
import com.theblood.springfood.chat.service.dto.ConversationSettingsDTO;
import com.theblood.springfood.chat.service.mapper.ConversationSettingsMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.ConversationSettings}.
 */
@Service
@Transactional
public class ConversationSettingsService {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationSettingsService.class);

    private final ConversationSettingsRepository conversationSettingsRepository;

    private final ConversationSettingsMapper conversationSettingsMapper;

    public ConversationSettingsService(
        ConversationSettingsRepository conversationSettingsRepository,
        ConversationSettingsMapper conversationSettingsMapper
    ) {
        this.conversationSettingsRepository = conversationSettingsRepository;
        this.conversationSettingsMapper = conversationSettingsMapper;
    }

    /**
     * Save a conversationSettings.
     *
     * @param conversationSettingsDTO the entity to save.
     * @return the persisted entity.
     */
    public ConversationSettingsDTO save(ConversationSettingsDTO conversationSettingsDTO) {
        LOG.debug("Request to save ConversationSettings : {}", conversationSettingsDTO);
        ConversationSettings conversationSettings = conversationSettingsMapper.toEntity(conversationSettingsDTO);
        conversationSettings = conversationSettingsRepository.save(conversationSettings);
        return conversationSettingsMapper.toDto(conversationSettings);
    }

    /**
     * Update a conversationSettings.
     *
     * @param conversationSettingsDTO the entity to save.
     * @return the persisted entity.
     */
    public ConversationSettingsDTO update(ConversationSettingsDTO conversationSettingsDTO) {
        LOG.debug("Request to update ConversationSettings : {}", conversationSettingsDTO);
        ConversationSettings conversationSettings = conversationSettingsMapper.toEntity(conversationSettingsDTO);
        conversationSettings.setIsPersisted();
        conversationSettings = conversationSettingsRepository.save(conversationSettings);
        return conversationSettingsMapper.toDto(conversationSettings);
    }

    /**
     * Partially update a conversationSettings.
     *
     * @param conversationSettingsDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ConversationSettingsDTO> partialUpdate(ConversationSettingsDTO conversationSettingsDTO) {
        LOG.debug("Request to partially update ConversationSettings : {}", conversationSettingsDTO);

        return conversationSettingsRepository
            .findById(conversationSettingsDTO.getSettingsId())
            .map(existingConversationSettings -> {
                conversationSettingsMapper.partialUpdate(existingConversationSettings, conversationSettingsDTO);

                return existingConversationSettings;
            })
            .map(conversationSettingsRepository::save)
            .map(conversationSettingsMapper::toDto);
    }

    /**
     * Get all the conversationSettings.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ConversationSettingsDTO> findAll() {
        LOG.debug("Request to get all ConversationSettings");
        return conversationSettingsRepository
            .findAll()
            .stream()
            .map(conversationSettingsMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     *  Get all the conversationSettings where Conversation is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ConversationSettingsDTO> findAllWhereConversationIsNull() {
        LOG.debug("Request to get all conversationSettings where Conversation is null");
        return StreamSupport.stream(conversationSettingsRepository.findAll().spliterator(), false)
            .filter(conversationSettings -> conversationSettings.getConversation() == null)
            .map(conversationSettingsMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one conversationSettings by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ConversationSettingsDTO> findOne(String id) {
        LOG.debug("Request to get ConversationSettings : {}", id);
        return conversationSettingsRepository.findById(id).map(conversationSettingsMapper::toDto);
    }

    /**
     * Delete the conversationSettings by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete ConversationSettings : {}", id);
        conversationSettingsRepository.deleteById(id);
    }
}
