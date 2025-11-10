package com.ryuqq.crawlinghub.application.task.port.out;

import com.ryuqq.crawlinghub.domain.task.Task;
import com.ryuqq.crawlinghub.domain.task.TaskId;

import java.util.Optional;

/**
 * Task 조회 Port
 *
 * <p>Persistence Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface LoadTaskPort {

    /**
     * Task ID로 조회
     *
     * @param taskId Task ID
     * @return Task (없으면 Optional.empty())
     */
    Optional<Task> findById(TaskId taskId);
}
