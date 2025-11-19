package com.ryuqq.crawlinghub.application.seller.event;

import com.ryuqq.crawlinghub.application.seller.port.out.command.SchedulerCommandPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeactivatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Seller 비활성화 이벤트 핸들러.
 *
 * <p>Seller가 비활성화되면 해당 Seller의 모든 활성 스케줄러를 비활성화합니다.</p>
 */
@Component
public class SellerDeactivatedEventHandler {

    private final SchedulerQueryPort schedulerQueryPort;
    private final SchedulerCommandPort schedulerCommandPort;

    public SellerDeactivatedEventHandler(
        SchedulerQueryPort schedulerQueryPort,
        SchedulerCommandPort schedulerCommandPort
    ) {
        this.schedulerQueryPort = schedulerQueryPort;
        this.schedulerCommandPort = schedulerCommandPort;
    }

    /**
     * Seller 비활성화 이벤트를 처리합니다.
     *
     * <p>트랜잭션 커밋 후에 실행되어, Seller 비활성화가 확정된 후에 스케줄러를 비활성화합니다.</p>
     *
     * @param event Seller 비활성화 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SellerDeactivatedEvent event) {
        Long sellerId = event.sellerId().value();
        if (sellerId == null) {
            return;
        }

        schedulerQueryPort.findActiveSchedulerIdsBySellerId(sellerId)
            .forEach(schedulerCommandPort::deactivateScheduler);
    }
}

