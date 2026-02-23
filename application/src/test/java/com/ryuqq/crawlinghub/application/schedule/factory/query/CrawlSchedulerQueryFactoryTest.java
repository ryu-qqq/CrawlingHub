package com.ryuqq.crawlinghub.application.schedule.factory.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import com.ryuqq.crawlinghub.application.common.dto.query.CommonSearchParams;
import com.ryuqq.crawlinghub.application.common.factory.CommonVoFactory;
import com.ryuqq.crawlinghub.application.schedule.dto.query.CrawlSchedulerSearchParams;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.common.vo.SortDirection;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchField;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSortKey;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@Tag("application")
@Tag("factory")
@DisplayName("CrawlSchedulerQueryFactory 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlSchedulerQueryFactoryTest {

    @Mock private CommonVoFactory commonVoFactory;

    private CrawlSchedulerQueryFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CrawlSchedulerQueryFactory(commonVoFactory);
    }

    @Nested
    @DisplayName("createCriteria() 메서드는")
    class CreateCriteriaMethod {

        @Test
        @DisplayName("모든 필드가 있는 Params를 Criteria로 변환한다")
        void shouldConvertParamsWithAllFields() {
            // Given
            CrawlSchedulerSearchParams params =
                    CrawlSchedulerSearchParams.of(
                            100L,
                            List.of("ACTIVE"),
                            "schedulerName",
                            "daily",
                            CommonSearchParams.of(null, null, null, "createdAt", "DESC", 1, 20));

            PageRequest pageRequest = PageRequest.of(1, 20);
            QueryContext<CrawlSchedulerSortKey> queryContext =
                    QueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, pageRequest);

            given(commonVoFactory.parseSortDirection("DESC")).willReturn(SortDirection.DESC);
            given(commonVoFactory.createPageRequest(1, 20)).willReturn(pageRequest);
            given(
                            commonVoFactory.createQueryContext(
                                    eq(CrawlSchedulerSortKey.CREATED_AT),
                                    eq(SortDirection.DESC),
                                    eq(pageRequest)))
                    .willReturn(queryContext);

            // When
            CrawlSchedulerSearchCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.sellerId()).isNotNull();
            assertThat(criteria.sellerId().value()).isEqualTo(100L);
            assertThat(criteria.statuses()).containsExactly(SchedulerStatus.ACTIVE);
            assertThat(criteria.searchField()).isEqualTo(CrawlSchedulerSearchField.SCHEDULER_NAME);
            assertThat(criteria.searchWord()).isEqualTo("daily");
            assertThat(criteria.queryContext()).isEqualTo(queryContext);
        }

        @Test
        @DisplayName("sellerId가 null이면 Criteria에도 null로 설정한다")
        void shouldSetNullSellerIdWhenParamsHasNull() {
            // Given
            CrawlSchedulerSearchParams params =
                    CrawlSchedulerSearchParams.of(
                            null,
                            List.of("ACTIVE"),
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 1, 10));

            PageRequest pageRequest = PageRequest.of(1, 10);
            QueryContext<CrawlSchedulerSortKey> queryContext =
                    QueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, pageRequest);

            given(commonVoFactory.parseSortDirection(any())).willReturn(SortDirection.DESC);
            given(commonVoFactory.createPageRequest(1, 10)).willReturn(pageRequest);
            doReturn(queryContext).when(commonVoFactory).createQueryContext(any(), any(), any());

            // When
            CrawlSchedulerSearchCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.sellerId()).isNull();
            assertThat(criteria.statuses()).containsExactly(SchedulerStatus.ACTIVE);
        }

        @Test
        @DisplayName("status가 null이어도 Criteria로 변환한다")
        void shouldConvertParamsWithNullStatus() {
            // Given
            CrawlSchedulerSearchParams params =
                    CrawlSchedulerSearchParams.of(
                            100L,
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 2, 50));

            PageRequest pageRequest = PageRequest.of(2, 50);
            QueryContext<CrawlSchedulerSortKey> queryContext =
                    QueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, pageRequest);

            given(commonVoFactory.parseSortDirection(any())).willReturn(SortDirection.DESC);
            given(commonVoFactory.createPageRequest(2, 50)).willReturn(pageRequest);
            doReturn(queryContext).when(commonVoFactory).createQueryContext(any(), any(), any());

            // When
            CrawlSchedulerSearchCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.sellerId()).isNotNull();
            assertThat(criteria.sellerId().value()).isEqualTo(100L);
            assertThat(criteria.statuses()).isNull();
        }

        @Test
        @DisplayName("INACTIVE 상태로 필터링할 수 있다")
        void shouldConvertParamsWithInactiveStatus() {
            // Given
            CrawlSchedulerSearchParams params =
                    CrawlSchedulerSearchParams.of(
                            200L,
                            List.of("INACTIVE"),
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 0, 10));

            PageRequest pageRequest = PageRequest.of(0, 10);
            QueryContext<CrawlSchedulerSortKey> queryContext =
                    QueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, pageRequest);

            given(commonVoFactory.parseSortDirection(any())).willReturn(SortDirection.DESC);
            given(commonVoFactory.createPageRequest(0, 10)).willReturn(pageRequest);
            doReturn(queryContext).when(commonVoFactory).createQueryContext(any(), any(), any());

            // When
            CrawlSchedulerSearchCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.statuses()).containsExactly(SchedulerStatus.INACTIVE);
        }

        @Test
        @DisplayName("모든 검색 조건이 null이어도 페이지네이션은 유지한다")
        void shouldKeepPaginationWhenAllSearchConditionsAreNull() {
            // Given
            CrawlSchedulerSearchParams params =
                    CrawlSchedulerSearchParams.of(
                            null,
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 5, 50));

            PageRequest pageRequest = PageRequest.of(5, 50);
            QueryContext<CrawlSchedulerSortKey> queryContext =
                    QueryContext.of(
                            CrawlSchedulerSortKey.CREATED_AT, SortDirection.DESC, pageRequest);

            given(commonVoFactory.parseSortDirection(any())).willReturn(SortDirection.DESC);
            given(commonVoFactory.createPageRequest(5, 50)).willReturn(pageRequest);
            doReturn(queryContext).when(commonVoFactory).createQueryContext(any(), any(), any());

            // When
            CrawlSchedulerSearchCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.sellerId()).isNull();
            assertThat(criteria.statuses()).isNull();
            assertThat(criteria.queryContext().page()).isEqualTo(5);
            assertThat(criteria.queryContext().size()).isEqualTo(50);
        }

        @Test
        @DisplayName("sortKey 문자열을 CrawlSchedulerSortKey로 변환한다")
        void shouldResolveSortKeyFromString() {
            // Given
            CrawlSchedulerSearchParams params =
                    CrawlSchedulerSearchParams.of(
                            null,
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, "schedulerName", "ASC", 0, 10));

            PageRequest pageRequest = PageRequest.of(0, 10);
            QueryContext<CrawlSchedulerSortKey> queryContext =
                    QueryContext.of(
                            CrawlSchedulerSortKey.SCHEDULER_NAME, SortDirection.ASC, pageRequest);

            given(commonVoFactory.parseSortDirection("ASC")).willReturn(SortDirection.ASC);
            given(commonVoFactory.createPageRequest(0, 10)).willReturn(pageRequest);
            given(
                            commonVoFactory.createQueryContext(
                                    eq(CrawlSchedulerSortKey.SCHEDULER_NAME),
                                    eq(SortDirection.ASC),
                                    eq(pageRequest)))
                    .willReturn(queryContext);

            // When
            CrawlSchedulerSearchCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.queryContext().sortKey())
                    .isEqualTo(CrawlSchedulerSortKey.SCHEDULER_NAME);
        }
    }
}
