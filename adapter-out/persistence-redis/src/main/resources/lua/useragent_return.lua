--[[
    UserAgent Return Lua Script (HikariCP connection.close 대응)

    KEYS[1]: borrowed set key (useragent:borrowed)
    KEYS[2]: idle set key (useragent:idle)
    KEYS[3]: cooldown set key (useragent:cooldown)
    KEYS[4]: suspended set key (useragent:suspended)
    KEYS[5]: pool key prefix (useragent:pool:)

    ARGV[1]: userAgentId
    ARGV[2]: success (1/0)
    ARGV[3]: httpStatusCode
    ARGV[4]: now (epoch millis)
    ARGV[5]: healthDelta (양수=증가, 음수=감소)
    ARGV[6]: cooldownUntil (epoch millis, 0 if none)
    ARGV[7]: consecutiveRateLimits
    ARGV[8]: suspensionThreshold

    RETURN: 0=IDLE, 1=COOLDOWN, 2=SUSPENDED
]]

local borrowedSetKey = KEYS[1]
local idleSetKey = KEYS[2]
local cooldownSetKey = KEYS[3]
local suspendedSetKey = KEYS[4]
local poolKeyPrefix = KEYS[5]

local userAgentId = ARGV[1]
local success = tonumber(ARGV[2])
local httpStatusCode = tonumber(ARGV[3])
local now = ARGV[4]
local healthDelta = tonumber(ARGV[5])
local cooldownUntil = ARGV[6]
local consecutiveRateLimits = ARGV[7]
local suspensionThreshold = tonumber(ARGV[8])

local key = poolKeyPrefix .. userAgentId

-- 1. BORROWED Set에서 제거
redis.call('SREM', borrowedSetKey, userAgentId)

local health = tonumber(redis.call('HGET', key, 'healthScore') or '100')
local newHealth = math.max(math.min(health + healthDelta, 100), 0)

if success == 1 then
    -- 성공: BORROWED → IDLE
    redis.call('HSET', key,
        'status', 'IDLE',
        'healthScore', newHealth,
        'borrowedAt', '0',
        'consecutiveRateLimits', '0',
        'cooldownUntil', '0')
    redis.call('SADD', idleSetKey, userAgentId)
    return 0
else
    if httpStatusCode == 429 then
        local consecutive = tonumber(consecutiveRateLimits)
        if consecutive >= 5 then
            -- 연속 5회 429 → SUSPENDED
            redis.call('HSET', key,
                'status', 'SUSPENDED',
                'healthScore', newHealth,
                'borrowedAt', '0',
                'suspendedAt', now,
                'consecutiveRateLimits', consecutiveRateLimits)
            redis.call('SADD', suspendedSetKey, userAgentId)
            return 2
        else
            -- BORROWED → COOLDOWN
            redis.call('HSET', key,
                'status', 'COOLDOWN',
                'healthScore', newHealth,
                'cooldownUntil', cooldownUntil,
                'consecutiveRateLimits', consecutiveRateLimits,
                'borrowedAt', '0')
            redis.call('SADD', cooldownSetKey, userAgentId)
            return 1
        end
    elseif newHealth < suspensionThreshold then
        -- Health 임계값 이하 → SUSPENDED
        redis.call('HSET', key,
            'status', 'SUSPENDED',
            'healthScore', newHealth,
            'borrowedAt', '0',
            'suspendedAt', now)
        redis.call('SADD', suspendedSetKey, userAgentId)
        return 2
    else
        -- 경미한 실패 → IDLE
        redis.call('HSET', key,
            'status', 'IDLE',
            'healthScore', newHealth,
            'borrowedAt', '0')
        redis.call('SADD', idleSetKey, userAgentId)
        return 0
    end
end
