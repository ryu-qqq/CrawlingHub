--[[
    UserAgent 성공 기록 Lua Script

    KEYS[1]: pool key (useragent:pool:{id})

    RETURN: 새로운 Health Score
]]

local healthScore = tonumber(redis.call('HGET', KEYS[1], 'healthScore') or '100')
local newHealth = math.min(healthScore + 5, 100)
redis.call('HSET', KEYS[1], 'healthScore', newHealth)

return newHealth
