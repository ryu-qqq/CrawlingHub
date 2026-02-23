package com.ryuqq.crawlinghub.application.seller.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.seller.factory.command.SellerCommandFactory;
import com.ryuqq.crawlinghub.application.seller.manager.SellerCommandManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UpdateSellerProductCountService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ReadManager/Factory/CommandManager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSellerProductCountService 테스트")
class UpdateSellerProductCountServiceTest {

    @Mock private SellerReadManager readManager;

    @Mock private SellerCommandFactory commandFactory;

    @Mock private SellerCommandManager commandManager;

    @InjectMocks private UpdateSellerProductCountService service;

    @Nested
    @DisplayName("execute() 상품 수 업데이트 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 존재하는 셀러의 상품 수 업데이트")
        void shouldUpdateProductCountWhenSellerExists() {
            // Given
            Long sellerId = 1L;
            int productCount = 100;
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            SellerId id = SellerId.of(sellerId);
            UpdateContext<SellerId, Integer> context =
                    new UpdateContext<>(id, productCount, fixedInstant);
            Seller seller = SellerFixture.anActiveSeller();

            given(commandFactory.createProductCountUpdateContext(sellerId, productCount))
                    .willReturn(context);
            given(readManager.findById(id)).willReturn(Optional.of(seller));

            // When
            service.execute(sellerId, productCount);

            // Then
            then(commandFactory).should().createProductCountUpdateContext(sellerId, productCount);
            then(readManager).should().findById(id);
            then(commandManager).should().persist(seller);
        }

        @Test
        @DisplayName("[성공] 상품 수가 0인 경우도 정상 업데이트")
        void shouldUpdateProductCountToZero() {
            // Given
            Long sellerId = 1L;
            int productCount = 0;
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            SellerId id = SellerId.of(sellerId);
            UpdateContext<SellerId, Integer> context =
                    new UpdateContext<>(id, productCount, fixedInstant);
            Seller seller = SellerFixture.anActiveSellerWithProducts(50);

            given(commandFactory.createProductCountUpdateContext(sellerId, productCount))
                    .willReturn(context);
            given(readManager.findById(id)).willReturn(Optional.of(seller));

            // When
            service.execute(sellerId, productCount);

            // Then
            then(readManager).should().findById(id);
            then(commandManager).should().persist(seller);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 셀러의 상품 수 업데이트 시 SellerNotFoundException 발생")
        void shouldThrowExceptionWhenSellerNotFound() {
            // Given
            Long sellerId = 999L;
            int productCount = 100;
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            SellerId id = SellerId.of(sellerId);
            UpdateContext<SellerId, Integer> context =
                    new UpdateContext<>(id, productCount, fixedInstant);

            given(commandFactory.createProductCountUpdateContext(sellerId, productCount))
                    .willReturn(context);
            given(readManager.findById(id)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(sellerId, productCount))
                    .isInstanceOf(SellerNotFoundException.class);

            then(commandManager).should(never()).persist(any());
        }
    }
}
