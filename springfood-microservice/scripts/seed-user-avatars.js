/**
 * Seed default avatars for users.
 *
 * Steps:
 *  1. Generate 24 avatar SVGs from DiceBear (2 styles × 12 seeds)
 *  2. Upload them to R2 under avatars/preset/<style>/<seed>.svg
 *  3. Write manifest avatars/preset/manifest.json (for frontend dynamic fetch)
 *  4. Randomly assign one avatar per user (deterministic by user_id hash for repeatable runs)
 *  5. Save URL list to seed-avatar-urls.json for frontend hardcoded import
 *
 * Run: node seed-user-avatars.js
 */
const AWS = require('aws-sdk');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { Client } = require('pg');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

const R2_PUBLIC_BASE = process.env.R2_PUBLIC_BASE_URL;
const R2_BUCKET = process.env.R2_BUCKET || 'springfood-media';

const STYLES = [
  {
    style: 'avataaars',
    seeds: ['Felix', 'Aneka', 'Luna', 'Max', 'Bella', 'Charlie',
            'Lucy', 'Oliver', 'Milo', 'Daisy', 'Leo', 'Sophie'],
  },
  {
    style: 'lorelei',
    seeds: ['Mira', 'Kai', 'Rose', 'Linh', 'Tuan', 'Mai',
            'An', 'Hoa', 'Nam', 'Hieu', 'Trang', 'Phuc'],
  },
];

const s3 = new AWS.S3({
  endpoint: process.env.MINIO_ENDPOINT,
  accessKeyId: process.env.MINIO_ACCESS_KEY,
  secretAccessKey: process.env.MINIO_SECRET_KEY,
  s3ForcePathStyle: true, signatureVersion: 'v4', region: 'auto',
});

function buildConnString() {
  const jdbc = process.env.DEFAULT_DATABASE_URL.replace(/^jdbc:/, '');
  return jdbc.replace('postgresql://',
    `postgresql://${encodeURIComponent(process.env.DEFAULT_DATABASE_USERNAME)}:${encodeURIComponent(process.env.DEFAULT_DATABASE_PASSWORD)}@`);
}

async function fetchDicebear(style, seed) {
  const url = `https://api.dicebear.com/9.x/${style}/svg?seed=${encodeURIComponent(seed)}&radius=50&backgroundType=gradientLinear,solid&size=256`;
  const r = await fetch(url);
  if (!r.ok) throw new Error(`DiceBear ${r.status} for ${style}/${seed}`);
  return Buffer.from(await r.arrayBuffer());
}

async function uploadR2(key, buf) {
  await s3.putObject({
    Bucket: R2_BUCKET, Key: key, Body: buf,
    ContentType: 'image/svg+xml',
    CacheControl: 'public, max-age=31536000, immutable',
  }).promise();
  return `${R2_PUBLIC_BASE}/${key}`;
}

// Deterministic int from string (for stable user → avatar mapping)
function hashInt(s) {
  return parseInt(crypto.createHash('md5').update(s).digest('hex').slice(0, 8), 16);
}

async function main() {
  console.log('=== 1. Generate & upload 24 preset avatars ===');
  const avatars = [];

  for (const { style, seeds } of STYLES) {
    for (const seed of seeds) {
      try {
        const buf = await fetchDicebear(style, seed);
        const key = `avatars/preset/${style}/${seed.toLowerCase()}.svg`;
        const url = await uploadR2(key, buf);
        avatars.push({ style, seed, url, sizeBytes: buf.length });
        console.log(`  [${style.padEnd(10)}] ${seed.padEnd(10)} ${(buf.length / 1024).toFixed(1).padStart(5)} KB`);
      } catch (e) {
        console.warn(`  FAIL ${style}/${seed}: ${e.message}`);
      }
    }
  }

  console.log(`\n[OK] ${avatars.length} avatars on R2`);

  // Manifest
  const manifest = {
    generatedAt: new Date().toISOString(),
    bucket: R2_BUCKET,
    publicBase: R2_PUBLIC_BASE,
    count: avatars.length,
    avatars: avatars.map(a => ({ style: a.style, seed: a.seed, url: a.url })),
  };
  const manifestKey = 'avatars/preset/manifest.json';
  await s3.putObject({
    Bucket: R2_BUCKET, Key: manifestKey,
    Body: Buffer.from(JSON.stringify(manifest, null, 2)),
    ContentType: 'application/json',
    CacheControl: 'public, max-age=300',
  }).promise();
  const manifestUrl = `${R2_PUBLIC_BASE}/${manifestKey}`;
  console.log(`Manifest: ${manifestUrl}`);

  // Local copy for frontend hardcoded import
  fs.writeFileSync(path.join(__dirname, 'seed-avatar-urls.json'), JSON.stringify(manifest, null, 2));
  console.log(`Local copy: scripts/seed-avatar-urls.json`);

  // === 2. Assign avatars to users ===
  console.log('\n=== 2. Assign avatars to users ===');
  if (avatars.length === 0) {
    console.error('No avatars to assign');
    return;
  }

  const client = new Client({ connectionString: buildConnString() });
  await client.connect();
  try {
    const users = (await client.query(
      `SELECT user_id FROM springfood_authentication."user" ORDER BY user_id`
    )).rows;
    console.log(`Found ${users.length} users`);

    await client.query('BEGIN');
    for (const u of users) {
      const idx = hashInt(u.user_id) % avatars.length;
      const url = avatars[idx].url;
      await client.query(
        `UPDATE springfood_authentication."user" SET avatar = $1, updated_at = NOW() WHERE user_id = $2`,
        [url, u.user_id]
      );
    }
    await client.query('COMMIT');
    console.log(`[OK] ${users.length} users updated`);

    const v = await client.query(`
      SELECT
        COUNT(*) FILTER (WHERE avatar LIKE '%pravatar%' OR avatar LIKE '%ui-avatars%' OR avatar LIKE '%placeholder%') AS placeholder,
        COUNT(*) FILTER (WHERE avatar LIKE '%/avatars/preset/%') AS r2_preset,
        COUNT(*) AS total
      FROM springfood_authentication."user";
    `);
    console.log('\nVerification:');
    console.table(v.rows);
  } catch (e) {
    await client.query('ROLLBACK');
    console.error('[FAIL]', e.message);
    throw e;
  } finally {
    await client.end();
  }
}

main().catch(e => { console.error(e); process.exit(1); });
