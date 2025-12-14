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
@DisplayName("DuplicateMustItSellerIdException 단위 테스트")
class DuplicateMustItSellerIdExceptionTest {

    @Nested
    @DisplayName("예외 생성")
    class ExceptionCreation {

        @Test
        @DisplayName("머스트잇 셀러 이름으로 예외 생성")
        void shouldCreateWithMustItSellerName() {
            // Given
            String mustItSellerName = "mustit-seller-123";

            // When
            DuplicateMustItSellerIdException exception =
                    new DuplicateMustItSellerIdException(mustItSellerName);

            // Then
            assertThat(exception.code()).isEqualTo("SELLER-001");
            assertThat(exception.getMessage()).contains("이미 존재하는");
            assertThat(exception.getMessage()).contains("머스트잇");
            assertThat(exception.getMessage()).contains("mustit-seller-123");
            assertThat(exception.args()).containsEntry("mustItSellerName", mustItSellerName);
        }

        @Test
        @DisplayName("다른 머스트잇 셀러 이름으로 예외 생성")
        void shouldCreateWithDifferentMustItSellerName() {
            // Given
            String mustItSellerName = "another-mustit-seller";

            // When
            DuplicateMustItSellerIdException exception =
                    new DuplicateMustItSellerIdException(mustItSellerName);

            // Then
            assertThat(exception.getMessage()).contains("another-mustit-seller");
            assertThat(exception.args()).containsEntry("mustItSellerName", mustItSellerName);
        }
    }

    @Nested
    @DisplayName("DomainException 상속 검증")
    class DomainExceptionInheritance {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            // Given
            DuplicateMustItSellerIdException exception =
                    new DuplicateMustItSellerIdException("test");

            // When & Then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("HTTP 상태 코드 409를 반환한다")
        void shouldReturn409HttpStatus() {
            // Given
            DuplicateMustItSellerIdException exception =
                    new DuplicateMustItSellerIdException("test");

            // When & Then
            assertThat(exception.httpStatus()).isEqualTo(409);
        }
    }
}
