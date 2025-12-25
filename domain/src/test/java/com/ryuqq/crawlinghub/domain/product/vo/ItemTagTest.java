package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ItemTag VO 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ItemTag 테스트")
class ItemTagTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 값으로 ItemTag 생성")
        void shouldCreateWithValidValues() {
            // given & when
            ItemTag tag = ItemTag.of("무료배송", "#888888", "#ffffff", "#dddddd");

            // then
            assertThat(tag.title()).isEqualTo("무료배송");
            assertThat(tag.textColor()).isEqualTo("#888888");
            assertThat(tag.bgColor()).isEqualTo("#ffffff");
            assertThat(tag.borderColor()).isEqualTo("#dddddd");
        }

        @Test
        @DisplayName("제목만으로 ItemTag 생성")
        void shouldCreateWithTitleOnly() {
            // given & when
            ItemTag tag = ItemTag.ofTitle("할인");

            // then
            assertThat(tag.title()).isEqualTo("할인");
            assertThat(tag.textColor()).isNull();
            assertThat(tag.bgColor()).isNull();
            assertThat(tag.borderColor()).isNull();
        }

        @Test
        @DisplayName("title이 null이면 예외 발생")
        void shouldThrowWhenTitleIsNull() {
            // given & when & then
            assertThatThrownBy(() -> ItemTag.of(null, "#888888", "#ffffff", "#dddddd"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("태그 title은 필수");
        }

        @Test
        @DisplayName("title이 빈 문자열이면 예외 발생")
        void shouldThrowWhenTitleIsBlank() {
            // given & when & then
            assertThatThrownBy(() -> ItemTag.of("  ", "#888888", "#ffffff", "#dddddd"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("태그 title은 필수");
        }
    }
}
