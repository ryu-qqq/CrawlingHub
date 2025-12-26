package com.ryuqq.crawlinghub.domain.product.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CrawledProductNotFoundException 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawledProductNotFoundException 테스트")
class CrawledProductNotFoundExceptionTest {

    @Test
    @DisplayName("CrawledProduct ID로 예외 생성")
    void shouldCreateWithCrawledProductId() {
        // given
        long crawledProductId = 12345L;

        // when
        CrawledProductNotFoundException exception =
                new CrawledProductNotFoundException(crawledProductId);

        // then
        assertThat(exception.code()).isEqualTo("PRODUCT-001");
        assertThat(exception.getMessage()).contains("존재하지 않는 크롤링 상품");
        assertThat(exception.getMessage()).contains(String.valueOf(crawledProductId));
        assertThat(exception.args()).containsEntry("crawledProductId", crawledProductId);
    }

    @Test
    @DisplayName("예외는 404 HTTP 상태를 가짐")
    void shouldHave404HttpStatus() {
        // given
        long crawledProductId = 1L;

        // when
        CrawledProductNotFoundException exception =
                new CrawledProductNotFoundException(crawledProductId);

        // then
        assertThat(exception.httpStatus()).isEqualTo(404);
    }
}
