package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.TaskMessageOutBoxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.mapper.TaskMessageOutBoxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.repository.TaskMessageOutBoxJpaRepository;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.repository.TaskMessageOutBoxQueryDslRepository;
import com.ryuqq.crawlinghub.application.task.port.out.LoadTaskMessageOutboxPort;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageOutbox;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TaskMessageOutbox 조회 Adapter
 *
 * <p>LoadTaskMessageOutboxPort 인터페이스의 구현체로, Persistence Layer에서 TaskMessageOutbox 조회를 담당합니다.</p>
 *
 * <p><strong>역할:</strong></p>
 * <ul>
 *   <li>Application Layer의 LoadTaskMessageOutboxPort 포트를 구현</li>
 *   <li>TaskMessageOutBoxJpaRepository를 통해 기본 조회</li>
 *   <li>TaskMessageOutBoxQueryDslRepository를 통해 Pessimistic Lock 조회</li>
 *   <li>TaskMessageOutBoxMapper를 통해 Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>Pessimistic Lock 사용:</strong></p>
 * <ul>
 *   <li>findByStatusWithLock() - Scheduler에서 PENDING 레코드 조회 시 사용</li>
 *   <li>다중 Scheduler 인스턴스 실행 시 동일 레코드 중복 처리 방지</li>
 *   <li>트랜잭션 내에서 Lock 획득 후 SQS 발행 및 상태 변경</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - 명시적 생성자 주입</li>
 *   <li>✅ Port 구현 - LoadTaskMessageOutboxPort 인터페이스 구현</li>
 *   <li>✅ Mapper 사용 - Entity ↔ Domain 변환</li>
 *   <li>✅ @Component - Spring Bean 등록</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
@Component
public class TaskMessageOutBoxQueryAdapter implements LoadTaskMessageOutboxPort {

    private final TaskMessageOutBoxJpaRepository jpaRepository;
    private final TaskMessageOutBoxQueryDslRepository queryDslRepository;

    /**
     * 생성자 주입
     *
     * @param jpaRepository      TaskMessageOutBox JPA Repository
     * @param queryDslRepository TaskMessageOutBox QueryDSL Repository
     */
    public TaskMessageOutBoxQueryAdapter(
        TaskMessageOutBoxJpaRepository jpaRepository,
        TaskMessageOutBoxQueryDslRepository queryDslRepository
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
        this.queryDslRepository = Objects.requireNonNull(queryDslRepository, "queryDslRepository must not be null");
    }

    /**
     * ID로 TaskMessageOutbox 조회
     *
     * <p>Outbox ID를 받아 DB에서 TaskMessageOutbox를 조회하고 Domain 객체로 변환하여 반환합니다.</p>
     *
     * @param outboxId Outbox ID (Long)
     * @return Optional&lt;TaskMessageOutbox&gt; 조회된 TaskMessageOutbox Domain 객체 (존재하지 않으면 empty)
     * @throws NullPointerException outboxId가 null인 경우
     */
    @Override
    public Optional<TaskMessageOutbox> findById(Long outboxId) {
        Objects.requireNonNull(outboxId, "outboxId must not be null");

        return jpaRepository.findById(outboxId)
            .map(TaskMessageOutBoxMapper::toDomain);
    }

    /**
     * 상태별 TaskMessageOutbox 조회
     *
     * <p>특정 상태(PENDING, SENT)의 Outbox 레코드들을 조회합니다.</p>
     *
     * <p><strong>주의:</strong></p>
     * <ul>
     *   <li>Lock 없이 조회하므로 단순 조회용으로만 사용</li>
     *   <li>Scheduler에서 처리할 PENDING 레코드 조회 시에는 findByStatusWithLock() 사용</li>
     * </ul>
     *
     * @param status TaskMessageStatus (PENDING, SENT)
     * @return List&lt;TaskMessageOutbox&gt; 해당 상태의 Outbox Domain 객체 리스트
     * @throws NullPointerException status가 null인 경우
     */
    @Override
    public List<TaskMessageOutbox> findByStatus(TaskMessageStatus status) {
        Objects.requireNonNull(status, "status must not be null");

        List<TaskMessageOutBoxEntity> entities = jpaRepository.findByStatus(status.name());

        return entities.stream()
            .map(TaskMessageOutBoxMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 상태별 TaskMessageOutbox 조회 (Pessimistic Lock)
     *
     * <p>특정 상태의 Outbox 레코드를 조회하면서 배타적 잠금을 획득합니다.</p>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <pre>
     * // TaskMessageOutboxScheduler에서 사용
     * &#64;Transactional
     * public void processOutbox() {
     *     // PENDING 레코드를 Lock으로 조회
     *     List&lt;TaskMessageOutbox&gt; pending =
     *         loadPort.findByStatusWithLock(TaskMessageStatus.PENDING);
     *
     *     for (TaskMessageOutbox outbox : pending) {
     *         // SQS 발행 시도
     *         sqsService.send(outbox);
     *
     *         // 성공 시 SENT로 변경
     *         outbox.markSent();
     *         savePort.save(outbox);
     *     }
     * }
     * </pre>
     *
     * <p><strong>동시성 보장:</strong></p>
     * <ul>
     *   <li>다중 Scheduler 인스턴스가 동시 실행되어도 동일 레코드 중복 처리 방지</li>
     *   <li>Scheduler A가 Lock 획득한 레코드는 Scheduler B가 접근 불가</li>
     *   <li>트랜잭션 완료 후 Lock 자동 해제</li>
     * </ul>
     *
     * @param status TaskMessageStatus (보통 PENDING)
     * @return List&lt;TaskMessageOutbox&gt; Lock이 걸린 Outbox Domain 객체 리스트
     * @throws NullPointerException status가 null인 경우
     */
    @Override
    public List<TaskMessageOutbox> findByStatusWithLock(TaskMessageStatus status) {
        Objects.requireNonNull(status, "status must not be null");

        List<TaskMessageOutBoxEntity> entities = queryDslRepository.findByStatusWithLock(status.name());

        return entities.stream()
            .map(TaskMessageOutBoxMapper::toDomain)
            .collect(Collectors.toList());
    }
}
