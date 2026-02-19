package com.ryuqq.crawlinghub.application.seller.event;

import com.ryuqq.crawlinghub.application.schedule.port.in.command.DeactivateSchedulersBySellerUseCase;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeActiveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerDeactivatedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SellerDeactivatedEventHandler.class);

    private final DeactivateSchedulersBySellerUseCase deactivateSchedulersBySellerUseCase;

    public SellerDeactivatedEventHandler(
            DeactivateSchedulersBySellerUseCase deactivateSchedulersBySellerUseCase) {
        this.deactivateSchedulersBySellerUseCase = deactivateSchedulersBySellerUseCase;
    }

    /**
     * Seller 비활성화 이벤트 처리
     *
     * @param event Seller 비활성화 이벤트
     */
    @EventListener
    public void handle(SellerDeActiveEvent event) {
        Long sellerId = event.getSellerIdValue();

        log.info("Handling SellerDeActiveEvent for sellerId={}", sellerId);

        int deactivatedCount = deactivateSchedulersBySellerUseCase.execute(sellerId);

        log.info("Deactivated {} schedulers for sellerId={}", deactivatedCount, sellerId);
    }
}
