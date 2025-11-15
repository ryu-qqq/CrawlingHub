package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.TaskEntity;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerName;
import com.ryuqq.crawlinghub.domain.task.*;

import java.util.Objects;

/**
 * Task 도메인 객체와 TaskEntity 간 변환을 담당하는 Mapper
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java Static Methods</li>
 *   <li>✅ Stateless Mapper - 모든 메서드 static</li>
 *   <li>✅ Enum 변환 - String ↔ Enum 양방향 변환</li>
 *   <li>✅ Value Object 변환 - TaskId, MustitSellerId, SellerName, RequestUrl</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
public class TaskMapper {

    /**
     * Private 생성자 - Utility 클래스이므로 인스턴스화 금지
     */
    private TaskMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Domain Task → TaskEntity 변환
     *
     * <p>Domain 객체를 JPA Entity로 변환합니다.</p>
     *
     * @param task 도메인 Task 객체
     * @return TaskEntity JPA Entity
     * @throws NullPointerException task가 null인 경우
     */
    public static TaskEntity toEntity(Task task) {
        Objects.requireNonNull(task, "task must not be null");

        Long id = task.getIdValue();

        if (id == null) {
            // 신규 생성 (ID 없음)
            return TaskEntity.create(
                task.getSellerIdValue(),
                task.getSellerNameValue(),
                task.getTaskType(),
                task.getStatus(),
                task.getRequestUrlValue(),
                task.getPageNumber(),
                task.getRetryCount(),
                task.getIdempotencyKey(),
                task.getCrawlScheduleId(),
                task.getTriggerType(),
                task.getScheduledAt()
            );
        } else {
            // DB reconstitute (ID 있음)
            return TaskEntity.reconstitute(
                id,
                task.getSellerIdValue(),
                task.getSellerNameValue(),
                task.getTaskType(),
                task.getStatus(),
                task.getRequestUrlValue(),
                task.getPageNumber(),
                task.getRetryCount(),
                task.getIdempotencyKey(),
                task.getCrawlScheduleId(),
                task.getTriggerType(),
                task.getScheduledAt(),
                task.getStartedAt(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
            );
        }
    }

    /**
     * TaskEntity → Domain Task 변환
     *
     * <p>JPA Entity를 Domain 객체로 변환합니다.</p>
     *
     * @param entity TaskEntity JPA Entity
     * @return Task 도메인 객체
     * @throws NullPointerException entity가 null인 경우
     * @throws IllegalArgumentException entity의 필수 필드가 null인 경우
     */
    public static Task toDomain(TaskEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        return Task.reconstitute(
            TaskId.of(entity.getId()),
            MustItSellerId.of(entity.getSellerId()),
            SellerName.of(entity.getSellerName()),
            entity.getTaskType(),
            entity.getStatus(),
            RequestUrl.of(entity.getRequestUrl()),
            entity.getPageNumber(),
            entity.getRetryCount(),
            entity.getIdempotencyKey(),
            entity.getCrawlScheduleId(),
            entity.getTriggerType(),
            entity.getScheduledAt(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
