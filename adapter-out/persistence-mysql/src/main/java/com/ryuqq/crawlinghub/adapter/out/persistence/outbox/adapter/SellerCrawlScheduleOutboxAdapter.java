package com.ryuqq.crawlinghub.adapter.out.persistence.outbox.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity.SellerCrawlScheduleOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.mapper.SellerCrawlScheduleOutboxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.repository.SellerCrawlScheduleOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.outbox.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.outbox.SellerCrawlScheduleOutbox;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 셀러 크롤링 스케줄 Outbox Adapter (Persistence Adapter)
 * <p>
 * SellerCrawlScheduleOutboxPort의 JPA 구현체입니다.
 * Domain Model과 JPA Entity 간의 변환을 담당합니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>Domain → Entity 변환</li>
 *   <li>Entity → Domain 변환</li>
 *   <li>JPA Repository 호출</li>
 *   <li>트랜잭션 관리</li>
 * </ul>
 * </p>
 * <p>
 * 트랜잭션 전략:
 * <ul>
 *   <li>REQUIRES_NEW: 독립적인 Outbox 트랜잭션</li>
 *   <li>이유: Domain 트랜잭션과 분리하여 Outbox만 커밋</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class SellerCrawlScheduleOutboxAdapter implements SellerCrawlScheduleOutboxPort {

    private final SellerCrawlScheduleOutboxJpaRepository repository;
    private final SellerCrawlScheduleOutboxMapper mapper;

    /**
     * 생성자
     *
     * @param repository Outbox JPA Repository
     * @param mapper     Domain ↔ Entity Mapper
     */
    public SellerCrawlScheduleOutboxAdapter(
            SellerCrawlScheduleOutboxJpaRepository repository,
            SellerCrawlScheduleOutboxMapper mapper
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.mapper = Objects.requireNonNull(mapper);
    }

    /**
     * Outbox 저장
     * <p>
     * REQUIRES_NEW 트랜잭션으로 독립적으로 커밋됩니다.
     * </p>
     *
     * @param outbox Outbox 도메인 모델
     * @return 저장된 Outbox (ID 포함)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SellerCrawlScheduleOutbox save(SellerCrawlScheduleOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");

        SellerCrawlScheduleOutboxEntity entity = mapper.toEntity(outbox);
        SellerCrawlScheduleOutboxEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    /**
     * OpId로 Outbox 조회
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return Outbox 도메인 모델 (존재하지 않으면 null)
     */
    @Override
    @Transactional(readOnly = true)
    public SellerCrawlScheduleOutbox findByOpId(String opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        return repository.findByOpId(opId)
                .map(mapper::toDomain)
                .orElse(null);
    }

    /**
     * Idempotency Key로 Outbox 조회
     *
     * @param idemKey Idempotency Key
     * @return Outbox 도메인 모델 (존재하지 않으면 null)
     */
    @Override
    @Transactional(readOnly = true)
    public SellerCrawlScheduleOutbox findByIdemKey(String idemKey) {
        Objects.requireNonNull(idemKey, "idemKey must not be null");

        return repository.findByIdemKey(idemKey)
                .map(mapper::toDomain)
                .orElse(null);
    }

    /**
     * Seller ID로 최신 Outbox 조회
     *
     * @param sellerId Seller PK (Long FK)
     * @return Outbox 도메인 모델 (존재하지 않으면 null)
     */
    @Override
    @Transactional(readOnly = true)
    public SellerCrawlScheduleOutbox findLatestBySellerId(Long sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        List<SellerCrawlScheduleOutboxEntity> outboxes =
                repository.findBySellerIdOrderByCreatedAtDesc(sellerId);

        if (outboxes.isEmpty()) {
            return null;
        }

        return mapper.toDomain(outboxes.get(0));
    }

    /**
     * OpId 업데이트
     * <p>
     * REQUIRES_NEW 트랜잭션으로 독립적으로 커밋됩니다.
     * </p>
     *
     * @param outboxId Outbox PK
     * @param opId     Orchestrator OpId
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOpId(Long outboxId, String opId) {
        Objects.requireNonNull(outboxId, "outboxId must not be null");
        Objects.requireNonNull(opId, "opId must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findById(outboxId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for ID: " + outboxId
                ));

        entity.setOpId(opId);
        repository.save(entity);
    }
}
