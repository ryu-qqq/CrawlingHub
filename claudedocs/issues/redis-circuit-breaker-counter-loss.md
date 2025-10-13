# Issue: Lost failure/success counters in Redis circuit breaker under concurrent updates

## Summary
Redis-based `CircuitBreakerManager` updates the `consecutive_failures` and `consecutive_successes` hash fields by reading the current value in Java and then overwriting it with `HSET`. When multiple instances record failures or successes at the same time, only the last write survives, so the counters never reach the configured thresholds and the circuit does not open or close as expected.

## Impact
* **Severity:** High – the circuit breaker can remain CLOSED despite repeated downstream failures, so throttling/blacklisting never activates. In HALF_OPEN, successful probes can also be dropped, causing the circuit to flap between OPEN and HALF_OPEN.
* **Affected component:** `adapter/adapter-out-redis/src/main/java/com/ryuqq/crawlinghub/adapter/redis/circuit/CircuitBreakerManager.java`
* **Risk:** Production traffic may continue hammering an unhealthy upstream because failure counts reset every time concurrent updates collide.

## Technical Details
* `recordFailure` calls `getState`, increments `consecutiveFailures` in memory, then calls `incrementFailureCount`, which executes `HSET` with the computed value. Concurrent calls to `recordFailure` race, and the largest write wins while other increments are dropped.【F:adapter/adapter-out-redis/src/main/java/com/ryuqq/crawlinghub/adapter/redis/circuit/CircuitBreakerManager.java†L57-L101】【F:adapter/adapter-out-redis/src/main/java/com/ryuqq/crawlinghub/adapter/redis/circuit/CircuitBreakerManager.java†L144-L152】
* `recordSuccess` follows the same pattern via `incrementSuccessCount`, leading to lost updates during HALF_OPEN recovery.【F:adapter/adapter-out-redis/src/main/java/com/ryuqq/crawlinghub/adapter/redis/circuit/CircuitBreakerManager.java†L45-L76】【F:adapter/adapter-out-redis/src/main/java/com/ryuqq/crawlinghub/adapter/redis/circuit/CircuitBreakerManager.java†L154-L162】

## Steps to Reproduce
1. Deploy two application instances sharing the same Redis cluster.
2. Trigger simultaneous `recordFailure` calls for the same `userAgentId` from both instances (e.g., by making two threads send requests that fail with 429).
3. Inspect the Redis hash `HGETALL circuit_breaker:<userAgentId>`.

**Expected:** The `consecutive_failures` field increases by 2, eventually exceeding the failure threshold and opening the circuit.

**Actual:** The field increases by only 1 (the last write wins), so the threshold is never met.

## Suggested Fix
Use Redis atomic operations instead of read-modify-write on the application side. Options include:
* Replace `HSET` increments with `HINCRBY` / `HINCRBYFLOAT` to perform atomic counters.
* Or, encapsulate the entire state transition in a Redis Lua script to guarantee atomicity (increment + threshold check + state change).

Once fixed, add concurrent integration tests (e.g., using multiple threads against Testcontainers Redis) to ensure counters monotonically increase under concurrency.
