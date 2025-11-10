package com.ryuqq.crawlinghub.application.task.service;


import com.ryuqq.crawlinghub.application.task.dto.command.ProcessTaskCommand;
import com.ryuqq.crawlinghub.application.task.manager.TaskManager;
import com.ryuqq.crawlinghub.application.task.port.in.ProcessCrawlTaskUseCase;
import com.ryuqq.crawlinghub.application.task.provider.TaskTypeProvider;
import com.ryuqq.crawlinghub.application.task.strategy.TaskStrategy;
import com.ryuqq.crawlinghub.domain.task.Task;
import com.ryuqq.crawlinghub.domain.task.TaskId;

import org.springframework.stereotype.Service;

/**
 * 크롤링 태스크 처리 UseCase 구현체 (SQS Consumer)
 * <p>
 * ⭐ Strategy Pattern 적용:
 * - TaskTypeProvider를 통해 TaskType별 전략 선택
 * - 각 전략이 자신의 처리 로직 실행
 * - 각 Strategy 내부에서 CrawlerFacade 사용 (TokenAcquisitionManager 통합)
 * </p>
 * <p>
 * ⚠️ Transaction 경계:
 * - 외부 API 호출이 메인 로직이므로 트랜잭션 사용 안 함
 * - 상태 업데이트는 각 Strategy 내부에서 별도 트랜잭션으로 실행
 * </p>
 * <p>
 * ✅ 레거시 코드 제거:
 * - UserAgentPort, TokenManagerPort 제거 (TokenAcquisitionManager로 대체)
 * - 각 Strategy가 CrawlerFacade를 통해 크롤링 실행
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class ProcessCrawlTaskService implements ProcessCrawlTaskUseCase {

    private final TaskManager taskManager;
    private final TaskTypeProvider taskTypeProvider;

    /**
     * Service 생성자
     *
     * @param taskManager      Task 관리자
     * @param taskTypeProvider TaskType별 Strategy Provider
     */
    public ProcessCrawlTaskService(
        TaskManager taskManager,
        TaskTypeProvider taskTypeProvider
    ) {
        this.taskManager = taskManager;
        this.taskTypeProvider = taskTypeProvider;
    }

    /**
     * 태스크 처리 (Strategy Pattern 적용)
     * <p>
     * 실행 흐름:
     * 1. 태스크 조회 (TaskManager)
     * 2. TaskType에 맞는 Strategy 선택 (TaskTypeProvider)
     * 3. Strategy 실행 (각 Strategy가 자신의 로직 처리)
     * </p>
     * <p>
     * ⭐ Strategy 내부에서:
     * - CrawlerFacade.execute() 호출
     * - CrawlerFacade → TokenAcquisitionManager.acquireToken()
     * - TokenAcquisitionManager가 7-Step Flow 실행
     * </p>
     *
     * @param command SQS 메시지
     */
    @Override
    public void execute(ProcessTaskCommand command) {
        TaskId taskId = TaskId.of(command.taskId());

        // 1. 태스크 조회
        Task task = taskManager.getTask(taskId);

        // 2. TaskType에 맞는 Strategy 선택
        TaskStrategy strategy = taskTypeProvider.getStrategy(task.getTaskType());

        // 3. Strategy 실행 (각 Strategy가 CrawlerFacade 사용)
        strategy.execute(task);
    }
}
