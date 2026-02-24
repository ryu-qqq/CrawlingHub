package com.ryuqq.crawlinghub.application.common.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CommonCursorParams 단위 테스트
 *
 * <p>커서 기반 페이징 파라미터의 유효성 검사 및 기본값 처리 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CommonCursorParams 테스트")
class CommonCursorParamsTest {

    @Nested
    @DisplayName("생성자 compact constructor 테스트")
    class CompactConstructor {

        @Test
        @DisplayName("[성공] 빈 문자열 cursor는 null로 변환")
        void shouldConvertBlankCursorToNull() {
            // When
            CommonCursorParams params = new CommonCursorParams("   ", 10);

            // Then
            assertThat(params.cursor()).isNull();
        }

        @Test
        @DisplayName("[성공] null size는 기본값 20으로 설정")
        void shouldSetDefaultSizeWhenNull() {
            // When
            CommonCursorParams params = new CommonCursorParams("cursor-value", null);

            // Then
            assertThat(params.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] size 0은 기본값 20으로 설정")
        void shouldSetDefaultSizeWhenZero() {
            // When
            CommonCursorParams params = new CommonCursorParams(null, 0);

            // Then
            assertThat(params.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] 음수 size는 기본값 20으로 설정")
        void shouldSetDefaultSizeWhenNegative() {
            // When
            CommonCursorParams params = new CommonCursorParams(null, -5);

            // Then
            assertThat(params.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] 최대값 100 초과 size는 100으로 제한")
        void shouldCapSizeAtMaximum() {
            // When
            CommonCursorParams params = new CommonCursorParams(null, 200);

            // Then
            assertThat(params.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("[성공] 정확히 100인 size는 그대로 유지")
        void shouldKeepSizeAtExactMaximum() {
            // When
            CommonCursorParams params = new CommonCursorParams(null, 100);

            // Then
            assertThat(params.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("[성공] 유효한 cursor와 size는 그대로 유지")
        void shouldKeepValidCursorAndSize() {
            // When
            CommonCursorParams params = new CommonCursorParams("valid-cursor", 50);

            // Then
            assertThat(params.cursor()).isEqualTo("valid-cursor");
            assertThat(params.size()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethods {

        @Test
        @DisplayName("[성공] of() - cursor와 size 지정")
        void shouldCreateWithCursorAndSize() {
            // When
            CommonCursorParams params = CommonCursorParams.of("my-cursor", 30);

            // Then
            assertThat(params.cursor()).isEqualTo("my-cursor");
            assertThat(params.size()).isEqualTo(30);
        }

        @Test
        @DisplayName("[성공] first() - cursor 없이 size만 지정 (첫 페이지)")
        void shouldCreateFirstPageWithSize() {
            // When
            CommonCursorParams params = CommonCursorParams.first(15);

            // Then
            assertThat(params.cursor()).isNull();
            assertThat(params.size()).isEqualTo(15);
            assertThat(params.isFirstPage()).isTrue();
        }

        @Test
        @DisplayName("[성공] defaultPage() - 기본값으로 생성")
        void shouldCreateDefaultPage() {
            // When
            CommonCursorParams params = CommonCursorParams.defaultPage();

            // Then
            assertThat(params.cursor()).isNull();
            assertThat(params.size()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("isFirstPage() / hasCursor() 테스트")
    class PageChecks {

        @Test
        @DisplayName("[성공] cursor null이면 첫 페이지")
        void shouldBeFirstPageWhenCursorIsNull() {
            // Given
            CommonCursorParams params = new CommonCursorParams(null, 10);

            // Then
            assertThat(params.isFirstPage()).isTrue();
            assertThat(params.hasCursor()).isFalse();
        }

        @Test
        @DisplayName("[성공] cursor 있으면 첫 페이지 아님")
        void shouldNotBeFirstPageWhenCursorExists() {
            // Given
            CommonCursorParams params = new CommonCursorParams("some-cursor", 10);

            // Then
            assertThat(params.isFirstPage()).isFalse();
            assertThat(params.hasCursor()).isTrue();
        }
    }
}
