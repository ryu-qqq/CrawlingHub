--[[
  Circuit Breaker - Allow Request (Atomic)

  Atomically checks if a request should be allowed based on circuit state.
  Prevents race conditions in HALF_OPEN state by using a test flag.

  KEYS[1] = circuit_breaker:<userAgentId>
  ARGV[1] = current_timestamp_millis
  ARGV[2] = timeout_duration_seconds

  Returns: "ALLOW" or "DENY"
]]

local circuit_key = KEYS[1]
local current_time = tonumber(ARGV[1])
local default_timeout = tonumber(ARGV[2])

-- Get current state (default to CLOSED if not exists)
local current_state = redis.call('HGET', circuit_key, 'state')
if not current_state then
    -- Circuit doesn't exist, initialize as CLOSED
    redis.call('HMSET', circuit_key,
        'state', 'CLOSED',
        'consecutive_failures', '0',
        'consecutive_successes', '0',
        'failure_threshold', '3',
        'timeout_duration_seconds', tostring(default_timeout)
    )
    redis.call('EXPIRE', circuit_key, 3600)  -- 1 hour TTL
    return 'ALLOW'
end

-- CLOSED state: always allow
if current_state == 'CLOSED' then
    return 'ALLOW'

-- OPEN state: check timeout and potentially transition to HALF_OPEN
elseif current_state == 'OPEN' then
    local opened_at = redis.call('HGET', circuit_key, 'opened_at')
    if not opened_at then
        return 'DENY'  -- No opened_at timestamp, deny by default
    end

    local timeout_seconds = tonumber(redis.call('HGET', circuit_key, 'timeout_duration_seconds') or default_timeout)
    local elapsed_seconds = (current_time - tonumber(opened_at)) / 1000

    if elapsed_seconds >= timeout_seconds then
        -- Timeout elapsed: transition to HALF_OPEN and allow test request
        redis.call('HMSET', circuit_key,
            'state', 'HALF_OPEN',
            'consecutive_failures', '0',
            'consecutive_successes', '0',
            'test_request_active', '1'  -- Flag to prevent concurrent test requests
        )
        redis.call('EXPIRE', circuit_key, 3600)
        return 'ALLOW'
    else
        -- Timeout not elapsed: deny
        return 'DENY'
    end

-- HALF_OPEN state: allow only one test request atomically
else  -- current_state == 'HALF_OPEN'
    local test_request_active = redis.call('HGET', circuit_key, 'test_request_active')

    if test_request_active == '1' then
        -- Test request already in progress, deny
        return 'DENY'
    else
        -- No test request active, allow and set flag
        redis.call('HSET', circuit_key, 'test_request_active', '1')
        redis.call('EXPIRE', circuit_key, 3600)
        return 'ALLOW'
    end
end
