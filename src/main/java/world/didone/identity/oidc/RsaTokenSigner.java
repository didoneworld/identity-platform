package world.didone.identity.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

public final class RsaTokenSigner implements TokenSigner {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final RsaKeyMaterial keyMaterial;

    public RsaTokenSigner(RsaKeyMaterial keyMaterial) {
        this.keyMaterial = keyMaterial;
    }

    @Override
    public String algorithm() {
        return "RS256";
    }

    @Override
    public String keyId() {
        return keyMaterial.keyId();
    }

    @Override
    public String sign(Map<String, Object> claims) {
        try {
            Map<String, Object> header = Map.of(
                    "typ", "JWT",
                    "alg", algorithm(),
                    "kid", keyId()
            );
            String signingInput = encode(header) + "." + encode(claims);
            return signingInput + "." + signInput(signingInput, keyMaterial.keyPair().getPrivate());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign token", e);
        }
    }

    private static String encode(Object value) throws JsonProcessingException {
        byte[] json = JSON.writeValueAsBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
    }

    private static String signInput(String signingInput, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(signingInput.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
    }
}
