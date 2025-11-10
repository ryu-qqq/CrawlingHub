package com.ryuqq.crawlinghub.application.task.manager;

import com.ryuqq.crawlinghub.application.task.port.out.LoadTaskPort;
import com.ryuqq.crawlinghub.application.task.port.out.SaveTaskPort;
import com.ryuqq.crawlinghub.domain.task.Task;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Task 관리 매니저
 *
 * <p>Task의 CRUD 및 상태 관리를 담당
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class TaskManager {

    private final LoadTaskPort loadTaskPort;
    private final SaveTaskPort saveTaskPort;

    public TaskManager(LoadTaskPort loadTaskPort, SaveTaskPort saveTaskPort) {
        this.loadTaskPort = loadTaskPort;
        this.saveTaskPort = saveTaskPort;
    }

    /**
     * Task 조회
     */
    @Transactional(readOnly = true)
    public Task getTask(TaskId taskId) {
        return loadTaskPort.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task를 찾을 수 없습니다: " + taskId));
    }

    /**
     * Task 저장
     */
    @Transactional
    public Task saveTask(Task task) {
        return saveTaskPort.save(task);
    }

    /**
     * Task 일괄 저장
     */
    @Transactional
    public List<Task> saveTasks(List<Task> tasks) {
        return saveTaskPort.saveAll(tasks);
    }

    /**
     * Task 완료 처리
     */
    @Transactional
    public void completeTask(TaskId taskId) {
        Task task = getTask(taskId);
        task.completeSuccessfully();
        saveTaskPort.save(task);
    }

    /**
     * Task 실패 처리
     */
    @Transactional
    public void failTask(TaskId taskId, String errorMessage) {
        Task task = getTask(taskId);
        task.failWithError(errorMessage);
        saveTaskPort.save(task);
    }
}
