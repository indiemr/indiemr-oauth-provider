package in.indiemr.teleconsult.service;

import in.indiemr.teleconsult.crypto.CryptoService;
import in.indiemr.teleconsult.dao.OAuthAccountDao;
import in.indiemr.teleconsult.dao.OAuthProviderDao;
import in.indiemr.teleconsult.dto.AccountStatusResponse;
import in.indiemr.teleconsult.dto.ConnectState;
import in.indiemr.teleconsult.model.OAuthAccount;
import in.indiemr.teleconsult.model.OAuthProvider;
import in.indiemr.teleconsult.provider.OAuthProviderAdapter;
import in.indiemr.teleconsult.provider.dto.OAuthToken;
import in.indiemr.teleconsult.provider.google.GoogleCalendarMeetingAdapter;
import in.indiemr.teleconsult.provider.google.GoogleOAuthProviderAdapter;
import in.indiemr.teleconsult.provider.registry.OAuthProviderRegistry;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class OAuthConnectService {

    private final GoogleCalendarMeetingAdapter googleCalendarMeetingAdapter;
    private static final List<String> GOOGLE_CAPABILITIES = List.of("CALENDAR", "VIDEO_MEETING", "EMAIL");
    private final OAuthProviderRegistry oauthRegistry;
    private final OAuthProviderDao oauthProviderDao;
    private final OAuthAccountDao oauthAccountDao;
    private final CryptoService crypto;

    public OAuthConnectService(OAuthProviderRegistry oauthRegistry,
                               OAuthProviderDao oauthProviderDao,
                               OAuthAccountDao oauthAccountDao,
                               CryptoService crypto, GoogleCalendarMeetingAdapter googleCalendarMeetingAdapter) {
        this.oauthRegistry = oauthRegistry;
        this.oauthProviderDao = oauthProviderDao;
        this.oauthAccountDao = oauthAccountDao;
        this.crypto = crypto;
        this.googleCalendarMeetingAdapter = googleCalendarMeetingAdapter;
    }

    public String buildConnectUrl(
        String providerUuid,
        String providerDisplay,
        String oauthProviderCode
    ) throws Exception {
        oauthProviderDao.findEnabledByCode(oauthProviderCode)
            .orElseThrow(() -> new IllegalArgumentException("Provider not enabled: " + oauthProviderCode));
        
        long t = System.currentTimeMillis();
        String signed = crypto.signState(
            Map.of(
                "providerUuid", providerUuid,
                "providerDisplay", providerDisplay,
                "oauthProviderCode", oauthProviderCode,
                "t", t
            )
        );

        OAuthProviderAdapter adapter = oauthRegistry.require(oauthProviderCode);
        return adapter.buildAuthorizationUrl(signed);
    }

    public ConnectResult handleCallback(String code, String state) throws Exception {
        ConnectState parsed = crypto.verifyState(state, ConnectState.class);
        String oauthProviderCode = parsed.getOauthProviderCode();

        OAuthProvider providerEntity = oauthProviderDao.findEnabledByCode(oauthProviderCode)
            .orElseThrow(() -> new IllegalStateException("Provider not enabled: " + oauthProviderCode));

        OAuthProviderAdapter adapter = oauthRegistry.require(oauthProviderCode);
        OAuthToken token = adapter.exchangeAuthorizationCode(code);

        OAuthAccount account = oauthAccountDao
            .findByProviderUuidAndProviderCode(parsed.getProviderUuid(), oauthProviderCode)
            .orElseGet(OAuthAccount::new);

        account.setProviderUuid(parsed.getProviderUuid());
        account.setOauthProvider(providerEntity);
        account.setDisplayName(parsed.getProviderDisplay());

        if (token.refreshToken() != null) {
            account.setRefreshTokenEnc(crypto.encrypt(token.refreshToken()));
        }
        if (token.accessToken() != null) {
            account.setAccessTokenEnc(crypto.encrypt(token.accessToken()));
        }
        if (token.idToken() != null) {
            account.setIdTokenEnc(crypto.encrypt(token.idToken()));
            account.setExternalEmail(GoogleOAuthProviderAdapter.emailFromIdToken(token.idToken()));
        }
        account.setScope(token.scope());
        if (token.expiresAt() != null) {
            account.setExpiresAt(LocalDateTime.ofInstant(token.expiresAt(), ZoneOffset.UTC));
        }
        account.setStatus(OAuthAccount.STATUS_ACTIVE);
        account.setVoided(false);

        List<String> capabilities = capabilitiesForProvider(oauthProviderCode);
        OAuthAccount saved = oauthAccountDao.saveWithCapabilities(account, capabilities);

        return new ConnectResult(
            saved.getProviderUuid(),
            oauthProviderCode,
            saved.getExternalEmail()
        );
    }

    public Optional<AccountStatusResponse> getAccountStatus(String providerUuid, String oauthProviderCode) {
        return oauthAccountDao.findByProviderUuidAndProviderCode(providerUuid, oauthProviderCode)
        .map(a -> new AccountStatusResponse(
            "STORED",
            a.getOauthProvider().getCode(),
            a.getExternalEmail(),
            a.getScope()
        ));
    }

    private List<String> capabilitiesForProvider(String oauthProviderCode) {
        if (GoogleOAuthProviderAdapter.CODE.equals(oauthProviderCode)) {
            return GOOGLE_CAPABILITIES;
        }
        return List.of();
    }

    public record ConnectResult(String providerUuid, String oauthProviderCode, String email) {}
}