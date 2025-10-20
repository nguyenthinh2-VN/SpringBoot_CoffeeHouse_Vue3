-- Script to insert ice_options from db.json

USE coffeehouse_prod;

-- Assuming the table name is 'ice_options'
INSERT INTO ice_options (id, tenice) VALUES
(1, 'Ít đá'),
(2, 'Nhiều đá'),
(3, 'Nóng');

-- Verify the inserted data
SELECT * FROM ice_options;
