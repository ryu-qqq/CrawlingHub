package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("ShippingInfo Value Object 단위 테스트")
class ShippingInfoTest {

    @Nested
    @DisplayName("생성 검증 테스트")
    class CreationValidationTest {

        @Test
        @DisplayName("shippingFee가 음수이면 예외가 발생한다")
        void negativeShippingFeeThrowsException() {
            assertThatThrownBy(() -> new ShippingInfo("DOMESTIC", -1, "PAID", 3, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("shippingFee");
        }

        @Test
        @DisplayName("shippingType이 null이면 DOMESTIC으로 기본값이 설정된다")
        void nullShippingTypeDefaultsToDomestic() {
            ShippingInfo info = new ShippingInfo(null, 0, "FREE", 3, true);
            assertThat(info.shippingType()).isEqualTo("DOMESTIC");
        }

        @Test
        @DisplayName("shippingFeeType이 null이면 PAID로 기본값이 설정된다")
        void nullShippingFeeTypeDefaultsToPaid() {
            ShippingInfo info = new ShippingInfo("DOMESTIC", 3000, null, 3, false);
            assertThat(info.shippingFeeType()).isEqualTo("PAID");
        }

        @Test
        @DisplayName("averageDeliveryDays가 음수이면 0으로 정규화된다")
        void negativeDeliveryDaysNormalizedToZero() {
            ShippingInfo info = new ShippingInfo("DOMESTIC", 3000, "PAID", -1, false);
            assertThat(info.averageDeliveryDays()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("freeShipping() 팩토리 메서드 테스트")
    class FreeShippingFactoryTest {

        @Test
        @DisplayName("무료배송 ShippingInfo를 생성한다")
        void createFreeShipping() {
            ShippingInfo info = ShippingInfo.freeShipping("DOMESTIC", 3);

            assertThat(info.shippingType()).isEqualTo("DOMESTIC");
            assertThat(info.shippingFee()).isEqualTo(0);
            assertThat(info.shippingFeeType()).isEqualTo("FREE");
            assertThat(info.averageDeliveryDays()).isEqualTo(3);
            assertThat(info.freeShipping()).isTrue();
        }
    }

    @Nested
    @DisplayName("paidShipping() 팩토리 메서드 테스트")
    class PaidShippingFactoryTest {

        @Test
        @DisplayName("유료배송 ShippingInfo를 생성한다")
        void createPaidShipping() {
            ShippingInfo info = ShippingInfo.paidShipping("DOMESTIC", 3000, 5);

            assertThat(info.shippingType()).isEqualTo("DOMESTIC");
            assertThat(info.shippingFee()).isEqualTo(3000);
            assertThat(info.shippingFeeType()).isEqualTo("PAID");
            assertThat(info.averageDeliveryDays()).isEqualTo(5);
            assertThat(info.freeShipping()).isFalse();
        }
    }

    @Nested
    @DisplayName("fromShippingModule() 팩토리 메서드 테스트")
    class FromShippingModuleTest {

        @Test
        @DisplayName("배송 모듈 데이터로 ShippingInfo를 생성한다")
        void createFromShippingModule() {
            ShippingInfo info =
                    ShippingInfo.fromShippingModule("DOMESTIC", 0, "FREE", "평균 배송 3일 소요");

            assertThat(info.shippingType()).isEqualTo("DOMESTIC");
            assertThat(info.shippingFee()).isEqualTo(0);
            assertThat(info.shippingFeeType()).isEqualTo("FREE");
            assertThat(info.averageDeliveryDays()).isEqualTo(3);
            assertThat(info.freeShipping()).isTrue();
        }

        @Test
        @DisplayName("배송일 텍스트가 null이면 0일로 파싱한다")
        void nullDeliveryDaysTextParsesToZero() {
            ShippingInfo info = ShippingInfo.fromShippingModule("DOMESTIC", 3000, "PAID", null);

            assertThat(info.averageDeliveryDays()).isEqualTo(0);
        }

        @Test
        @DisplayName("배송비가 0이면 무료배송으로 판단한다")
        void zeroFeeIsFreeShipping() {
            ShippingInfo info = ShippingInfo.fromShippingModule("DOMESTIC", 0, "PAID", null);

            assertThat(info.freeShipping()).isTrue();
        }
    }

    @Nested
    @DisplayName("isDomestic() / isInternational() 테스트")
    class ShippingTypeCheckTest {

        @Test
        @DisplayName("DOMESTIC 배송 타입이면 isDomestic()이 true이다")
        void domesticShippingTypeIsDomestic() {
            ShippingInfo info = ShippingInfo.freeShipping("DOMESTIC", 3);
            assertThat(info.isDomestic()).isTrue();
            assertThat(info.isInternational()).isFalse();
        }

        @Test
        @DisplayName("INTERNATIONAL 배송 타입이면 isInternational()이 true이다")
        void internationalShippingTypeIsInternational() {
            ShippingInfo info = ShippingInfo.paidShipping("INTERNATIONAL", 5000, 14);
            assertThat(info.isInternational()).isTrue();
            assertThat(info.isDomestic()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasChanges() 테스트")
    class HasChangesTest {

        @Test
        @DisplayName("같은 값이면 변경이 없다")
        void sameValuesHaveNoChanges() {
            ShippingInfo info1 = ShippingInfo.freeShipping("DOMESTIC", 3);
            ShippingInfo info2 = ShippingInfo.freeShipping("DOMESTIC", 3);

            assertThat(info1.hasChanges(info2)).isFalse();
        }

        @Test
        @DisplayName("배송비가 다르면 변경이 있다")
        void differentShippingFeeHasChanges() {
            ShippingInfo info1 = ShippingInfo.paidShipping("DOMESTIC", 3000, 3);
            ShippingInfo info2 = ShippingInfo.paidShipping("DOMESTIC", 5000, 3);

            assertThat(info1.hasChanges(info2)).isTrue();
        }

        @Test
        @DisplayName("배송 타입이 다르면 변경이 있다")
        void differentShippingTypeHasChanges() {
            ShippingInfo info1 = ShippingInfo.paidShipping("DOMESTIC", 3000, 3);
            ShippingInfo info2 = ShippingInfo.paidShipping("INTERNATIONAL", 3000, 3);

            assertThat(info1.hasChanges(info2)).isTrue();
        }

        @Test
        @DisplayName("null이면 변경이 있다")
        void nullHasChanges() {
            ShippingInfo info = ShippingInfo.freeShipping("DOMESTIC", 3);
            assertThat(info.hasChanges(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 필드이면 동일하다")
        void sameFieldsAreEqual() {
            ShippingInfo info1 = ShippingInfo.freeShipping("DOMESTIC", 3);
            ShippingInfo info2 = ShippingInfo.freeShipping("DOMESTIC", 3);

            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("다른 필드이면 다르다")
        void differentFieldsAreNotEqual() {
            ShippingInfo info1 = ShippingInfo.freeShipping("DOMESTIC", 3);
            ShippingInfo info2 = ShippingInfo.paidShipping("DOMESTIC", 3000, 5);

            assertThat(info1).isNotEqualTo(info2);
        }
    }
}
