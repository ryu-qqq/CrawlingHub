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
@DisplayName("SellerHasActiveSchedulersException 단위 테스트")
class SellerHasActiveSchedulersExceptionTest {

    @Nested
    @DisplayName("예외 생성")
    class ExceptionCreation {

        @Test
        @DisplayName("셀러 ID와 활성 스케줄러 개수로 예외 생성")
        void shouldCreateWithSellerIdAndActiveSchedulerCount() {
            // Given
            long sellerId = 100L;
            int activeSchedulerCount = 3;

            // When
            SellerHasActiveSchedulersException exception =
                    new SellerHasActiveSchedulersException(sellerId, activeSchedulerCount);

            // Then
            assertThat(exception.code()).isEqualTo("SELLER-003");
            assertThat(exception.getMessage()).contains("활성화된 스케줄러");
            assertThat(exception.getMessage()).contains("비활성화할 수 없습니다");
            assertThat(exception.getMessage()).contains("100");
            assertThat(exception.getMessage()).contains("3개");
            assertThat(exception.args()).containsEntry("sellerId", sellerId);
            assertThat(exception.args())
                    .containsEntry("activeSchedulerCount", activeSchedulerCount);
        }

        @Test
        @DisplayName("다른 셀러 ID와 활성 스케줄러 개수로 예외 생성")
        void shouldCreateWithDifferentSellerIdAndCount() {
            // Given
            long sellerId = 999L;
            int activeSchedulerCount = 10;

            // When
            SellerHasActiveSchedulersException exception =
                    new SellerHasActiveSchedulersException(sellerId, activeSchedulerCount);

            // Then
            assertThat(exception.getMessage()).contains("999");
            assertThat(exception.getMessage()).contains("10개");
            assertThat(exception.args()).containsEntry("sellerId", sellerId);
            assertThat(exception.args())
                    .containsEntry("activeSchedulerCount", activeSchedulerCount);
        }

        @Test
        @DisplayName("활성 스케줄러가 1개일 때 예외 생성")
        void shouldCreateWithSingleActiveScheduler() {
            // Given
            long sellerId = 50L;
            int activeSchedulerCount = 1;

            // When
            SellerHasActiveSchedulersException exception =
                    new SellerHasActiveSchedulersException(sellerId, activeSchedulerCount);

            // Then
            assertThat(exception.getMessage()).contains("1개");
            assertThat(exception.args())
                    .containsEntry("activeSchedulerCount", activeSchedulerCount);
        }
    }

    @Nested
    @DisplayName("DomainException 상속 검증")
    class DomainExceptionInheritance {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            // Given
            SellerHasActiveSchedulersException exception =
                    new SellerHasActiveSchedulersException(1L, 1);

            // When & Then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("HTTP 상태 코드 400을 반환한다")
        void shouldReturn400HttpStatus() {
            // Given
            SellerHasActiveSchedulersException exception =
                    new SellerHasActiveSchedulersException(1L, 1);

            // When & Then
            assertThat(exception.httpStatus()).isEqualTo(400);
        }
    }
}
