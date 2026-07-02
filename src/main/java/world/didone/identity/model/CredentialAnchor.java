package world.didone.identity.model;

import java.time.Instant;

public record CredentialAnchor(
        String credentialId,
        String credentialType,
        String issuerDid,
        String holderDid,
        String subjectDid,
        String status,
        String schemaId,
        String proofDigest,
        Instant issuedAt,
        Instant expiresAt,
        int version
) {}
