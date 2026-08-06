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
import com.holytrinity.expenso.user.domain.User;
import com.holytrinity.expenso.user.domain.UserOAuthProvider;
import com.holytrinity.expenso.user.adapter.out.persistence.SpringDataUserRepository;
import com.holytrinity.expenso.user.adapter.out.persistence.SpringDataUserOAuthProviderRepository;
import com.holytrinity.expenso.auth.adapter.out.persistence.SpringDataRefreshTokenRepository;
import com.holytrinity.expenso.auth.domain.RefreshToken;
import com.holytrinity.expenso.auth.application.port.out.OAuthProviderPort;
import com.holytrinity.expenso.auth.application.port.out.SmsProviderPort;
import com.holytrinity.expenso.auth.application.dto.RefreshRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Value;
import com.holytrinity.expenso.auth.application.dto.GoogleOAuthRequest;
import com.holytrinity.expenso.auth.application.dto.PhoneAuthRequest;
import com.holytrinity.expenso.auth.application.dto.PhoneVerifyRequest;
import com.holytrinity.expenso.auth.application.dto.SignupRequest;
import com.holytrinity.expenso.auth.application.dto.EmailAuthRequest;
import com.holytrinity.expenso.auth.application.dto.EmailVerifyRequest;
import com.holytrinity.expenso.auth.application.dto.EmailCheckResponse;
import com.holytrinity.expenso.auth.application.port.out.EmailProviderPort;
import com.holytrinity.expenso.auth.application.port.in.AuthUseCase;
import org.springframework.security.core.Authentication;
import com.holytrinity.expenso.security.SecurityUser;
import com.holytrinity.expenso.shared.exception.UnauthorizedException;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserUseCase userUseCase;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final SpringDataUserRepository userRepository;

    private final SpringDataUserOAuthProviderRepository userOAuthProviderRepository;
    private final SpringDataRefreshTokenRepository refreshTokenRepository;
    private final List<OAuthProviderPort> oauthProviders;
    private final SmsProviderPort smsProviderPort;
    private final EmailProviderPort emailProviderPort;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // OTP store with expiry — each entry holds the code and its expiry epoch
    // millis.
    // TODO: Replace with Redis for multi-instance / persistent deployments.
    private record OtpEntry(String code, long expiresAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();
    private static final long OTP_TTL_MS = 5 * 60 * 1000L; // 5 minutes
    private final SecureRandom secureRandom = new SecureRandom();

    private void cleanExpiredOtps() {
        otpCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private AuthResponse buildAuthResponse(UserDetails userDetails, String userId, boolean isNewUser) {
        String jwtToken = jwtUtils.generateToken(userDetails);

        String rawRefreshToken = java.util.UUID.randomUUID().toString();
        String hashedToken = hashToken(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hashedToken);
        refreshToken.setUserId(userId);
        refreshToken.setExpiresAt(System.currentTimeMillis() + refreshExpirationMs);
        refreshTokenRepository.save(refreshToken);

        UserDTO userDTO = userUseCase.getUser(userId);

        AuthResponse response = new AuthResponse();
        response.setToken(jwtToken);
        response.setRefreshToken(rawRefreshToken); // Send raw token to client
        response.setExpiresIn(jwtExpirationMs / 1000);
        response.setNewUser(isNewUser);
        response.setUser(userDTO);
        return response;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not hash token", e);
        }
    }

    @Override
    public AuthResponse register(SignupRequest request) {
        String userName = (request.getUserName() == null || request.getUserName().trim().isEmpty())
                ? "Expenso User"
                : request.getUserName();

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getAuthProviders().contains("LOCAL")) {
                throw new RuntimeException("Email already registered. Please log in.");
            } else {
                // Link account by adding LOCAL provider and setting password
                user.getAuthProviders().add("LOCAL");
                user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
                userRepository.save(user);

                UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
                return buildAuthResponse(userDetails, user.getUserId(), false);
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(request.getEmail());
        userDTO.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
        userDTO.setUserName(userName);
        userDTO.setAuthProviders(Collections.singletonList("LOCAL"));
        userDTO.setEmailVerified(false);
        userDTO.setProfileCompleted(false);
        userDTO.setDefaultCurrency("USD");
        userDTO.setLanguage("en");
        userDTO.setSmsConsentGranted(false);
        userDTO.setVoiceConsentGranted(false);

        userUseCase.createUserForRegistration(userDTO);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return buildAuthResponse(userDetails, user.getUserId(), true);
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        cleanExpiredOtps();
        String email = request.getEmail().trim().toLowerCase();
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
        return buildAuthResponse(securityUser, securityUser.getUserId(), false);
    }

    @Override
    public AuthResponse authenticateGoogle(GoogleOAuthRequest request) {
        cleanExpiredOtps();
        OAuthProviderPort provider = oauthProviders.stream()
                .filter(p -> p.getProviderName().equals("GOOGLE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Google OAuth provider not configured"));

        OAuthProviderPort.OAuthIdentity identity = provider.extractAndVerify(request.getIdToken());
        String email = identity.getEmail().trim().toLowerCase();

        Optional<UserOAuthProvider> existingProvider = userOAuthProviderRepository
                .findByProviderNameAndProviderSubjectId("GOOGLE", identity.getSubjectId());

        boolean isNewUser = false;
        User user;
        if (existingProvider.isPresent()) {
            user = userRepository.findById(existingProvider.get().getUserId()).orElseThrow();
        } else {
            // Check if user exists by email to link account, else create new
            Optional<User> existingUserByEmail = userRepository.findByEmail(email);
            if (existingUserByEmail.isPresent()) {
                user = existingUserByEmail.get();
                if (!user.getAuthProviders().contains("GOOGLE")) {
                    user.getAuthProviders().add("GOOGLE");
                    userRepository.save(user);
                }
            } else {
                UserDTO newUser = new UserDTO();
                newUser.setEmail(email);
                newUser.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                newUser.setUserName(identity.getName());
                newUser.setAuthProviders(Collections.singletonList("GOOGLE"));
                newUser.setEmailVerified(true);
                newUser.setDefaultCurrency("USD");
                newUser.setLanguage("en");
                newUser.setSmsConsentGranted(false);
                newUser.setVoiceConsentGranted(false);
                userUseCase.createUserForRegistration(newUser);
                user = userRepository.findByEmail(email).orElseThrow();
                isNewUser = true;
            }

            UserOAuthProvider newProvider = new UserOAuthProvider();
            newProvider.setUserId(user.getUserId());
            newProvider.setProviderName("GOOGLE");
            newProvider.setProviderSubjectId(identity.getSubjectId());
            userOAuthProviderRepository.save(newProvider);
        }

        return buildAuthResponse(new SecurityUser(user), user.getUserId(), isNewUser);
    }

    @Override
    public AuthResponse refreshToken(RefreshRequest request) {
        cleanExpiredOtps();
        String hashedToken = hashToken(request.getRefreshToken());
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return buildAuthResponse(new SecurityUser(user), user.getUserId(), false);
    }

    @Override
    public void sendPhoneOtp(PhoneAuthRequest request) {
        cleanExpiredOtps();
        // SecureRandom — cryptographically safe, unlike java.util.Random
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpCache.put(request.getPhoneNumber(), new OtpEntry(otp, System.currentTimeMillis() + OTP_TTL_MS));
        smsProviderPort.sendSms(request.getPhoneNumber(), "Your Expenso OTP is: " + otp + ". Valid for 5 minutes.");
    }

    @Override
    public AuthResponse verifyPhoneOtp(PhoneVerifyRequest request) {
        cleanExpiredOtps();
        OtpEntry entry = otpCache.get(request.getPhoneNumber());
        if (entry == null) {
            throw new RuntimeException("OTP not found. Please request a new OTP.");
        }
        if (entry.isExpired()) {
            otpCache.remove(request.getPhoneNumber());
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }
        if (!entry.code().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP.");
        }
        otpCache.remove(request.getPhoneNumber());

        // We use a deterministic placeholder email since the User entity requires a
        // unique non-null email.
        // Users can later update this to their real email in the profile settings if
        // desired.
        String mockEmail = request.getPhoneNumber() + "@phone.expenso.local";

        try {
            User user = userRepository.findByEmail(mockEmail).orElseThrow();
            return buildAuthResponse(new SecurityUser(user), user.getUserId(), false);
        } catch (Exception e) {
            UserDTO newUser = new UserDTO();
            newUser.setEmail(mockEmail);
            newUser.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            newUser.setUserName("New User");
            // NOTE: emailVerified is false by default — a verification flow should be
            // implemented to allow phone users to link and verify a real email address.
            newUser.setAuthProviders(Collections.singletonList("PHONE"));
            newUser.setEmailVerified(false);
            newUser.setDefaultCurrency(null);
            newUser.setLanguage("en");
            newUser.setSmsConsentGranted(false);
            newUser.setVoiceConsentGranted(false);
            userUseCase.createUserForRegistration(newUser);

            // Fetch the user to update the phone number specifically since UserDTO might
            // not have phone mapped yet
            User user = userRepository.findByEmail(mockEmail).orElseThrow();
            user.setPhone(request.getPhoneNumber());
            userRepository.save(user);

            return buildAuthResponse(new SecurityUser(user), user.getUserId(), true);
        }
    }

    @Override
    public void sendEmailVerification(EmailAuthRequest request) {
        cleanExpiredOtps();
        String email = request.getEmail().trim().toLowerCase();
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpCache.put("EMAIL_" + email, new OtpEntry(otp, System.currentTimeMillis() + OTP_TTL_MS));

        String emailBody = "Welcome to Expenso!\n\n" +
                "Your email verification OTP is: " + otp + "\n" +
                "This OTP is valid for 5 minutes.\n\n" +
                "If you didn't request this, you can ignore this email.";

        emailProviderPort.sendEmail(email, "Expenso Email Verification", emailBody);
    }

    @Override
    public void verifyEmailOtp(EmailVerifyRequest request) {
        cleanExpiredOtps();
        String email = request.getEmail().trim().toLowerCase();
        String cacheKey = "EMAIL_" + email;
        OtpEntry entry = otpCache.get(cacheKey);

        if (entry == null) {
            throw new RuntimeException("OTP not found. Please request a new OTP.");
        }
        if (entry.isExpired()) {
            otpCache.remove(cacheKey);
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }
        if (!entry.code().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP.");
        }

        otpCache.remove(cacheKey);

        // Update user to verified
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email."));

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public EmailCheckResponse checkEmailExists(String email) {
        cleanExpiredOtps();
        String emailToSearch = email.trim().toLowerCase();
        Optional<User> user = userRepository.findByEmail(emailToSearch);
        if (user.isPresent()) {
            return new EmailCheckResponse(true, user.get().getAuthProviders());
        }
        return new EmailCheckResponse(false, Collections.emptyList());
    }
}
