package world.didone.identity.keys;

import java.time.Instant;

public record KeyRotationPlan(
        String rotationId,
        String ownerDid,
        String currentKeyId,
        String nextKeyId,
        String reason,
        int status,
        Instant requestedAt,
        Instant activateAfter,
        Instant completedAt
) {}
