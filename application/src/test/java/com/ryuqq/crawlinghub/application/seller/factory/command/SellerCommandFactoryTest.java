package com.ryuqq.crawlinghub.application.seller.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerUpdateData;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@Tag("application")
@Tag("factory")
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerCommandFactory 단위 테스트")
class SellerCommandFactoryTest {

    @Mock private TimeProvider timeProvider;

    private SellerCommandFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SellerCommandFactory(timeProvider);
    }

    @Nested
    @DisplayName("create() 메서드는")
    class CreateMethod {

        @Test
        @DisplayName("RegisterSellerCommand로 신규 Seller를 생성한다")
        void shouldCreateNewSellerFromCommand() {
            // Given
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedInstant);

            RegisterSellerCommand command =
                    new RegisterSellerCommand("mustit-seller-123", "테스트 셀러");

            // When
            Seller seller = factory.create(command);

            // Then
            assertThat(seller.getMustItSellerNameValue()).isEqualTo("mustit-seller-123");
            assertThat(seller.getSellerNameValue()).isEqualTo("테스트 셀러");
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(seller.getProductCount()).isZero();
        }

        @Test
        @DisplayName("생성된 Seller는 ACTIVE 상태이다")
        void shouldCreateSellerWithActiveStatus() {
            // Given
            Instant fixedInstant = Instant.now();
            given(timeProvider.now()).willReturn(fixedInstant);

            RegisterSellerCommand command = new RegisterSellerCommand("mustit-001", "셀러명");

            // When
            Seller seller = factory.create(command);

            // Then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("createUpdateContext() 메서드는")
    class CreateUpdateContextMethod {

        @Test
        @DisplayName("active=true인 UpdateSellerCommand를 UpdateContext로 변환한다")
        void shouldConvertCommandWithActiveTrue() {
            // Given
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedInstant);

            UpdateSellerCommand command =
                    new UpdateSellerCommand(1L, "updated-mustit", "업데이트된 셀러", true);

            // When
            UpdateContext<SellerId, SellerUpdateData> context =
                    factory.createUpdateContext(command);

            // Then
            assertThat(context.id().value()).isEqualTo(1L);
            assertThat(context.updateData().mustItSellerName().value()).isEqualTo("updated-mustit");
            assertThat(context.updateData().sellerName().value()).isEqualTo("업데이트된 셀러");
            assertThat(context.updateData().status()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(context.changedAt()).isEqualTo(fixedInstant);
        }

        @Test
        @DisplayName("active=false이면 INACTIVE 상태로 설정한다")
        void shouldSetInactiveStatusWhenActiveFalse() {
            // Given
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedInstant);

            UpdateSellerCommand command =
                    new UpdateSellerCommand(1L, "mustit-seller", "셀러명", false);

            // When
            UpdateContext<SellerId, SellerUpdateData> context =
                    factory.createUpdateContext(command);

            // Then
            assertThat(context.updateData().status()).isEqualTo(SellerStatus.INACTIVE);
        }

        @Test
        @DisplayName("changedAt에 TimeProvider의 현재 시간이 설정된다")
        void shouldSetChangedAtFromTimeProvider() {
            // Given
            Instant fixedInstant = Instant.parse("2025-06-01T12:00:00Z");
            given(timeProvider.now()).willReturn(fixedInstant);

            UpdateSellerCommand command = new UpdateSellerCommand(1L, "mustit-seller", "셀러명", true);

            // When
            UpdateContext<SellerId, SellerUpdateData> context =
                    factory.createUpdateContext(command);

            // Then
            assertThat(context.changedAt()).isEqualTo(fixedInstant);
        }
    }

    @Nested
    @DisplayName("createProductCountUpdateContext() 메서드는")
    class CreateProductCountUpdateContextMethod {

        @Test
        @DisplayName("sellerId와 productCount로 UpdateContext를 생성한다")
        void shouldCreateProductCountUpdateContext() {
            // Given
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedInstant);

            Long sellerId = 1L;
            int productCount = 100;

            // When
            UpdateContext<SellerId, Integer> context =
                    factory.createProductCountUpdateContext(sellerId, productCount);

            // Then
            assertThat(context.id().value()).isEqualTo(1L);
            assertThat(context.updateData()).isEqualTo(100);
            assertThat(context.changedAt()).isEqualTo(fixedInstant);
        }

        @Test
        @DisplayName("상품 수 0도 정상적으로 컨텍스트가 생성된다")
        void shouldCreateContextWithZeroProductCount() {
            // Given
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedInstant);

            // When
            UpdateContext<SellerId, Integer> context =
                    factory.createProductCountUpdateContext(1L, 0);

            // Then
            assertThat(context.updateData()).isZero();
        }
    }
}
