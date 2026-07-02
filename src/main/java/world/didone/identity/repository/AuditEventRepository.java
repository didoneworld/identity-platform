package world.didone.identity.repository;

import world.didone.identity.audit.AuditEvent;

import java.util.List;

public interface AuditEventRepository {
    AuditEvent append(AuditEvent event);

    List<AuditEvent> recent(int limit);

    List<AuditEvent> findBySubjectDid(String subjectDid, int limit);
}
