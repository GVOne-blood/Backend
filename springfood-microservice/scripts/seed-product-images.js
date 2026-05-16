/**
 * Seed real food images for all products.
 *
 * Pipeline:
 *  1. Classify each product into a category by keyword on name
 *  2. For each category: search Pexels (VN-specific query), download top-3 photos, upload to R2
 *  3. Build SQL UPDATEs that set products.images = jsonb of 1-3 R2 URLs (round-robin from pool)
 *  4. Print SQL to stdout AND save to seed-product-images.sql for review
 *
 * Run: node seed-product-images.js
 */
const AWS = require('aws-sdk');
const fs = require('fs');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

const PEXELS_KEY = process.env.PEXELS_API_KEY;
const R2_PUBLIC_BASE = process.env.R2_PUBLIC_BASE_URL || 'https://pub-db7036086154479380d282adb29af0a4.r2.dev';
const R2_BUCKET = process.env.R2_BUCKET || 'springfood-media';

if (!PEXELS_KEY) { console.error('Missing PEXELS_API_KEY'); process.exit(1); }

const s3 = new AWS.S3({
  endpoint: process.env.MINIO_ENDPOINT,
  accessKeyId: process.env.MINIO_ACCESS_KEY,
  secretAccessKey: process.env.MINIO_SECRET_KEY,
  s3ForcePathStyle: true,
  signatureVersion: 'v4',
  region: 'auto',
});

