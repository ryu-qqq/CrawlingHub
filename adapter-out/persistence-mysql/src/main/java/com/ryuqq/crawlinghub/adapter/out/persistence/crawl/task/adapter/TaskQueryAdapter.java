package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.TaskEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.mapper.TaskMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.repository.TaskJpaRepository;
import com.ryuqq.crawlinghub.application.task.port.out.LoadTaskPort;
import com.ryuqq.crawlinghub.domain.task.Task;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Task 조회 Adapter
 *
 * <p>LoadTaskPort 인터페이스의 구현체로, Persistence Layer에서 Task 조회를 담당합니다.</p>
 *
 * <p><strong>역할:</strong></p>
 * <ul>
 *   <li>Application Layer의 LoadTaskPort 포트를 구현</li>
 *   <li>TaskJpaRepository를 통해 DB에서 Task 조회</li>
 *   <li>TaskMapper를 통해 Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - 명시적 생성자 주입</li>
 *   <li>✅ Port 구현 - LoadTaskPort 인터페이스 구현</li>
 *   <li>✅ Mapper 사용 - Entity ↔ Domain 변환</li>
 *   <li>✅ @Component - Spring Bean 등록</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
@Component
public class TaskQueryAdapter implements LoadTaskPort {

    private final TaskJpaRepository taskJpaRepository;

    /**
     * 생성자 주입
     *
     * @param taskJpaRepository Task JPA Repository
     */
    public TaskQueryAdapter(TaskJpaRepository taskJpaRepository) {
        this.taskJpaRepository = Objects.requireNonNull(taskJpaRepository, "taskJpaRepository must not be null");
    }

    /**
     * ID로 Task 조회
     *
     * <p>Task ID를 받아 DB에서 Task를 조회하고 Domain 객체로 변환하여 반환합니다.</p>
     *
     * @param taskId Task ID (Value Object)
     * @return Optional&lt;Task&gt; 조회된 Task Domain 객체 (존재하지 않으면 empty)
     * @throws NullPointerException taskId가 null인 경우
     */
    @Override
    public Optional<Task> findById(TaskId taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");

        if (taskId.value() == null) {
            return Optional.empty();
        }

        return taskJpaRepository.findById(taskId.value())
            .map(TaskMapper::toDomain);
    }
}
