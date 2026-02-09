package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.MessageReadReceipt;
import com.theblood.springfood.chat.repository.MessageReadReceiptRepository;
import com.theblood.springfood.chat.service.dto.MessageReadReceiptDTO;
import com.theblood.springfood.chat.service.mapper.MessageReadReceiptMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.MessageReadReceipt}.
 */
@Service
@Transactional
public class MessageReadReceiptService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageReadReceiptService.class);

    private final MessageReadReceiptRepository messageReadReceiptRepository;

    private final MessageReadReceiptMapper messageReadReceiptMapper;

    public MessageReadReceiptService(
        MessageReadReceiptRepository messageReadReceiptRepository,
        MessageReadReceiptMapper messageReadReceiptMapper
    ) {
        this.messageReadReceiptRepository = messageReadReceiptRepository;
        this.messageReadReceiptMapper = messageReadReceiptMapper;
    }

    /**
     * Save a messageReadReceipt.
     *
     * @param messageReadReceiptDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageReadReceiptDTO save(MessageReadReceiptDTO messageReadReceiptDTO) {
        LOG.debug("Request to save MessageReadReceipt : {}", messageReadReceiptDTO);
        MessageReadReceipt messageReadReceipt = messageReadReceiptMapper.toEntity(messageReadReceiptDTO);
        messageReadReceipt = messageReadReceiptRepository.save(messageReadReceipt);
        return messageReadReceiptMapper.toDto(messageReadReceipt);
    }

    /**
     * Update a messageReadReceipt.
     *
     * @param messageReadReceiptDTO the entity to save.
     * @return the persisted entity.
     */
    public MessageReadReceiptDTO update(MessageReadReceiptDTO messageReadReceiptDTO) {
        LOG.debug("Request to update MessageReadReceipt : {}", messageReadReceiptDTO);
        MessageReadReceipt messageReadReceipt = messageReadReceiptMapper.toEntity(messageReadReceiptDTO);
        messageReadReceipt.setIsPersisted();
        messageReadReceipt = messageReadReceiptRepository.save(messageReadReceipt);
        return messageReadReceiptMapper.toDto(messageReadReceipt);
    }

    /**
     * Partially update a messageReadReceipt.
     *
     * @param messageReadReceiptDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MessageReadReceiptDTO> partialUpdate(MessageReadReceiptDTO messageReadReceiptDTO) {
        LOG.debug("Request to partially update MessageReadReceipt : {}", messageReadReceiptDTO);

        return messageReadReceiptRepository
            .findById(messageReadReceiptDTO.getReceiptId())
            .map(existingMessageReadReceipt -> {
                messageReadReceiptMapper.partialUpdate(existingMessageReadReceipt, messageReadReceiptDTO);

                return existingMessageReadReceipt;
            })
            .map(messageReadReceiptRepository::save)
            .map(messageReadReceiptMapper::toDto);
    }

    /**
     * Get all the messageReadReceipts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MessageReadReceiptDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all MessageReadReceipts");
        return messageReadReceiptRepository.findAll(pageable).map(messageReadReceiptMapper::toDto);
    }

    /**
     * Get one messageReadReceipt by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MessageReadReceiptDTO> findOne(String id) {
        LOG.debug("Request to get MessageReadReceipt : {}", id);
        return messageReadReceiptRepository.findById(id).map(messageReadReceiptMapper::toDto);
    }

    /**
     * Delete the messageReadReceipt by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete MessageReadReceipt : {}", id);
        messageReadReceiptRepository.deleteById(id);
    }
}
