-- Script to insert categories from db.json

USE coffeehouse_prod;

-- Assuming the table is 'categories' and column is 'name' based on Category.java model
INSERT INTO categories (id, name) VALUES
(1, 'Cà phê'),
(2, 'Trà');

-- Verify the inserted data
SELECT * FROM categories;
