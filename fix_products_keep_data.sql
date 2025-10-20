-- Script sửa bảng products mà vẫn giữ dữ liệu

USE coffeehouse_prod;

-- 1. Backup dữ liệu hiện tại (tùy chọn)
CREATE TABLE products_backup AS SELECT * FROM products;

-- 2. Kiểm tra xem có PRIMARY KEY chưa
SHOW INDEX FROM products WHERE Key_name = 'PRIMARY';

-- 3. Nếu chưa có PRIMARY KEY, thêm vào
-- ALTER TABLE products ADD PRIMARY KEY (id);

-- 4. Sửa cột id để có AUTO_INCREMENT
ALTER TABLE products MODIFY COLUMN id INT AUTO_INCREMENT;

-- 5. Nếu cần reset AUTO_INCREMENT từ giá trị cao nhất hiện tại
SET @max_id = (SELECT COALESCE(MAX(id), 0) FROM products);
SET @sql = CONCAT('ALTER TABLE products AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. Kiểm tra lại cấu trúc
DESCRIBE products;
SHOW CREATE TABLE products;

-- 7. Test insert
INSERT INTO products (tensp, gia, category_id, hinh) VALUES 
('Test Auto Increment', 30000, 1, 'https://example.com/test.jpg');

SELECT * FROM products ORDER BY id DESC LIMIT 3;
