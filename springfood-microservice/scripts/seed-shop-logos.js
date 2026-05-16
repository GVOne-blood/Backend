/**
 * Seed real shop logos.
 *
 * Strategy (3-tier):
 *   1. If shop is mapped to a brand domain → fetch from logo.dev (high-quality PNG, ~40-80 KB)
 *      Falls back to Google S2 favicon if logo.dev gives <2 KB blank/error.
 *   2. If shop is local/generic (e.g., "Phở Hà Nội 24h") → search Pexels for relevant
 *      restaurant/food image (e.g., "vietnamese pho restaurant", "vietnamese coffee shop interior")
 *      and use that as the logo. This makes each shop visually distinct.
 *   3. If everything fails → use SpringFood brand logo we uploaded earlier.
 */
const AWS = require('aws-sdk');
const fs = require('fs');
const path = require('path');
const { Client } = require('pg');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

const PEXELS_KEY = process.env.PEXELS_API_KEY;
const R2_PUBLIC_BASE = process.env.R2_PUBLIC_BASE_URL;
const R2_BUCKET = process.env.R2_BUCKET || 'springfood-media';
const SPRINGFOOD_LOGO = `${R2_PUBLIC_BASE}/brand/springfood-logo-512.png`;
const LOGO_DEV_TOKEN = 'pk_X-1ZO13GSgeOoUrIuJ6GMQ'; // public free-tier token

const s3 = new AWS.S3({
  endpoint: process.env.MINIO_ENDPOINT,
  accessKeyId: process.env.MINIO_ACCESS_KEY,
  secretAccessKey: process.env.MINIO_SECRET_KEY,
  s3ForcePathStyle: true, signatureVersion: 'v4', region: 'auto',
});

// Brand mapping: shop_name → known domain
const BRAND_DOMAIN = {
  'Gong Cha':                'gong-cha.com',
  'Highlands Coffee':        'highlandscoffee.com.vn',
  'Phúc Long Coffee & Tea':  'phuclong.com.vn',
  'The Coffee House':        'thecoffeehouse.com',
  'Ding Tea':                'dingteavietnam.com',
  'Mixue':                   'mixueicecream.com',
  'TocoToco':                'tocotocotea.com',
  'Cà Phê Cộng':             'congcaphe.com',
  'Kem Tràng Tiền':          'kemtrangtien.vn',
};

// For local-style shops, what Pexels query represents them best
const LOCAL_SHOP_QUERY = {
  'BBQ Garden':               'vietnamese bbq grill restaurant',
  'Bánh Mì Hòa Mã':           'vietnamese banh mi street food',
  'Bánh Xèo Miền Tây':        'vietnamese banh xeo pancake',
  'Bún Bò Huế Mẹ Liên':       'bun bo hue vietnamese',
  'Bún Chả Hà Nội':           'bun cha hanoi grilled pork',
  'Cháo Lòng Bà Hoa':         'vietnamese rice porridge restaurant',
  'Chè Thái Lan':             'thai dessert sweet bowl',
  'Cơm Gà Xối Mỡ Hội An':     'vietnamese hoi an chicken rice',
  'Cơm Niêu Singapore':       'asian clay pot rice',
  'Cơm Tấm Sài Gòn':          'com tam vietnamese broken rice',
  'Hải Sản Nướng Biển Đông':  'grilled seafood vietnam',
  'Lẩu Thái Hải Sản':         'thai seafood hotpot',
  'Nem Nướng Nha Trang':      'nem nuong vietnamese grill',
  'Phở Bò Tái Lăn':           'vietnamese pho beef restaurant',
  'Phở Gà Tân Định':          'vietnamese chicken pho',
  'Phở Hà Nội 24h':           'pho hanoi vietnam restaurant',
  'Quán Nướng Sài Gòn':       'vietnamese grill restaurant',
  'Xôi Xéo Hà Nội':           'vietnamese sticky rice xoi',
};

function buildConnString() {
  const jdbc = process.env.DEFAULT_DATABASE_URL.replace(/^jdbc:/, '');
  return jdbc.replace('postgresql://',
    `postgresql://${encodeURIComponent(process.env.DEFAULT_DATABASE_USERNAME)}:${encodeURIComponent(process.env.DEFAULT_DATABASE_PASSWORD)}@`);
}

async function fetchBuf(url) {
  const r = await fetch(url, { redirect: 'follow' });
  const buf = Buffer.from(await r.arrayBuffer());
  return { ok: r.ok, status: r.status, buf, contentType: r.headers.get('content-type') || 'image/png' };
}

