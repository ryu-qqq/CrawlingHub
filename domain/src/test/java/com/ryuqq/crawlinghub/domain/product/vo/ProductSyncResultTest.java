package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("ProductSyncResult 단위 테스트")
class ProductSyncResultTest {

    @Nested
    @DisplayName("success() 팩토리 메서드 테스트")
    class SuccessTest {

        @Test
        @DisplayName("성공 결과를 생성한다")
        void createSuccessResult() {
            // when
            ProductSyncResult result = ProductSyncResult.success(12345L);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(12345L);
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("failure() 팩토리 메서드 테스트")
    class FailureTest {

        @Test
        @DisplayName("실패 결과를 생성한다")
        void createFailureResult() {
            // when
            ProductSyncResult result = ProductSyncResult.failure("ERR-001", "Product not found");

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.externalProductId()).isNull();
            assertThat(result.errorCode()).isEqualTo("ERR-001");
            assertThat(result.errorMessage()).isEqualTo("Product not found");
        }
    }

    @Nested
    @DisplayName("toErrorMessage() 메서드 테스트")
    class ToErrorMessageTest {

        @Test
        @DisplayName("실패 결과의 에러 메시지를 포맷한다")
        void formatErrorMessage() {
            // given
            ProductSyncResult result = ProductSyncResult.failure("ERR-001", "Product not found");

            // when
            String errorMessage = result.toErrorMessage();

            // then
            assertThat(errorMessage).contains("ERR-001");
            assertThat(errorMessage).contains("Product not found");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 필드이면 동일하다")
        void sameFieldsAreEqual() {
            // given
            ProductSyncResult result1 = ProductSyncResult.success(1L);
            ProductSyncResult result2 = ProductSyncResult.success(1L);

            // then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("다른 필드이면 다르다")
        void differentFieldsAreNotEqual() {
            // given
            ProductSyncResult success = ProductSyncResult.success(1L);
            ProductSyncResult failure = ProductSyncResult.failure("ERR", "error");

            // then
            assertThat(success).isNotEqualTo(failure);
        }
    }
}
