-- Script to insert sizes from db.json

USE coffeehouse_prod;

-- Assuming the table name is 'sizes'
INSERT INTO sizes (id, tensize, gia) VALUES
(1, 'S', 0),
(2, 'M', 5000),
(3, 'L', 10000);

-- Verify the inserted data
SELECT * FROM sizes;
