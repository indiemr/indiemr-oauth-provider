package in.indiemr.teleconsult.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "teleconsult")
public class TeleconsultProperties {

    private String publicBaseUrl = "http://localhost:8080";
    private String encKey;

    public String getPublicBaseUrl() { return publicBaseUrl; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
    public String getEncKey() { return encKey; }
    public void setEncKey(String encKey) { this.encKey = encKey; }
}