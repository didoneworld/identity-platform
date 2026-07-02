package world.didone.identity.model;

import java.util.List;

public record RecoveryPolicy(
        String recoveryPolicyId,
        String subjectDid,
        String recoveryMode,
        List<String> guardianDids,
        int threshold,
        int cooldownSeconds,
        boolean allowKeyRotation,
        boolean allowControllerChange,
        boolean allowGracefulExit,
        int version
) {}
