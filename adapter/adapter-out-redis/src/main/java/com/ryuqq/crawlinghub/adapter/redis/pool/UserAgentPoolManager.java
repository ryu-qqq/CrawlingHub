package com.ryuqq.crawlinghub.adapter.redis.pool;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * User-Agent Availability Pool Manager
 * Sorted Set 기반 LRU 선택 로직
 *
 * Score: last_used_timestamp
 * Members: user_agent_id
  *
 * @author crawlinghub
 */
@Service
public class UserAgentPoolManager {

    private static final String POOL_KEY = "user_agent:availability_pool";

    private final RedisTemplate<String, Object> redisTemplate;

    public UserAgentPoolManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * User-Agent를 Pool에 추가
      *
 * @author crawlinghub
 */
    public void addToPool(Long userAgentId) {
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(POOL_KEY, String.valueOf(userAgentId), score);
    }

    /**
     * 가장 오래 사용되지 않은 User-Agent 선택 (LRU)
     * ZPOPMIN 사용
      *
 * @author crawlinghub
 */
    public Long acquireLeastRecentlyUsed() {
        ZSetOperations.TypedTuple<Object> result =
            redisTemplate.opsForZSet().popMin(POOL_KEY);

        if (result == null || result.getValue() == null) {
            return null;
        }

        return Long.parseLong(result.getValue().toString());
    }

    /**
     * User-Agent를 Pool에 반환
     * 현재 timestamp로 score 갱신
      *
 * @author crawlinghub
 */
    public void returnToPool(Long userAgentId) {
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(POOL_KEY, String.valueOf(userAgentId), score);
    }

    /**
     * 특정 User-Agent를 Pool에서 제거
      *
 * @author crawlinghub
 */
    public void removeFromPool(Long userAgentId) {
        redisTemplate.opsForZSet().remove(POOL_KEY, String.valueOf(userAgentId));
    }

    /**
     * Pool 크기 조회
      *
 * @author crawlinghub
 */
    public Long getPoolSize() {
        return redisTemplate.opsForZSet().size(POOL_KEY);
    }

    /**
     * 가장 오래된 N개 User-Agent 조회 (제거하지 않음)
      *
 * @author crawlinghub
 */
    public Set<Object> peekLeastRecentlyUsed(int count) {
        return redisTemplate.opsForZSet().range(POOL_KEY, 0, count - 1);
    }

    /**
     * 특정 User-Agent의 마지막 사용 시각 조회
      *
 * @author crawlinghub
 */
    public Long getLastUsedTimestamp(Long userAgentId) {
        Double score = redisTemplate.opsForZSet().score(POOL_KEY, String.valueOf(userAgentId));
        return score != null ? score.longValue() : null;
    }

    /**
     * Pool 초기화
      *
 * @author crawlinghub
 */
    public void clearPool() {
        redisTemplate.delete(POOL_KEY);
    }

    /**
     * 모든 User-Agent ID 조회
      *
 * @author crawlinghub
 */
    public Set<Object> getAllUserAgentIds() {
        return redisTemplate.opsForZSet().range(POOL_KEY, 0, -1);
    }
}
