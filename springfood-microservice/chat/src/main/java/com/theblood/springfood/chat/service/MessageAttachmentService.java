package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.MessageAttachment;
import com.theblood.springfood.chat.repository.MessageAttachmentRepository;
import com.theblood.springfood.chat.service.dto.MessageAttachmentDTO;
import com.theblood.springfood.chat.service.mapper.MessageAttachmentMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.MessageAttachment}.
 */
@Service
@Transactional
public class MessageAttachmentService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageAttachmentService.class);

    private final MessageAttachmentRepository messageAttachmentRepository;

    private final MessageAttachmentMapper messageAttachmentMapper;

    public MessageAttachmentService(
        MessageAttachmentRepository messageAttachmentRepository,
        MessageAttachmentMapper messageAttachmentMapper
    ) {
        this.messageAttachmentRepository = messageAttachmentRepository;
        this.messageAttachmentMapper = messageAttachmentMapper;
    }

    /**
     * Save a messageAttachment.
     *
     * @param messageAttachmentDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageAttachmentDTO save(MessageAttachmentDTO messageAttachmentDTO) {
        LOG.debug("Request to save MessageAttachment : {}", messageAttachmentDTO);
        MessageAttachment messageAttachment = messageAttachmentMapper.toEntity(messageAttachmentDTO);
        messageAttachment = messageAttachmentRepository.save(messageAttachment);
        return messageAttachmentMapper.toDto(messageAttachment);
    }

    /**
     * Update a messageAttachment.
     *
     * @param messageAttachmentDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageAttachmentDTO update(MessageAttachmentDTO messageAttachmentDTO) {
        LOG.debug("Request to update MessageAttachment : {}", messageAttachmentDTO);
        MessageAttachment messageAttachment = messageAttachmentMapper.toEntity(messageAttachmentDTO);
        messageAttachment.setIsPersisted();
        messageAttachment = messageAttachmentRepository.save(messageAttachment);
        return messageAttachmentMapper.toDto(messageAttachment);
    }

    /**
     * Partially update a messageAttachment.
     *
     * @param messageAttachmentDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MessageAttachmentDTO> partialUpdate(MessageAttachmentDTO messageAttachmentDTO) {
        LOG.debug("Request to partially update MessageAttachment : {}", messageAttachmentDTO);

        return messageAttachmentRepository
            .findById(messageAttachmentDTO.getAttachmentId())
            .map(existingMessageAttachment -> {
                messageAttachmentMapper.partialUpdate(existingMessageAttachment, messageAttachmentDTO);

                return existingMessageAttachment;
            })
            .map(messageAttachmentRepository::save)
            .map(messageAttachmentMapper::toDto);
    }

    /**
     * Get all the messageAttachments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MessageAttachmentDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all MessageAttachments");
        return messageAttachmentRepository.findAll(pageable).map(messageAttachmentMapper::toDto);
    }

    /**
     * Get one messageAttachment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MessageAttachmentDTO> findOne(String id) {
        LOG.debug("Request to get MessageAttachment : {}", id);
        return messageAttachmentRepository.findById(id).map(messageAttachmentMapper::toDto);
    }

    /**
     * Delete the messageAttachment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete MessageAttachment : {}", id);
        messageAttachmentRepository.deleteById(id);
    }
}
