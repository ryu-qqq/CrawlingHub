package com.ryuqq.crawlinghub.application.product.port.out.query;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;

/**
 * 이미지 업로드 Outbox 조회 Port (Port Out - Query)
 *
 * <p>스케줄러 및 Facade에서 사용됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ImageOutboxQueryPort {

    /**
     * ID로 ImageOutbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return ImageOutbox (Optional)
     */
    Optional<CrawledProductImageOutbox> findById(Long outboxId);

    /**
     * Idempotency Key로 ImageOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return ImageOutbox (Optional)
     */
    Optional<CrawledProductImageOutbox> findByIdempotencyKey(String idempotencyKey);

    /**
     * CrawledProduct ID로 ImageOutbox 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return ImageOutbox 목록
     */
    List<CrawledProductImageOutbox> findByCrawledProductId(CrawledProductId crawledProductId);

    /**
     * 상태로 ImageOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return ImageOutbox 목록
     */
    List<CrawledProductImageOutbox> findByStatus(ProductOutboxStatus status, int limit);

    /**
     * PENDING 상태의 ImageOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 ImageOutbox 목록
     */
    List<CrawledProductImageOutbox> findPendingOutboxes(int limit);

    /**
     * FAILED 상태이고 재시도 가능한 ImageOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 ImageOutbox 목록
     */
    List<CrawledProductImageOutbox> findRetryableOutboxes(int maxRetryCount, int limit);

    /**
     * 원본 URL로 이미 존재하는지 확인
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return 존재하면 true
     */
    boolean existsByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl);
}
