package in.indiemr.teleconsult.provider.dto;

import java.time.Instant;

public record OAuthToken(
    String accessToken,
    String refreshToken,
    String idToken,
    String scope,
    Instant expiresAt
) {}