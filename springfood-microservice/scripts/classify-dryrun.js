/**
 * Dry-run: classify only, no API calls. Verify no products fall to fallback.
 */
const fs = require('fs');
const path = require('path');

// Re-import the classify logic by extracting it.
// Easiest: require the seed script's module — but it auto-runs main().
// So inline a copy of CATEGORIES list here from the same source.
const src = fs.readFileSync(path.join(__dirname, 'seed-product-images.js'), 'utf8');

// Hacky but isolated: eval the CATEGORIES array literal.
const m = src.match(/const CATEGORIES = (\[[\s\S]*?\n\]);/);
if (!m) { console.error('Could not extract CATEGORIES'); process.exit(1); }
const CATEGORIES = eval(m[1]);

const FALLBACK = { slug: 'vn-food', query: 'vietnamese food cuisine' };

function classify(name) {
  for (const c of CATEGORIES) if (c.match(name)) return c;
  return FALLBACK;
}

const input = JSON.parse(fs.readFileSync(path.join(__dirname, '.seed-input.json'), 'utf8'));
const groups = new Map();
for (const p of input.products) {
  const c = classify(p.name);
  if (!groups.has(c.slug)) groups.set(c.slug, { ...c, products: [] });
  groups.get(c.slug).products.push(p.name);
}

console.log('Total products:', input.products.length);
console.log(`Categories: ${groups.size}\n`);

const sorted = [...groups.entries()].sort((a, b) => b[1].products.length - a[1].products.length);
for (const [slug, g] of sorted) {
  console.log(`[${slug}] (${g.products.length}): ${g.products.join(', ')}`);
}

const fallback = groups.get('vn-food');
if (fallback) {
  console.log(`\nWARNING: ${fallback.products.length} products in fallback`);
  process.exit(2);
}
console.log('\nOK: no fallback hits');
