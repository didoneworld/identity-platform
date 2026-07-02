# Lifecycle Governance

Every identity and agent must have lifecycle governance.

```text
No agent exists outside lifecycle.
No agent acts outside governance.
No agent keeps autonomy after trust breaks.
```

## Agent lifecycle states

```text
0  unborn
1  requested
2  registered
3  verified
4  provisioned
5  active
6  monitored
7  constrained
8  probation
9  suspended
10 revoked
11 graceful_exit_requested
12 handoff_in_progress
13 exited
14 retired
15 archived
16 backward_compatible
```

## Strong rule

```text
Capability can increase through learning.
Autonomy can only increase through governance.
```

## Transition gate

Every transition requires:

```text
DID check
+ VC claim check
+ policy decision
+ relationship check
+ trust-chain verification
+ immutable audit event
+ state projection
+ receipt
```
