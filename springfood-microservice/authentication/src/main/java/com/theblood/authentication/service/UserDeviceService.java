package com.theblood.authentication.service;

import com.theblood.authentication.model.UserDevice;
import com.theblood.authentication.repository.UserDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing UserDevice entities.
 */
@Service
@Transactional
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;

    @Autowired
    public UserDeviceService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    /**
     * Register a new device for user
     */
    public UserDevice registerDevice(UUID userId, String pushToken, String platform, String deviceName) {
        // Check if device with this push token already exists
        Optional<UserDevice> existingDevice = userDeviceRepository.findByPushToken(pushToken);
        if (existingDevice.isPresent()) {
            UserDevice device = existingDevice.get();
            // Update existing device
            device.setUserId(userId);
            device.setPlatform(platform);
            device.setDeviceName(deviceName);
            device.activate();
            return userDeviceRepository.save(device);
        }

        // Create new device
        UserDevice newDevice = new UserDevice(userId, pushToken, platform, deviceName);
        return userDeviceRepository.save(newDevice);
    }

    /**
     * Update device push token
     */
    public Optional<UserDevice> updatePushToken(UUID deviceId, String newPushToken) {
        Optional<UserDevice> device = userDeviceRepository.findById(deviceId);
        if (device.isPresent()) {
            UserDevice userDevice = device.get();
            userDevice.setPushToken(newPushToken);
            userDevice.updateLastUsed();
            return Optional.of(userDeviceRepository.save(userDevice));
        }
        return Optional.empty();
    }

    /**
     * Get all devices for a user
     */
    @Transactional(readOnly = true)
    public List<UserDevice> getUserDevices(UUID userId) {
        return userDeviceRepository.findByUserId(userId);
    }

    /**
     * Get active devices for a user
     */
    @Transactional(readOnly = true)
    public List<UserDevice> getActiveUserDevices(UUID userId) {
        return userDeviceRepository.findActiveDevicesByUserId(userId);
    }

    /**
     * Get devices by platform for a user
     */
    @Transactional(readOnly = true)
    public List<UserDevice> getUserDevicesByPlatform(UUID userId, String platform) {
        return userDeviceRepository.findByUserIdAndPlatform(userId, platform);
    }

    /**
     * Find device by push token
     */
    @Transactional(readOnly = true)
    public Optional<UserDevice> getDeviceByPushToken(String pushToken) {
        return userDeviceRepository.findByPushToken(pushToken);
    }

    /**
     * Activate device
     */
    public boolean activateDevice(UUID deviceId) {
        Optional<UserDevice> device = userDeviceRepository.findById(deviceId);
        if (device.isPresent()) {
            UserDevice userDevice = device.get();
            userDevice.activate();
            userDeviceRepository.save(userDevice);
            return true;
        }
        return false;
    }

    /**
     * Deactivate device
     */
    public boolean deactivateDevice(UUID deviceId) {
        Optional<UserDevice> device = userDeviceRepository.findById(deviceId);
        if (device.isPresent()) {
            UserDevice userDevice = device.get();
            userDevice.deactivate();
            userDeviceRepository.save(userDevice);
            return true;
        }
        return false;
    }

    /**
     * Deactivate device by push token
     */
    public int deactivateDeviceByPushToken(String pushToken) {
        return userDeviceRepository.deactivateByPushToken(pushToken);
    }

    /**
     * Deactivate all devices for a user
     */
    public int deactivateAllUserDevices(UUID userId) {
        return userDeviceRepository.deactivateAllByUserId(userId);
    }

    /**
     * Update device last used timestamp
     */
    public boolean updateDeviceLastUsed(UUID deviceId) {
        return userDeviceRepository.updateLastUsedAt(deviceId, Instant.now()) > 0;
    }

    /**
     * Update device last used by push token
     */
    public boolean updateDeviceLastUsedByToken(String pushToken) {
        Optional<UserDevice> device = userDeviceRepository.findByPushToken(pushToken);
        if (device.isPresent()) {
            return updateDeviceLastUsed(device.get().getDeviceId());
        }
        return false;
    }

    /**
     * Get count of active devices for user
     */
    @Transactional(readOnly = true)
    public long getActiveDeviceCount(UUID userId) {
        return userDeviceRepository.countActiveDevicesByUserId(userId);
    }

    /**
     * Get push tokens for active devices of a user
     */
    @Transactional(readOnly = true)
    public List<String> getUserPushTokens(UUID userId) {
        return userDeviceRepository.findPushTokensByUserIds(List.of(userId));
    }

    /**
     * Get push tokens for active devices of multiple users
     */
    @Transactional(readOnly = true)
    public List<String> getUsersPushTokens(List<UUID> userIds) {
        return userDeviceRepository.findPushTokensByUserIds(userIds);
    }

    /**
     * Get push tokens for specific platform
     */
    @Transactional(readOnly = true)
    public List<String> getUserPushTokensByPlatform(UUID userId, String platform) {
        return userDeviceRepository.findPushTokensByUserIdAndPlatform(userId, platform);
    }

    /**
     * Clean up inactive devices (older than specified days)
     */
    public int cleanupInactiveDevices(int daysThreshold) {
        Instant threshold = Instant.now().minus(daysThreshold, ChronoUnit.DAYS);
        return userDeviceRepository.deleteInactiveDevicesOlderThan(threshold);
    }

    /**
     * Get inactive devices for cleanup review
     */
    @Transactional(readOnly = true)
    public List<UserDevice> getInactiveDevices(int daysThreshold) {
        Instant threshold = Instant.now().minus(daysThreshold, ChronoUnit.DAYS);
        return userDeviceRepository.findInactiveDevices(threshold);
    }

    /**
     * Delete device
     */
    public boolean deleteDevice(UUID deviceId, UUID userId) {
        Optional<UserDevice> device = userDeviceRepository.findById(deviceId);
        if (device.isPresent() && device.get().getUserId().equals(userId)) {
            userDeviceRepository.deleteById(deviceId);
            return true;
        }
        return false;
    }

    /**
     * Check if push token exists
     */
    @Transactional(readOnly = true)
    public boolean pushTokenExists(String pushToken) {
        return userDeviceRepository.existsByPushToken(pushToken);
    }

    /**
     * Update device info
     */
    public Optional<UserDevice> updateDeviceInfo(UUID deviceId, UUID userId, String deviceName, String platform) {
        Optional<UserDevice> device = userDeviceRepository.findById(deviceId);
        if (device.isPresent() && device.get().getUserId().equals(userId)) {
            UserDevice userDevice = device.get();
            userDevice.setDeviceName(deviceName);
            userDevice.setPlatform(platform);
            userDevice.updateLastUsed();
            return Optional.of(userDeviceRepository.save(userDevice));
        }
        return Optional.empty();
    }
}