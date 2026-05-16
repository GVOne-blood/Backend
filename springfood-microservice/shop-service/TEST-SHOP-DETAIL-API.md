# Test Shop Detail API

## 🎯 API Endpoint
```
GET http://localhost:8080/shop/{shopId}
```

## 📋 Test Cases

### Test Case 1: Gong Cha (Trà sữa)
```bash
curl -X GET "http://localhost:8080/shop/104b1828-abdd-4afe-a2fa-ac5c93f455f4" \
  -H "Accept: application/json"
```

**Expected Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "shopId": "104b1828-abdd-4afe-a2fa-ac5c93f455f4",
    "shopName": "Gong Cha",
    "logo": "https://ui-avatars.com/api/?name=Gong%20Cha&size=200&background=random",
    "introduction": "Gong Cha - Thương hiệu trà sữa nổi tiếng từ Đài Loan...",
    "shopAddress": "211 Hai Bà Trưng",
    "city": "Quận 1",
    "province": "Hồ Chí Minh",
    "avgStar": 3.93,
    "totalFeedback": null,
    "activeHours": null,
    "distance": 0.0,
    "totalProducts": 0,
    "totalSold": 817,
    "totalOrders": null,
    "phoneNumber": "02546515165",
    "email": "gongcha@springfood.vn",
    "shopStatus": "ACTIVE",
    "isActive": null,
    "shopType": null,
    "businessType": null
  }
}
```

---

### Test Case 2: The Coffee House
```bash
curl -X GET "http://localhost:8080/shop/27797bf3-017e-4416-9f8f-846311a7759a" \
  -H "Accept: application/json"
```

---

### Test Case 3: Highlands Coffee
```bash
curl -X GET "http://localhost:8080/shop/adad75ee-46ea-47b8-9b5b-e23c41955ac9" \
  -H "Accept: application/json"
```

---

### Test Case 4: Phở Hà Nội 24h
```bash
curl -X GET "http://localhost:8080/shop/1af4c735-438e-49db-9876-d6dd8bc41970" \
  -H "Accept: application/json"
```

---

### Test Case 5: Bánh Mì Hòa Mã
```bash
curl -X GET "http://localhost:8080/shop/675dd695-314a-438f-8205-c5afd4866fbd" \
  -H "Accept: application/json"
```

---

### Test Case 6: Invalid Shop ID (404 Error)
```bash
curl -X GET "http://localhost:8080/shop/00000000-0000-0000-0000-000000000000" \
  -H "Accept: application/json"
```

**Expected Response:**
```json
{
  "code": 404,
  "message": "Shop not found with id: 00000000-0000-0000-0000-000000000000",
  "data": null
}
```

---

## 🧪 PowerShell Test Script

Tạo file `test-shop-api.ps1`:

```powershell
# Test Shop Detail API
$baseUrl = "http://localhost:8080/shop"

# Test shops
$shopIds = @(
    "104b1828-abdd-4afe-a2fa-ac5c93f455f4",  # Gong Cha
    "27797bf3-017e-4416-9f8f-846311a7759a",  # The Coffee House
    "adad75ee-46ea-47b8-9b5b-e23c41955ac9",  # Highlands Coffee
    "1af4c735-438e-49db-9876-d6dd8bc41970",  # Phở Hà Nội 24h
    "675dd695-314a-438f-8205-c5afd4866fbd"   # Bánh Mì Hòa Mã
)

Write-Host "=== Testing Shop Detail API ===" -ForegroundColor Cyan
Write-Host ""

foreach ($shopId in $shopIds) {
    Write-Host "Testing Shop ID: $shopId" -ForegroundColor Yellow
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$shopId" -Method Get -ContentType "application/json"
        
        Write-Host "✓ Success!" -ForegroundColor Green
        Write-Host "  Shop Name: $($response.data.shopName)" -ForegroundColor White
        Write-Host "  Address: $($response.data.shopAddress), $($response.data.city)" -ForegroundColor White
        Write-Host "  Rating: $($response.data.avgStar) ⭐" -ForegroundColor White
        Write-Host "  Total Sold: $($response.data.totalSold)" -ForegroundColor White
        Write-Host ""
    }
    catch {
        Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
    }
}

# Test invalid shop ID
Write-Host "Testing Invalid Shop ID..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/00000000-0000-0000-0000-000000000000" -Method Get -ContentType "application/json"
    Write-Host "✗ Should have failed but didn't!" -ForegroundColor Red
}
catch {
    Write-Host "✓ Correctly returned 404 error" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
```

**Run the script:**
```powershell
cd springfood-microservice/shop-service
.\test-shop-api.ps1
```

---

## 🌐 Postman Collection

Import this JSON into Postman:

```json
{
  "info": {
    "name": "Shop Service API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Shop Detail - Gong Cha",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/shop/104b1828-abdd-4afe-a2fa-ac5c93f455f4",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["shop", "104b1828-abdd-4afe-a2fa-ac5c93f455f4"]
        }
      }
    },
    {
      "name": "Get Shop Detail - The Coffee House",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/shop/27797bf3-017e-4416-9f8f-846311a7759a",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["shop", "27797bf3-017e-4416-9f8f-846311a7759a"]
        }
      }
    },
    {
      "name": "Get Featured Shops",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/shop/featured?page=0&size=10",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["shop", "featured"],
          "query": [
            {"key": "page", "value": "0"},
            {"key": "size", "value": "10"}
          ]
        }
      }
    }
  ]
}
```

---

## 📊 Available Shop IDs for Testing

| Shop Name | Shop ID | Type |
|-----------|---------|------|
| Gong Cha | `104b1828-abdd-4afe-a2fa-ac5c93f455f4` | Trà sữa |
| The Coffee House | `27797bf3-017e-4416-9f8f-846311a7759a` | Cà phê |
| Highlands Coffee | `adad75ee-46ea-47b8-9b5b-e23c41955ac9` | Cà phê |
| Phúc Long Coffee & Tea | `5e31a1d0-9ec4-4a0b-bb7d-603946a079bb` | Cà phê & Trà |
| Phở Hà Nội 24h | `1af4c735-438e-49db-9876-d6dd8bc41970` | Phở |
| Bún Chả Hà Nội | `2e1c1517-693c-48af-bb27-3d223ac46a18` | Bún |
| Bánh Mì Hòa Mã | `675dd695-314a-438f-8205-c5afd4866fbd` | Bánh mì |
| Cơm Tấm Sài Gòn | `587d74f1-22dc-4c2a-a4bb-ddf59abb60d8` | Cơm |

---

## ✅ Checklist

- [ ] Services are running (Eureka, API Gateway, Shop Service)
- [ ] Database has seed data
- [ ] Redis is running (for caching)
- [ ] Test with valid shop IDs
- [ ] Test with invalid shop ID (404 error)
- [ ] Verify response format matches ShopDetailResponse
- [ ] Check caching (second request should be faster)
