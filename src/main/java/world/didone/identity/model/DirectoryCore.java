package world.didone.identity.model;

public record DirectoryCore(
        String directoryId,
        String orgDid,
        String name,
        String sourceType,
        String sourceSystem,
        int lifecycleState,
        int version
) {}
