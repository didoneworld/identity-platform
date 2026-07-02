package world.didone.identity.oidc;

import world.didone.identity.repository.OidcClientRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DynamicClientRegistrationService {
    private final OidcClientRepository clients;

    public DynamicClientRegistrationService(OidcClientRepository clients) {
        this.clients = clients;
    }

    public DynamicClientRegistrationResponse register(DynamicClientRegistrationInput input) {
        String clientId = "didone-client-" + UUID.randomUUID();
        List<String> redirectUris = input.redirectUris() == null || input.redirectUris().isEmpty()
                ? List.of("http://localhost/callback")
                : input.redirectUris();
        List<String> grantTypes = input.grantTypes() == null || input.grantTypes().isEmpty()
                ? List.of("authorization_code", "client_credentials")
                : input.grantTypes();
        List<String> responseTypes = input.responseTypes() == null || input.responseTypes().isEmpty()
                ? List.of("code")
                : input.responseTypes();
        String tokenEndpointAuthMethod = input.tokenEndpointAuthMethod() == null || input.tokenEndpointAuthMethod().isBlank()
                ? "none"
                : input.tokenEndpointAuthMethod();
        String applicationType = input.applicationType() == null || input.applicationType().isBlank()
                ? "web"
                : input.applicationType();
        String clientName = input.clientName() == null || input.clientName().isBlank()
                ? "DID One Client"
                : input.clientName();

        OidcClient client = new OidcClient(
                clientId,
                clientName,
                redirectUris,
                grantTypes,
                responseTypes,
                tokenEndpointAuthMethod,
                applicationType,
                1,
                Instant.now(),
                1
        );
        clients.save(client);

        return new DynamicClientRegistrationResponse(
                client.clientId(),
                client.clientName(),
                client.redirectUris(),
                client.grantTypes(),
                client.responseTypes(),
                client.tokenEndpointAuthMethod(),
                client.applicationType(),
                client.lifecycleState(),
                client.createdAt().toString()
        );
    }
}
