package in.indiemr.teleconsult.dto;

public record AccountStatusResponse(
    String status,
    String oauthProviderCode,
    String externalEmail,
    String scope
) {}