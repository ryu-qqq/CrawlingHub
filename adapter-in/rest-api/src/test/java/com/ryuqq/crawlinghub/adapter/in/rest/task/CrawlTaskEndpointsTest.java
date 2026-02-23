package com.ryuqq.crawlinghub.adapter.in.rest.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskEndpoints 단위 테스트
 *
 * <p>크롤 태스크 도메인 엔드포인트 상수값을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("CrawlTaskEndpoints 단위 테스트")
class CrawlTaskEndpointsTest {

    @Test
    @DisplayName("BASE 상수가 올바른 경로이다")
    void shouldHaveCorrectBaseConstant() {
        assertThat(CrawlTaskEndpoints.BASE).isEqualTo("/api/v1/crawling/tasks");
    }

    @Test
    @DisplayName("BY_ID 상수가 올바른 경로이다")
    void shouldHaveCorrectByIdConstant() {
        assertThat(CrawlTaskEndpoints.BY_ID).isEqualTo("/{id}");
    }

    @Test
    @DisplayName("RETRY 상수가 올바른 경로이다")
    void shouldHaveCorrectRetryConstant() {
        assertThat(CrawlTaskEndpoints.RETRY).isEqualTo("/{id}/retry");
    }

    @Test
    @DisplayName("생성자를 호출하면 UnsupportedOperationException이 발생한다")
    void shouldThrowExceptionWhenInstantiated() throws Exception {
        Constructor<CrawlTaskEndpoints> constructor =
                CrawlTaskEndpoints.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .cause()
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Utility class");
    }
}
