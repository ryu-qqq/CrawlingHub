-- CrawledProduct에 pending_changes 컬럼 추가
ALTER TABLE crawled_product
    ADD COLUMN pending_changes VARCHAR(200) NULL DEFAULT NULL
    COMMENT '보류 중인 변경 유형 (쉼표 구분: PRICE,IMAGE,DESCRIPTION,OPTION_STOCK,PRODUCT_INFO)';
