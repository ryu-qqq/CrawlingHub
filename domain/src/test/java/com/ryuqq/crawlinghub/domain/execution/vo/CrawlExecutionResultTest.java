package com.ryuqq.crawlinghub.domain.execution.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("CrawlExecutionResult Value Object 단위 테스트")
class CrawlExecutionResultTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("모든 필드로 복원 생성한다")
        void createWithAllFields() {
            CrawlExecutionResult result = CrawlExecutionResult.of("{}", 200, null);

            assertThat(result.responseBody()).isEqualTo("{}");
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("에러 메시지가 있는 결과를 생성한다")
        void createWithErrorMessage() {
            CrawlExecutionResult result = CrawlExecutionResult.of(null, 500, "서버 오류");

            assertThat(result.responseBody()).isNull();
            assertThat(result.httpStatusCode()).isEqualTo(500);
            assertThat(result.errorMessage()).isEqualTo("서버 오류");
        }
    }

    @Nested
    @DisplayName("isSuccess() 테스트")
    class IsSuccessTest {

        @Test
        @DisplayName("HTTP 200이면 true를 반환한다")
        void returnsTrueFor200() {
            CrawlExecutionResult result = CrawlExecutionResult.success("{}", 200);
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("HTTP 201이면 true를 반환한다")
        void returnsTrueFor201() {
            CrawlExecutionResult result = CrawlExecutionResult.success("{}", 201);
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("HTTP 400이면 false를 반환한다")
        void returnsFalseFor400() {
            CrawlExecutionResult result = CrawlExecutionResult.failure(400, "Bad Request");
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("HTTP 상태 코드가 null이면 false를 반환한다")
        void returnsFalseWhenNullStatusCode() {
            CrawlExecutionResult result = CrawlExecutionResult.timeout("timeout");
            assertThat(result.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("isClientError() 테스트")
    class IsClientErrorTest {

        @Test
        @DisplayName("HTTP 400이면 true를 반환한다")
        void returnsTrueFor400() {
            CrawlExecutionResult result = CrawlExecutionResult.failure(400, "Bad Request");
            assertThat(result.isClientError()).isTrue();
        }

        @Test
        @DisplayName("HTTP 429이면 true를 반환한다")
        void returnsTrueFor429() {
            CrawlExecutionResult result = CrawlExecutionResult.failure(429, "Rate Limit");
            assertThat(result.isClientError()).isTrue();
        }

        @Test
        @DisplayName("HTTP 500이면 false를 반환한다")
        void returnsFalseFor500() {
            CrawlExecutionResult result = CrawlExecutionResult.failure(500, "Server Error");
            assertThat(result.isClientError()).isFalse();
        }

        @Test
        @DisplayName("HTTP 상태 코드가 null이면 false를 반환한다")
        void returnsFalseWhenNullStatusCode() {
            CrawlExecutionResult result = CrawlExecutionResult.timeout("timeout");
            assertThat(result.isClientError()).isFalse();
        }
    }

    @Nested
    @DisplayName("isServerError() 테스트")
    class IsServerErrorTest {

        @Test
        @DisplayName("HTTP 500이면 true를 반환한다")
        void returnsTrueFor500() {
            CrawlExecutionResult result =
                    CrawlExecutionResult.failure(500, "Internal Server Error");
            assertThat(result.isServerError()).isTrue();
        }

        @Test
        @DisplayName("HTTP 503이면 true를 반환한다")
        void returnsTrueFor503() {
            CrawlExecutionResult result = CrawlExecutionResult.failure(503, "Service Unavailable");
            assertThat(result.isServerError()).isTrue();
        }

        @Test
        @DisplayName("HTTP 400이면 false를 반환한다")
        void returnsFalseFor400() {
            CrawlExecutionResult result = CrawlExecutionResult.failure(400, "Bad Request");
            assertThat(result.isServerError()).isFalse();
        }

        @Test
        @DisplayName("HTTP 상태 코드가 null이면 false를 반환한다")
        void returnsFalseWhenNullStatusCode() {
            CrawlExecutionResult result = CrawlExecutionResult.timeout("timeout");
            assertThat(result.isServerError()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasError() 테스트")
    class HasErrorTest {

        @Test
        @DisplayName("에러 메시지가 있으면 true를 반환한다")
        void returnsTrueWhenHasErrorMessage() {
            CrawlExecutionResult result = CrawlExecutionResult.failure(500, "에러 발생");
            assertThat(result.hasError()).isTrue();
        }

        @Test
        @DisplayName("에러 메시지가 null이면 false를 반환한다")
        void returnsFalseWhenNullErrorMessage() {
            CrawlExecutionResult result = CrawlExecutionResult.success("{}", 200);
            assertThat(result.hasError()).isFalse();
        }

        @Test
        @DisplayName("에러 메시지가 빈 문자열이면 false를 반환한다")
        void returnsFalseWhenBlankErrorMessage() {
            CrawlExecutionResult result = CrawlExecutionResult.of("{}", 200, "  ");
            assertThat(result.hasError()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            CrawlExecutionResult result1 = CrawlExecutionResult.of("{}", 200, null);
            CrawlExecutionResult result2 = CrawlExecutionResult.of("{}", 200, null);
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }
    }
}
