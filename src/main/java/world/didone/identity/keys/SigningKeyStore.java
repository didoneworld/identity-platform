package world.didone.identity.keys;

import world.didone.identity.oidc.JsonWebKey;
import world.didone.identity.oidc.RsaKeyMaterial;

import java.util.List;
import java.util.Optional;

public interface SigningKeyStore {
    RsaKeyMaterial activeSigningKey();

    List<JsonWebKey> publicKeys();

    Optional<KeyRecord> activeKeyRecord();

    KeyRotationPlan rotationPlan();
}
