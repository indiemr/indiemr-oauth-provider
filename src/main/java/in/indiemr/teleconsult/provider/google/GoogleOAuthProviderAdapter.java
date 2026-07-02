package in.indiemr.teleconsult.provider.google;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Base64;
import in.indiemr.teleconsult.config.GoogleProperties;
import in.indiemr.teleconsult.model.OAuthAccount;
import in.indiemr.teleconsult.provider.OAuthProviderAdapter;
import in.indiemr.teleconsult.provider.dto.OAuthToken;
import in.indiemr.teleconsult.provider.dto.OAuthUser;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuthProviderAdapter implements OAuthProviderAdapter {

    public static final String CODE = "GOOGLE";

    private static final List<String> SCOPES = List.of(
        "openid", "email", "https://www.googleapis.com/auth/calendar.events");

    private final GoogleProperties props;

    public GoogleOAuthProviderAdapter(GoogleProperties props) {
        this.props = props;
    }

    @Override
    public String getProviderCode() { return CODE; }

    @Override
    public String buildAuthorizationUrl(String state) throws Exception {
        return flow().newAuthorizationUrl()
            .setRedirectUri(props.getRedirectUri())
            .setState(state)
            .build();
    }

    @Override
    public OAuthToken exchangeAuthorizationCode(String code) throws Exception {
        TokenResponse r = flow().newTokenRequest(code)
            .setRedirectUri(props.getRedirectUri())
            .execute();
        if (r.getRefreshToken() == null) {
            throw new IllegalStateException(
                "Google did not return a refresh token — revoke prior access and reconnect.");
        }
        Instant expires = r.getExpiresInSeconds() != null
            ? Instant.now().plusSeconds(r.getExpiresInSeconds())
            : null;
        return new OAuthToken(
            r.getAccessToken(),
            r.getRefreshToken(),
            r.get("id_token") != null ? String.valueOf(r.get("id_token")) : null,
            r.getScope(),
            expires
        );
    }

    @Override
    public OAuthUser getCurrentUser(OAuthAccount account, String decryptedAccessToken) {
        return new OAuthUser(
            account.getExternalAccountId(),
            account.getExternalEmail(),
            account.getDisplayName()
        );
    }

    @Override
    public void revoke(OAuthAccount account, String decryptedRefreshToken) {
        // optional: call https://oauth2.googleapis.com/revoke
    }

    private GoogleAuthorizationCodeFlow flow() throws Exception {
        GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
        web.setClientId(props.getClientId());
        web.setClientSecret(props.getClientSecret());
        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new GoogleClientSecrets().setWeb(web),
                SCOPES)
            .setAccessType("offline")
            .setApprovalPrompt("force")
            .build();
    }

    public static String emailFromIdToken(String idToken) {
        try {
            String payload = idToken.split("\\.")[1];
            String json = new String(Base64.decodeBase64(payload), StandardCharsets.UTF_8);
            int idx = json.indexOf("\"email\"");
            if (idx < 0) return null;
            int start = json.indexOf('"', idx + 8) + 1;
            int end = json.indexOf('"', start);
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
}