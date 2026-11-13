# MyScool Superpowers Audit and API Hardening Design

**Date:** 2026-11-13  
**Repository:** `hakimdino787-sketch/Scooltools`  
**Target branch:** `fix/myscool-superpowers`

## Intent
Improve the reliability of MyScool's serverless API input handling and automated verification without changing the production deployment, advertising configuration, or Android release behavior.

## Current evidence
- `api/ai.js` coerces `body.prompt` to a string before validation, so non-string JSON values can become accepted text (for example, an object becomes `[object Object]`).
- `api/translate.js` does the same coercion for `text` and `target`.
- `scripts/check-inline-js.cjs` exits nonzero for JavaScript syntax errors, but missing static DOM IDs are warnings only.
- The Android Gradle default banner ID and workflow-supplied banner ID are both `ca-app-pub-2536808054649336/1278723301`. A previously mentioned alternative ID is not sufficient evidence to change the configured value; leave both IDs untouched.
- API handlers rely on environment variables and upstream services; verification must mock upstream calls and must not expose or modify secrets.

## Goals
- Reject malformed/non-string API input predictably with HTTP 400.
- Add repeatable automated regression tests for AI and translation request validation, method handling, and missing-key behavior.
- Make the existing CI workflow run those tests alongside its JavaScript syntax check.
- Preserve current valid string input behavior and API response shapes.

## Non-goals
- No AdMob ID changes.
- No production deployment, merge, or release.
- No new dependencies or paid services.
- No broad frontend rewrite or speculative DOM-ID changes.
- No live calls to AI or QR providers during tests.

## Approaches considered
1. Change only validation inline: smallest diff, but no regression coverage.
2. Add Node's built-in test runner and strict validation: selected because it adds no dependencies and proves the API boundary behavior in CI.
3. Rewrite the API layer or add a validation framework: unnecessary scope and dependency cost.

## Constraints and decisions
- Use Node.js 24 already configured by the workflow.
- Use built-in `node:test` and `node:assert`; add no packages.
- Validate that `prompt`, `text`, and `target` are strings before trimming.
- Preserve existing length limits and existing 400/503/405 semantics.
- Keep `AI_API_KEY` / `OPENAI_API_KEY` lookup and upstream request format unchanged.
- Do not modify AdMob IDs, Android build settings, Vercel production configuration, or secrets.
- Work only on `fix/myscool-superpowers`; no merge or deployment.

## Acceptance
- Regression tests fail against the current coercion behavior and pass after the minimal fix.
- Node's built-in test suite and existing inline-JavaScript checker both pass in CI.
- A draft pull request may be used solely to run the existing PR CI; it remains unmerged and does not deploy production.
