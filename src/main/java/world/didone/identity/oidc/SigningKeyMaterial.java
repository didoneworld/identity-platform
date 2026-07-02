package world.didone.identity.oidc;

public record SigningKeyMaterial(
        String keyId,
        String algorithm,
        String keyType,
        String publicMaterial,
        String privateMaterialRef,
        int lifecycleState,
        int version
) {}
