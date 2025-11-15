package com.ryuqq.crawlinghub.application.task.service;


import com.ryuqq.crawlinghub.application.task.dto.command.InitiateCrawlingCommand;
import com.ryuqq.crawlinghub.application.task.component.TaskManager;
import com.ryuqq.crawlinghub.application.task.component.TaskMessageOutboxManager;
import com.ryuqq.crawlinghub.application.task.port.in.InitiateCrawlingUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import com.ryuqq.crawlinghub.domain.seller.MustItSeller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.task.Task;
import com.ryuqq.crawlinghub.domain.task.TaskId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 크롤링 시작 UseCase 구현체
 *
 * <p>초기 META 태스크를 생성하고 TaskMessage로 발행합니다.
 *
 * <p>변경 사항:
 * - Task static 메서드를 사용한 Task 생성
 * - TaskManager를 사용한 Task 저장
 * - TaskMessageManager를 사용한 메시지 발행
 *
 * <p>⚠️ Transaction 경계:
 * <ul>
 *   <li>트랜잭션 내부: DB 작업 (태스크 저장, TaskMessageOutbox 저장)</li>
 *   <li>트랜잭션 외부: 실제 SQS 발행 (Polling Worker가 담당)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class InitiateCrawlingService implements InitiateCrawlingUseCase {

    private final LoadSellerPort loadSellerPort;
    private final TaskManager taskManager;
    private final TaskMessageOutboxManager taskMessageOutboxManager;
    private final com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler sellerAssembler;

    public InitiateCrawlingService(
        LoadSellerPort loadSellerPort,
        TaskManager taskManager,
        TaskMessageOutboxManager taskMessageOutboxManager,
        com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler sellerAssembler
    ) {
        this.loadSellerPort = loadSellerPort;
        this.taskManager = taskManager;
        this.taskMessageOutboxManager = taskMessageOutboxManager;
        this.sellerAssembler = sellerAssembler;
    }

    /**
     * 크롤링 시작 (간소화된 파이프라인)
     *
     * <p>실행 흐름:
     * 1. 셀러 상태 확인 (ACTIVE만 크롤링 가능)
     * 2. META Task 생성 (Task.forMeta())
     * 3. Task 저장 (TaskManager)
     * 4. TaskMessage 발행 (TaskMessageManager)
     *
     * <p>변경 사항:
     * - 초기 Task를 MINI_SHOP이 아닌 META로 변경
     * - META Task가 전체 상품 수 조회 → MINI_SHOP Task들 생성
     * - Task static 메서드 사용으로 TaskFactory 제거
     *
     * @param command 시작할 셀러 정보
     * @throws IllegalArgumentException 셀러를 찾을 수 없거나 비활성 상태인 경우
     */
    @Override
    @Transactional
    public void execute(InitiateCrawlingCommand command) {
        MustItSellerId sellerId = MustItSellerId.of(command.sellerId());

        // 1. 셀러 상태 확인
        MustItSeller seller = loadSellerPort.findById(sellerId)
            .map(sellerAssembler::toDomain)
            .orElseThrow(() -> new SellerNotFoundException(command.sellerId()));

        if (!seller.isActive()) {
            throw new IllegalStateException(
                "비활성 셀러는 크롤링할 수 없습니다: " + command.sellerId()
            );
        }

        // 2. META Task 생성 (Task static 메서드 - MANUAL 트리거)
        Task metaTask = Task.forMeta(
            sellerId,
            seller.getSellerName(),
            null, // REST API를 통한 수동 실행이므로 crawlScheduleId는 null
            com.ryuqq.crawlinghub.domain.task.TriggerType.MANUAL, // REST API → MANUAL
            LocalDateTime.now()
        );

        // 3. Task 저장 (TaskManager)
        Task savedTask = taskManager.saveTask(metaTask);

        // 4. TaskMessage 발행 (TaskMessageOutboxManager)
        taskMessageOutboxManager.createTaskMessage(
            TaskId.of(savedTask.getIdValue()),
            savedTask.getTaskType()
        );
    }
}
