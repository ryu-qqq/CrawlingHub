--[[
    UserAgent 실패 기록 Lua Script

    KEYS[1]: pool key (useragent:pool:{id})
    KEYS[2]: available set key (useragent:available)
    KEYS[3]: suspended set key (useragent:suspended)

    ARGV[1]: penalty (5 or 10)
    ARGV[2]: threshold (30)
    ARGV[3]: userAgentId
    ARGV[4]: suspendedAt (epoch millis)

    RETURN: 1 if SUSPENDED, 0 otherwise
]]

local penalty = tonumber(ARGV[1])
local threshold = tonumber(ARGV[2])
local userAgentId = ARGV[3]
local suspendedAt = ARGV[4]

local healthScore = tonumber(redis.call('HGET', KEYS[1], 'healthScore') or '100')
local newHealth = math.max(healthScore - penalty, 0)
redis.call('HSET', KEYS[1], 'healthScore', newHealth)

-- Health Score < threshold 시 SUSPENDED 처리
if newHealth < threshold then
    redis.call('HSET', KEYS[1], 'status', 'SUSPENDED')
    redis.call('HSET', KEYS[1], 'suspendedAt', suspendedAt)
    redis.call('SREM', KEYS[2], userAgentId)
    redis.call('SADD', KEYS[3], userAgentId)
    return 1
end

return 0
