-- Tạo bảng vouchers
CREATE TABLE IF NOT EXISTS vouchers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_type VARCHAR(50) NOT NULL COMMENT 'FIXED hoặc PERCENT',
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_amount DECIMAL(10, 2) COMMENT 'Tối thiểu để áp dụng',
    max_discount DECIMAL(10, 2) COMMENT 'Giảm tối đa (cho PERCENT)',
    is_active BOOLEAN DEFAULT TRUE,
    start_date DATETIME,
    end_date DATETIME,
    usage_limit INT COMMENT 'Số lần sử dụng tối đa',
    usage_count INT DEFAULT 0 COMMENT 'Số lần đã sử dụng',
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_code (code),
    INDEX idx_is_active (is_active),
    INDEX idx_dates (start_date, end_date)
);

-- Thêm cột discount và voucher_code vào bảng orders (nếu chưa có)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS discount DECIMAL(10, 2) DEFAULT 0;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS voucher_code VARCHAR(50);

-- Tạo index cho orders
CREATE INDEX IF NOT EXISTS idx_orders_voucher_code ON orders(voucher_code);

-- Insert dữ liệu voucher mẫu
INSERT INTO vouchers (code, discount_type, discount_value, min_order_amount, max_discount, is_active, start_date, end_date, usage_limit, usage_count, created_at) VALUES
-- Voucher 1: Miễn phí ship 30,000 VND
('FREESHIP', 'FIXED', 30000, NULL, 30000, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, 0, NOW()),

-- Voucher 2: Giảm 20% (tối đa 100,000), tối thiểu 100,000
('SAVE20', 'PERCENT', 20, 100000, 100000, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, 0, NOW()),

-- Voucher 3: Giảm cố định 50,000 (tối thiểu 200,000)
('SAVE50K', 'FIXED', 50000, 200000, 50000, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, 0, NOW()),

-- Voucher 4: Giảm 10% (tối đa 50,000), không giới hạn tối thiểu
('SAVE10', 'PERCENT', 10, NULL, 50000, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, 0, NOW()),

-- Voucher 5: Giảm 15% (tối đa 75,000), tối thiểu 150,000
('SAVE15', 'PERCENT', 15, 150000, 75000, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, 0, NOW());

-- Xem danh sách voucher
SELECT * FROM vouchers;
