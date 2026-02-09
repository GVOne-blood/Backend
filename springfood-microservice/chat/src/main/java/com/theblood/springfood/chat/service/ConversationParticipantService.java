package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.service.dto.ConversationParticipantDTO;
import com.theblood.springfood.chat.service.mapper.ConversationParticipantMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.ConversationParticipant}.
 */
@Service
@Transactional
public class ConversationParticipantService {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationParticipantService.class);

    private final ConversationParticipantRepository conversationParticipantRepository;

    private final ConversationParticipantMapper conversationParticipantMapper;

    public ConversationParticipantService(
        ConversationParticipantRepository conversationParticipantRepository,
        ConversationParticipantMapper conversationParticipantMapper
    ) {
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.conversationParticipantMapper = conversationParticipantMapper;
    }

    /**
     * Save a conversationParticipant.
     *
     * @param conversationParticipantDTO the entity to save.
     * @return the persisted entity.
     */
    public ConversationParticipantDTO save(ConversationParticipantDTO conversationParticipantDTO) {
        LOG.debug("Request to save ConversationParticipant : {}", conversationParticipantDTO);
        ConversationParticipant conversationParticipant = conversationParticipantMapper.toEntity(conversationParticipantDTO);
        conversationParticipant = conversationParticipantRepository.save(conversationParticipant);
        return conversationParticipantMapper.toDto(conversationParticipant);
    }

    /**
     * Update a conversationParticipant.
     *
     * @param conversationParticipantDTO the entity to save.
     * @return the persisted entity.
     */
    public ConversationParticipantDTO update(ConversationParticipantDTO conversationParticipantDTO) {
        LOG.debug("Request to update ConversationParticipant : {}", conversationParticipantDTO);
        ConversationParticipant conversationParticipant = conversationParticipantMapper.toEntity(conversationParticipantDTO);
        conversationParticipant.setIsPersisted();
        conversationParticipant = conversationParticipantRepository.save(conversationParticipant);
        return conversationParticipantMapper.toDto(conversationParticipant);
    }

    /**
     * Partially update a conversationParticipant.
     *
     * @param conversationParticipantDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ConversationParticipantDTO> partialUpdate(ConversationParticipantDTO conversationParticipantDTO) {
        LOG.debug("Request to partially update ConversationParticipant : {}", conversationParticipantDTO);

        return conversationParticipantRepository
            .findById(conversationParticipantDTO.getParticipantId())
            .map(existingConversationParticipant -> {
                conversationParticipantMapper.partialUpdate(existingConversationParticipant, conversationParticipantDTO);

                return existingConversationParticipant;
            })
            .map(conversationParticipantRepository::save)
            .map(conversationParticipantMapper::toDto);
    }

    /**
     * Get all the conversationParticipants.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ConversationParticipantDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ConversationParticipants");
        return conversationParticipantRepository.findAll(pageable).map(conversationParticipantMapper::toDto);
    }

    /**
     * Get one conversationParticipant by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ConversationParticipantDTO> findOne(String id) {
        LOG.debug("Request to get ConversationParticipant : {}", id);
        return conversationParticipantRepository.findById(id).map(conversationParticipantMapper::toDto);
    }

    /**
     * Delete the conversationParticipant by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete ConversationParticipant : {}", id);
        conversationParticipantRepository.deleteById(id);
    }
}
