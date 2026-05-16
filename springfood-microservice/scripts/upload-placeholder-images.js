/**
 * Script to generate and upload placeholder images to MinIO (Cloudflare R2)
 * 
 * Usage: node upload-placeholder-images.js
 */

const AWS = require('aws-sdk');
const fs = require('fs');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

// MinIO/R2 Configuration
const s3 = new AWS.S3({
  endpoint: process.env.MINIO_ENDPOINT,
  accessKeyId: process.env.MINIO_ACCESS_KEY,
  secretAccessKey: process.env.MINIO_SECRET_KEY,
  s3ForcePathStyle: true,
  signatureVersion: 'v4',
});

const BUCKET = process.env.MINIO_BUCKET || 'test';

/**
 * Generate SVG placeholder
 */
function generateSVGPlaceholder(width, height, text, bgColor, textColor) {
  return `<svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
  <rect width="100%" height="100%" fill="${bgColor}"/>
  <text x="50%" y="50%" font-family="Arial, sans-serif" font-size="48" font-weight="bold" 
        fill="${textColor}" text-anchor="middle" dominant-baseline="middle">${text}</text>
</svg>`;
}

/**
 * Upload to MinIO
 */
async function uploadToMinIO(content, key, contentType) {
  const params = {
    Bucket: BUCKET,
    Key: key,
    Body: Buffer.from(content),
    ContentType: contentType,
    ACL: 'public-read',
  };

  try {
    const result = await s3.upload(params).promise();
    console.log(`✅ Uploaded: ${key}`);
    console.log(`   URL: ${result.Location}`);
    return result.Location;
  } catch (error) {
    console.error(`❌ Failed to upload ${key}:`, error.message);
    throw error;
  }
}

/**
 * Main function
 */
async function main() {
  console.log('🚀 Generating and uploading placeholder images...\n');

  // Generate SVG placeholders
  const productSVG = generateSVGPlaceholder(
    600, 600,
    'No Image',
    '#f3f4f6',
    '#9ca3af'
  );

  const shopSVG = generateSVGPlaceholder(
    400, 300,
    'No Logo',
    '#e5e7eb',
    '#6b7280'
  );

  // Upload to MinIO
  const productUrl = await uploadToMinIO(
    productSVG,
    'placeholders/product-placeholder.svg',
    'image/svg+xml'
  );

  const shopUrl = await uploadToMinIO(
    shopSVG,
    'placeholders/shop-placeholder.svg',
    'image/svg+xml'
  );

  console.log('\n✨ Done! Update your frontend with these URLs:');
  console.log(`\nProduct Placeholder: ${productUrl}`);
  console.log(`Shop Placeholder: ${shopUrl}`);

  // Save URLs to a config file
  const config = {
    productPlaceholder: productUrl,
    shopPlaceholder: shopUrl,
    generatedAt: new Date().toISOString(),
  };

  fs.writeFileSync(
    path.join(__dirname, 'placeholder-urls.json'),
    JSON.stringify(config, null, 2)
  );

  console.log('\n📝 URLs saved to scripts/placeholder-urls.json');
}

main().catch(console.error);
