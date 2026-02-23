package com.ryuqq.crawlinghub.domain.schedule.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.vo.SortKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Tag("unit")
@Tag("domain")
@Tag("query")
@DisplayName("CrawlSchedulerSortKey 단위 테스트")
class CrawlSchedulerSortKeyTest {

    @Nested
    @DisplayName("SortKey 구현 검증")
    class SortKeyImplementation {

        @ParameterizedTest
        @EnumSource(CrawlSchedulerSortKey.class)
        @DisplayName("모든 SortKey는 fieldName을 가진다")
        void shouldHaveFieldNameForAllEnums(CrawlSchedulerSortKey sortKey) {
            // then
            assertThat(sortKey.fieldName()).isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(CrawlSchedulerSortKey.class)
        @DisplayName("모든 CrawlSchedulerSortKey는 SortKey 인터페이스를 구현한다")
        void shouldImplementSortKeyInterface(CrawlSchedulerSortKey sortKey) {
            // then
            assertThat(sortKey).isInstanceOf(SortKey.class);
        }
    }

    @Nested
    @DisplayName("개별 SortKey 값 검증")
    class IndividualSortKeyValues {

        @Test
        @DisplayName("CREATED_AT은 createdAt 필드명을 가진다")
        void createdAtHasCorrectFieldName() {
            assertThat(CrawlSchedulerSortKey.CREATED_AT.fieldName()).isEqualTo("createdAt");
        }

        @Test
        @DisplayName("UPDATED_AT은 updatedAt 필드명을 가진다")
        void updatedAtHasCorrectFieldName() {
            assertThat(CrawlSchedulerSortKey.UPDATED_AT.fieldName()).isEqualTo("updatedAt");
        }

        @Test
        @DisplayName("SCHEDULER_NAME은 schedulerName 필드명을 가진다")
        void schedulerNameHasCorrectFieldName() {
            assertThat(CrawlSchedulerSortKey.SCHEDULER_NAME.fieldName()).isEqualTo("schedulerName");
        }
    }

    @Nested
    @DisplayName("defaultKey() 메서드 테스트")
    class DefaultKeyTest {

        @Test
        @DisplayName("기본 정렬 키는 CREATED_AT이다")
        void defaultKeyIsCreatedAt() {
            assertThat(CrawlSchedulerSortKey.defaultKey())
                    .isEqualTo(CrawlSchedulerSortKey.CREATED_AT);
        }
    }

    @Nested
    @DisplayName("고유성 검증")
    class UniquenessTest {

        @Test
        @DisplayName("모든 SortKey는 고유한 fieldName을 가진다")
        void shouldHaveUniqueFieldNames() {
            // given
            CrawlSchedulerSortKey[] sortKeys = CrawlSchedulerSortKey.values();

            // when
            long uniqueCount =
                    java.util.Arrays.stream(sortKeys)
                            .map(CrawlSchedulerSortKey::fieldName)
                            .distinct()
                            .count();

            // then
            assertThat(uniqueCount).isEqualTo(sortKeys.length);
        }
    }
}
