package com.ryuqq.crawlinghub.application.task.component;

import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.exception.InvalidSchedulerStateException;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.exception.DuplicateCrawlTaskException;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
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

    private static final List<CrawlTaskStatus> IN_PROGRESS_STATUSES =
            List.of(
                    CrawlTaskStatus.WAITING,
                    CrawlTaskStatus.PUBLISHED,
                    CrawlTaskStatus.RUNNING,
                    CrawlTaskStatus.RETRY);

    private final CrawlScheduleQueryPort crawlScheduleQueryPort;
    private final CrawlTaskQueryPort crawlTaskQueryPort;

    public CrawlTaskPersistenceValidator(
            CrawlScheduleQueryPort crawlScheduleQueryPort, CrawlTaskQueryPort crawlTaskQueryPort) {
        this.crawlScheduleQueryPort = crawlScheduleQueryPort;
        this.crawlTaskQueryPort = crawlTaskQueryPort;
    }

    /**
     * 스케줄러 조회 및 상태 검증
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @return 검증된 CrawlScheduler
     * @throws CrawlSchedulerNotFoundException 스케줄러가 존재하지 않는 경우
     * @throws InvalidSchedulerStateException 스케줄러가 ACTIVE 상태가 아닌 경우
     */
    public CrawlScheduler findAndValidateScheduler(CrawlSchedulerId crawlSchedulerId) {
        CrawlScheduler scheduler =
                crawlScheduleQueryPort
                        .findById(crawlSchedulerId)
                        .orElseThrow(
                                () ->
                                        new CrawlSchedulerNotFoundException(
                                                crawlSchedulerId.value()));

        if (scheduler.getStatus() != SchedulerStatus.ACTIVE) {
            throw new InvalidSchedulerStateException(scheduler.getStatus(), SchedulerStatus.ACTIVE);
        }

        return scheduler;
    }

    /**
     * 중복 Task 검증 (스케줄러 ID, 태스크 타입, 엔드포인트 조합)
     *
     * <p>동일 스케줄러 내에서도 태스크 타입과 엔드포인트 조합이 다르면 별개의 태스크입니다. 예를 들어, 같은 스케줄러에서 SEARCH 태스크가 진행 중이더라도
     * DETAIL 태스크는 새로 생성 가능합니다.
     *
     * <p><strong>IN_PROGRESS 상태만 체크</strong>:
     *
     * <ul>
     *   <li>WAITING, PUBLISHED, RUNNING, RETRY → 진행 중이므로 중복 생성 방지
     *   <li>SUCCESS, FAILED → 종료 상태이므로 다음 주기에 새 태스크 생성 허용
     * </ul>
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID (로깅용)
     * @param taskType 태스크 유형
     * @param endpointPath 엔드포인트 경로
     * @param endpointQueryParams 엔드포인트 쿼리 파라미터
     * @throws DuplicateCrawlTaskException 진행 중인 동일 Task가 이미 존재하는 경우
     */
    public void validateNoDuplicateTask(
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            CrawlTaskType taskType,
            String endpointPath,
            String endpointQueryParams) {
        boolean exists =
                crawlTaskQueryPort.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                        crawlSchedulerId,
                        taskType,
                        endpointPath,
                        endpointQueryParams,
                        IN_PROGRESS_STATUSES);

        if (exists) {
            log.error(
                    "중복 Task 감지! DuplicateCrawlTaskException 발생 예정 - "
                            + "schedulerId={}, sellerId={}, taskType={}, endpointPath={}, "
                            + "endpointQueryParams={}, checkStatuses={}",
                    crawlSchedulerId.value(),
                    sellerId.value(),
                    taskType,
                    endpointPath,
                    endpointQueryParams,
                    IN_PROGRESS_STATUSES);
            throw new DuplicateCrawlTaskException(sellerId.value(), taskType);
        }

        log.debug(
                "중복 Task 검증 통과: schedulerId={}, sellerId={}, taskType={}, endpointPath={}",
                crawlSchedulerId.value(),
                sellerId.value(),
                taskType,
                endpointPath);
    }
}
