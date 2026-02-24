package com.ryuqq.crawlinghub.domain.schedule.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerHistoryIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CronExpressionFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.SchedulerNameFixture;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@DisplayName("CrawlSchedulerHistory Aggregate 단위 테스트")
class CrawlSchedulerHistoryTest {

    private static final Instant NOW = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNewTest {

        @Test
        @DisplayName("신규 CrawlSchedulerHistory를 생성한다")
        void createNewHistory() {
            // given
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            SellerId sellerId = SellerId.of(1L);
            SchedulerName schedulerName = SchedulerNameFixture.aDefaultName();
            CronExpression cronExpression = CronExpressionFixture.aDefaultCron();

            // when
            CrawlSchedulerHistory history =
                    CrawlSchedulerHistory.forNew(
                            schedulerId,
                            sellerId,
                            schedulerName,
                            cronExpression,
                            SchedulerStatus.ACTIVE,
                            NOW);

            // then
            assertThat(history.getHistoryId()).isNull();
            assertThat(history.getHistoryIdValue()).isNull();
            assertThat(history.getCrawlSchedulerId()).isEqualTo(schedulerId);
            assertThat(history.getSellerId()).isEqualTo(sellerId);
            assertThat(history.getSchedulerName()).isEqualTo(schedulerName);
            assertThat(history.getCronExpression()).isEqualTo(cronExpression);
            assertThat(history.getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(history.getCreatedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("reconstitute() 팩토리 메서드 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("기존 데이터로 CrawlSchedulerHistory를 복원한다")
        void reconstituteHistory() {
            // given
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            SellerId sellerId = SellerId.of(1L);
            SchedulerName schedulerName = SchedulerNameFixture.aDefaultName();
            CronExpression cronExpression = CronExpressionFixture.aDefaultCron();

            // when
            CrawlSchedulerHistory history =
                    CrawlSchedulerHistory.reconstitute(
                            historyId,
                            schedulerId,
                            sellerId,
                            schedulerName,
                            cronExpression,
                            SchedulerStatus.INACTIVE,
                            NOW);

            // then
            assertThat(history.getHistoryId()).isEqualTo(historyId);
            assertThat(history.getHistoryIdValue()).isNotNull();
            assertThat(history.getStatus()).isEqualTo(SchedulerStatus.INACTIVE);
            assertThat(history.getCrawlSchedulerIdValue()).isEqualTo(schedulerId.value());
            assertThat(history.getSellerIdValue()).isEqualTo(1L);
            assertThat(history.getSchedulerNameValue()).isEqualTo(schedulerName.value());
            assertThat(history.getCronExpressionValue()).isEqualTo(cronExpression.value());
        }
    }

    @Nested
    @DisplayName("fromScheduler() 팩토리 메서드 테스트")
    class FromSchedulerTest {

        @Test
        @DisplayName("CrawlScheduler로부터 히스토리를 생성한다")
        void createHistoryFromScheduler() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // when
            CrawlSchedulerHistory history = CrawlSchedulerHistory.fromScheduler(scheduler, NOW);

            // then
            assertThat(history.getCrawlSchedulerId()).isEqualTo(scheduler.getCrawlSchedulerId());
            assertThat(history.getSellerId()).isEqualTo(scheduler.getSellerId());
            assertThat(history.getSchedulerName()).isEqualTo(scheduler.getSchedulerName());
            assertThat(history.getCronExpression()).isEqualTo(scheduler.getCronExpression());
            assertThat(history.getStatus()).isEqualTo(scheduler.getStatus());
        }
    }

    @Nested
    @DisplayName("equals/hashCode 테스트")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 historyId이면 동일하다")
        void sameHistoryIdAreEqual() {
            // given
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            SellerId sellerId = SellerId.of(1L);

            CrawlSchedulerHistory history1 =
                    CrawlSchedulerHistory.reconstitute(
                            historyId,
                            schedulerId,
                            sellerId,
                            SchedulerNameFixture.aDefaultName(),
                            CronExpressionFixture.aDefaultCron(),
                            SchedulerStatus.ACTIVE,
                            NOW);

            CrawlSchedulerHistory history2 =
                    CrawlSchedulerHistory.reconstitute(
                            historyId,
                            schedulerId,
                            sellerId,
                            SchedulerNameFixture.aDefaultName(),
                            CronExpressionFixture.aDefaultCron(),
                            SchedulerStatus.INACTIVE,
                            NOW.plusSeconds(100));

            // then
            assertThat(history1).isEqualTo(history2);
            assertThat(history1.hashCode()).isEqualTo(history2.hashCode());
        }

        @Test
        @DisplayName("null historyId를 가진 히스토리들은 동일하다")
        void nullHistoryIdAreEqual() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerHistory history1 = CrawlSchedulerHistory.fromScheduler(scheduler, NOW);
            CrawlSchedulerHistory history2 = CrawlSchedulerHistory.fromScheduler(scheduler, NOW);

            // then - null historyId는 Objects.equals로 같음
            assertThat(history1).isEqualTo(history2);
        }
    }
}
