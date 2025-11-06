package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.ScheduleOutboxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.ScheduleOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.ScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * ScheduleOutbox Command Adapter (CQRS - Command, JpaRepository)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ CUD (Create, Update, Delete) 작업 전담</li>
 *   <li>✅ ScheduleOutboxPort 구현</li>
 *   <li>✅ JpaRepository 사용</li>
 *   <li>✅ Mapper를 통한 Domain ↔ Entity 변환</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>✅ Command (쓰기) 전용 Adapter</li>
 *   <li>✅ Query (읽기)는 ScheduleOutboxQueryAdapter에 위임</li>
 *   <li>✅ JpaRepository 사용 (save, delete)</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java Constructor</li>
 *   <li>✅ @Component (Spring Bean 등록)</li>
 *   <li>✅ Objects.requireNonNull() 검증</li>
 *   <li>✅ Mapper 패턴 사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleOutboxCommandAdapter implements ScheduleOutboxPort {

    private final ScheduleOutboxJpaRepository repository;
    private final ScheduleOutboxMapper mapper;
    private final ScheduleOutboxQueryAdapter queryAdapter;

    /**
     * 생성자
     *
     * @param repository   Outbox JPA Repository
     * @param mapper       Domain ↔ Entity Mapper
     * @param queryAdapter Query Adapter (Read 작업 위임)
     */
    public ScheduleOutboxCommandAdapter(
        ScheduleOutboxJpaRepository repository,
        ScheduleOutboxMapper mapper,
        ScheduleOutboxQueryAdapter queryAdapter
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
        this.queryAdapter = Objects.requireNonNull(queryAdapter, "queryAdapter must not be null");
    }

    /**
     * Outbox 저장
     *
     * <p>트랜잭션은 Application Layer에서 관리됩니다.</p>
     *
     * @param outbox Outbox 도메인 모델
     * @return 저장된 Outbox (ID 포함)
     */
    @Override
    public ScheduleOutbox save(ScheduleOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");

        ScheduleOutboxEntity entity = mapper.toEntity(outbox);
        ScheduleOutboxEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    /**
     * Idempotency Key로 Outbox 조회
     *
     * <p>Query Adapter로 위임합니다.</p>
     *
     * @param idemKey Idempotency Key
     * @return Outbox (Optional)
     */
    @Override
    public Optional<ScheduleOutbox> findByIdemKey(String idemKey) {
        return queryAdapter.findByIdemKey(idemKey);
    }

    /**
     * Idempotency Key 존재 여부 확인
     *
     * <p>Query Adapter로 위임합니다.</p>
     *
     * @param idemKey Idempotency Key
     * @return 존재 여부
     */
    @Override
    public boolean existsByIdemKey(String idemKey) {
        return queryAdapter.existsByIdemKey(idemKey);
    }

    /**
     * WAL State가 PENDING인 Outbox 목록 조회
     *
     * <p>Query Adapter로 위임합니다.</p>
     *
     * @return PENDING 상태의 Outbox 목록
     */
    @Override
    public java.util.List<ScheduleOutbox> findByWalStatePending() {
        return queryAdapter.findByWalStatePending();
    }

    /**
     * Operation State가 FAILED인 Outbox 목록 조회
     *
     * <p>Query Adapter로 위임합니다.</p>
     *
     * @return FAILED 상태의 Outbox 목록
     */
    @Override
    public java.util.List<ScheduleOutbox> findByOperationStateFailed() {
        return queryAdapter.findByOperationStateFailed();
    }

    /**
     * WAL State가 COMPLETED인 Outbox 목록 조회
     *
     * <p>Query Adapter로 위임합니다.</p>
     *
     * @return COMPLETED 상태의 Outbox 목록
     */
    @Override
    public java.util.List<ScheduleOutbox> findByWalStateCompleted() {
        return queryAdapter.findByWalStateCompleted();
    }

    /**
     * Outbox 삭제
     *
     * <p>트랜잭션은 Application Layer에서 관리됩니다.</p>
     *
     * @param outbox 삭제할 Outbox
     */
    @Override
    public void delete(ScheduleOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");
        if (outbox.getId() == null) {
            throw new IllegalArgumentException("Outbox ID가 없어 삭제할 수 없습니다");
        }
        repository.deleteById(outbox.getId());
    }
}
