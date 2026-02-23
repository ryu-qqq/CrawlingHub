package com.ryuqq.crawlinghub.application.task.port.out.query;

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

/**
 * CrawlTask 조회 Port (Port Out - Query)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlTaskQueryPort {

    /**
     * 태스크 유형별 통계 DTO
     *
     * @param total 전체 개수
     * @param success 성공 개수
     * @param failed 실패 개수
     */
    record TaskTypeCount(long total, long success, long failed) {}

    /**
     * CrawlTask ID로 단건 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return CrawlTask (Optional)
     */
    Optional<CrawlTask> findById(CrawlTaskId crawlTaskId);

    /**
     * Schedule ID와 상태 목록으로 존재 여부 확인
     *
     * <p>중복 Task 생성 방지를 위해 사용
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param statuses 확인할 상태 목록
     * @return 존재 여부
     * @deprecated 태스크 유형과 엔드포인트까지 확인하는 {@link
     *     #existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn} 사용 권장
     */
    @Deprecated
    boolean existsByScheduleIdAndStatusIn(
            CrawlSchedulerId crawlSchedulerId, List<CrawlTaskStatus> statuses);

    /**
     * 스케줄러 ID, 태스크 타입, 엔드포인트 조합으로 존재 여부 확인
     *
     * <p>동일 스케줄러 내에서도 태스크 타입과 엔드포인트가 다르면 별개의 태스크입니다. 예: SEARCH 태스크가 진행 중이어도 DETAIL 태스크는 새로 생성 가능
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param taskType 태스크 유형
     * @param endpointPath 엔드포인트 경로
     * @param endpointQueryParams 엔드포인트 쿼리 파라미터 (JSON 문자열)
     * @param statuses 확인할 상태 목록
     * @return 존재 여부
     */
    boolean existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
            CrawlSchedulerId crawlSchedulerId,
            CrawlTaskType taskType,
            String endpointPath,
            String endpointQueryParams,
            List<CrawlTaskStatus> statuses);

    /**
     * 조건으로 CrawlTask 목록 조회
     *
     * @param criteria 조회 조건
     * @return CrawlTask 목록
     */
    List<CrawlTask> findByCriteria(CrawlTaskCriteria criteria);

    /**
     * 조건으로 CrawlTask 총 개수 조회
     *
     * @param criteria 조회 조건
     * @return 총 개수
     */
    long countByCriteria(CrawlTaskCriteria criteria);

    /**
     * 통계 조건으로 상태별 개수 조회
     *
     * @param criteria 통계 조회 조건
     * @return 상태별 개수 맵
     */
    Map<CrawlTaskStatus, Long> countByStatus(CrawlTaskStatisticsCriteria criteria);

    /**
     * 통계 조건으로 태스크 유형별 통계 조회
     *
     * @param criteria 통계 조회 조건
     * @return 태스크 유형별 통계 맵
     */
    Map<CrawlTaskType, TaskTypeCount> countByTaskType(CrawlTaskStatisticsCriteria criteria);

    /**
     * 셀러별 최근 태스크 조회
     *
     * <p>해당 셀러의 스케줄러에 속한 태스크 중 가장 최근 것을 조회
     *
     * @param sellerId 셀러 ID
     * @return 최근 태스크 (Optional)
     */
    Optional<CrawlTask> findLatestBySellerId(SellerId sellerId);

    /**
     * 셀러별 최근 태스크 N개 조회
     *
     * <p>셀러 상세 조회 시 최근 태스크 목록을 표시하기 위해 사용
     *
     * @param sellerId 셀러 ID
     * @param limit 조회할 개수
     * @return 최근 태스크 리스트 (생성일시 내림차순)
     */
    List<CrawlTask> findRecentBySellerId(SellerId sellerId, int limit);

    /**
     * RUNNING 상태에서 일정 시간 이상 머물러 있는 CrawlTask 조회
     *
     * <p>프로세스 크래시 등으로 RUNNING 상태에서 멈춰있는 고아 태스크를 찾기 위해 사용
     *
     * @param limit 조회할 최대 개수
     * @param timeoutSeconds RUNNING 상태 유지 시간 기준 (초)
     * @return 고아 CrawlTask 목록
     */
    List<CrawlTask> findRunningOlderThan(int limit, long timeoutSeconds);
}
