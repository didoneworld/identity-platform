package world.didone.identity.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InMemoryAuditSink implements AuditSink {
    private final List<AuditEvent> events = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void append(AuditEvent event) {
        events.add(event);
    }

    @Override
    public List<AuditEvent> recentEvents() {
        synchronized (events) {
            return List.copyOf(events);
        }
    }
}
