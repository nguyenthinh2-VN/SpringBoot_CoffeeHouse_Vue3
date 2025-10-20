-- Script to replace products with ID 1 to 9

USE coffeehouse_prod;

-- Disable foreign key checks to avoid issues during replacement
SET FOREIGN_KEY_CHECKS=0;

-- REPLACE INTO will delete the old row and insert the new one if a primary key conflict occurs.
REPLACE INTO products (id, tensp, gia, category_id, hinh) VALUES
(1, 'Đường Đen Sữa Đá', 49000, 1, 'https://product.hstatic.net/1000075078/product/1737357048_uong-den-sua-da_5876b3829fe94af788996ca234a7894f_large.png'),
(2, 'Bạc Xĩu', 20000, 1, 'https://product.hstatic.net/1000075078/product/1737357020_bac-xiu-da_43a593b61b8e44379111a9ebb00904d9_large.png'),
(3, 'The Coffee House Sữa Đá', 39000, 1, 'https://product.hstatic.net/1000075078/product/1737357037_tch-sua-da_a83639744db94a7a96c1fd6b08e023e1_large.png'),
(4, 'Bạc Xỉu Nóng', 39000, 1, 'https://product.hstatic.net/1000075078/product/1737357029_bac-xiu-nong_e0a0732dee144dfda4e3cb43949ce065_large.png'),
(5, 'Cà Phê Đen Nóng', 35000, 1, 'https://product.hstatic.net/1000075078/product/1737356979_cf-den-nong_3ce91ffbdc04405b93855cd3b5d5aec2_large.png'),
(6, 'Latte Almond', 59000, 1, 'https://files.catbox.moe/5m1ltr.png'),
(7, 'Latte Caramel', 59000, 1, 'https://files.catbox.moe/isn6wi.png'),
(8, 'Latte Hazelnut', 59000, 1, 'https://files.catbox.moe/bgu6ln.png'),
(9, 'Latte Coconut', 59000, 1, 'https://files.catbox.moe/izdmgg.png');

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS=1;

-- Verify the replaced data
SELECT * FROM products WHERE id BETWEEN 1 AND 9;
