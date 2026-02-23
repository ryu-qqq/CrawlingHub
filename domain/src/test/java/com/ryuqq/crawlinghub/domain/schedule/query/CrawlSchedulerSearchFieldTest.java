package com.ryuqq.crawlinghub.domain.schedule.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.vo.SearchField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
@Tag("domain")
@Tag("query")
@DisplayName("CrawlSchedulerSearchField 단위 테스트")
class CrawlSchedulerSearchFieldTest {

    @Nested
    @DisplayName("SearchField 구현 검증")
    class SearchFieldImplementation {

        @ParameterizedTest
        @EnumSource(CrawlSchedulerSearchField.class)
        @DisplayName("모든 SearchField는 fieldName을 가진다")
        void shouldHaveFieldNameForAllEnums(CrawlSchedulerSearchField searchField) {
            // then
            assertThat(searchField.fieldName()).isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(CrawlSchedulerSearchField.class)
        @DisplayName("모든 CrawlSchedulerSearchField는 SearchField 인터페이스를 구현한다")
        void shouldImplementSearchFieldInterface(CrawlSchedulerSearchField searchField) {
            // then
            assertThat(searchField).isInstanceOf(SearchField.class);
        }
    }

    @Nested
    @DisplayName("개별 SearchField 값 검증")
    class IndividualSearchFieldValues {

        @Test
        @DisplayName("SCHEDULER_NAME은 schedulerName 필드명을 가진다")
        void schedulerNameHasCorrectFieldName() {
            assertThat(CrawlSchedulerSearchField.SCHEDULER_NAME.fieldName())
                    .isEqualTo("schedulerName");
        }
    }

    @Nested
    @DisplayName("fromString() 메서드 테스트")
    class FromStringTest {

        @Test
        @DisplayName("fieldName으로 검색 필드를 파싱한다")
        void parseByFieldName() {
            // when
            CrawlSchedulerSearchField result =
                    CrawlSchedulerSearchField.fromString("schedulerName");

            // then
            assertThat(result).isEqualTo(CrawlSchedulerSearchField.SCHEDULER_NAME);
        }

        @Test
        @DisplayName("enum 이름으로 검색 필드를 파싱한다")
        void parseByEnumName() {
            // when
            CrawlSchedulerSearchField result =
                    CrawlSchedulerSearchField.fromString("SCHEDULER_NAME");

            // then
            assertThat(result).isEqualTo(CrawlSchedulerSearchField.SCHEDULER_NAME);
        }

        @Test
        @DisplayName("대소문자 무관하게 fieldName으로 파싱한다")
        void parseCaseInsensitiveByFieldName() {
            // "schedulerName"과 equalsIgnoreCase로 비교되므로 "SCHEDULERNAME"은 매칭됨
            // when
            CrawlSchedulerSearchField result =
                    CrawlSchedulerSearchField.fromString("SCHEDULERNAME");

            // then - "SCHEDULERNAME".equalsIgnoreCase("schedulerName") == true
            assertThat(result).isEqualTo(CrawlSchedulerSearchField.SCHEDULER_NAME);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "unknown"})
        @DisplayName("null이거나 매칭 없으면 null을 반환한다")
        void returnNullForUnknownValues(String value) {
            // when
            CrawlSchedulerSearchField result = CrawlSchedulerSearchField.fromString(value);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null을 입력하면 null을 반환한다")
        void returnNullForNullInput() {
            // when
            CrawlSchedulerSearchField result = CrawlSchedulerSearchField.fromString(null);

            // then
            assertThat(result).isNull();
        }
    }
}
