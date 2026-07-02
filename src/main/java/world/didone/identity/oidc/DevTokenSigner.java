package world.didone.identity.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public final class DevTokenSigner implements TokenSigner {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final String keyId;

    public DevTokenSigner(String keyId) {
        this.keyId = keyId;
    }

    @Override
    public String algorithm() {
        return "none";
    }

    @Override
    public String keyId() {
        return keyId;
    }

    @Override
    public String sign(Map<String, Object> claims) {
        try {
            Map<String, Object> header = Map.of(
                    "typ", "JWT",
                    "alg", algorithm(),
                    "kid", keyId
            );
            return encode(header) + "." + encode(claims) + ".";
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to create development token", e);
        }
    }

    private static String encode(Object value) throws JsonProcessingException {
        byte[] json = JSON.writeValueAsBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
    }
}
