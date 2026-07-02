package in.indiemr.teleconsult.provider.dto;

public record OAuthUser(
    String externalAccountId,
    String email,
    String displayName
) {}