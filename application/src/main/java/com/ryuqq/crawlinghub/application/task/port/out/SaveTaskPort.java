package com.ryuqq.crawlinghub.application.task.port.out;

import com.ryuqq.crawlinghub.domain.task.Task;

import java.util.List;

/**
 * Task 저장 Port
 *
 * <p>Persistence Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface SaveTaskPort {

    /**
     * Task 단건 저장
     *
     * @param task 저장할 Task
     * @return 저장된 Task (ID 포함)
     */
    Task save(Task task);

    /**
     * Task 일괄 저장
     *
     * @param tasks 저장할 Task 목록
     * @return 저장된 Task 목록 (ID 포함)
     */
    List<Task> saveAll(List<Task> tasks);
}
