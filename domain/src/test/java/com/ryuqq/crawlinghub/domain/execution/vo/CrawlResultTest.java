package com.ryuqq.crawlinghub.domain.execution.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("CrawlResult 단위 테스트")
class CrawlResultTest {

    @Nested
    @DisplayName("success() 팩토리 메서드 테스트")
    class SuccessTest {

        @Test
        @DisplayName("성공 결과를 생성한다")
        void createSuccessResult() {
            // when
            CrawlResult result = CrawlResult.success("{\"items\":[]}", 200);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.responseBody()).isEqualTo("{\"items\":[]}");
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("failure() 팩토리 메서드 테스트")
    class FailureTest {

        @Test
        @DisplayName("에러 메시지로 실패 결과를 생성한다")
        void createFailureResultWithMessage() {
            // when
            CrawlResult result = CrawlResult.failure("Connection refused");

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.responseBody()).isNull();
            assertThat(result.httpStatusCode()).isNull();
            assertThat(result.errorMessage()).isEqualTo("Connection refused");
        }

        @Test
        @DisplayName("HTTP 상태 코드와 에러 메시지로 실패 결과를 생성한다")
        void createFailureResultWithStatusCode() {
            // when
            CrawlResult result = CrawlResult.failure(503, "Service Unavailable");

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.responseBody()).isNull();
            assertThat(result.httpStatusCode()).isEqualTo(503);
            assertThat(result.errorMessage()).isEqualTo("Service Unavailable");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 필드이면 동일하다")
        void sameFieldsAreEqual() {
            // given
            CrawlResult result1 = CrawlResult.success("{}", 200);
            CrawlResult result2 = CrawlResult.success("{}", 200);

            // then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("다른 필드이면 다르다")
        void differentFieldsAreNotEqual() {
            // given
            CrawlResult success = CrawlResult.success("{}", 200);
            CrawlResult failure = CrawlResult.failure("error");

            // then
            assertThat(success).isNotEqualTo(failure);
        }
    }
}
