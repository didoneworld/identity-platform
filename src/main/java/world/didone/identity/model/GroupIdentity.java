package world.didone.identity.model;

import java.util.List;

public record GroupIdentity(
        String groupId,
        String groupDid,
        String directoryId,
        String name,
        String description,
        List<String> memberDids,
        int lifecycleState,
        int version
) {}
