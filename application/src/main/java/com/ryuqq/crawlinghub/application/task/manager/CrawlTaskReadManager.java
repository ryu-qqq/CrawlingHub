package com.ryuqq.crawlinghub.application.task.manager;

import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 조회 전용 Manager
 *
 * <p><strong>책임</strong>: CrawlTask 조회 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 QueryPort만 의존, 트랜잭션 없음
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskReadManager {

    private final CrawlTaskQueryPort crawlTaskQueryPort;

    public CrawlTaskReadManager(CrawlTaskQueryPort crawlTaskQueryPort) {
        this.crawlTaskQueryPort = crawlTaskQueryPort;
    }

    /**
     * CrawlTask ID로 단건 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return CrawlTask (Optional)
     */
    public Optional<CrawlTask> findById(CrawlTaskId crawlTaskId) {
        return crawlTaskQueryPort.findById(crawlTaskId);
    }

    /**
     * Schedule ID와 상태 목록으로 존재 여부 확인
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param statuses 확인할 상태 목록
     * @return 존재 여부
     */
    public boolean existsByScheduleIdAndStatusIn(
            CrawlSchedulerId crawlSchedulerId, List<CrawlTaskStatus> statuses) {
        return crawlTaskQueryPort.existsByScheduleIdAndStatusIn(crawlSchedulerId, statuses);
    }

    /**
     * 조건으로 CrawlTask 목록 조회
     *
     * @param criteria 조회 조건
     * @return CrawlTask 목록
     */
    public List<CrawlTask> findByCriteria(CrawlTaskCriteria criteria) {
        return crawlTaskQueryPort.findByCriteria(criteria);
    }

    /**
     * 조건으로 CrawlTask 총 개수 조회
     *
     * @param criteria 조회 조건
     * @return 총 개수
     */
    public long countByCriteria(CrawlTaskCriteria criteria) {
        return crawlTaskQueryPort.countByCriteria(criteria);
    }

    /**
     * 통계 조건으로 상태별 개수 조회
     *
     * @param criteria 통계 조회 조건
     * @return 상태별 개수 맵
     */
    public Map<CrawlTaskStatus, Long> countByStatus(CrawlTaskStatisticsCriteria criteria) {
        return crawlTaskQueryPort.countByStatus(criteria);
    }

    /**
     * 통계 조건으로 태스크 유형별 통계 조회
     *
     * @param criteria 통계 조회 조건
     * @return 태스크 유형별 통계 맵
     */
    public Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> countByTaskType(
            CrawlTaskStatisticsCriteria criteria) {
        return crawlTaskQueryPort.countByTaskType(criteria);
    }

    /**
     * 셀러별 최근 태스크 조회
     *
     * @param sellerId 셀러 ID
     * @return 최근 태스크 (Optional)
     */
    public Optional<CrawlTask> findLatestBySellerId(SellerId sellerId) {
        return crawlTaskQueryPort.findLatestBySellerId(sellerId);
    }

    /**
     * 셀러별 최근 태스크 N개 조회
     *
     * @param sellerId 셀러 ID
     * @param limit 조회할 개수
     * @return 최근 태스크 리스트 (생성일시 내림차순)
     */
    public List<CrawlTask> findRecentBySellerId(SellerId sellerId, int limit) {
        return crawlTaskQueryPort.findRecentBySellerId(sellerId, limit);
    }

    /**
     * 스케줄러 ID, 태스크 타입, 엔드포인트 조합으로 존재 여부 확인
     *
     * <p>중복 Task 생성 방지를 위해 사용
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param taskType 태스크 유형
     * @param endpointPath 엔드포인트 경로
     * @param endpointQueryParams 엔드포인트 쿼리 파라미터 (JSON 문자열)
     * @param statuses 확인할 상태 목록
     * @return 존재 여부
     */
    public boolean existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
            CrawlSchedulerId crawlSchedulerId,
            CrawlTaskType taskType,
            String endpointPath,
            String endpointQueryParams,
            List<CrawlTaskStatus> statuses) {
        return crawlTaskQueryPort.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                crawlSchedulerId, taskType, endpointPath, endpointQueryParams, statuses);
    }

    /**
     * RUNNING 상태에서 일정 시간 이상 머물러 있는 CrawlTask 조회
     *
     * @param limit 조회할 최대 개수
     * @param timeoutSeconds RUNNING 상태 유지 시간 기준 (초)
     * @return 고아 CrawlTask 목록
     */
    public List<CrawlTask> findRunningOlderThan(int limit, long timeoutSeconds) {
        return crawlTaskQueryPort.findRunningOlderThan(limit, timeoutSeconds);
    }
}
