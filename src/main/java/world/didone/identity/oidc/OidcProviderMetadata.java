package world.didone.identity.oidc;

import java.util.List;

public record OidcProviderMetadata(
        String issuer,
        String authorizationEndpoint,
        String tokenEndpoint,
        String userInfoEndpoint,
        String jwksUri,
        String registrationEndpoint,
        List<String> scopesSupported,
        List<String> responseTypesSupported,
        List<String> grantTypesSupported,
        List<String> subjectTypesSupported,
        List<String> idTokenSigningAlgValuesSupported,
        List<String> tokenEndpointAuthMethodsSupported,
        List<String> claimsSupported
) {}
