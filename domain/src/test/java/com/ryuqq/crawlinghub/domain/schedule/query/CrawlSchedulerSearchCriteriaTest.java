package com.ryuqq.crawlinghub.domain.schedule.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.common.vo.SortDirection;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("query")
@DisplayName("CrawlSchedulerSearchCriteria 단위 테스트")
class CrawlSchedulerSearchCriteriaTest {

    private QueryContext<CrawlSchedulerSortKey> defaultQueryContext() {
        return QueryContext.of(
                CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, PageRequest.of(0, 20));
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("모든 필드로 생성한다")
        void createWithAllFields() {
            // given
            SellerId sellerId = SellerId.of(1L);
            List<SchedulerStatus> statuses = List.of(SchedulerStatus.ACTIVE);
            QueryContext<CrawlSchedulerSortKey> queryContext = defaultQueryContext();

            // when
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            sellerId,
                            statuses,
                            CrawlSchedulerSearchField.SCHEDULER_NAME,
                            "test",
                            queryContext);

            // then
            assertThat(criteria.sellerId()).isEqualTo(sellerId);
            assertThat(criteria.statuses()).isEqualTo(statuses);
            assertThat(criteria.searchField()).isEqualTo(CrawlSchedulerSearchField.SCHEDULER_NAME);
            assertThat(criteria.searchWord()).isEqualTo("test");
            assertThat(criteria.queryContext()).isEqualTo(queryContext);
        }

        @Test
        @DisplayName("선택 필드 없이 생성한다")
        void createWithMinimalFields() {
            // when
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(null, null, null, null, defaultQueryContext());

            // then
            assertThat(criteria.sellerId()).isNull();
            assertThat(criteria.statuses()).isNull();
            assertThat(criteria.searchField()).isNull();
            assertThat(criteria.searchWord()).isNull();
        }

        @Test
        @DisplayName("queryContext가 null이면 예외가 발생한다")
        void throwWhenQueryContextIsNull() {
            assertThatThrownBy(() -> CrawlSchedulerSearchCriteria.of(null, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("queryContext");
        }

        @Test
        @DisplayName("statuses 목록은 방어적 복사된다")
        void statusesAreDefensivelyCopied() {
            // given
            List<SchedulerStatus> mutableStatuses = new java.util.ArrayList<>();
            mutableStatuses.add(SchedulerStatus.ACTIVE);

            // when
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null, mutableStatuses, null, null, defaultQueryContext());
            mutableStatuses.add(SchedulerStatus.INACTIVE);

            // then
            assertThat(criteria.statuses()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("필터 여부 확인 메서드 테스트")
    class FilterCheckTest {

        @Test
        @DisplayName("hasSellerFilter - sellerId가 있으면 true")
        void hasSellerFilterWhenSellerIdPresent() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            SellerId.of(1L), null, null, null, defaultQueryContext());

            // then
            assertThat(criteria.hasSellerFilter()).isTrue();
        }

        @Test
        @DisplayName("hasSellerFilter - sellerId가 없으면 false")
        void hasSellerFilterWhenNoSellerId() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(null, null, null, null, defaultQueryContext());

            // then
            assertThat(criteria.hasSellerFilter()).isFalse();
        }

        @Test
        @DisplayName("hasStatusFilter - statuses가 있으면 true")
        void hasStatusFilterWhenStatusesPresent() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            List.of(SchedulerStatus.ACTIVE),
                            null,
                            null,
                            defaultQueryContext());

            // then
            assertThat(criteria.hasStatusFilter()).isTrue();
        }

        @Test
        @DisplayName("hasStatusFilter - statuses가 빈 목록이면 false")
        void hasStatusFilterWhenEmptyStatuses() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null, List.of(), null, null, defaultQueryContext());

            // then
            assertThat(criteria.hasStatusFilter()).isFalse();
        }

        @Test
        @DisplayName("hasSearchCondition - searchField와 searchWord가 있으면 true")
        void hasSearchConditionWhenBothPresent() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            CrawlSchedulerSearchField.SCHEDULER_NAME,
                            "test",
                            defaultQueryContext());

            // then
            assertThat(criteria.hasSearchCondition()).isTrue();
        }

        @Test
        @DisplayName("hasSearchCondition - searchWord가 없으면 false")
        void hasSearchConditionWhenNoSearchWord() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            CrawlSchedulerSearchField.SCHEDULER_NAME,
                            null,
                            defaultQueryContext());

            // then
            assertThat(criteria.hasSearchCondition()).isFalse();
        }

        @Test
        @DisplayName("hasSearchCondition - searchWord가 빈 문자열이면 false")
        void hasSearchConditionWhenBlankSearchWord() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            CrawlSchedulerSearchField.SCHEDULER_NAME,
                            "  ",
                            defaultQueryContext());

            // then
            assertThat(criteria.hasSearchCondition()).isFalse();
        }

        @Test
        @DisplayName("hasSearchField - searchField가 있으면 true")
        void hasSearchFieldWhenPresent() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            CrawlSchedulerSearchField.SCHEDULER_NAME,
                            null,
                            defaultQueryContext());

            // then
            assertThat(criteria.hasSearchField()).isTrue();
        }
    }

    @Nested
    @DisplayName("페이징 헬퍼 메서드 테스트")
    class PagingHelperTest {

        @Test
        @DisplayName("offset()은 QueryContext에서 계산된 값을 반환한다")
        void offsetFromQueryContext() {
            // given
            QueryContext<CrawlSchedulerSortKey> queryContext =
                    QueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.DESC,
                            PageRequest.of(2, 20));
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(null, null, null, null, queryContext);

            // then
            assertThat(criteria.offset()).isEqualTo(40L);
        }

        @Test
        @DisplayName("size()는 QueryContext에서 값을 반환한다")
        void sizeFromQueryContext() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            null,
                            null,
                            QueryContext.of(
                                    CrawlSchedulerSortKey.CREATED_AT,
                                    SortDirection.DESC,
                                    PageRequest.of(0, 50)));

            // then
            assertThat(criteria.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("page()는 QueryContext에서 값을 반환한다")
        void pageFromQueryContext() {
            // given
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            null,
                            null,
                            QueryContext.of(
                                    CrawlSchedulerSortKey.CREATED_AT,
                                    SortDirection.DESC,
                                    PageRequest.of(3, 20)));

            // then
            assertThat(criteria.page()).isEqualTo(3);
        }
    }
}
