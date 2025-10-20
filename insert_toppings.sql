-- Script to insert toppings from db.json

USE coffeehouse_prod;

-- Assuming the table name is 'toppings'
INSERT INTO toppings (id, tentopping, gia) VALUES
(1, 'Chân trâu đen', 5000),
(2, 'Chân hoàng kim', 5000),
(3, 'Chân trâu đường đen', 7000),
(4, 'Chân trâu phô mai', 10000),
(10, 'Full topping ', 35000);

-- Verify the inserted data
SELECT * FROM toppings;
