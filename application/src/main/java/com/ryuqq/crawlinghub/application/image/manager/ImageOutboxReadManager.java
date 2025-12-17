package com.ryuqq.crawlinghub.application.image.manager;

import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ImageOutbox 조회 관리자
 *
 * <p>ImageOutboxQueryPort를 래핑하여 조회 로직을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ImageOutboxReadManager {

    private final ImageOutboxQueryPort imageOutboxQueryPort;

    public ImageOutboxReadManager(ImageOutboxQueryPort imageOutboxQueryPort) {
        this.imageOutboxQueryPort = imageOutboxQueryPort;
    }

    /**
     * ID로 ImageOutbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return ImageOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductImageOutbox> findById(Long outboxId) {
        return imageOutboxQueryPort.findById(outboxId);
    }

    /**
     * Idempotency Key로 ImageOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return ImageOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductImageOutbox> findByIdempotencyKey(String idempotencyKey) {
        return imageOutboxQueryPort.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * CrawledProduct ID로 ImageOutbox 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return ImageOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImageOutbox> findByCrawledProductId(
            CrawledProductId crawledProductId) {
        return imageOutboxQueryPort.findByCrawledProductId(crawledProductId);
    }

    /**
     * 상태로 ImageOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return ImageOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImageOutbox> findByStatus(ProductOutboxStatus status, int limit) {
        return imageOutboxQueryPort.findByStatus(status, limit);
    }

    /**
     * PENDING 상태의 ImageOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 ImageOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImageOutbox> findPendingOutboxes(int limit) {
        return imageOutboxQueryPort.findPendingOutboxes(limit);
    }

    /**
     * 재시도 가능한 ImageOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 ImageOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImageOutbox> findRetryableOutboxes(int maxRetryCount, int limit) {
        return imageOutboxQueryPort.findRetryableOutboxes(maxRetryCount, limit);
    }

    /**
     * 새로운 이미지 URL 목록 필터링 (N+1 해결용 배치 쿼리 사용)
     *
     * <p>주어진 URL 목록에서 이미 Outbox에 존재하는 URL을 제외하고 새로운 URL만 반환합니다.
     *
     * @param crawledProductId CrawledProduct ID
     * @param imageUrls 확인할 이미지 URL 목록
     * @return 새로운 이미지 URL 목록 (기존에 없는 URL만)
     */
    @Transactional(readOnly = true)
    public List<String> filterNewImageUrls(
            CrawledProductId crawledProductId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }

        List<String> existingUrls =
                imageOutboxQueryPort.findExistingOriginalUrls(crawledProductId, imageUrls);
        Set<String> existingSet = new HashSet<>(existingUrls);

        return imageUrls.stream().filter(url -> !existingSet.contains(url)).toList();
    }

    /**
     * CrawledProduct ID와 원본 URL로 ImageOutbox 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return ImageOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductImageOutbox> findByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl) {
        return imageOutboxQueryPort.findByCrawledProductIdAndOriginalUrl(
                crawledProductId, originalUrl);
    }

    /**
     * CrawledProduct ID와 원본 URL 목록으로 ImageOutbox 배치 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrls 원본 URL 목록
     * @return ImageOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImageOutbox> findByCrawledProductIdAndOriginalUrls(
            CrawledProductId crawledProductId, List<String> originalUrls) {
        return imageOutboxQueryPort.findByCrawledProductIdAndOriginalUrls(
                crawledProductId, originalUrls);
    }
}
