package com.holytrinity.expenso.security;

import com.holytrinity.expenso.user.application.port.out.UserPort;
import com.holytrinity.expenso.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContext {

    private final UserPort userPort;

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        
        // 1. If it's our SecurityUser, return it directly
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getUserId();
        }

        // 2. Extract email/username based on principal type
        String email = null;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
        } else if (principal instanceof String str) {
            if ("anonymousUser".equals(str)) {
                throw new IllegalStateException("Anonymous user is not authorized");
            }
            email = str;
        }

        if (email != null && !email.isBlank()) {
            final String targetEmail = email;
            return userPort.loadUserByEmail(targetEmail)
                    .map(User::getUserId)
                    .orElseThrow(() -> new IllegalStateException("User not found with email: " + targetEmail));
        }

        throw new IllegalStateException("Principal is of unknown type and email could not be resolved");
    }

    public String getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getUserId();
        }

        String email = null;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
        } else if (principal instanceof String str) {
            if ("anonymousUser".equals(str)) {
                return null;
            }
            email = str;
        }

        if (email != null && !email.isBlank()) {
            final String targetEmail = email;
            return userPort.loadUserByEmail(targetEmail)
                    .map(User::getUserId)
                    .orElse(null);
        }

        return null;
    }
}
