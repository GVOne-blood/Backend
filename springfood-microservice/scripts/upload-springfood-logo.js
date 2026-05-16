/**
 * Upload SpringFood brand assets (logo / favicons) from frontend/public to R2.
 * Run: node upload-springfood-logo.js
 */
const AWS = require('aws-sdk');
const fs = require('fs');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

const BUCKET = 'springfood-media';
const PUBLIC_BASE = 'https://pub-db7036086154479380d282adb29af0a4.r2.dev';

// Frontend public folder (relative to repo root parent)
const FRONTEND_PUBLIC = path.resolve(
  __dirname,
  '..', '..', '..',
  'Frontend', 'Springfood-frontend', 'springfood', 'public'
);

// Source filename → R2 key + content type
const ASSETS = [
  { src: 'android-chrome-512x512.png', key: 'brand/springfood-logo-512.png',  type: 'image/png' },
  { src: 'android-chrome-192x192.png', key: 'brand/springfood-logo-192.png',  type: 'image/png' },
  { src: 'apple-touch-icon.png',       key: 'brand/springfood-apple-touch.png', type: 'image/png' },
  { src: 'favicon-32x32.png',          key: 'brand/springfood-favicon-32.png',  type: 'image/png' },
  { src: 'favicon-16x16.png',          key: 'brand/springfood-favicon-16.png',  type: 'image/png' },
  { src: 'favicon.ico',                key: 'brand/springfood-favicon.ico',     type: 'image/x-icon' },
];

const s3 = new AWS.S3({
  endpoint: process.env.MINIO_ENDPOINT,
  accessKeyId: process.env.MINIO_ACCESS_KEY,
  secretAccessKey: process.env.MINIO_SECRET_KEY,
  s3ForcePathStyle: true,
  signatureVersion: 'v4',
  region: 'auto',
});

async function uploadOne({ src, key, type }) {
  const filePath = path.join(FRONTEND_PUBLIC, src);
  if (!fs.existsSync(filePath)) {
    console.log(`[SKIP] ${src} not found at ${filePath}`);
    return null;
  }

  const body = fs.readFileSync(filePath);
  await s3.putObject({
    Bucket: BUCKET,
    Key: key,
    Body: body,
    ContentType: type,
    CacheControl: 'public, max-age=31536000, immutable',
  }).promise();

  const url = `${PUBLIC_BASE}/${key}`;
  console.log(`[OK] ${src.padEnd(28)} -> ${url}  (${body.length} B)`);
  return { src, key, url, size: body.length };
}

async function main() {
  console.log(`Source : ${FRONTEND_PUBLIC}`);
  console.log(`Bucket : ${BUCKET}\n`);

  const results = [];
  for (const a of ASSETS) {
    try {
      const r = await uploadOne(a);
      if (r) results.push(r);
    } catch (e) {
      console.error(`[FAIL] ${a.src}: ${e.code} ${e.message}`);
    }
  }

  // Verify the main logo is reachable
  const main = results.find(r => r.key === 'brand/springfood-logo-512.png');
  if (main) {
    try {
      const res = await fetch(main.url);
      console.log(`\nVerify ${main.url}: HTTP ${res.status}, ${res.headers.get('content-type')}, ${res.headers.get('content-length')} B`);
    } catch (e) {
      console.error('verify failed:', e.message);
    }
  }

  // Save summary
  const out = path.join(__dirname, 'springfood-brand-urls.json');
  fs.writeFileSync(out, JSON.stringify({
    bucket: BUCKET,
    publicBase: PUBLIC_BASE,
    uploadedAt: new Date().toISOString(),
    assets: results,
  }, null, 2));
  console.log(`\nSummary saved to ${out}`);
}

main().catch(e => { console.error(e); process.exit(1); });
