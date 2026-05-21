package com.aiagent.gateway.user.domain;

import com.aiagent.gateway.common.entity.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_tenant_email",
                columnNames = {"tenant_id", "email"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends TenantBaseEntity {

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Builder
    public User(String email, String passwordHash, String name, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
}