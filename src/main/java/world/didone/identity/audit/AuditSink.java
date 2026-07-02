package world.didone.identity.audit;

import java.util.List;

public interface AuditSink {
    void append(AuditEvent event);

    List<AuditEvent> recentEvents();
}
