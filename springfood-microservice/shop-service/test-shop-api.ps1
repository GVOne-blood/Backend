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
