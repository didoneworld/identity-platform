package world.didone.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import world.didone.identity.audit.AuditEvent;
import world.didone.identity.audit.AuditSink;
import world.didone.identity.audit.InMemoryAuditSink;
import world.didone.identity.didcore.DIDCore;
import world.didone.identity.didcore.DIDCoreLifecycleState;
import world.didone.identity.keys.InMemorySigningKeyStore;
import world.didone.identity.keys.SigningKeyStore;
import world.didone.identity.lifecycle.AgentLifecycleState;
import world.didone.identity.oidc.DynamicClientRegistrationInput;
import world.didone.identity.oidc.DynamicClientRegistrationResponse;
import world.didone.identity.oidc.DynamicClientRegistrationService;
import world.didone.identity.oidc.JsonWebKey;
import world.didone.identity.oidc.OidcProviderMetadata;
import world.didone.identity.oidc.OidcTokenService;
import world.didone.identity.oidc.RsaTokenSigner;
import world.didone.identity.oidc.TokenResponse;
import world.didone.identity.oidc.UserInfoClaims;
import world.didone.identity.recovery.RecoveryState;
import world.didone.identity.repository.InMemoryOidcClientRepository;
import world.didone.identity.repository.OidcClientRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DidOneIdentityApplication {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String ISSUER = System.getenv().getOrDefault("DIDONE_ISSUER", "http://localhost:8080");
    private static final String ROOT_DID = "did:didone:identity:root";
    private static final SigningKeyStore KEY_STORE = new InMemorySigningKeyStore(ROOT_DID, "didone-rs256-dev-key-1");
    private static final AuditSink AUDIT = new InMemoryAuditSink();
    private static final OidcClientRepository CLIENTS = new InMemoryOidcClientRepository();
    private static final DynamicClientRegistrationService CLIENT_REGISTRATION = new DynamicClientRegistrationService(CLIENTS);
    private static final OidcTokenService TOKEN_SERVICE = new OidcTokenService(ISSUER, new RsaTokenSigner(KEY_STORE.activeSigningKey()));

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/health", exchange -> respond(exchange, 200, Map.of(
                "status", "ok",
                "service", "didone-identity-platform",
                "time", Instant.now().toString()
        )));

        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, 200, providerMetadata()));
        server.createContext("/.well-known/jwks.json", exchange -> respond(exchange, 200, Map.of("keys", KEY_STORE.publicKeys())));
        server.createContext("/oauth2/v1/keys", exchange -> respond(exchange, 200, Map.of(
                "active", KEY_STORE.activeKeyRecord().orElse(null),
                "rotation", KEY_STORE.rotationPlan(),
                "public_keys", KEY_STORE.publicKeys()
        )));
        server.createContext("/oauth2/v1/userinfo", exchange -> respond(exchange, 200, sampleUserInfo()));
        server.createContext("/oauth2/v1/audit", exchange -> respond(exchange, 200, Map.of("events", AUDIT.recentEvents())));
        server.createContext("/oauth2/v1/clients", exchange -> respond(exchange, 200, Map.of("clients", CLIENTS.findAll())));
        server.createContext("/oauth2/v1/authorize", exchange -> respond(exchange, 501, Map.of(
                "error", "not_implemented",
                "message", "Authorization endpoint is reserved. Human login and consent ceremony will be implemented next."
        )));
        server.createContext("/oauth2/v1/token", DidOneIdentityApplication::handleToken);
        server.createContext("/connect/register", DidOneIdentityApplication::handleClientRegistration);

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
                        "/oauth2/v1/keys",
                        "/oauth2/v1/audit",
                        "/oauth2/v1/clients",
                        "/oauth2/v1/userinfo",
                        "/oauth2/v1/authorize",
                        "/oauth2/v1/token",
                        "/connect/register",
                        "/v1/didcore",
                        "/v1/lifecycle/states"
                )
        )));
        server.start();
        AUDIT.append(new AuditEvent(
                UUID.randomUUID().toString(),
                "RUNTIME_STARTED",
                ROOT_DID,
                ROOT_DID,
                "didone-identity-platform",
                "success",
                Instant.now(),
                Map.of("issuer", ISSUER, "active_key", KEY_STORE.activeSigningKey().keyId())
        ));
        System.out.printf("DID One Identity Platform running on port %d%n", port);
    }

    private static void handleClientRegistration(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, Map.of("error", "method_not_allowed", "message", "Use POST for dynamic client registration."));
            return;
        }

        DynamicClientRegistrationInput input = JSON.readValue(exchange.getRequestBody(), DynamicClientRegistrationInput.class);
        DynamicClientRegistrationResponse response = CLIENT_REGISTRATION.register(input);
        AUDIT.append(new AuditEvent(
                UUID.randomUUID().toString(),
                "OIDC_CLIENT_REGISTERED",
                ROOT_DID,
                response.clientId(),
                "/connect/register",
                "success",
                Instant.now(),
                Map.of("client_id", response.clientId(), "client_name", response.clientName())
        ));
        respond(exchange, 201, response);
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
        String subjectDid = form.getOrDefault("did", ROOT_DID);

        if (!List.of("client_credentials", "authorization_code", "refresh_token").contains(grantType)) {
            AUDIT.append(new AuditEvent(
                    UUID.randomUUID().toString(),
                    "TOKEN_ISSUE_DENIED",
                    ROOT_DID,
                    subjectDid,
                    "/oauth2/v1/token",
                    "unsupported_grant_type",
                    Instant.now(),
                    Map.of("grant_type", grantType, "client_id", clientId)
            ));
            respond(exchange, 400, Map.of("error", "unsupported_grant_type", "grant_type", grantType));
            return;
        }

        TokenResponse tokenResponse = TOKEN_SERVICE.issueDevelopmentTokens(clientId, subjectDid, scope);
        AUDIT.append(new AuditEvent(
                UUID.randomUUID().toString(),
                "TOKEN_ISSUED",
                ROOT_DID,
                subjectDid,
                "/oauth2/v1/token",
                "success",
                Instant.now(),
                Map.of("grant_type", grantType, "client_id", clientId, "scope", scope, "key_id", KEY_STORE.activeSigningKey().keyId())
        ));
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
                List.of("RS256"),
                List.of("client_secret_basic", "client_secret_post", "private_key_jwt", "none"),
                List.of("sub", "name", "preferred_username", "email", "email_verified", "profile", "picture", "locale", "zoneinfo", "did", "lifecycle_state", "trust_score")
        );
    }

    private static JsonWebKey sampleSigningKey() {
        return KEY_STORE.publicKeys().getFirst();
    }

    private static UserInfoClaims sampleUserInfo() {
        return new UserInfoClaims(
                "pairwise-subject-root",
                ROOT_DID,
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
                ROOT_DID,
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
