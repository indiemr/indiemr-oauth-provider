package org.openmrs.module.indiemroauthprovider.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.openmrs.util.OpenmrsUtil;
import org.yaml.snakeyaml.Yaml;

/**
 * Loads module settings from an application.yml file, with environment-variable overrides.
 */
public class ModuleConfigLoader {
	
	private final Map<String, Object> config;
	
	public ModuleConfigLoader() {
		config = loadConfig();
	}
	
	public String getPublicBaseUrl() {
		return resolve("INDIEMR_OAUTH_PUBLIC_BASE_URL", "teleconsult.public-base-url");
	}
	
	public String getEncKey() {
		return resolve("INDIEMR_OAUTH_ENC_KEY", "teleconsult.enc-key");
	}
	
	public String getGoogleClientId() {
		return resolve("INDIEMR_OAUTH_GOOGLE_CLIENT_ID", "google.client-id");
	}
	
	public String getGoogleClientSecret() {
		return resolve("INDIEMR_OAUTH_GOOGLE_CLIENT_SECRET", "google.client-secret");
	}
	
	public String getGoogleRedirectUri() {
		return resolve("INDIEMR_OAUTH_GOOGLE_REDIRECT_URI", "google.redirect-uri");
	}
	
	private String resolve(String envKey, String dottedKey) {
		String env = System.getenv(envKey);
		if (env != null && !env.trim().isEmpty()) {
			return env.trim();
		}
		return lookupDottedKey(dottedKey);
	}
	
	@SuppressWarnings("unchecked")
	private String lookupDottedKey(String dottedKey) {
		if (config == null) {
			return null;
		}
		String[] parts = dottedKey.split("\\.");
		Object current = config;
		for (String part : parts) {
			if (!(current instanceof Map)) {
				return null;
			}
			current = ((Map<String, Object>) current).get(part);
			if (current == null) {
				return null;
			}
		}
		return current instanceof String ? (String) current : String.valueOf(current);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> loadConfig() {
		File configFile = resolveConfigFile();
		if (configFile == null || !configFile.isFile()) {
			return null;
		}
		try (InputStream in = new FileInputStream(configFile)) {
			Object loaded = new Yaml().load(in);
			if (loaded instanceof Map) {
				return (Map<String, Object>) loaded;
			}
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to load config from " + configFile.getAbsolutePath(), e);
		}
		return null;
	}
	
	private File resolveConfigFile() {
		String explicitPath = System.getenv("INDIEMR_OAUTH_CONFIG_FILE");
		if (explicitPath != null && !explicitPath.trim().isEmpty()) {
			return new File(explicitPath.trim());
		}
		String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
		if (appDataDir != null) {
			File defaultFile = new File(appDataDir, "indiemroauthprovider/application.yml");
			if (defaultFile.isFile()) {
				return defaultFile;
			}
		}
		return null;
	}
}
