--[[
    UserAgent 토큰 소비 Lua Script

    KEYS[1]: ready set key (useragent:ready)
    KEYS[2]: pool key prefix (useragent:pool:)
    KEYS[3]: session_required set key (useragent:session_required)

    ARGV[1]: 현재 시간 (epoch millis)
    ARGV[2]: max tokens (80)
    ARGV[3]: window duration millis (1시간 = 3600000)

    RETURN: 선택된 UserAgent ID (없으면 nil)

    세션 만료된 UserAgent는 자동으로 SESSION_REQUIRED로 이동됨
]]

local readySetKey = KEYS[1]
local poolKeyPrefix = KEYS[2]
local sessionRequiredSetKey = KEYS[3]

local availableIds = redis.call('SMEMBERS', readySetKey)
if #availableIds == 0 then
    return nil
end

local now = tonumber(ARGV[1])
local maxTokens = tonumber(ARGV[2])
local windowDuration = tonumber(ARGV[3])

for _, id in ipairs(availableIds) do
    local poolKey = poolKeyPrefix .. id

    -- 세션 만료 체크 (핵심 버그 수정)
    local sessionExpiresAt = tonumber(redis.call('HGET', poolKey, 'sessionExpiresAt') or '0')
    if sessionExpiresAt > 0 and sessionExpiresAt < now then
        -- 세션 만료됨: READY → SESSION_REQUIRED로 이동
        redis.call('SREM', readySetKey, id)
        redis.call('SADD', sessionRequiredSetKey, id)
        redis.call('HSET', poolKey, 'cacheStatus', 'SESSION_REQUIRED')
        redis.call('HSET', poolKey, 'sessionToken', '')
        redis.call('HSET', poolKey, 'sessionExpiresAt', '0')
        -- 다음 UserAgent로 계속
    else
        -- 세션 유효: Rate Limit 체크
        local tokens = tonumber(redis.call('HGET', poolKey, 'remainingTokens') or '0')
        local windowEnd = tonumber(redis.call('HGET', poolKey, 'windowEnd') or '0')

        -- Window 만료 시 토큰 리셋
        if windowEnd > 0 and windowEnd < now then
            tokens = maxTokens
            redis.call('HSET', poolKey, 'remainingTokens', tokens)
            redis.call('HSET', poolKey, 'windowStart', now)
            redis.call('HSET', poolKey, 'windowEnd', now + windowDuration)
        end

        -- 토큰이 있으면 소비
        if tokens > 0 then
            redis.call('HINCRBY', poolKey, 'remainingTokens', -1)

            -- 첫 사용 시 window 설정
            local windowStart = tonumber(redis.call('HGET', poolKey, 'windowStart') or '0')
            if windowStart == 0 then
                redis.call('HSET', poolKey, 'windowStart', now)
                redis.call('HSET', poolKey, 'windowEnd', now + windowDuration)
            end

            return id
        end
    end
end

return nil
