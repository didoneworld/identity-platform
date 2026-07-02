package world.didone.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import world.didone.identity.didcore.DIDCore;
import world.didone.identity.didcore.DIDCoreLifecycleState;
import world.didone.identity.lifecycle.AgentLifecycleState;
import world.didone.identity.oidc.DevTokenSigner;
import world.didone.identity.oidc.JsonWebKey;
import world.didone.identity.oidc.OidcProviderMetadata;
import world.didone.identity.oidc.OidcTokenService;
import world.didone.identity.oidc.TokenResponse;
import world.didone.identity.oidc.UserInfoClaims;
import world.didone.identity.recovery.RecoveryState;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DidOneIdentityApplication {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String ISSUER = System.getenv().getOrDefault("DIDONE_ISSUER", "http://localhost:8080");
    private static final OidcTokenService TOKEN_SERVICE = new OidcTokenService(ISSUER, new DevTokenSigner("didone-dev-key-1"));

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/health", exchange -> respond(exchange, 200, Map.of(
                "status", "ok",
                "service", "didone-identity-platform",
                "time", Instant.now().toString()
        )));

        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, 200, providerMetadata()));
        server.createContext("/.well-known/jwks.json", exchange -> respond(exchange, 200, Map.of("keys", List.of(sampleSigningKey()))));
        server.createContext("/oauth2/v1/userinfo", exchange -> respond(exchange, 200, sampleUserInfo()));
        server.createContext("/oauth2/v1/authorize", exchange -> respond(exchange, 501, Map.of(
                "error", "not_implemented",
                "message", "Authorization endpoint is reserved. Human login and consent ceremony will be implemented next."
        )));
        server.createContext("/oauth2/v1/token", DidOneIdentityApplication::handleToken);
        server.createContext("/connect/register", exchange -> respond(exchange, 501, Map.of(
                "error", "not_implemented",
                "message", "Dynamic client registration model exists. Persistence and policy gates are next."
        )));

        server.createContext("/v1/didcore", exchange -> respond(exchange, 200, sampleDidCore()));
        server.createContext("/v1/lifecycle/states", exchange -> respond(exchange, 200, Map.of(
                "didcore", List.of(DIDCoreLifecycleState.values()),
                "agent", List.of(AgentLifecycleState.values()),
                "recovery", List.of(RecoveryState.values())
        )));
        server.createContext("/", exchange -> respond(exchange, 200, Map.of(
                "name", "DID One Identity Platform",
                "law", "Identity first. Proof always. Recover or retire.",
                "endpoints", List.of(
                        "/health",
                        "/.well-known/openid-configuration",
                        "/.well-known/jwks.json",
                        "/oauth2/v1/userinfo",
                        "/oauth2/v1/authorize",
                        "/oauth2/v1/token",
                        "/connect/register",
                        "/v1/didcore",
                        "/v1/lifecycle/states"
                )
        )));
        server.start();
        System.out.printf("DID One Identity Platform running on port %d%n", port);
    }

    private static void handleToken(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, Map.of("error", "method_not_allowed", "message", "Use POST for token exchange."));
            return;
        }

        Map<String, String> form = parseForm(exchange);
        String grantType = form.getOrDefault("grant_type", "client_credentials");
        String clientId = form.getOrDefault("client_id", "didone-dev-client");
        String scope = form.getOrDefault("scope", "openid profile email did");
        String subjectDid = form.getOrDefault("did", "did:didone:identity:root");

        if (!List.of("client_credentials", "authorization_code", "refresh_token").contains(grantType)) {
            respond(exchange, 400, Map.of("error", "unsupported_grant_type", "grant_type", grantType));
            return;
        }

        TokenResponse tokenResponse = TOKEN_SERVICE.issueDevelopmentTokens(clientId, subjectDid, scope);
        respond(exchange, 200, tokenResponse);
    }

    private static Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        String raw = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (raw.isBlank()) {
            return Map.of();
        }
        return Arrays.stream(raw.split("&"))
                .map(part -> part.split("=", 2))
                .filter(pair -> pair.length == 2)
                .collect(Collectors.toMap(
                        pair -> decode(pair[0]),
                        pair -> decode(pair[1]),
                        (left, right) -> right
                ));
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static OidcProviderMetadata providerMetadata() {
        return new OidcProviderMetadata(
                ISSUER,
                ISSUER + "/oauth2/v1/authorize",
                ISSUER + "/oauth2/v1/token",
                ISSUER + "/oauth2/v1/userinfo",
                ISSUER + "/.well-known/jwks.json",
                ISSUER + "/connect/register",
                List.of("openid", "profile", "email", "did", "wallet", "vc"),
                List.of("code"),
                List.of("authorization_code", "refresh_token", "client_credentials"),
                List.of("public", "pairwise", "did"),
                List.of("none", "RS256"),
                List.of("client_secret_basic", "client_secret_post", "private_key_jwt", "none"),
                List.of("sub", "name", "preferred_username", "email", "email_verified", "profile", "picture", "locale", "zoneinfo", "did", "lifecycle_state", "trust_score")
        );
    }

    private static JsonWebKey sampleSigningKey() {
        return new JsonWebKey(
                "didone-dev-key-1",
                "oct",
                "sig",
                "none",
                null,
                null,
                null,
                null,
                null
        );
    }

    private static UserInfoClaims sampleUserInfo() {
        return new UserInfoClaims(
                "pairwise-subject-root",
                "did:didone:identity:root",
                "DID One Root",
                "didone-root",
                "root@didone.world",
                true,
                ISSUER + "/v1/didcore",
                null,
                "en",
                "UTC",
                DIDCoreLifecycleState.ACTIVE.code(),
                1000
        );
    }

    private static DIDCore sampleDidCore() {
        return new DIDCore(
                "did:didone:identity:root",
                "didone",
                "world",
                "did:didone:controller:root",
                "wallet:didone:root",
                DIDCoreLifecycleState.ACTIVE.code(),
                1000,
                0,
                1,
                "sha256:pending-document-hash",
                "recovery:root",
                "compatibility:root"
        );
    }

    private static void respond(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JSON.writerWithDefaultPrettyPrinter().writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
