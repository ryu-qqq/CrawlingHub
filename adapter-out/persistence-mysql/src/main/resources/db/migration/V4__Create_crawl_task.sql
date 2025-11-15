-- =====================================================
-- Flyway Migration Script V4
-- =====================================================
-- 목적: crawl_task 테이블 생성
-- 작성자: ryu-qqq
-- 작성일: 2025-11-12
-- =====================================================

-- =====================================================
-- crawl_task (크롤링 작업)
-- =====================================================
-- 셀러별 크롤링 작업 정보를 저장
-- Idempotency Key를 통한 중복 작업 방지
CREATE TABLE `crawl_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '작업 PK',
    `seller_id` BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    `seller_name` VARCHAR(255) NOT NULL COMMENT '셀러 이름',
    `task_type` VARCHAR(50) NOT NULL COMMENT '작업 타입 (META, MINI_SHOP, PRODUCT_DETAIL, PRODUCT_OPTION)',
    `status` VARCHAR(20) NOT NULL COMMENT '작업 상태 (PENDING, PROCESSING, COMPLETED, FAILED)',
    `request_url` VARCHAR(1000) NOT NULL COMMENT '요청 URL',
    `page_number` INT COMMENT '페이지 번호 (페이지네이션)',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    `idempotency_key` VARCHAR(255) NOT NULL COMMENT 'Idempotency Key - 중복 작업 방지',
    `crawl_schedule_id` BIGINT COMMENT '크롤 스케줄 ID (Long FK)',
    `trigger_type` VARCHAR(20) NOT NULL COMMENT '트리거 타입 (SCHEDULED, MANUAL)',
    `scheduled_at` DATETIME NOT NULL COMMENT '예약 시각',
    `started_at` DATETIME COMMENT '시작 시각',
    `completed_at` DATETIME COMMENT '완료 시각',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
    INDEX `idx_seller_id` (`seller_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_scheduled_at` (`scheduled_at`),
    INDEX `idx_crawl_schedule_id` (`crawl_schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='크롤링 작업';
