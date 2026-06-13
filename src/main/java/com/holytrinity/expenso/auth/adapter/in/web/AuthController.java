package com.holytrinity.expenso.auth.adapter.in.web;

import com.holytrinity.expenso.auth.application.dto.AuthRequest;
import com.holytrinity.expenso.auth.application.dto.AuthResponse;
import com.holytrinity.expenso.auth.application.service.AuthService;
import com.holytrinity.expenso.auth.application.dto.GoogleOAuthRequest;
import com.holytrinity.expenso.auth.application.dto.PhoneAuthRequest;
import com.holytrinity.expenso.auth.application.dto.PhoneVerifyRequest;
import com.holytrinity.expenso.auth.application.dto.RefreshRequest;
import com.holytrinity.expenso.auth.application.dto.SignupRequest;
import com.holytrinity.expenso.auth.application.dto.EmailAuthRequest;
import com.holytrinity.expenso.auth.application.dto.EmailVerifyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid SignupRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> authenticateGoogle(@RequestBody @Valid GoogleOAuthRequest request) {
        return ResponseEntity.ok(authService.authenticateGoogle(request));
    }

    @PostMapping("/phone/send-otp")
    public ResponseEntity<Void> sendPhoneOtp(@RequestBody @Valid PhoneAuthRequest request) {
        authService.sendPhoneOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/phone/verify-otp")
    public ResponseEntity<AuthResponse> verifyPhoneOtp(@RequestBody @Valid PhoneVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyPhoneOtp(request));
    }

    @PostMapping("/email/send-verification")
    public ResponseEntity<Void> sendEmailVerification(@RequestBody @Valid EmailAuthRequest request) {
        authService.sendEmailVerification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmailOtp(@RequestBody @Valid EmailVerifyRequest request) {
        authService.verifyEmailOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
