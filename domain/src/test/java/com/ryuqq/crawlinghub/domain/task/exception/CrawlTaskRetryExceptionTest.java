package com.ryuqq.crawlinghub.domain.task.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("CrawlTaskRetryException 단위 테스트")
class CrawlTaskRetryExceptionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("crawlTaskId, currentStatus, retryCount로 예외를 생성한다")
        void createWithRequiredFields() {
            // given
            Long crawlTaskId = 1L;
            CrawlTaskStatus status = CrawlTaskStatus.FAILED;
            int retryCount = 3;

            // when
            CrawlTaskRetryException exception =
                    new CrawlTaskRetryException(crawlTaskId, status, retryCount);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception).isInstanceOf(CrawlTaskException.class);
        }

        @Test
        @DisplayName("메시지에 Task ID가 포함된다")
        void messageContainsTaskId() {
            // given
            Long crawlTaskId = 42L;

            // when
            CrawlTaskRetryException exception =
                    new CrawlTaskRetryException(crawlTaskId, CrawlTaskStatus.TIMEOUT, 2);

            // then
            assertThat(exception.getMessage()).contains("42");
        }

        @Test
        @DisplayName("메시지에 현재 상태가 포함된다")
        void messageContainsStatus() {
            // when
            CrawlTaskRetryException exception =
                    new CrawlTaskRetryException(1L, CrawlTaskStatus.TIMEOUT, 1);

            // then
            assertThat(exception.getMessage()).contains("TIMEOUT");
        }

        @Test
        @DisplayName("메시지에 재시도 횟수가 포함된다")
        void messageContainsRetryCount() {
            // when
            CrawlTaskRetryException exception =
                    new CrawlTaskRetryException(1L, CrawlTaskStatus.FAILED, 3);

            // then
            assertThat(exception.getMessage()).contains("3");
        }
    }

    @Nested
    @DisplayName("ErrorCode 테스트")
    class ErrorCodeTest {

        @Test
        @DisplayName("에러 코드는 RETRY_LIMIT_EXCEEDED이다")
        void errorCodeIsRetryLimitExceeded() {
            // when
            CrawlTaskRetryException exception =
                    new CrawlTaskRetryException(1L, CrawlTaskStatus.FAILED, 3);

            // then
            assertThat(exception.getErrorCode()).isEqualTo(CrawlTaskErrorCode.RETRY_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("에러 코드의 HTTP 상태는 400이다")
        void errorCodeHttpStatusIs400() {
            // when
            CrawlTaskRetryException exception =
                    new CrawlTaskRetryException(1L, CrawlTaskStatus.FAILED, 3);

            // then
            assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("에러 코드는 CRAWL-TASK-004이다")
        void errorCodeIsCrawlTask004() {
            // when
            CrawlTaskRetryException exception =
                    new CrawlTaskRetryException(1L, CrawlTaskStatus.FAILED, 3);

            // then
            assertThat(exception.getErrorCode().getCode()).isEqualTo("CRAWL-TASK-004");
        }
    }
}
