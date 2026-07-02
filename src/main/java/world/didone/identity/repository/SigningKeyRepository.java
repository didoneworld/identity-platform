package world.didone.identity.repository;

import world.didone.identity.keys.KeyRecord;
import world.didone.identity.keys.KeyRotationPlan;

import java.util.List;
import java.util.Optional;

public interface SigningKeyRepository {
    KeyRecord save(KeyRecord keyRecord);

    Optional<KeyRecord> findActiveByOwnerDid(String ownerDid);

    List<KeyRecord> findByOwnerDid(String ownerDid);

    KeyRotationPlan saveRotationPlan(KeyRotationPlan plan);
}
