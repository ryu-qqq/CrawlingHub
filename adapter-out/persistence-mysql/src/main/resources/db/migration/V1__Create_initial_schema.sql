-- =====================================================
-- Flyway Migration Script V1
-- =====================================================
-- 목적: 초기 데이터베이스 스키마 생성
-- 작성자: ryu-qqq
-- 작성일: 2025-11-12
-- =====================================================

-- =====================================================
-- seller_crawl_schedule_outbox (셀러 크롤링 스케줄 Outbox)
-- =====================================================
-- Orchestrator SDK의 Write-Ahead Log (WAL) 패턴을 위한 Outbox 테이블
-- 비동기 스케줄 생성/수정/삭제를 추적하고 재시도 처리를 담당
CREATE TABLE `seller_crawl_schedule_outbox` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Outbox PK',
    `op_id` VARCHAR(36) COMMENT 'Orchestrator OpId (UUID) - 초기 저장 시 NULL',
    `seller_id` BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    `idem_key` VARCHAR(100) NOT NULL COMMENT 'Idempotency Key - 중복 실행 방지',
    `event_type` VARCHAR(50) NOT NULL COMMENT 'EventBridge 이벤트 타입 (REGISTER, UPDATE, DELETE)',
    `payload` TEXT NOT NULL COMMENT 'EventBridge Schedule 생성/수정을 위한 JSON 페이로드',
    `outcome_json` TEXT COMMENT '작업 결과 JSON',
    `operation_state` VARCHAR(20) NOT NULL COMMENT '작업 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)',
    `wal_state` VARCHAR(20) NOT NULL COMMENT 'WAL 상태 (PENDING, COMPLETED)',
    `error_message` TEXT COMMENT '에러 메시지',
    `retry_count` INT NOT NULL COMMENT '현재 재시도 횟수',
    `max_retries` INT NOT NULL COMMENT '최대 재시도 횟수',
    `timeout_millis` BIGINT NOT NULL COMMENT '타임아웃 시간 (밀리초)',
    `completed_at` DATETIME COMMENT '완료 일시',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_op_id` (`op_id`),
    UNIQUE KEY `uk_idem_key` (`idem_key`),
    INDEX `idx_seller_id` (`seller_id`),
    INDEX `idx_operation_state` (`operation_state`),
    INDEX `idx_wal_state` (`wal_state`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='셀러 크롤링 스케줄 Outbox (WAL 패턴)';

-- =====================================================
-- crawl_schedule (크롤링 스케줄)
-- =====================================================
-- 셀러별 크롤링 스케줄 정보를 저장
-- Cron 표현식 기반으로 실행 시간 관리
CREATE TABLE `crawl_schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '스케줄 PK',
    `seller_id` BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    `cron_expression` VARCHAR(100) NOT NULL COMMENT 'Cron 표현식 (예: 0 0 * * * ?)',
    `status` VARCHAR(20) NOT NULL COMMENT '스케줄 상태 (ACTIVE, PAUSED, DELETED)',
    `next_execution_time` DATETIME COMMENT '다음 실행 예정 시간',
    `last_executed_at` DATETIME COMMENT '마지막 실행 시간',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    INDEX `idx_seller_id` (`seller_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_next_execution_time` (`next_execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='크롤링 스케줄';

-- =====================================================
-- must_it_seller (머스트잇 셀러)
-- =====================================================
-- 머스트잇 플랫폼의 셀러 정보를 저장
-- 크롤링 대상 셀러 관리
CREATE TABLE `must_it_seller` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '셀러 PK',
    `seller_code` VARCHAR(100) NOT NULL COMMENT '셀러 코드 (Unique)',
    `seller_name` VARCHAR(255) NOT NULL COMMENT '셀러 이름',
    `status` VARCHAR(20) NOT NULL COMMENT '셀러 상태 (ACTIVE, INACTIVE, DELETED)',
    `total_product_count` INT NOT NULL COMMENT '총 상품 수',
    `last_crawled_at` DATETIME COMMENT '마지막 크롤링 시간',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seller_code` (`seller_code`),
    INDEX `idx_seller_code` (`seller_code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='머스트잇 셀러';

-- =====================================================
-- user_agent (User Agent 관리)
-- =====================================================
-- 크롤링에 사용할 User Agent 및 토큰 정보를 저장
-- Rate Limit 관리 및 토큰 로테이션 지원
CREATE TABLE `user_agent` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User Agent PK',
    `user_agent_string` VARCHAR(500) NOT NULL COMMENT 'User-Agent 문자열',
    `current_token` VARCHAR(500) COMMENT '현재 사용 중인 토큰',
    `token_status` VARCHAR(20) NOT NULL COMMENT '토큰 상태 (ACTIVE, EXPIRED, INVALID)',
    `remaining_requests` INT NOT NULL COMMENT '남은 요청 수 (Rate Limit)',
    `token_issued_at` DATETIME COMMENT '토큰 발급 시간',
    `rate_limit_reset_at` DATETIME COMMENT 'Rate Limit 리셋 시간',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    INDEX `idx_token_status` (`token_status`),
    INDEX `idx_remaining_requests` (`remaining_requests`),
    INDEX `idx_rate_limit_reset_at` (`rate_limit_reset_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Agent 관리';

-- =====================================================
-- crawl_result (크롤링 결과)
-- =====================================================
-- 크롤링 작업의 원본 데이터를 저장
-- Task 기반 크롤링 결과 추적
CREATE TABLE `crawl_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '크롤링 결과 PK',
    `task_id` BIGINT NOT NULL COMMENT '작업 ID (Long FK)',
    `task_type` VARCHAR(50) NOT NULL COMMENT '작업 타입 (META, PRODUCT, OPTION 등)',
    `seller_id` BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    `raw_data` JSON NOT NULL COMMENT '원본 크롤링 데이터 (JSON)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_seller_id` (`seller_id`),
    INDEX `idx_task_type` (`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='크롤링 결과';
