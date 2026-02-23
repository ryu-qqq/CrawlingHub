--[[
    UserAgent Cooldown Recovery Lua Script (Housekeeper 호출)

    KEYS[1]: cooldown set key (useragent:cooldown)
    KEYS[2]: idle set key (useragent:idle)
    KEYS[3]: session_required set key (useragent:session_required)
    KEYS[4]: pool key prefix (useragent:pool:)

    ARGV[1]: now (epoch millis)

    RETURN: 복구된 UserAgent 수
]]

local cooldownSetKey = KEYS[1]
local idleSetKey = KEYS[2]
local sessionRequiredSetKey = KEYS[3]
local poolKeyPrefix = KEYS[4]

local now = tonumber(ARGV[1])

local members = redis.call('SMEMBERS', cooldownSetKey)
local recovered = 0

for _, id in ipairs(members) do
    local key = poolKeyPrefix .. id
    local cooldownUntil = tonumber(redis.call('HGET', key, 'cooldownUntil') or '0')

    if cooldownUntil > 0 and now >= cooldownUntil then
        local sessionExpires = tonumber(redis.call('HGET', key, 'sessionExpiresAt') or '0')
        redis.call('SREM', cooldownSetKey, id)

        if sessionExpires > 0 and sessionExpires > now then
            -- 세션 유효 → IDLE
            redis.call('HSET', key, 'status', 'IDLE', 'cooldownUntil', '0')
            redis.call('SADD', idleSetKey, id)
        else
            -- 세션 만료 → SESSION_REQUIRED
            redis.call('HSET', key, 'status', 'SESSION_REQUIRED', 'cooldownUntil', '0')
            redis.call('SADD', sessionRequiredSetKey, id)
        end
        recovered = recovered + 1
    end
end

return recovered
