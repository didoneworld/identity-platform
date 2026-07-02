package world.didone.identity.oidc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OidcTokenService {
    private final String issuer;
    private final TokenSigner signer;

    public OidcTokenService(String issuer, TokenSigner signer) {
        this.issuer = issuer;
        this.signer = signer;
    }

    public TokenResponse issueDevelopmentTokens(String clientId, String subjectDid, String scope) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + 3600;
        String subject = "pairwise-" + Math.abs(subjectDid.hashCode());
        List<String> audience = List.of(clientId == null || clientId.isBlank() ? "didone-dev-client" : clientId);

        Map<String, Object> identityClaims = Map.ofEntries(
                Map.entry("iss", issuer),
                Map.entry("sub", subject),
                Map.entry("aud", audience),
                Map.entry("exp", expiresAt),
                Map.entry("iat", issuedAt),
                Map.entry("jti", UUID.randomUUID().toString()),
                Map.entry("did", subjectDid),
                Map.entry("lifecycle_state", 8),
                Map.entry("trust_score", 1000),
                Map.entry("scope", scope == null || scope.isBlank() ? "openid profile email did" : scope)
        );

        Map<String, Object> accessClaims = Map.ofEntries(
                Map.entry("iss", issuer),
                Map.entry("sub", subject),
                Map.entry("aud", audience),
                Map.entry("exp", expiresAt),
                Map.entry("iat", issuedAt),
                Map.entry("jti", UUID.randomUUID().toString()),
                Map.entry("token_use", "access"),
                Map.entry("did", subjectDid),
                Map.entry("scope", scope == null || scope.isBlank() ? "openid profile email did" : scope)
        );

        return new TokenResponse(
                signer.sign(accessClaims),
                "Bearer",
                3600,
                null,
                signer.sign(identityClaims),
                scope == null || scope.isBlank() ? "openid profile email did" : scope
        );
    }
}
