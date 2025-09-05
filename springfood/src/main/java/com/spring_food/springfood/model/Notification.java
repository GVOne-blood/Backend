package com.spring_food.springfood.model;


import com.spring_food.springfood.model.ENUM.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(generator = "objectid-generator")
    private String notificationId;

    @Column(name = "notify_type")
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
}
