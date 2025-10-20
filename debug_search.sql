-- Script debug tìm kiếm sản phẩm

USE coffeehouse_prod;

-- Kiểm tra tất cả sản phẩm
SELECT id, tensp, gia, category_id FROM products ORDER BY id;

-- Tìm kiếm với LIKE (giống query trong code)
SELECT * FROM products WHERE LOWER(tensp) LIKE LOWER('%latte%');

-- Tìm kiếm với từ khóa khác
SELECT * FROM products WHERE LOWER(tensp) LIKE LOWER('%cà phê%');

-- Kiểm tra có sản phẩm nào không
SELECT COUNT(*) as total_products FROM products;
