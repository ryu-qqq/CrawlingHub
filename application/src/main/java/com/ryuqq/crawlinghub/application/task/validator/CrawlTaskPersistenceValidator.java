package com.ryuqq.crawlinghub.application.task.validator;

import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.DuplicateCrawlTaskException;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 저장 전 검증기
 *
 * <p><strong>검증 항목</strong>:
 *
 * <ul>
 *   <li>스케줄러 존재 여부 및 ACTIVE 상태 검증
 *   <li>중복 Task 존재 여부 검증 (진행 중인 Task가 있으면 생성 불가)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskPersistenceValidator {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskPersistenceValidator.class);

    private final CrawlSchedulerReadManager crawlSchedulerReadManager;
    private final CrawlTaskReadManager crawlTaskReadManager;

    public CrawlTaskPersistenceValidator(
            CrawlSchedulerReadManager crawlSchedulerReadManager,
            CrawlTaskReadManager crawlTaskReadManager) {
        this.crawlSchedulerReadManager = crawlSchedulerReadManager;
        this.crawlTaskReadManager = crawlTaskReadManager;
    }

    /**
     * 스케줄러 조회 및 상태 검증
     *
     * <p>스케줄러가 존재하지 않으면 CrawlSchedulerNotFoundException, ACTIVE가 아니면
     * InvalidSchedulerStateException을 발생시킵니다.
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @return 검증된 CrawlScheduler
     */
    public CrawlScheduler findAndValidateScheduler(CrawlSchedulerId crawlSchedulerId) {
        CrawlScheduler scheduler = crawlSchedulerReadManager.getById(crawlSchedulerId);
        scheduler.validateActive();
        return scheduler;
    }

    /**
     * 중복 Task 검증 (CrawlTask 기반)
     *
     * <p>동일 스케줄러 내에서도 태스크 타입과 엔드포인트 조합이 다르면 별개의 태스크입니다. 예를 들어, 같은 스케줄러에서 SEARCH 태스크가 진행 중이더라도
     * DETAIL 태스크는 새로 생성 가능합니다.
     *
     * <p><strong>비종료 상태만 체크</strong>:
     *
     * <ul>
     *   <li>WAITING, PUBLISHED, RUNNING, RETRY, TIMEOUT → 비종료 상태이므로 중복 생성 방지
     *   <li>SUCCESS, FAILED → 종료 상태이므로 다음 주기에 새 태스크 생성 허용
     * </ul>
     *
     * @param crawlTask 검증할 CrawlTask
     * @throws DuplicateCrawlTaskException 진행 중인 동일 Task가 이미 존재하는 경우
     */
    public void validateNoDuplicateTask(CrawlTask crawlTask) {
        CrawlEndpoint endpoint = crawlTask.getEndpoint();

        boolean exists =
                crawlTaskReadManager.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                        crawlTask.getCrawlSchedulerId(),
                        crawlTask.getTaskType(),
                        endpoint.path(),
                        endpoint.toQueryParamsJson(),
                        CrawlTaskStatus.nonTerminalStatuses());

        if (exists) {
            log.error(
                    "중복 Task 감지! DuplicateCrawlTaskException 발생 예정 - "
                            + "schedulerId={}, sellerId={}, taskType={}, endpointPath={}, "
                            + "endpointQueryParams={}",
                    crawlTask.getCrawlSchedulerIdValue(),
                    crawlTask.getSellerIdValue(),
                    crawlTask.getTaskType(),
                    endpoint.path(),
                    endpoint.toQueryParamsJson());
            throw new DuplicateCrawlTaskException(
                    crawlTask.getSellerIdValue(), crawlTask.getTaskType());
        }

        log.debug(
                "중복 Task 검증 통과: schedulerId={}, sellerId={}, taskType={}, endpointPath={}",
                crawlTask.getCrawlSchedulerIdValue(),
                crawlTask.getSellerIdValue(),
                crawlTask.getTaskType(),
                endpoint.path());
    }
}
