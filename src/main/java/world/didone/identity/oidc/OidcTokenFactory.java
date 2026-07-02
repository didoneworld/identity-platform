package world.didone.identity.oidc;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OidcTokenFactory {
    private final DidOneTokenSigner signer;
    private final String issuer;

    public OidcTokenFactory(DidOneTokenSigner signer, String issuer) {
        this.signer = signer;
        this.issuer = issuer;
    }

    public TokenResponse issueDevToken(String subject, String did, String scope, int lifecycleState, int trustScore) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + 3600;

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", issuer);
        claims.put("sub", subject);
        claims.put("aud", List.of("didone-identity-platform"));
        claims.put("iat", issuedAt);
        claims.put("exp", expiresAt);
        claims.put("did", did);
        claims.put("lifecycle_state", lifecycleState);
        claims.put("trust_score", trustScore);
        claims.put("scope", scope);

        String identityToken = signer.sign(claims);
        String accessToken = signer.sign(Map.of(
                "iss", issuer,
                "sub", subject,
                "aud", List.of("didone-api"),
                "iat", issuedAt,
                "exp", expiresAt,
                "scope", scope,
                "did", did
        ));

        return new TokenResponse(
                accessToken,
                "Bearer",
                3600,
                null,
                identityToken,
                scope
        );
    }
}
