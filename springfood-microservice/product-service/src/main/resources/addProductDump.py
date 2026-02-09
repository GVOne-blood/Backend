import pandas as pd
import random
import uuid
from datetime import datetime, timedelta

# Số lượng bản ghi
NUM_RECORDS = 100

# 1. Cấu hình UUID Shop cố định (để test consistency)
SHOP_ID = str(uuid.uuid4())

# 2. Danh mục mẫu
CATEGORIES = ["Laptop", "Smartphone", "Tablet", "Monitor", "Keyboard", "Mouse", "Headphone", "Smartwatch"]


# 3. Hàm random ngày tháng
def random_date(start_year=2024, end_year=2025):
    start = datetime(start_year, 1, 1)
    end = datetime(end_year, 12, 31)
    return start + timedelta(days=random.randint(0, (end - start).days))


# 4. Sinh dữ liệu
data = []
for i in range(1, NUM_RECORDS + 1):
    cat = random.choice(CATEGORIES)
    base_price = random.randint(1000000, 50000000)

    # Logic map đúng theo thứ tự index trong method addProductsByExcel
    row = {
        # Index 0: shopId
        "shopId": SHOP_ID,

        # Index 1: categoryNames
        "categoryName": cat,

        # Index 2: name
        "name": f"{cat} Model {random.choice(['X', 'Pro', 'Air', 'Ultra'])} - Gen {random.randint(1, 10)}",

        # Index 3: description
        "description": f"Mô tả chi tiết cho sản phẩm {cat} thứ {i}. Cấu hình mạnh, bảo hành chính hãng.",

        # Index 4: price (String trong code, nhưng Excel nên để số hoặc text số)
        "price": str(base_price),

        # Index 5: images (String url)
        "images": f"https://minio.theblood.com/products/img_{i}.jpg",

        # Index 6: quantity (Integer)
        "quantity": random.randint(10, 500),

        # Index 7: sku (String - Unique)
        "sku": f"SKU-{cat[:3].upper()}-{i:04d}",

        # Index 8: msg (Manufacturing Date - LocalDate)
        "msg": random_date(2023, 2023).strftime("%Y-%m-%d"),

        # Index 9: exp (Expiry Date - LocalDate)
        "exp": random_date(2025, 2026).strftime("%Y-%m-%d"),

        # Index 10: wholesalePrice (String)
        "wholesalePrice": str(int(base_price * 0.8)),

        # Index 11: avgRate (BigDecimal - Code đọc ở dòng row.getCell(11))
        "avgRate": round(random.uniform(3.5, 5.0), 1)
    }
    data.append(row)

# 5. Tạo DataFrame
df = pd.DataFrame(data)

# 6. Xuất ra Excel
# Lưu ý: Code Java sử dụng apache POI nên format date trong Excel cần chuẩn
file_name = "products_import_sample.xlsx"
df.to_excel(file_name, index=False)

print(f"Đã tạo file {file_name} với {NUM_RECORDS} bản ghi.")
print(f"Shop ID dùng chung: {SHOP_ID}")
