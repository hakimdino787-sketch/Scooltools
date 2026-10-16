const fs = require("node:fs");
const vm = require("node:vm");

const html = fs.readFileSync("index.html", "utf8");
const scripts = [...html.matchAll(/<script\b([^>]*)>([\s\S]*?)<\/script\s*>/gi)];
let checked = 0;
const errors = [];

for (let i = 0; i < scripts.length; i++) {
  const attrs = scripts[i][1] || "";
  const code = scripts[i][2] || "";
  const type = (attrs.match(/\btype\s*=\s*["']([^"']+)["']/i) || [])[1] || "";
  if (!code.trim() || /application\/ld\+json/i.test(type)) continue;
  try {
    new vm.Script(code, { filename: `index.html:inline-script-${i + 1}` });
    checked++;
  } catch (error) {
    errors.push(`inline script ${i + 1}: ${error.message}`);
  }
}

if (errors.length) {
  console.error("Inline JavaScript syntax check failed:");
  for (const error of errors) console.error(" - " + error);
  process.exit(1);
}
console.log(`Inline JavaScript syntax OK (${checked} scripts checked).`);

const ids = new Set([...html.matchAll(/\bid\s*=\s*["']([^"']+)["']/gi)].map(m => m[1]));
const refs = new Set([...html.matchAll(/getElementById\(\s*["']([^"']+)["']\s*\)/g)].map(m => m[1]));
const missing = [...refs].filter(id => !ids.has(id));
if (missing.length) {
  console.warn("Review possible missing static element IDs: " + missing.join(", "));
}
