-- V10__drop_legacy_outbox_columns.sql
-- product_image_outbox 테이블에서 deprecated 컬럼들 삭제
--
-- V9에서 crawled_product_image 테이블로 이미지 데이터가 분리되었으므로
-- 더 이상 필요없는 legacy 컬럼들을 삭제합니다.

-- =====================================================
-- 1. 인덱스 삭제 (컬럼 삭제 전)
-- =====================================================
DROP INDEX idx_product_image_outbox_crawled_product ON product_image_outbox;
DROP INDEX idx_product_image_outbox_crawled_url ON product_image_outbox;

-- =====================================================
-- 2. Legacy 컬럼들 삭제
-- =====================================================
ALTER TABLE product_image_outbox
    DROP COLUMN crawled_product_id,
    DROP COLUMN original_url,
    DROP COLUMN s3_url,
    DROP COLUMN image_type;
