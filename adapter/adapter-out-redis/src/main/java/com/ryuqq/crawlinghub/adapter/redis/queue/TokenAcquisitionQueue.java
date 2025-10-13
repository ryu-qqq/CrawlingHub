package com.ryuqq.crawlinghub.adapter.redis.queue;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token Acquisition Queue
 * List 기반 FIFO 큐
 *
 * 특징:
 * - LPUSH / RPOP 기반 FIFO 큐
 * - 타임아웃 초과 시 자동 제거 로직
  *
 * @author crawlinghub
 */
@Service
public class TokenAcquisitionQueue {

    private static final String QUEUE_KEY_PREFIX = "token_queue:";
    private static final String TIMEOUT_KEY_PREFIX = "token_queue_timeout:";
    private static final int DEFAULT_TIMEOUT_SECONDS = 60; // 1분

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenAcquisitionQueue(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 큐에 요청 추가
     *
     * @param userAgentId User-Agent ID
     * @param requestId 요청 ID
      *
 * @author crawlinghub
 */
    public void enqueue(Long userAgentId, String requestId) {
        String queueKey = QUEUE_KEY_PREFIX + userAgentId;
        String timeoutKey = TIMEOUT_KEY_PREFIX + requestId;

        // 큐에 요청 추가 (LPUSH)
        redisTemplate.opsForList().leftPush(queueKey, requestId);

        // 타임아웃 설정
        redisTemplate.opsForValue().set(timeoutKey, System.currentTimeMillis(),
                                       DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 큐에서 요청 제거 (FIFO)
     *
     * @param userAgentId User-Agent ID
     * @return 요청 ID (없으면 null)
      *
 * @author crawlinghub
 */
    public String dequeue(Long userAgentId) {
        String queueKey = QUEUE_KEY_PREFIX + userAgentId;
        Object requestId = redisTemplate.opsForList().rightPop(queueKey);

        if (requestId == null) {
            return null;
        }

        String requestIdStr = requestId.toString();

        // 타임아웃 체크
        if (isTimedOut(requestIdStr)) {
            // 타임아웃된 요청은 null 반환 (재귀적으로 다음 요청 확인)
            removeTimeoutKey(requestIdStr);
            return dequeue(userAgentId);
        }

        removeTimeoutKey(requestIdStr);
        return requestIdStr;
    }

    /**
     * 큐 크기 조회
      *
 * @author crawlinghub
 */
    public Long getQueueSize(Long userAgentId) {
        String queueKey = QUEUE_KEY_PREFIX + userAgentId;
        return redisTemplate.opsForList().size(queueKey);
    }

    /**
     * 큐 초기화
      *
 * @author crawlinghub
 */
    public void clearQueue(Long userAgentId) {
        String queueKey = QUEUE_KEY_PREFIX + userAgentId;
        redisTemplate.delete(queueKey);
    }

    /**
     * 타임아웃 체크
      *
 * @author crawlinghub
 */
    private boolean isTimedOut(String requestId) {
        String timeoutKey = TIMEOUT_KEY_PREFIX + requestId;
        return !Boolean.TRUE.equals(redisTemplate.hasKey(timeoutKey));
    }

    /**
     * 타임아웃 키 제거
      *
 * @author crawlinghub
 */
    private void removeTimeoutKey(String requestId) {
        String timeoutKey = TIMEOUT_KEY_PREFIX + requestId;
        redisTemplate.delete(timeoutKey);
    }

    /**
     * 대기 중인 모든 요청 ID 조회
      *
 * @author crawlinghub
 */
    public java.util.List<Object> getAllPendingRequests(Long userAgentId) {
        String queueKey = QUEUE_KEY_PREFIX + userAgentId;
        return redisTemplate.opsForList().range(queueKey, 0, -1);
    }
}
