package com.ryuqq.crawlinghub.adapter.in.rest.task.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskRetryException;
import com.ryuqq.crawlinghub.domain.task.exception.InvalidCrawlTaskStateException;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.net.URI;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;

/**
 * CrawlTaskErrorMapper 단위 테스트
 *
 * <p>CrawlTask 도메인 예외 → HTTP 응답 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>PREFIX 기반 supports() 테스트
 *   <li>각 CrawlTask 예외별 HttpStatus 매핑 검증
 *   <li>RFC 7807 Type URI 생성 검증
 *   <li>I18N 메시지 변환 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("error")
@DisplayName("CrawlTaskErrorMapper 단위 테스트")
class CrawlTaskErrorMapperTest {

    private CrawlTaskErrorMapper crawlTaskErrorMapper;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        this.messageSource = messageSource;

        crawlTaskErrorMapper = new CrawlTaskErrorMapper(messageSource);
    }

    @Nested
    @DisplayName("supports() 테스트")
    class SupportsTests {

        @Test
        @DisplayName("CRAWL-TASK- prefix를 가진 에러 코드는 supports()가 true를 반환한다")
        void supports_WhenCrawlTaskPrefix_ShouldReturnTrue() {
            // given
            DomainException ex = createDomainException("CRAWL-TASK-001");

            // when
            boolean result = crawlTaskErrorMapper.supports(ex);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("CRAWL-TASK- prefix가 없는 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenNonCrawlTaskPrefix_ShouldReturnFalse() {
            // given
            DomainException ex = createDomainException("SCHEDULE-001");

            // when
            boolean result = crawlTaskErrorMapper.supports(ex);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenNullCode_ShouldReturnFalse() {
            // when
            boolean result = crawlTaskErrorMapper.supports(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenEmptyCode_ShouldReturnFalse() {
            // given
            DomainException ex = createDomainException("");

            // when
            boolean result = crawlTaskErrorMapper.supports(ex);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("CRAWL- prefix만 있는 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenOnlyCrawlPrefix_ShouldReturnFalse() {
            // given
            DomainException ex = createDomainException("CRAWL-EXEC-001");

            // when
            boolean result = crawlTaskErrorMapper.supports(ex);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("map() 테스트 - CrawlTaskNotFoundException (CRAWL-TASK-001)")
    class CrawlTaskNotFoundTests {

        @Test
        @DisplayName("CrawlTaskNotFoundException은 404 NOT_FOUND로 매핑된다")
        void map_CrawlTaskNotFoundException_ShouldReturn404() {
            // given
            Long crawlTaskId = 999L;
            var exception = new CrawlTaskNotFoundException(crawlTaskId);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlTaskErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create(
                                    "https://api.example.com/problems/crawl-task/crawl-task-001"));
            assertThat(result.detail()).contains("존재하지 않는 크롤 태스크입니다");
            assertThat(result.detail()).contains("999");
        }

        @Test
        @DisplayName("CrawlTaskNotFoundException은 crawlTaskId 파라미터가 포함된 메시지로 변환된다")
        void map_CrawlTaskNotFoundException_ShouldIncludeTaskId() {
            // given
            Long crawlTaskId = 123L;
            var exception = new CrawlTaskNotFoundException(crawlTaskId);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlTaskErrorMapper.map(exception, locale);

            // then
            assertThat(result.title()).isNotNull();
            assertThat(result.detail()).contains("123");
        }
    }

    @Nested
    @DisplayName("map() 테스트 - InvalidCrawlTaskStateException (CRAWL-TASK-002)")
    class InvalidCrawlTaskStateTests {

        @Test
        @DisplayName("InvalidCrawlTaskStateException은 400 BAD_REQUEST로 매핑된다")
        void map_InvalidCrawlTaskStateException_ShouldReturn400() {
            // given
            CrawlTaskStatus currentStatus = CrawlTaskStatus.WAITING;
            CrawlTaskStatus expectedStatus = CrawlTaskStatus.RUNNING;
            var exception = new InvalidCrawlTaskStateException(currentStatus, expectedStatus);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlTaskErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create(
                                    "https://api.example.com/problems/crawl-task/crawl-task-002"));
            assertThat(result.detail()).contains("유효하지 않은 상태 전환입니다");
            assertThat(result.detail()).contains("WAITING");
            assertThat(result.detail()).contains("RUNNING");
        }

        @Test
        @DisplayName("InvalidCrawlTaskStateException은 상태 정보가 포함된 메시지로 변환된다")
        void map_InvalidCrawlTaskStateException_ShouldIncludeStateInfo() {
            // given
            CrawlTaskStatus currentStatus = CrawlTaskStatus.FAILED;
            CrawlTaskStatus expectedStatus = CrawlTaskStatus.SUCCESS;
            var exception = new InvalidCrawlTaskStateException(currentStatus, expectedStatus);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlTaskErrorMapper.map(exception, locale);

            // then
            assertThat(result.title()).isNotNull();
            assertThat(result.detail()).contains("FAILED");
            assertThat(result.detail()).contains("SUCCESS");
        }
    }

    @Nested
    @DisplayName("map() 테스트 - CrawlTaskRetryException (CRAWL-TASK-004)")
    class CrawlTaskRetryTests {

        @Test
        @DisplayName("CrawlTaskRetryException은 400 BAD_REQUEST로 매핑된다")
        void map_CrawlTaskRetryException_ShouldReturn400() {
            // given
            Long crawlTaskId = 100L;
            CrawlTaskStatus currentStatus = CrawlTaskStatus.SUCCESS;
            int retryCount = 3;
            var exception = new CrawlTaskRetryException(crawlTaskId, currentStatus, retryCount);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlTaskErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create(
                                    "https://api.example.com/problems/crawl-task/crawl-task-004"));
            assertThat(result.detail()).contains("재시도할 수 없습니다");
        }

        @Test
        @DisplayName("CrawlTaskRetryException은 재시도 정보가 포함된 메시지로 변환된다")
        void map_CrawlTaskRetryException_ShouldIncludeRetryInfo() {
            // given
            Long crawlTaskId = 200L;
            CrawlTaskStatus currentStatus = CrawlTaskStatus.TIMEOUT;
            int retryCount = 5;
            var exception = new CrawlTaskRetryException(crawlTaskId, currentStatus, retryCount);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlTaskErrorMapper.map(exception, locale);

            // then
            assertThat(result.title()).isNotNull();
            assertThat(result.detail()).contains("200");
            assertThat(result.detail()).contains("TIMEOUT");
            assertThat(result.detail()).contains("5");
        }
    }

    @Nested
    @DisplayName("Type URI 생성 테스트")
    class TypeUriTests {

        @Test
        @DisplayName("Type URI는 소문자로 변환되어 생성된다")
        void map_ShouldGenerateLowercaseTypeUri() {
            // given
            var exception = new CrawlTaskNotFoundException(1L);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlTaskErrorMapper.map(exception, locale);

            // then
            String typeUri = result.type().toString();
            assertThat(typeUri)
                    .isEqualTo("https://api.example.com/problems/crawl-task/crawl-task-001");
            assertThat(typeUri).doesNotContain("CRAWL-TASK");
        }
    }

    private DomainException createDomainException(String code) {
        ErrorCode errorCode = Mockito.mock(ErrorCode.class);
        given(errorCode.getCode()).willReturn(code);
        return new DomainException(errorCode, "test") {
            @Override
            public String code() {
                return code;
            }
        };
    }
}
