package world.didone.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import world.didone.identity.model.ApplicationIdentity;
import world.didone.identity.model.CompatibilityProfile;
import world.didone.identity.model.CredentialAnchor;
import world.didone.identity.model.DirectoryCore;
import world.didone.identity.model.GroupIdentity;
import world.didone.identity.model.OrganizationCore;
import world.didone.identity.model.PolicyCore;
import world.didone.identity.model.RecoveryPolicy;
import world.didone.identity.model.UserIdentity;
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
import world.didone.identity.repository.IdentityBootstrap;
import world.didone.identity.repository.IdentityCatalog;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class DidOneIdentityApplication {
    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final String ISSUER = System.getenv().getOrDefault("DIDONE_ISSUER", "http://localhost:8080");
    private static final String ROOT_DID = "did:didone:identity:root";
    private static final SigningKeyStore KEY_STORE = new InMemorySigningKeyStore(ROOT_DID, "didone-rs256-dev-key-1");
    private static final AuditSink AUDIT = new InMemoryAuditSink();
    private static final IdentityCatalog IDENTITIES = IdentityBootstrap.rootCatalog();
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

        server.createContext("/v1/identity", DidOneIdentityApplication::handleIdentitySummary);
        server.createContext("/v1/identity/organizations", exchange -> collection(exchange, "organizations", OrganizationCore.class, IDENTITIES::organizations, IDENTITIES::saveOrganization));
        server.createContext("/v1/identity/directories", exchange -> collection(exchange, "directories", DirectoryCore.class, IDENTITIES::directories, IDENTITIES::saveDirectory));
        server.createContext("/v1/identity/users", exchange -> collection(exchange, "users", UserIdentity.class, IDENTITIES::users, IDENTITIES::saveUser));
        server.createContext("/v1/identity/groups", exchange -> collection(exchange, "groups", GroupIdentity.class, IDENTITIES::groups, IDENTITIES::saveGroup));
        server.createContext("/v1/identity/applications", exchange -> collection(exchange, "applications", ApplicationIdentity.class, IDENTITIES::applications, IDENTITIES::saveApplication));
        server.createContext("/v1/identity/policies", exchange -> collection(exchange, "policies", PolicyCore.class, IDENTITIES::policies, IDENTITIES::savePolicy));
        server.createContext("/v1/identity/credentials", exchange -> collection(exchange, "credentials", CredentialAnchor.class, IDENTITIES::credentials, IDENTITIES::saveCredential));
        server.createContext("/v1/identity/recovery-policies", exchange -> collection(exchange, "recovery_policies", RecoveryPolicy.class, IDENTITIES::recoveryPolicies, IDENTITIES::saveRecoveryPolicy));
        server.createContext("/v1/identity/compatibility-profiles", exchange -> collection(exchange, "compatibility_profiles", CompatibilityProfile.class, IDENTITIES::compatibilityProfiles, IDENTITIES::saveCompatibilityProfile));

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
                        "/v1/identity",
                        "/v1/identity/organizations",
                        "/v1/identity/directories",
                        "/v1/identity/users",
                        "/v1/identity/groups",
                        "/v1/identity/applications",
                        "/v1/identity/policies",
                        "/v1/identity/credentials",
                        "/v1/identity/recovery-policies",
                        "/v1/identity/compatibility-profiles",
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

    private static void handleIdentitySummary(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, Map.of("error", "method_not_allowed", "message", "Use GET for identity summary."));
            return;
        }
        respond(exchange, 200, Map.of(
                "organizations", IDENTITIES.organizations().size(),
                "directories", IDENTITIES.directories().size(),
                "users", IDENTITIES.users().size(),
                "groups", IDENTITIES.groups().size(),
                "applications", IDENTITIES.applications().size(),
                "policies", IDENTITIES.policies().size(),
                "credentials", IDENTITIES.credentials().size(),
                "recovery_policies", IDENTITIES.recoveryPolicies().size(),
                "compatibility_profiles", IDENTITIES.compatibilityProfiles().size(),
                "law", "No identity, no action. No proof, no trust. No recovery, no production identity."
        ));
    }

    private static <T> void collection(
            HttpExchange exchange,
            String resourceName,
            Class<T> type,
            Supplier<List<T>> list,
            Function<T, T> save
    ) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 200, Map.of(resourceName, list.get()));
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            T value = JSON.readValue(exchange.getRequestBody(), type);
            T saved = save.apply(value);
            AUDIT.append(new AuditEvent(
                    UUID.randomUUID().toString(),
                    "IDENTITY_RESOURCE_REGISTERED",
                    ROOT_DID,
                    resourceName,
                    "/v1/identity/" + resourceName,
                    "success",
                    Instant.now(),
                    Map.of("resource_type", resourceName)
            ));
            respond(exchange, 201, saved);
            return;
        }
        respond(exchange, 405, Map.of("error", "method_not_allowed", "message", "Use GET or POST for " + resourceName + "."));
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
