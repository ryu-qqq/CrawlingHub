-- Redis Distributed Lock Unlock Script
-- 락 소유자만 해제 가능하도록 보장

local lockKey = KEYS[1]
local lockValue = ARGV[1]

local currentValue = redis.call('GET', lockKey)

if currentValue == lockValue then
    redis.call('DEL', lockKey)
    return 1
else
    return 0
end
