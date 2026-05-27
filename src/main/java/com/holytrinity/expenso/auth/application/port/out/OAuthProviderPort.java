package com.holytrinity.expenso.auth.application.port.out;

import lombok.Builder;
import lombok.Getter;

public interface OAuthProviderPort {
    
    @Builder
    @Getter
    class OAuthIdentity {
        private String subjectId;
        private String email;
        private String name;
        private String pictureUrl;
    }

    /**
     * Verifies the OAuth provider's token and extracts the user's identity.
     * @param token the raw ID token or access token from the provider
     * @return the verified OAuth identity
     * @throws RuntimeException if verification fails
     */
    OAuthIdentity extractAndVerify(String token);
    
    /**
     * Identifies the provider this adapter supports (e.g. "GOOGLE").
     */
    String getProviderName();
}
