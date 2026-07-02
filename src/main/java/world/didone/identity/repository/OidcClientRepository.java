package world.didone.identity.repository;

import world.didone.identity.oidc.OidcClient;

import java.util.List;
import java.util.Optional;

public interface OidcClientRepository {
    OidcClient save(OidcClient client);

    Optional<OidcClient> findByClientId(String clientId);

    List<OidcClient> findAll();
}
