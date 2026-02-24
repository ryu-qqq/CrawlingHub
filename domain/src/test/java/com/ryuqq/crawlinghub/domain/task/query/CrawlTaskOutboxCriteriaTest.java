package com.ryuqq.crawlinghub.domain.task.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("query")
@DisplayName("CrawlTaskOutboxCriteria 단위 테스트")
class CrawlTaskOutboxCriteriaTest {

    @Nested
    @DisplayName("생성자 정규화 테스트")
    class NormalizationTest {

        @Test
        @DisplayName("음수 offset은 0으로 정규화된다")
        void normalizeNegativeOffset() {
            // when
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, null, null, -5, 100);

            // then
            assertThat(criteria.offset()).isEqualTo(0);
        }

        @Test
        @DisplayName("0 이하 limit은 DEFAULT_LIMIT(100)으로 정규화된다")
        void normalizeZeroLimit() {
            // when
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, null, null, 0, 0);

            // then
            assertThat(criteria.limit()).isEqualTo(100);
        }

        @Test
        @DisplayName("음수 limit은 DEFAULT_LIMIT(100)으로 정규화된다")
        void normalizeNegativeLimit() {
            // when
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, null, null, 0, -10);

            // then
            assertThat(criteria.limit()).isEqualTo(100);
        }

        @Test
        @DisplayName("statuses 목록은 방어적 복사된다")
        void statusesAreDefensivelyCopied() {
            // given
            List<OutboxStatus> mutableStatuses = new ArrayList<>();
            mutableStatuses.add(OutboxStatus.PENDING);

            // when
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, mutableStatuses, null, null, 0, 100);
            mutableStatuses.add(OutboxStatus.FAILED);

            // then
            assertThat(criteria.statuses()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("byStatus 팩토리 메서드 테스트")
    class ByStatusTest {

        @Test
        @DisplayName("단일 상태로 조회 조건을 생성한다")
        void createByStatus() {
            // when
            CrawlTaskOutboxCriteria criteria =
                    CrawlTaskOutboxCriteria.byStatus(OutboxStatus.PENDING, 50);

            // then
            assertThat(criteria.status()).isEqualTo(OutboxStatus.PENDING);
            assertThat(criteria.statuses()).isNull();
            assertThat(criteria.offset()).isEqualTo(0);
            assertThat(criteria.limit()).isEqualTo(50);
            assertThat(criteria.hasSingleStatusFilter()).isTrue();
        }
    }

    @Nested
    @DisplayName("byStatuses 팩토리 메서드 테스트")
    class ByStatusesTest {

        @Test
        @DisplayName("다중 상태로 조회 조건을 생성한다")
        void createByStatuses() {
            // given
            List<OutboxStatus> statuses = List.of(OutboxStatus.PENDING, OutboxStatus.FAILED);

            // when
            CrawlTaskOutboxCriteria criteria = CrawlTaskOutboxCriteria.byStatuses(statuses, 100);

            // then
            assertThat(criteria.status()).isNull();
            assertThat(criteria.statuses())
                    .containsExactly(OutboxStatus.PENDING, OutboxStatus.FAILED);
            assertThat(criteria.offset()).isEqualTo(0);
            assertThat(criteria.limit()).isEqualTo(100);
            assertThat(criteria.hasMultipleStatusFilter()).isTrue();
        }
    }

    @Nested
    @DisplayName("byStatusesWithPaging 팩토리 메서드 테스트")
    class ByStatusesWithPagingTest {

        @Test
        @DisplayName("다중 상태와 페이징으로 조회 조건을 생성한다")
        void createByStatusesWithPaging() {
            // given
            List<OutboxStatus> statuses = List.of(OutboxStatus.PENDING);

            // when
            CrawlTaskOutboxCriteria criteria =
                    CrawlTaskOutboxCriteria.byStatusesWithPaging(statuses, 20, 20);

            // then
            assertThat(criteria.statuses()).containsExactly(OutboxStatus.PENDING);
            assertThat(criteria.offset()).isEqualTo(20);
            assertThat(criteria.limit()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("withDateRange 팩토리 메서드 테스트")
    class WithDateRangeTest {

        @Test
        @DisplayName("다중 상태와 기간으로 조회 조건을 생성한다")
        void createWithDateRange() {
            // given
            List<OutboxStatus> statuses = List.of(OutboxStatus.PENDING, OutboxStatus.FAILED);
            Instant from = Instant.now().minusSeconds(3600);
            Instant to = Instant.now();

            // when
            CrawlTaskOutboxCriteria criteria =
                    CrawlTaskOutboxCriteria.withDateRange(statuses, from, to, 0, 100);

            // then
            assertThat(criteria.statuses()).hasSize(2);
            assertThat(criteria.createdFrom()).isEqualTo(from);
            assertThat(criteria.createdTo()).isEqualTo(to);
            assertThat(criteria.hasDateRangeFilter()).isTrue();
        }
    }

    @Nested
    @DisplayName("pendingOrFailed 팩토리 메서드 테스트")
    class PendingOrFailedTest {

        @Test
        @DisplayName("PENDING 또는 FAILED 상태 조회 조건을 생성한다")
        void createPendingOrFailed() {
            // when
            CrawlTaskOutboxCriteria criteria = CrawlTaskOutboxCriteria.pendingOrFailed(200);

            // then
            assertThat(criteria.statuses())
                    .containsExactlyInAnyOrder(OutboxStatus.PENDING, OutboxStatus.FAILED);
            assertThat(criteria.offset()).isEqualTo(0);
            assertThat(criteria.limit()).isEqualTo(200);
            assertThat(criteria.hasMultipleStatusFilter()).isTrue();
        }
    }

    @Nested
    @DisplayName("필터 여부 확인 메서드 테스트")
    class FilterCheckTest {

        @Test
        @DisplayName("hasSingleStatusFilter - 단일 상태가 있으면 true")
        void hasSingleStatusFilterWhenPresent() {
            // given
            CrawlTaskOutboxCriteria criteria =
                    CrawlTaskOutboxCriteria.byStatus(OutboxStatus.PENDING, 100);

            // then
            assertThat(criteria.hasSingleStatusFilter()).isTrue();
            assertThat(criteria.hasStatusFilter()).isTrue();
        }

        @Test
        @DisplayName("hasSingleStatusFilter - 단일 상태가 없으면 false")
        void hasSingleStatusFilterWhenNull() {
            // given
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, null, null, 0, 100);

            // then
            assertThat(criteria.hasSingleStatusFilter()).isFalse();
        }

        @Test
        @DisplayName("hasMultipleStatusFilter - 다중 상태가 있으면 true")
        void hasMultipleStatusFilterWhenPresent() {
            // given
            CrawlTaskOutboxCriteria criteria =
                    CrawlTaskOutboxCriteria.byStatuses(List.of(OutboxStatus.PENDING), 100);

            // then
            assertThat(criteria.hasMultipleStatusFilter()).isTrue();
            assertThat(criteria.hasStatusFilter()).isTrue();
        }

        @Test
        @DisplayName("hasMultipleStatusFilter - 다중 상태가 없으면 false")
        void hasMultipleStatusFilterWhenNull() {
            // given
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, null, null, 0, 100);

            // then
            assertThat(criteria.hasMultipleStatusFilter()).isFalse();
        }

        @Test
        @DisplayName("hasStatusFilter - 단일 또는 다중 상태가 있으면 true")
        void hasStatusFilterWhenEitherPresent() {
            // given
            CrawlTaskOutboxCriteria singleStatus =
                    CrawlTaskOutboxCriteria.byStatus(OutboxStatus.PROCESSING, 100);
            CrawlTaskOutboxCriteria multipleStatus =
                    CrawlTaskOutboxCriteria.byStatuses(List.of(OutboxStatus.PENDING), 100);

            // then
            assertThat(singleStatus.hasStatusFilter()).isTrue();
            assertThat(multipleStatus.hasStatusFilter()).isTrue();
        }

        @Test
        @DisplayName("hasStatusFilter - 아무 상태도 없으면 false")
        void hasStatusFilterWhenNone() {
            // given
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, null, null, 0, 100);

            // then
            assertThat(criteria.hasStatusFilter()).isFalse();
        }

        @Test
        @DisplayName("hasCreatedFromFilter - createdFrom이 있으면 true")
        void hasCreatedFromFilterWhenPresent() {
            // given
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, Instant.now(), null, 0, 100);

            // then
            assertThat(criteria.hasCreatedFromFilter()).isTrue();
        }

        @Test
        @DisplayName("hasCreatedFromFilter - createdFrom이 없으면 false")
        void hasCreatedFromFilterWhenNull() {
            // given
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, null, null, 0, 100);

            // then
            assertThat(criteria.hasCreatedFromFilter()).isFalse();
        }

        @Test
        @DisplayName("hasCreatedToFilter - createdTo가 있으면 true")
        void hasCreatedToFilterWhenPresent() {
            // given
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, null, Instant.now(), 0, 100);

            // then
            assertThat(criteria.hasCreatedToFilter()).isTrue();
        }

        @Test
        @DisplayName("hasDateRangeFilter - from 또는 to가 있으면 true")
        void hasDateRangeFilterWhenEitherPresent() {
            // given
            CrawlTaskOutboxCriteria onlyFrom =
                    new CrawlTaskOutboxCriteria(null, null, Instant.now(), null, 0, 100);
            CrawlTaskOutboxCriteria onlyTo =
                    new CrawlTaskOutboxCriteria(null, null, null, Instant.now(), 0, 100);

            // then
            assertThat(onlyFrom.hasDateRangeFilter()).isTrue();
            assertThat(onlyTo.hasDateRangeFilter()).isTrue();
        }

        @Test
        @DisplayName("hasDateRangeFilter - 모두 없으면 false")
        void hasDateRangeFilterWhenNone() {
            // given
            CrawlTaskOutboxCriteria criteria =
                    new CrawlTaskOutboxCriteria(null, null, null, null, 0, 100);

            // then
            assertThat(criteria.hasDateRangeFilter()).isFalse();
        }
    }
}
