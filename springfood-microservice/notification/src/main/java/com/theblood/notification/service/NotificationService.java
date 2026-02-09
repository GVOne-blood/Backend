package com.theblood.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.common.constant.Constant.ResponseCode;
import com.theblood.common.dto.request.CustomUserPrincipal;
import com.theblood.common.dto.request.UserContextHolder;
import com.theblood.common.exception.custom.ApplicationException;
import com.theblood.common.exception.custom.CustomException;
import com.theblood.common.exception.custom.StatusCode;
import com.theblood.common.util.CommonHttpRequestUtil;
import com.theblood.notification.constant.Constant;
import com.theblood.notification.domain.Notification;
import com.theblood.notification.repository.NotificationRepository;
import com.theblood.notification.service.dto.DeleteNotificationResponseDTO;
import com.theblood.notification.service.dto.NotificationDTO;
import com.theblood.notification.service.dto.ViewNotificationDTO;
import com.theblood.notification.service.mapper.NotificationMapper;
import com.theblood.springfood.client.service.LoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link com.theblood.springfood.notification.domain.Notification}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationsRepository;
    private final NotificationQueryService notificationQueryService;
    private final LoggingService loggingService;
    private final NotificationMapper notificationsMapper;

    /**
     * Save a notifications.
     *
     * @param notificationsDTO the entity to save.
     * @return the persisted entity.
     */
    public NotificationDTO save(NotificationDTO notificationsDTO, HttpServletRequest request) {
        LOG.debug("Request to save Notification : {}", notificationsDTO);

        CustomUserPrincipal userContext = UserContextHolder.getContext();

        validateNotificationOnCreate(notificationsDTO);
        Notification notifications = notificationsMapper.toEntity(notificationsDTO);

        notifications = notificationsRepository.save(notifications);

        // call service find all devices of user to push notify to message queue
        notificationQueryService.sendNotificationToUserDevices(notifications);


        try {
            String afterData = convertToJson(notifications);
            loggingService.createLogAction(
                Constant.ACTION_TYPE_DELETE,        // actionType
                null,                               // oldValue (null for CREATE)
                afterData,                          // newValue
                "Lưu thông báo mới",       // description
                Constant.TABLE_NAME_NOTIFICATIONS,       // tableName
                null,                   // objectId
                userContext.getUserIdString(),                   // accountId
                userContext.getUsername(),                    // userName
                null,                    // organizationId
                CommonHttpRequestUtil.getClientIpAddress(request),                          // ipAddress
                CommonHttpRequestUtil.getUserAgent(request)                           // userAgent
            );
        } catch (Exception logEx) {
            LOG.error("Failed to create log action for Learning {}: {}", notifications, logEx.getMessage());
        }
        return notificationsMapper.toDto(notifications);
    }

    public List<NotificationDTO> saveAll(List<NotificationDTO> notificationsDTOs) {
        LOG.debug("Request to save Notification : {}", notificationsDTOs);

        CustomUserPrincipal userContext = UserContextHolder.getContext();

        notificationsDTOs.forEach(this::validateNotificationOnCreate);

        List<Notification> notifications = notificationsMapper.toEntity(notificationsDTOs);

        notifications = notificationsRepository.saveAll(notifications);

        // call service find all devices of user to push notify to message queue
        notifications.forEach(notificationQueryService::sendNotificationToUserDevices);

        return notificationsMapper.toDto(notifications);
    }


    public void getAllActiveUserDevices() {

    }

    /**
     * Get all the notifications for current user.
     *
     * @return the list of DTO entities.
     */
    public Page<NotificationDTO> getUserNotification(Pageable pageable) {
        String currentAccountId = UserContextHolder.getContext().getUserIdString();
        LOG.debug("Request to get notifications for user: {}", currentAccountId);
        String shopId = UserContextHolder.getContext().getShopId();
        if (currentAccountId == null || currentAccountId.isEmpty())
            throw new ApplicationException(StatusCode.NOT_PERMIT, "User not authenticated. Missing user context.");
        Page<Notification> notificationsList = notificationsRepository.findByReceiveIdAndIsActiveOrderByCreatedDateDesc(pageable, currentAccountId, 1);

        return notificationsList.map(notificationsMapper::toDto);
    }


    public Long countUnreadNotification() {
        String currentAccountId = UserContextHolder.getContext().getUserIdString();
        if (currentAccountId == null || currentAccountId.isEmpty())
            throw new ApplicationException(StatusCode.NOT_PERMIT, "User not authenticated. Missing user context.");
        LOG.debug("Request to count unread notification");
        return notificationsRepository.countUnreadNotification(currentAccountId);
    }

    /**
     * View details a notification, system will redirect to actionUrl if any.
     *
     * @param id the id of the entity.
     * @return the actionUrl + message .
     */
    public ViewNotificationDTO viewNotification(String id) {
        String currentAccountId = UserContextHolder.getContext().getUserIdString();
        if (currentAccountId == null || currentAccountId.isEmpty())
            throw new ApplicationException(StatusCode.NOT_PERMIT, "User not authenticated. Missing user context.");
        LOG.debug("Request to view a notification : {}", id);
        Notification notify = notificationsRepository.findById(id).orElseThrow(() -> new CustomException(ResponseCode.CODE.NOT_FOUND, "Notification not found with id: " + id));
        if (!currentAccountId.equals(notify.getReceiveId()))
            throw new CustomException(ResponseCode.CODE.INVALID_INPUT_DATA, "User not authorized to mark this notification as read: " + notify.getNotificationId());
        notify.isViewed(1);
        String actionUrl = notify.getActionUrl().isEmpty() ? null : notify.getActionUrl();
        String message = actionUrl == null ? "No action URL available for this notification" : "Get action URL successfully";

        notificationsRepository.save(notify);
        return ViewNotificationDTO.builder()
            .actionUrl(actionUrl)
            .message(message)
            .build();
    }

    public List<NotificationDTO> markAsRead(List<String> ids) {
        CustomUserPrincipal userContext = UserContextHolder.getContext();
        validateUserNotification(userContext, ids);
        String currentAccountId = userContext.getUserIdString();
        List<Notification> notificationsList = notificationsRepository.findAllById(ids);
        if (notificationsList.isEmpty())
            throw new CustomException(ResponseCode.CODE.NOT_FOUND, "No notifications found for the provided IDs");
        for (Notification notify : notificationsList) {
            if (!currentAccountId.equals(notify.getReceiveId()))
                throw new CustomException(ResponseCode.CODE.INVALID_INPUT_DATA, "User not authorized to mark this notification as read: " + notify.getNotificationId());

            notify.setIsViewed(1);
        }
        notificationsRepository.saveAll(notificationsList);
        return notificationsMapper.toDto(notificationsList);
    }


    public List<NotificationDTO> markAsClicked(List<String> ids) {
        CustomUserPrincipal userContext = UserContextHolder.getContext();
        validateUserNotification(userContext, ids);
        String currentAccountId = userContext.getUserIdString();
        List<Notification> notificationsList = notificationsRepository.findAllById(ids);
        if (notificationsList.isEmpty())
            throw new CustomException(ResponseCode.CODE.NOT_FOUND, "No notifications found for the provided IDs");
        for (Notification notify : notificationsList) {
            if (!currentAccountId.equals(notify.getReceiveId()))
                throw new CustomException(ResponseCode.CODE.INVALID_INPUT_DATA, "User not authorized to mark this notification as read: " + notify.getNotificationId());

            notify.setIsClicked(1);
        }
        notificationsRepository.saveAll(notificationsList);
        return notificationsMapper.toDto(notificationsList);
    }

    /**
     * Update a notifications.
     *
     * @param notificationsDTO the entity to save.
     * @return the persisted entity.
     */
    public NotificationDTO update(NotificationDTO notificationsDTO) {
        LOG.debug("Request to update Notification : {}", notificationsDTO);
        Notification
            notifications = notificationsMapper.toEntity(notificationsDTO);
        notifications.setIsPersisted();
        notifications = notificationsRepository.save(notifications);
        return notificationsMapper.toDto(notifications);
    }

    /**
     * Partially update a notifications.
     *
     * @param notificationsDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<NotificationDTO> partialUpdate(NotificationDTO notificationsDTO) {
        LOG.debug("Request to partially update Notification : {}", notificationsDTO);

        return notificationsRepository
            .findById(notificationsDTO.getNotificationId())
            .map(existingNotification -> {
                notificationsMapper.partialUpdate(existingNotification, notificationsDTO);

                return existingNotification;
            })
            .map(notificationsRepository::save)
            .map(notificationsMapper::toDto);
    }

    /**
     * Get one notifications by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional
    public Optional<NotificationDTO> findOne(String id) {
        LOG.debug("Request to get Notification : {}", id);
        return notificationsRepository.findById(id).map(notificationsMapper::toDto);
    }

    /**
     * Delete the notifications by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete Notification : {}", id);
        Notification notify = notificationsRepository.findById(id).orElseThrow(() -> new CustomException(ResponseCode.CODE.NOT_FOUND, "Notification not found with id: " + id));
        notify.setIsActive(0);
        notificationsRepository.save(notify);
    }

    private void validateUserNotification(CustomUserPrincipal userContext, List<String> ids) {
        String currentAccountId = UserContextHolder.getContext().getUserIdString();
        LOG.debug("Request to mark notifications as clicked by user: {}, ids: {}", currentAccountId, ids);
        if (currentAccountId == null || currentAccountId.isEmpty())
            throw new ApplicationException(StatusCode.NOT_PERMIT, "User not authenticated. Missing user context.");
        if (ids == null || ids.isEmpty())
            throw new CustomException(ResponseCode.CODE.NOT_FOUND, "No notification IDs provided");

    }

    /**
     * Delete batch notifications by ids.
     *
     * @param ids the ids of the entity.
     *
     */
    public DeleteNotificationResponseDTO deleteBatch(List<String> ids, HttpServletRequest request) {
        CustomUserPrincipal userContext = UserContextHolder.getContext();
        String currentAccountId = userContext.getUserIdString();
        String currentOrgId = userContext.getShopId();
        String currentUserName = userContext.getUsername();
        String ipAddress = CommonHttpRequestUtil.getClientIpAddress(request);
        String userAgent = CommonHttpRequestUtil.getUserAgent(request);

        LOG.debug("Request to delete batch Notification : {}", ids);
        if (currentAccountId == null || currentAccountId.isEmpty())
            throw new ApplicationException(StatusCode.NOT_PERMIT, "User not authenticated. Missing user context.");
        List<Notification> notifyList = notificationsRepository.findAllById(ids);
        if (notifyList.isEmpty())
            throw new CustomException(ResponseCode.CODE.NOT_FOUND, "No notifications found for the provided IDs");
        for (Notification notify : notifyList) {
            if (!currentAccountId.equals(notify.getReceiveId()))
                throw new CustomException(ResponseCode.CODE.INVALID_INPUT_DATA, "User not authorized to delete this notification: " + notify.getNotificationId());
            notify.setIsActive(0);
        }

        notificationsRepository.saveAll(notifyList);

        try {
            String beforeData = convertToJson(notifyList);
            String deletedIds = String.join(" ,", ids);

            loggingService.createLogAction(
                Constant.ACTION_TYPE_DELETE,        // actionType
                beforeData,                               // oldValue (null for CREATE)
                null,                          // newValue
                "Xóa thông báo",       // description
                Constant.TABLE_NAME_NOTIFICATIONS,       // tableName
                deletedIds,                   // objectId
                currentAccountId,                   // accountId
                currentUserName,                    // userName
                currentOrgId,                       // organizationId
                ipAddress,                          // ipAddress
                userAgent                           // userAgent
            );
        } catch (Exception logEx) {
            LOG.error("Failed to create log action for Learning {}: {}", notifyList, logEx.getMessage());
        }
        return DeleteNotificationResponseDTO.builder()
            .success(true)
            .message("Xóa " + notifyList.size() + " thông báo thành công")
            .deletedCount(notifyList.size())
            .build();

    }

    private void validateNotificationOnCreate(NotificationDTO notificationsDTO) {

        if (notificationsDTO.getNotificationType() == null || notificationsDTO.getNotificationType().isEmpty()) {
            throw new CustomException(ResponseCode.CODE.INVALID_INPUT_DATA, "Notification type is required");
        }
        if (notificationsDTO.getTableName() == null || notificationsDTO.getTableName().isEmpty()) {
            throw new CustomException(ResponseCode.CODE.INVALID_INPUT_DATA, "Table name is required");
        }
    }

    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOG.error("Error converting to JSON: {}", e.getMessage());
            return null;
        }
    }

    public List<NotificationDTO> createListNotification(List<NotificationDTO> notificationsDTOList) {
        LOG.debug("Request to create list Notification : {}", notificationsDTOList);
        List<Notification> notificationsList = notificationsDTOList.stream()
            .filter(this::isDuplicateNotification)
            .map(notificationsMapper::toEntity)
            .toList();

        notificationsRepository.saveAll(notificationsList);
        return notificationsMapper.toDto(notificationsList);
    }

    private boolean isDuplicateNotification(NotificationDTO notificationsDTO) {
        long count = notificationsRepository.countByTableNameAndObjectIdAndNotificationTypeAndReceiveIdAndEventId(
            notificationsDTO.getTableName(),
            notificationsDTO.getObjectId(),
            notificationsDTO.getNotificationType(),
            notificationsDTO.getReceiveId(),
            notificationsDTO.getEventId()
        );
        return count == 0;
    }
}
