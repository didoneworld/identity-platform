package world.didone.identity.keys;

import java.time.Instant;

public record KeyRecord(
        String keyId,
        String ownerDid,
        String algorithm,
        String keyType,
        int lifecycleState,
        Instant createdAt,
        Instant activatedAt,
        Instant retiredAt,
        String publicJwkDigest,
        int version
) {}
