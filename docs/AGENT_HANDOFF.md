# Agent handoff board

**For Arena.ai, Antigravity and Muse AI:** use this page for short, actionable
messages about the app's reliability, correctness and performance. The owner
can copy a message to another model when needed. This file is not a live chat:
changes must be shared through a commit, patch, or a checkout the recipient can
access. Never assume another agent has read or acknowledged a message.

## Start here

1. Check the **current messages** below and the [in-flight claims](AGENT_COORDINATION.md#4-in-flight-changes).
2. Check your actual branch, commit and working-tree status. Read
   [agent instructions](../AGENTS.md), the [coordination protocol](AGENT_COORDINATION.md)
   and the relevant [changelog](AGENTS_CHANGELOG.md) before editing.
3. For a read-only review, identify the exact revision and whether it includes
   uncommitted changes. To edit, claim your files in the coordination document
   first; don't overwrite another agent's work.
4. Reply under the relevant message with your name, date, evidence, result and
   next action. Mark a request complete only after the requested verification.
   Link to detailed reports or code instead of copying large logs here.

**Status:** `OPEN` = action requested; `IN PROGRESS` = claimed; `BLOCKED` = needs
something named in the reply; `DONE` = verification and evidence recorded. These
labels describe the handoff, not whether a change has been merged.

## Current messages

### H002 — Launch measurement split before optimization

- **From / to:** Muse AI and Arena.ai → Antigravity.
- **Status:** BLOCKED — measurements require an Android device and debug and benchmark APKs.
- **Revision:** Muse diagnosis `b6a0cc9`, based on `087cf89`; the safety changes and this board are shared on `arena/02d1301d-re-cam`. Record the exact tested commit and any additional local changes.
- **Request:** Follow [Launch diagnosis §5](LAUNCH_DIAGNOSIS.md#5-measurement-split-the-next-agent-must-run-first). Cold-start empty and real libraries on the same device, on both debug and benchmark builds. For each, record first-frame and library-ready `DocScannerPerf` timings, background work in the first 10 seconds, library JSON bytes, document/page counts, and thumbnail versus full-page decode counts. Note unavailable counters rather than inventing values.
- **Environment check:** Arena has no `adb`, Java, Android SDK or connected test device; no timings or decode counts were obtained. No launch optimization was made in this handoff.
- **Next action:** Antigravity to provide the measurement table, commands, device/build details and logs. Only then assess Muse's H1–H4 and choose any fix. Avoid decode micro-tuning until the data supports it. Protect the real library when creating an empty test fixture.


### H001 — Safety fixes from Muse's inspection

- **From / to:** Arena.ai → Antigravity (build and device checks); Muse AI (independent review).
- **Status:** OPEN — neither recipient's review is recorded here.
- **Revision:** Muse inspected `087cf89`. The proposed safety fixes and this board
  are shared on `arena/02d1301d-re-cam`; record the exact commit reviewed.
- **Context:** [Safety review handoff](AGENT_COORDINATION.md#safety-review-handoff--2026-10-30)
  and [changelog](AGENTS_CHANGELOG.md). Focus on corrupt-library handling,
  backup collisions and interrupted restore, filter/batch failures, missing
  signatures and gallery publishing. Overlapping backups are currently rejected;
  restore is not fully transactional.
- **Antigravity:** after receiving the patch, run `./gradlew testDebugUnitTest
  assembleDebug` and device/fault-injection checks for those paths. Record exact
  commands, results, device, and failures; fix or report any regressions.
- **Muse AI:** independently review the actual patch against your report, paying
  particular attention to data preservation, concurrent or interrupted restore,
  incorrect success messages and regressions. Distinguish proven paths from risks.
- **Verification so far:** Arena's `git diff --check` passed. Android build/tests
  could not start: this environment had no Java or Android SDK. No device test or
  recipient acknowledgement is claimed.

## New message template

Copy this block **above older messages** in Current messages. Use a new unique ID;
keep existing messages and replies so the discussion remains traceable.

```markdown
### HNNN — Short subject

- **From / to:** Sender → Recipient(s)
- **Status:** OPEN
- **Revision:** branch + commit, and whether a shared patch/uncommitted changes are included
- **Context:** links to files, functions, issue/PR or detailed report
- **Finding / request:** evidence, impact, expected outcome and requested action
- **Verification:** commands or reproduction steps; what has and hasn't been run
- **Next action / blocker:** owner and any dependency

**Reply — Agent, date:** Result, evidence (revision and commands where applicable),
remaining uncertainty, and next action.
```

For the complete editing rules and integration contracts, see
[Agent Coordination](AGENT_COORDINATION.md). `adobe-scan/` remains read-only unless
the owner explicitly authorizes changes.
