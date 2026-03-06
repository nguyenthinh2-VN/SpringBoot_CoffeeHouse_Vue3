-- Tạo bảng shipping_zones
CREATE TABLE IF NOT EXISTS shipping_zones (
    id INT PRIMARY KEY AUTO_INCREMENT,
    zone_name VARCHAR(100) NOT NULL COMMENT 'Tên khu vực',
    district VARCHAR(100) NOT NULL COMMENT 'Quận/Huyện',
    city VARCHAR(100) NOT NULL COMMENT 'Thành phố',
    shipping_fee DECIMAL(10, 2) NOT NULL COMMENT 'Phí giao hàng',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    UNIQUE KEY uk_district_city (district, city),
    INDEX idx_is_active (is_active),
    INDEX idx_city (city)
);

-- Cập nhật bảng orders - thêm cột district, city
ALTER TABLE orders ADD COLUMN IF NOT EXISTS district VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS city VARCHAR(100);

-- Tạo index cho orders
CREATE INDEX IF NOT EXISTS idx_orders_district_city ON orders(district, city);

-- Insert dữ liệu shipping zones mẫu cho TP.HCM
INSERT INTO shipping_zones (zone_name, district, city, shipping_fee, is_active, created_at) VALUES
-- Quận trung tâm (15,000 - 20,000)
('Quận 1', 'Quận 1', 'TP.HCM', 15000, TRUE, NOW()),
('Quận 2', 'Quận 2', 'TP.HCM', 20000, TRUE, NOW()),
('Quận 3', 'Quận 3', 'TP.HCM', 18000, TRUE, NOW()),
('Quận 4', 'Quận 4', 'TP.HCM', 18000, TRUE, NOW()),
('Quận 5', 'Quận 5', 'TP.HCM', 18000, TRUE, NOW()),
('Quận 6', 'Quận 6', 'TP.HCM', 18000, TRUE, NOW()),
('Quận 7', 'Quận 7', 'TP.HCM', 20000, TRUE, NOW()),
('Quận 8', 'Quận 8', 'TP.HCM', 20000, TRUE, NOW()),
('Quận 9', 'Quận 9', 'TP.HCM', 25000, TRUE, NOW()),
('Quận 10', 'Quận 10', 'TP.HCM', 18000, TRUE, NOW()),
('Quận 11', 'Quận 11', 'TP.HCM', 18000, TRUE, NOW()),
('Quận 12', 'Quận 12', 'TP.HCM', 25000, TRUE, NOW()),

-- Quận ngoài (22,000 - 30,000)
('Quận Tân Bình', 'Quận Tân Bình', 'TP.HCM', 22000, TRUE, NOW()),
('Quận Tân Phú', 'Quận Tân Phú', 'TP.HCM', 22000, TRUE, NOW()),
('Quận Phú Nhuận', 'Quận Phú Nhuận', 'TP.HCM', 20000, TRUE, NOW()),
('Quận Bình Thạnh', 'Quận Bình Thạnh', 'TP.HCM', 22000, TRUE, NOW()),
('Quận Gò Vấp', 'Quận Gò Vấp', 'TP.HCM', 22000, TRUE, NOW()),

-- Huyện (28,000 - 35,000)
('Huyện Bình Chánh', 'Huyện Bình Chánh', 'TP.HCM', 30000, TRUE, NOW()),
('Huyện Cần Giờ', 'Huyện Cần Giờ', 'TP.HCM', 35000, TRUE, NOW()),
('Huyện Củ Chi', 'Huyện Củ Chi', 'TP.HCM', 35000, TRUE, NOW()),
('Huyện Hóc Môn', 'Huyện Hóc Môn', 'TP.HCM', 30000, TRUE, NOW()),
('Huyện Nhà Bè', 'Huyện Nhà Bè', 'TP.HCM', 28000, TRUE, NOW()),

-- Thành phố Thủ Đức
('Thành phố Thủ Đức', 'Thành phố Thủ Đức', 'TP.HCM', 25000, TRUE, NOW());

-- Xem danh sách shipping zones
SELECT * FROM shipping_zones ORDER BY city, district;

-- Xem số lượng zones
SELECT COUNT(*) as total_zones FROM shipping_zones WHERE is_active = TRUE;
