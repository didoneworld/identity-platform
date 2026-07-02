package world.didone.identity.repository;

import world.didone.identity.didcore.DIDCoreLifecycleState;
import world.didone.identity.model.ApplicationIdentity;
import world.didone.identity.model.CompatibilityProfile;
import world.didone.identity.model.CredentialAnchor;
import world.didone.identity.model.DirectoryCore;
import world.didone.identity.model.GroupIdentity;
import world.didone.identity.model.IdentityProfile;
import world.didone.identity.model.OrganizationCore;
import world.didone.identity.model.PolicyCore;
import world.didone.identity.model.RecoveryPolicy;
import world.didone.identity.model.UserIdentity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class IdentityBootstrap {
    private IdentityBootstrap() {}

    public static IdentityCatalog rootCatalog() {
        IdentityCatalog catalog = new IdentityCatalog();
        Instant now = Instant.now();
        String rootDid = "did:didone:identity:root";
        String orgDid = "did:didone:org:didone-world";
        String directoryId = "directory:didone:root";

        catalog.saveOrganization(new OrganizationCore(
                "org:didone-world",
                orgDid,
                "DID One World",
                "didone.world",
                rootDid,
                DIDCoreLifecycleState.ACTIVE.code(),
                1000,
                0,
                now,
                1
        ));

        catalog.saveDirectory(new DirectoryCore(
                directoryId,
                orgDid,
                "DID One Root Directory",
                "native",
                "didone-identity-platform",
                DIDCoreLifecycleState.ACTIVE.code(),
                1
        ));

        catalog.saveUser(new UserIdentity(
                "user:root",
                rootDid,
                directoryId,
                "didone-root",
                "root@didone.world",
                new IdentityProfile(
                        "DID One Root",
                        "root@didone.world",
                        "didone-root",
                        "en",
                        "UTC",
                        Map.of("role", "root", "subject_type", "world")
                ),
                DIDCoreLifecycleState.ACTIVE.code(),
                1000,
                0,
                now,
                now,
                1
        ));

        catalog.saveGroup(new GroupIdentity(
                "group:identity-admins",
                "did:didone:group:identity-admins",
                directoryId,
                "Identity Administrators",
                "Administrators allowed to manage identity resources.",
                List.of(rootDid),
                DIDCoreLifecycleState.ACTIVE.code(),
                1
        ));

        catalog.saveApplication(new ApplicationIdentity(
                "app:didone-console",
                "did:didone:app:console",
                orgDid,
                "DID One Console",
                "oidc",
                List.of("authorization_code", "client_credentials", "refresh_token"),
                List.of("http://localhost:3000/callback"),
                DIDCoreLifecycleState.ACTIVE.code(),
                1
        ));

        catalog.savePolicy(new PolicyCore(
                "policy:identity-default-deny",
                "did:didone:policy:identity-default-deny",
                orgDid,
                "Identity Default Deny",
                "authorization",
                List.of("identity_exists", "did_active", "proof_present"),
                List.of("allow_read_identity", "require_gate_for_write"),
                100,
                DIDCoreLifecycleState.ACTIVE.code(),
                1
        ));

        catalog.saveCredential(new CredentialAnchor(
                "credential:root-identity-admin",
                "IdentityAdministratorCredential",
                orgDid,
                rootDid,
                rootDid,
                "active",
                "schema:didone:identity-admin:v1",
                "sha256:pending-proof-digest",
                now,
                null,
                1
        ));

        catalog.saveRecoveryPolicy(new RecoveryPolicy(
                "recovery:root",
                rootDid,
                "guardian-threshold",
                List.of(orgDid),
                1,
                0,
                true,
                true,
                true,
                1
        ));

        catalog.saveCompatibilityProfile(new CompatibilityProfile(
                "compatibility:root",
                rootDid,
                1,
                1,
                1,
                null,
                "migration:none"
        ));

        return catalog;
    }
}
