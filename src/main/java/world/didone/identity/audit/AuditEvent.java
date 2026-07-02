package world.didone.identity.audit;

import java.time.Instant;
import java.util.Map;

public record AuditEvent(
        String eventId,
        String eventType,
        String actorDid,
        String subjectDid,
        String resource,
        String outcome,
        Instant occurredAt,
        Map<String, Object> evidence
) {}
