package world.didone.identity.oidc;

import java.util.List;

public record DynamicClientRegistrationInput(
        String clientName,
        List<String> redirectUris,
        List<String> grantTypes,
        List<String> responseTypes,
        String tokenEndpointAuthMethod,
        String applicationType
) {}
