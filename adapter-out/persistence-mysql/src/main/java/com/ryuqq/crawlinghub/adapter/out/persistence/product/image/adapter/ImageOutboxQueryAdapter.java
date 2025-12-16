package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.mapper.ProductImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.repository.ProductImageOutboxQueryDslRepository;
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
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>ID/Key로 단건 조회
 *   <li>상태별 목록 조회
 *   <li>재시도 가능한 Outbox 조회 (스케줄러용)
 *   <li>중복 체크 (existsByCrawledProductIdAndOriginalUrl)
 *   <li>QueryDslRepository 호출
 *   <li>Mapper를 통한 Entity -> Domain 변환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>비즈니스 로직
 *   <li>저장/수정/삭제 (CommandAdapter로 분리)
 *   <li>JPAQueryFactory 직접 사용 (QueryDslRepository에서 처리)
 * </ul>
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

    /**
     * ID로 ImageOutbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return ImageOutbox (Optional)
     */
    @Override
    public Optional<CrawledProductImageOutbox> findById(Long outboxId) {
        return queryDslRepository.findById(outboxId).map(mapper::toDomain);
    }

    /**
     * Idempotency Key로 ImageOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return ImageOutbox (Optional)
     */
    @Override
    public Optional<CrawledProductImageOutbox> findByIdempotencyKey(String idempotencyKey) {
        return queryDslRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    /**
     * CrawledProduct ID로 ImageOutbox 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return ImageOutbox 목록
     */
    @Override
    public List<CrawledProductImageOutbox> findByCrawledProductId(
            CrawledProductId crawledProductId) {
        List<ProductImageOutboxJpaEntity> entities =
                queryDslRepository.findByCrawledProductId(crawledProductId.value());
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 상태로 ImageOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return ImageOutbox 목록
     */
    @Override
    public List<CrawledProductImageOutbox> findByStatus(ProductOutboxStatus status, int limit) {
        List<ProductImageOutboxJpaEntity> entities = queryDslRepository.findByStatus(status, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * PENDING 상태의 ImageOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 ImageOutbox 목록
     */
    @Override
    public List<CrawledProductImageOutbox> findPendingOutboxes(int limit) {
        List<ProductImageOutboxJpaEntity> entities = queryDslRepository.findPendingOutboxes(limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * FAILED 상태이고 재시도 가능한 ImageOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 ImageOutbox 목록
     */
    @Override
    public List<CrawledProductImageOutbox> findRetryableOutboxes(int maxRetryCount, int limit) {
        List<ProductImageOutboxJpaEntity> entities =
                queryDslRepository.findRetryableOutboxes(maxRetryCount, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 원본 URL로 이미 존재하는지 확인
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return 존재하면 true
     */
    @Override
    public boolean existsByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl) {
        return queryDslRepository.existsByCrawledProductIdAndOriginalUrl(
                crawledProductId.value(), originalUrl);
    }

    /**
     * 이미 존재하는 원본 URL 목록 조회 (IN 절 배치 쿼리)
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrls 확인할 원본 URL 목록
     * @return 이미 존재하는 원본 URL 목록
     */
    @Override
    public List<String> findExistingOriginalUrls(
            CrawledProductId crawledProductId, List<String> originalUrls) {
        return queryDslRepository.findExistingOriginalUrls(crawledProductId.value(), originalUrls);
    }

    /**
     * CrawledProduct ID와 원본 URL로 ImageOutbox 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return ImageOutbox (Optional)
     */
    @Override
    public Optional<CrawledProductImageOutbox> findByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl) {
        return queryDslRepository
                .findByCrawledProductIdAndOriginalUrl(crawledProductId.value(), originalUrl)
                .map(mapper::toDomain);
    }

    /**
     * CrawledProduct ID와 원본 URL 목록으로 ImageOutbox 배치 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrls 원본 URL 목록
     * @return ImageOutbox 목록
     */
    @Override
    public List<CrawledProductImageOutbox> findByCrawledProductIdAndOriginalUrls(
            CrawledProductId crawledProductId, List<String> originalUrls) {
        List<ProductImageOutboxJpaEntity> entities =
                queryDslRepository.findByCrawledProductIdAndOriginalUrls(
                        crawledProductId.value(), originalUrls);
        return entities.stream().map(mapper::toDomain).toList();
    }
}
