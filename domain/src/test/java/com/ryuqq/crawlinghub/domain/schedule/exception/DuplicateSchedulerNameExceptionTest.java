package com.ryuqq.crawlinghub.domain.schedule.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("DuplicateSchedulerNameException 단위 테스트")
class DuplicateSchedulerNameExceptionTest {

    @Nested
    @DisplayName("예외 생성")
    class ExceptionCreation {

        @Test
        @DisplayName("셀러 ID와 스케줄러 이름으로 예외 생성")
        void shouldCreateWithSellerIdAndSchedulerName() {
            // Given
            Long sellerId = 100L;
            String schedulerName = "daily-crawler";

            // When
            DuplicateSchedulerNameException exception =
                    new DuplicateSchedulerNameException(sellerId, schedulerName);

            // Then
            assertThat(exception.code()).isEqualTo("SCHEDULE-002");
            assertThat(exception.getMessage()).contains("이미 존재하는");
            assertThat(exception.getMessage()).contains("100");
            assertThat(exception.getMessage()).contains("daily-crawler");
            assertThat(exception.args()).containsEntry("sellerId", sellerId);
            assertThat(exception.args()).containsEntry("schedulerName", schedulerName);
        }

        @Test
        @DisplayName("다른 셀러 ID와 스케줄러 이름으로 예외 생성")
        void shouldCreateWithDifferentSellerIdAndSchedulerName() {
            // Given
            Long sellerId = 999L;
            String schedulerName = "weekly-sync";

            // When
            DuplicateSchedulerNameException exception =
                    new DuplicateSchedulerNameException(sellerId, schedulerName);

            // Then
            assertThat(exception.getMessage()).contains("999");
            assertThat(exception.getMessage()).contains("weekly-sync");
            assertThat(exception.args()).containsEntry("sellerId", sellerId);
            assertThat(exception.args()).containsEntry("schedulerName", schedulerName);
        }
    }

    @Nested
    @DisplayName("DomainException 상속 검증")
    class DomainExceptionInheritance {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            // Given
            DuplicateSchedulerNameException exception =
                    new DuplicateSchedulerNameException(1L, "test");

            // When & Then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("HTTP 상태 코드 409를 반환한다")
        void shouldReturn409HttpStatus() {
            // Given
            DuplicateSchedulerNameException exception =
                    new DuplicateSchedulerNameException(1L, "test");

            // When & Then
            assertThat(exception.httpStatus()).isEqualTo(409);
        }
    }
}
