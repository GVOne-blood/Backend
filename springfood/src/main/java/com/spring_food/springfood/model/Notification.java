package com.spring_food.springfood.model;


import com.spring_food.springfood.common.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification")
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(generator = "objectid-generator")
    @GenericGenerator(name = "objectid-generator",
            strategy = "com.spring_food.springfood.common.util.ObjectIdGeneratorUtil")
    @Column(name = "notification_id")
    private String notificationId;

    @Column(name = "notify_type")
    @Enumerated(EnumType.STRING)
    private NotificationType notifyType;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "link", columnDefinition = "TEXT")
    private String link;

    @Column(name = "is_read")
    private boolean isRead = false;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
