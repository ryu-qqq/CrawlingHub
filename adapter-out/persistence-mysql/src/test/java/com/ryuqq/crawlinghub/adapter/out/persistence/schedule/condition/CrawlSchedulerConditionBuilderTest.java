package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.condition;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchField;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSortKey;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlSchedulerConditionBuilder 단위 테스트
 *
 * <p>검색 조건 빌더의 각 메서드에 대한 조건 생성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("condition")
@DisplayName("CrawlSchedulerConditionBuilder 단위 테스트")
class CrawlSchedulerConditionBuilderTest {

    private CrawlSchedulerConditionBuilder conditionBuilder;

    @BeforeEach
    void setUp() {
        conditionBuilder = new CrawlSchedulerConditionBuilder();
    }

    /** 기본 QueryContext 생성 헬퍼 */
    private QueryContext<CrawlSchedulerSortKey> defaultQueryContext() {
        return QueryContext.of(CrawlSchedulerSortKey.CREATED_AT, null, PageRequest.of(0, 10));
    }

    @Nested
    @DisplayName("sellerIdEq 메서드 테스트")
    class SellerIdEqTests {

        @Test
        @DisplayName("sellerId가 있을 때 BooleanExpression 반환")
        void shouldReturnExpressionWhenSellerIdExists() {
            // Given - 셀러 ID가 있는 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            SellerId.of(1L), null, null, null, defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.sellerIdEq(criteria);

            // Then - BooleanExpression이 반환되어야 함
            assertThat(expression).isNotNull();
        }

        @Test
        @DisplayName("sellerId가 없을 때 null 반환")
        void shouldReturnNullWhenNoSellerId() {
            // Given - 셀러 ID가 없는 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(null, null, null, null, defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.sellerIdEq(criteria);

            // Then - null이 반환되어야 함 (필터 없음)
            assertThat(expression).isNull();
        }
    }

    @Nested
    @DisplayName("statusIn 메서드 테스트")
    class StatusInTests {

        @Test
        @DisplayName("상태 목록이 있을 때 BooleanExpression 반환")
        void shouldReturnExpressionWhenStatusesExist() {
            // Given - 상태 필터가 있는 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            List.of(SchedulerStatus.ACTIVE),
                            null,
                            null,
                            defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.statusIn(criteria);

            // Then - BooleanExpression이 반환되어야 함
            assertThat(expression).isNotNull();
        }

        @Test
        @DisplayName("여러 상태가 있을 때 BooleanExpression 반환")
        void shouldReturnExpressionWhenMultipleStatusesExist() {
            // Given - 여러 상태 필터가 있는 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            List.of(SchedulerStatus.ACTIVE, SchedulerStatus.INACTIVE),
                            null,
                            null,
                            defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.statusIn(criteria);

            // Then - BooleanExpression이 반환되어야 함
            assertThat(expression).isNotNull();
        }

        @Test
        @DisplayName("상태 목록이 없을 때 null 반환")
        void shouldReturnNullWhenNoStatuses() {
            // Given - 상태 필터가 없는 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(null, null, null, null, defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.statusIn(criteria);

            // Then - null이 반환되어야 함 (필터 없음)
            assertThat(expression).isNull();
        }

        @Test
        @DisplayName("빈 상태 목록일 때 null 반환")
        void shouldReturnNullWhenEmptyStatuses() {
            // Given - 빈 상태 목록인 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null, List.of(), null, null, defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.statusIn(criteria);

            // Then - null이 반환되어야 함 (필터 없음)
            assertThat(expression).isNull();
        }
    }

    @Nested
    @DisplayName("searchCondition 메서드 테스트")
    class SearchConditionTests {

        @Test
        @DisplayName("SCHEDULER_NAME 검색 필드와 검색어가 있을 때 BooleanExpression 반환")
        void shouldReturnExpressionForSchedulerNameSearch() {
            // Given - 스케줄러 이름 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            CrawlSchedulerSearchField.SCHEDULER_NAME,
                            "test-scheduler",
                            defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.searchCondition(criteria);

            // Then - BooleanExpression이 반환되어야 함
            assertThat(expression).isNotNull();
        }

        @Test
        @DisplayName("검색 조건이 없을 때 null 반환")
        void shouldReturnNullWhenNoSearchCondition() {
            // Given - 검색 조건이 없는 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(null, null, null, null, defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.searchCondition(criteria);

            // Then - null이 반환되어야 함 (필터 없음)
            assertThat(expression).isNull();
        }

        @Test
        @DisplayName("검색 필드는 있지만 검색어가 없을 때 null 반환")
        void shouldReturnNullWhenSearchWordIsBlank() {
            // Given - 검색 필드는 있지만 검색어가 공백인 경우
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            CrawlSchedulerSearchField.SCHEDULER_NAME,
                            "   ",
                            defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.searchCondition(criteria);

            // Then - null이 반환되어야 함 (유효한 검색어 없음)
            assertThat(expression).isNull();
        }

        @Test
        @DisplayName("검색어는 있지만 검색 필드가 null일 때 null 반환")
        void shouldReturnNullWhenSearchFieldIsNull() {
            // Given - 검색어는 있지만 검색 필드가 null인 경우
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null, null, null, "test-scheduler", defaultQueryContext());

            // When
            BooleanExpression expression = conditionBuilder.searchCondition(criteria);

            // Then - null이 반환되어야 함 (검색 필드 없음)
            assertThat(expression).isNull();
        }
    }

    @Nested
    @DisplayName("복합 조건 테스트")
    class CombinedConditionTests {

        @Test
        @DisplayName("셀러 ID와 상태 조건 모두 있을 때 각각 BooleanExpression 반환")
        void shouldReturnExpressionsForCombinedConditions() {
            // Given - 셀러 ID와 상태 필터가 모두 있는 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            SellerId.of(1L),
                            List.of(SchedulerStatus.ACTIVE),
                            CrawlSchedulerSearchField.SCHEDULER_NAME,
                            "test",
                            defaultQueryContext());

            // When
            BooleanExpression sellerExpression = conditionBuilder.sellerIdEq(criteria);
            BooleanExpression statusExpression = conditionBuilder.statusIn(criteria);
            BooleanExpression searchExpression = conditionBuilder.searchCondition(criteria);

            // Then - 모든 조건이 BooleanExpression으로 반환되어야 함
            assertThat(sellerExpression).isNotNull();
            assertThat(statusExpression).isNotNull();
            assertThat(searchExpression).isNotNull();
        }

        @Test
        @DisplayName("모든 조건이 없을 때 모두 null 반환")
        void shouldReturnNullsForEmptyConditions() {
            // Given - 모든 조건이 없는 검색 조건
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(null, null, null, null, defaultQueryContext());

            // When
            BooleanExpression sellerExpression = conditionBuilder.sellerIdEq(criteria);
            BooleanExpression statusExpression = conditionBuilder.statusIn(criteria);
            BooleanExpression searchExpression = conditionBuilder.searchCondition(criteria);

            // Then - 모든 조건이 null이어야 함
            assertThat(sellerExpression).isNull();
            assertThat(statusExpression).isNull();
            assertThat(searchExpression).isNull();
        }
    }
}
