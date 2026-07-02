package in.indiemr.teleconsult.provider;

import in.indiemr.teleconsult.model.OAuthAccount;
import in.indiemr.teleconsult.provider.dto.OAuthToken;
import in.indiemr.teleconsult.provider.dto.OAuthUser;

public interface OAuthProviderAdapter {

    /** Must match oauth_provider.code — e.g. GOOGLE, MICROSOFT */
    String getProviderCode();

    String buildAuthorizationUrl(String state) throws Exception;

    OAuthToken exchangeAuthorizationCode(String code) throws Exception;

    OAuthUser getCurrentUser(OAuthAccount account, String decryptedAccessToken) throws Exception;

    void revoke(OAuthAccount account, String decryptedRefreshToken) throws Exception;
}