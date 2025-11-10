package com.ryuqq.crawlinghub.domain.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TaskType Enum 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("TaskType Enum 단위 테스트")
class TaskTypeTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTests {

        @Test
        @DisplayName("TaskType은 4개의 유형을 가진다")
        void shouldHaveFourTypes() {
            // When
            TaskType[] types = TaskType.values();

            // Then
            assertThat(types).hasSize(4);
            assertThat(types).containsExactly(
                TaskType.META,
                TaskType.MINI_SHOP,
                TaskType.PRODUCT_DETAIL,
                TaskType.PRODUCT_OPTION
            );
        }

        @ParameterizedTest
        @EnumSource(TaskType.class)
        @DisplayName("모든 TaskType은 priority와 description을 가진다")
        void shouldHavePriorityAndDescription(TaskType type) {
            // Then
            assertThat(type.getPriority()).isNotNegative();
            assertThat(type.getDescription()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("fromString() 테스트")
    class FromStringTests {

        @ParameterizedTest
        @ValueSource(strings = {"META", "MINI_SHOP", "PRODUCT_DETAIL", "PRODUCT_OPTION"})
        @DisplayName("유효한 문자열로 TaskType 변환 성공")
        void shouldConvertFromValidString(String typeStr) {
            // When
            TaskType type = TaskType.fromString(typeStr);

            // Then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo(typeStr);
        }

        @ParameterizedTest
        @ValueSource(strings = {" META ", "  MINI_SHOP  ", "\tPRODUCT_DETAIL\n"})
        @DisplayName("공백이 있는 문자열도 trim 후 변환 성공")
        void shouldTrimAndConvertFromStringWithWhitespace(String typeStr) {
            // When
            TaskType type = TaskType.fromString(typeStr);

            // Then
            assertThat(type).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"meta", "mini_shop", "product_detail"})
        @DisplayName("소문자 문자열도 대문자 변환 후 처리")
        void shouldConvertLowercaseString(String typeStr) {
            // When
            TaskType type = TaskType.fromString(typeStr);

            // Then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo(typeStr.toUpperCase());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("빈 문자열이나 공백 문자열 입력 시 예외 발생")
        void shouldThrowExceptionWhenBlankString(String blankStr) {
            // When & Then
            assertThatThrownBy(() -> TaskType.fromString(blankStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TaskType은 필수입니다");
        }

        @Test
        @DisplayName("null 입력 시 예외 발생")
        void shouldThrowExceptionWhenNull() {
            // When & Then
            assertThatThrownBy(() -> TaskType.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TaskType은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "UNKNOWN", "TEST", "SAMPLE"})
        @DisplayName("유효하지 않은 문자열 입력 시 예외 발생")
        void shouldThrowExceptionWhenInvalidString(String invalidStr) {
            // When & Then
            assertThatThrownBy(() -> TaskType.fromString(invalidStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 TaskType입니다");
        }
    }

    @Nested
    @DisplayName("Priority 테스트")
    class PriorityTests {

        @Test
        @DisplayName("각 유형은 고유한 priority를 가진다")
        void shouldHaveUniquePriority() {
            // When
            TaskType[] types = TaskType.values();

            // Then: 4개 유형 각각 다른 priority
            assertThat(types).extracting(TaskType::getPriority)
                .containsExactlyInAnyOrder(0, 1, 2, 3);
        }

        @Test
        @DisplayName("유형 우선순위: META(0) < MINI_SHOP(1) < PRODUCT_DETAIL(2) < PRODUCT_OPTION(3)")
        void shouldHaveCorrectPriorityOrder() {
            // Then
            assertThat(TaskType.META.getPriority()).isEqualTo(0);
            assertThat(TaskType.MINI_SHOP.getPriority()).isEqualTo(1);
            assertThat(TaskType.PRODUCT_DETAIL.getPriority()).isEqualTo(2);
            assertThat(TaskType.PRODUCT_OPTION.getPriority()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Description 테스트")
    class DescriptionTests {

        @Test
        @DisplayName("각 유형은 한글 설명을 가진다")
        void shouldHaveKoreanDescription() {
            // Then
            assertThat(TaskType.META.getDescription()).isEqualTo("메타");
            assertThat(TaskType.MINI_SHOP.getDescription()).isEqualTo("미니샵");
            assertThat(TaskType.PRODUCT_DETAIL.getDescription()).isEqualTo("상품 상세");
            assertThat(TaskType.PRODUCT_OPTION.getDescription()).isEqualTo("상품 옵션");
        }
    }
}
