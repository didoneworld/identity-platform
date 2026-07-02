-- DID One Identity Platform SurrealDB schema
-- Namespace: didone
-- Database: identity

DEFINE TABLE did_core SCHEMAFULL;
DEFINE FIELD did ON did_core TYPE string;
DEFINE FIELD did_method ON did_core TYPE string;
DEFINE FIELD subject_type ON did_core TYPE string;
DEFINE FIELD controller_did ON did_core TYPE string;
DEFINE FIELD wallet_id ON did_core TYPE string;
DEFINE FIELD lifecycle_state ON did_core TYPE int;
DEFINE FIELD trust_score ON did_core TYPE int;
DEFINE FIELD risk_score ON did_core TYPE int;
DEFINE FIELD version ON did_core TYPE int;
DEFINE FIELD did_document_hash ON did_core TYPE string;
DEFINE FIELD recovery_policy_id ON did_core TYPE string;
DEFINE FIELD compatibility_profile_id ON did_core TYPE string;
DEFINE INDEX did_core_did_unique ON did_core FIELDS did UNIQUE;

DEFINE TABLE organization_core SCHEMAFULL;
DEFINE FIELD org_id ON organization_core TYPE string;
DEFINE FIELD org_did ON organization_core TYPE string;
DEFINE FIELD name ON organization_core TYPE string;
DEFINE FIELD domain ON organization_core TYPE string;
DEFINE FIELD owner_did ON organization_core TYPE string;
DEFINE FIELD lifecycle_state ON organization_core TYPE int;
DEFINE FIELD trust_score ON organization_core TYPE int;
DEFINE FIELD risk_score ON organization_core TYPE int;
DEFINE FIELD created ON organization_core TYPE datetime;
DEFINE FIELD version ON organization_core TYPE int;
DEFINE INDEX organization_core_did_unique ON organization_core FIELDS org_did UNIQUE;

DEFINE TABLE directory_core SCHEMAFULL;
DEFINE FIELD directory_id ON directory_core TYPE string;
DEFINE FIELD org_did ON directory_core TYPE string;
DEFINE FIELD name ON directory_core TYPE string;
DEFINE FIELD source_type ON directory_core TYPE string;
DEFINE FIELD source_system ON directory_core TYPE string;
DEFINE FIELD lifecycle_state ON directory_core TYPE int;
DEFINE FIELD version ON directory_core TYPE int;
DEFINE INDEX directory_core_id_unique ON directory_core FIELDS directory_id UNIQUE;

DEFINE TABLE user_identity SCHEMAFULL;
DEFINE FIELD user_id ON user_identity TYPE string;
DEFINE FIELD user_did ON user_identity TYPE string;
DEFINE FIELD directory_id ON user_identity TYPE string;
DEFINE FIELD username ON user_identity TYPE string;
DEFINE FIELD email ON user_identity TYPE string;
DEFINE FIELD profile ON user_identity TYPE object;
DEFINE FIELD lifecycle_state ON user_identity TYPE int;
DEFINE FIELD trust_score ON user_identity TYPE int;
DEFINE FIELD risk_score ON user_identity TYPE int;
DEFINE FIELD created ON user_identity TYPE datetime;
DEFINE FIELD updated ON user_identity TYPE datetime;
DEFINE FIELD version ON user_identity TYPE int;
DEFINE INDEX user_identity_did_unique ON user_identity FIELDS user_did UNIQUE;
DEFINE INDEX user_identity_email_unique ON user_identity FIELDS email UNIQUE;

DEFINE TABLE group_identity SCHEMAFULL;
DEFINE FIELD group_id ON group_identity TYPE string;
DEFINE FIELD group_did ON group_identity TYPE string;
DEFINE FIELD directory_id ON group_identity TYPE string;
DEFINE FIELD name ON group_identity TYPE string;
DEFINE FIELD description ON group_identity TYPE string;
DEFINE FIELD member_dids ON group_identity TYPE array<string>;
DEFINE FIELD lifecycle_state ON group_identity TYPE int;
DEFINE FIELD version ON group_identity TYPE int;
DEFINE INDEX group_identity_did_unique ON group_identity FIELDS group_did UNIQUE;

