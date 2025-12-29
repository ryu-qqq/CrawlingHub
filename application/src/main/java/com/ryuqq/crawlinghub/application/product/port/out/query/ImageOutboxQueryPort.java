package com.ryuqq.crawlinghub.application.product.port.out.query;

import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxWithImageResponse;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
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

    /**
     * 조건으로 ImageOutbox 목록 검색 (페이징)
     *
     * @param crawledProductImageId CrawledProductImage ID (nullable)
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @param offset 오프셋
     * @param size 페이지 크기
     * @return ImageOutbox 목록
     */
    List<ProductImageOutbox> search(
            Long crawledProductImageId,
            Long crawledProductId,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            long offset,
            int size);

    /**
     * 조건으로 ImageOutbox 개수 조회
     *
     * @param crawledProductImageId CrawledProductImage ID (nullable)
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @return 총 개수
     */
    long count(
            Long crawledProductImageId,
            Long crawledProductId,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo);

    /**
     * PROCESSING 상태이고 타임아웃된 ImageOutbox 조회
     *
     * <p>processedAt 기준으로 지정된 시간(초)이 지난 PROCESSING 상태의 Outbox를 조회합니다.
     *
     * @param timeoutSeconds 타임아웃 기준 시간(초)
     * @param limit 조회 개수 제한
     * @return 타임아웃된 ImageOutbox 목록
     */
    List<ProductImageOutbox> findTimedOutProcessingOutboxes(int timeoutSeconds, int limit);

    /**
     * 조건으로 ImageOutbox 목록 검색 (이미지 정보 포함, 페이징)
     *
     * <p>CrawledProductImage와 JOIN하여 이미지 정보를 함께 반환합니다.
     *
     * @param crawledProductImageId CrawledProductImage ID (nullable)
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @param offset 오프셋
     * @param size 페이지 크기
     * @return Outbox + 이미지 정보 응답 목록
     */
    List<ProductImageOutboxWithImageResponse> searchWithImageInfo(
            Long crawledProductImageId,
            Long crawledProductId,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            long offset,
            int size);
}
