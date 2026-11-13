# MyScool API Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reject malformed non-string API input in MyScool's AI and translation endpoints and protect the behavior with CI regression tests.

**Architecture:** Use Node.js built-in `node:test` tests with mocked request/response objects and no external network calls. Keep the existing serverless handler interfaces and patch only request validation plus the existing CI workflow.

**Tech Stack:** Node.js 24, built-in `node:test`, GitHub Actions, Vercel serverless handlers.

**Spec:** `docs/superpowers/specs/2026-11-13-myscool-api-hardening-design.md`

## Global Constraints
- No new dependencies.
- Keep existing valid string inputs and response shapes.
- Keep existing size limits: 12,000 characters for prompt/text and 80 characters for target.
- Keep existing 400/405/503 behavior for invalid input, wrong method, and missing API key.
- Do not change AdMob IDs, Android settings, secrets, Vercel deployment configuration, or production.
- All tests must mock external fetch calls.

## Review Focus
- JSON object/array/null values supplied instead of strings must return 400 rather than being coerced.
- Whitespace-only strings must remain invalid after trimming.
- Over-limit values must remain rejected.
- Wrong HTTP methods and absent API keys must not reach upstream services.
- Successful valid requests must retain the current upstream request and response contract (test with mocked fetch).

---

### Task 1: Add API validation regression tests

**Files:**
- Create: `tests/api-handlers.test.mjs`
- Modify: `.github/workflows/scooltools-android.yml`

**Interfaces:**
- Tests import default handlers from `../api/ai.js` and `../api/translate.js`.
- Test harness supplies `req.method`, `req.body`, `req.query`, and a response mock with `setHeader`, `status`, `json`, `end`, and `send`.

- [ ] **Step 1: Add tests for AI prompt validation**
  - `prompt` missing, empty, whitespace-only, object, array, and >12,000 chars each return 400.
  - Wrong method returns 405.
  - Missing `AI_API_KEY` and `OPENAI_API_KEY` returns 503.
  - Valid string request with mocked fetch returns 200 and the expected `text` field.

- [ ] **Step 2: Add tests for translation validation**
  - `text` and `target` missing, empty, whitespace-only, object/array, >12,000 text, and >80 target each return 400.
  - Wrong method returns 405.
  - Missing keys returns 503.
  - Valid string request with mocked fetch returns 200 and the expected `translation` field.

- [ ] **Step 3: Run the tests against current code**
  - Run: `node --test tests/api-handlers.test.mjs`
  - Expected: FAIL for non-string object/array values, demonstrating the current coercion bug; unrelated cases should pass.

- [ ] **Step 4: Add test command to CI**
  - Add `node --test tests/api-handlers.test.mjs` after the inline JavaScript validation step in the `web-check` job.
  - No changes to Android build parameters.

### Task 2: Fix API input validation

**Files:**
- Modify: `api/ai.js`
- Modify: `api/translate.js`

**Interfaces:**
- Handler signatures and response contracts remain unchanged.

- [ ] **Step 1: Validate AI prompt type before trimming**
  - Require `typeof body.prompt === "string"`; otherwise return the existing 400 validation response.
  - Trim only after the type check; retain the 12,000-character maximum.

- [ ] **Step 2: Validate translation input types before trimming**
  - Require both `typeof body.text === "string"` and `typeof body.target === "string"`; otherwise return HTTP 400.
  - Trim only after the type check; retain both existing maximum lengths.

- [ ] **Step 3: Run the regression tests**
  - Run: `node --test tests/api-handlers.test.mjs`
  - Expected: all API tests PASS.

- [ ] **Step 4: Run the existing checker**
  - Run: `node scripts/check-inline-js.cjs`
  - Expected: exit 0 and existing syntax-success message.

### Task 3: Final verification and review

**Files:** No additional files unless a test reveals a regression.

- [ ] **Step 1: Run both checks together**
  - Run: `node --test tests/api-handlers.test.mjs && node scripts/check-inline-js.cjs`
  - Expected: exit 0; all tests pass and inline JavaScript syntax is valid.

- [ ] **Step 2: Review the branch diff**
  - Confirm only the two API handlers, test file, workflow, and these design/plan docs changed.
  - Confirm AdMob IDs and Android build configuration are unchanged.
  - Confirm no secrets or real API keys were added.

- [ ] **Step 3: Trigger PR CI without merging**
  - Open a draft PR from `fix/myscool-superpowers` to `main` only after local/available verification.
  - Inspect workflow result and artifact behavior; do not merge or deploy.
