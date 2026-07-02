package world.didone.identity.model;

import java.time.Instant;

public record CompatibilityProfile(
        String compatibilityProfileId,
        String subjectDid,
        int currentVersion,
        int minimumReadableVersion,
        int compatibilityStatus,
        Instant compatibleUntil,
        String migrationPlanDigest
) {}
