-- Script tạo full-text index cho MySQL để tối ưu hóa tìm kiếm
-- Chạy script này sau khi đã tạo bảng products

USE coffeehouse_prod;

-- Kiểm tra engine của bảng products (phải là InnoDB hoặc MyISAM để hỗ trợ FULLTEXT)
SHOW TABLE STATUS WHERE Name = 'products';

-- Tạo FULLTEXT INDEX cho cột tensp (tên sản phẩm)
-- Điều này sẽ tăng tốc độ tìm kiếm full-text đáng kể
ALTER TABLE products ADD FULLTEXT(tensp);

-- Nếu muốn tạo index cho nhiều cột (ví dụ: tên và mô tả sản phẩm)
-- ALTER TABLE products ADD FULLTEXT(tensp, mota);

-- Tạo các index thông thường để tối ưu hóa các truy vấn khác
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_gia ON products(gia);
CREATE INDEX idx_products_category_gia ON products(category_id, gia);

-- Kiểm tra các index đã được tạo
SHOW INDEX FROM products;

-- Test FULLTEXT search (tùy chọn)
-- SELECT * FROM products WHERE MATCH(tensp) AGAINST('cà phê' IN NATURAL LANGUAGE MODE);
-- SELECT * FROM products WHERE MATCH(tensp) AGAINST('+cà +phê' IN BOOLEAN MODE);

-- Thông tin về FULLTEXT search modes:
-- NATURAL LANGUAGE MODE: Tìm kiếm tự nhiên (mặc định)
-- BOOLEAN MODE: Tìm kiếm với các toán tử logic (+, -, *, etc.)
-- WITH QUERY EXPANSION: Mở rộng truy vấn với các từ liên quan

COMMIT;