// ─── Category definitions ─────────────────────────────────────────────────────
// Each category: slug, query for Pexels (English-friendly works better),
// match function on Vietnamese product name.
// Order matters — first match wins (so put more-specific BEFORE general).
const CATEGORIES = [
  // Specific subtypes first
  // Order matters — first match wins. Specific subtypes BEFORE generic.
  // NOTE: JS \b word-boundary is ASCII-only; do NOT rely on it for Vietnamese tone-marked
  // characters. Use explicit substring checks; deliberate ordering disambiguates.
  { slug: 'pho-cuon',      query: 'vietnamese fresh spring rolls',        match: n => /phở cuốn/i.test(n) },
  { slug: 'pho-xao',       query: 'stir fried vietnamese noodles',        match: n => /phở xào/i.test(n) },
  { slug: 'pho',           query: 'pho vietnamese beef noodle soup',      match: n => /phở/i.test(n) },
  { slug: 'banh-mi',       query: 'vietnamese banh mi sandwich',          match: n => /bánh mì/i.test(n) },
  { slug: 'banh-bao',      query: 'chinese steamed bun bao',              match: n => /bánh bao/i.test(n) },
  { slug: 'banh-trang',    query: 'vietnamese rice paper salad',          match: n => /bánh tráng/i.test(n) },
  { slug: 'bun-bo-hue',    query: 'bun bo hue vietnamese spicy noodle',   match: n => /bún bò huế/i.test(n) },
  { slug: 'bun-cha',       query: 'bun cha hanoi grilled pork',           match: n => /bún chả/i.test(n) },
  { slug: 'bun-thit-nuong',query: 'vietnamese grilled pork vermicelli',   match: n => /bún thịt nướng/i.test(n) },
  { slug: 'bun-dau',       query: 'bun dau mam tom vietnamese',           match: n => /bún đậu/i.test(n) },
  { slug: 'bun',           query: 'vietnamese rice vermicelli noodle bowl', match: n => /bún/i.test(n) },
  { slug: 'com-tam',       query: 'com tam broken rice vietnamese',       match: n => /cơm tấm|cơm sườn/i.test(n) },
  { slug: 'com-chien',     query: 'fried rice asian',                     match: n => /cơm chiên|cơm rang/i.test(n) },
  { slug: 'com-ga',        query: 'vietnamese chicken rice',              match: n => /cơm gà/i.test(n) },
  { slug: 'com',           query: 'vietnamese rice plate',                match: n => /cơm/i.test(n) },
  { slug: 'pizza',         query: 'pizza',                                match: n => /pizza/i.test(n) },
  { slug: 'burger',        query: 'cheeseburger',                         match: n => /burger/i.test(n) },
  { slug: 'ga-ran',        query: 'fried chicken crispy',                 match: n => /gà rán/i.test(n) },
  { slug: 'khoai-tay',     query: 'french fries',                         match: n => /khoai tây/i.test(n) },
  { slug: 'ha-cao',        query: 'dim sum dumpling',                     match: n => /há cảo|dimsum/i.test(n) },
  { slug: 'xiu-mai',       query: 'siu mai dim sum',                      match: n => /xíu mại/i.test(n) },
  { slug: 'lau',           query: 'vietnamese hotpot',                    match: n => /lẩu/i.test(n) },
  { slug: 'bbq',           query: 'grilled bbq pork ribs',                match: n => /sườn|nướng bbq|ba chỉ nướng|thịt xiên/i.test(n) },
  { slug: 'nem-nuong',     query: 'nem nuong vietnamese grilled pork',    match: n => /nem nướng/i.test(n) },
  { slug: 'nem-cuon',      query: 'fresh spring roll vietnamese',         match: n => /nem cuốn/i.test(n) },
  { slug: 'nem-lui',       query: 'vietnamese skewered pork',             match: n => /nem lui/i.test(n) },
  { slug: 'cha-gio',       query: 'fried spring roll vietnamese',         match: n => /chả giò|nem rán|chả ram|nem chua rán/i.test(n) },
  { slug: 'cha',           query: 'vietnamese pork sausage cha lua',      match: n => /chả cá|chả lụa/i.test(n) },
  { slug: 'tom',           query: 'grilled shrimp seafood',               match: n => /tôm/i.test(n) },
  { slug: 'cua-ghe',       query: 'crab seafood vietnamese',              match: n => /cua|ghẹ/i.test(n) },
  { slug: 'muc',           query: 'grilled squid',                        match: n => /mực/i.test(n) },
  { slug: 'so-diep',       query: 'grilled scallop cheese',               match: n => /sò điệp/i.test(n) },
  { slug: 'ngheu',         query: 'steamed clam seafood',                 match: n => /nghêu/i.test(n) },
  { slug: 'oc',            query: 'vietnamese snail dish',                match: n => /^ốc|\sốc/i.test(n) },
  { slug: 'bach-tuoc',     query: 'grilled octopus seafood',              match: n => /bạch tuộc/i.test(n) },
  { slug: 'ca',            query: 'grilled fish seafood vietnamese',      match: n => /^cá\s|\scá\s/i.test(n) },
  { slug: 'chao',          query: 'congee rice porridge vietnamese',      match: n => /cháo/i.test(n) },
  { slug: 'xoi',           query: 'sticky rice xoi vietnamese',           match: n => /xôi/i.test(n) },
  { slug: 'che',           query: 'vietnamese sweet dessert che',         match: n => /chè/i.test(n) },
  { slug: 'tra-sua',       query: 'bubble milk tea boba',                 match: n => /trà sữa/i.test(n) },
  { slug: 'tra',           query: 'iced fruit tea drink',                 match: n => /^trà\s|\strà\s|^trà$/i.test(n) },
  { slug: 'cf-sua-da',     query: 'vietnamese iced milk coffee',          match: n => /cà phê sữa đá|cà phê đen đá|bạc xỉu/i.test(n) },
  { slug: 'cf-trung',      query: 'vietnamese egg coffee',                match: n => /cà phê trứng/i.test(n) },
  { slug: 'cf-dua',        query: 'coconut coffee',                       match: n => /cà phê dừa/i.test(n) },
  { slug: 'ca-phe',        query: 'vietnamese coffee shop',               match: n => /cà phê/i.test(n) },
  { slug: 'espresso',      query: 'espresso coffee shot',                 match: n => /espresso/i.test(n) },
  { slug: 'cappuccino',    query: 'cappuccino latte art',                 match: n => /cappuccino/i.test(n) },
  { slug: 'latte',         query: 'caffe latte coffee',                   match: n => /^latte$/i.test(n) },
  { slug: 'sinh-to',       query: 'fruit smoothie tropical',              match: n => /sinh tố/i.test(n) },
  { slug: 'nuoc-ep',       query: 'fresh fruit juice glass',              match: n => /nước ép/i.test(n) },
];

const FALLBACK = { slug: 'vn-food', query: 'vietnamese food cuisine' };

// ─── Helpers ─────────────────────────────────────────────────────────────────
function classify(name) {
  for (const c of CATEGORIES) if (c.match(name)) return c;
  return FALLBACK;
}

async function pexelsSearch(query, perPage = 4) {
  const url = `https://api.pexels.com/v1/search?query=${encodeURIComponent(query)}&per_page=${perPage}&orientation=landscape`;
  const res = await fetch(url, { headers: { Authorization: PEXELS_KEY } });
  if (!res.ok) throw new Error(`Pexels ${res.status}: ${await res.text()}`);
  const j = await res.json();
  return (j.photos || []).map(p => ({ id: p.id, alt: p.alt, url: p.src.large }));
}

async function downloadBuffer(url) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`download ${res.status} for ${url}`);
  return Buffer.from(await res.arrayBuffer());
}

async function uploadToR2(key, buf, contentType = 'image/jpeg') {
  await s3.putObject({
    Bucket: R2_BUCKET,
    Key: key,
    Body: buf,
    ContentType: contentType,
    CacheControl: 'public, max-age=31536000, immutable',
  }).promise();
  return `${R2_PUBLIC_BASE}/${key}`;
}

