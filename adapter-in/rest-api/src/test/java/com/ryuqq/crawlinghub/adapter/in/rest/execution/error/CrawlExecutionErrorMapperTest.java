package com.ryuqq.crawlinghub.adapter.in.rest.execution.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import com.ryuqq.crawlinghub.domain.execution.exception.CrawlExecutionNotFoundException;
import com.ryuqq.crawlinghub.domain.execution.exception.InvalidCrawlExecutionStateException;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
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
 * CrawlExecutionErrorMapper 단위 테스트
 *
 * <p>CrawlExecution 도메인 예외 → HTTP 응답 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>PREFIX 기반 supports() 테스트
 *   <li>각 CrawlExecution 예외별 HttpStatus 매핑 검증
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
@DisplayName("CrawlExecutionErrorMapper 단위 테스트")
class CrawlExecutionErrorMapperTest {

    private CrawlExecutionErrorMapper crawlExecutionErrorMapper;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        this.messageSource = messageSource;

        crawlExecutionErrorMapper = new CrawlExecutionErrorMapper(messageSource);
    }

    @Nested
    @DisplayName("supports() 테스트")
    class SupportsTests {

        @Test
        @DisplayName("CRAWL-EXEC- prefix를 가진 에러 코드는 supports()가 true를 반환한다")
        void supports_WhenCrawlExecPrefix_ShouldReturnTrue() {
            // given
            DomainException ex = createDomainException("CRAWL-EXEC-001");

            // when
            boolean result = crawlExecutionErrorMapper.supports(ex);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("CRAWL-EXEC- prefix가 없는 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenNonCrawlExecPrefix_ShouldReturnFalse() {
            // given
            DomainException ex = createDomainException("CRAWL-TASK-001");

            // when
            boolean result = crawlExecutionErrorMapper.supports(ex);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenNullCode_ShouldReturnFalse() {
            // when
            boolean result = crawlExecutionErrorMapper.supports(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenEmptyCode_ShouldReturnFalse() {
            // given
            DomainException ex = createDomainException("");

            // when
            boolean result = crawlExecutionErrorMapper.supports(ex);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("CRAWL- prefix만 있는 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenOnlyCrawlPrefix_ShouldReturnFalse() {
            // given
            DomainException ex = createDomainException("CRAWL-TASK-001");

            // when
            boolean result = crawlExecutionErrorMapper.supports(ex);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("map() 테스트 - CrawlExecutionNotFoundException (CRAWL-EXEC-001)")
    class CrawlExecutionNotFoundTests {

        @Test
        @DisplayName("CrawlExecutionNotFoundException은 404 NOT_FOUND로 매핑된다")
        void map_CrawlExecutionNotFoundException_ShouldReturn404() {
            // given
            Long crawlExecutionId = 999L;
            var exception = new CrawlExecutionNotFoundException(crawlExecutionId);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlExecutionErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create(
                                    "https://api.example.com/problems/execution/crawl-exec-001"));
            assertThat(result.detail()).contains("존재하지 않는 크롤 실행입니다");
            assertThat(result.detail()).contains("999");
        }

        @Test
        @DisplayName("CrawlExecutionNotFoundException은 crawlExecutionId 파라미터가 포함된 메시지로 변환된다")
        void map_CrawlExecutionNotFoundException_ShouldIncludeExecutionId() {
            // given
            Long crawlExecutionId = 123L;
            var exception = new CrawlExecutionNotFoundException(crawlExecutionId);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlExecutionErrorMapper.map(exception, locale);

            // then
            assertThat(result.title()).isNotNull();
            assertThat(result.detail()).contains("123");
        }
    }

    @Nested
    @DisplayName("map() 테스트 - InvalidCrawlExecutionStateException (CRAWL-EXEC-002)")
    class InvalidCrawlExecutionStateTests {

        @Test
        @DisplayName("InvalidCrawlExecutionStateException은 400 BAD_REQUEST로 매핑된다")
        void map_InvalidCrawlExecutionStateException_ShouldReturn400() {
            // given
            CrawlExecutionStatus currentStatus = CrawlExecutionStatus.SUCCESS;
            var exception = new InvalidCrawlExecutionStateException(currentStatus);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlExecutionErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create(
                                    "https://api.example.com/problems/execution/crawl-exec-002"));
            assertThat(result.detail()).contains("유효하지 않은 상태 전환입니다");
            assertThat(result.detail()).contains("SUCCESS");
        }

        @Test
        @DisplayName("InvalidCrawlExecutionStateException은 현재 상태 정보가 포함된 메시지로 변환된다")
        void map_InvalidCrawlExecutionStateException_ShouldIncludeCurrentStatus() {
            // given
            CrawlExecutionStatus currentStatus = CrawlExecutionStatus.FAILED;
            var exception = new InvalidCrawlExecutionStateException(currentStatus);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlExecutionErrorMapper.map(exception, locale);

            // then
            assertThat(result.title()).isNotNull();
            assertThat(result.detail()).contains("FAILED");
            assertThat(result.detail()).contains("RUNNING 상태에서만 완료 처리 가능");
        }

        @Test
        @DisplayName("TIMEOUT 상태에서 상태 전환 시도 시 예외가 올바르게 매핑된다")
        void map_InvalidCrawlExecutionStateException_WhenTimeout_ShouldMapCorrectly() {
            // given
            CrawlExecutionStatus currentStatus = CrawlExecutionStatus.TIMEOUT;
            var exception = new InvalidCrawlExecutionStateException(currentStatus);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlExecutionErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.detail()).contains("TIMEOUT");
        }
    }

    @Nested
    @DisplayName("Type URI 생성 테스트")
    class TypeUriTests {

        @Test
        @DisplayName("Type URI는 소문자로 변환되어 생성된다")
        void map_ShouldGenerateLowercaseTypeUri() {
            // given
            var exception = new CrawlExecutionNotFoundException(1L);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlExecutionErrorMapper.map(exception, locale);

            // then
            String typeUri = result.type().toString();
            assertThat(typeUri)
                    .isEqualTo("https://api.example.com/problems/execution/crawl-exec-001");
            assertThat(typeUri).doesNotContain("CRAWL-EXEC");
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
