package com.ryuqq.crawlinghub.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSortKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("QueryContext Value Object 단위 테스트")
class QueryContextTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 파라미터로 생성한다")
        void createWithValidParams() {
            // when
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT,
                    SortDirection.DESC,
                    PageRequest.of(0, 20));

            // then
            assertThat(ctx.sortKey()).isEqualTo(CrawlSchedulerSortKey.CREATED_AT);
            assertThat(ctx.sortDirection()).isEqualTo(SortDirection.DESC);
            assertThat(ctx.pageRequest()).isEqualTo(PageRequest.of(0, 20));
            assertThat(ctx.includeDeleted()).isFalse();
        }

        @Test
        @DisplayName("includeDeleted를 포함하여 생성한다")
        void createWithIncludeDeleted() {
            // when
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT,
                    SortDirection.ASC,
                    PageRequest.of(0, 20),
                    true);

            // then
            assertThat(ctx.includeDeleted()).isTrue();
        }

        @Test
        @DisplayName("defaultOf()로 기본값으로 생성한다")
        void createWithDefaultValues() {
            // when
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.defaultOf(CrawlSchedulerSortKey.CREATED_AT);

            // then
            assertThat(ctx.sortKey()).isEqualTo(CrawlSchedulerSortKey.CREATED_AT);
            assertThat(ctx.sortDirection()).isEqualTo(SortDirection.defaultDirection());
            assertThat(ctx.includeDeleted()).isFalse();
            assertThat(ctx.page()).isEqualTo(0);
        }

        @Test
        @DisplayName("firstPage()로 첫 페이지 QueryContext를 생성한다")
        void createFirstPage() {
            // when
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.firstPage(
                    CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, 50);

            // then
            assertThat(ctx.page()).isEqualTo(0);
            assertThat(ctx.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("sortKey가 null이면 예외가 발생한다")
        void throwWhenSortKeyIsNull() {
            assertThatThrownBy(() -> QueryContext.of(null, SortDirection.DESC, PageRequest.defaultPage()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sortKey");
        }

        @Test
        @DisplayName("sortDirection이 null이면 기본값으로 대체된다")
        void useDefaultSortDirectionWhenNull() {
            // when
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT, null, PageRequest.defaultPage());

            // then
            assertThat(ctx.sortDirection()).isEqualTo(SortDirection.defaultDirection());
        }

        @Test
        @DisplayName("pageRequest가 null이면 기본값으로 대체된다")
        void useDefaultPageRequestWhenNull() {
            // when
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, null);

            // then
            assertThat(ctx.pageRequest()).isEqualTo(PageRequest.defaultPage());
        }
    }

    @Nested
    @DisplayName("페이지 탐색 메서드 테스트")
    class NavigationTest {

        @Test
        @DisplayName("nextPage()로 다음 페이지 QueryContext를 생성한다")
        void nextPage() {
            // given
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT,
                    SortDirection.DESC,
                    PageRequest.of(0, 20));

            // when
            QueryContext<CrawlSchedulerSortKey> nextCtx = ctx.nextPage();

            // then
            assertThat(nextCtx.page()).isEqualTo(1);
            assertThat(nextCtx.sortKey()).isEqualTo(CrawlSchedulerSortKey.CREATED_AT);
        }

        @Test
        @DisplayName("previousPage()로 이전 페이지 QueryContext를 생성한다")
        void previousPage() {
            // given
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT,
                    SortDirection.DESC,
                    PageRequest.of(2, 20));

            // when
            QueryContext<CrawlSchedulerSortKey> prevCtx = ctx.previousPage();

            // then
            assertThat(prevCtx.page()).isEqualTo(1);
        }

        @Test
        @DisplayName("reverseSortDirection()으로 정렬 방향을 반전한다")
        void reverseSortDirection() {
            // given
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT,
                    SortDirection.DESC,
                    PageRequest.defaultPage());

            // when
            QueryContext<CrawlSchedulerSortKey> reversed = ctx.reverseSortDirection();

            // then
            assertThat(reversed.sortDirection()).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("withSortKey()로 정렬 키를 변경한다")
        void withSortKey() {
            // given
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT,
                    SortDirection.DESC,
                    PageRequest.defaultPage());

            // when
            QueryContext<CrawlSchedulerSortKey> newCtx = ctx.withSortKey(CrawlSchedulerSortKey.SCHEDULER_NAME);

            // then
            assertThat(newCtx.sortKey()).isEqualTo(CrawlSchedulerSortKey.SCHEDULER_NAME);
        }

        @Test
        @DisplayName("withPageSize()로 페이지 크기를 변경한다")
        void withPageSize() {
            // given
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT,
                    SortDirection.DESC,
                    PageRequest.of(0, 20));

            // when
            QueryContext<CrawlSchedulerSortKey> newCtx = ctx.withPageSize(50);

            // then
            assertThat(newCtx.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("withIncludeDeleted()로 삭제 포함 여부를 변경한다")
        void withIncludeDeleted() {
            // given
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.defaultOf(CrawlSchedulerSortKey.CREATED_AT);

            // when
            QueryContext<CrawlSchedulerSortKey> newCtx = ctx.withIncludeDeleted(true);

            // then
            assertThat(newCtx.includeDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("헬퍼 메서드 테스트")
    class HelperMethodTest {

        @Test
        @DisplayName("offset()은 page * size를 반환한다")
        void offsetCalculation() {
            // given
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT,
                    SortDirection.DESC,
                    PageRequest.of(2, 20));

            // then
            assertThat(ctx.offset()).isEqualTo(40L);
        }

        @Test
        @DisplayName("isFirstPage()는 0 페이지이면 true")
        void isFirstPage() {
            // given
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.defaultOf(CrawlSchedulerSortKey.CREATED_AT);

            // then
            assertThat(ctx.isFirstPage()).isTrue();
        }

        @Test
        @DisplayName("isAscending()은 ASC이면 true")
        void isAscending() {
            // given
            QueryContext<CrawlSchedulerSortKey> ctx = QueryContext.of(
                    CrawlSchedulerSortKey.CREATED_AT,
                    SortDirection.ASC,
                    PageRequest.defaultPage());

            // then
            assertThat(ctx.isAscending()).isTrue();
        }
    }
}
