package org.openmrs.module.indiemroauthprovider.api.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.indiemroauthprovider.api.OAuthConnectService;
import org.openmrs.module.indiemroauthprovider.crypto.CryptoService;
import org.openmrs.module.indiemroauthprovider.dao.OAuthAccountDao;
import org.openmrs.module.indiemroauthprovider.dao.OAuthProviderDao;
import org.openmrs.module.indiemroauthprovider.dto.AccountStatusResponse;
import org.openmrs.module.indiemroauthprovider.dto.ConnectResult;
import org.openmrs.module.indiemroauthprovider.dto.ConnectState;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.model.OAuthProvider;
import org.openmrs.module.indiemroauthprovider.provider.OAuthProviderAdapter;
import org.openmrs.module.indiemroauthprovider.provider.dto.OAuthToken;
import org.openmrs.module.indiemroauthprovider.provider.google.GoogleOAuthProviderAdapter;
import org.openmrs.module.indiemroauthprovider.provider.registry.OAuthProviderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("indiemroauthprovider.OAuthConnectService")
@Transactional
public class OAuthConnectServiceImpl extends BaseOpenmrsService implements OAuthConnectService {
	
	private static final List<String> GOOGLE_CAPABILITIES = Arrays.asList("CALENDAR", "VIDEO_MEETING", "EMAIL");
	
	@Autowired
	@Qualifier("indiemroauthprovider.OAuthProviderRegistry")
	private OAuthProviderRegistry oauthRegistry;
	
	@Autowired
	@Qualifier("indiemroauthprovider.OAuthProviderDao")
	private OAuthProviderDao oauthProviderDao;
	
	@Autowired
	@Qualifier("indiemroauthprovider.OAuthAccountDao")
	private OAuthAccountDao oauthAccountDao;
	
	@Autowired
	@Qualifier("indiemroauthprovider.CryptoService")
	private CryptoService crypto;
	
	@Override
	public String buildConnectUrl(Provider provider, String providerDisplay, String oauthProviderCode) throws Exception {
		if (oauthProviderDao.findEnabledByCode(oauthProviderCode) == null) {
			throw new IllegalArgumentException("Provider not enabled: " + oauthProviderCode);
		}
		
		String signed = crypto.signState(crypto.buildStatePayload(provider.getUuid(), providerDisplay, oauthProviderCode));
		OAuthProviderAdapter adapter = oauthRegistry.require(oauthProviderCode);
		return adapter.buildAuthorizationUrl(signed);
	}
	
	@Override
	public ConnectResult handleCallback(String code, String state) throws Exception {
		ConnectState parsed = crypto.verifyState(state, ConnectState.class);
		String oauthProviderCode = parsed.getOauthProviderCode();
		
		OAuthProvider providerEntity = oauthProviderDao.findEnabledByCode(oauthProviderCode);
		if (providerEntity == null) {
			throw new IllegalStateException("Provider not enabled: " + oauthProviderCode);
		}
		
		Provider openmrsProvider = Context.getProviderService().getProviderByUuid(parsed.getProviderUuid());
		if (openmrsProvider == null) {
			throw new IllegalStateException("OpenMRS provider not found: " + parsed.getProviderUuid());
		}
		
		OAuthProviderAdapter adapter = oauthRegistry.require(oauthProviderCode);
		OAuthToken token = adapter.exchangeAuthorizationCode(code);
		
		OAuthAccount account = oauthAccountDao.findByProviderAndProviderCode(openmrsProvider, oauthProviderCode);
		if (account == null) {
			account = new OAuthAccount();
		}
		
		account.setProvider(openmrsProvider);
		account.setOauthProvider(providerEntity);
		account.setDisplayName(parsed.getProviderDisplay());
		
		if (token.getRefreshToken() != null) {
			account.setRefreshTokenEnc(crypto.encrypt(token.getRefreshToken()));
		}
		if (token.getAccessToken() != null) {
			account.setAccessTokenEnc(crypto.encrypt(token.getAccessToken()));
		}
		if (token.getIdToken() != null) {
			account.setIdTokenEnc(crypto.encrypt(token.getIdToken()));
			account.setExternalEmail(GoogleOAuthProviderAdapter.emailFromIdToken(token.getIdToken()));
		}
		account.setScope(token.getScope());
		if (token.getExpiresAt() != null) {
			account.setExpiresAt(token.getExpiresAt());
		}
		account.setStatus(OAuthAccount.STATUS_ACTIVE);
		account.setVoided(false);
		
		List<String> capabilities = capabilitiesForProvider(oauthProviderCode);
		OAuthAccount saved = oauthAccountDao.saveWithCapabilities(account, capabilities);
		
		return new ConnectResult(saved.getProvider().getUuid(), oauthProviderCode, saved.getExternalEmail());
	}
	
	@Override
	public AccountStatusResponse getAccountStatus(Provider provider, String oauthProviderCode) {
		OAuthAccount account = oauthAccountDao.findByProviderAndProviderCode(provider, oauthProviderCode);
		if (account == null) {
			return null;
		}
		return new AccountStatusResponse("STORED", account.getOauthProvider().getCode(), account.getExternalEmail(),
		        account.getScope());
	}
	
	private List<String> capabilitiesForProvider(String oauthProviderCode) {
		if (GoogleOAuthProviderAdapter.CODE.equals(oauthProviderCode)) {
			return GOOGLE_CAPABILITIES;
		}
		return Collections.emptyList();
	}
}
