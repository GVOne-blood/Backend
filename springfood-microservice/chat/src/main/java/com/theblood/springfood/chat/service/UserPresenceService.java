package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.domain.UserPresence;
import com.theblood.springfood.chat.repository.UserPresenceRepository;
import com.theblood.springfood.chat.service.dto.UserPresenceDTO;
import com.theblood.springfood.chat.service.mapper.UserPresenceMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.chat.domain.UserPresence}.
 */
@Service
@Transactional
public class UserPresenceService {

    private static final Logger LOG = LoggerFactory.getLogger(UserPresenceService.class);

    private final UserPresenceRepository userPresenceRepository;

    private final UserPresenceMapper userPresenceMapper;

    public UserPresenceService(UserPresenceRepository userPresenceRepository, UserPresenceMapper userPresenceMapper) {
        this.userPresenceRepository = userPresenceRepository;
        this.userPresenceMapper = userPresenceMapper;
    }

    /**
     * Save a userPresence.
     *
     * @param userPresenceDTO the entity to save.
     * @return the persisted entity.
     */
    public UserPresenceDTO save(UserPresenceDTO userPresenceDTO) {
        LOG.debug("Request to save UserPresence : {}", userPresenceDTO);
        UserPresence userPresence = userPresenceMapper.toEntity(userPresenceDTO);
        userPresence = userPresenceRepository.save(userPresence);
        return userPresenceMapper.toDto(userPresence);
    }

    /**
     * Update a userPresence.
     *
     * @param userPresenceDTO the entity to save.
     * @return the persisted entity.
     */
    public UserPresenceDTO update(UserPresenceDTO userPresenceDTO) {
        LOG.debug("Request to update UserPresence : {}", userPresenceDTO);
        UserPresence userPresence = userPresenceMapper.toEntity(userPresenceDTO);
        userPresence.setIsPersisted();
        userPresence = userPresenceRepository.save(userPresence);
        return userPresenceMapper.toDto(userPresence);
    }

    /**
     * Partially update a userPresence.
     *
     * @param userPresenceDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<UserPresenceDTO> partialUpdate(UserPresenceDTO userPresenceDTO) {
        LOG.debug("Request to partially update UserPresence : {}", userPresenceDTO);

        return userPresenceRepository
            .findById(userPresenceDTO.getPresenceId())
            .map(existingUserPresence -> {
                userPresenceMapper.partialUpdate(existingUserPresence, userPresenceDTO);

                return existingUserPresence;
            })
            .map(userPresenceRepository::save)
            .map(userPresenceMapper::toDto);
    }

    /**
     * Get all the userPresences.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<UserPresenceDTO> findAll() {
        LOG.debug("Request to get all UserPresences");
        return userPresenceRepository.findAll().stream().map(userPresenceMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one userPresence by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<UserPresenceDTO> findOne(String id) {
        LOG.debug("Request to get UserPresence : {}", id);
        return userPresenceRepository.findById(id).map(userPresenceMapper::toDto);
    }

    /**
     * Delete the userPresence by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete UserPresence : {}", id);
        userPresenceRepository.deleteById(id);
    }
}
