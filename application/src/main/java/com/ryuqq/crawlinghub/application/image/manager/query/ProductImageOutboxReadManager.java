package com.ryuqq.crawlinghub.application.image.manager.query;

import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ImageOutbox 조회 관리자
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>ProductImageOutbox 조회 전용
 *   <li>Outbox 상태별 조회
 *   <li>스케줄러용 PENDING/재시도 대상 조회
 * </ul>
 *
 * <p><strong>SRP</strong>: Outbox 조회만 담당 (이미지 조회는 CrawledProductImageReadManager)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductImageOutboxReadManager {

    private final ImageOutboxQueryPort outboxQueryPort;

    public ProductImageOutboxReadManager(ImageOutboxQueryPort outboxQueryPort) {
        this.outboxQueryPort = outboxQueryPort;
    }

    /**
     * ID로 ImageOutbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return ImageOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<ProductImageOutbox> findById(Long outboxId) {
        return outboxQueryPort.findById(outboxId);
    }

    /**
     * Idempotency Key로 ImageOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return ImageOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<ProductImageOutbox> findByIdempotencyKey(String idempotencyKey) {
        return outboxQueryPort.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * CrawledProductImage ID로 Outbox 조회
     *
     * @param crawledProductImageId 이미지 ID
     * @return ImageOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<ProductImageOutbox> findByCrawledProductImageId(Long crawledProductImageId) {
        return outboxQueryPort.findByCrawledProductImageId(crawledProductImageId);
    }

    /**
     * 상태로 ImageOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return ImageOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<ProductImageOutbox> findByStatus(ProductOutboxStatus status, int limit) {
        return outboxQueryPort.findByStatus(status, limit);
    }

    /**
     * PENDING 상태의 ImageOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 ImageOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<ProductImageOutbox> findPendingOutboxes(int limit) {
        return outboxQueryPort.findPendingOutboxes(limit);
    }

    /**
     * 재시도 가능한 ImageOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 ImageOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<ProductImageOutbox> findRetryableOutboxes(int maxRetryCount, int limit) {
        return outboxQueryPort.findRetryableOutboxes(maxRetryCount, limit);
    }
}
