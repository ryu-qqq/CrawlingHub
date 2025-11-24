-- Crawl Scheduler 테이블 생성
-- CrawlSchedulerJpaEntity, CrawlSchedulerHistoryJpaEntity, CrawlSchedulerOutBoxJpaEntity 기반 DDL

-- crawl_scheduler 테이블
CREATE TABLE IF NOT EXISTS crawl_scheduler (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키 (AUTO_INCREMENT)',
    seller_id BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    scheduler_name VARCHAR(100) NOT NULL COMMENT '스케줄러 이름',
    cron_expression VARCHAR(100) NOT NULL COMMENT '크론 표현식',
    status VARCHAR(20) NOT NULL COMMENT '스케줄러 상태 (ACTIVE/INACTIVE)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_seller_scheduler_name (seller_id, scheduler_name)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 스케줄러 테이블';

-- crawl_scheduler_history 테이블
CREATE TABLE IF NOT EXISTS crawl_scheduler_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키 (AUTO_INCREMENT)',
    crawl_scheduler_id BIGINT NOT NULL COMMENT '스케줄러 ID (Long FK)',
    seller_id BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    scheduler_name VARCHAR(100) NOT NULL COMMENT '스케줄러 이름',
    cron_expression VARCHAR(100) NOT NULL COMMENT '크론 표현식',
    status VARCHAR(20) NOT NULL COMMENT '스케줄러 상태',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    INDEX idx_crawl_scheduler_id (crawl_scheduler_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_created_at (created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 스케줄러 히스토리 테이블';

-- crawl_scheduler_outbox 테이블
CREATE TABLE IF NOT EXISTS crawl_scheduler_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키 (AUTO_INCREMENT)',
    history_id BIGINT NOT NULL COMMENT '히스토리 ID (Long FK)',
    status VARCHAR(20) NOT NULL COMMENT '아웃박스 상태 (PENDING/COMPLETED/FAILED)',
    event_payload TEXT NOT NULL COMMENT '이벤트 페이로드 (JSON)',
    error_message VARCHAR(500) COMMENT '에러 메시지',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '버전 (Optimistic Locking)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    INDEX idx_history_id (history_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 스케줄러 아웃박스 테이블';
