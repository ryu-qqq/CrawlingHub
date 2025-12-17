-- V8__create_product_outbox_tables.sql
-- product_sync_outbox, product_image_outbox 테이블 생성

-- =====================================================
-- 1. product_sync_outbox 테이블 (외부 서버 동기화 Outbox)
-- =====================================================
-- Outbox 패턴을 통해 외부 상품 서버 동기화의 트랜잭션 보장
-- CrawledProduct 저장과 같은 트랜잭션에서 Outbox 저장
-- 별도 스케줄러/이벤트 리스너가 PENDING 상태 조회 후 외부 API 호출
CREATE TABLE product_sync_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키',
    crawled_product_id BIGINT NOT NULL COMMENT 'CrawledProduct ID (FK)',
    seller_id BIGINT NOT NULL COMMENT '셀러 ID (FK)',
    item_no BIGINT NOT NULL COMMENT '상품 번호',
    sync_type VARCHAR(20) NOT NULL COMMENT '동기화 타입 (CREATE/UPDATE)',
    idempotency_key VARCHAR(100) NOT NULL COMMENT '멱등성 키 (중복 방지)',
    external_product_id BIGINT NULL COMMENT '외부 서버 상품 ID (CREATE 완료 시 설정)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '상태 (PENDING/PROCESSING/COMPLETED/FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    error_message VARCHAR(1000) NULL COMMENT '에러 메시지',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    processed_at DATETIME(6) NULL COMMENT '처리 완료 일시',

    CONSTRAINT uk_product_sync_outbox_idempotency UNIQUE (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 서버 동기화 Outbox 테이블';

-- product_sync_outbox 인덱스
CREATE INDEX idx_product_sync_outbox_status ON product_sync_outbox (status);
CREATE INDEX idx_product_sync_outbox_crawled_product ON product_sync_outbox (crawled_product_id);
CREATE INDEX idx_product_sync_outbox_seller ON product_sync_outbox (seller_id);
CREATE INDEX idx_product_sync_outbox_status_retry ON product_sync_outbox (status, retry_count);

-- =====================================================
-- 2. product_image_outbox 테이블 (이미지 업로드 Outbox)
-- =====================================================
-- Outbox 패턴을 통해 이미지 업로드의 트랜잭션 보장
-- CrawledProduct 저장과 같은 트랜잭션에서 Outbox 저장
-- 별도 스케줄러/이벤트 리스너가 PENDING 상태 조회 후 이미지 업로드 실행
CREATE TABLE product_image_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키',
    crawled_product_id BIGINT NOT NULL COMMENT 'CrawledProduct ID (FK)',
    image_type VARCHAR(20) NOT NULL COMMENT '이미지 타입 (THUMBNAIL/DESCRIPTION)',
    original_url VARCHAR(2000) NOT NULL COMMENT '원본 이미지 URL',
    idempotency_key VARCHAR(100) NOT NULL COMMENT '멱등성 키 (중복 방지)',
    s3_url VARCHAR(2000) NULL COMMENT '업로드된 S3 URL (완료 시 설정)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '상태 (PENDING/PROCESSING/COMPLETED/FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    error_message VARCHAR(1000) NULL COMMENT '에러 메시지',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    processed_at DATETIME(6) NULL COMMENT '처리 완료 일시',

    CONSTRAINT uk_product_image_outbox_idempotency UNIQUE (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='이미지 업로드 Outbox 테이블';

-- product_image_outbox 인덱스
CREATE INDEX idx_product_image_outbox_status ON product_image_outbox (status);
CREATE INDEX idx_product_image_outbox_crawled_product ON product_image_outbox (crawled_product_id);
CREATE INDEX idx_product_image_outbox_status_retry ON product_image_outbox (status, retry_count);
CREATE INDEX idx_product_image_outbox_type_status ON product_image_outbox (image_type, status);
CREATE INDEX idx_product_image_outbox_crawled_url ON product_image_outbox (crawled_product_id, original_url(255));
