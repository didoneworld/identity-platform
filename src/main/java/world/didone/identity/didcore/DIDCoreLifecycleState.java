package world.didone.identity.didcore;

public enum DIDCoreLifecycleState {
    UNBORN(0),
    REQUESTED(1),
    RESERVED(2),
    CREATED(3),
    CONTROLLER_BOUND(4),
    VERIFICATION_METHOD_BOUND(5),
    WALLET_BOUND(6),
    CREDENTIAL_ANCHOR_BOUND(7),
    ACTIVE(8),
    CONSTRAINED(9),
    SUSPENDED(10),
    RECOVERY_STARTED(11),
    RECOVERED(12),
    GRACEFUL_EXIT(13),
    RETIRED(14),
    ARCHIVED(15),
    CANONICAL(16);

    private final int code;

    DIDCoreLifecycleState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
