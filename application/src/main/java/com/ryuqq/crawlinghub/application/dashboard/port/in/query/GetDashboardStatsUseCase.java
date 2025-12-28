package com.ryuqq.crawlinghub.application.dashboard.port.in.query;

import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse;

/**
 * Dashboard 통계 조회 UseCase
 *
 * <p>관리자 대시보드에서 필요한 전반적인 통계 정보를 조회합니다.
 *
 * <p><strong>조회 항목:</strong>
 *
 * <ul>
 *   <li>오늘 태스크 통계 (전체/성공/실패/진행중/대기중)
 *   <li>최근 7일 성공률 추이
 *   <li>스케줄 통계 (전체/활성/비활성)
 *   <li>Outbox 통계 (대기/처리중/실패/완료)
 *   <li>최근 실패 태스크 목록
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetDashboardStatsUseCase {

    /**
     * Dashboard 통계 조회
     *
     * @return Dashboard 통계 정보
     */
    DashboardStatsResponse execute();
}
