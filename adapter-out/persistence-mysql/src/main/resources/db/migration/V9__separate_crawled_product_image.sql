-- V9__separate_crawled_product_image.sql
-- CrawledProductImage 테이블 분리 (Outbox에서 이미지 데이터 분리)
--
-- 변경 사항:
-- 1. crawled_product_image 테이블 신규 생성 (이미지 영구 저장)
-- 2. product_image_outbox 테이블에 crawled_product_image_id FK 추가
-- 3. 기존 데이터 마이그레이션
-- 4. 기존 이미지 관련 컬럼 유지 (안전한 마이그레이션)

-- =====================================================
-- 1. crawled_product_image 테이블 생성
-- =====================================================
CREATE TABLE crawled_product_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키',
    crawled_product_id BIGINT NOT NULL COMMENT 'CrawledProduct ID (FK)',
    original_url VARCHAR(2000) NOT NULL COMMENT '원본 이미지 URL',
    s3_url VARCHAR(2000) NULL COMMENT '업로드된 S3 URL',
    file_asset_id VARCHAR(100) NULL COMMENT 'Fileflow 파일 자산 ID',
    image_type VARCHAR(20) NOT NULL COMMENT '이미지 타입 (THUMBNAIL/DESCRIPTION)',
    display_order INT NOT NULL DEFAULT 0 COMMENT '표시 순서',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NULL COMMENT '수정 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 상품 이미지 테이블';

-- crawled_product_image 인덱스
CREATE INDEX idx_crawled_product_image_product ON crawled_product_image (crawled_product_id);
CREATE INDEX idx_crawled_product_image_type ON crawled_product_image (crawled_product_id, image_type);
CREATE INDEX idx_crawled_product_image_product_url ON crawled_product_image (crawled_product_id, original_url(255));

-- =====================================================
-- 2. 기존 데이터 마이그레이션 (product_image_outbox → crawled_product_image)
-- =====================================================
INSERT INTO crawled_product_image
    (crawled_product_id, original_url, s3_url, image_type, display_order, created_at, updated_at)
SELECT
    crawled_product_id,
    original_url,
    s3_url,
    image_type,
    0 AS display_order,
    created_at,
    processed_at AS updated_at
FROM product_image_outbox;

-- =====================================================
-- 3. product_image_outbox 테이블에 crawled_product_image_id 컬럼 추가
-- =====================================================
ALTER TABLE product_image_outbox
    ADD COLUMN crawled_product_image_id BIGINT NULL COMMENT 'CrawledProductImage ID (FK)' AFTER id;

-- crawled_product_image_id 인덱스
CREATE INDEX idx_product_image_outbox_image_id ON product_image_outbox (crawled_product_image_id);

-- =====================================================
-- 4. FK 값 업데이트 (기존 outbox에 image_id 연결)
-- =====================================================
UPDATE product_image_outbox o
INNER JOIN crawled_product_image i
    ON o.crawled_product_id = i.crawled_product_id
    AND o.original_url = i.original_url
SET o.crawled_product_image_id = i.id;

-- =====================================================
-- 5. (안전을 위해 기존 컬럼은 유지 - 추후 별도 마이그레이션에서 삭제)
-- =====================================================
-- 다음 마이그레이션에서 처리할 사항:
-- ALTER TABLE product_image_outbox DROP COLUMN crawled_product_id;
-- ALTER TABLE product_image_outbox DROP COLUMN original_url;
-- ALTER TABLE product_image_outbox DROP COLUMN s3_url;
-- ALTER TABLE product_image_outbox DROP COLUMN image_type;
