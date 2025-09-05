package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_has_permission")
@Getter
@Setter
@IdClass(RoleHasPermission.RolePermissionId.class)
@EntityListeners(AuditingEntityListener.class)
public class RoleHasPermission {
    @Id
    @ManyToOne
    @JoinColumn(name = "role_name", referencedColumnName = "role_name")
    private Role role;

    @Id
    @ManyToOne
    @JoinColumn(name = "permission_name", referencedColumnName = "permission_name")
    private Permission permission;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Getter
    @Setter
    public static class RolePermissionId implements Serializable {
        private String role;
        private String permission;
    }
}
