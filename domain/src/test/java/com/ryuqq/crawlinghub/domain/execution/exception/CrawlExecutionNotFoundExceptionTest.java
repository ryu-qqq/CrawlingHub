package com.ryuqq.crawlinghub.domain.execution.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("CrawlExecutionNotFoundException 단위 테스트")
class CrawlExecutionNotFoundExceptionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("crawlExecutionId로 예외를 생성한다")
        void createWithCrawlExecutionId() {
            // given
            Long crawlExecutionId = 42L;

            // when
            CrawlExecutionNotFoundException exception =
                    new CrawlExecutionNotFoundException(crawlExecutionId);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception).isInstanceOf(CrawlExecutionException.class);
        }

        @Test
        @DisplayName("메시지에 crawlExecutionId가 포함된다")
        void messageContainsCrawlExecutionId() {
            // when
            CrawlExecutionNotFoundException exception = new CrawlExecutionNotFoundException(99L);

            // then
            assertThat(exception.getMessage()).contains("99");
        }
    }

    @Nested
    @DisplayName("ErrorCode 테스트")
    class ErrorCodeTest {

        @Test
        @DisplayName("에러 코드는 CRAWL_EXECUTION_NOT_FOUND이다")
        void errorCodeIsCrawlExecutionNotFound() {
            // when
            CrawlExecutionNotFoundException exception = new CrawlExecutionNotFoundException(1L);

            // then
            assertThat(exception.getErrorCode())
                    .isEqualTo(CrawlExecutionErrorCode.CRAWL_EXECUTION_NOT_FOUND);
        }

        @Test
        @DisplayName("에러 코드의 HTTP 상태는 404이다")
        void errorCodeHttpStatusIs404() {
            // when
            CrawlExecutionNotFoundException exception = new CrawlExecutionNotFoundException(1L);

            // then
            assertThat(exception.httpStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("에러 코드는 CRAWL-EXEC-001이다")
        void errorCodeIsCrawlExec001() {
            // when
            CrawlExecutionNotFoundException exception = new CrawlExecutionNotFoundException(1L);

            // then
            assertThat(exception.code()).isEqualTo("CRAWL-EXEC-001");
        }
    }
}
