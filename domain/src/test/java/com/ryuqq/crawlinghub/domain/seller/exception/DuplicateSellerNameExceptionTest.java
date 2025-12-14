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
@DisplayName("DuplicateSellerNameException 단위 테스트")
class DuplicateSellerNameExceptionTest {

    @Nested
    @DisplayName("예외 생성")
    class ExceptionCreation {

        @Test
        @DisplayName("셀러 이름으로 예외 생성")
        void shouldCreateWithSellerName() {
            // Given
            String sellerName = "테스트셀러";

            // When
            DuplicateSellerNameException exception = new DuplicateSellerNameException(sellerName);

            // Then
            assertThat(exception.code()).isEqualTo("SELLER-002");
            assertThat(exception.getMessage()).contains("이미 존재하는");
            assertThat(exception.getMessage()).contains("테스트셀러");
            assertThat(exception.args()).containsEntry("sellerName", sellerName);
        }

        @Test
        @DisplayName("다른 셀러 이름으로 예외 생성")
        void shouldCreateWithDifferentSellerName() {
            // Given
            String sellerName = "AnotherSeller";

            // When
            DuplicateSellerNameException exception = new DuplicateSellerNameException(sellerName);

            // Then
            assertThat(exception.getMessage()).contains("AnotherSeller");
            assertThat(exception.args()).containsEntry("sellerName", sellerName);
        }
    }

    @Nested
    @DisplayName("DomainException 상속 검증")
    class DomainExceptionInheritance {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            // Given
            DuplicateSellerNameException exception = new DuplicateSellerNameException("test");

            // When & Then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("HTTP 상태 코드 409를 반환한다")
        void shouldReturn409HttpStatus() {
            // Given
            DuplicateSellerNameException exception = new DuplicateSellerNameException("test");

            // When & Then
            assertThat(exception.httpStatus()).isEqualTo(409);
        }
    }
}