DEFINE TABLE application_identity SCHEMAFULL;
DEFINE FIELD app_id ON application_identity TYPE string;
DEFINE FIELD app_did ON application_identity TYPE string;
DEFINE FIELD org_did ON application_identity TYPE string;
DEFINE FIELD name ON application_identity TYPE string;
DEFINE FIELD protocol ON application_identity TYPE string;
DEFINE FIELD allowed_grant_types ON application_identity TYPE array<string>;
DEFINE FIELD redirect_uris ON application_identity TYPE array<string>;
DEFINE FIELD lifecycle_state ON application_identity TYPE int;
DEFINE FIELD version ON application_identity TYPE int;
DEFINE INDEX application_identity_did_unique ON application_identity FIELDS app_did UNIQUE;

DEFINE TABLE oidc_client SCHEMAFULL;
DEFINE FIELD client_id ON oidc_client TYPE string;
DEFINE FIELD client_name ON oidc_client TYPE string;
DEFINE FIELD redirect_uris ON oidc_client TYPE array<string>;
DEFINE FIELD grant_types ON oidc_client TYPE array<string>;
DEFINE FIELD response_types ON oidc_client TYPE array<string>;
DEFINE FIELD token_endpoint_auth_method ON oidc_client TYPE string;
DEFINE FIELD application_type ON oidc_client TYPE string;
DEFINE FIELD lifecycle_state ON oidc_client TYPE int;
DEFINE FIELD created_at ON oidc_client TYPE datetime;
DEFINE FIELD version ON oidc_client TYPE int;
DEFINE INDEX oidc_client_id_unique ON oidc_client FIELDS client_id UNIQUE;

DEFINE TABLE credential_anchor SCHEMAFULL;
DEFINE FIELD credential_id ON credential_anchor TYPE string;
DEFINE FIELD credential_type ON credential_anchor TYPE string;
DEFINE FIELD issuer_did ON credential_anchor TYPE string;
DEFINE FIELD holder_did ON credential_anchor TYPE string;
DEFINE FIELD subject_did ON credential_anchor TYPE string;
DEFINE FIELD status ON credential_anchor TYPE string;
DEFINE FIELD schema_id ON credential_anchor TYPE string;
DEFINE FIELD proof_digest ON credential_anchor TYPE string;
DEFINE FIELD issued_at ON credential_anchor TYPE datetime;
DEFINE FIELD expires_at ON credential_anchor TYPE option<datetime>;
DEFINE FIELD version ON credential_anchor TYPE int;
DEFINE INDEX credential_anchor_id_unique ON credential_anchor FIELDS credential_id UNIQUE;

DEFINE TABLE signing_key SCHEMAFULL;
DEFINE FIELD key_id ON signing_key TYPE string;
DEFINE FIELD owner_did ON signing_key TYPE string;
DEFINE FIELD algorithm ON signing_key TYPE string;
DEFINE FIELD key_type ON signing_key TYPE string;
DEFINE FIELD lifecycle_state ON signing_key TYPE int;
DEFINE FIELD created_at ON signing_key TYPE datetime;
DEFINE FIELD activated_at ON signing_key TYPE datetime;
DEFINE FIELD retired_at ON signing_key TYPE option<datetime>;
DEFINE FIELD public_jwk_digest ON signing_key TYPE string;
DEFINE FIELD version ON signing_key TYPE int;
DEFINE INDEX signing_key_id_unique ON signing_key FIELDS key_id UNIQUE;

DEFINE TABLE audit_event SCHEMAFULL;
DEFINE FIELD event_id ON audit_event TYPE string;
DEFINE FIELD event_type ON audit_event TYPE string;
DEFINE FIELD actor_did ON audit_event TYPE string;
DEFINE FIELD subject_did ON audit_event TYPE string;
DEFINE FIELD resource ON audit_event TYPE string;
DEFINE FIELD outcome ON audit_event TYPE string;
DEFINE FIELD occurred_at ON audit_event TYPE datetime;
DEFINE FIELD evidence ON audit_event TYPE object;
DEFINE INDEX audit_event_id_unique ON audit_event FIELDS event_id UNIQUE;
DEFINE INDEX audit_event_time ON audit_event FIELDS occurred_at;
