package com.ryuqq.crawlinghub.application.execution.factory.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@Tag("factory")
@DisplayName("CrawlExecutionQueryFactory 단위 테스트")
class CrawlExecutionQueryFactoryTest {

    private CrawlExecutionQueryFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CrawlExecutionQueryFactory();
    }

    @Nested
    @DisplayName("createCriteria() 메서드는")
    class CreateCriteriaMethod {

        @Test
        @DisplayName("모든 필드가 있는 Query를 Criteria로 변환한다")
        void shouldConvertQueryWithAllFields() {
            // Given
            LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime to = LocalDateTime.of(2024, 1, 31, 23, 59);
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(
                            100L,
                            200L,
                            300L,
                            List.of(CrawlExecutionStatus.SUCCESS),
                            from,
                            to,
                            1,
                            20);

            // When
            CrawlExecutionCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.crawlTaskId().value()).isEqualTo(100L);
            assertThat(criteria.crawlSchedulerId().value()).isEqualTo(200L);
            assertThat(criteria.status()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            assertThat(criteria.from()).isNotNull();
            assertThat(criteria.to()).isNotNull();
            assertThat(criteria.page()).isEqualTo(1);
            assertThat(criteria.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("crawlTaskId가 null이면 Criteria에도 null로 설정한다")
        void shouldSetNullCrawlTaskIdWhenQueryHasNull() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(null, 200L, 300L, null, null, null, 0, 10);

            // When
            CrawlExecutionCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.crawlTaskId()).isNull();
            assertThat(criteria.crawlSchedulerId().value()).isEqualTo(200L);
        }

        @Test
        @DisplayName("crawlSchedulerId가 null이면 Criteria에도 null로 설정한다")
        void shouldSetNullCrawlSchedulerIdWhenQueryHasNull() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(100L, null, 300L, null, null, null, 0, 10);

            // When
            CrawlExecutionCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.crawlTaskId().value()).isEqualTo(100L);
            assertThat(criteria.crawlSchedulerId()).isNull();
        }

        @Test
        @DisplayName("시간 필드가 null이면 Criteria에도 null로 설정한다")
        void shouldSetNullTimeFieldsWhenQueryHasNull() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(100L, 200L, 300L, null, null, null, 0, 10);

            // When
            CrawlExecutionCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.from()).isNull();
            assertThat(criteria.to()).isNull();
        }

        @Test
        @DisplayName("RUNNING 상태로 필터링할 수 있다")
        void shouldConvertQueryWithRunningStatus() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(
                            100L,
                            200L,
                            300L,
                            List.of(CrawlExecutionStatus.RUNNING),
                            null,
                            null,
                            0,
                            10);

            // When
            CrawlExecutionCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.status()).isEqualTo(CrawlExecutionStatus.RUNNING);
        }

        @Test
        @DisplayName("FAILED 상태로 필터링할 수 있다")
        void shouldConvertQueryWithFailedStatus() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(
                            100L,
                            200L,
                            300L,
                            List.of(CrawlExecutionStatus.FAILED),
                            null,
                            null,
                            0,
                            10);

            // When
            CrawlExecutionCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.status()).isEqualTo(CrawlExecutionStatus.FAILED);
        }

        @Test
        @DisplayName("모든 필터 조건이 null이어도 페이지네이션은 유지한다")
        void shouldKeepPaginationWhenAllFiltersAreNull() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(null, null, null, null, null, null, 5, 50);

            // When
            CrawlExecutionCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.crawlTaskId()).isNull();
            assertThat(criteria.crawlSchedulerId()).isNull();
            assertThat(criteria.status()).isNull();
            assertThat(criteria.from()).isNull();
            assertThat(criteria.to()).isNull();
            assertThat(criteria.page()).isEqualTo(5);
            assertThat(criteria.size()).isEqualTo(50);
        }
    }
}
