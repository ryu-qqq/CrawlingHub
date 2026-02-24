-- crawled_product 테이블에 soft delete 컬럼 추가
ALTER TABLE crawled_product
    ADD COLUMN deleted_at DATETIME(6) NULL COMMENT '삭제 일시 (소프트 딜리트)' AFTER updated_at;
