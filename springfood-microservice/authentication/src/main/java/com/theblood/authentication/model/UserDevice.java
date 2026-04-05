package com.theblood.authentication.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing user device for push notifications.
 * Maps to springfood_authentication.user_device table.
 */
@Entity
@Table(name = "user_device", schema = "springfood_authentication")
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "device_id")
    private UUID deviceId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Size(max = 500)
    @Column(name = "push_token", length = 500)
    private String pushToken; // FCM/APNs token

    @Size(max = 20)
    @Column(name = "platform", length = 20)
    private String platform; // IOS, ANDROID, WEB

    @Size(max = 255)
    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public UserDevice() {}

    public UserDevice(UUID userId, String pushToken, String platform, String deviceName) {
        this.userId = userId;
        this.pushToken = pushToken;
        this.platform = platform;
        this.deviceName = deviceName;
        this.lastUsedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Update last used timestamp
     */
    public void updateLastUsed() {
        this.lastUsedAt = Instant.now();
    }

    /**
     * Mark device as active
     */
    public void activate() {
        this.isActive = true;
        updateLastUsed();
    }

    /**
     * Mark device as inactive
     */
    public void deactivate() {
        this.isActive = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDevice)) return false;
        UserDevice that = (UserDevice) o;
        return deviceId != null && deviceId.equals(that.deviceId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UserDevice{" +
                "deviceId=" + deviceId +
                ", userId=" + userId +
                ", platform='" + platform + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", isActive=" + isActive +
                ", lastUsedAt=" + lastUsedAt +
                '}';
    }

    /**
     * Enum for supported platforms
     */
    public enum Platform {
        IOS,
        ANDROID,
        WEB
    }
}