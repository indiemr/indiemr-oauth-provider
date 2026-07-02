package org.openmrs.module.indiemroauthprovider.util;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component("indiemroauthprovider.ModuleConfig")
public class ModuleConfig {
	
	private ModuleConfigLoader loader;
	
	@PostConstruct
	public void init() {
		loader = new ModuleConfigLoader();
	}
	
	public String getPublicBaseUrl() {
		return loader.getPublicBaseUrl();
	}
	
	public String getEncKey() {
		return loader.getEncKey();
	}
	
	public String getGoogleClientId() {
		return loader.getGoogleClientId();
	}
	
	public String getGoogleClientSecret() {
		return loader.getGoogleClientSecret();
	}
	
	public String getGoogleRedirectUri() {
		return loader.getGoogleRedirectUri();
	}
}
