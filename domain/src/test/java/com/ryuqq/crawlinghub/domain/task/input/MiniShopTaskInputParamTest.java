package com.ryuqq.crawlinghub.domain.task.input;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MiniShopTaskInputParam Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("MiniShopTaskInputParam Value Object 단위 테스트")
class MiniShopTaskInputParamTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("유효한 sellerId와 pageNo로 MiniShopTaskInputParam 생성 성공 (pageSize=500, order=LATEST 자동 설정)")
        void shouldCreateWithValidSellerIdAndPageNo() {
            // Given
            Long sellerId = 12345L;
            int pageNo = 0;

            // When
            MiniShopTaskInputParam param = MiniShopTaskInputParam.of(sellerId, pageNo);

            // Then
            assertThat(param).isNotNull();
            assertThat(param.getSellerId()).isEqualTo(12345L);
            assertThat(param.getPageNo()).isEqualTo(0);
            assertThat(param.getPageSize()).isEqualTo(500); // MINI_SHOP은 pageSize=500 고정
            assertThat(param.getOrder()).isEqualTo("LATEST");
        }

        @Test
        @DisplayName("여러 페이지를 위한 MiniShopTaskInputParam 생성 (pageNo=5)")
        void shouldCreateWithMultiplePages() {
            // Given
            Long sellerId = 12345L;
            int pageNo = 5;

            // When
            MiniShopTaskInputParam param = MiniShopTaskInputParam.of(sellerId, pageNo);

            // Then
            assertThat(param.getSellerId()).isEqualTo(12345L);
            assertThat(param.getPageNo()).isEqualTo(5);
            assertThat(param.getPageSize()).isEqualTo(500);
        }

        @Test
        @DisplayName("MiniShopTaskInputParam은 TaskInputParam의 하위 타입이다")
        void shouldBeSubtypeOfTaskInputParam() {
            // Given
            Long sellerId = 12345L;
            int pageNo = 0;

            // When
            MiniShopTaskInputParam param = MiniShopTaskInputParam.of(sellerId, pageNo);

            // Then
            assertThat(param).isInstanceOf(TaskInputParam.class);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("sellerId가 null이면 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> MiniShopTaskInputParam.of(null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId는 필수이며 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        @DisplayName("sellerId가 0 이하면 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNotPositive(Long invalidSellerId) {
            // When & Then
            assertThatThrownBy(() -> MiniShopTaskInputParam.of(invalidSellerId, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId는 필수이며 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100})
        @DisplayName("pageNo가 음수면 예외 발생")
        void shouldThrowExceptionWhenPageNoIsNegative(int invalidPageNo) {
            // When & Then
            assertThatThrownBy(() -> MiniShopTaskInputParam.of(12345L, invalidPageNo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pageNo는 0 이상이어야 합니다");
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTests {

        @Test
        @DisplayName("MINI_SHOP Task는 항상 pageSize=500이어야 한다 (실제 크롤링용)")
        void shouldAlwaysHavePageSize500() {
            // Given
            Long sellerId = 12345L;
            int pageNo = 3;

            // When
            MiniShopTaskInputParam param = MiniShopTaskInputParam.of(sellerId, pageNo);

            // Then
            assertThat(param.getPageSize()).isEqualTo(500);
        }

        @Test
        @DisplayName("MINI_SHOP Task는 항상 order=LATEST여야 한다")
        void shouldAlwaysHaveOrderLatest() {
            // Given
            Long sellerId = 12345L;
            int pageNo = 0;

            // When
            MiniShopTaskInputParam param = MiniShopTaskInputParam.of(sellerId, pageNo);

            // Then
            assertThat(param.getOrder()).isEqualTo("LATEST");
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 sellerId와 pageNo를 가진 두 MiniShopTaskInputParam은 같다")
        void shouldBeEqualForSameSellerIdAndPageNo() {
            // Given
            Long sellerId = 12345L;
            int pageNo = 3;
            MiniShopTaskInputParam param1 = MiniShopTaskInputParam.of(sellerId, pageNo);
            MiniShopTaskInputParam param2 = MiniShopTaskInputParam.of(sellerId, pageNo);

            // When & Then
            assertThat(param1).isEqualTo(param2);
        }

        @Test
        @DisplayName("다른 sellerId를 가진 두 MiniShopTaskInputParam은 다르다")
        void shouldNotBeEqualForDifferentSellerId() {
            // Given
            MiniShopTaskInputParam param1 = MiniShopTaskInputParam.of(12345L, 0);
            MiniShopTaskInputParam param2 = MiniShopTaskInputParam.of(67890L, 0);

            // When & Then
            assertThat(param1).isNotEqualTo(param2);
        }

        @Test
        @DisplayName("다른 pageNo를 가진 두 MiniShopTaskInputParam은 다르다")
        void shouldNotBeEqualForDifferentPageNo() {
            // Given
            Long sellerId = 12345L;
            MiniShopTaskInputParam param1 = MiniShopTaskInputParam.of(sellerId, 0);
            MiniShopTaskInputParam param2 = MiniShopTaskInputParam.of(sellerId, 5);

            // When & Then
            assertThat(param1).isNotEqualTo(param2);
        }

        @Test
        @DisplayName("같은 sellerId와 pageNo를 가진 두 MiniShopTaskInputParam은 같은 hashCode를 반환한다")
        void shouldReturnSameHashCodeForSameSellerIdAndPageNo() {
            // Given
            Long sellerId = 12345L;
            int pageNo = 3;
            MiniShopTaskInputParam param1 = MiniShopTaskInputParam.of(sellerId, pageNo);
            MiniShopTaskInputParam param2 = MiniShopTaskInputParam.of(sellerId, pageNo);

            // When & Then
            assertThat(param1.hashCode()).isEqualTo(param2.hashCode());
        }

        @Test
        @DisplayName("다른 파라미터를 가진 두 MiniShopTaskInputParam은 다른 hashCode를 반환한다")
        void shouldReturnDifferentHashCodeForDifferentParams() {
            // Given
            MiniShopTaskInputParam param1 = MiniShopTaskInputParam.of(12345L, 0);
            MiniShopTaskInputParam param2 = MiniShopTaskInputParam.of(67890L, 5);

            // When & Then
            assertThat(param1.hashCode()).isNotEqualTo(param2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 모든 필드를 포함한다")
        void shouldIncludeAllFieldsInToString() {
            // Given
            Long sellerId = 12345L;
            int pageNo = 3;
            MiniShopTaskInputParam param = MiniShopTaskInputParam.of(sellerId, pageNo);

            // When
            String result = param.toString();

            // Then
            assertThat(result).contains("sellerId=12345");
            assertThat(result).contains("pageNo=3");
            assertThat(result).contains("pageSize=500");
            assertThat(result).contains("order='LATEST'");
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("MiniShopTaskInputParam은 생성 후 상태를 변경할 수 없다")
        void shouldBeImmutable() {
            // Given
            Long sellerId = 12345L;
            int pageNo = 3;
            MiniShopTaskInputParam param = MiniShopTaskInputParam.of(sellerId, pageNo);

            // When
            Long retrievedSellerId = param.getSellerId();
            int retrievedPageNo = param.getPageNo();
            int retrievedPageSize = param.getPageSize();
            String retrievedOrder = param.getOrder();

            // Then: 여러 번 호출해도 같은 값
            assertThat(param.getSellerId()).isEqualTo(retrievedSellerId);
            assertThat(param.getPageNo()).isEqualTo(retrievedPageNo);
            assertThat(param.getPageSize()).isEqualTo(retrievedPageSize);
            assertThat(param.getOrder()).isEqualTo(retrievedOrder);
        }
    }
}
