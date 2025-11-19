package com.ryuqq.crawlinghub.application.seller.event;

import com.ryuqq.crawlinghub.application.seller.port.out.command.SchedulerCommandPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeactivatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerDeactivatedEventHandler")
class SellerDeactivatedEventHandlerTest {

    @Mock
    private SchedulerQueryPort schedulerQueryPort;

    @Mock
    private SchedulerCommandPort schedulerCommandPort;

    @InjectMocks
    private SellerDeactivatedEventHandler handler;

    @Test
    @DisplayName("이벤트를 처리하고 활성 스케줄러를 비활성화한다")
    void shouldHandleEventAndDeactivateSchedulers() {
        Seller seller = SellerFixture.anInactiveSeller();
        SellerDeactivatedEvent event = new SellerDeactivatedEvent(
            seller.getSellerId(),
            LocalDateTime.now()
        );
        List<Long> activeSchedulerIds = List.of(1L, 2L, 3L);

        given(schedulerQueryPort.findActiveSchedulerIdsBySellerId(seller.getSellerId().value()))
            .willReturn(activeSchedulerIds);

        handler.handle(event);

        then(schedulerCommandPort).should(times(3))
            .deactivateScheduler(any(Long.class));
        then(schedulerCommandPort).should().deactivateScheduler(1L);
        then(schedulerCommandPort).should().deactivateScheduler(2L);
        then(schedulerCommandPort).should().deactivateScheduler(3L);
    }

    @Test
    @DisplayName("활성 스케줄러가 없으면 비활성화 처리를 하지 않는다")
    void shouldNotDeactivateWhenNoActiveSchedulers() {
        Seller seller = SellerFixture.anInactiveSeller();
        SellerDeactivatedEvent event = new SellerDeactivatedEvent(
            seller.getSellerId(),
            LocalDateTime.now()
        );

        given(schedulerQueryPort.findActiveSchedulerIdsBySellerId(seller.getSellerId().value()))
            .willReturn(List.of());

        handler.handle(event);

        then(schedulerCommandPort).should(never())
            .deactivateScheduler(any(Long.class));
    }

    @Test
    @DisplayName("트랜잭션 커밋 후에 처리된다")
    void shouldProcessAfterTransactionCommit() throws NoSuchMethodException {
        Method handleMethod = SellerDeactivatedEventHandler.class.getMethod("handle", SellerDeactivatedEvent.class);
        TransactionalEventListener annotation = handleMethod.getAnnotation(TransactionalEventListener.class);

        assertThat(annotation)
            .as("handle 메서드는 @TransactionalEventListener를 가져야 합니다")
            .isNotNull();
        assertThat(annotation.phase())
            .as("트랜잭션 커밋 후에 실행되어야 합니다")
            .isEqualTo(TransactionPhase.AFTER_COMMIT);
    }
}

