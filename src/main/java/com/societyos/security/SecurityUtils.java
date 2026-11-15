package com.societyos.security;

import com.societyos.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Long getCurrentUserId() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
    }

    public static String getCurrentUsername() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::getUsername)
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
    }

    public static boolean hasRole(String role) {
        return getCurrentUserPrincipal()
                .map(p -> p.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role)))
                .orElse(false);
    }
}

