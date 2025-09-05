package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "permission")
@AttributeOverride(name = "id", column = @Column(name = "permission_name", nullable = false))
public class Permission extends AbstractEntity {

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL)
    private List<RoleHasPermission> rolePermissions = new ArrayList<>();
}
