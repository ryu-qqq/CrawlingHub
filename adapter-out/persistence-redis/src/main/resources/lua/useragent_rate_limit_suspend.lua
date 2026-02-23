--[[
    UserAgent Rate Limit Suspend Lua Script

    세션 만료 + Pool 제거를 원자적으로 수행합니다.
    expireSession + removeFromPool을 하나의 트랜잭션으로 묶습니다.

    KEYS[1]: pool key (useragent:pool:{id})
    KEYS[2]: ready set key (useragent:ready)
    KEYS[3]: session_required set key (useragent:session_required)
    KEYS[4]: suspended set key (useragent:suspended)

    ARGV[1]: userAgentId
    ARGV[2]: suspendedAt (epoch millis)

    RETURN: 1
]]

local userAgentId = ARGV[1]
local suspendedAt = ARGV[2]

-- 세션 데이터 초기화
redis.call('HSET', KEYS[1], 'sessionToken', '')
redis.call('HSET', KEYS[1], 'nid', '')
redis.call('HSET', KEYS[1], 'mustitUid', '')
redis.call('HSET', KEYS[1], 'sessionExpiresAt', '0')

-- SUSPENDED 상태로 전환
redis.call('HSET', KEYS[1], 'status', 'SUSPENDED')
redis.call('HSET', KEYS[1], 'suspendedAt', suspendedAt)

-- Set 이동: ready/session_required에서 제거, suspended에 추가
redis.call('SREM', KEYS[2], userAgentId)
redis.call('SREM', KEYS[3], userAgentId)
redis.call('SADD', KEYS[4], userAgentId)

return 1
