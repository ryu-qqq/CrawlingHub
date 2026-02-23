package com.ryuqq.crawlinghub.domain.schedule.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CronExpressionFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.SchedulerNameFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerUpdateData;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlScheduler Aggregate Root 단위 테스트
 *
 * <p>테스트 대상:
 *
 * <ul>
 *   <li>신규 생성 {@code forNew()} - ACTIVE 상태, ID=null
 *   <li>영속성 복원 {@code reconstitute()} - 모든 필드 복원
 *   <li>통합 수정 {@code update()} - 필드 변경 및 updatedAt 갱신
 *   <li>이름 비교 {@code hasSameSchedulerName()} - 동일 이름 확인
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlScheduler Aggregate Root 테스트")
class CrawlSchedulerTest {

    private static final Instant DEFAULT_INSTANT = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("forNew() 신규 생성 테스트")
    class ForNew {

        @Test
        @DisplayName("유효한 파라미터로 신규 스케줄러 생성 시 ID=null, ACTIVE 상태")
        void shouldCreateNewSchedulerWithNullIdAndActiveStatus() {
            // given
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SchedulerName schedulerName = SchedulerNameFixture.aDefaultName();
            CronExpression cronExpression = CronExpressionFixture.aDefaultCron();

            // when
            CrawlScheduler scheduler =
                    CrawlScheduler.forNew(sellerId, schedulerName, cronExpression, DEFAULT_INSTANT);

            // then
            assertThat(scheduler.getCrawlSchedulerId()).isNull();
            assertThat(scheduler.getSellerId()).isEqualTo(sellerId);
            assertThat(scheduler.getSchedulerName()).isEqualTo(schedulerName);
            assertThat(scheduler.getCronExpression()).isEqualTo(cronExpression);
            assertThat(scheduler.getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(scheduler.isActive()).isTrue();
        }

        @Test
        @DisplayName("신규 생성 시 createdAt과 updatedAt이 현재 시각으로 설정됨")
        void shouldSetCreatedAtAndUpdatedAtToCurrentTime() {
            // given
            Instant expectedTime = FixedClock.aDefaultClock().instant();

            // when
            CrawlScheduler scheduler =
                    CrawlScheduler.forNew(
                            SellerIdFixture.anAssignedId(),
                            SchedulerNameFixture.aDefaultName(),
                            CronExpressionFixture.aDefaultCron(),
                            expectedTime);

            // then
            assertThat(scheduler.getCreatedAt()).isEqualTo(expectedTime);
            assertThat(scheduler.getUpdatedAt()).isEqualTo(expectedTime);
        }
    }

    @Nested
    @DisplayName("reconstitute() 영속성 복원 테스트")
    class Reconstitute {

        @Test
        @DisplayName("유효한 파라미터로 스케줄러 복원 성공")
        void shouldReconstituteSchedulerWithValidParameters() {
            // given
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SchedulerName schedulerName = SchedulerNameFixture.aDefaultName();
            CronExpression cronExpression = CronExpressionFixture.aDefaultCron();
            Instant now = Instant.now();

            // when
            CrawlScheduler scheduler =
                    CrawlScheduler.reconstitute(
                            schedulerId,
                            sellerId,
                            schedulerName,
                            cronExpression,
                            SchedulerStatus.ACTIVE,
                            now,
                            now);

            // then
            assertThat(scheduler.getCrawlSchedulerId()).isEqualTo(schedulerId);
            assertThat(scheduler.getCrawlSchedulerIdValue()).isEqualTo(1L);
            assertThat(scheduler.getSellerId()).isEqualTo(sellerId);
            assertThat(scheduler.getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
        }

        @Test
        @DisplayName("영속성 복원 시 INACTIVE 상태 복원")
        void shouldReconstituteInactiveScheduler() {
            // given
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SchedulerName schedulerName = SchedulerNameFixture.aDefaultName();
            CronExpression cronExpression = CronExpressionFixture.aDefaultCron();
            Instant now = Instant.now();

            // when
            CrawlScheduler scheduler =
                    CrawlScheduler.reconstitute(
                            schedulerId,
                            sellerId,
                            schedulerName,
                            cronExpression,
                            SchedulerStatus.INACTIVE,
                            now,
                            now);

            // then
            assertThat(scheduler.getCrawlSchedulerId()).isEqualTo(schedulerId);
            assertThat(scheduler.getStatus()).isEqualTo(SchedulerStatus.INACTIVE);
            assertThat(scheduler.isInactive()).isTrue();
        }
    }

