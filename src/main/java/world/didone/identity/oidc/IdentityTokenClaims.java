package world.didone.identity.oidc;

import java.util.List;

public record IdentityTokenClaims(
        String issuer,
        String subject,
        List<String> audience,
        long expiresAt,
        long issuedAt,
        String nonceValue,
        String authTime,
        String assuranceLevel,
        String authMethods,
        String did,
        String lifecycleState,
        int trustScore
) {}
