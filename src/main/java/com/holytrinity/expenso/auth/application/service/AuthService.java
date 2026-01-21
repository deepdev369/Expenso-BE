package com.holytrinity.expenso.auth.application.service;

import com.holytrinity.expenso.auth.application.dto.AuthRequest;
import com.holytrinity.expenso.auth.application.dto.AuthResponse;
import com.holytrinity.expenso.security.JwtUtils;
import com.holytrinity.expenso.user.application.dto.UserDTO;
import com.holytrinity.expenso.user.application.port.in.UserUseCase;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserUseCase userUseCase;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(UserDTO request) {
        request.setPasswordHash(passwordEncoder.encode(request.getPasswordHash())); // Using passwordHash field for
                                                                                    // password
        request.setAuthProviders(Collections.singletonList("LOCAL"));
        userUseCase.createUser(request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String jwtToken = jwtUtils.generateToken(userDetails);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String jwtToken = jwtUtils.generateToken(userDetails);
        return AuthResponse.builder().token(jwtToken).build();
    }
}