async function uploadR2(key, buf, ct) {
  await s3.putObject({
    Bucket: R2_BUCKET, Key: key, Body: buf, ContentType: ct,
    CacheControl: 'public, max-age=31536000, immutable',
  }).promise();
  return `${R2_PUBLIC_BASE}/${key}`;
}

async function getBrandLogo(domain) {
  // Tier 1a: logo.dev (high-quality)
  try {
    const r = await fetchBuf(`https://img.logo.dev/${domain}?token=${LOGO_DEV_TOKEN}&size=256&format=png`);
    if (r.ok && r.buf.length >= 5000) return { source: 'logo.dev', buf: r.buf, ct: r.contentType };
  } catch (_) {}
  // Tier 1b: Google favicon (always works, lower quality)
  try {
    const r = await fetchBuf(`https://www.google.com/s2/favicons?domain=${domain}&sz=128`);
    if (r.ok && r.buf.length >= 1500) return { source: 'google-s2', buf: r.buf, ct: r.contentType };
  } catch (_) {}
  return null;
}

async function pexelsFirstPhoto(query) {
  const url = `https://api.pexels.com/v1/search?query=${encodeURIComponent(query)}&per_page=1&orientation=landscape`;
  const r = await fetch(url, { headers: { Authorization: PEXELS_KEY } });
  if (!r.ok) return null;
  const j = await r.json();
  const p = (j.photos || [])[0];
  if (!p) return null;
  const dl = await fetchBuf(p.src.large);
  if (!dl.ok) return null;
  return { source: 'pexels', buf: dl.buf, ct: dl.contentType };
}

async function main() {
  const input = JSON.parse(fs.readFileSync(path.join(__dirname, '.seed-input.json'), 'utf8'));
  const shops = input.shops;

  console.log(`Processing ${shops.length} shops`);
  const updates = [];

  for (const shop of shops) {
    let result = null;
    const domain = BRAND_DOMAIN[shop.shop_name];

    if (domain) {
      result = await getBrandLogo(domain);
      if (result) {
        const ext = result.ct.includes('jpeg') ? 'jpg' : (result.ct.includes('x-icon') ? 'ico' : 'png');
        const key = `seed/shops/${shop.shop_id}.${ext}`;
        const url = await uploadR2(key, result.buf, result.ct);
        updates.push({ ...shop, url, kind: result.source });
        console.log(`[${result.source.padEnd(10)}] ${shop.shop_name.padEnd(28)} (${domain})  ${(result.buf.length/1024).toFixed(1)} KB`);
        continue;
      }
    }

    const q = LOCAL_SHOP_QUERY[shop.shop_name];
    if (q) {
      try {
        result = await pexelsFirstPhoto(q);
      } catch (e) {
        console.warn(`  Pexels fail for "${shop.shop_name}": ${e.message}`);
      }
      if (result) {
        const key = `seed/shops/${shop.shop_id}.jpg`;
        const url = await uploadR2(key, result.buf, result.ct);
        updates.push({ ...shop, url, kind: 'pexels' });
        console.log(`[pexels    ] ${shop.shop_name.padEnd(28)} q="${q}"  ${(result.buf.length/1024).toFixed(1)} KB`);
        continue;
      }
    }

    // Final fallback
    updates.push({ ...shop, url: SPRINGFOOD_LOGO, kind: 'springfood-fallback' });
    console.log(`[fallback  ] ${shop.shop_name}`);
  }

  // Apply
  console.log('\nApplying SQL...');
  const client = new Client({ connectionString: buildConnString() });
  await client.connect();
  try {
    await client.query('BEGIN');
    for (const u of updates) {
      await client.query(
        `UPDATE springfood_shop.shops SET logo = $1, updated_at = NOW() WHERE shop_id = $2`,
        [u.url, u.shop_id]
      );
    }
    await client.query('COMMIT');
    console.log(`[OK] ${updates.length} shops updated\n`);

    const v = await client.query(`
      SELECT
        COUNT(*) FILTER (WHERE logo LIKE '%/brand/springfood-logo%') AS uses_fallback,
        COUNT(*) FILTER (WHERE logo LIKE '%/seed/shops/%') AS uses_seed,
        COUNT(*) AS total
      FROM springfood_shop.shops;
    `);
    console.log('Verification:');
    console.table(v.rows);
  } catch (e) {
    await client.query('ROLLBACK');
    throw e;
  } finally {
    await client.end();
  }

  fs.writeFileSync(path.join(__dirname, 'seed-shop-logos-mapping.json'), JSON.stringify(updates, null, 2));
}

main().catch(e => { console.error(e); process.exit(1); });
