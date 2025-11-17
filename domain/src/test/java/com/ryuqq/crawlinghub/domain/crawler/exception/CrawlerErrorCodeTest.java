package com.ryuqq.crawlinghub.domain.crawler.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrawlerErrorCode 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("CrawlerErrorCode 테스트")
class CrawlerErrorCodeTest {

    @Test
    @DisplayName("CRAWLER_TASK_NOT_FOUND 에러 코드 검증")
    void shouldReturnCorrectErrorCodeForCrawlerTaskNotFound() {
        // when
        CrawlerErrorCode errorCode = CrawlerErrorCode.CRAWLER_TASK_NOT_FOUND;

        // then
        assertThat(errorCode.getCode()).isEqualTo("CRAWLER-001");
        assertThat(errorCode.getHttpStatus()).isEqualTo(404);
        assertThat(errorCode.getMessage()).isEqualTo("Crawler task not found");
    }

    @Test
    @DisplayName("USER_AGENT_NOT_FOUND 에러 코드 검증")
    void shouldReturnCorrectErrorCodeForUserAgentNotFound() {
        // when
        CrawlerErrorCode errorCode = CrawlerErrorCode.USER_AGENT_NOT_FOUND;

        // then
        assertThat(errorCode.getCode()).isEqualTo("CRAWLER-002");
        assertThat(errorCode.getHttpStatus()).isEqualTo(404);
        assertThat(errorCode.getMessage()).isEqualTo("User agent not found");
    }

    @Test
    @DisplayName("INVALID_CRAWLER_ARGUMENT 에러 코드 검증")
    void shouldReturnCorrectErrorCodeForInvalidCrawlerArgument() {
        // when
        CrawlerErrorCode errorCode = CrawlerErrorCode.INVALID_CRAWLER_ARGUMENT;

        // then
        assertThat(errorCode.getCode()).isEqualTo("CRAWLER-003");
        assertThat(errorCode.getHttpStatus()).isEqualTo(400);
        assertThat(errorCode.getMessage()).isEqualTo("Invalid crawler argument");
    }

    @Test
    @DisplayName("모든 ErrorCode Enum 상수 검증")
    void shouldHaveAllErrorCodeConstants() {
        // when
        CrawlerErrorCode[] errorCodes = CrawlerErrorCode.values();

        // then
        assertThat(errorCodes).hasSize(6);
        assertThat(errorCodes).contains(
                CrawlerErrorCode.CRAWLER_TASK_NOT_FOUND,
                CrawlerErrorCode.USER_AGENT_NOT_FOUND,
                CrawlerErrorCode.INVALID_CRAWLER_ARGUMENT,
                CrawlerErrorCode.INVALID_TASK_STATE,
                CrawlerErrorCode.USER_AGENT_RATE_LIMIT_EXCEEDED,
                CrawlerErrorCode.INVALID_REQUEST_URL
        );
    }
}
