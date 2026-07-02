package world.didone.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import world.didone.identity.didcore.DIDCore;
import world.didone.identity.didcore.DIDCoreLifecycleState;
import world.didone.identity.lifecycle.AgentLifecycleState;
import world.didone.identity.recovery.RecoveryState;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class DidOneIdentityApplication {
    private static final ObjectMapper JSON = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", exchange -> respond(exchange, 200, Map.of(
                "status", "ok",
                "service", "didone-identity-platform",
                "time", Instant.now().toString()
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
                "endpoints", List.of("/health", "/v1/didcore", "/v1/lifecycle/states")
        )));
        server.start();
        System.out.printf("DID One Identity Platform running on port %d%n", port);
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
