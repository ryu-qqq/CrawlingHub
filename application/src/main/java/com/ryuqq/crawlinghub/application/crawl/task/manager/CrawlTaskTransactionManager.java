package com.ryuqq.crawlinghub.application.crawl.task.manager;

import com.ryuqq.crawlinghub.application.crawl.task.port.out.command.CrawlTaskPersistencePort;
import com.ryuqq.crawlinghub.application.crawl.task.port.out.messaging.CrawlTaskMessagePort;
import com.ryuqq.crawlinghub.application.crawl.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.crawl.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.crawl.task.exception.DuplicateCrawlTaskException;
import com.ryuqq.crawlinghub.domain.crawl.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskType;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.exception.InvalidSchedulerStateException;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

/**
 * CrawlTask Transaction Manager
 *
 * <p>CrawlTask 트랜잭션 경계 관리 및 SQS 발행 조정
 *
 * <p><strong>책임</strong>:
 * <ul>
 *   <li>Schedule 상태 검증 (ACTIVE만 트리거 가능)</li>
 *   <li>중복 Task 검증 (진행 중인 Task가 있으면 생성 불가)</li>
 *   <li>Task 생성 및 저장</li>
 *   <li>afterCommit에서 SQS 발행 등록</li>
 *   <li>Idempotency Key 생성</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskTransactionManager {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskTransactionManager.class);

    private static final List<CrawlTaskStatus> IN_PROGRESS_STATUSES = List.of(
            CrawlTaskStatus.WAITING,
            CrawlTaskStatus.PUBLISHED,
            CrawlTaskStatus.RUNNING,
            CrawlTaskStatus.RETRY
    );

    private final CrawlTaskPersistencePort crawlTaskPersistencePort;
    private final CrawlTaskQueryPort crawlTaskQueryPort;
    private final CrawlTaskMessagePort crawlTaskMessagePort;
    private final CrawlScheduleQueryPort crawlScheduleQueryPort;

    public CrawlTaskTransactionManager(
            CrawlTaskPersistencePort crawlTaskPersistencePort,
            CrawlTaskQueryPort crawlTaskQueryPort,
            CrawlTaskMessagePort crawlTaskMessagePort,
            CrawlScheduleQueryPort crawlScheduleQueryPort
    ) {
        this.crawlTaskPersistencePort = crawlTaskPersistencePort;
        this.crawlTaskQueryPort = crawlTaskQueryPort;
        this.crawlTaskMessagePort = crawlTaskMessagePort;
        this.crawlScheduleQueryPort = crawlScheduleQueryPort;
    }

    /**
     * CrawlTask 트리거 (생성 + 저장 + SQS 발행 예약)
     *
     * <p>Transaction commit 후 SQS 발행을 수행하여 메시지 손실 방지
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @return 생성된 CrawlTask
     */
    @Transactional
    public CrawlTask trigger(CrawlSchedulerId crawlSchedulerId) {
        // 1. Schedule 조회 및 상태 검증
        CrawlScheduler scheduler = findAndValidateScheduler(crawlSchedulerId);

        // 2. 중복 Task 검증
        validateNoDuplicateTask(crawlSchedulerId, scheduler.getSellerIdValue());

        // 3. CrawlTask 생성
        CrawlTask crawlTask = createCrawlTask(scheduler);

        // 4. Task 저장
        CrawlTaskId savedId = crawlTaskPersistencePort.persist(crawlTask);

        // 5. 저장된 ID로 Task 재구성 (영속화된 ID 반영)
        CrawlTask savedTask = CrawlTask.reconstitute(
                savedId,
                crawlTask.getCrawlSchedulerId(),
                crawlTask.getSellerId(),
                crawlTask.getTaskType(),
                crawlTask.getEndpoint(),
                crawlTask.getStatus(),
                crawlTask.getRetryCount(),
                crawlTask.getCreatedAt(),
                crawlTask.getUpdatedAt()
        );

        // 6. afterCommit에서 SQS 발행 등록
        registerAfterCommitPublish(savedTask);

        return savedTask;
    }

    /**
     * Schedule 조회 및 상태 검증
     */
    private CrawlScheduler findAndValidateScheduler(CrawlSchedulerId crawlSchedulerId) {
        CrawlScheduler scheduler = crawlScheduleQueryPort.findById(crawlSchedulerId)
                .orElseThrow(() -> new CrawlSchedulerNotFoundException(crawlSchedulerId.value()));

        if (scheduler.getStatus() != SchedulerStatus.ACTIVE) {
            throw new InvalidSchedulerStateException(
                    scheduler.getStatus(),
                    SchedulerStatus.ACTIVE
            );
        }

        return scheduler;
    }

    /**
     * 중복 Task 검증
     */
    private void validateNoDuplicateTask(CrawlSchedulerId crawlSchedulerId, Long sellerId) {
        boolean exists = crawlTaskQueryPort.existsByScheduleIdAndStatusIn(
                crawlSchedulerId,
                IN_PROGRESS_STATUSES
        );

        if (exists) {
            throw new DuplicateCrawlTaskException(sellerId, CrawlTaskType.MINI_SHOP);
        }
    }

    /**
     * CrawlTask 생성
     */
    private CrawlTask createCrawlTask(CrawlScheduler scheduler) {
        // 미니샵 목록 크롤링 Task 생성 (기본)
        CrawlEndpoint endpoint = CrawlEndpoint.forMiniShopList(
                scheduler.getSellerIdValue(),
                1, // 첫 페이지
                20 // 기본 페이지 크기
        );

        return CrawlTask.forNew(
                scheduler.getCrawlSchedulerId(),
                scheduler.getSellerId(),
                CrawlTaskType.MINI_SHOP,
                endpoint
        );
    }

    /**
     * afterCommit에서 SQS 발행 등록
     *
     * <p>Transaction이 성공적으로 commit된 후에만 SQS 메시지 발행
     * <p>발행 실패 시 로그만 남기고 Fallback Scheduler가 재처리
     */
    private void registerAfterCommitPublish(CrawlTask crawlTask) {
        String idempotencyKey = generateIdempotencyKey(crawlTask);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            crawlTaskMessagePort.publish(crawlTask, idempotencyKey);
                            log.info("CrawlTask SQS 발행 완료. taskId={}, idempotencyKey={}",
                                    crawlTask.getId().value(), idempotencyKey);
                        } catch (Exception e) {
                            log.error("CrawlTask SQS 발행 실패. taskId={}, idempotencyKey={}. Fallback Scheduler가 재처리 예정.",
                                    crawlTask.getId().value(), idempotencyKey, e);
                        }
                    }
                }
        );
    }

    /**
     * Idempotency Key 생성
     *
     * <p>schedulerId + taskId + timestamp 조합으로 고유 키 생성
     */
    private String generateIdempotencyKey(CrawlTask crawlTask) {
        return String.format("%s-%s-%s",
                crawlTask.getCrawlSchedulerId().value(),
                crawlTask.getId().value(),
                UUID.randomUUID().toString().substring(0, 8)
        );
    }
}
