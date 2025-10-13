-- ========================================
-- Distributed Unlock (분산 락 해제)
-- ========================================
-- KEYS[1]: lock:{resource_id}
-- ARGV[1]: lock_owner (락 소유자 ID, UUID)
-- ========================================
-- 반환값:
--   1: 락 해제 성공
--   0: 락 해제 실패 (소유자 불일치 또는 락 없음)
-- ========================================

local lock_key = KEYS[1]
local lock_owner = ARGV[1]

-- 현재 락 소유자 확인
local current_owner = redis.call('GET', lock_key)

if current_owner == lock_owner then
    -- 소유자가 일치하면 락 해제
    redis.call('DEL', lock_key)
    return 1
else
    -- 소유자 불일치 또는 락 없음
    return 0
end
