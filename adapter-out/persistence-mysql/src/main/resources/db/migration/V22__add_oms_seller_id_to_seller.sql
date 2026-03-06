-- seller 테이블에 OMS 셀러 ID 컬럼 추가 (nullable)
ALTER TABLE seller
    ADD COLUMN oms_seller_id BIGINT NULL COMMENT 'OMS 셀러 ID' AFTER seller_name;
