CREATE OR REPLACE FUNCTION ProductOrderUpdate() RETURNS TRIGGER AS
$$
BEGIN    
    UPDATE Product
    SET numberOfUnits = numberOfUnits - new.unitsOrdered
    WHERE storeID = new.storeID AND productName = new.productName;

    RETURN new;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER OrderTrigger
    AFTER INSERT ON Orders
    FOR EACH ROW
    EXECUTE PROCEDURE ProductOrderUpdate();

-----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION UpdateProductSupply() RETURNS TRIGGER AS
$$
BEGIN    
    UPDATE Product
    SET numberOfUnits = numberOfUnits + new.unitsRequested
    WHERE storeID = new.storeID AND productName = new.productName;

    RETURN new;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER SupplyRequestTrigger
    AFTER INSERT ON ProductSupplyRequests
    FOR EACH ROW
    EXECUTE PROCEDURE UpdateProductSupply();