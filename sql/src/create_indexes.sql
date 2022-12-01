--CREATE INDEX nyc_on_hand_index ON part_nyc (on_hand);

-- These are only used once per user login.
-- I think this is worth because users are created less frequently than loging in.
-- So fast login, slower user creation time
-- Better experience for returning customers
CREATE INDEX userID_index ON Users (userID);
CREATE INDEX userName ON Users (name);
CREATE INDEX userPassword ON Users (password);
CREATE INDEX userLatitude ON Users (latitude);
CREATE INDEX userLongititude ON Users (longitude);
CREATE INDEX userType ON Users (longitude);

-- CREATE INDEX storeID
