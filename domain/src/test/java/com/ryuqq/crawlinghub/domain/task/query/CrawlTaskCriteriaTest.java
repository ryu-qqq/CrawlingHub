package com.ryuqq.crawlinghub.domain.task.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("query")
@DisplayName("CrawlTaskCriteria 단위 테스트")
class CrawlTaskCriteriaTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("모든 필드로 생성한다")
        void createWithAllFields() {
            // given
            List<CrawlSchedulerId> schedulerIds = List.of(CrawlSchedulerId.of(1L));
            List<SellerId> sellerIds = List.of(SellerId.of(100L));
            List<CrawlTaskStatus> statuses = List.of(CrawlTaskStatus.WAITING);
            List<CrawlTaskType> taskTypes = List.of(CrawlTaskType.MINI_SHOP);
            Instant from = Instant.now().minusSeconds(3600);
            Instant to = Instant.now();

            // when
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(
                            schedulerIds, sellerIds, statuses, taskTypes, from, to, 0, 20);

            // then
            assertThat(criteria.crawlSchedulerIds()).hasSize(1);
            assertThat(criteria.sellerIds()).hasSize(1);
            assertThat(criteria.statuses()).hasSize(1);
            assertThat(criteria.taskTypes()).hasSize(1);
            assertThat(criteria.createdFrom()).isEqualTo(from);
            assertThat(criteria.createdTo()).isEqualTo(to);
            assertThat(criteria.page()).isEqualTo(0);
            assertThat(criteria.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("선택 필드 없이 생성한다")
        void createWithMinimalFields() {
            // when
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(null, null, null, null, null, null, 0, 20);

            // then
            assertThat(criteria.crawlSchedulerIds()).isNull();
            assertThat(criteria.sellerIds()).isNull();
            assertThat(criteria.statuses()).isNull();
            assertThat(criteria.taskTypes()).isNull();
        }

        @Test
        @DisplayName("음수 page는 0으로 정규화된다")
        void normalizeNegativePage() {
            // when
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(null, null, null, null, null, null, -5, 20);

            // then
            assertThat(criteria.page()).isEqualTo(0);
        }

        @Test
        @DisplayName("0 이하 size는 DEFAULT_SIZE로 정규화된다")
        void normalizeInvalidSize() {
            // when
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(null, null, null, null, null, null, 0, 0);

            // then
            assertThat(criteria.size()).isEqualTo(20); // DEFAULT_SIZE
        }

        @Test
        @DisplayName("MAX_SIZE 초과 size는 DEFAULT_SIZE로 정규화된다")
        void normalizeExcessiveSize() {
            // when
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(
                            null, null, null, null, null, null, 0, 200); // > MAX_SIZE(100)

            // then
            assertThat(criteria.size()).isEqualTo(20); // DEFAULT_SIZE
        }

        @Test
        @DisplayName("목록 필드는 방어적 복사된다")
        void listsAreDefensivelyCopied() {
            // given
            List<CrawlTaskStatus> mutableStatuses = new java.util.ArrayList<>();
            mutableStatuses.add(CrawlTaskStatus.WAITING);

            // when
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(null, null, mutableStatuses, null, null, null, 0, 20);
            mutableStatuses.add(CrawlTaskStatus.RUNNING);

            // then
            assertThat(criteria.statuses()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("필터 여부 확인 메서드 테스트")
    class FilterCheckTest {

        @Test
        @DisplayName("hasSchedulerIdFilter - schedulerIds가 있으면 true")
        void hasSchedulerIdFilterWhenPresent() {
            // given
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(
                            List.of(CrawlSchedulerId.of(1L)), null, null, null, null, null, 0, 20);

            // then
            assertThat(criteria.hasSchedulerIdFilter()).isTrue();
        }

        @Test
        @DisplayName("hasSchedulerIdFilter - schedulerIds가 없으면 false")
        void hasSchedulerIdFilterWhenNull() {
            // given
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(null, null, null, null, null, null, 0, 20);

            // then
            assertThat(criteria.hasSchedulerIdFilter()).isFalse();
        }

        @Test
        @DisplayName("hasSellerIdFilter - sellerIds가 있으면 true")
        void hasSellerIdFilterWhenPresent() {
            // given
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(
                            null, List.of(SellerId.of(1L)), null, null, null, null, 0, 20);

            // then
            assertThat(criteria.hasSellerIdFilter()).isTrue();
        }

        @Test
        @DisplayName("hasStatusFilter - statuses가 있으면 true")
        void hasStatusFilterWhenPresent() {
            // given
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(
                            null, null, List.of(CrawlTaskStatus.WAITING), null, null, null, 0, 20);

            // then
            assertThat(criteria.hasStatusFilter()).isTrue();
        }

        @Test
        @DisplayName("hasTaskTypeFilter - taskTypes가 있으면 true")
        void hasTaskTypeFilterWhenPresent() {
            // given
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(
                            null, null, null, List.of(CrawlTaskType.MINI_SHOP), null, null, 0, 20);

            // then
            assertThat(criteria.hasTaskTypeFilter()).isTrue();
        }

        @Test
        @DisplayName("hasCreatedFromFilter - createdFrom이 있으면 true")
        void hasCreatedFromFilterWhenPresent() {
            // given
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(null, null, null, null, Instant.now(), null, 0, 20);

            // then
            assertThat(criteria.hasCreatedFromFilter()).isTrue();
        }

        @Test
        @DisplayName("hasCreatedToFilter - createdTo가 있으면 true")
        void hasCreatedToFilterWhenPresent() {
            // given
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(null, null, null, null, null, Instant.now(), 0, 20);

            // then
            assertThat(criteria.hasCreatedToFilter()).isTrue();
        }
    }

    @Nested
    @DisplayName("offset() 메서드 테스트")
    class OffsetTest {

        @Test
        @DisplayName("page * size를 반환한다")
        void calculateOffset() {
            // given
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(null, null, null, null, null, null, 2, 20);

            // then
            assertThat(criteria.offset()).isEqualTo(40L);
        }
    }
}
