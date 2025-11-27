-- V5__add_seller_product_count.sql
-- seller 테이블에 product_count 컬럼 추가 (META 크롤링 결과 저장)

ALTER TABLE seller
    ADD COLUMN product_count INT NOT NULL DEFAULT 0
    COMMENT '셀러 상품 수 (META 크롤링 결과)';

-- 인덱스 추가 (필요 시 상품 수 기반 조회용)
CREATE INDEX idx_seller_product_count ON seller (product_count);
