package world.didone.identity.model;

import java.util.Map;

public record IdentityProfile(
        String displayName,
        String email,
        String username,
        String locale,
        String timezone,
        Map<String, Object> attributes
) {}
