-- ========================================
-- V3: Schedule 관련 테이블 (2개)
-- ========================================
-- 크롤링 스케줄 관리 테이블
-- ⚠️ FOREIGN KEY 제약조건 없음 (애플리케이션 레벨에서 관리)
-- ✅ INDEX는 필수 (조회 성능 최적화)
-- ========================================

-- ========================================
-- 1. crawl_schedule (크롤링 스케줄)
-- ========================================
CREATE TABLE crawl_schedule (
    schedule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '스케줄 고유 ID',
    workflow_id BIGINT NOT NULL COMMENT 'crawl_workflow.workflow_id 참조',
    schedule_name VARCHAR(200) NOT NULL COMMENT '스케줄 이름',
    schedule_type VARCHAR(50) NOT NULL COMMENT '스케줄 타입 (CRON, FIXED_RATE, FIXED_DELAY, ONE_TIME 등)',
    cron_expression VARCHAR(200) COMMENT 'Cron 표현식 (schedule_type이 CRON인 경우)',
    fixed_rate_seconds INT COMMENT '고정 주기 (초, FIXED_RATE인 경우)',
    fixed_delay_seconds INT COMMENT '고정 지연 (초, FIXED_DELAY인 경우)',
    scheduled_time DATETIME(6) COMMENT '예약 시각 (ONE_TIME인 경우)',
    start_date DATETIME(6) COMMENT '스케줄 시작일',
    end_date DATETIME(6) COMMENT '스케줄 종료일',
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC' COMMENT '타임존',
    priority INT NOT NULL DEFAULT 5 COMMENT '우선순위 (1-10, 낮을수록 높은 우선순위)',
    max_concurrent_executions INT NOT NULL DEFAULT 1 COMMENT '최대 동시 실행 수',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    last_execution_time DATETIME(6) COMMENT '마지막 실행 시각',
    next_execution_time DATETIME(6) COMMENT '다음 실행 예정 시각',
    description TEXT COMMENT '스케줄 설명',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (schedule_id),
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_schedule_name (schedule_name),
    INDEX idx_schedule_type (schedule_type),
    INDEX idx_is_enabled (is_enabled),
    INDEX idx_priority (priority),
    INDEX idx_last_execution_time (last_execution_time),
    INDEX idx_next_execution_time (next_execution_time),
    INDEX idx_enabled_next (is_enabled, next_execution_time),
    INDEX idx_workflow_enabled (workflow_id, is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 스케줄';

-- ========================================
-- 2. schedule_input_param (스케줄 입력 파라미터)
-- ========================================
CREATE TABLE schedule_input_param (
    param_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '파라미터 고유 ID',
    schedule_id BIGINT NOT NULL COMMENT 'crawl_schedule.schedule_id 참조',
    param_name VARCHAR(200) NOT NULL COMMENT '파라미터 이름',
    param_type VARCHAR(50) NOT NULL COMMENT '파라미터 타입 (STRING, INTEGER, BOOLEAN, JSON, DATE 등)',
    param_value TEXT COMMENT '파라미터 값',
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '암호화 여부',
    description VARCHAR(500) COMMENT '파라미터 설명',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (param_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_param_name (param_name),
    INDEX idx_param_type (param_type),
    INDEX idx_is_encrypted (is_encrypted),
    INDEX idx_schedule_param (schedule_id, param_name),
    UNIQUE KEY uk_schedule_param_name (schedule_id, param_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스케줄 입력 파라미터';
