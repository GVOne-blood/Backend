package com.theblood.shopservice.service;

import com.theblood.shopservice.domain.PlatformCommissionConfig;
import com.theblood.shopservice.repository.PlatformCommissionConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing PlatformCommissionConfig entities.
 */
@Service
@Transactional
public class PlatformCommissionConfigService {

    private final PlatformCommissionConfigRepository platformCommissionConfigRepository;

    @Autowired
    public PlatformCommissionConfigService(PlatformCommissionConfigRepository platformCommissionConfigRepository) {
        this.platformCommissionConfigRepository = platformCommissionConfigRepository;
    }

    /**
     * Create a new platform commission config
     */
    public PlatformCommissionConfig createCommissionConfig(PlatformCommissionConfig config) {
        // Deactivate current active config if this one is set to active
        if (Boolean.TRUE.equals(config.getIsActive())) {
            deactivateCurrentConfig();
        }
        
        return platformCommissionConfigRepository.save(config);
    }

    /**
     * Update an existing commission config
     */
    public PlatformCommissionConfig updateCommissionConfig(UUID configId, PlatformCommissionConfig config) {
        PlatformCommissionConfig existingConfig = platformCommissionConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Commission config not found with id: " + configId));

        // If setting this config as active, deactivate others
        if (Boolean.TRUE.equals(config.getIsActive()) && !Boolean.TRUE.equals(existingConfig.getIsActive())) {
            deactivateCurrentConfig();
        }

        existingConfig.setName(config.getName());
        existingConfig.setDescription(config.getDescription());
        existingConfig.setCommissionType(config.getCommissionType());
        existingConfig.setPercentRate(config.getPercentRate());
        existingConfig.setFlatAmount(config.getFlatAmount());
        existingConfig.setMinCommission(config.getMinCommission());
        existingConfig.setMaxCommission(config.getMaxCommission());
        existingConfig.setIsActive(config.getIsActive());
        existingConfig.setEffectiveFrom(config.getEffectiveFrom());
        existingConfig.setEffectiveTo(config.getEffectiveTo());
        existingConfig.setUpdatedBy(config.getUpdatedBy());

        return platformCommissionConfigRepository.save(existingConfig);
    }

    /**
     * Get commission config by ID
     */
    @Transactional(readOnly = true)
    public Optional<PlatformCommissionConfig> getCommissionConfigById(UUID configId) {
        return platformCommissionConfigRepository.findById(configId);
    }

    /**
     * Get currently active commission config
     */
    @Transactional(readOnly = true)
    public Optional<PlatformCommissionConfig> getActiveCommissionConfig() {
        return platformCommissionConfigRepository.findActiveConfig();
    }

    /**
     * Get currently effective commission config
     */
    @Transactional(readOnly = true)
    public Optional<PlatformCommissionConfig> getCurrentlyEffectiveConfig() {
        return platformCommissionConfigRepository.findCurrentlyEffectiveConfig(Instant.now());
    }

    /**
     * Get all commission configs
     */
    @Transactional(readOnly = true)
    public List<PlatformCommissionConfig> getAllCommissionConfigs() {
        return platformCommissionConfigRepository.findAll();
    }

    /**
     * Get configs by commission type
     */
    @Transactional(readOnly = true)
    public List<PlatformCommissionConfig> getConfigsByCommissionType(String commissionType) {
        return platformCommissionConfigRepository.findByCommissionType(commissionType);
    }

    /**
     * Get configs effective within date range
     */
    @Transactional(readOnly = true)
    public List<PlatformCommissionConfig> getConfigsEffectiveInDateRange(Instant startDate, Instant endDate) {
        return platformCommissionConfigRepository.findEffectiveInDateRange(startDate, endDate);
    }

    /**
     * Get configs created by specific admin
     */
    @Transactional(readOnly = true)
    public List<PlatformCommissionConfig> getConfigsByCreatedBy(String createdBy) {
        return platformCommissionConfigRepository.findByCreatedByOrderByCreatedAtDesc(createdBy);
    }

    /**
     * Get future configs
     */
    @Transactional(readOnly = true)
    public List<PlatformCommissionConfig> getFutureConfigs() {
        return platformCommissionConfigRepository.findFutureConfigs(Instant.now());
    }

    /**
     * Get expired configs
     */
    @Transactional(readOnly = true)
    public List<PlatformCommissionConfig> getExpiredConfigs() {
        return platformCommissionConfigRepository.findExpiredConfigs(Instant.now());
    }

    /**
     * Search configs by name
     */
    @Transactional(readOnly = true)
    public List<PlatformCommissionConfig> searchConfigsByName(String name) {
        return platformCommissionConfigRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Activate a commission config
     */
    public void activateCommissionConfig(UUID configId) {
        PlatformCommissionConfig config = platformCommissionConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Commission config not found with id: " + configId));

        // Deactivate current active config
        deactivateCurrentConfig();

        // Activate this config
        config.setIsActive(true);
        platformCommissionConfigRepository.save(config);
    }

    /**
     * Deactivate a commission config
     */
    public void deactivateCommissionConfig(UUID configId) {
        PlatformCommissionConfig config = platformCommissionConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Commission config not found with id: " + configId));

        config.setIsActive(false);
        platformCommissionConfigRepository.save(config);
    }

    /**
     * Delete commission config
     */
    public void deleteCommissionConfig(UUID configId) {
        if (!platformCommissionConfigRepository.existsById(configId)) {
            throw new IllegalArgumentException("Commission config not found with id: " + configId);
        }
        platformCommissionConfigRepository.deleteById(configId);
    }

    /**
     * Get count of active configs
     */
    @Transactional(readOnly = true)
    public long getActiveConfigCount() {
        return platformCommissionConfigRepository.countActiveConfigs();
    }

    /**
     * Get latest config
     */
    @Transactional(readOnly = true)
    public Optional<PlatformCommissionConfig> getLatestConfig() {
        return platformCommissionConfigRepository.findTopByOrderByCreatedAtDesc();
    }

    /**
     * Check if there's an active config at specific time
     */
    @Transactional(readOnly = true)
    public boolean hasActiveConfigAt(Instant time) {
        return platformCommissionConfigRepository.hasActiveConfigAt(time);
    }

    /**
     * Get commission config effective at specific time
     */
    @Transactional(readOnly = true)
    public Optional<PlatformCommissionConfig> getConfigEffectiveAt(Instant time) {
        return platformCommissionConfigRepository.findCurrentlyEffectiveConfig(time);
    }

    /**
     * Private method to deactivate current active config
     */
    private void deactivateCurrentConfig() {
        Optional<PlatformCommissionConfig> currentActive = platformCommissionConfigRepository.findActiveConfig();
        if (currentActive.isPresent()) {
            PlatformCommissionConfig activeConfig = currentActive.get();
            activeConfig.setIsActive(false);
            platformCommissionConfigRepository.save(activeConfig);
        }
    }

    /**
     * Schedule config to become active at future date
     */
    public PlatformCommissionConfig scheduleCommissionConfig(PlatformCommissionConfig config, Instant effectiveFrom) {
        config.setEffectiveFrom(effectiveFrom);
        config.setIsActive(true);
        return platformCommissionConfigRepository.save(config);
    }

    /**
     * Expire config at specific date
     */
    public void expireCommissionConfig(UUID configId, Instant effectiveTo) {
        PlatformCommissionConfig config = platformCommissionConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Commission config not found with id: " + configId));

        config.setEffectiveTo(effectiveTo);
        platformCommissionConfigRepository.save(config);
    }
}