function sqlString(s) { return `'${s.replace(/'/g, "''")}'`; }

// ─── Main ────────────────────────────────────────────────────────────────────
async function main() {
  const input = JSON.parse(fs.readFileSync(path.join(__dirname, '.seed-input.json'), 'utf8'));
  const products = input.products;

  // Group products by category
  const groups = new Map();
  for (const p of products) {
    const c = classify(p.name);
    if (!groups.has(c.slug)) groups.set(c.slug, { ...c, products: [] });
    groups.get(c.slug).products.push(p);
  }

  console.log(`\n=== Category breakdown (${groups.size} categories, ${products.length} products) ===`);
  for (const [slug, g] of groups) {
    console.log(`  ${slug.padEnd(15)} (${String(g.products.length).padStart(3)} products): ${g.query}`);
  }

  // For each category: get pool of 3 photos
  const categoryPool = new Map(); // slug -> [r2Url, r2Url, r2Url]
  for (const [slug, g] of groups) {
    console.log(`\n[${slug}] searching Pexels: "${g.query}"`);
    let photos;
    try {
      photos = await pexelsSearch(g.query, 4);
    } catch (e) {
      console.error(`  Pexels search failed: ${e.message}`);
      continue;
    }
    if (photos.length === 0) {
      console.error(`  No photos returned for "${g.query}"`);
      continue;
    }
    photos = photos.slice(0, 3);
    const urls = [];
    for (let i = 0; i < photos.length; i++) {
      const ph = photos[i];
      const ext = 'jpg';
      const key = `seed/products/${slug}/${i + 1}.${ext}`;
      try {
        const buf = await downloadBuffer(ph.url);
        const r2Url = await uploadToR2(key, buf);
        urls.push(r2Url);
        console.log(`  [${i + 1}/${photos.length}] ${(buf.length / 1024).toFixed(0)} KB -> ${r2Url}`);
      } catch (e) {
        console.error(`  Upload ${key} failed: ${e.message}`);
      }
    }
    categoryPool.set(slug, urls);
    // Light delay to be nice to APIs
    await new Promise(r => setTimeout(r, 250));
  }

  // Build SQL: each product gets 1-3 images via round-robin offset
  // To get variety: product N in category gets pool starting at N % poolSize, plus next 1-2
  const sqlLines = [
    '-- Auto-generated by seed-product-images.js',
    '-- Updates products.images with real Pexels-sourced food images uploaded to Cloudflare R2',
    `-- Generated at: ${new Date().toISOString()}`,
    '',
    'BEGIN;',
    '',
  ];

  let totalUpdated = 0;
  let totalSkipped = 0;

  for (const [slug, g] of groups) {
    const pool = categoryPool.get(slug) || [];
    if (pool.length === 0) {
      console.log(`[${slug}] no pool, skipping ${g.products.length} products`);
      totalSkipped += g.products.length;
      continue;
    }
    sqlLines.push(`-- Category: ${slug} (${g.products.length} products, pool=${pool.length})`);
    g.products.forEach((p, idx) => {
      // count = 1, 2, or 3 based on pool & deterministic variety
      const count = Math.min(pool.length, 1 + (idx % 3));
      const imgs = [];
      for (let k = 0; k < count; k++) {
        imgs.push(pool[(idx + k) % pool.length]);
      }
      const jsonbValue = `'${JSON.stringify(imgs).replace(/'/g, "''")}'::jsonb`;
      sqlLines.push(
        `UPDATE springfood_product.products SET images = ${jsonbValue}, updated_at = NOW() WHERE product_id = '${p.product_id}'; -- ${p.name}`
      );
      totalUpdated++;
    });
    sqlLines.push('');
  }

  sqlLines.push('COMMIT;');
  sqlLines.push(`-- Done: ${totalUpdated} updated, ${totalSkipped} skipped`);

  const outFile = path.join(__dirname, 'seed-product-images.sql');
  fs.writeFileSync(outFile, sqlLines.join('\n'));
  console.log(`\n=== Summary ===`);
  console.log(`Updated: ${totalUpdated}, Skipped: ${totalSkipped}`);
  console.log(`SQL saved: ${outFile}`);
  console.log(`\nNext step: apply this SQL through Neon MCP.`);

  // Save mapping JSON for human review
  const mapping = {};
  for (const [slug, g] of groups) {
    mapping[slug] = {
      query: g.query,
      pool: categoryPool.get(slug) || [],
      productCount: g.products.length,
      products: g.products.map(p => p.name),
    };
  }
  const mappingFile = path.join(__dirname, 'seed-product-images-mapping.json');
  fs.writeFileSync(mappingFile, JSON.stringify(mapping, null, 2));
  console.log(`Mapping saved: ${mappingFile}`);
}

main().catch(e => { console.error(e); process.exit(1); });
