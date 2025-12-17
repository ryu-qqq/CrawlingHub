package com.ryuqq.crawlinghub.application.common.config;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Transaction Event Registry
 *
 * <p>트랜잭션 커밋 후 Event를 자동 발행합니다.
 *
 * <p><strong>특징</strong>:
 *
 * <ul>
 *   <li>ThreadLocal 대신 TransactionSynchronization 사용 (Virtual Thread 안전)
 *   <li>커밋 성공 시에만 Event 발행
 *   <li>롤백 시 Event 발행 안 함
 * </ul>
 *
 * <p><strong>사용 조건</strong>:
 *
 * <ul>
 *   <li>반드시 {@code @Transactional} 컨텍스트 내에서 호출
 *   <li>Facade에서 영속화 후 Event 등록
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class TransactionEventRegistry {

    private final ApplicationEventPublisher eventPublisher;

    public TransactionEventRegistry(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 단일 Event 등록 (커밋 후 발행)
     *
     * <p>트랜잭션 커밋 성공 시에만 Event가 발행됩니다. 롤백 시에는 발행되지 않습니다.
     *
     * @param event 발행할 Domain Event
     * @throws IllegalStateException 트랜잭션 컨텍스트가 없을 경우
     */
    public void registerForPublish(DomainEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Transaction synchronization is not active. "
                            + "registerForPublish() must be called within @Transactional context.");
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishEvent(event);
                    }
                });
    }

    /**
     * 여러 Event 등록 (커밋 후 발행)
     *
     * <p>각 Event별로 TransactionSynchronization이 등록됩니다.
     *
     * @param events 발행할 Domain Event 목록
     */
    public void registerAllForPublish(List<? extends DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        events.forEach(this::registerForPublish);
    }
}
