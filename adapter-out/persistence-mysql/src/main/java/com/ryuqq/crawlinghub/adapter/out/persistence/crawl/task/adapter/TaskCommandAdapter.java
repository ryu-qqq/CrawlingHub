package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.TaskEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.mapper.TaskMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.repository.TaskJpaRepository;
import com.ryuqq.crawlinghub.application.task.port.out.SaveTaskPort;
import com.ryuqq.crawlinghub.domain.task.Task;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Task 저장 Adapter
 *
 * <p>SaveTaskPort 인터페이스의 구현체로, Persistence Layer에서 Task 저장을 담당합니다.</p>
 *
 * <p><strong>역할:</strong></p>
 * <ul>
 *   <li>Application Layer의 SaveTaskPort 포트를 구현</li>
 *   <li>TaskJpaRepository를 통해 DB에 Task 저장</li>
 *   <li>TaskMapper를 통해 Domain ↔ Entity 변환</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - 명시적 생성자 주입</li>
 *   <li>✅ Port 구현 - SaveTaskPort 인터페이스 구현</li>
 *   <li>✅ Mapper 사용 - Entity ↔ Domain 변환</li>
 *   <li>✅ @Component - Spring Bean 등록</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
@Component
public class TaskCommandAdapter implements SaveTaskPort {

    private final TaskJpaRepository taskJpaRepository;

    /**
     * 생성자 주입
     *
     * @param taskJpaRepository Task JPA Repository
     */
    public TaskCommandAdapter(TaskJpaRepository taskJpaRepository) {
        this.taskJpaRepository = Objects.requireNonNull(taskJpaRepository, "taskJpaRepository must not be null");
    }

    /**
     * Task 저장
     *
     * <p>Domain Task 객체를 Entity로 변환하여 DB에 저장하고, 저장된 결과를 다시 Domain 객체로 변환하여 반환합니다.</p>
     *
     * @param task 저장할 Task Domain 객체
     * @return Task 저장된 Task Domain 객체 (ID 포함)
     * @throws NullPointerException task가 null인 경우
     */
    @Override
    public Task save(Task task) {
        Objects.requireNonNull(task, "task must not be null");

        TaskEntity entity = TaskMapper.toEntity(task);
        TaskEntity savedEntity = taskJpaRepository.save(entity);
        return TaskMapper.toDomain(savedEntity);
    }

    /**
     * 여러 Task 일괄 저장
     *
     * <p>여러 Task를 일괄로 저장합니다. 각 Task를 순회하며 save() 메서드를 호출합니다.</p>
     *
     * @param tasks 저장할 Task 리스트
     * @return List&lt;Task&gt; 저장된 Task 리스트 (ID 포함)
     * @throws NullPointerException tasks가 null인 경우
     */
    @Override
    public List<Task> saveAll(List<Task> tasks) {
        Objects.requireNonNull(tasks, "tasks must not be null");

        List<TaskEntity> entities = tasks.stream()
            .map(TaskMapper::toEntity)
            .collect(Collectors.toList());

        List<TaskEntity> savedEntities = taskJpaRepository.saveAll(entities);

        return savedEntities.stream()
            .map(TaskMapper::toDomain)
            .collect(Collectors.toList());
    }
}
