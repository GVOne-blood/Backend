package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.BlockedUser;
import com.theblood.springfood.chat.repository.BlockedUserRepository;
import com.theblood.springfood.chat.service.dto.BlockedUserDTO;
import com.theblood.springfood.chat.service.mapper.BlockedUserMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.BlockedUser}.
 */
@Service
@Transactional
public class BlockedUserService {

    private static final Logger LOG = LoggerFactory.getLogger(BlockedUserService.class);

    private final BlockedUserRepository blockedUserRepository;

    private final BlockedUserMapper blockedUserMapper;

    public BlockedUserService(BlockedUserRepository blockedUserRepository, BlockedUserMapper blockedUserMapper) {
        this.blockedUserRepository = blockedUserRepository;
        this.blockedUserMapper = blockedUserMapper;
    }

    /**
     * Save a blockedUser.
     *
     * @param blockedUserDTO the entity to save.
     * @return the persisted entity.
     */
    public BlockedUserDTO save(BlockedUserDTO blockedUserDTO) {
        LOG.debug("Request to save BlockedUser : {}", blockedUserDTO);
        BlockedUser blockedUser = blockedUserMapper.toEntity(blockedUserDTO);
        blockedUser = blockedUserRepository.save(blockedUser);
        return blockedUserMapper.toDto(blockedUser);
    }

    /**
     * Update a blockedUser.
     *
     * @param blockedUserDTO the entity to save.
     * @return the persisted entity.
     */
    public BlockedUserDTO update(BlockedUserDTO blockedUserDTO) {
        LOG.debug("Request to update BlockedUser : {}", blockedUserDTO);
        BlockedUser blockedUser = blockedUserMapper.toEntity(blockedUserDTO);
        blockedUser.setIsPersisted();
        blockedUser = blockedUserRepository.save(blockedUser);
        return blockedUserMapper.toDto(blockedUser);
    }

    /**
     * Partially update a blockedUser.
     *
     * @param blockedUserDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<BlockedUserDTO> partialUpdate(BlockedUserDTO blockedUserDTO) {
        LOG.debug("Request to partially update BlockedUser : {}", blockedUserDTO);

        return blockedUserRepository
            .findById(blockedUserDTO.getBlockId())
            .map(existingBlockedUser -> {
                blockedUserMapper.partialUpdate(existingBlockedUser, blockedUserDTO);

                return existingBlockedUser;
            })
            .map(blockedUserRepository::save)
            .map(blockedUserMapper::toDto);
    }

    /**
     * Get all the blockedUsers.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<BlockedUserDTO> findAll() {
        LOG.debug("Request to get all BlockedUsers");
        return blockedUserRepository.findAll().stream().map(blockedUserMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one blockedUser by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<BlockedUserDTO> findOne(String id) {
        LOG.debug("Request to get BlockedUser : {}", id);
        return blockedUserRepository.findById(id).map(blockedUserMapper::toDto);
    }

    /**
     * Delete the blockedUser by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete BlockedUser : {}", id);
        blockedUserRepository.deleteById(id);
    }
}
