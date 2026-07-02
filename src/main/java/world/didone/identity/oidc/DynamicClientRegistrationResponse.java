package world.didone.identity.oidc;

import java.util.List;

public record DynamicClientRegistrationResponse(
        String clientId,
        String clientName,
        List<String> redirectUris,
        List<String> grantTypes,
        List<String> responseTypes,
        String tokenEndpointAuthMethod,
        String applicationType,
        int lifecycleState,
        String clientIdIssuedAt
) {}
