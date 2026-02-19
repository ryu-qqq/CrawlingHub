package com.ryuqq.crawlinghub.application.seller.facade;

import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SellerCommandFacade {

    private final SellerTransactionManager transactionManager;
    private final ApplicationEventPublisher eventPublisher;

    public SellerCommandFacade(
            SellerTransactionManager transactionManager, ApplicationEventPublisher eventPublisher) {
        this.transactionManager = transactionManager;
        this.eventPublisher = eventPublisher;
    }

    public Seller persist(Seller seller) {
        transactionManager.persist(seller);
        publishDomainEvents(seller);
        return seller;
    }

    /**
     * Seller 정보 수정 + 영속화 + 이벤트 발행
     *
     * <p>TransactionManager에 위임하여 TimeProvider 의존성 캡슐화
     *
     * @param seller 수정 대상 Seller
     * @param newMustItSellerName 새로운 머스트잇 셀러명
     * @param newSellerName 새로운 셀러명
     * @param newStatus 새로운 상태
     * @return 수정된 Seller
     */
    public Seller update(
            Seller seller,
            MustItSellerName newMustItSellerName,
            SellerName newSellerName,
            SellerStatus newStatus) {
        transactionManager.update(seller, newMustItSellerName, newSellerName, newStatus);
        publishDomainEvents(seller);
        return seller;
    }

    /**
     * Domain Event 발행
     *
     * <p>Seller 내부에 쌓인 Domain Event를 Spring ApplicationEventPublisher로 발행
     *
     * <p>예: SellerDeActiveEvent → SellerDeactivatedEventHandler가 스케줄러 중지 처리
     *
     * <p><strong>중요</strong>: Spring Event는 구체 타입으로 매칭되므로 DomainEvent가 아닌 실제 이벤트 객체를 발행해야 함
     *
     * @param seller Domain Event를 보유한 Seller Aggregate
     */
    private void publishDomainEvents(Seller seller) {
        seller.getDomainEvents().forEach(eventPublisher::publishEvent);
        seller.clearDomainEvents();
    }
}
