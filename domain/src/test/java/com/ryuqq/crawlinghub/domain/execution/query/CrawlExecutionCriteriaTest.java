package com.ryuqq.crawlinghub.domain.execution.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
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
@DisplayName("CrawlExecutionCriteria 단위 테스트")
class CrawlExecutionCriteriaTest {

    @Nested
    @DisplayName("생성자 정규화 테스트")
    class NormalizationTest {

        @Test
        @DisplayName("음수 page는 0으로 정규화된다")
        void normalizeNegativePage() {
            // when
            CrawlExecutionCriteria criteria =
                    new CrawlExecutionCriteria(null, null, null, null, null, null, -1, 20);

            // then
            assertThat(criteria.page()).isEqualTo(0);
        }

        @Test
        @DisplayName("0 이하 size는 DEFAULT_SIZE(20)로 정규화된다")
        void normalizeZeroSize() {
            // when
            CrawlExecutionCriteria criteria =
                    new CrawlExecutionCriteria(null, null, null, null, null, null, 0, 0);

            // then
            assertThat(criteria.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("statuses 목록은 방어적 복사된다")
        void statusesAreDefensivelyCopied() {
            // given
            List<CrawlExecutionStatus> mutableStatuses = new ArrayList<>();
            mutableStatuses.add(CrawlExecutionStatus.RUNNING);

            // when
            CrawlExecutionCriteria criteria =
                    new CrawlExecutionCriteria(
                            null, null, null, mutableStatuses, null, null, 0, 20);
            mutableStatuses.add(CrawlExecutionStatus.FAILED);

            // then
            assertThat(criteria.statuses()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("byTaskId 팩토리 메서드 테스트")
    class ByTaskIdTest {

        @Test
        @DisplayName("Task ID로 조회 조건을 생성한다")
        void createByTaskId() {
            // given
            CrawlTaskId taskId = CrawlTaskId.of(1L);

            // when
            CrawlExecutionCriteria criteria = CrawlExecutionCriteria.byTaskId(taskId, 0, 20);

            // then
            assertThat(criteria.crawlTaskId()).isEqualTo(taskId);
            assertThat(criteria.crawlSchedulerId()).isNull();
            assertThat(criteria.statuses()).isNull();
            assertThat(criteria.hasTaskIdFilter()).isTrue();
        }
    }

    @Nested
    @DisplayName("bySchedulerIdAndPeriod 팩토리 메서드 테스트")
    class BySchedulerIdAndPeriodTest {

        @Test
        @DisplayName("Scheduler ID와 기간으로 조회 조건을 생성한다")
        void createBySchedulerIdAndPeriod() {
            // given
            CrawlSchedulerId schedulerId = CrawlSchedulerId.of(1L);
            Instant from = Instant.now().minusSeconds(3600);
            Instant to = Instant.now();

            // when
            CrawlExecutionCriteria criteria =
                    CrawlExecutionCriteria.bySchedulerIdAndPeriod(schedulerId, from, to, 0, 20);

            // then
            assertThat(criteria.crawlSchedulerId()).isEqualTo(schedulerId);
            assertThat(criteria.from()).isEqualTo(from);
            assertThat(criteria.to()).isEqualTo(to);
            assertThat(criteria.hasSchedulerIdFilter()).isTrue();
            assertThat(criteria.hasPeriodFilter()).isTrue();
        }
    }

    @Nested
    @DisplayName("bySchedulerIdAndStatusAndPeriod 팩토리 메서드 테스트")
    class BySchedulerIdAndStatusAndPeriodTest {

        @Test
        @DisplayName("Scheduler ID, 상태, 기간으로 조회 조건을 생성한다")
        void createBySchedulerIdAndStatusAndPeriod() {
            // given
            CrawlSchedulerId schedulerId = CrawlSchedulerId.of(1L);
            Instant from = Instant.now().minusSeconds(3600);
            Instant to = Instant.now();

            // when
            CrawlExecutionCriteria criteria =
                    CrawlExecutionCriteria.bySchedulerIdAndStatusAndPeriod(
                            schedulerId, CrawlExecutionStatus.FAILED, from, to, 0, 20);

            // then
            assertThat(criteria.crawlSchedulerId()).isEqualTo(schedulerId);
            assertThat(criteria.hasStatusFilter()).isTrue();
            assertThat(criteria.status()).isEqualTo(CrawlExecutionStatus.FAILED);
        }

        @Test
        @DisplayName("상태가 null이면 상태 필터 없이 생성한다")
        void createWithNullStatus() {
            // given
            CrawlSchedulerId schedulerId = CrawlSchedulerId.of(1L);

            // when
            CrawlExecutionCriteria criteria =
                    CrawlExecutionCriteria.bySchedulerIdAndStatusAndPeriod(
                            schedulerId, null, null, null, 0, 20);

            // then
            assertThat(criteria.hasStatusFilter()).isFalse();
            assertThat(criteria.status()).isNull();
        }
    }

    @Nested
    @DisplayName("offset() 메서드 테스트")
    class OffsetTest {

        @Test
        @DisplayName("page * size를 반환한다")
        void calculateOffset() {
            // given
            CrawlExecutionCriteria criteria =
                    new CrawlExecutionCriteria(null, null, null, null, null, null, 2, 20);

            // then
            assertThat(criteria.offset()).isEqualTo(40L);
        }
    }

    @Nested
    @DisplayName("필터 여부 확인 메서드 테스트")
    class FilterCheckTest {

        @Test
        @DisplayName("hasTaskIdFilter - crawlTaskId가 있으면 true")
        void hasTaskIdFilterWhenPresent() {
            // given
            CrawlExecutionCriteria criteria =
                    CrawlExecutionCriteria.byTaskId(CrawlTaskId.of(1L), 0, 20);

            // then
            assertThat(criteria.hasTaskIdFilter()).isTrue();
        }

        @Test
        @DisplayName("hasSchedulerIdFilter - crawlSchedulerId가 있으면 true")
        void hasSchedulerIdFilterWhenPresent() {
            // given
            CrawlExecutionCriteria criteria =
                    new CrawlExecutionCriteria(
                            null, CrawlSchedulerId.of(1L), null, null, null, null, 0, 20);

            // then
            assertThat(criteria.hasSchedulerIdFilter()).isTrue();
        }

        @Test
        @DisplayName("hasStatusFilter - statuses가 있으면 true")
        void hasStatusFilterWhenPresent() {
            // given
            CrawlExecutionCriteria criteria =
                    new CrawlExecutionCriteria(
                            null,
                            null,
                            null,
                            List.of(CrawlExecutionStatus.RUNNING),
                            null,
                            null,
                            0,
                            20);

            // then
            assertThat(criteria.hasStatusFilter()).isTrue();
        }

        @Test
        @DisplayName("hasPeriodFilter - from 또는 to가 있으면 true")
        void hasPeriodFilterWhenPresent() {
            // given
            CrawlExecutionCriteria onlyFrom =
                    new CrawlExecutionCriteria(null, null, null, null, Instant.now(), null, 0, 20);
            CrawlExecutionCriteria onlyTo =
                    new CrawlExecutionCriteria(null, null, null, null, null, Instant.now(), 0, 20);

            // then
            assertThat(onlyFrom.hasPeriodFilter()).isTrue();
            assertThat(onlyTo.hasPeriodFilter()).isTrue();
        }
    }
}
