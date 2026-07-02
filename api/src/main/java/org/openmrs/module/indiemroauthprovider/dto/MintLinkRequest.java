package org.openmrs.module.indiemroauthprovider.dto;

/**
 * Request to mint a teleconsult link.
 */
public class MintLinkRequest {
	
	private String oauthProviderCode = "GOOGLE";
	
	private String title;
	
	private String resourceType;
	
	private String resourceUuid;
	
	public String getOauthProviderCode() {
		return oauthProviderCode;
	}
	
	public void setOauthProviderCode(String oauthProviderCode) {
		this.oauthProviderCode = oauthProviderCode;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getResourceType() {
		return resourceType;
	}
	
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	public String getResourceUuid() {
		return resourceUuid;
	}
	
	public void setResourceUuid(String resourceUuid) {
		this.resourceUuid = resourceUuid;
	}
}
