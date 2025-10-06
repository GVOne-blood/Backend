package com.theblood.identityservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "permission")
@EntityListeners(AuditingEntityListener.class)
public class Permission {

    @Id
    @Column(name = "permission_name")
    private String name;

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL)
    private List<RoleHasPermission> rolePermissions = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}