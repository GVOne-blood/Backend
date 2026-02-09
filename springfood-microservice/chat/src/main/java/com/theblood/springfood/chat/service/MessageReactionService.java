package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.MessageReaction;
import com.theblood.springfood.chat.repository.MessageReactionRepository;
import com.theblood.springfood.chat.service.dto.MessageReactionDTO;
import com.theblood.springfood.chat.service.mapper.MessageReactionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.MessageReaction}.
 */
@Service
@Transactional
public class MessageReactionService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageReactionService.class);

    private final MessageReactionRepository messageReactionRepository;

    private final MessageReactionMapper messageReactionMapper;

    public MessageReactionService(MessageReactionRepository messageReactionRepository, MessageReactionMapper messageReactionMapper) {
        this.messageReactionRepository = messageReactionRepository;
        this.messageReactionMapper = messageReactionMapper;
    }

    /**
     * Save a messageReaction.
     *
     * @param messageReactionDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageReactionDTO save(MessageReactionDTO messageReactionDTO) {
        LOG.debug("Request to save MessageReaction : {}", messageReactionDTO);
        MessageReaction messageReaction = messageReactionMapper.toEntity(messageReactionDTO);
        messageReaction = messageReactionRepository.save(messageReaction);
        return messageReactionMapper.toDto(messageReaction);
    }

    /**
     * Update a messageReaction.
     *
     * @param messageReactionDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageReactionDTO update(MessageReactionDTO messageReactionDTO) {
        LOG.debug("Request to update MessageReaction : {}", messageReactionDTO);
        MessageReaction messageReaction = messageReactionMapper.toEntity(messageReactionDTO);
        messageReaction.setIsPersisted();
        messageReaction = messageReactionRepository.save(messageReaction);
        return messageReactionMapper.toDto(messageReaction);
    }

    /**
     * Partially update a messageReaction.
     *
     * @param messageReactionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MessageReactionDTO> partialUpdate(MessageReactionDTO messageReactionDTO) {
        LOG.debug("Request to partially update MessageReaction : {}", messageReactionDTO);

        return messageReactionRepository
            .findById(messageReactionDTO.getReactionId())
            .map(existingMessageReaction -> {
                messageReactionMapper.partialUpdate(existingMessageReaction, messageReactionDTO);

                return existingMessageReaction;
            })
            .map(messageReactionRepository::save)
            .map(messageReactionMapper::toDto);
    }

    /**
     * Get all the messageReactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MessageReactionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all MessageReactions");
        return messageReactionRepository.findAll(pageable).map(messageReactionMapper::toDto);
    }

    /**
     * Get one messageReaction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MessageReactionDTO> findOne(String id) {
        LOG.debug("Request to get MessageReaction : {}", id);
        return messageReactionRepository.findById(id).map(messageReactionMapper::toDto);
    }

    /**
     * Delete the messageReaction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete MessageReaction : {}", id);
        messageReactionRepository.deleteById(id);
    }
}
