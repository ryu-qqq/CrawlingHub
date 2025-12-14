package com.ryuqq.crawlinghub.application.task.factory.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@Tag("factory")
@DisplayName("CrawlTaskQueryFactory 단위 테스트")
class CrawlTaskQueryFactoryTest {

    private CrawlTaskQueryFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CrawlTaskQueryFactory();
    }

    @Nested
    @DisplayName("createCriteria() 메서드는")
    class CreateCriteriaMethod {

        @Test
        @DisplayName("모든 필드가 있는 Query를 Criteria로 변환한다")
        void shouldConvertQueryWithAllFields() {
            // Given
            ListCrawlTasksQuery query =
                    new ListCrawlTasksQuery(100L, CrawlTaskStatus.WAITING, 1, 20);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.crawlSchedulerId().value()).isEqualTo(100L);
            assertThat(criteria.status()).isEqualTo(CrawlTaskStatus.WAITING);
            assertThat(criteria.page()).isEqualTo(1);
            assertThat(criteria.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("status가 null이어도 Criteria로 변환한다")
        void shouldConvertQueryWithNullStatus() {
            // Given
            ListCrawlTasksQuery query = new ListCrawlTasksQuery(100L, null, 2, 50);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.crawlSchedulerId().value()).isEqualTo(100L);
            assertThat(criteria.status()).isNull();
            assertThat(criteria.page()).isEqualTo(2);
            assertThat(criteria.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("RUNNING 상태로 필터링할 수 있다")
        void shouldConvertQueryWithRunningStatus() {
            // Given
            ListCrawlTasksQuery query =
                    new ListCrawlTasksQuery(200L, CrawlTaskStatus.RUNNING, 0, 10);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.status()).isEqualTo(CrawlTaskStatus.RUNNING);
        }

        @Test
        @DisplayName("SUCCESS 상태로 필터링할 수 있다")
        void shouldConvertQueryWithSuccessStatus() {
            // Given
            ListCrawlTasksQuery query =
                    new ListCrawlTasksQuery(300L, CrawlTaskStatus.SUCCESS, 0, 10);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.status()).isEqualTo(CrawlTaskStatus.SUCCESS);
        }

        @Test
        @DisplayName("FAILED 상태로 필터링할 수 있다")
        void shouldConvertQueryWithFailedStatus() {
            // Given
            ListCrawlTasksQuery query =
                    new ListCrawlTasksQuery(400L, CrawlTaskStatus.FAILED, 0, 10);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.status()).isEqualTo(CrawlTaskStatus.FAILED);
        }
    }
}
