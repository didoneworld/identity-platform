package world.didone.identity.lifecycle;

public enum AgentLifecycleState {
    UNBORN(0),
    REQUESTED(1),
    REGISTERED(2),
    VERIFIED(3),
    PROVISIONED(4),
    ACTIVE(5),
    MONITORED(6),
    CONSTRAINED(7),
    PROBATION(8),
    SUSPENDED(9),
    REVOKED(10),
    GRACEFUL_EXIT_REQUESTED(11),
    HANDOFF_IN_PROGRESS(12),
    EXITED(13),
    RETIRED(14),
    ARCHIVED(15),
    BACKWARD_COMPATIBLE(16);

    private final int code;

    AgentLifecycleState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
