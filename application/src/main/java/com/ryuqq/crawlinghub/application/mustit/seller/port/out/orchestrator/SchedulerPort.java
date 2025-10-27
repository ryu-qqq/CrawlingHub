package com.ryuqq.crawlinghub.application.mustit.seller.port.out.orchestrator;

import com.ryuqq.orchestrator.core.model.OpId;

/**
 * 스케줄러 Port (Outbound Port)
 * <p>
 * 헥사고날 아키텍처의 Outbound Port로서,
 * Application Layer가 특정 스케줄러 기술(AWS EventBridge)에 의존하지 않도록
 * 추상화합니다.
 * </p>
 * <p>
 * 이 인터페이스는 Application Layer에 정의되며,
 * Adapter-AWS-EventBridge Layer에서 구현됩니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>스케줄 생성 요청</li>
 *   <li>스케줄 수정 요청</li>
 *   <li>스케줄 삭제 요청</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public interface SchedulerPort {

    /**
     * 셀러 크롤링 스케줄 생성 요청
     * <p>
     * Orchestrator를 통해 비동기로 AWS EventBridge Schedule Rule을 생성합니다.
     * </p>
     *
     * @param sellerId       셀러 PK (Long FK)
     * @param cronExpression Cron 표현식
     * @return Orchestrator OpId (작업 추적용)
     */
    OpId createSchedule(Long sellerId, String cronExpression);

    /**
     * 셀러 크롤링 스케줄 수정 요청
     * <p>
     * Orchestrator를 통해 비동기로 AWS EventBridge Schedule Rule을 업데이트합니다.
     * </p>
     *
     * @param sellerId       셀러 PK (Long FK)
     * @param cronExpression 새로운 Cron 표현식
     * @return Orchestrator OpId (작업 추적용)
     */
    OpId updateSchedule(Long sellerId, String cronExpression);

    /**
     * 셀러 크롤링 스케줄 삭제 요청
     * <p>
     * Orchestrator를 통해 비동기로 AWS EventBridge Schedule Rule을 삭제합니다.
     * </p>
     *
     * @param sellerId 셀러 PK (Long FK)
     * @return Orchestrator OpId (작업 추적용)
     */
    OpId deleteSchedule(Long sellerId);
}
