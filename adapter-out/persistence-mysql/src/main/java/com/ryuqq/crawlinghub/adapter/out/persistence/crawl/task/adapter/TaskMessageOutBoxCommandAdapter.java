package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.TaskMessageOutBoxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.mapper.TaskMessageOutBoxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.repository.TaskMessageOutBoxJpaRepository;
import com.ryuqq.crawlinghub.application.task.port.out.SaveTaskMessageOutboxPort;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageOutbox;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * TaskMessageOutbox 저장 Adapter
 *
 * <p>SaveTaskMessageOutboxPort 인터페이스의 구현체로, Persistence Layer에서 TaskMessageOutbox 저장을 담당합니다.</p>
 *
 * <p><strong>역할:</strong></p>
 * <ul>
 *   <li>Application Layer의 SaveTaskMessageOutboxPort 포트를 구현</li>
 *   <li>TaskMessageOutBoxJpaRepository를 통해 DB에 TaskMessageOutbox 저장</li>
 *   <li>TaskMessageOutBoxMapper를 통해 Domain ↔ Entity 변환</li>
 * </ul>
 *
 * <p><strong>Outbox 패턴 사용:</strong></p>
 * <ul>
 *   <li>Task 저장과 동시에 Outbox 레코드 생성 (트랜잭션 보장)</li>
 *   <li>별도 Scheduler가 PENDING 상태 레코드를 조회하여 SQS 발행</li>
 *   <li>발행 성공 시 SENT 상태로 변경</li>
 *   <li>발행 실패 시 retryCount 증가 (최대 3회)</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - 명시적 생성자 주입</li>
 *   <li>✅ Port 구현 - SaveTaskMessageOutboxPort 인터페이스 구현</li>
 *   <li>✅ Mapper 사용 - Entity ↔ Domain 변환</li>
 *   <li>✅ @Component - Spring Bean 등록</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
@Component
public class TaskMessageOutBoxCommandAdapter implements SaveTaskMessageOutboxPort {

    private final TaskMessageOutBoxJpaRepository jpaRepository;

    /**
     * 생성자 주입
     *
     * @param jpaRepository TaskMessageOutBox JPA Repository
     */
    public TaskMessageOutBoxCommandAdapter(TaskMessageOutBoxJpaRepository jpaRepository) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
    }

    /**
     * TaskMessageOutbox 저장
     *
     * <p>Domain TaskMessageOutbox 객체를 Entity로 변환하여 DB에 저장하고, 저장된 결과를 다시 Domain 객체로 변환하여 반환합니다.</p>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <pre>
     * // Task 생성 시 Outbox 함께 생성
     * &#64;Transactional
     * public Task createTask(CreateTaskCommand command) {
     *     // 1. Task 저장
     *     Task task = Task.create(...);
     *     Task savedTask = saveTaskPort.save(task);
     *
     *     // 2. Outbox 생성 (PENDING 상태)
     *     TaskMessageOutbox outbox = TaskMessageOutbox.createPending(
     *         savedTask.getId(),
     *         savedTask.getTaskType()
     *     );
     *     saveOutboxPort.save(outbox);
     *
     *     return savedTask;
     * }
     * </pre>
     *
     * <p><strong>Scheduler에서 상태 변경 시 사용:</strong></p>
     * <pre>
     * // PENDING → SENT 상태 변경
     * &#64;Transactional
     * public void processOutbox() {
     *     List&lt;TaskMessageOutbox&gt; pending =
     *         loadPort.findByStatusWithLock(TaskMessageStatus.PENDING);
     *
     *     for (TaskMessageOutbox outbox : pending) {
     *         sqsService.send(outbox);
     *
     *         outbox.markSent(); // SENT 상태로 변경
     *         saveOutboxPort.save(outbox); // 저장
     *     }
     * }
     * </pre>
     *
     * @param outbox 저장할 TaskMessageOutbox Domain 객체
     * @return TaskMessageOutbox 저장된 TaskMessageOutbox Domain 객체 (outboxId 포함)
     * @throws NullPointerException outbox가 null인 경우
     */
    @Override
    public TaskMessageOutbox save(TaskMessageOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");

        TaskMessageOutBoxEntity entity = TaskMessageOutBoxMapper.toEntity(outbox);
        TaskMessageOutBoxEntity savedEntity = jpaRepository.save(entity);
        return TaskMessageOutBoxMapper.toDomain(savedEntity);
    }
}
