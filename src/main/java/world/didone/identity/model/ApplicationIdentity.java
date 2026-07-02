package world.didone.identity.model;

import java.util.List;

public record ApplicationIdentity(
        String appId,
        String appDid,
        String orgDid,
        String name,
        String protocol,
        List<String> allowedGrantTypes,
        List<String> redirectUris,
        int lifecycleState,
        int version
) {}
