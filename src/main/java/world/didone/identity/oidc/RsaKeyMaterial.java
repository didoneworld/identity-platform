package world.didone.identity.oidc;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public final class RsaKeyMaterial {
    private final String keyId;
    private final KeyPair keyPair;

    public RsaKeyMaterial(String keyId, KeyPair keyPair) {
        this.keyId = keyId;
        this.keyPair = keyPair;
    }

    public String keyId() {
        return keyId;
    }

    public KeyPair keyPair() {
        return keyPair;
    }

    public JsonWebKey toPublicJwk() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return new JsonWebKey(
                keyId,
                "RSA",
                "sig",
                "RS256",
                unsignedBase64(publicKey.getModulus().toByteArray()),
                unsignedBase64(publicKey.getPublicExponent().toByteArray()),
                null,
                null,
                null
        );
    }

    private static String unsignedBase64(byte[] bytes) {
        int start = 0;
        while (start < bytes.length - 1 && bytes[start] == 0) {
            start++;
        }
        byte[] normalized = java.util.Arrays.copyOfRange(bytes, start, bytes.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(normalized);
    }
}
