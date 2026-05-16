# SpringFood Scripts

Utility scripts for SpringFood project.

## Upload Placeholder Images

Script to generate and upload placeholder images to MinIO (Cloudflare R2).

### Prerequisites

- Node.js installed
- MinIO credentials configured in `.env` file

### Usage

```bash
cd scripts
npm install
npm run upload-placeholders
```

### Output

The script will:
1. Generate SVG placeholder images for products and shops
2. Upload them to MinIO bucket under `placeholders/` folder
3. Save the URLs to `placeholder-urls.json`
4. Print the URLs to console

### Example Output

```
🚀 Generating and uploading placeholder images...

✅ Uploaded: placeholders/product-placeholder.svg
   URL: https://338316a4fb54dcea0475e6749b7a79a7.r2.cloudflarestorage.com/test/placeholders/product-placeholder.svg

✅ Uploaded: placeholders/shop-placeholder.svg
   URL: https://338316a4fb54dcea0475e6749b7a79a7.r2.cloudflarestorage.com/test/placeholders/shop-placeholder.svg

✨ Done! Update your frontend with these URLs:

Product Placeholder: https://...
Shop Placeholder: https://...

📝 URLs saved to scripts/placeholder-urls.json
```

### Next Steps

After running the script, update the frontend environment configuration with the generated URLs.
