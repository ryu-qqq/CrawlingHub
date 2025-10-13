-- ========================================
-- Distributed Lock (분산 락)
-- ========================================
-- KEYS[1]: lock:{resource_id}
-- ARGV[1]: lock_owner (락 소유자 ID, UUID)
-- ARGV[2]: ttl_seconds (락 타임아웃, 기본 30초)
-- ========================================
-- 반환값:
--   1: 락 획득 성공
--   0: 락 획득 실패 (이미 다른 소유자가 보유)
-- ========================================

local lock_key = KEYS[1]
local lock_owner = ARGV[1]
local ttl_seconds = tonumber(ARGV[2])

-- SET NX EX: 키가 존재하지 않을 때만 설정 + TTL
local result = redis.call('SET', lock_key, lock_owner, 'NX', 'EX', ttl_seconds)

if result then
    return 1
else
    return 0
end
