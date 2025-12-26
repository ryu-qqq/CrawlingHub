package com.ryuqq.crawlinghub.domain.product.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * CrawledProductErrorCode Enum 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawledProductErrorCode 테스트")
class CrawledProductErrorCodeTest {

    @ParameterizedTest
    @EnumSource(CrawledProductErrorCode.class)
    @DisplayName("모든 에러 코드는 code, httpStatus, message를 가짐")
    void shouldHaveCorrectCodeAndMessage(CrawledProductErrorCode errorCode) {
        // given & when & then
        assertThat(errorCode.getCode()).matches("(PRODUCT|OUTBOX)-\\d+");
        assertThat(errorCode.getHttpStatus()).isGreaterThan(0);
        assertThat(errorCode.getMessage()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(CrawledProductErrorCode.class)
    @DisplayName("HTTP 상태 코드는 유효한 범위 내에 있어야 함")
    void shouldHaveValidHttpStatusRange(CrawledProductErrorCode errorCode) {
        // given & when
        int httpStatus = errorCode.getHttpStatus();

        // then
        assertThat(httpStatus).isBetween(100, 599);
    }
}
