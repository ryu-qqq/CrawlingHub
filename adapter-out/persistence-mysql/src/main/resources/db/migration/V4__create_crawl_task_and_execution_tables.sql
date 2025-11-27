-- Crawl Task 및 Execution 관련 테이블 생성
-- CrawlTaskJpaEntity, CrawlTaskOutboxJpaEntity, CrawlExecutionJpaEntity 기반 DDL

-- =====================================================
-- crawl_task 테이블
-- =====================================================
CREATE TABLE IF NOT EXISTS crawl_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키 (AUTO_INCREMENT)',
    crawl_scheduler_id BIGINT NOT NULL COMMENT '스케줄러 ID (Long FK)',
    seller_id BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    task_type VARCHAR(30) NOT NULL COMMENT '태스크 유형 (PRODUCT_LIST, PRODUCT_DETAIL 등)',
    endpoint_base_url VARCHAR(500) NOT NULL COMMENT '크롤링 Base URL',
    endpoint_path VARCHAR(500) NOT NULL COMMENT '크롤링 Path',
    endpoint_query_params TEXT COMMENT '크롤링 Query Params (JSON)',
    status VARCHAR(20) NOT NULL COMMENT '현재 상태 (PENDING, PROCESSING, COMPLETED, FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    INDEX idx_crawl_scheduler_id (crawl_scheduler_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_scheduler_status (crawl_scheduler_id, status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 태스크 테이블';

-- =====================================================
-- crawl_task_outbox 테이블
-- =====================================================
CREATE TABLE IF NOT EXISTS crawl_task_outbox (
    crawl_task_id BIGINT PRIMARY KEY COMMENT 'CrawlTask ID (PK, 1:1 관계)',
    idempotency_key VARCHAR(100) NOT NULL COMMENT 'Idempotency Key (SQS 중복 발행 방지)',
    payload TEXT NOT NULL COMMENT '발행 페이로드 (JSON)',
    status VARCHAR(20) NOT NULL COMMENT '현재 상태 (PENDING, SENT, FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    processed_at TIMESTAMP NULL COMMENT '처리 일시 (발행 성공/실패 시각)',

    UNIQUE KEY uk_idempotency_key (idempotency_key),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_status_created_at (status, created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 태스크 아웃박스 테이블 (Transactional Outbox 패턴)';

-- =====================================================
-- crawl_execution 테이블
-- =====================================================
CREATE TABLE IF NOT EXISTS crawl_execution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키 (AUTO_INCREMENT)',
    crawl_task_id BIGINT NOT NULL COMMENT 'CrawlTask ID (Long FK)',
    crawl_scheduler_id BIGINT NOT NULL COMMENT '스케줄러 ID (Long FK)',
    seller_id BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    status VARCHAR(20) NOT NULL COMMENT '실행 상태 (RUNNING, COMPLETED, FAILED)',
    response_body TEXT COMMENT '응답 본문 (성공 시 또는 에러 응답)',
    http_status_code INT COMMENT 'HTTP 상태 코드',
    error_message VARCHAR(1000) COMMENT '에러 메시지',
    started_at TIMESTAMP NOT NULL COMMENT '실행 시작 시각',
    completed_at TIMESTAMP NULL COMMENT '실행 완료 시각',
    duration_ms BIGINT COMMENT '실행 소요 시간 (밀리초)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    INDEX idx_crawl_task_id (crawl_task_id),
    INDEX idx_crawl_scheduler_id (crawl_scheduler_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_started_at (started_at),
    INDEX idx_scheduler_status_period (crawl_scheduler_id, status, created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 실행 이력 테이블';
