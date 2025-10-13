package com.ryuqq.crawlinghub.adapter.persistence.jpa.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Rate Limit Bucket JPA Repository
 *
 * @author crawlinghub
 */
public interface RateLimitBucketJpaRepository extends JpaRepository<RateLimitBucketEntity, Long> {

    /**
     * User-Agent ID로 조회
     */
    Optional<RateLimitBucketEntity> findByUserAgentId(Long userAgentId);

    /**
     * User-Agent ID 목록으로 조회
     */
    List<RateLimitBucketEntity> findAllByUserAgentIdIn(List<Long> userAgentIds);

    /**
     * 마지막 동기화 시각 이전의 Bucket 조회 (Stale Bucket)
     */
    @Query("SELECT b FROM RateLimitBucketEntity b " +
           "WHERE b.lastSyncedAt < :threshold")
    List<RateLimitBucketEntity> findStaleBuckets(@Param("threshold") LocalDateTime threshold);

    /**
     * 거부율이 높은 Bucket 조회 (모니터링용)
     */
    @Query("SELECT b FROM RateLimitBucketEntity b " +
           "WHERE b.totalRequests > :minRequests " +
           "AND (CAST(b.totalRejected AS double) / b.totalRequests) > :threshold")
    List<RateLimitBucketEntity> findHighRejectionRateBuckets(
        @Param("minRequests") long minRequests,
        @Param("threshold") double threshold
    );
}
