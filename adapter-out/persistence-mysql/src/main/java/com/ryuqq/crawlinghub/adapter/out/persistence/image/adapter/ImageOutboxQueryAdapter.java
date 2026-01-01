package com.ryuqq.crawlinghub.adapter.out.persistence.image.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper.ProductImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.repository.ProductImageOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxWithImageResponse;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImageOutboxCriteria;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * ImageOutboxQueryAdapter - Outbox Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p>Outbox 패턴에 필요한 조회 기능만 제공합니다. 이미지 데이터 조회는 CrawledProductImageQueryAdapter를 사용하세요.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ImageOutboxQueryAdapter implements ImageOutboxQueryPort {

    private final ProductImageOutboxQueryDslRepository queryDslRepository;
    private final ProductImageOutboxJpaEntityMapper mapper;

    public ImageOutboxQueryAdapter(
            ProductImageOutboxQueryDslRepository queryDslRepository,
            ProductImageOutboxJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ProductImageOutbox> findById(Long outboxId) {
        return queryDslRepository.findById(outboxId).map(mapper::toDomain);
    }

    @Override
    public Optional<ProductImageOutbox> findByIdempotencyKey(String idempotencyKey) {
        return queryDslRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public Optional<ProductImageOutbox> findByCrawledProductImageId(Long crawledProductImageId) {
        return queryDslRepository
                .findByCrawledProductImageId(crawledProductImageId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ProductImageOutbox> findByStatus(ProductOutboxStatus status, int limit) {
        List<ProductImageOutboxJpaEntity> entities = queryDslRepository.findByStatus(status, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ProductImageOutbox> findPendingOutboxes(int limit) {
        List<ProductImageOutboxJpaEntity> entities = queryDslRepository.findPendingOutboxes(limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ProductImageOutbox> findRetryableOutboxes(int maxRetryCount, int limit) {
        List<ProductImageOutboxJpaEntity> entities =
                queryDslRepository.findRetryableOutboxes(maxRetryCount, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * Criteria 기반 ImageOutbox 조회 (SQS 스케줄러용)
     *
     * <p>ProductImageOutboxCriteria VO를 사용하여 유연한 조건 조회를 지원합니다.
     *
     * @param criteria 조회 조건 VO
     * @return ImageOutbox 목록
     */
    @Override
    public List<ProductImageOutbox> findByCriteria(ProductImageOutboxCriteria criteria) {
        List<ProductImageOutboxJpaEntity> entities = queryDslRepository.findByCriteria(criteria);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * Criteria 기반 ImageOutbox 개수 조회
     *
     * @param criteria 조회 조건 VO
     * @return 총 개수
     */
    @Override
    public long countByCriteria(ProductImageOutboxCriteria criteria) {
        return queryDslRepository.countByCriteria(criteria);
    }

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
    @Override
    public List<ProductImageOutbox> search(
            Long crawledProductImageId,
            Long crawledProductId,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            long offset,
            int size) {
        List<ProductImageOutboxJpaEntity> entities =
                queryDslRepository.search(
                        crawledProductImageId,
                        crawledProductId,
                        statuses,
                        createdFrom,
                        createdTo,
                        offset,
                        size);
        return entities.stream().map(mapper::toDomain).toList();
    }

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
    @Override
    public long count(
            Long crawledProductImageId,
            Long crawledProductId,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo) {
        return queryDslRepository.count(
                crawledProductImageId, crawledProductId, statuses, createdFrom, createdTo);
    }

    /**
     * PROCESSING 상태이고 타임아웃된 ImageOutbox 조회
     *
     * <p>processedAt 기준으로 지정된 시간(초)이 지난 PROCESSING 상태의 Outbox를 조회합니다.
     *
     * @param timeoutSeconds 타임아웃 기준 시간(초)
     * @param limit 조회 개수 제한
     * @return 타임아웃된 ImageOutbox 목록
     */
    @Override
    public List<ProductImageOutbox> findTimedOutProcessingOutboxes(int timeoutSeconds, int limit) {
        List<ProductImageOutboxJpaEntity> entities =
                queryDslRepository.findTimedOutProcessingOutboxes(timeoutSeconds, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 조건으로 ImageOutbox 목록 검색 (이미지 정보 포함, 페이징)
     *
     * <p>CrawledProductImage와 LEFT JOIN하여 이미지 정보를 함께 반환합니다.
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
    @Override
    public List<ProductImageOutboxWithImageResponse> searchWithImageInfo(
            Long crawledProductImageId,
            Long crawledProductId,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            long offset,
            int size) {
        return queryDslRepository
                .searchWithImageInfo(
                        crawledProductImageId,
                        crawledProductId,
                        statuses,
                        createdFrom,
                        createdTo,
                        offset,
                        size)
                .stream()
                .map(
                        dto ->
                                ProductImageOutboxWithImageResponse.of(
                                        dto.id(),
                                        dto.crawledProductImageId(),
                                        dto.idempotencyKey(),
                                        dto.status(),
                                        dto.retryCount(),
                                        dto.errorMessage(),
                                        toInstant(dto.createdAt()),
                                        null, // updatedAt: entity에 미존재, 향후 추가 예정
                                        toInstant(dto.processedAt()),
                                        dto.crawledProductId(),
                                        dto.originalUrl(),
                                        dto.s3Url(),
                                        dto.imageType()))
                .toList();
    }

    /**
     * LocalDateTime을 Instant로 변환
     *
     * <p>null 안전 변환 (Asia/Seoul 타임존 기준)
     *
     * @param localDateTime LocalDateTime
     * @return Instant (null인 경우 null 반환)
     */
    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }
}
