package world.didone.identity.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DevIdentityTokenSigner implements DidOneTokenSigner {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final String keyId;

    public DevIdentityTokenSigner(String keyId) {
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
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("typ", "JWT");
        header.put("alg", algorithm());
        header.put("kid", keyId());
        return encode(header) + "." + encode(claims) + ".";
    }

    private static String encode(Object value) {
        try {
            return URL_ENCODER.encodeToString(JSON.writeValueAsString(value).getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("Unable to encode identity token", error);
        }
    }
}
