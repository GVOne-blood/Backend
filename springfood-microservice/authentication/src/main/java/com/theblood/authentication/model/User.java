package com.theblood.authentication.model;

import com.theblood.authentication.common.Gender;
import com.theblood.authentication.common.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "\"user\"") // Quoted to avoid SQL keyword conflict
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class User extends AbstractEntity implements UserDetails {

    @Column(unique = true)
    private String username;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(length = 20, unique = true)
    private String phone;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "shopId")
    private String shopId;
    
    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    // Legacy address column from monolithic DB
    private String address;


    // =================================================================
    // RELATIONSHIPS
    // =================================================================

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserHasRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Token> tokens = new HashSet<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        this.userRoles.forEach(userRole -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getName()));

            userRole.getRole().getRolePermissions().forEach(rolePermission -> {
                authorities.add(new SimpleGrantedAuthority(rolePermission.getPermission().getName()));
            });
        });

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
