package com.ryuqq.crawlinghub.application.seller.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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

    @Mock private ClockHolder clockHolder;

    private SellerCommandFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SellerCommandFactory(clockHolder);
    }

    @Nested
    @DisplayName("create() 메서드는")
    class CreateMethod {

        @Test
        @DisplayName("RegisterSellerCommand로 신규 Seller를 생성한다")
        void shouldCreateNewSellerFromCommand() {
            // Given
            Clock fixedClock = Clock.fixed(Instant.parse("2024-01-15T10:00:00Z"), ZoneId.of("UTC"));
            given(clockHolder.getClock()).willReturn(fixedClock);

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
            Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
            given(clockHolder.getClock()).willReturn(fixedClock);

            RegisterSellerCommand command = new RegisterSellerCommand("mustit-001", "셀러명");

            // When
            Seller seller = factory.create(command);

            // Then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("createForComparison() 메서드는")
    class CreateForComparisonMethod {

        @Test
        @DisplayName("모든 필드가 있는 UpdateSellerCommand를 Seller로 변환한다")
        void shouldConvertCommandWithAllFields() {
            // Given
            UpdateSellerCommand command =
                    new UpdateSellerCommand(1L, "updated-mustit", "업데이트된 셀러", true);

            // When
            Seller seller = factory.createForComparison(command);

            // Then
            assertThat(seller.getSellerId().value()).isEqualTo(1L);
            assertThat(seller.getMustItSellerNameValue()).isEqualTo("updated-mustit");
            assertThat(seller.getSellerNameValue()).isEqualTo("업데이트된 셀러");
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("active가 false이면 INACTIVE 상태로 설정한다")
        void shouldSetInactiveStatusWhenActiveFalse() {
            // Given
            UpdateSellerCommand command =
                    new UpdateSellerCommand(1L, "mustit-seller", "셀러명", false);

            // When
            Seller seller = factory.createForComparison(command);

            // Then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        }

        @Test
        @DisplayName("mustItSellerName이 null이면 Seller에도 null로 설정한다")
        void shouldSetNullMustItSellerNameWhenCommandHasNull() {
            // Given
            UpdateSellerCommand command = new UpdateSellerCommand(1L, null, "셀러명", true);

            // When
            Seller seller = factory.createForComparison(command);

            // Then
            assertThat(seller.getMustItSellerName()).isNull();
        }

        @Test
        @DisplayName("sellerName이 null이면 Seller에도 null로 설정한다")
        void shouldSetNullSellerNameWhenCommandHasNull() {
            // Given
            UpdateSellerCommand command = new UpdateSellerCommand(1L, "mustit-seller", null, true);

            // When
            Seller seller = factory.createForComparison(command);

            // Then
            assertThat(seller.getSellerName()).isNull();
        }

        @Test
        @DisplayName("active가 null이면 status도 null로 설정한다")
        void shouldSetNullStatusWhenActiveIsNull() {
            // Given
            UpdateSellerCommand command = new UpdateSellerCommand(1L, "mustit-seller", "셀러명", null);

            // When
            Seller seller = factory.createForComparison(command);

            // Then
            assertThat(seller.getStatus()).isNull();
        }
    }
}
