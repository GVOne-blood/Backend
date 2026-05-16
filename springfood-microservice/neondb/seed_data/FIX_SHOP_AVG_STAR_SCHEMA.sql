-- ============================================
-- FIX SHOP AVG_STAR SCHEMA
-- ============================================
-- Sửa field avg_star từ numeric(2,2) sang numeric(3,2)
-- để chứa rating 0-5 sao (ví dụ: 4.50)

BEGIN;

-- Alter column type
ALTER TABLE springfood_shop.shops 
ALTER COLUMN avg_star TYPE numeric(3, 2);

COMMIT;

-- Verification
SELECT column_name, data_type, numeric_precision, numeric_scale
FROM information_schema.columns
WHERE table_schema = 'springfood_shop' 
  AND table_name = 'shops' 
  AND column_name = 'avg_star';

-- Expected result:
-- column_name | data_type | numeric_precision | numeric_scale
-- avg_star    | numeric   | 3                 | 2
