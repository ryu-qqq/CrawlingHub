package com.ryuqq.crawlinghub.adapter.out.persistence.sync.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.sync.entity.ProductSyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.mapper.ProductSyncOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.repository.ProductSyncOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.application.product.port.out.query.SyncOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * SyncOutboxQueryAdapter - SyncOutbox Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>ID/Key로 단건 조회
 *   <li>상태별 목록 조회
 *   <li>재시도 가능한 Outbox 조회 (스케줄러용)
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
public class SyncOutboxQueryAdapter implements SyncOutboxQueryPort {

    private final ProductSyncOutboxQueryDslRepository queryDslRepository;
    private final ProductSyncOutboxJpaEntityMapper mapper;

    public SyncOutboxQueryAdapter(
            ProductSyncOutboxQueryDslRepository queryDslRepository,
            ProductSyncOutboxJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    /**
     * ID로 SyncOutbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return SyncOutbox (Optional)
     */
    @Override
    public Optional<CrawledProductSyncOutbox> findById(Long outboxId) {
        return queryDslRepository.findById(outboxId).map(mapper::toDomain);
    }

    /**
     * Idempotency Key로 SyncOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return SyncOutbox (Optional)
     */
    @Override
    public Optional<CrawledProductSyncOutbox> findByIdempotencyKey(String idempotencyKey) {
        return queryDslRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    /**
     * CrawledProduct ID로 SyncOutbox 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return SyncOutbox 목록
     */
    @Override
    public List<CrawledProductSyncOutbox> findByCrawledProductId(
            CrawledProductId crawledProductId) {
        List<ProductSyncOutboxJpaEntity> entities =
                queryDslRepository.findByCrawledProductId(crawledProductId.value());
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 상태로 SyncOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return SyncOutbox 목록
     */
    @Override
    public List<CrawledProductSyncOutbox> findByStatus(ProductOutboxStatus status, int limit) {
        List<ProductSyncOutboxJpaEntity> entities = queryDslRepository.findByStatus(status, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * PENDING 상태의 SyncOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 SyncOutbox 목록
     */
    @Override
    public List<CrawledProductSyncOutbox> findPendingOutboxes(int limit) {
        List<ProductSyncOutboxJpaEntity> entities = queryDslRepository.findPendingOutboxes(limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * FAILED 상태이고 재시도 가능한 SyncOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 SyncOutbox 목록
     */
    @Override
    public List<CrawledProductSyncOutbox> findRetryableOutboxes(int maxRetryCount, int limit) {
        List<ProductSyncOutboxJpaEntity> entities =
                queryDslRepository.findRetryableOutboxes(maxRetryCount, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 조건으로 SyncOutbox 목록 검색 (페이징)
     *
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param sellerId 셀러 ID (nullable)
     * @param itemNos 외부 상품번호 목록 (IN 조건, nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @param offset 오프셋
     * @param size 페이지 크기
     * @return SyncOutbox 목록
     */
    @Override
    public List<CrawledProductSyncOutbox> search(
            Long crawledProductId,
            Long sellerId,
            List<Long> itemNos,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            long offset,
            int size) {
        List<ProductSyncOutboxJpaEntity> entities =
                queryDslRepository.search(
                        crawledProductId,
                        sellerId,
                        itemNos,
                        statuses,
                        createdFrom,
                        createdTo,
                        offset,
                        size);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 조건으로 SyncOutbox 개수 조회
     *
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param sellerId 셀러 ID (nullable)
     * @param itemNos 외부 상품번호 목록 (IN 조건, nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @return 총 개수
     */
    @Override
    public long count(
            Long crawledProductId,
            Long sellerId,
            List<Long> itemNos,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo) {
        return queryDslRepository.count(
                crawledProductId, sellerId, itemNos, statuses, createdFrom, createdTo);
    }
}
