package world.didone.identity.model;

import java.time.Instant;

public record UserIdentity(
        String userId,
        String userDid,
        String directoryId,
        String username,
        String email,
        IdentityProfile profile,
        int lifecycleState,
        int trustScore,
        int riskScore,
        Instant created,
        Instant updated,
        int version
) {}
