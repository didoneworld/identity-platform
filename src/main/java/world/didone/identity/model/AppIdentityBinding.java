package world.didone.identity.model;

import java.util.Map;

public record AppIdentityBinding(
        String bindingId,
        String appDid,
        String subjectDid,
        String appLocalSubjectId,
        Map<String, Object> mappedProfile,
        int lifecycleState,
        int version
) {}
