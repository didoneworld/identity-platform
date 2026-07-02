package world.didone.identity.oidc;

public record JsonWebKey(
        String kid,
        String kty,
        String use,
        String alg,
        String n,
        String e,
        String crv,
        String x,
        String y
) {}
