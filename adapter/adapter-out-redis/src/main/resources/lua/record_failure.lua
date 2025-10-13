--[[
  Circuit Breaker - Record Failure (Atomic)

  Atomically records a failure and transitions circuit state if threshold is reached.
  Prevents lost updates in concurrent environments.

  KEYS[1] = circuit_breaker:<userAgentId>
  ARGV[1] = failure_threshold (e.g., 3)
  ARGV[2] = current_timestamp (milliseconds)
  ARGV[3] = ttl_seconds (e.g., 3600)

  Returns: new state ("CLOSED", "OPEN", or "HALF_OPEN")
]]

local circuit_key = KEYS[1]
local failure_threshold = tonumber(ARGV[1])  -- For comparison only
local current_time = ARGV[2]
local ttl_seconds = ARGV[3]  -- Keep as string for Redis commands

-- Get current state (default to CLOSED if not exists)
local current_state = redis.call('HGET', circuit_key, 'state')
if not current_state then
    -- Initialize circuit in CLOSED state
    redis.call('HMSET', circuit_key,
        'state', 'CLOSED',
        'consecutive_failures', '0',
        'consecutive_successes', '0',
        'failure_threshold', ARGV[1],
        'timeout_duration_seconds', '600'
    )
    redis.call('EXPIRE', circuit_key, ttl_seconds)
    current_state = 'CLOSED'
end

-- CLOSED state: increment failure counter and check threshold
if current_state == 'CLOSED' then
    local failures = redis.call('HINCRBY', circuit_key, 'consecutive_failures', 1)

    -- Threshold reached: transition to OPEN
    if failures >= failure_threshold then
        redis.call('HMSET', circuit_key,
            'state', 'OPEN',
            'consecutive_failures', '0',
            'consecutive_successes', '0',
            'opened_at', current_time
        )
        redis.call('EXPIRE', circuit_key, ttl_seconds)
        return 'OPEN'
    else
        -- Still CLOSED, just refresh TTL
        redis.call('EXPIRE', circuit_key, ttl_seconds)
        return 'CLOSED'
    end

-- HALF_OPEN state: immediately transition back to OPEN on failure
elseif current_state == 'HALF_OPEN' then
    redis.call('HMSET', circuit_key,
        'state', 'OPEN',
        'consecutive_failures', '0',
        'consecutive_successes', '0',
        'opened_at', current_time
    )
    -- Remove test_request_active flag (no longer in HALF_OPEN)
    redis.call('HDEL', circuit_key, 'test_request_active')
    redis.call('EXPIRE', circuit_key, ttl_seconds)
    return 'OPEN'

-- OPEN state: ignore (already open)
else
    redis.call('EXPIRE', circuit_key, ttl_seconds)
    return 'OPEN'
end
