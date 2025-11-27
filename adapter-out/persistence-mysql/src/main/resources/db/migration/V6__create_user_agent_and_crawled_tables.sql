-- V6__create_user_agent_and_crawled_tables.sql
-- user_agent, crawled_raw, crawled_product 테이블 생성

-- =====================================================
-- 1. user_agent 테이블 (UserAgent 관리)
-- =====================================================
CREATE TABLE user_agent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키',
    token VARCHAR(500) NOT NULL COMMENT '암호화된 토큰 (AES-256 Base64)',
    status VARCHAR(20) NOT NULL COMMENT 'UserAgent 상태 (AVAILABLE/SUSPENDED/BLOCKED)',
    health_score INT NOT NULL DEFAULT 100 COMMENT 'Health Score (0-100)',
    last_used_at DATETIME(6) NULL COMMENT '마지막 사용 시각',
    requests_per_day INT NOT NULL DEFAULT 0 COMMENT '일일 요청 수',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='UserAgent 관리 테이블';

-- user_agent 인덱스
CREATE INDEX idx_user_agent_status ON user_agent (status);
CREATE INDEX idx_user_agent_health_score ON user_agent (health_score);

-- =====================================================
-- 2. crawled_raw 테이블 (크롤링 Raw 데이터)
-- =====================================================
CREATE TABLE crawled_raw (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키',
    crawl_scheduler_id BIGINT NOT NULL COMMENT '스케줄러 ID (FK)',
    seller_id BIGINT NOT NULL COMMENT '셀러 ID (FK)',
    item_no BIGINT NOT NULL COMMENT '상품 번호',
    crawl_type VARCHAR(20) NOT NULL COMMENT '크롤링 타입 (MINI_SHOP/DETAIL/OPTION)',
    raw_data LONGTEXT NOT NULL COMMENT 'JSON 형태의 파싱 결과',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태 (PENDING/PROCESSED/FAILED)',
    error_message VARCHAR(1000) NULL COMMENT '처리 실패 시 에러 메시지',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    processed_at DATETIME(6) NULL COMMENT '처리 완료 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 Raw 데이터 테이블';

-- crawled_raw 인덱스
CREATE INDEX idx_crawled_raw_scheduler ON crawled_raw (crawl_scheduler_id);
CREATE INDEX idx_crawled_raw_seller ON crawled_raw (seller_id);
CREATE INDEX idx_crawled_raw_item ON crawled_raw (item_no);
CREATE INDEX idx_crawled_raw_status ON crawled_raw (status);
CREATE INDEX idx_crawled_raw_type_status ON crawled_raw (crawl_type, status);
CREATE INDEX idx_crawled_raw_seller_item ON crawled_raw (seller_id, item_no);

-- =====================================================
-- 3. crawled_product 테이블 (크롤링 상품 정보)
-- =====================================================
CREATE TABLE crawled_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키',
    seller_id BIGINT NOT NULL COMMENT '셀러 ID (FK)',
    item_no BIGINT NOT NULL COMMENT '상품 번호 (UNIQUE with seller_id)',

    -- MINI_SHOP 데이터
    item_name VARCHAR(500) NULL COMMENT '상품명',
    brand_name VARCHAR(200) NULL COMMENT '브랜드명',
    original_price BIGINT NULL COMMENT '원가',
    discount_price BIGINT NULL COMMENT '할인가',
    discount_rate INT NULL COMMENT '할인율',
    images_json LONGTEXT NULL COMMENT '이미지 정보 JSON (ProductImages)',
    free_shipping TINYINT(1) NOT NULL DEFAULT 0 COMMENT '무료 배송 여부',

    -- DETAIL 데이터
    category_json VARCHAR(1000) NULL COMMENT '카테고리 정보 JSON (ProductCategory)',
    shipping_info_json VARCHAR(1000) NULL COMMENT '배송 정보 JSON (ShippingInfo)',
    description_mark_up LONGTEXT NULL COMMENT '상세 설명 HTML',
    item_status VARCHAR(50) NULL COMMENT '상품 상태',
    origin_country VARCHAR(100) NULL COMMENT '원산지',
    shipping_location VARCHAR(200) NULL COMMENT '배송 출발지',

    -- OPTION 데이터
    options_json LONGTEXT NULL COMMENT '옵션 정보 JSON (ProductOptions)',

    -- 크롤링 완료 상태
    mini_shop_crawled_at DATETIME(6) NULL COMMENT 'MINI_SHOP 크롤링 완료 시각',
    detail_crawled_at DATETIME(6) NULL COMMENT 'DETAIL 크롤링 완료 시각',
    option_crawled_at DATETIME(6) NULL COMMENT 'OPTION 크롤링 완료 시각',

    -- 외부 서버 동기화 상태
    external_product_id BIGINT NULL COMMENT '외부 서버 상품 ID',
    last_synced_at DATETIME(6) NULL COMMENT '마지막 동기화 시각',
    needs_sync TINYINT(1) NOT NULL DEFAULT 0 COMMENT '동기화 필요 여부',

    -- 감사 정보
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 일시',

    -- UNIQUE 제약조건: 셀러별 상품번호 중복 방지
    CONSTRAINT uk_crawled_product_seller_item UNIQUE (seller_id, item_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 상품 정보 테이블';

-- crawled_product 인덱스
CREATE INDEX idx_crawled_product_seller ON crawled_product (seller_id);
CREATE INDEX idx_crawled_product_item_no ON crawled_product (item_no);
CREATE INDEX idx_crawled_product_needs_sync ON crawled_product (needs_sync);
CREATE INDEX idx_crawled_product_external ON crawled_product (external_product_id);
CREATE INDEX idx_crawled_product_brand ON crawled_product (brand_name);
