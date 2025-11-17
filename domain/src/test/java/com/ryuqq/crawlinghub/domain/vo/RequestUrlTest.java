package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.exception.InvalidRequestUrlException;
import com.ryuqq.crawlinghub.domain.crawler.vo.CrawlerTaskType;
import com.ryuqq.crawlinghub.domain.crawler.vo.RequestUrl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestUrlTest {

    @Test
    void shouldCreateRequestUrlWithValidMinishopUrl() {
        // Given
        String validUrl = "https://m.mustit.co.kr/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123";
        CrawlerTaskType taskType = CrawlerTaskType.MINISHOP;

        // When
        RequestUrl requestUrl = new RequestUrl(validUrl, taskType);

        // Then
        assertThat(requestUrl.value()).isEqualTo(validUrl);
    }

    @Test
    void shouldThrowExceptionWhenMinishopUrlInvalid() {
        // Given
        String invalidUrl = "https://invalid.com/wrong-path";

        // When & Then
        assertThatThrownBy(() -> new RequestUrl(invalidUrl, CrawlerTaskType.MINISHOP))
            .isInstanceOf(InvalidRequestUrlException.class)
            .hasMessageContaining("MINISHOP URL은 /searchmini-shop-search 패턴을 포함해야 합니다");
    }

    @ParameterizedTest
    @CsvSource({
        "https://m.mustit.co.kr/mustit-api/facade-api/v1/item/12345/detail/top, PRODUCT_DETAIL",
        "https://m.mustit.co.kr/mustit-api/legacy-api/v1/auction_products/12345/options, PRODUCT_OPTION"
    })
    void shouldValidateUrlByTaskType(String url, CrawlerTaskType taskType) {
        // When & Then
        assertThatCode(() -> new RequestUrl(url, taskType))
            .doesNotThrowAnyException();
    }
}
