package world.didone.identity.oidc;

public record UserInfoClaims(
        String subject,
        String did,
        String name,
        String preferredUsername,
        String email,
        boolean emailVerified,
        String profile,
        String picture,
        String locale,
        String zoneInfo,
        int lifecycleState,
        int trustScore
) {}
