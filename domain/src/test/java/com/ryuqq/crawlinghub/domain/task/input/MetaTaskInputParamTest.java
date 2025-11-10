package com.ryuqq.crawlinghub.domain.task.input;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MetaTaskInputParam Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("MetaTaskInputParam Value Object 단위 테스트")
class MetaTaskInputParamTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("유효한 sellerId로 MetaTaskInputParam 생성 성공 (pageNo=0, pageSize=1, order=LATEST 자동 설정)")
        void shouldCreateWithValidSellerId() {
            // Given
            Long sellerId = 12345L;

            // When
            MetaTaskInputParam param = MetaTaskInputParam.of(sellerId);

            // Then
            assertThat(param).isNotNull();
            assertThat(param.getSellerId()).isEqualTo(12345L);
            assertThat(param.getPageNo()).isEqualTo(0); // META는 pageNo=0 고정
            assertThat(param.getPageSize()).isEqualTo(1); // META는 pageSize=1 고정
            assertThat(param.getOrder()).isEqualTo("LATEST");
        }

        @Test
        @DisplayName("MetaTaskInputParam은 TaskInputParam의 하위 타입이다")
        void shouldBeSubtypeOfTaskInputParam() {
            // Given
            Long sellerId = 12345L;

            // When
            MetaTaskInputParam param = MetaTaskInputParam.of(sellerId);

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
            assertThatThrownBy(() -> MetaTaskInputParam.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId는 필수이며 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        @DisplayName("sellerId가 0 이하면 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNotPositive(Long invalidSellerId) {
            // When & Then
            assertThatThrownBy(() -> MetaTaskInputParam.of(invalidSellerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId는 필수이며 양수여야 합니다");
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTests {

        @Test
        @DisplayName("META Task는 항상 pageNo=0이어야 한다 (총 상품 수 파악용)")
        void shouldAlwaysHavePageNoZero() {
            // Given
            Long sellerId = 12345L;

            // When
            MetaTaskInputParam param = MetaTaskInputParam.of(sellerId);

            // Then
            assertThat(param.getPageNo()).isEqualTo(0);
        }

        @Test
        @DisplayName("META Task는 항상 pageSize=1이어야 한다 (단일 페이지만 조회)")
        void shouldAlwaysHavePageSizeOne() {
            // Given
            Long sellerId = 12345L;

            // When
            MetaTaskInputParam param = MetaTaskInputParam.of(sellerId);

            // Then
            assertThat(param.getPageSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("META Task는 항상 order=LATEST여야 한다")
        void shouldAlwaysHaveOrderLatest() {
            // Given
            Long sellerId = 12345L;

            // When
            MetaTaskInputParam param = MetaTaskInputParam.of(sellerId);

            // Then
            assertThat(param.getOrder()).isEqualTo("LATEST");
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 sellerId를 가진 두 MetaTaskInputParam은 같다")
        void shouldBeEqualForSameSellerId() {
            // Given
            Long sellerId = 12345L;
            MetaTaskInputParam param1 = MetaTaskInputParam.of(sellerId);
            MetaTaskInputParam param2 = MetaTaskInputParam.of(sellerId);

            // When & Then
            assertThat(param1).isEqualTo(param2);
        }

        @Test
        @DisplayName("다른 sellerId를 가진 두 MetaTaskInputParam은 다르다")
        void shouldNotBeEqualForDifferentSellerId() {
            // Given
            MetaTaskInputParam param1 = MetaTaskInputParam.of(12345L);
            MetaTaskInputParam param2 = MetaTaskInputParam.of(67890L);

            // When & Then
            assertThat(param1).isNotEqualTo(param2);
        }

        @Test
        @DisplayName("같은 sellerId를 가진 두 MetaTaskInputParam은 같은 hashCode를 반환한다")
        void shouldReturnSameHashCodeForSameSellerId() {
            // Given
            Long sellerId = 12345L;
            MetaTaskInputParam param1 = MetaTaskInputParam.of(sellerId);
            MetaTaskInputParam param2 = MetaTaskInputParam.of(sellerId);

            // When & Then
            assertThat(param1.hashCode()).isEqualTo(param2.hashCode());
        }

        @Test
        @DisplayName("다른 sellerId를 가진 두 MetaTaskInputParam은 다른 hashCode를 반환한다")
        void shouldReturnDifferentHashCodeForDifferentSellerId() {
            // Given
            MetaTaskInputParam param1 = MetaTaskInputParam.of(12345L);
            MetaTaskInputParam param2 = MetaTaskInputParam.of(67890L);

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
            MetaTaskInputParam param = MetaTaskInputParam.of(sellerId);

            // When
            String result = param.toString();

            // Then
            assertThat(result).contains("sellerId=12345");
            assertThat(result).contains("pageNo=0");
            assertThat(result).contains("pageSize=1");
            assertThat(result).contains("order='LATEST'");
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("MetaTaskInputParam은 생성 후 상태를 변경할 수 없다")
        void shouldBeImmutable() {
            // Given
            Long sellerId = 12345L;
            MetaTaskInputParam param = MetaTaskInputParam.of(sellerId);

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
