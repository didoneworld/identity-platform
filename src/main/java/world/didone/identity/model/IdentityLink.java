package world.didone.identity.model;

public record IdentityLink(
        String rel,
        String href,
        String method,
        String action,
        int status
) {}
