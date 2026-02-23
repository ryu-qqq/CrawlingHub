package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("ProductCategory Value Object 단위 테스트")
class ProductCategoryTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("모든 필드로 ProductCategory를 생성한다")
        void createWithAllFields() {
            ProductCategory category = ProductCategory.of("W", "여성", "001", "가방", "A01", "백팩");

            assertThat(category.headerCategoryCode()).isEqualTo("W");
            assertThat(category.headerCategoryName()).isEqualTo("여성");
            assertThat(category.largeCategoryCode()).isEqualTo("001");
            assertThat(category.largeCategoryName()).isEqualTo("가방");
            assertThat(category.mediumCategoryCode()).isEqualTo("A01");
            assertThat(category.mediumCategoryName()).isEqualTo("백팩");
        }
    }

    @Nested
    @DisplayName("getFullPath() 테스트")
    class GetFullPathTest {

        @Test
        @DisplayName("대분류 > 중분류 > 소분류 형식의 전체 경로를 반환한다")
        void returnsFullCategoryPath() {
            ProductCategory category = ProductCategory.of("W", "여성", "001", "가방", "A01", "백팩");

            assertThat(category.getFullPath()).isEqualTo("여성 > 가방 > 백팩");
        }

        @Test
        @DisplayName("일부 필드가 null이면 null 필드를 제외하고 경로를 반환한다")
        void returnsPartialPathWhenSomeFieldsAreNull() {
            ProductCategory category = ProductCategory.of("W", "여성", "001", null, "A01", null);

            assertThat(category.getFullPath()).isEqualTo("여성");
        }

        @Test
        @DisplayName("모든 이름 필드가 null이면 빈 문자열을 반환한다")
        void returnsEmptyStringWhenAllNameFieldsAreNull() {
            ProductCategory category = ProductCategory.of("W", null, "001", null, "A01", null);

            assertThat(category.getFullPath()).isEmpty();
        }
    }

    @Nested
    @DisplayName("카테고리 판별 메서드 테스트")
    class CategoryCheckTest {

        @Test
        @DisplayName("headerCategoryCode가 W이면 여성 카테고리이다")
        void wCodeIsWomen() {
            ProductCategory category = ProductCategory.of("W", "여성", null, null, null, null);

            assertThat(category.isWomen()).isTrue();
            assertThat(category.isMen()).isFalse();
            assertThat(category.isKids()).isFalse();
        }

        @Test
        @DisplayName("headerCategoryCode가 M이면 남성 카테고리이다")
        void mCodeIsMen() {
            ProductCategory category = ProductCategory.of("M", "남성", null, null, null, null);

            assertThat(category.isMen()).isTrue();
            assertThat(category.isWomen()).isFalse();
            assertThat(category.isKids()).isFalse();
        }

        @Test
        @DisplayName("headerCategoryCode가 K이면 키즈 카테고리이다")
        void kCodeIsKids() {
            ProductCategory category = ProductCategory.of("K", "키즈", null, null, null, null);

            assertThat(category.isKids()).isTrue();
            assertThat(category.isWomen()).isFalse();
            assertThat(category.isMen()).isFalse();
        }
    }

    @Nested
    @DisplayName("mediumCategory() 별칭 메서드 테스트")
    class MediumCategoryTest {

        @Test
        @DisplayName("mediumCategoryName을 반환한다")
        void returnsMediumCategoryName() {
            ProductCategory category = ProductCategory.of("W", "여성", "001", "가방", "A01", "백팩");

            assertThat(category.mediumCategory()).isEqualTo("백팩");
        }

        @Test
        @DisplayName("mediumCategoryName이 null이면 null을 반환한다")
        void returnsNullWhenMediumCategoryNameIsNull() {
            ProductCategory category = ProductCategory.of("W", "여성", "001", "가방", "A01", null);

            assertThat(category.mediumCategory()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 필드이면 동일하다")
        void sameFieldsAreEqual() {
            ProductCategory category1 = ProductCategory.of("W", "여성", "001", "가방", "A01", "백팩");
            ProductCategory category2 = ProductCategory.of("W", "여성", "001", "가방", "A01", "백팩");

            assertThat(category1).isEqualTo(category2);
            assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
        }

        @Test
        @DisplayName("다른 필드이면 다르다")
        void differentFieldsAreNotEqual() {
            ProductCategory category1 = ProductCategory.of("W", "여성", "001", "가방", "A01", "백팩");
            ProductCategory category2 = ProductCategory.of("M", "남성", "002", "신발", "B01", "스니커즈");

            assertThat(category1).isNotEqualTo(category2);
        }
    }
}
