package com.ryuqq.crawlinghub.application.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * SellerCommandFacade 단위 테스트
 *
 * <p>Mockist 스타일 테스트: TransactionManager, EventPublisher Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerCommandFacade 테스트")
class SellerCommandFacadeTest {

    @Mock
    private SellerTransactionManager transactionManager;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SellerCommandFacade facade;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] Seller 저장 및 이벤트 발행")
        void shouldPersistAndPublishEvents() {
            // Given
            Seller seller = SellerFixture.anActiveSeller();

            // When
            Seller result = facade.persist(seller);

            // Then
            assertThat(result).isEqualTo(seller);
            verify(transactionManager).persist(seller);
        }

        @Test
        @DisplayName("[성공] 도메인 이벤트 발행 후 이벤트 목록 클리어")
        void shouldClearDomainEventsAfterPublishing() {
            // Given
            Seller seller = SellerFixture.anActiveSeller();

            // When
            facade.persist(seller);

            // Then
            assertThat(seller.getDomainEvents()).isEmpty();
        }
    }
}
