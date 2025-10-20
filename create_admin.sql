-- Script tạo admin user cho production/
-- Chạy script này trong MySQL để tạo admin user đầu tiên

USE coffeehouse_prod;

-- Tạo admin user (password: admin123456 - đã được hash bằng BCrypt)
INSERT INTO users (username, email, password, role, is_active, created_at) 
VALUES (
    'admin', 
    'admin@coffeehouse.com', 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGdMKOKtufuFviAVpbdJyC', -- admin123456
    'ADMIN', 
    true, 
    NOW()
);

-- Kiểm tra admin đã được tạo
SELECT id, username, email, role, is_active, created_at 
FROM users 
WHERE role = 'ADMIN';

-- Lưu ý: 
-- 1. Đổi password ngay sau khi login lần đầu
-- 2. Sử dụng password mạnh trong production
-- 3. Có thể tạo thêm admin users qua API sau khi login
