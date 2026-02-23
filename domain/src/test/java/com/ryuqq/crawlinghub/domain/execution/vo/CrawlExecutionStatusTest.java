package com.ryuqq.crawlinghub.domain.execution.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("CrawlExecutionStatus Enum 단위 테스트")
class CrawlExecutionStatusTest {

    @Nested
    @DisplayName("isTerminal() 테스트")
    class IsTerminalTest {

        @Test
        @DisplayName("RUNNING은 종료 상태가 아니다")
        void runningIsNotTerminal() {
            assertThat(CrawlExecutionStatus.RUNNING.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("SUCCESS는 종료 상태다")
        void successIsTerminal() {
            assertThat(CrawlExecutionStatus.SUCCESS.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("FAILED는 종료 상태다")
        void failedIsTerminal() {
            assertThat(CrawlExecutionStatus.FAILED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("TIMEOUT은 종료 상태다")
        void timeoutIsTerminal() {
            assertThat(CrawlExecutionStatus.TIMEOUT.isTerminal()).isTrue();
        }
    }

    @Nested
    @DisplayName("isFailure() 테스트")
    class IsFailureTest {

        @Test
        @DisplayName("RUNNING은 실패 상태가 아니다")
        void runningIsNotFailure() {
            assertThat(CrawlExecutionStatus.RUNNING.isFailure()).isFalse();
        }

        @Test
        @DisplayName("SUCCESS는 실패 상태가 아니다")
        void successIsNotFailure() {
            assertThat(CrawlExecutionStatus.SUCCESS.isFailure()).isFalse();
        }

        @Test
        @DisplayName("FAILED는 실패 상태다")
        void failedIsFailure() {
            assertThat(CrawlExecutionStatus.FAILED.isFailure()).isTrue();
        }

        @Test
        @DisplayName("TIMEOUT은 실패 상태다")
        void timeoutIsFailure() {
            assertThat(CrawlExecutionStatus.TIMEOUT.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("isSuccess() 테스트")
    class IsSuccessTest {

        @Test
        @DisplayName("SUCCESS만 성공 상태다")
        void successIsSuccess() {
            assertThat(CrawlExecutionStatus.SUCCESS.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("RUNNING은 성공 상태가 아니다")
        void runningIsNotSuccess() {
            assertThat(CrawlExecutionStatus.RUNNING.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("FAILED는 성공 상태가 아니다")
        void failedIsNotSuccess() {
            assertThat(CrawlExecutionStatus.FAILED.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("TIMEOUT은 성공 상태가 아니다")
        void timeoutIsNotSuccess() {
            assertThat(CrawlExecutionStatus.TIMEOUT.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("getDescription() 테스트")
    class GetDescriptionTest {

        @Test
        @DisplayName("각 상태의 설명을 반환한다")
        void returnsDescriptions() {
            assertThat(CrawlExecutionStatus.RUNNING.getDescription()).isEqualTo("실행 중");
            assertThat(CrawlExecutionStatus.SUCCESS.getDescription()).isEqualTo("성공");
            assertThat(CrawlExecutionStatus.FAILED.getDescription()).isEqualTo("실패");
            assertThat(CrawlExecutionStatus.TIMEOUT.getDescription()).isEqualTo("타임아웃");
        }
    }
}
