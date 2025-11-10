-- ProductSnapshot 테이블
CREATE TABLE product_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    must_it_item_no BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,

    -- 미니샵 데이터
    product_name VARCHAR(500),
    price BIGINT,
    main_image_url VARCHAR(1000),

    -- 옵션 데이터
    options JSON,
    total_stock INT,

    -- 상세 데이터
    product_info JSON,
    shipping JSON,
    detail_info JSON,

    -- 메타 정보
    last_synced_at TIMESTAMP,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    UNIQUE KEY uk_item_seller (must_it_item_no, seller_id),
    INDEX idx_last_synced (last_synced_at)
);

-- ProductSyncOutbox 테이블
CREATE TABLE product_sync_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    product_json JSON NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,

    INDEX idx_status_created (status, created_at)
);

