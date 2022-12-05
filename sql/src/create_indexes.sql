-- These are only used once per user login.
-- I think this is worth because users are created less frequently than logging in.
-- So fast login, slower user creation time
-- Better experience for returning customers
CREATE INDEX userID_index ON Users USING HASH (userID);
CREATE INDEX userPassword_index ON Users USING HASH (password);

-- The following were created because there are equality based
-- queries in our program using these columns. There are no
-- Updates on these tables, so there is no downside to adding these.
CREATE INDEX storeID_index ON Store USING HASH (storeID);
CREATE INDEX managerID_index ON Store USING HASH (managerID);

CREATE INDEX warehouseID_index ON Warehouse USING HASH (WarehouseID);