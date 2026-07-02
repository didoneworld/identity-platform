package world.didone.identity.oidc;

import java.util.Map;

public interface DidOneTokenSigner {
    String algorithm();
    String keyId();
    String sign(Map<String, Object> claims);
}
