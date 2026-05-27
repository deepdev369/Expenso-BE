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
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @org.springframework.beans.factory.annotation.Value("${app.oauth2.redirect-url}")
    private String redirectUrl;

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
            newUser.setUserId(java.util.UUID.randomUUID().toString());
            newUser.setEmail(email);
            newUser.setUserName(name);
            newUser.setEmailVerified(true); // Assumed verified from OAuth
            newUser.setAuthProviders(Collections.singletonList("GOOGLE"));
            newUser.setDefaultCurrency("USD"); // Default
            newUser.setLanguage("en"); // Default
            newUser.setSmsConsentGranted(false);
            newUser.setVoiceConsentGranted(false);
            userUseCase.syncBulk(Collections.singletonList(newUser));
        }

        // Generate Token
        // Load genuine UserDetails from the database
        org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String token = jwtUtils.generateToken(userDetails);

        // Redirect to the configured frontend URL with the token
        response.sendRedirect(redirectUrl + "?token=" + token);
    }
}
