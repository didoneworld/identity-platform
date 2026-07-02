package world.didone.identity.model;

import java.time.Instant;

public record OrganizationCore(
        String orgId,
        String orgDid,
        String name,
        String domain,
        String ownerDid,
        int lifecycleState,
        int trustScore,
        int riskScore,
        Instant created,
        int version
) {}
