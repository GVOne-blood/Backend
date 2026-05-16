# Placeholder Images Setup

Hướng dẫn tạo và upload ảnh placeholder lên MinIO.

## Bước 1: Cài đặt dependencies

```bash
cd springfood-microservice/scripts
npm install
```

## Bước 2: Chạy script upload

```bash
npm run upload-placeholders
```

Script sẽ:
1. Tạo 2 file SVG placeholder:
   - `product-placeholder.svg` (600x600px) - cho sản phẩm
   - `shop-placeholder.svg` (400x300px) - cho shop logo
2. Upload lên MinIO bucket `test` trong folder `placeholders/`
3. In ra URLs và lưu vào `placeholder-urls.json`

## Bước 3: Cập nhật frontend (đã làm sẵn)

Frontend đã được cập nhật để sử dụng placeholder URLs từ `environment.ts`:

```typescript
// springfood/src/environments/environment.ts
export const environment = {
  // ...
  placeholders: {
    product: 'https://...r2.cloudflarestorage.com/test/placeholders/product-placeholder.svg',
    shop: 'https://...r2.cloudflarestorage.com/test/placeholders/shop-placeholder.svg',
  },
};
```

## Các component đã được cập nhật:

- ✅ `ProductItemComponent` - Dùng `environment.placeholders.product` khi ảnh lỗi
- ✅ `ProductService.getFirstImage()` - Trả về placeholder nếu không có ảnh
- ✅ `ShopService.getShopLogo()` - Trả về placeholder nếu shop không có logo
- ✅ `FeaturedProductsComponent` - Dùng placeholder khi map data

## Lưu ý:

- Placeholder images được host trên Cloudflare R2 (MinIO compatible)
- URLs có thể truy cập public
- Nếu cần thay đổi design placeholder, chỉnh sửa hàm `generateSVGPlaceholder()` trong script
- Sau khi upload, URLs sẽ được lưu trong `scripts/placeholder-urls.json`

## Troubleshooting:

**Lỗi: "Access Denied"**
- Kiểm tra MinIO credentials trong `.env`
- Đảm bảo bucket `test` tồn tại và có quyền public-read

**Lỗi: "Cannot find module 'aws-sdk'"**
- Chạy `npm install` trong folder `scripts/`

**Muốn thay đổi design placeholder:**
- Chỉnh sửa hàm `generateSVGPlaceholder()` trong `upload-placeholder-images.js`
- Chạy lại `npm run upload-placeholders`
- Cập nhật URLs trong `environment.ts`
