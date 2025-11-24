package com.ryuqq.crawlinghub.application.seller.event;

import com.ryuqq.crawlinghub.application.schedule.port.in.command.DeactivateSchedulersBySellerUseCase;
import com.ryuqq.crawlinghub.application.seller.metrics.SellerEventMetrics;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeActiveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Seller 비활성화 이벤트 핸들러
 *
 * <p>Seller가 비활성화되면 해당 Seller의 모든 활성 Scheduler를 비활성화
 *
 * <h3>동기 처리 이유</h3>
 *
 * <ul>
 *   <li>비즈니스 정합성: Seller 비활성화 ↔ Scheduler 비활성화 강결합
 *   <li>안정성: Graceful Shutdown 시에도 완료 보장
 *   <li>단순성: 비동기 처리의 복잡도 불필요 (Scheduler 조회/수정 빠름)
 * </ul>
 *
 * <h3>@Async를 사용하지 않는 이유</h3>
 *
 * <ul>
 *   <li>서버 크래시 시 작업 유실 가능 (Outbox 없이는 복구 불가)
 *   <li>Graceful Shutdown 타임아웃(30초) 초과 시 강제 종료
 *   <li>Scheduler 업데이트는 충분히 빠름 (수십 ms)
 * </ul>
 *
 * <h3>메트릭 수집</h3>
 *
 * <ul>
 *   <li>SellerEventMetrics 위임: 처리 횟수, 시간, 비활성화된 Scheduler 수
 *   <li>메트릭 수집 로직 분리로 Handler는 비즈니스 로직에만 집중
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerDeactivatedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SellerDeactivatedEventHandler.class);

    private final DeactivateSchedulersBySellerUseCase deactivateSchedulersBySellerUseCase;
    private final SellerEventMetrics metrics;

    public SellerDeactivatedEventHandler(
            DeactivateSchedulersBySellerUseCase deactivateSchedulersBySellerUseCase,
            SellerEventMetrics metrics) {
        this.deactivateSchedulersBySellerUseCase = deactivateSchedulersBySellerUseCase;
        this.metrics = metrics;
    }

    /**
     * Seller 비활성화 이벤트 처리
     *
     * <p>트랜잭션 커밋 후 동기 실행 (AFTER_COMMIT)
     *
     * <p>Seller 저장이 확정된 후 Scheduler를 안전하게 비활성화
     *
     * <h3>실행 흐름</h3>
     *
     * <ol>
     *   <li>Seller 트랜잭션 COMMIT 완료 대기
     *   <li>활성 Scheduler 조회 (sellerId 기준)
     *   <li>각 Scheduler 비활성화 (별도 트랜잭션)
     *   <li>완료 후 UpdateSellerService 응답 반환
     * </ol>
     *
     * <h3>메트릭 수집</h3>
     *
     * <ul>
     *   <li>metrics.recordDeactivationEvent(): 횟수 + 시간 자동 측정
     *   <li>metrics.recordSchedulersDeactivated(): Seller별 비활성화 수 기록
     * </ul>
     *
     * @param event Seller 비활성화 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SellerDeActiveEvent event) {
        Long sellerId = event.getSellerIdValue();

        log.info("Handling SellerDeActiveEvent for sellerId={}", sellerId);

        // 메트릭 수집 + 비즈니스 로직 실행
        metrics.recordDeactivationEvent(
                () -> {
                    // UseCase를 통해 활성 스케줄러 비활성화
                    int deactivatedCount = deactivateSchedulersBySellerUseCase.execute(sellerId);

                    // Seller별 비활성화된 Scheduler 수 기록
                    metrics.recordSchedulersDeactivated(sellerId, deactivatedCount);

                    log.info(
                            "Deactivated {} schedulers for sellerId={}",
                            deactivatedCount,
                            sellerId);
                });
    }
}
