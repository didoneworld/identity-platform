package world.didone.identity.repository;

import world.didone.identity.model.ApplicationIdentity;
import world.didone.identity.model.CompatibilityProfile;
import world.didone.identity.model.CredentialAnchor;
import world.didone.identity.model.DirectoryCore;
import world.didone.identity.model.GroupIdentity;
import world.didone.identity.model.OrganizationCore;
import world.didone.identity.model.PolicyCore;
import world.didone.identity.model.RecoveryPolicy;
import world.didone.identity.model.UserIdentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class IdentityCatalog {
    private final Map<String, OrganizationCore> organizations = new ConcurrentHashMap<>();
    private final Map<String, DirectoryCore> directories = new ConcurrentHashMap<>();
    private final Map<String, UserIdentity> users = new ConcurrentHashMap<>();
    private final Map<String, GroupIdentity> groups = new ConcurrentHashMap<>();
    private final Map<String, ApplicationIdentity> applications = new ConcurrentHashMap<>();
    private final Map<String, PolicyCore> policies = new ConcurrentHashMap<>();
    private final Map<String, CredentialAnchor> credentials = new ConcurrentHashMap<>();
    private final Map<String, RecoveryPolicy> recoveryPolicies = new ConcurrentHashMap<>();
    private final Map<String, CompatibilityProfile> compatibilityProfiles = new ConcurrentHashMap<>();

    public OrganizationCore saveOrganization(OrganizationCore value) {
        organizations.put(value.orgId(), value);
        return value;
    }

    public DirectoryCore saveDirectory(DirectoryCore value) {
        directories.put(value.directoryId(), value);
        return value;
    }

    public UserIdentity saveUser(UserIdentity value) {
        users.put(value.userId(), value);
        return value;
    }

    public GroupIdentity saveGroup(GroupIdentity value) {
        groups.put(value.groupId(), value);
        return value;
    }

    public ApplicationIdentity saveApplication(ApplicationIdentity value) {
        applications.put(value.appId(), value);
        return value;
    }

    public PolicyCore savePolicy(PolicyCore value) {
        policies.put(value.policyId(), value);
        return value;
    }

    public CredentialAnchor saveCredential(CredentialAnchor value) {
        credentials.put(value.credentialId(), value);
        return value;
    }

    public RecoveryPolicy saveRecoveryPolicy(RecoveryPolicy value) {
        recoveryPolicies.put(value.recoveryPolicyId(), value);
        return value;
    }

    public CompatibilityProfile saveCompatibilityProfile(CompatibilityProfile value) {
        compatibilityProfiles.put(value.compatibilityProfileId(), value);
        return value;
    }

    public List<OrganizationCore> organizations() { return new ArrayList<>(organizations.values()); }
    public List<DirectoryCore> directories() { return new ArrayList<>(directories.values()); }
    public List<UserIdentity> users() { return new ArrayList<>(users.values()); }
    public List<GroupIdentity> groups() { return new ArrayList<>(groups.values()); }
    public List<ApplicationIdentity> applications() { return new ArrayList<>(applications.values()); }
    public List<PolicyCore> policies() { return new ArrayList<>(policies.values()); }
    public List<CredentialAnchor> credentials() { return new ArrayList<>(credentials.values()); }
    public List<RecoveryPolicy> recoveryPolicies() { return new ArrayList<>(recoveryPolicies.values()); }
    public List<CompatibilityProfile> compatibilityProfiles() { return new ArrayList<>(compatibilityProfiles.values()); }

    public Optional<UserIdentity> findUser(String userId) { return Optional.ofNullable(users.get(userId)); }
    public Optional<OrganizationCore> findOrganization(String orgId) { return Optional.ofNullable(organizations.get(orgId)); }
}
