package com.ryuqq.crawlinghub.adapter.out.persistence.image.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper.ProductImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.repository.ProductImageOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
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
     * 조건으로 ImageOutbox 목록 검색 (페이징)
     *
     * @param crawledProductImageId CrawledProductImage ID (nullable)
     * @param status 상태 (nullable)
     * @param offset 오프셋
     * @param size 페이지 크기
     * @return ImageOutbox 목록
     */
    @Override
    public List<ProductImageOutbox> search(
            Long crawledProductImageId, ProductOutboxStatus status, long offset, int size) {
        List<ProductImageOutboxJpaEntity> entities =
                queryDslRepository.search(crawledProductImageId, status, offset, size);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 조건으로 ImageOutbox 개수 조회
     *
     * @param crawledProductImageId CrawledProductImage ID (nullable)
     * @param status 상태 (nullable)
     * @return 총 개수
     */
    @Override
    public long count(Long crawledProductImageId, ProductOutboxStatus status) {
        return queryDslRepository.count(crawledProductImageId, status);
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
}
