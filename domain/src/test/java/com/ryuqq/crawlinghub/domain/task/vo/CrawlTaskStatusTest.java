package com.ryuqq.crawlinghub.domain.task.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * CrawlTaskStatus Enum 단위 테스트
 *
 * <p>Kent Beck TDD - Red Phase
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskStatus Enum 테스트")
class CrawlTaskStatusTest {

    @Nested
    @DisplayName("isInProgress() 테스트")
    class IsInProgressTest {

        @ParameterizedTest
        @EnumSource(
                value = CrawlTaskStatus.class,
                names = {"WAITING", "PUBLISHED", "RUNNING"})
        @DisplayName("WAITING, PUBLISHED, RUNNING 상태는 진행 중")
        void shouldReturnTrueForInProgressStatuses(CrawlTaskStatus status) {
            // given & when
            boolean result = status.isInProgress();

            // then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @EnumSource(
                value = CrawlTaskStatus.class,
                names = {"SUCCESS", "FAILED", "RETRY", "TIMEOUT"})
        @DisplayName("SUCCESS, FAILED, RETRY, TIMEOUT 상태는 진행 중 아님")
        void shouldReturnFalseForNonInProgressStatuses(CrawlTaskStatus status) {
            // given & when
            boolean result = status.isInProgress();

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isTerminal() 테스트")
    class IsTerminalTest {

        @ParameterizedTest
        @EnumSource(
                value = CrawlTaskStatus.class,
                names = {"SUCCESS", "FAILED"})
        @DisplayName("SUCCESS, FAILED 상태는 종료 상태")
        void shouldReturnTrueForTerminalStatuses(CrawlTaskStatus status) {
            // given & when
            boolean result = status.isTerminal();

            // then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @EnumSource(
                value = CrawlTaskStatus.class,
                names = {"WAITING", "PUBLISHED", "RUNNING", "RETRY", "TIMEOUT"})
        @DisplayName("WAITING, PUBLISHED, RUNNING, RETRY, TIMEOUT 상태는 종료 상태 아님")
        void shouldReturnFalseForNonTerminalStatuses(CrawlTaskStatus status) {
            // given & when
            boolean result = status.isTerminal();

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("description 테스트")
    class DescriptionTest {

        @Test
        @DisplayName("모든 상태는 description을 가짐")
        void shouldHaveDescription() {
            // given & when & then
            for (CrawlTaskStatus status : CrawlTaskStatus.values()) {
                assertThat(status.getDescription())
                        .as("상태 %s는 description이 있어야 함", status)
                        .isNotNull()
                        .isNotBlank();
            }
        }

        @Test
        @DisplayName("WAITING 상태 description 확인")
        void shouldHaveCorrectDescriptionForWaiting() {
            // given & when & then
            assertThat(CrawlTaskStatus.WAITING.getDescription()).contains("대기");
        }

        @Test
        @DisplayName("RUNNING 상태 description 확인")
        void shouldHaveCorrectDescriptionForRunning() {
            // given & when & then
            assertThat(CrawlTaskStatus.RUNNING.getDescription()).contains("실행");
        }
    }
}
