package com.ryuqq.crawlinghub.domain.crawl.task.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrawlTaskErrorCode Enum 단위 테스트
 *
 * <p>Kent Beck TDD - Red Phase
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskErrorCode 테스트")
class CrawlTaskErrorCodeTest {

    @ParameterizedTest
    @EnumSource(CrawlTaskErrorCode.class)
    @DisplayName("모든 에러 코드는 code, httpStatus, message를 가짐")
    void shouldHaveCorrectCodeAndMessage(CrawlTaskErrorCode errorCode) {
        // given & when & then
        assertThat(errorCode.getCode()).startsWith("CRAWL-TASK-");
        assertThat(errorCode.getHttpStatus()).isGreaterThan(0);
        assertThat(errorCode.getMessage()).isNotBlank();
    }
}
