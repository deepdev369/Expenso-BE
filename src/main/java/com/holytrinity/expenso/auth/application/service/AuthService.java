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
import org.springframework.beans.factory.annotation.Value;
import com.holytrinity.expenso.auth.application.dto.GoogleOAuthRequest;
import com.holytrinity.expenso.auth.application.dto.PhoneAuthRequest;
import com.holytrinity.expenso.auth.application.dto.PhoneVerifyRequest;
import com.holytrinity.expenso.auth.application.dto.SignupRequest;

@Service
@RequiredArgsConstructor
public class AuthService {

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

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // TODO: Replace in-memory cache with Redis for distributed environments
    private final Map<String, String> otpCache = new ConcurrentHashMap<>();

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

    public AuthResponse register(SignupRequest request) {
        String userName = (request.getUserName() == null || request.getUserName().trim().isEmpty()) 
            ? "Expenso User" 
            : request.getUserName();

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(request.getEmail());
        userDTO.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
        userDTO.setUserName(userName);
        userDTO.setAuthProviders(Collections.singletonList("LOCAL"));
        userDTO.setEmailVerified(false);
        userDTO.setDefaultCurrency("USD");
        userDTO.setLanguage("en");
        userDTO.setSmsConsentGranted(false);
        userDTO.setVoiceConsentGranted(false);
        
        userUseCase.createUserForRegistration(userDTO);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return buildAuthResponse(userDetails, user.getUserId(), true);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return buildAuthResponse(userDetails, user.getUserId(), false);
    }

    public AuthResponse authenticateGoogle(GoogleOAuthRequest request) {
        OAuthProviderPort provider = oauthProviders.stream()
                .filter(p -> p.getProviderName().equals("GOOGLE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Google OAuth provider not configured"));
                
        OAuthProviderPort.OAuthIdentity identity = provider.extractAndVerify(request.getIdToken());
        
        Optional<UserOAuthProvider> existingProvider = userOAuthProviderRepository
                .findByProviderNameAndProviderSubjectId("GOOGLE", identity.getSubjectId());
                
        boolean isNewUser = false;
        User user;
        if (existingProvider.isPresent()) {
            user = userRepository.findById(existingProvider.get().getUserId()).orElseThrow();
        } else {
            // Check if user exists by email to link account, else create new
            Optional<User> existingUserByEmail = userRepository.findByEmail(identity.getEmail());
            if (existingUserByEmail.isPresent()) {
                user = existingUserByEmail.get();
                if (!user.getAuthProviders().contains("GOOGLE")) {
                    user.getAuthProviders().add("GOOGLE");
                    userRepository.save(user);
                }
            } else {
                UserDTO newUser = new UserDTO();
                newUser.setEmail(identity.getEmail());
                newUser.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                newUser.setUserName(identity.getName());
                newUser.setAuthProviders(Collections.singletonList("GOOGLE"));
                newUser.setEmailVerified(true);
                newUser.setDefaultCurrency("USD");
                newUser.setLanguage("en");
                newUser.setSmsConsentGranted(false);
                newUser.setVoiceConsentGranted(false);
                userUseCase.createUserForRegistration(newUser);
                user = userRepository.findByEmail(identity.getEmail()).orElseThrow();
                isNewUser = true;
            }
            
            UserOAuthProvider newProvider = new UserOAuthProvider();
            newProvider.setUserId(user.getUserId());
            newProvider.setProviderName("GOOGLE");
            newProvider.setProviderSubjectId(identity.getSubjectId());
            userOAuthProviderRepository.save(newProvider);
        }
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return buildAuthResponse(userDetails, user.getUserId(), isNewUser);
    }
    
    public AuthResponse refreshToken(RefreshRequest request) {
        String hashedToken = hashToken(request.getRefreshToken());
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
                
        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }
        
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        
        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return buildAuthResponse(userDetails, user.getUserId(), false);
    }

    public void sendPhoneOtp(PhoneAuthRequest request) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpCache.put(request.getPhoneNumber(), otp);
        
        smsProviderPort.sendSms(request.getPhoneNumber(), "Your Expenso OTP is: " + otp);
    }

    public AuthResponse verifyPhoneOtp(PhoneVerifyRequest request) {
        String cachedOtp = otpCache.get(request.getPhoneNumber());
        if (cachedOtp == null || !cachedOtp.equals(request.getOtp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        otpCache.remove(request.getPhoneNumber());

        // We use a deterministic placeholder email since the User entity requires a unique non-null email.
        // Users can later update this to their real email in the profile settings if desired.
        String mockEmail = request.getPhoneNumber() + "@phone.expenso.local";
        
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(mockEmail);
            User user = userRepository.findByEmail(mockEmail).orElseThrow();
            return buildAuthResponse(userDetails, user.getUserId(), false);
        } catch (Exception e) {
            UserDTO newUser = new UserDTO();
            newUser.setEmail(mockEmail);
            newUser.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            newUser.setUserName("Phone User");
            newUser.setAuthProviders(Collections.singletonList("PHONE"));
            newUser.setEmailVerified(false);
            newUser.setDefaultCurrency("USD");
            newUser.setLanguage("en");
            newUser.setSmsConsentGranted(false);
            newUser.setVoiceConsentGranted(false);
            userUseCase.createUserForRegistration(newUser);

            // Fetch the user to update the phone number specifically since UserDTO might not have phone mapped yet
            User user = userRepository.findByEmail(mockEmail).orElseThrow();
            user.setPhone(request.getPhoneNumber());
            userRepository.save(user);

            UserDetails userDetails = userDetailsService.loadUserByUsername(mockEmail);
            return buildAuthResponse(userDetails, user.getUserId(), true);
        }
    }
}
