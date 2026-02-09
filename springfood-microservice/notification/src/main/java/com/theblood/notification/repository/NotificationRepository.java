package com.theblood.notification.repository;

import com.theblood.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Notification entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String>, JpaSpecificationExecutor<Notification> {

    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.receiveId = :receiveId
          AND n.isActive = :isActive
        ORDER BY n.createdDate DESC
        """)
    Page<Notification> findByReceiveIdAndIsActiveOrderByCreatedDateDesc(Pageable pageable, @Param("receiveId") String receiveId, Integer isActive);

    @Query("""
            SELECT COUNT(n)
            FROM Notification n
            WHERE n.receiveId = :receiveId
            AND n.isActive = 1 AND n.isViewed = 0
        """)
    Long countUnreadNotification(@Param("receiveId") String currentAccountId);

    @Query("""
        SELECT COUNT(n)
        FROM Notification n
        WHERE n.tableName = :tableName
          AND n.objectId = :objectId
          AND n.notificationType = :notificationType
          AND n.receiveId = :receiveId
          AND n.eventId = :eventId
        """)
    long countByTableNameAndObjectIdAndNotificationTypeAndReceiveIdAndEventId(String tableName,
                                                                              String objectId,
                                                                              String notificationType,
                                                                              String receiveId,
                                                                              String eventId);


}
