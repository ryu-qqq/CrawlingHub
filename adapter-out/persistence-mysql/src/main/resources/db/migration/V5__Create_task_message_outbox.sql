-- =====================================================
-- Flyway Migration Script V5
-- =====================================================
-- 목적: task_message_outbox 테이블 생성 (Outbox 패턴)
-- 작성자: ryu-qqq
-- 작성일: 2025-11-12
-- =====================================================

-- =====================================================
-- task_message_outbox (Task 메시지 Outbox)
-- =====================================================
-- Task 생성 후 SQS 메시지 발행을 안정적으로 보장하기 위한 Outbox 패턴 테이블
-- Task 저장과 동시에 Outbox 레코드 생성 (트랜잭션 보장)
-- Scheduler가 PENDING 레코드를 주기적으로 조회하여 SQS 발행
CREATE TABLE `task_message_outbox` (
    `outbox_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Outbox PK',
    `task_id` BIGINT NOT NULL COMMENT 'Task ID (Long FK)',
    `task_type` VARCHAR(50) NOT NULL COMMENT 'Task 타입 (META, MINI_SHOP, PRODUCT_DETAIL, PRODUCT_OPTION)',
    `status` VARCHAR(20) NOT NULL COMMENT 'Outbox 상태 (PENDING, SENT)',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수 (최대 3회)',
    `error_message` VARCHAR(1000) COMMENT '에러 메시지 (발행 실패 시)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `sent_at` DATETIME COMMENT '발행 완료 일시',
    PRIMARY KEY (`outbox_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Task 메시지 Outbox';
