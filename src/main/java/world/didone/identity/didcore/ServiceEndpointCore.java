package world.didone.identity.didcore;

public record ServiceEndpointCore(
        String id,
        String type,
        String serviceEndpoint,
        int status
) {}
