package com.ryuqq.crawlinghub.application.common.component;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Transaction Event Registry.
 *
 * <p>트랜잭션 커밋 후 Event를 자동 발행합니다.
 *
 * <p><strong>핵심 특징</strong>:
 *
 * <ul>
 *   <li>커밋 후 발행: 트랜잭션 성공 시에만 Event 발행
 *   <li>롤백 시 미발행: 트랜잭션 실패 시 Event 발행 안 함
 *   <li>Virtual Thread 안전: ThreadLocal 대신 TransactionSynchronization 사용
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class TransactionEventRegistry {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventRegistry.class);

    private final ApplicationEventPublisher eventPublisher;

    public TransactionEventRegistry(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 단일 Event를 커밋 후 발행하도록 등록합니다.
     *
     * <p>트랜잭션 컨텍스트가 없는 경우 즉시 발행합니다 (Fallback).
     *
     * @param event 발행할 Domain Event
     */
    public void registerForPublish(DomainEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.debug("트랜잭션 컨텍스트 없음, 즉시 Event 발행: eventType={}", event.getClass().getSimpleName());
            eventPublisher.publishEvent(event);
            return;
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
     * 여러 Event를 커밋 후 발행하도록 등록합니다.
     *
     * @param events 발행할 Domain Event 목록
     */
    public void registerAllForPublish(List<? extends DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        events.forEach(this::registerForPublish);
    }

    /**
     * Event를 즉시 발행합니다.
     *
     * @param event 발행할 Event (모든 타입)
     */
    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }

    /**
     * Object 타입 Event를 커밋 후 발행하도록 등록합니다.
     *
     * <p>트랜잭션 컨텍스트가 없는 경우 즉시 발행합니다 (Fallback).
     *
     * @param event 발행할 Event (모든 타입)
     */
    public void registerObjectForPublish(Object event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            eventPublisher.publishEvent(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishEvent(event);
                    }
                });
    }
}
