package world.didone.identity.keys;

import world.didone.identity.oidc.EphemeralRsaKeyProvider;
import world.didone.identity.oidc.JsonWebKey;
import world.didone.identity.oidc.RsaKeyMaterial;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class InMemorySigningKeyStore implements SigningKeyStore {
    private final String ownerDid;
    private final RsaKeyMaterial activeKey;
    private final KeyRecord activeRecord;

    public InMemorySigningKeyStore(String ownerDid, String keyId) {
        this.ownerDid = ownerDid;
        this.activeKey = EphemeralRsaKeyProvider.create(keyId);
        this.activeRecord = new KeyRecord(
                keyId,
                ownerDid,
                "RS256",
                "RSA",
                KeyLifecycleState.ACTIVE.code(),
                Instant.now(),
                Instant.now(),
                null,
                "runtime-public-jwk",
                1
        );
    }

    @Override
    public RsaKeyMaterial activeSigningKey() {
        return activeKey;
    }

    @Override
    public List<JsonWebKey> publicKeys() {
        return List.of(activeKey.toPublicJwk());
    }

    @Override
    public Optional<KeyRecord> activeKeyRecord() {
        return Optional.of(activeRecord);
    }

    @Override
    public KeyRotationPlan rotationPlan() {
        return new KeyRotationPlan(
                "rotation:not-requested",
                ownerDid,
                activeKey.keyId(),
                null,
                "No rotation requested",
                0,
                Instant.now(),
                null,
                null
        );
    }
}
