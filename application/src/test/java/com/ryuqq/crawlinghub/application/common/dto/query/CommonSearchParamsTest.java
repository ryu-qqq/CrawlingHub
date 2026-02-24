package com.ryuqq.crawlinghub.application.common.dto.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CommonSearchParams 단위 테스트
 *
 * <p>Compact Constructor 검증 및 null 방어 로직 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CommonSearchParams 테스트")
class CommonSearchParamsTest {

    @Nested
    @DisplayName("Compact Constructor 기본값 적용 테스트")
    class CompactConstructor {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            CommonSearchParams params =
                    new CommonSearchParams(
                            false,
                            LocalDate.of(2025, 1, 1),
                            LocalDate.of(2025, 12, 31),
                            "createdAt",
                            "ASC",
                            0,
                            10);

            assertThat(params.includeDeleted()).isFalse();
            assertThat(params.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
            assertThat(params.endDate()).isEqualTo(LocalDate.of(2025, 12, 31));
            assertThat(params.sortKey()).isEqualTo("createdAt");
            assertThat(params.sortDirection()).isEqualTo("ASC");
            assertThat(params.page()).isZero();
            assertThat(params.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("[성공] includeDeleted가 null이면 false로 기본값 적용")
        void shouldDefaultIncludeDeletedToFalse() {
            CommonSearchParams params =
                    new CommonSearchParams(null, null, null, "createdAt", "DESC", 0, 10);
            assertThat(params.includeDeleted()).isFalse();
        }

        @Test
        @DisplayName("[성공] sortKey가 null이면 createdAt으로 기본값 적용")
        void shouldDefaultSortKeyToCreatedAt() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, null, "DESC", 0, 10);
            assertThat(params.sortKey()).isEqualTo("createdAt");
        }

        @Test
        @DisplayName("[성공] sortKey가 빈 문자열이면 createdAt으로 기본값 적용")
        void shouldDefaultSortKeyWhenBlank() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, "  ", "DESC", 0, 10);
            assertThat(params.sortKey()).isEqualTo("createdAt");
        }

        @Test
        @DisplayName("[성공] sortDirection이 null이면 DESC로 기본값 적용")
        void shouldDefaultSortDirectionToDesc() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, "createdAt", null, 0, 10);
            assertThat(params.sortDirection()).isEqualTo("DESC");
        }

        @Test
        @DisplayName("[성공] page가 null이면 0으로 기본값 적용")
        void shouldDefaultPageToZero() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, "createdAt", "DESC", null, 10);
            assertThat(params.page()).isZero();
        }

        @Test
        @DisplayName("[성공] page가 음수이면 0으로 기본값 적용")
        void shouldDefaultPageWhenNegative() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, "createdAt", "DESC", -1, 10);
            assertThat(params.page()).isZero();
        }

        @Test
        @DisplayName("[성공] size가 null이면 20으로 기본값 적용")
        void shouldDefaultSizeToTwenty() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, "createdAt", "DESC", 0, null);
            assertThat(params.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] size가 0이면 20으로 기본값 적용")
        void shouldDefaultSizeWhenZero() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, "createdAt", "DESC", 0, 0);
            assertThat(params.size()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class Of {

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            CommonSearchParams params =
                    CommonSearchParams.of(false, null, null, "createdAt", "DESC", 0, 10);
            assertThat(params.page()).isZero();
            assertThat(params.size()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("toQueryContext() 메서드 테스트")
    class ToQueryContext {

        @Test
        @DisplayName("[성공] SortKey enum 클래스로 변환 - 일치하는 키")
        void shouldConvertToQueryContextWithMatchingKey() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, "createdAt", "DESC", 0, 10);

            var queryContext = params.toQueryContext(TestSortKey.class);
            assertThat(queryContext).isNotNull();
        }

        @Test
        @DisplayName("[성공] SortKey enum에 없는 키이면 첫 번째 상수 반환")
        void shouldReturnFirstConstantWhenKeyNotFound() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, "unknownKey", "DESC", 0, 10);

            var queryContext = params.toQueryContext(TestSortKey.class);
            assertThat(queryContext).isNotNull();
        }

        @Test
        @DisplayName("[실패] SortKey 클래스가 enum이 아니면 예외")
        void shouldThrowWhenNotEnumClass() {
            CommonSearchParams params =
                    new CommonSearchParams(false, null, null, "createdAt", "DESC", 0, 10);

            assertThatThrownBy(() -> params.toQueryContext(NotEnumClass.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SortKey class must be an enum");
        }
    }

    // ─── 테스트용 SortKey enum ───────────────────────────────

    enum TestSortKey implements com.ryuqq.crawlinghub.domain.common.vo.SortKey {
        CREATED_AT("createdAt"),
        UPDATED_AT("updatedAt");

        private final String field;

        TestSortKey(String field) {
            this.field = field;
        }

        @Override
        public String fieldName() {
            return field;
        }
    }

    static class NotEnumClass implements com.ryuqq.crawlinghub.domain.common.vo.SortKey {
        @Override
        public String fieldName() {
            return "field";
        }
    }
}
