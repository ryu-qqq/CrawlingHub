package com.ryuqq.crawlinghub.application.schedule.port.in.command;

/**
 * 셀러별 스케줄러 비활성화 UseCase (Port In).
 *
 * <p><strong>사용 시나리오</strong>:
 *
 * <ul>
 *   <li>Seller 비활성화 시 해당 셀러의 모든 활성 스케줄러 비활성화
 *   <li>SellerDeactivatedEventHandler에서 호출
 * </ul>
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>셀러의 활성 스케줄러 조회
 *   <li>각 스케줄러 비활성화 (Domain 비즈니스 로직)
 *   <li>History + OutBox 저장 (AWS EventBridge 동기화)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface DeactivateSchedulersBySellerUseCase {

    /**
     * 셀러의 모든 활성 스케줄러 비활성화
     *
     * @param sellerId 셀러 ID
     * @return 비활성화된 스케줄러 수
     */
    int execute(Long sellerId);
}
