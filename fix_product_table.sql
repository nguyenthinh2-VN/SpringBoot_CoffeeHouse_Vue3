-- Script sửa lỗi AUTO_INCREMENT cho bảng products

USE coffeehouse_prod;

-- Kiểm tra cấu trúc bảng hiện tại
DESCRIBE products;

-- Sửa cột id để có AUTO_INCREMENT
ALTER TABLE products MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Nếu bảng đã có dữ liệu và cần reset AUTO_INCREMENT
-- ALTER TABLE products AUTO_INCREMENT = 1;

-- Kiểm tra lại cấu trúc sau khi sửa
DESCRIBE products;

-- Kiểm tra dữ liệu
SELECT * FROM products ORDER BY id DESC LIMIT 5;
