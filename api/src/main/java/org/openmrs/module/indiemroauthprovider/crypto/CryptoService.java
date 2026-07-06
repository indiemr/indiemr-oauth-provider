package org.openmrs.module.indiemroauthprovider.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.indiemroauthprovider.util.ModuleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("indiemroauthprovider.CryptoService")
public class CryptoService {
	
	private static final String AES_GCM = "AES/GCM/NoPadding";
	
	private static final int GCM_TAG_BITS = 128;
	
	private static final int IV_BYTES = 12;
	
	@Autowired
	@Qualifier("indiemroauthprovider.ModuleConfig")
	private ModuleConfig moduleConfig;
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	private final SecureRandom random = new SecureRandom();
	
	private byte[] key() throws Exception {
		String secret = moduleConfig.getEncKey();
		if (secret == null || secret.trim().isEmpty()) {
			throw new IllegalStateException("INDIEMR_OAUTH_ENC_KEY is not set (env or application.yml)");
		}
		return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
	}
	
	public String encrypt(String plaintext) throws Exception {
		byte[] iv = new byte[IV_BYTES];
		random.nextBytes(iv);
		Cipher cipher = Cipher.getInstance(AES_GCM);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
		byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
		int tagLen = 16;
		byte[] tag = new byte[tagLen];
		byte[] data = new byte[ct.length - tagLen];
		System.arraycopy(ct, ct.length - tagLen, tag, 0, tagLen);
		System.arraycopy(ct, 0, data, 0, data.length);
		return b64url(iv) + "." + b64url(tag) + "." + b64url(data);
	}
	
	public String decrypt(String payload) throws Exception {
		String[] parts = payload.split("\\.");
		if (parts.length != 3) {
			throw new IllegalArgumentException("Malformed encrypted payload");
		}
		byte[] iv = b64urlDecode(parts[0]);
		byte[] tag = b64urlDecode(parts[1]);
		byte[] data = b64urlDecode(parts[2]);
		byte[] ct = new byte[data.length + tag.length];
		System.arraycopy(data, 0, ct, 0, data.length);
		System.arraycopy(tag, 0, ct, data.length, tag.length);
		Cipher cipher = Cipher.getInstance(AES_GCM);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
		return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
	}
	
	public String signState(Map<String, Object> payload) throws Exception {
		String body = b64url(mapper.writeValueAsBytes(payload));
		String sig = hmac(body);
		return body + "." + sig;
	}
	
	public <T> T verifyState(String state, Class<T> type) throws Exception {
		String[] parts = state.split("\\.");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid OAuth state");
		}
		String body = parts[0];
		String sig = parts[1];
		String expected = hmac(body);
		if (!MessageDigest.isEqual(sig.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))) {
			throw new IllegalArgumentException("OAuth state signature mismatch");
		}
		return mapper.readValue(b64urlDecode(body), type);
	}
	
	public Map<String, Object> buildStatePayload(String providerUuid, String providerDisplay, String oauthProviderCode) {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("providerUuid", providerUuid);
		payload.put("providerDisplay", providerDisplay);
		payload.put("oauthProviderCode", oauthProviderCode);
		payload.put("t", System.currentTimeMillis());
		return payload;
	}
	
	public String randomToken(int bytes) {
		byte[] buf = new byte[bytes];
		random.nextBytes(buf);
		return b64url(buf);
	}
	
	private String hmac(String body) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(moduleConfig.getEncKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		return b64url(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
	}
	
	private static String b64url(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
	
	private static byte[] b64urlDecode(String s) {
		return Base64.getUrlDecoder().decode(s);
	}
}
