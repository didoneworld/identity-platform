package world.didone.identity.didcore;

public record DIDCore(
        String did,
        String didMethod,
        String subjectType,
        String controllerDid,
        String walletId,
        int lifecycleState,
        int trustScore,
        int riskScore,
        int version,
        String didDocumentHash,
        String recoveryPolicyId,
        String compatibilityProfileId
) {}
