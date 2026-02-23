-- Phase 3: UserAgent Pool 리팩토링 - 상태 변경 및 신규 컬럼 추가

-- 1. status 값 변경 (READY → IDLE)
UPDATE user_agent SET status = 'IDLE' WHERE status = 'READY';

-- 2. 신규 컬럼 추가
ALTER TABLE user_agent
    ADD COLUMN cooldown_until DATETIME(6) NULL AFTER requests_per_day,
    ADD COLUMN consecutive_rate_limits INT NOT NULL DEFAULT 0 AFTER cooldown_until;
