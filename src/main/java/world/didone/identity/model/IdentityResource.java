package world.didone.identity.model;

import java.time.Instant;
import java.util.Map;

public record IdentityResource(
        String id,
        String resourceType,
        Instant created,
        Instant updated,
        Map<String, Object> profile,
        Map<String, IdentityLink> links,
        int lifecycleState,
        int version
) {}
