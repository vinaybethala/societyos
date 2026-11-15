package com.societyos.security;

import com.societyos.entity.User;
import com.societyos.entity.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final String fullName;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public UserPrincipal(Long id, String username, String email, String password, String fullName,
                         Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static UserPrincipal create(User user) {
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .fullName(user.getFullName())
                .authorities(authorities)
                .enabled(user.getStatus() == UserStatus.ACTIVE)
                .build();
    }

    public static UserPrincipalBuilder builder() {
        return new UserPrincipalBuilder();
    }

    public static class UserPrincipalBuilder {
        private Long id;
        private String username;
        private String email;
        private String password;
        private String fullName;
        private Collection<? extends GrantedAuthority> authorities;
        private boolean enabled;

        public UserPrincipalBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserPrincipalBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserPrincipalBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserPrincipalBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserPrincipalBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserPrincipalBuilder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public UserPrincipalBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public UserPrincipal build() {
            return new UserPrincipal(id, username, email, password, fullName, authorities, enabled);
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
