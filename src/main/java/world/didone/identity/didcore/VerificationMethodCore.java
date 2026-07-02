package world.didone.identity.didcore;

public record VerificationMethodCore(
        String id,
        String type,
        String controllerDid,
        String publicKeyMultibase,
        int status,
        int version
) {}
