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
-- User-Agent Pool Seed 데이터 삭제 (V8에서 추가한 100개만 명시적으로 삭제)
-- ⚠️ 주의: V8에서 추가된 특정 User-Agent만 삭제하여 데이터 손실 방지
DELETE FROM user_agent_pool
WHERE user_agent IN (
    'Mozilla/5.0 (Linux; Android 11; Pixel 6) AppleWebKit/5251.0 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/5251.0',
    'Mozilla/5.0 (Linux; Android 11; Pixel 6) AppleWebKit/5324.0 (KHTML, like Gecko) Chrome/113.0.0.0 Mobile Safari/5324.0',
    'Mozilla/5.0 (Linux; Android 11; Pixel 6) AppleWebKit/5379.0 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/5379.0',
    'Mozilla/5.0 (Linux; Android 11; Pixel 6) AppleWebKit/5842.0 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/5842.0',
    'Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/5780.0 (KHTML, like Gecko) Chrome/113.0.0.0 Mobile Safari/5780.0',
    'Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/5925.0 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/5925.0',
    'Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/6101.0 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/6101.0',
    'Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/5661.0 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/5661.0',
    'Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/5850.0 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/5850.0',
    'Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/5923.0 (KHTML, like Gecko) Chrome/113.0.0.0 Mobile Safari/5923.0',
    'Mozilla/5.0 (Linux; Android 14; Pixel 6) AppleWebKit/5705.0 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/5705.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15.7; rv:114.0) Gecko/20100101 Firefox/114.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15.7; rv:119.0) Gecko/20100101 Firefox/119.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/5216.0 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/5216.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_0) AppleWebKit/5311.0 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/5311.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_0) AppleWebKit/5323.0 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/5323.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 12.0.0; rv:125.0) Gecko/20100101 Firefox/125.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 12.1.0; rv:113.0) Gecko/20100101 Firefox/113.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 12_0_0) AppleWebKit/618.1.15 (KHTML, like Gecko) Version/17.0 Safari/618.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 12_0_0) AppleWebKit/620.1.15 (KHTML, like Gecko) Version/16.2 Safari/620.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 12_1_0) AppleWebKit/608.1.15 (KHTML, like Gecko) Version/16.0 Safari/608.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 12_1_0) AppleWebKit/609.1.15 (KHTML, like Gecko) Version/16.3 Safari/609.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 12_1_0) AppleWebKit/610.1.15 (KHTML, like Gecko) Version/16.5 Safari/610.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 12_1_0) AppleWebKit/612.1.15 (KHTML, like Gecko) Version/16.2 Safari/612.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 12_1_0) AppleWebKit/614.1.15 (KHTML, like Gecko) Version/17.1 Safari/614.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 13.0.0; rv:112.0) Gecko/20100101 Firefox/112.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 13.1.0; rv:111.0) Gecko/20100101 Firefox/111.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 13_0_0) AppleWebKit/5269.0 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/5269.0 Edg/121.0.0.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 13_0_0) AppleWebKit/5304.0 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/5304.0 Edg/122.0.0.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 13_0_0) AppleWebKit/5899.0 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/5899.0 Edg/114.0.0.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 13_0_0) AppleWebKit/608.1.15 (KHTML, like Gecko) Version/16.2 Safari/608.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 13_0_0) AppleWebKit/608.1.15 (KHTML, like Gecko) Version/17.1 Safari/608.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 13_1_0) AppleWebKit/5202.0 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/5202.0 Edg/121.0.0.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 14.0.0; rv:123.0) Gecko/20100101 Firefox/123.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0_0) AppleWebKit/615.1.15 (KHTML, like Gecko) Version/16.4 Safari/615.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0_0) AppleWebKit/618.1.15 (KHTML, like Gecko) Version/16.4 Safari/618.1.15',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/5206.0 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/5206.0 Edg/114.0.0.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/5411.0 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/5411.0 Edg/118.0.0.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/5412.0 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/5412.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/5498.0 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/5498.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/5574.0 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/5574.0 Edg/123.0.0.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/5643.0 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/5643.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/5671.0 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/5671.0 Edg/118.0.0.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/5901.0 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/5901.0 Edg/116.0.0.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/5972.0 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/5972.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/6007.0 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/6007.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/6048.0 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/6048.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/6051.0 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/6051.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:114.0) Gecko/20100101 Firefox/114.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:116.0) Gecko/20100101 Firefox/116.0',
    'Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/5459.0 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/5459.0 Edg/119.0.0.0',
    'Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/5839.0 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/5839.0 Edg/119.0.0.0',
    'Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/6004.0 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/6004.0',
    'Mozilla/5.0 (Windows NT 11.0; Win64; x64; rv:111.0) Gecko/20100101 Firefox/111.0',
    'Mozilla/5.0 (Windows NT 11.0; Win64; x64; rv:118.0) Gecko/20100101 Firefox/118.0',
    'Mozilla/5.0 (Windows NT 11.0; Win64; x64; rv:119.0) Gecko/20100101 Firefox/119.0',
    'Mozilla/5.0 (Windows NT 11.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0',
    'Mozilla/5.0 (Windows NT 11.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0',
    'Mozilla/5.0 (X11; Linux x86_64; rv:115.0) Gecko/20100101 Firefox/115.0',
    'Mozilla/5.0 (X11; Linux x86_64; rv:120.0) Gecko/20100101 Firefox/120.0',
    'Mozilla/5.0 (X11; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0',
    'Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/5509.0 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/5509.0',
    'Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/5685.0 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/5685.0',
    'Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/5780.0 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/5780.0',
    'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:118.0) Gecko/20100101 Firefox/118.0',
    'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:120.0) Gecko/20100101 Firefox/120.0',
    'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:121.0) Gecko/20100101 Firefox/121.0',
    'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/5358.0 (KHTML, like Gecko) CriOS/126.0.0.0 Mobile/15E148 Safari/5358.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/5531.0 (KHTML, like Gecko) CriOS/120.0.0.0 Mobile/15E148 Safari/5531.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/5554.0 (KHTML, like Gecko) CriOS/128.0.0.0 Mobile/15E148 Safari/5554.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/616.1.15 (KHTML, like Gecko) Version/16.2 Mobile/15E148 Safari/616.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/616.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/616.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_1 like Mac OS X) AppleWebKit/5256.0 (KHTML, like Gecko) CriOS/123.0.0.0 Mobile/15E148 Safari/5256.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_1 like Mac OS X) AppleWebKit/5918.0 (KHTML, like Gecko) CriOS/120.0.0.0 Mobile/15E148 Safari/5918.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_1 like Mac OS X) AppleWebKit/608.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/608.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_1 like Mac OS X) AppleWebKit/618.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/618.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 15_1 like Mac OS X) AppleWebKit/620.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/620.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/5158.0 (KHTML, like Gecko) CriOS/122.0.0.0 Mobile/15E148 Safari/5158.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/5531.0 (KHTML, like Gecko) CriOS/113.0.0.0 Mobile/15E148 Safari/5531.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/607.1.15 (KHTML, like Gecko) Version/16.4 Mobile/15E148 Safari/607.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/609.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/609.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_1 like Mac OS X) AppleWebKit/5131.0 (KHTML, like Gecko) CriOS/116.0.0.0 Mobile/15E148 Safari/5131.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_1 like Mac OS X) AppleWebKit/609.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/609.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_1 like Mac OS X) AppleWebKit/614.1.15 (KHTML, like Gecko) Version/16.3 Mobile/15E148 Safari/614.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_1 like Mac OS X) AppleWebKit/614.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/614.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_1 like Mac OS X) AppleWebKit/618.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/618.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_1 like Mac OS X) AppleWebKit/620.1.15 (KHTML, like Gecko) Version/16.1 Mobile/15E148 Safari/620.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/605.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/6050.0 (KHTML, like Gecko) CriOS/127.0.0.0 Mobile/15E148 Safari/6050.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/608.1.15 (KHTML, like Gecko) Version/16.1 Mobile/15E148 Safari/608.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/612.1.15 (KHTML, like Gecko) Version/16.4 Mobile/15E148 Safari/612.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/617.1.15 (KHTML, like Gecko) Version/16.4 Mobile/15E148 Safari/617.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/617.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/617.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_2 like Mac OS X) AppleWebKit/619.1.15 (KHTML, like Gecko) Version/16.4 Mobile/15E148 Safari/619.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/5323.0 (KHTML, like Gecko) CriOS/124.0.0.0 Mobile/15E148 Safari/5323.0',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/616.1.15 (KHTML, like Gecko) Version/16.3 Mobile/15E148 Safari/616.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/619.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/619.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/620.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/620.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/610.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/610.1'
);

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
