--[[
  Circuit Breaker - Record Success (Atomic)

  Atomically records a success and transitions circuit state if threshold is reached.
  Prevents lost updates in concurrent environments.

  KEYS[1] = circuit_breaker:<userAgentId>
  ARGV[1] = ttl_seconds (e.g., 3600)

  Returns: new state ("CLOSED", "OPEN", or "HALF_OPEN")
]]

local circuit_key = KEYS[1]
local ttl_seconds = ARGV[1]  -- Keep as string for Redis commands
local success_threshold = 3  -- Number of successes needed to transition HALF_OPEN → CLOSED

-- Get current state (default to CLOSED if not exists)
local current_state = redis.call('HGET', circuit_key, 'state')
if not current_state then
    -- Initialize circuit in CLOSED state
    redis.call('HMSET', circuit_key,
        'state', 'CLOSED',
        'consecutive_failures', '0',
        'consecutive_successes', '0',
        'failure_threshold', '3',
        'timeout_duration_seconds', '600'
    )
    redis.call('EXPIRE', circuit_key, ttl_seconds)
    current_state = 'CLOSED'
end

-- HALF_OPEN state: increment success counter and check threshold
if current_state == 'HALF_OPEN' then
    local successes = redis.call('HINCRBY', circuit_key, 'consecutive_successes', 1)

    -- Threshold reached: transition to CLOSED
    if successes >= success_threshold then
        redis.call('HMSET', circuit_key,
            'state', 'CLOSED',
            'consecutive_failures', '0',
            'consecutive_successes', '0'
        )
        -- Remove opened_at field (not needed in CLOSED state)
        redis.call('HDEL', circuit_key, 'opened_at')
        redis.call('EXPIRE', circuit_key, ttl_seconds)
        return 'CLOSED'
    else
        -- Still HALF_OPEN, just refresh TTL
        redis.call('EXPIRE', circuit_key, ttl_seconds)
        return 'HALF_OPEN'
    end

-- CLOSED state: reset failure counter (success resets consecutive failures)
elseif current_state == 'CLOSED' then
    redis.call('HSET', circuit_key, 'consecutive_failures', '0')
    redis.call('EXPIRE', circuit_key, ttl_seconds)
    return 'CLOSED'

-- OPEN state: ignore (circuit is still open, waiting for timeout)
else
    redis.call('EXPIRE', circuit_key, ttl_seconds)
    return 'OPEN'
end
