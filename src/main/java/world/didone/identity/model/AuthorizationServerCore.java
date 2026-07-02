package world.didone.identity.model;

import java.util.List;

public record AuthorizationServerCore(
        String authorizationServerId,
        String authorizationServerDid,
        String orgDid,
        String issuer,
        List<String> supportedProtocols,
        List<String> audiences,
        int lifecycleState,
        int version
) {}
