package com.theblood.authentication.repository;

import com.theblood.authentication.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserDevice entity.
 */
@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {

    /**
     * Find all devices for a user
     */
    List<UserDevice> findByUserId(UUID userId);

    /**
     * Find active devices for a user
     */
    @Query("SELECT ud FROM UserDevice ud WHERE ud.userId = :userId AND ud.isActive = true")
    List<UserDevice> findActiveDevicesByUserId(@Param("userId") UUID userId);

    /**
     * Find device by push token
     */
    Optional<UserDevice> findByPushToken(String pushToken);

    /**
     * Find devices by platform
     */
    @Query("SELECT ud FROM UserDevice ud WHERE ud.userId = :userId AND ud.platform = :platform AND ud.isActive = true")
    List<UserDevice> findByUserIdAndPlatform(@Param("userId") UUID userId, @Param("platform") String platform);

    /**
     * Find device by user and device name
     */
    Optional<UserDevice> findByUserIdAndDeviceName(UUID userId, String deviceName);

    /**
     * Check if push token exists
     */
    boolean existsByPushToken(String pushToken);

    /**
     * Count active devices for user
     */
    @Query("SELECT COUNT(ud) FROM UserDevice ud WHERE ud.userId = :userId AND ud.isActive = true")
    long countActiveDevicesByUserId(@Param("userId") UUID userId);

    /**
     * Find devices not used for a long time (for cleanup)
     */
    @Query("SELECT ud FROM UserDevice ud WHERE ud.lastUsedAt < :threshold")
    List<UserDevice> findInactiveDevices(@Param("threshold") Instant threshold);

    /**
     * Update last used time for device
     */
    @Modifying
    @Query("UPDATE UserDevice ud SET ud.lastUsedAt = :lastUsedAt WHERE ud.deviceId = :deviceId")
    int updateLastUsedAt(@Param("deviceId") UUID deviceId, @Param("lastUsedAt") Instant lastUsedAt);

    /**
     * Deactivate device by push token
     */
    @Modifying
    @Query("UPDATE UserDevice ud SET ud.isActive = false WHERE ud.pushToken = :pushToken")
    int deactivateByPushToken(@Param("pushToken") String pushToken);

    /**
     * Deactivate all devices for user
     */
    @Modifying
    @Query("UPDATE UserDevice ud SET ud.isActive = false WHERE ud.userId = :userId")
    int deactivateAllByUserId(@Param("userId") UUID userId);

    /**
     * Delete inactive devices older than threshold
     */
    @Modifying
    @Query("DELETE FROM UserDevice ud WHERE ud.isActive = false AND ud.lastUsedAt < :threshold")
    int deleteInactiveDevicesOlderThan(@Param("threshold") Instant threshold);

    /**
     * Get push tokens for active devices of multiple users
     */
    @Query("SELECT ud.pushToken FROM UserDevice ud WHERE ud.userId IN :userIds AND ud.isActive = true AND ud.pushToken IS NOT NULL")
    List<String> findPushTokensByUserIds(@Param("userIds") List<UUID> userIds);

    /**
     * Get push tokens for active devices of a user by platform
     */
    @Query("SELECT ud.pushToken FROM UserDevice ud WHERE ud.userId = :userId AND ud.platform = :platform AND ud.isActive = true AND ud.pushToken IS NOT NULL")
    List<String> findPushTokensByUserIdAndPlatform(@Param("userId") UUID userId, @Param("platform") String platform);
}