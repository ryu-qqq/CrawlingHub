package com.ryuqq.crawlinghub.application.common.port.out;

import java.time.Duration;
import java.util.Optional;

/**
 * 캐시 작업을 위한 아웃바운드 포트 인터페이스
 *
 * @param <T> 캐시 값 타입
 * @author development-team
 * @since 1.0.0
 */
public interface CachePort<T> {

    void set(String key, T value);

    void set(String key, T value, Duration ttl);

    Optional<T> get(String key);

    Optional<T> get(String key, Class<T> clazz);

    void evict(String key);

    void evictByPattern(String pattern);

    boolean exists(String key);

    Duration getTtl(String key);
}
