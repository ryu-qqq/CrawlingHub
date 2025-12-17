package com.ryuqq.crawlinghub.application.image.manager;

import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductImageQueryPort;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ImageOutbox 및 CrawledProductImage 조회 관리자
 *
 * <p>Outbox 패턴 및 이미지 조회 로직을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ImageOutboxReadManager {

    private final ImageOutboxQueryPort outboxQueryPort;
    private final CrawledProductImageQueryPort imageQueryPort;

    public ImageOutboxReadManager(
            ImageOutboxQueryPort outboxQueryPort, CrawledProductImageQueryPort imageQueryPort) {
        this.outboxQueryPort = outboxQueryPort;
        this.imageQueryPort = imageQueryPort;
    }

    // === Outbox 조회 ===

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

    // === 이미지 조회 ===

    /**
     * 이미지 ID로 이미지 조회
     *
     * @param imageId 이미지 ID
     * @return CrawledProductImage (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductImage> findImageById(Long imageId) {
        return imageQueryPort.findById(imageId);
    }

    /**
     * CrawledProduct ID로 이미지 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImage> findImagesByCrawledProductId(
            CrawledProductId crawledProductId) {
        return imageQueryPort.findByCrawledProductId(crawledProductId);
    }

    /**
     * CrawledProduct ID와 원본 URL로 이미지 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return CrawledProductImage (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductImage> findImageByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl) {
        return imageQueryPort.findByCrawledProductIdAndOriginalUrl(crawledProductId, originalUrl);
    }

    /**
     * 새로운 이미지 URL 목록 필터링 (중복 체크용 배치 쿼리 사용)
     *
     * <p>주어진 URL 목록에서 이미 저장된 URL을 제외하고 새로운 URL만 반환합니다.
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
                imageQueryPort.findExistingOriginalUrls(crawledProductId, imageUrls);
        Set<String> existingSet = new HashSet<>(existingUrls);

        return imageUrls.stream().filter(url -> !existingSet.contains(url)).toList();
    }
}
