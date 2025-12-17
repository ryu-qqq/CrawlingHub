package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ImageOutboxJpaEntity.OutboxStatus;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.ImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.ImageOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * ImageOutboxQueryAdapter - ImageOutbox Query Adapter
 *
 * <p>CQRS의 Query(조회) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>QueryDslRepository 호출
 *   <li>JPA Entity → Domain 변환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>비즈니스 로직 (Domain에서 처리)
 *   <li>쓰기 로직 (CommandAdapter로 분리)
 *   <li>@Transactional 어노테이션 (Application Layer에서 관리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ImageOutboxQueryAdapter implements ImageOutboxQueryPort {

    private final ImageOutboxQueryDslRepository queryDslRepository;
    private final ImageOutboxJpaEntityMapper mapper;

    public ImageOutboxQueryAdapter(
            ImageOutboxQueryDslRepository queryDslRepository, ImageOutboxJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CrawledProductImageOutbox> findById(Long outboxId) {
        return queryDslRepository.findById(outboxId).map(mapper::toDomain);
    }

    @Override
    public Optional<CrawledProductImageOutbox> findByIdempotencyKey(String idempotencyKey) {
        return queryDslRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public List<CrawledProductImageOutbox> findByCrawledProductId(
            CrawledProductId crawledProductId) {
        List<ImageOutboxJpaEntity> entities =
                queryDslRepository.findByCrawledProductId(crawledProductId.value());
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CrawledProductImageOutbox> findByStatus(ProductOutboxStatus status, int limit) {
        OutboxStatus entityStatus = toEntityStatus(status);
        List<ImageOutboxJpaEntity> entities = queryDslRepository.findByStatus(entityStatus, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CrawledProductImageOutbox> findPendingOutboxes(int limit) {
        List<ImageOutboxJpaEntity> entities = queryDslRepository.findPendingOutboxes(limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CrawledProductImageOutbox> findRetryableOutboxes(int maxRetryCount, int limit) {
        List<ImageOutboxJpaEntity> entities =
                queryDslRepository.findRetryableOutboxes(maxRetryCount, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl) {
        return queryDslRepository.existsByCrawledProductIdAndOriginalUrl(
                crawledProductId.value(), originalUrl);
    }

    @Override
    public List<String> findExistingOriginalUrls(
            CrawledProductId crawledProductId, List<String> originalUrls) {
        return queryDslRepository.findExistingOriginalUrls(crawledProductId.value(), originalUrls);
    }

    @Override
    public Optional<CrawledProductImageOutbox> findByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl) {
        return queryDslRepository
                .findByCrawledProductIdAndOriginalUrl(crawledProductId.value(), originalUrl)
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawledProductImageOutbox> findByCrawledProductIdAndOriginalUrls(
            CrawledProductId crawledProductId, List<String> originalUrls) {
        List<ImageOutboxJpaEntity> entities =
                queryDslRepository.findByCrawledProductIdAndOriginalUrls(
                        crawledProductId.value(), originalUrls);
        return entities.stream().map(mapper::toDomain).toList();
    }

    private OutboxStatus toEntityStatus(ProductOutboxStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> OutboxStatus.PENDING;
            case PROCESSING -> OutboxStatus.PROCESSING;
            case COMPLETED -> OutboxStatus.COMPLETED;
            case FAILED -> OutboxStatus.FAILED;
        };
    }
}
