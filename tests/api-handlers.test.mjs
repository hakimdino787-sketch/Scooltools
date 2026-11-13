import test from 'node:test';
import assert from 'node:assert/strict';
import aiHandler from '../api/ai.js';
import translateHandler from '../api/translate.js';

function makeRes() {
  return {
    statusCode: 200, headers: {}, body: undefined,
    setHeader(name, value) { this.headers[name] = value; return this; },
    status(code) { this.statusCode = code; return this; },
    json(value) { this.body = value; return this; },
    end() { this.ended = true; return this; },
    send(value) { this.body = value; return this; },
  };
}

async function invoke(handler, method, body) {
  const res = makeRes();
  await handler({ method, body }, res);
  return res;
}

function withNoKeys() {
  delete process.env.AI_API_KEY;
  delete process.env.OPENAI_API_KEY;
}

function withTestKey() {
  process.env.AI_API_KEY = 'test-key-not-real';
  delete process.env.OPENAI_API_KEY;
}

function mockFetch(payload) {
  const original = globalThis.fetch;
  globalThis.fetch = async () => ({ ok: true, status: 200, json: async () => payload });
  return () => { globalThis.fetch = original; };
}

test('AI endpoint rejects missing, blank, non-string, and oversized prompts', async () => {
  withTestKey();
  const cases = [undefined, '', '   ', {}, [], 123, 'x'.repeat(12001)];
  for (const prompt of cases) {
    const res = await invoke(aiHandler, 'POST', { prompt });
    assert.equal(res.statusCode, 400, `prompt case ${String(prompt).slice(0, 20)}`);
  }
});

test('AI endpoint returns 405 for methods other than POST', async () => {
  const res = await invoke(aiHandler, 'GET', {});
  assert.equal(res.statusCode, 405);
});

test('AI endpoint returns 503 when no API key is configured', async () => {
  withNoKeys();
  const res = await invoke(aiHandler, 'POST', { prompt: 'hello' });
  assert.equal(res.statusCode, 503);
});

test('AI endpoint preserves successful string request response', async () => {
  withTestKey();
  const restore = mockFetch({ choices: [{ message: { content: 'hello back' } }] });
  try {
    const res = await invoke(aiHandler, 'POST', { prompt: 'hello' });
    assert.equal(res.statusCode, 200);
    assert.deepEqual(res.body, { ok: true, text: 'hello back', model: 'gpt-4.1-mini' });
  } finally { restore(); }
});

test('translation endpoint rejects missing, blank, non-string, and oversized fields', async () => {
  withTestKey();
  const cases = [
    [{ target: 'English' }], [{ text: 'hello' }], [{ text: ' ', target: 'English' }],
    [{ text: 'hello', target: ' ' }], [{ text: {}, target: 'English' }],
    [{ text: [], target: 'English' }], [{ text: 123, target: 'English' }],
    [{ text: 'hello', target: {} }], [{ text: 'hello', target: [] }],
    [{ text: 'hello', target: 123 }], [{ text: 'x'.repeat(12001), target: 'English' }],
    [{ text: 'hello', target: 'x'.repeat(81) }],
  ];
  for (const [body] of cases) {
    const res = await invoke(translateHandler, 'POST', body);
    assert.equal(res.statusCode, 400, JSON.stringify(body).slice(0, 40));
  }
});

test('translation endpoint returns 405 for methods other than POST', async () => {
  const res = await invoke(translateHandler, 'GET', {});
  assert.equal(res.statusCode, 405);
});

test('translation endpoint returns 503 when no API key is configured', async () => {
  withNoKeys();
  const res = await invoke(translateHandler, 'POST', { text: 'hello', target: 'English' });
  assert.equal(res.statusCode, 503);
});

test('translation endpoint preserves successful string request response', async () => {
  withTestKey();
  const restore = mockFetch({ choices: [{ message: { content: 'bonjour' } }] });
  try {
    const res = await invoke(translateHandler, 'POST', { text: 'hello', target: 'French' });
    assert.equal(res.statusCode, 200);
    assert.deepEqual(res.body, { ok: true, translation: 'bonjour' });
  } finally { restore(); }
});
