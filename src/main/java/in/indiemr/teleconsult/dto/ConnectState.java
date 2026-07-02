package in.indiemr.teleconsult.dto;

public class ConnectState {
    private String providerUuid;
    private String providerDisplay;
    private long t;
    private String oauthProviderCode;

    public String getOauthProviderCode() { return oauthProviderCode; }
    public void setOauthProviderCode(String oauthProviderCode) { this.oauthProviderCode = oauthProviderCode; }
    public String getProviderUuid() { return providerUuid; }
    public void setProviderUuid(String providerUuid) { this.providerUuid = providerUuid; }
    public String getProviderDisplay() { return providerDisplay; }
    public void setProviderDisplay(String providerDisplay) { this.providerDisplay = providerDisplay; }
    public long getT() { return t; }
    public void setT(long t) { this.t = t; }
}