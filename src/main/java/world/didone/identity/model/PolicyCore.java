package world.didone.identity.model;

import java.util.List;

public record PolicyCore(
        String policyId,
        String policyDid,
        String orgDid,
        String name,
        String policyType,
        List<String> conditions,
        List<String> actions,
        int priority,
        int lifecycleState,
        int version
) {}
