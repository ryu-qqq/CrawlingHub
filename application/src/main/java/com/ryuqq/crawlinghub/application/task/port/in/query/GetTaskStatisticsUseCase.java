package com.ryuqq.crawlinghub.application.task.port.in.query;

import com.ryuqq.crawlinghub.application.task.dto.query.GetTaskStatisticsQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.TaskStatisticsResponse;

/**
 * Task 통계 조회 UseCase (Port In)
 *
 * <p>어드민 대시보드용 Task 통계 정보를 조회합니다.
 *
 * <p><strong>제공 정보:</strong>
 *
 * <ul>
 *   <li>상태별 Task 수 (SUCCESS, FAILED, RUNNING, WAITING 등)
 *   <li>성공률
 *   <li>상위 실패 원인 분석
 *   <li>태스크 유형별 통계
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetTaskStatisticsUseCase {

    /**
     * Task 통계 조회
     *
     * @param query 조회 조건 (기간, 스케줄러, 셀러 필터)
     * @return 통계 응답
     */
    TaskStatisticsResponse execute(GetTaskStatisticsQuery query);
}
