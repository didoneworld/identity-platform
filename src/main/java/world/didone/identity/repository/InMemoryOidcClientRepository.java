package world.didone.identity.repository;

import world.didone.identity.oidc.OidcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryOidcClientRepository implements OidcClientRepository {
    private final Map<String, OidcClient> clients = new ConcurrentHashMap<>();

    @Override
    public OidcClient save(OidcClient client) {
        clients.put(client.clientId(), client);
        return client;
    }

    @Override
    public Optional<OidcClient> findByClientId(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    @Override
    public List<OidcClient> findAll() {
        return new ArrayList<>(clients.values());
    }
}
