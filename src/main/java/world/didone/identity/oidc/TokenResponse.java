package world.didone.identity.oidc;

public record TokenResponse(
        String accessToken,
        String tokenType,
        int expiresIn,
        String refreshToken,
        String idToken,
        String scope
) {}
