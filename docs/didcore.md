# DIDCore

DIDCore is the identity kernel of DID One World.

```text
DIDCore = identity kernel + controller law + wallet binding + credential root + proof engine + recovery path + compatibility contract + immutable audit
```

## DIDCore law

```text
No DID, no identity.
No controller, no authority.
No verification method, no proof.
No wallet, no possession.
No status, no governance.
No recovery, no production identity.
No compatibility, no stable world.
No audit, no trust.
```

## DIDCore objects

1. DID
2. DID Document
3. Controller
4. Verification Method
5. Wallet Binding
6. Credential Anchor
7. Status
8. Recovery Policy
9. Compatibility Profile
10. Audit Event

## Lifecycle

```text
0  unborn
1  requested
2  reserved
3  created
4  controller_bound
5  verification_method_bound
6  wallet_bound
7  credential_anchor_bound
8  active
9  constrained
10 suspended
11 recovery_started
12 recovered
13 graceful_exit
14 retired
15 archived
16 canonical
```
