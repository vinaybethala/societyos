package com.societyos.entity;

import com.societyos.entity.enums.RoleType;
import com.societyos.entity.enums.UserStatus;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    public User(String username, String email, String passwordHash, String fullName, String phone, UserStatus status, Set<Role> roles) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.roles = roles != null ? roles : new HashSet<>();
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String username;
        private String email;
        private String passwordHash;
        private String fullName;
        private String phone;
        private UserStatus status = UserStatus.ACTIVE;
        private Set<Role> roles = new HashSet<>();

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public UserBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserBuilder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public UserBuilder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public User build() {
            return new User(username, email, passwordHash, fullName, phone, status, roles);
        }
    }

    public boolean hasRole(RoleType roleType) {
        return roles != null && roles.stream().anyMatch(r -> r.getName() == roleType);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
