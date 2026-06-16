package com.holytrinity.expenso.auth.adapter.out.provider;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.holytrinity.expenso.auth.application.port.out.OAuthProviderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleOAuthAdapter implements OAuthProviderPort {

    private final GoogleIdTokenVerifier verifier;

    public GoogleOAuthAdapter(@Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId) {
        googleClientId = googleClientId != null ? googleClientId.trim() : null;
        if (googleClientId == null || googleClientId.isEmpty() || "YOUR_GOOGLE_CLIENT_ID".equals(googleClientId)) {
            // For testing environments without real keys, we might bypass strict validation if absolutely necessary,
            // but for a production SaaS, failing fast is best.
            throw new RuntimeException("Google Client ID is strictly required for OAuth validation.");
        }
        
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

  @Override
  public OAuthIdentity extractAndVerify(String token) {
        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                
                String subjectId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                if (name == null || name.isEmpty()) {
                    name = "Google User";
                }
                String pictureUrl = (String) payload.get("picture");

                return OAuthIdentity.builder()
                        .subjectId(subjectId)
                        .email(email)
                        .name(name)
                        .pictureUrl(pictureUrl)
                        .build();
            } else {
                throw new RuntimeException("Invalid Google ID token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "GOOGLE";
    }
}
