-- ========================================
-- Rollback Script for V7 & V8
-- ========================================
-- Token & Rate Limiting 시스템 테이블 롤백
-- V8 (Seed Data) → V7 (Tables) 순서로 롤백
-- ⚠️ 주의: 데이터 손실이 발생합니다!
-- ========================================

-- ========================================
-- 1. V8 롤백: Seed Data 삭제
-- ========================================
-- User-Agent Pool Seed 데이터 삭제
DELETE FROM user_agent_pool WHERE usage_count = 0 AND success_count = 0 AND failure_count = 0;

-- ========================================
-- 2. V7 롤백: 테이블 삭제 (역순)
-- ========================================

-- 7. token_refresh_schedule 삭제
DROP TABLE IF EXISTS token_refresh_schedule;

-- 6. rate_limit_bucket 삭제
DROP TABLE IF EXISTS rate_limit_bucket;

-- 5. circuit_breaker_event 삭제
DROP TABLE IF EXISTS circuit_breaker_event;

-- 4. circuit_breaker_state 삭제
DROP TABLE IF EXISTS circuit_breaker_state;

-- 3. token_usage_log 삭제 (파티셔닝 포함)
DROP TABLE IF EXISTS token_usage_log;

-- 2. user_agent_token 삭제
DROP TABLE IF EXISTS user_agent_token;

-- 1. user_agent_pool 삭제
DROP TABLE IF EXISTS user_agent_pool;

-- ========================================
-- 롤백 완료 확인
-- ========================================
SELECT
    'V7/V8 롤백 완료' AS status,
    COUNT(*) AS remaining_tables
FROM
    INFORMATION_SCHEMA.TABLES
WHERE
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN (
        'user_agent_pool',
        'user_agent_token',
        'token_usage_log',
        'circuit_breaker_state',
        'circuit_breaker_event',
        'rate_limit_bucket',
        'token_refresh_schedule'
    );
