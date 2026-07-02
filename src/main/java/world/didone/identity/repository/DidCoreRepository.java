package world.didone.identity.repository;

import world.didone.identity.didcore.DIDCore;

import java.util.Optional;

public interface DidCoreRepository {
    DIDCore save(DIDCore didCore);

    Optional<DIDCore> findByDid(String did);
}
