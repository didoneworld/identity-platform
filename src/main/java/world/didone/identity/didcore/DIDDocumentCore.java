package world.didone.identity.didcore;

import java.util.List;

public record DIDDocumentCore(
        String did,
        String controllerDid,
        List<String> alsoKnownAs,
        List<VerificationMethodCore> verificationMethods,
        List<String> authenticationMethods,
        List<String> assertionMethods,
        List<String> keyAgreementMethods,
        List<ServiceEndpointCore> services,
        int version,
        String previousDocumentHash,
        String documentHash
) {}
