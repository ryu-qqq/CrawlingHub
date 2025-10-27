package com.ryuqq.crawlinghub.application.mustit.seller.event;

import com.ryuqq.crawlinghub.application.mustit.seller.service.SellerScheduleOrchestrationService;
import com.ryuqq.crawlinghub.domain.mustit.seller.event.SellerCrawlIntervalChangedEvent;
import com.ryuqq.orchestrator.core.model.OpId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 * 셀러 크롤링 스케줄 Event Handler
 * <p>
 * {@link SellerCrawlIntervalChangedEvent}를 수신하여
 * AWS EventBridge 스케줄 업데이트를 트리거합니다.
 * </p>
 * <p>
 * Event Listener 전략:
 * <ul>
 *   <li>@TransactionalEventListener(AFTER_COMMIT): 트랜잭션 커밋 후에만 실행</li>
 *   <li>이유: Seller 업데이트가 성공적으로 커밋된 후에만 외부 API 호출</li>
 *   <li>실패 시: Outbox에 PENDING으로 남아서 Finalizer가 재처리</li>
 * </ul>
 * </p>
 * <p>
 * Orchestrator 패턴:
 * <ul>
 *   <li>Event Handler는 Orchestrator.start()만 호출 (비동기 시작)</li>
 *   <li>실제 EventBridge API 호출은 Orchestrator Executor가 별도로 처리</li>
 *   <li>장애 시 Finalizer/Reaper가 자동 복구</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class SellerCrawlScheduleEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SellerCrawlScheduleEventHandler.class);

    private final SellerScheduleOrchestrationService orchestrationService;

    /**
     * 생성자
     *
     * @param orchestrationService Orchestration Service
     */
    public SellerCrawlScheduleEventHandler(
            SellerScheduleOrchestrationService orchestrationService
    ) {
        this.orchestrationService = Objects.requireNonNull(orchestrationService);
    }

    /**
     * 셀러 크롤링 주기 변경 Event 처리
     * <p>
     * 트랜잭션 커밋 후(AFTER_COMMIT)에만 실행되어,
     * Seller 업데이트가 성공한 경우에만 EventBridge 스케줄 업데이트를 시작합니다.
     * </p>
     * <p>
     * 처리 흐름:
     * <ol>
     *   <li>Event 수신 (Seller 트랜잭션 커밋 후)</li>
     *   <li>Event에서 sellerPk 직접 획득 (DB 조회 불필요)</li>
     *   <li>OrchestrationService.startScheduleUpdate() 호출</li>
     *   <li>Outbox PENDING 저장 + OpId 획득</li>
     *   <li>Orchestrator가 비동기로 EventBridge API 호출</li>
     * </ol>
     * </p>
     * <p>
     * 에러 처리:
     * <ul>
     *   <li>Event Handler에서 예외 발생 시 로그만 남기고 무시</li>
     *   <li>이유: Seller 업데이트는 이미 커밋되었으므로 롤백 불가</li>
     *   <li>Outbox PENDING 건은 Finalizer가 나중에 재처리</li>
     * </ul>
     * </p>
     * <p>
     * 리팩토링 이력:
     * <ul>
     *   <li>2025-01-27: Event에 sellerPk 추가로 DB 조회 불필요 (parseSellerPk 제거)</li>
     * </ul>
     * </p>
     *
     * @param event SellerCrawlIntervalChangedEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSellerCrawlIntervalChanged(SellerCrawlIntervalChangedEvent event) {
        try {
            log.info("Handling SellerCrawlIntervalChangedEvent: sellerId={}, sellerPk={}, oldCron={}, newCron={}",
                    event.getSellerId(),
                    event.getSellerPk(),
                    event.getOldInterval().getCronExpression(),
                    event.getNewInterval().getCronExpression()
            );

            // Event에서 sellerPk 직접 획득 (DB 조회 불필요)
            Long sellerPk = event.getSellerPk();

            // Orchestrator를 통해 스케줄 업데이트 시작
            OpId opId = orchestrationService.startScheduleUpdate(
                    sellerPk,
                    event.getNewInterval().getCronExpression()
            );

            log.info("Schedule update started: sellerId={}, sellerPk={}, opId={}",
                    event.getSellerId(),
                    sellerPk,
                    opId.getValue()
            );

        } catch (Exception e) {
            // Event Handler에서 예외 발생 시 로그만 남기고 무시
            // Seller 업데이트는 이미 커밋되었으므로 롤백 불가
            // Outbox PENDING 건은 Finalizer가 나중에 재처리
            log.error("Failed to handle SellerCrawlIntervalChangedEvent: sellerId={}, sellerPk={}",
                    event.getSellerId(),
                    event.getSellerPk(),
                    e
            );
        }
    }
}
