package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.TaskMessageOutBoxEntity;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageOutbox;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageStatus;

import java.util.Objects;

/**
 * TaskMessageOutbox 도메인 객체와 TaskMessageOutBoxEntity 간 변환을 담당하는 Mapper
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java Static Methods</li>
 *   <li>✅ Stateless Mapper - 모든 메서드 static</li>
 *   <li>✅ Enum 변환 - String ↔ Enum 양방향 변환</li>
 *   <li>✅ Value Object 변환 - TaskId (Long 포함)</li>
 * </ul>
 *
 * <p><strong>변환 규칙:</strong></p>
 * <ul>
 *   <li>Domain TaskId → Entity Long (taskId.value())</li>
 *   <li>Domain TaskType Enum → Entity String (taskType.name())</li>
 *   <li>Domain TaskMessageStatus Enum → Entity String (status.name())</li>
 *   <li>Entity Long → Domain TaskId (TaskId.of(long))</li>
 *   <li>Entity String → Domain Enum (Enum.valueOf(string))</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
public class TaskMessageOutBoxMapper {

    /**
     * Private 생성자 - Utility 클래스이므로 인스턴스화 금지
     */
    private TaskMessageOutBoxMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Domain TaskMessageOutbox → TaskMessageOutBoxEntity 변환
     *
     * <p>Domain 객체를 JPA Entity로 변환합니다.</p>
     *
     * <p><strong>변환 로직:</strong></p>
     * <ul>
     *   <li>outboxId가 null이면 create() - 신규 생성</li>
     *   <li>outboxId가 있으면 reconstitute() - DB에서 로드된 객체</li>
     * </ul>
     *
     * @param outbox 도메인 TaskMessageOutbox 객체
     * @return TaskMessageOutBoxEntity JPA Entity
     * @throws NullPointerException outbox가 null인 경우
     */
    public static TaskMessageOutBoxEntity toEntity(TaskMessageOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");

        Long outboxId = outbox.getOutboxId();

        if (outboxId == null) {
            // 신규 생성 (outboxId 없음)
            return TaskMessageOutBoxEntity.create(
                outbox.getTaskId().value(),
                outbox.getTaskType().name(),
                outbox.getStatus().name(),
                outbox.getRetryCount(),
                outbox.getErrorMessage(),
                outbox.getCreatedAt()
            );
        } else {
            // DB reconstitute (outboxId 있음)
            return TaskMessageOutBoxEntity.reconstitute(
                outboxId,
                outbox.getTaskId().value(),
                outbox.getTaskType().name(),
                outbox.getStatus().name(),
                outbox.getRetryCount(),
                outbox.getErrorMessage(),
                outbox.getCreatedAt(),
                outbox.getSentAt()
            );
        }
    }

    /**
     * TaskMessageOutBoxEntity → Domain TaskMessageOutbox 변환
     *
     * <p>JPA Entity를 Domain 객체로 변환합니다.</p>
     *
     * <p><strong>변환 로직:</strong></p>
     * <ul>
     *   <li>Entity Long → Domain TaskId (Value Object)</li>
     *   <li>Entity String → Domain Enum (TaskType, TaskMessageStatus)</li>
     *   <li>항상 reconstitute() 사용 (DB에서 로드되었으므로)</li>
     * </ul>
     *
     * @param entity TaskMessageOutBoxEntity JPA Entity
     * @return TaskMessageOutbox 도메인 객체
     * @throws NullPointerException entity가 null인 경우
     * @throws IllegalArgumentException entity의 필수 필드가 null인 경우
     */
    public static TaskMessageOutbox toDomain(TaskMessageOutBoxEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        return TaskMessageOutbox.reconstitute(
            entity.getOutboxId(),
            TaskId.of(entity.getTaskId()),
            TaskType.valueOf(entity.getTaskType()),
            TaskMessageStatus.valueOf(entity.getStatus()),
            entity.getRetryCount(),
            entity.getErrorMessage(),
            entity.getCreatedAt(),
            entity.getSentAt()
        );
    }
}
