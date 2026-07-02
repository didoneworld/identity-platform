package world.didone.identity.recovery;

public enum RecoveryState {
    HEALTHY(0),
    CONCERN_DETECTED(1),
    DEGRADED(2),
    CONSTRAINED(3),
    QUARANTINED(4),
    REPAIR_STARTED(5),
    REPAIRED(6),
    VERIFICATION_PENDING(7),
    VERIFIED(8),
    RESTORED(9),
    RETIRED(10),
    ARCHIVED(11),
    UNRECOVERABLE(12);

    private final int code;

    RecoveryState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
