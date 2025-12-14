package com.ryuqq.crawlinghub.domain.seller.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("SellerNotFoundException 단위 테스트")
class SellerNotFoundExceptionTest {

    @Nested
    @DisplayName("예외 생성")
    class ExceptionCreation {

        @Test
        @DisplayName("셀러 ID로 예외 생성")
        void shouldCreateWithSellerId() {
            // Given
            long sellerId = 12345L;

            // When
            SellerNotFoundException exception = new SellerNotFoundException(sellerId);

            // Then
            assertThat(exception.code()).isEqualTo("SELLER-004");
            assertThat(exception.getMessage()).contains("존재하지 않는");
            assertThat(exception.getMessage()).contains("12345");
            assertThat(exception.args()).containsEntry("sellerId", sellerId);
        }

        @Test
        @DisplayName("다른 셀러 ID로 예외 생성")
        void shouldCreateWithDifferentSellerId() {
            // Given
            long sellerId = 99999L;

            // When
            SellerNotFoundException exception = new SellerNotFoundException(sellerId);

            // Then
            assertThat(exception.getMessage()).contains("99999");
            assertThat(exception.args()).containsEntry("sellerId", sellerId);
        }
    }

    @Nested
    @DisplayName("DomainException 상속 검증")
    class DomainExceptionInheritance {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(1L);

            // When & Then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("HTTP 상태 코드 404를 반환한다")
        void shouldReturn404HttpStatus() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(1L);

            // When & Then
            assertThat(exception.httpStatus()).isEqualTo(404);
        }
    }
}
