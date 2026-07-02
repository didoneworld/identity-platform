package world.didone.identity.oidc;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public final class EphemeralRsaKeyProvider {
    private EphemeralRsaKeyProvider() {}

    public static RsaKeyMaterial create(String keyId) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return new RsaKeyMaterial(keyId, generator.generateKeyPair());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA key generation is unavailable", e);
        }
    }
}
