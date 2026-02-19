package com.ryuqq.crawlinghub.adapter.in.rest.product.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import com.ryuqq.crawlinghub.domain.product.exception.CrawledProductNotFoundException;
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
 * CrawledProductErrorMapper 단위 테스트
 *
 * <p>CrawledProduct 도메인 예외 → HTTP 응답 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>PREFIX 기반 supports() 테스트
 *   <li>PRODUCT-001 예외별 HttpStatus 매핑 검증
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
@DisplayName("CrawledProductErrorMapper 단위 테스트")
class CrawledProductErrorMapperTest {

    private CrawledProductErrorMapper crawledProductErrorMapper;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        this.messageSource = messageSource;

        crawledProductErrorMapper = new CrawledProductErrorMapper(messageSource);
    }

    @Nested
    @DisplayName("supports() 테스트")
    class SupportsTests {

        @Test
        @DisplayName("PRODUCT- prefix를 가진 에러 코드는 supports()가 true를 반환한다")
        void supports_WhenProductPrefix_ShouldReturnTrue() {
            // given
            DomainException ex = createDomainException("PRODUCT-001");

            // when
            boolean result = crawledProductErrorMapper.supports(ex);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("PRODUCT- prefix가 없는 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenNonProductPrefix_ShouldReturnFalse() {
            // given
            DomainException ex = createDomainException("SCHEDULE-001");

            // when
            boolean result = crawledProductErrorMapper.supports(ex);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null 예외는 supports()가 false를 반환한다")
        void supports_WhenNull_ShouldReturnFalse() {
            // when
            boolean result = crawledProductErrorMapper.supports(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 에러 코드는 supports()가 false를 반환한다")
        void supports_WhenEmptyCode_ShouldReturnFalse() {
            // given
            DomainException ex = createDomainException("");

            // when
            boolean result = crawledProductErrorMapper.supports(ex);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("map() 테스트 - CrawledProductNotFoundException (PRODUCT-001)")
    class CrawledProductNotFoundTests {

        @Test
        @DisplayName("CrawledProductNotFoundException은 404 NOT_FOUND로 매핑된다")
        void map_CrawledProductNotFoundException_ShouldReturn404() {
            // given
            Long crawledProductId = 999L;
            var exception = new CrawledProductNotFoundException(crawledProductId);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawledProductErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.type())
                    .isEqualTo(URI.create("https://api.example.com/problems/product/product-001"));
            assertThat(result.detail()).contains("존재하지 않는 크롤링 상품입니다");
            assertThat(result.detail()).contains("999");
        }

        @Test
        @DisplayName("CrawledProductNotFoundException은 crawledProductId 파라미터가 포함된 메시지로 변환된다")
        void map_CrawledProductNotFoundException_ShouldIncludeProductId() {
            // given
            Long crawledProductId = 123L;
            var exception = new CrawledProductNotFoundException(crawledProductId);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawledProductErrorMapper.map(exception, locale);

            // then
            assertThat(result.title()).isNotNull();
            assertThat(result.detail()).contains("123");
        }
    }

    @Nested
    @DisplayName("map() 테스트 - 알 수 없는 PRODUCT 에러 코드 (기본값)")
    class UnknownProductErrorTests {

        @Test
        @DisplayName("알 수 없는 PRODUCT- 에러 코드는 400 BAD_REQUEST로 매핑된다")
        void map_UnknownProductCode_ShouldReturn400() {
            // given
            DomainException exception = createDomainException("PRODUCT-999");
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawledProductErrorMapper.map(exception, locale);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("알 수 없는 PRODUCT- 에러 코드의 Type URI도 소문자로 생성된다")
        void map_UnknownProductCode_ShouldGenerateLowercaseTypeUri() {
            // given
            DomainException exception = createDomainException("PRODUCT-999");
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawledProductErrorMapper.map(exception, locale);

            // then
            assertThat(result.type())
                    .isEqualTo(URI.create("https://api.example.com/problems/product/product-999"));
        }
    }

    @Nested
    @DisplayName("Type URI 생성 테스트")
    class TypeUriTests {

        @Test
        @DisplayName("Type URI는 소문자로 변환되어 생성된다")
        void map_ShouldGenerateLowercaseTypeUri() {
            // given
            var exception = new CrawledProductNotFoundException(1L);
            Locale locale = Locale.KOREA;

            // when
            MappedError result = crawledProductErrorMapper.map(exception, locale);

            // then
            String typeUri = result.type().toString();
            assertThat(typeUri).isEqualTo("https://api.example.com/problems/product/product-001");
            assertThat(typeUri).doesNotContain("PRODUCT-001");
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
