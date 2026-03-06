-- ============================================
-- MIGRATION: Tách Address thành bảng riêng
-- ============================================

-- 1. Tạo bảng addresses
CREATE TABLE IF NOT EXISTS addresses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    district VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    full_address TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_is_default (is_default)
);

-- 2. Cập nhật bảng orders - thêm address_id
ALTER TABLE orders ADD COLUMN IF NOT EXISTS address_id INT;

-- 3. Xóa cột cũ từ orders (nếu tồn tại)
ALTER TABLE orders DROP COLUMN IF EXISTS shipping_address;
ALTER TABLE orders DROP COLUMN IF EXISTS phone_number;
ALTER TABLE orders DROP COLUMN IF EXISTS district;
ALTER TABLE orders DROP COLUMN IF EXISTS city;

-- 4. Tạo index cho orders
CREATE INDEX IF NOT EXISTS idx_orders_address_id ON orders(address_id);

-- 5. Insert dữ liệu mẫu (nếu cần)
-- Ví dụ: Tạo địa chỉ mặc định cho user
INSERT INTO addresses (user_id, full_name, phone, district, city, full_address, is_default, created_at) VALUES
(107, 'Nguyễn Văn A', '0123456789', 'Quận 1', 'TP.HCM', '123 Nguyễn Huệ, Quận 1, TP.HCM', TRUE, NOW()),
(107, 'Nguyễn Văn A', '0987654321', 'Quận 3', 'TP.HCM', '456 Lê Lợi, Quận 3, TP.HCM', FALSE, NOW());

-- 6. Xem danh sách addresses
SELECT * FROM addresses;

-- 7. Xem danh sách orders
SELECT id, order_code, user_id, address_id, total_amount, delivery_method FROM orders;
