--[[
    UserAgent Borrow Lua Script (HikariCP getConnection 대응)

    KEYS[1]: idle set key (useragent:idle)
    KEYS[2]: pool key prefix (useragent:pool:)
    KEYS[3]: borrowed set key (useragent:borrowed)
    KEYS[4]: session_required set key (useragent:session_required)

    ARGV[1]: 현재 시간 (epoch millis)
    ARGV[2]: max tokens (80)
    ARGV[3]: window duration millis (1시간 = 3600000)

    RETURN: 선택된 UserAgent ID (없으면 nil)
]]

local idleSetKey = KEYS[1]
local poolKeyPrefix = KEYS[2]
local borrowedSetKey = KEYS[3]
local sessionRequiredSetKey = KEYS[4]

local now = tonumber(ARGV[1])
local maxTokens = tonumber(ARGV[2])
local windowDuration = tonumber(ARGV[3])

-- HikariCP SharedList 스캔 대응: 3개 후보 랜덤 선택
local candidates = redis.call('SRANDMEMBER', idleSetKey, 3)
if #candidates == 0 then
    -- fallback: 전체 스캔
    candidates = redis.call('SMEMBERS', idleSetKey)
    if #candidates == 0 then
        return nil
    end
end

for _, id in ipairs(candidates) do
    local poolKey = poolKeyPrefix .. id

    -- 1. 세션 만료 체크 (HikariCP isAlive 대응)
    local sessionExpiresAt = tonumber(redis.call('HGET', poolKey, 'sessionExpiresAt') or '0')
    if sessionExpiresAt > 0 and sessionExpiresAt < now then
        -- IDLE → SESSION_REQUIRED (Soft Eviction)
        redis.call('SMOVE', idleSetKey, sessionRequiredSetKey, id)
        redis.call('HSET', poolKey, 'status', 'SESSION_REQUIRED')
        redis.call('HSET', poolKey, 'sessionToken', '')
        redis.call('HSET', poolKey, 'sessionExpiresAt', '0')
    else
        -- 2. Token Bucket: Lazy Refill
        local tokens = tonumber(redis.call('HGET', poolKey, 'remainingTokens') or '0')
        local windowEnd = tonumber(redis.call('HGET', poolKey, 'windowEnd') or '0')

        if windowEnd > 0 and windowEnd < now then
            tokens = maxTokens
            redis.call('HSET', poolKey, 'remainingTokens', tokens)
            redis.call('HSET', poolKey, 'windowStart', now)
            redis.call('HSET', poolKey, 'windowEnd', now + windowDuration)
        end

        if tokens > 0 then
            -- 3. IDLE → BORROWED (CAS 전환)
            tokens = tokens - 1
            redis.call('SMOVE', idleSetKey, borrowedSetKey, id)
            redis.call('HSET', poolKey,
                'status', 'BORROWED',
                'borrowedAt', now,
                'remainingTokens', tokens)

            -- 첫 사용이면 윈도우 시작
            local windowStart = tonumber(redis.call('HGET', poolKey, 'windowStart') or '0')
            if windowStart == 0 then
                redis.call('HSET', poolKey,
                    'windowStart', now,
                    'windowEnd', now + windowDuration)
            end

            return id
        end
    end
end

return nil
