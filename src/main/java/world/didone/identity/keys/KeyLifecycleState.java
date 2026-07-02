package world.didone.identity.keys;

public enum KeyLifecycleState {
    CREATED(0),
    ACTIVE(1),
    ROTATION_PENDING(2),
    RETIRED(3),
    REVOKED(4),
    ARCHIVED(5);

    private final int code;

    KeyLifecycleState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
