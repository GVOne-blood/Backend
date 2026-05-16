# Shop Service API Documentation

## Base URL
```
http://localhost:8080/shop
```

## Endpoints

### 1. Get Shop Detail

Get detailed information about a specific shop.

**Endpoint:** `GET /shop/{shopId}`

**Authentication:** Not required (Public endpoint)

**Path Parameters:**
- `shopId` (string, required) - UUID of the shop

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "shopId": "550e8400-e29b-41d4-a716-446655440000",
    "shopName": "The Coffee Factory - Trương Định",
    "logo": "https://example.com/logo.jpg",
    "introduction": "Cà phê ngon, không gian đẹp",
    "shopAddress": "107A Trương Định, Phường Võ Thị Sáu, Quận 3",
    "city": "Hồ Chí Minh",
    "province": "Hồ Chí Minh",
    "avgStar": 5.0,
    "totalFeedback": 102,
    "activeHours": "07:00 - 22:00",
    "distance": 0.0,
    "totalProducts": 50,
    "totalSold": 1200,
    "totalOrders": 800,
    "phoneNumber": "0901234567",
    "email": "contact@coffeefactory.com",
    "shopStatus": "ACTIVE",
    "isActive": 1,
    "shopType": "FOOD_BEVERAGE",
    "businessType": "INDIVIDUAL"
  }
}
```

**Error Responses:**

404 Not Found:
```json
{
  "code": 404,
  "message": "Shop not found with id: {shopId}",
  "data": null
}
```

**Caching:**
- Cache duration: 5 minutes
- Cache key: `shop_detail:{shopId}`

---

### 2. Get Featured Shops

Get list of featured shops (top selling shops in last month).

**Endpoint:** `GET /shop/featured`

**Authentication:** Not required (Public endpoint)

**Query Parameters:**
- `page` (integer, optional, default: 0) - Page number
- `size` (integer, optional, default: 10) - Page size
- `sort` (string, optional, default: "totalSold,desc") - Sort field and direction

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "shopId": "550e8400-e29b-41d4-a716-446655440000",
        "shopName": "The Coffee Factory",
        "logo": "https://example.com/logo.jpg",
        "introduction": "Cà phê ngon",
        "totalProducts": 50,
        "totalSold": 1200
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 25,
    "totalPages": 3,
    "last": false
  }
}
```

**Caching:**
- Cache duration: 10 minutes
- Cache key: `featured_shops:page:{page}:size:{size}`

---

### 3. Get My Shop Info

Get shop information for the authenticated shop owner.

**Endpoint:** `GET /shop`

**Authentication:** Required (Shop Owner)

**Headers:**
- `X-User-Id` (string, required) - User ID from authentication
- `X-User-Username` (string, required) - Username from authentication
- `X-User-Roles` (string, required) - User roles from authentication

**Response:**
```json
{
  "shopId": "550e8400-e29b-41d4-a716-446655440000",
  "shopName": "My Coffee Shop",
  "logo": "https://example.com/logo.jpg",
  "introduction": "Welcome to my shop",
  "totalProducts": 30,
  "totalSold": 500
}
```

---

## Data Models

### ShopDetailResponse

| Field | Type | Description |
|-------|------|-------------|
| shopId | string (UUID) | Unique shop identifier |
| shopName | string | Shop name |
| logo | string | Shop logo URL |
| introduction | string | Shop description/introduction |
| shopAddress | string | Full shop address |
| city | string | City name |
| province | string | Province name |
| avgStar | decimal | Average rating (0-5) |
| totalFeedback | integer | Total number of reviews |
| activeHours | string | Shop opening hours |
| distance | double | Distance from user (in km) |
| totalProducts | integer | Total number of products |
| totalSold | integer | Total items sold |
| totalOrders | integer | Total orders completed |
| phoneNumber | string | Contact phone number |
| email | string | Contact email |
| shopStatus | string | Shop status (ACTIVE, INACTIVE, etc.) |
| isActive | integer | Active flag (0 or 1) |
| shopType | string | Type of shop |
| businessType | string | Business type |

---

## Frontend Integration

### Angular Service Example

```typescript
// shop.service.ts
getShopDetail(shopId: string): Observable<ApiResponse<ShopDetailResponse>> {
  return this.http.get<ApiResponse<ShopDetailResponse>>(
    `${this.apiUrl}/shop/${shopId}`
  );
}
```

### Usage in Component

```typescript
// store-detail.component.ts
ngOnInit() {
  const shopId = this.route.snapshot.paramMap.get('id');
  if (shopId) {
    this.shopService.getShopDetail(shopId).subscribe({
      next: (response) => {
        this.storeInfo = response.data;
      },
      error: (error) => {
        console.error('Error loading shop:', error);
      }
    });
  }
}
```

---

## Testing

### cURL Examples

**Get Shop Detail:**
```bash
curl -X GET "http://localhost:8080/shop/550e8400-e29b-41d4-a716-446655440000"
```

**Get Featured Shops:**
```bash
curl -X GET "http://localhost:8080/shop/featured?page=0&size=10"
```

---

## Notes

1. **Distance Calculation**: Currently returns 0.0. Will be implemented later based on user's location.
2. **Caching**: Redis caching is implemented for better performance.
3. **Public Access**: Shop detail and featured shops endpoints are public (no authentication required).
4. **Error Handling**: All errors return standard ResponseData format with appropriate HTTP status codes.
