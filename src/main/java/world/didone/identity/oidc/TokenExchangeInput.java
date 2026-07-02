package world.didone.identity.oidc;

public record TokenExchangeInput(
        String grantType,
        String authorizationCode,
        String redirectUri,
        String clientId,
        String refreshToken,
        String scope
) {}
