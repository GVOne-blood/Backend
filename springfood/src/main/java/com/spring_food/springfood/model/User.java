package com.spring_food.springfood.model;

import com.spring_food.springfood.model.ENUM.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user") //user trung voi key trong PostgreSQL
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class User extends AbstractEntity implements UserDetails {

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(length = 15, unique = true)
    private String phone;

    private String address;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "avartar")
    private String avatar;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "last_login_at")
    private LocalDateTime LastLoginAt;

    @Column(name = "phone_verified", columnDefinition = "false")
    private boolean phoneVerified;

    @Column(name = "email_verified", columnDefinition = "false")
    private boolean emailVerified;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserHasRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToOne(mappedBy = "owner")
    private Shop shop;

    // Gán giá trị mặc định trước khi lưu vào database
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (!phoneVerified) {
            phoneVerified = false;
        }
        if (!emailVerified) {
            emailVerified = false;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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
