package com.ryuqq.crawlinghub.application.product.port.out.query;

import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;

/**
 * 이미지 업로드 Outbox 조회 Port (Port Out - Query)
 *
 * <p>스케줄러 및 Facade에서 사용됩니다.
 *
 * <p>Outbox 패턴에 필요한 조회 기능만 제공합니다. 이미지 데이터 조회는 CrawledProductImageQueryPort를 사용하세요.
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
    Optional<ProductImageOutbox> findById(Long outboxId);

    /**
     * Idempotency Key로 ImageOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return ImageOutbox (Optional)
     */
    Optional<ProductImageOutbox> findByIdempotencyKey(String idempotencyKey);

    /**
     * CrawledProductImage ID로 ImageOutbox 조회
     *
     * @param crawledProductImageId CrawledProductImage ID
     * @return ImageOutbox (Optional)
     */
    Optional<ProductImageOutbox> findByCrawledProductImageId(Long crawledProductImageId);

    /**
     * 상태로 ImageOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return ImageOutbox 목록
     */
    List<ProductImageOutbox> findByStatus(ProductOutboxStatus status, int limit);

    /**
     * PENDING 상태의 ImageOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 ImageOutbox 목록
     */
    List<ProductImageOutbox> findPendingOutboxes(int limit);

    /**
     * FAILED 상태이고 재시도 가능한 ImageOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 ImageOutbox 목록
     */
    List<ProductImageOutbox> findRetryableOutboxes(int maxRetryCount, int limit);
}
