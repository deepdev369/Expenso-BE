package com.holytrinity.expenso.security;

import com.holytrinity.expenso.user.application.dto.UserDTO;
import com.holytrinity.expenso.user.application.port.in.UserUseCase;
import com.holytrinity.expenso.user.application.port.out.UserPort;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserUseCase userUseCase;
    private final UserPort userPort;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 Success for email: {}", email);

        // Check if user exists, if not create
        if (!userPort.existsByEmail(email)) {
            UserDTO newUser = new UserDTO();
            newUser.setEmail(email);
            newUser.setUserName(name);
            newUser.setEmailVerified(true); // Assumed verified from OAuth
            newUser.setAuthProviders(Collections.singletonList("GOOGLE"));
            newUser.setDefaultCurrency("USD"); // Default
            newUser.setLanguage("en"); // Default
            newUser.setSmsConsentGranted(false);
            newUser.setVoiceConsentGranted(false);
            userUseCase.createUser(newUser);
        }

        // Generate Token
        // We need to load user details for token generation
        // Simulating UserDetails for now since we have the email
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                email, "", Collections.singletonList(() -> "USER")); // dummy authorities for now

        String token = jwtUtils.generateToken(userDetails);

        // Redirect to frontend with token
        // In a real app, this might redirect to a specific frontend URL
        // For now, let's just write it to response or redirect to a dummy endpoint
        // Assuming frontend is at localhost:3000
        response.sendRedirect("http://localhost:3000/oauth2/redirect?token=" + token);
    }
}