    @Nested
    @DisplayName("update() 통합 수정 테스트")
    class Update {

        @Test
        @DisplayName("유효한 파라미터로 스케줄러 정보 수정 성공")
        void shouldUpdateSchedulerWithValidParameters() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            SchedulerName newName = SchedulerNameFixture.aName("updated-scheduler");
            CronExpression newCron = CronExpressionFixture.aDailyMidnightCron();
            SchedulerStatus newStatus = SchedulerStatus.INACTIVE;

            // when
            CrawlSchedulerUpdateData updateData =
                    CrawlSchedulerUpdateData.of(newName, newCron, newStatus);
            scheduler.update(updateData, DEFAULT_INSTANT);

            // then
            assertThat(scheduler.getSchedulerName()).isEqualTo(newName);
            assertThat(scheduler.getCronExpression()).isEqualTo(newCron);
            assertThat(scheduler.getStatus()).isEqualTo(newStatus);
            assertThat(scheduler.isInactive()).isTrue();
        }

        @Test
        @DisplayName("수정 후 updatedAt이 현재 시각으로 갱신됨")
        void shouldUpdateUpdatedAtToCurrentTime() {
            // given
            Instant updateTime = Instant.parse("2025-11-27T12:00:00Z");
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // when
            CrawlSchedulerUpdateData updateData =
                    CrawlSchedulerUpdateData.of(
                            SchedulerNameFixture.aDefaultName(),
                            CronExpressionFixture.aDefaultCron(),
                            SchedulerStatus.ACTIVE);
            scheduler.update(updateData, updateTime);

            // then
            assertThat(scheduler.getUpdatedAt()).isEqualTo(updateTime);
        }
    }

    @Nested
    @DisplayName("hasSameSchedulerName() 이름 비교 테스트")
    class HasSameSchedulerName {

        @Test
        @DisplayName("동일한 이름이면 true 반환")
        void shouldReturnTrueWhenNameIsSame() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // when & then
            assertThat(scheduler.hasSameSchedulerName("test-scheduler")).isTrue();
        }

        @Test
        @DisplayName("다른 이름이면 false 반환")
        void shouldReturnFalseWhenNameIsDifferent() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // when & then
            assertThat(scheduler.hasSameSchedulerName("other-scheduler")).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter 메서드 테스트")
    class GetterMethods {

        @Test
        @DisplayName("getCrawlSchedulerIdValue()는 ID의 원시값 반환")
        void shouldReturnSchedulerIdValue() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler(2L);

            // when & then
            assertThat(scheduler.getCrawlSchedulerIdValue()).isEqualTo(2L);
        }

        @Test
        @DisplayName("getCrawlSchedulerIdValue()는 ID가 null이면 null 반환")
        void shouldReturnNullWhenSchedulerIdIsNull() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.aNewActiveScheduler();

            // when & then
            assertThat(scheduler.getCrawlSchedulerIdValue()).isNull();
        }

        @Test
        @DisplayName("getSellerIdValue()는 SellerId의 원시값 반환")
        void shouldReturnSellerIdValue() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // when & then
            assertThat(scheduler.getSellerIdValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getSchedulerNameValue()는 이름의 원시값 반환")
        void shouldReturnSchedulerNameValue() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // when & then
            assertThat(scheduler.getSchedulerNameValue()).isEqualTo("test-scheduler");
        }

        @Test
        @DisplayName("getCronExpressionValue()는 크론 표현식의 원시값 반환")
        void shouldReturnCronExpressionValue() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // when & then
            assertThat(scheduler.getCronExpressionValue()).isEqualTo("cron(0 * * * ? *)");
        }

        @Test
        @DisplayName("isActive()는 ACTIVE 상태일 때 true 반환")
        void shouldReturnTrueWhenActive() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // when & then
            assertThat(scheduler.isActive()).isTrue();
            assertThat(scheduler.isInactive()).isFalse();
        }

        @Test
        @DisplayName("isInactive()는 INACTIVE 상태일 때 true 반환")
        void shouldReturnTrueWhenInactive() {
            // given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anInactiveScheduler();

            // when & then
            assertThat(scheduler.isInactive()).isTrue();
            assertThat(scheduler.isActive()).isFalse();
        }
    }
}
