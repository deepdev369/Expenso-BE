package com.holytrinity.expenso.auth.application.port.in;

import com.holytrinity.expenso.auth.application.dto.AuthRequest;
import com.holytrinity.expenso.auth.application.dto.AuthResponse;
import com.holytrinity.expenso.auth.application.dto.EmailAuthRequest;
import com.holytrinity.expenso.auth.application.dto.EmailCheckResponse;
import com.holytrinity.expenso.auth.application.dto.EmailVerifyRequest;
import com.holytrinity.expenso.auth.application.dto.GoogleOAuthRequest;
import com.holytrinity.expenso.auth.application.dto.PhoneAuthRequest;
import com.holytrinity.expenso.auth.application.dto.PhoneVerifyRequest;
import com.holytrinity.expenso.auth.application.dto.RefreshRequest;
import com.holytrinity.expenso.auth.application.dto.SignupRequest;

public interface AuthUseCase {
    AuthResponse register(SignupRequest request);
    AuthResponse authenticate(AuthRequest request);
    AuthResponse authenticateGoogle(GoogleOAuthRequest request);
    void sendPhoneOtp(PhoneAuthRequest request);
    AuthResponse verifyPhoneOtp(PhoneVerifyRequest request);
    void sendEmailVerification(EmailAuthRequest request);
    void verifyEmailOtp(EmailVerifyRequest request);
    EmailCheckResponse checkEmailExists(String email);
    AuthResponse refreshToken(RefreshRequest request);
}
