package com.ryuqq.crawlinghub.adapter.in.rest.schedule.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import java.net.URI;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;

/**
 * CrawlSchedulerErrorMapper 단위 테스트
 *
 * <p>CrawlScheduler 도메인 예외 → HTTP 응답 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>PREFIX 기반 supports() 테스트
 *   <li>각 CrawlScheduler 예외별 HttpStatus 매핑 검증
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
@DisplayName("CrawlSchedulerErrorMapper 단위 테스트")
class CrawlSchedulerErrorMapperTest {

    private CrawlSchedulerErrorMapper crawlSchedulerErrorMapper;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        this.messageSource = messageSource;

        crawlSchedulerErrorMapper = new CrawlSchedulerErrorMapper(messageSource);
    }

    @Nested
    @DisplayName("supports() 테스트")
    class SupportsTests {

        @Test
        @DisplayName("SCHEDULE- prefix를 가진 에러 코드는 supports()가 true를 반환한다")
        void supports_WhenSchedulePrefix_ShouldReturnTrue() {
            // given
            String code = "SCHEDULE-001";

            // when
            boolean result = crawlSchedulerErrorMapper.supports(code);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("SCHEDULE- prefix가 없는 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenNonSchedulePrefix_ShouldReturnFalse() {
            // given
            String code = "SELLER-001";

            // when
            boolean result = crawlSchedulerErrorMapper.supports(code);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenNullCode_ShouldReturnFalse() {
            // given
            String code = null;

            // when
            boolean result = crawlSchedulerErrorMapper.supports(code);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenEmptyCode_ShouldReturnFalse() {
            // given
            String code = "";

            // when
            boolean result = crawlSchedulerErrorMapper.supports(code);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("map() 테스트 - CrawlSchedulerNotFoundException (SCHEDULE-001)")
    class CrawlSchedulerNotFoundTests {

        @Test
        @DisplayName("CrawlSchedulerNotFoundException은 404 NOT_FOUND로 매핑된다")
        void map_CrawlSchedulerNotFoundException_ShouldReturn404() {
            // given
            long crawlSchedulerId = 999L;
            var exception = new CrawlSchedulerNotFoundException(crawlSchedulerId);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlSchedulerErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create("https://api.example.com/problems/schedule/schedule-001"));
            assertThat(result.detail()).contains("존재하지 않는 크롤 스케줄러입니다");
            assertThat(result.detail()).contains("999");
        }

        @Test
        @DisplayName("CrawlSchedulerNotFoundException은 crawlSchedulerId 파라미터가 포함된 메시지로 변환된다")
        void map_CrawlSchedulerNotFoundException_ShouldIncludeSchedulerId() {
            // given
            long crawlSchedulerId = 123L;
            var exception = new CrawlSchedulerNotFoundException(crawlSchedulerId);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlSchedulerErrorMapper.map(exception, locale);

            // then
            assertThat(result.title()).isNotNull();
            assertThat(result.detail()).contains("123");
        }
    }

    @Nested
    @DisplayName("map() 테스트 - DuplicateSchedulerNameException (SCHEDULE-002)")
    class DuplicateSchedulerNameTests {

        @Test
        @DisplayName("DuplicateSchedulerNameException은 409 CONFLICT로 매핑된다")
        void map_DuplicateSchedulerNameException_ShouldReturn409() {
            // given
            Long sellerId = 1L;
            String schedulerName = "테스트 스케줄러";
            var exception = new DuplicateSchedulerNameException(sellerId, schedulerName);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlSchedulerErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create("https://api.example.com/problems/schedule/schedule-002"));
            assertThat(result.detail()).contains("이미 존재하는 스케줄러 이름입니다");
            assertThat(result.detail()).contains("테스트 스케줄러");
        }

        @Test
        @DisplayName("DuplicateSchedulerNameException은 sellerId와 schedulerName 파라미터가 포함된 메시지로 변환된다")
        void map_DuplicateSchedulerNameException_ShouldIncludeParameters() {
            // given
            Long sellerId = 100L;
            String schedulerName = "무신사 크롤러";
            var exception = new DuplicateSchedulerNameException(sellerId, schedulerName);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlSchedulerErrorMapper.map(exception, locale);

            // then
            assertThat(result.title()).isNotNull();
            assertThat(result.detail()).contains("100");
            assertThat(result.detail()).contains("무신사 크롤러");
        }
    }

    @Nested
    @DisplayName("Type URI 생성 테스트")
    class TypeUriTests {

        @Test
        @DisplayName("Type URI는 소문자로 변환되어 생성된다")
        void map_ShouldGenerateLowercaseTypeUri() {
            // given
            var exception = new CrawlSchedulerNotFoundException(1L);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawlSchedulerErrorMapper.map(exception, locale);

            // then
            String typeUri = result.type().toString();
            assertThat(typeUri).isEqualTo("https://api.example.com/problems/schedule/schedule-001");
            assertThat(typeUri).doesNotContain("SCHEDULE");
        }
    }
}
