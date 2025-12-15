package com.ryuqq.crawlinghub.application.task.facade;

import com.ryuqq.crawlinghub.application.task.component.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.application.task.dto.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskTransactionManager;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlTask Facade
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>검증 로직 조율 (Validator)
 *   <li>저장 로직 조율 (TransactionManager)
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>스케줄러 상태 검증 (ACTIVE만 트리거 가능)
 *   <li>중복 Task 검증 (진행 중인 Task가 있으면 생성 불가)
 *   <li>CrawlTask + Outbox 저장 (단일 트랜잭션)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskFacade {

    private final CrawlTaskPersistenceValidator validator;
    private final CrawlTaskTransactionManager transactionManager;
    private final CrawlTaskOutboxTransactionManager crawlTaskOutboxTransactionManager;
    private final ApplicationEventPublisher eventPublisher;
    private final ClockHolder clockHolder;

    public CrawlTaskFacade(
            CrawlTaskPersistenceValidator validator,
            CrawlTaskTransactionManager transactionManager,
            CrawlTaskOutboxTransactionManager crawlTaskOutboxTransactionManager,
            ApplicationEventPublisher eventPublisher,
            ClockHolder clockHolder) {
        this.validator = validator;
        this.transactionManager = transactionManager;
        this.crawlTaskOutboxTransactionManager = crawlTaskOutboxTransactionManager;
        this.eventPublisher = eventPublisher;
        this.clockHolder = clockHolder;
    }

    /**
     * CrawlTask 번들을 하나의 트랜잭션으로 저장
     *
     * <p><strong>트랜잭션 범위</strong>:
     *
     * <ol>
     *   <li>스케줄러 검증
     *   <li>중복 Task 검증
     *   <li>CrawlTask 저장 → ID 반환 → 번들에 설정
     *   <li>Outbox 저장 (Task ID 참조)
     * </ol>
     *
     * @param bundle 저장할 CrawlTask 번들
     * @return 저장된 CrawlTask (ID 할당됨, ClockHolder 캡슐화)
     */
    @Transactional
    public CrawlTask persist(CrawlTaskBundle bundle) {
        CrawlTask crawlTask = bundle.getCrawlTask();

        // 1. 중복 Task 검증
        validator.validateNoDuplicateTask(
                bundle.getCrawlScheduleId(), crawlTask.getSellerId(), crawlTask.getTaskType());

        // 2. CrawlTask 저장 → ID 반환 → 새 번들 반환 (Immutable)
        CrawlTaskId savedTaskId = transactionManager.persist(crawlTask);
        bundle = bundle.withTaskId(savedTaskId);

        // 3. Outbox 저장
        crawlTaskOutboxTransactionManager.persist(bundle.createOutbox(clockHolder.getClock()));

        // 4. 저장된 CrawlTask 생성 + 도메인 이벤트 발행
        CrawlTask savedTask = bundle.getSavedCrawlTask(clockHolder.getClock());
        savedTask.getDomainEvents().forEach(eventPublisher::publishEvent);
        savedTask.clearDomainEvents();

        return savedTask;
    }

    /**
     * CrawlTask 재시도를 하나의 트랜잭션으로 처리
     *
     * <p><strong>트랜잭션 범위</strong>:
     *
     * <ol>
     *   <li>CrawlTask 업데이트 (상태 변경 → RETRY)
     *   <li>Outbox 저장 (SQS 재발행용)
     * </ol>
     *
     * @param crawlTask 재시도할 CrawlTask (attemptRetry 호출 완료 상태)
     * @param outboxPayload SQS 재발행용 페이로드
     * @return 업데이트된 CrawlTask
     */
    @Transactional
    public CrawlTask retry(CrawlTask crawlTask, String outboxPayload) {
        // 1. CrawlTask 업데이트 저장
        transactionManager.persist(crawlTask);

        // 2. Outbox 저장 (SQS 재발행용)
        CrawlTaskOutbox outbox =
                CrawlTaskOutbox.forNew(crawlTask.getId(), outboxPayload, clockHolder.getClock());
        crawlTaskOutboxTransactionManager.persist(outbox);

        return crawlTask;
    }
}
