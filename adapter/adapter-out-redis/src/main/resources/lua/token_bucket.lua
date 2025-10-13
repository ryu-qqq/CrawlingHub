-- ========================================
-- Token Bucket Algorithm (Rate Limiting)
-- ========================================
-- KEYS[1]: rate_limit:bucket:{user_agent_id}
-- ARGV[1]: tokens_to_consume (요청할 토큰 수, 기본 1)
-- ARGV[2]: current_timestamp (현재 시각, milliseconds)
-- ARGV[3]: refill_rate (tokens per second, 예: 0.1333 = 80 tokens/600 seconds)
-- ARGV[4]: max_tokens (최대 토큰 수, 예: 80)
-- ARGV[5]: ttl_seconds (TTL, 3600 = 1시간)
-- ========================================
-- 반환값:
--   success: 1 (허용) / 0 (거부)
--   current_tokens: 현재 남은 토큰 수
--   retry_after_ms: 거부 시 재시도까지 대기 시간 (ms)
-- ========================================

local bucket_key = KEYS[1]
local tokens_to_consume = tonumber(ARGV[1])
local current_timestamp = tonumber(ARGV[2])
local refill_rate = tonumber(ARGV[3])
local max_tokens = tonumber(ARGV[4])
local ttl_seconds = tonumber(ARGV[5])

-- 기존 Bucket 데이터 조회
local bucket_data = redis.call('HMGET', bucket_key, 'tokens', 'last_refill_timestamp')
local current_tokens = tonumber(bucket_data[1]) or max_tokens
local last_refill_timestamp = tonumber(bucket_data[2]) or current_timestamp

-- 경과 시간 계산 (초 단위)
local elapsed_seconds = (current_timestamp - last_refill_timestamp) / 1000.0

-- Token Refill 계산
local tokens_to_add = elapsed_seconds * refill_rate
current_tokens = math.min(max_tokens, current_tokens + tokens_to_add)

-- Token 소비 가능 여부 판단
if current_tokens >= tokens_to_consume then
    -- 소비 성공
    current_tokens = current_tokens - tokens_to_consume

    -- Redis에 업데이트
    redis.call('HSET', bucket_key,
        'tokens', tostring(current_tokens),
        'last_refill_timestamp', tostring(current_timestamp),
        'max_tokens', tostring(max_tokens),
        'refill_rate', tostring(refill_rate)
    )
    redis.call('EXPIRE', bucket_key, ttl_seconds)

    return {1, current_tokens, 0}
else
    -- 소비 실패 - 재시도 시간 계산
    local tokens_needed = tokens_to_consume - current_tokens
    local retry_after_seconds = tokens_needed / refill_rate
    local retry_after_ms = math.ceil(retry_after_seconds * 1000)

    -- 현재 상태만 업데이트 (TTL 갱신)
    redis.call('HSET', bucket_key,
        'tokens', tostring(current_tokens),
        'last_refill_timestamp', tostring(current_timestamp),
        'max_tokens', tostring(max_tokens),
        'refill_rate', tostring(refill_rate)
    )
    redis.call('EXPIRE', bucket_key, ttl_seconds)

    return {0, current_tokens, retry_after_ms}
end
