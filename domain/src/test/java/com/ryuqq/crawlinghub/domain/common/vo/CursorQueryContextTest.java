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
@DisplayName("CursorQueryContext Value Object 단위 테스트")
class CursorQueryContextTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 파라미터로 생성한다")
        void createWithValidParams() {
            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.DESC,
                            CursorPageRequest.first(20));

            // then
            assertThat(ctx.sortKey()).isEqualTo(CrawlSchedulerSortKey.CREATED_AT);
            assertThat(ctx.sortDirection()).isEqualTo(SortDirection.DESC);
            assertThat(ctx.includeDeleted()).isFalse();
        }

        @Test
        @DisplayName("includeDeleted를 포함하여 생성한다")
        void createWithIncludeDeleted() {
            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.ASC,
                            CursorPageRequest.first(20),
                            true);

            // then
            assertThat(ctx.includeDeleted()).isTrue();
        }

        @Test
        @DisplayName("defaultOf()로 기본값으로 생성한다")
        void createWithDefaultValues() {
            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.defaultOf(CrawlSchedulerSortKey.CREATED_AT);

            // then
            assertThat(ctx.sortKey()).isEqualTo(CrawlSchedulerSortKey.CREATED_AT);
            assertThat(ctx.sortDirection()).isEqualTo(SortDirection.defaultDirection());
            assertThat(ctx.includeDeleted()).isFalse();
            assertThat(ctx.isFirstPage()).isTrue();
        }

        @Test
        @DisplayName("firstPage()로 첫 페이지 CursorQueryContext를 생성한다")
        void createFirstPage() {
            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.firstPage(
                            CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, 50);

            // then
            assertThat(ctx.size()).isEqualTo(50);
            assertThat(ctx.isFirstPage()).isTrue();
        }

        @Test
        @DisplayName("sortKey가 null이면 예외가 발생한다")
        void throwWhenSortKeyIsNull() {
            assertThatThrownBy(
                            () ->
                                    CursorQueryContext.of(
                                            null,
                                            SortDirection.DESC,
                                            CursorPageRequest.defaultPage()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sortKey");
        }

        @Test
        @DisplayName("sortDirection이 null이면 기본값으로 대체된다")
        void useDefaultSortDirectionWhenNull() {
            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            null,
                            CursorPageRequest.defaultPage());

            // then
            assertThat(ctx.sortDirection()).isEqualTo(SortDirection.defaultDirection());
        }

        @Test
        @DisplayName("cursorPageRequest가 null이면 기본값으로 대체된다")
        void useDefaultCursorPageRequestWhenNull() {
            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, null);

            // then
            assertThat(ctx.cursorPageRequest()).isNotNull();
            assertThat(ctx.isFirstPage()).isTrue();
        }
    }

    @Nested
    @DisplayName("커서 탐색 메서드 테스트")
    class NavigationTest {

        @Test
        @DisplayName("nextPage()로 다음 커서로 이동한다")
        void nextPage() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.DESC,
                            CursorPageRequest.first(20));

            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> nextCtx = ctx.nextPage(12345L);

            // then
            assertThat(nextCtx.cursor()).isEqualTo(12345L);
            assertThat(nextCtx.hasCursor()).isTrue();
        }

        @Test
        @DisplayName("reverseSortDirection()으로 정렬 방향을 반전한다")
        void reverseSortDirection() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.DESC,
                            CursorPageRequest.defaultPage());

            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> reversed = ctx.reverseSortDirection();

            // then
            assertThat(reversed.sortDirection()).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("withSortKey()로 정렬 키를 변경한다")
        void withSortKey() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.DESC,
                            CursorPageRequest.defaultPage());

            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> newCtx =
                    ctx.withSortKey(CrawlSchedulerSortKey.SCHEDULER_NAME);

            // then
            assertThat(newCtx.sortKey()).isEqualTo(CrawlSchedulerSortKey.SCHEDULER_NAME);
        }

        @Test
        @DisplayName("withPageSize()로 페이지 크기를 변경한다")
        void withPageSize() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.DESC,
                            CursorPageRequest.first(20));

            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> newCtx = ctx.withPageSize(50);

            // then
            assertThat(newCtx.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("withIncludeDeleted()로 삭제 포함 여부를 변경한다")
        void withIncludeDeleted() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.defaultOf(CrawlSchedulerSortKey.CREATED_AT);

            // when
            CursorQueryContext<CrawlSchedulerSortKey, Long> newCtx = ctx.withIncludeDeleted(true);

            // then
            assertThat(newCtx.includeDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("헬퍼 메서드 테스트")
    class HelperMethodTest {

        @Test
        @DisplayName("isFirstPage() - 첫 페이지이면 true")
        void isFirstPage() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.defaultOf(CrawlSchedulerSortKey.CREATED_AT);

            // then
            assertThat(ctx.isFirstPage()).isTrue();
        }

        @Test
        @DisplayName("hasCursor() - 커서가 있으면 true")
        void hasCursor() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.DESC,
                            CursorPageRequest.afterId(100L, 20));

            // then
            assertThat(ctx.hasCursor()).isTrue();
        }

        @Test
        @DisplayName("cursor() - 커서 값을 반환한다")
        void cursor() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.DESC,
                            CursorPageRequest.afterId(999L, 20));

            // then
            assertThat(ctx.cursor()).isEqualTo(999L);
        }

        @Test
        @DisplayName("fetchSize()는 size+1을 반환한다")
        void fetchSize() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.firstPage(
                            CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, 20);

            // then
            assertThat(ctx.fetchSize()).isEqualTo(21);
        }

        @Test
        @DisplayName("isAscending() - ASC이면 true")
        void isAscending() {
            // given
            CursorQueryContext<CrawlSchedulerSortKey, Long> ctx =
                    CursorQueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT,
                            SortDirection.ASC,
                            CursorPageRequest.defaultPage());

            // then
            assertThat(ctx.isAscending()).isTrue();
        }
    }
}
