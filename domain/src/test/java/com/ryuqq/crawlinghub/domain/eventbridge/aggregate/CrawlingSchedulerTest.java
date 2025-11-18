package com.ryuqq.crawlinghub.domain.eventbridge.aggregate;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.crawlinghub.domain.eventbridge.event.SchedulerUpdatedEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CronExpressionFixture;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.SchedulerStatusFixture;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.SchedulerUpdatedEventFixture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("CrawlingScheduler Aggregate - 생성")
class CrawlingSchedulerTest {

    @Test
    @DisplayName("forNew() 호출 시 기본 상태는 PENDING이다")
    void shouldCreateSchedulerWithForNew() {
        Long sellerId = CrawlingSchedulerFixture.aSellerId();
        String schedulerName = CrawlingSchedulerFixture.aSchedulerName();
        var cronExpression = CronExpressionFixture.aCronExpression();

        CrawlingScheduler scheduler = CrawlingScheduler.forNew(
            sellerId,
            schedulerName,
            cronExpression,
            CrawlingSchedulerFixture.aFixedClock()
        );

        assertAll(
            () -> assertNotNull(scheduler),
            () -> assertEquals(SchedulerStatus.PENDING, scheduler.getStatus()),
            () -> assertEquals(sellerId, scheduler.getSellerId()),
            () -> assertEquals(schedulerName, scheduler.getSchedulerName()),
            () -> assertEquals(cronExpression, scheduler.getCronExpression())
        );
    }

    @Test
    @DisplayName("of() 호출 시 기존 스케줄러 정보를 기반으로 Aggregate를 생성한다")
    void shouldCreateSchedulerWithOf() {
        Long schedulerId = CrawlingSchedulerFixture.aSchedulerId();
        Long sellerId = CrawlingSchedulerFixture.aSellerId();
        String schedulerName = "updated-name";
        var cronExpression = CrawlingSchedulerFixture.anotherCronExpression();
        SchedulerStatus status = SchedulerStatusFixture.active();

        CrawlingScheduler scheduler = CrawlingScheduler.of(
            schedulerId,
            sellerId,
            schedulerName,
            cronExpression,
            status,
            CrawlingSchedulerFixture.aFixedClock()
        );

        assertAll(
            () -> assertEquals(schedulerId, scheduler.getSchedulerId()),
            () -> assertEquals(sellerId, scheduler.getSellerId()),
            () -> assertEquals(schedulerName, scheduler.getSchedulerName()),
            () -> assertEquals(cronExpression, scheduler.getCronExpression()),
            () -> assertEquals(status, scheduler.getStatus()),
            () -> assertNotNull(scheduler.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("reconstitute() 호출 시 영속화된 값을 기반으로 Aggregate를 복원한다")
    void shouldReconstituteSchedulerFromPersistence() {
        CrawlingScheduler expected = CrawlingSchedulerFixture.aReconstitutedScheduler();

        CrawlingScheduler scheduler = CrawlingScheduler.reconstitute(
            expected.getSchedulerId(),
            expected.getSellerId(),
            expected.getSchedulerName(),
            expected.getCronExpression(),
            expected.getStatus(),
            expected.getCreatedAt(),
            expected.getUpdatedAt(),
            CrawlingSchedulerFixture.aFixedClock()
        );

        assertAll(
            () -> assertEquals(expected.getSchedulerId(), scheduler.getSchedulerId()),
            () -> assertEquals(expected.getSellerId(), scheduler.getSellerId()),
            () -> assertEquals(expected.getSchedulerName(), scheduler.getSchedulerName()),
            () -> assertEquals(expected.getCronExpression(), scheduler.getCronExpression()),
            () -> assertEquals(expected.getStatus(), scheduler.getStatus()),
            () -> assertEquals(expected.getCreatedAt(), scheduler.getCreatedAt()),
            () -> assertEquals(expected.getUpdatedAt(), scheduler.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("스케줄러 이름을 업데이트할 수 있다")
    void shouldUpdateSchedulerName() {
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();
        String updatedName = SchedulerUpdatedEventFixture.anUpdatedSchedulerName();

        SchedulerUpdatedEvent event = scheduler.update(
            updatedName,
            scheduler.getCronExpression(),
            scheduler.getStatus()
        );

        assertAll(
            () -> assertEquals(updatedName, scheduler.getSchedulerName()),
            () -> assertEquals(LocalDateTime.now(CrawlingSchedulerFixture.aFixedClock()), scheduler.getUpdatedAt()),
            () -> assertNotNull(event)
        );
    }

    @Test
    @DisplayName("Cron Expression을 업데이트할 수 있다")
    void shouldUpdateCronExpression() {
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();
        var updatedCronExpression = CrawlingSchedulerFixture.anotherCronExpression();

        scheduler.update(
            scheduler.getSchedulerName(),
            updatedCronExpression,
            scheduler.getStatus()
        );

        assertEquals(updatedCronExpression, scheduler.getCronExpression());
    }

    @Test
    @DisplayName("SchedulerStatus를 업데이트할 수 있다")
    void shouldUpdateStatus() {
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();
        SchedulerStatus updatedStatus = SchedulerStatusFixture.active();

        scheduler.update(
            scheduler.getSchedulerName(),
            scheduler.getCronExpression(),
            updatedStatus
        );

        assertEquals(updatedStatus, scheduler.getStatus());
    }

    @Test
    @DisplayName("스케줄 변경 시 SchedulerUpdatedEvent를 발행한다")
    void shouldPublishSchedulerUpdatedEvent() {
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();
        SchedulerStatus previousStatus = scheduler.getStatus();
        String updatedName = SchedulerUpdatedEventFixture.anUpdatedSchedulerName();
        var updatedCronExpression = SchedulerUpdatedEventFixture.anUpdatedCronExpression();
        SchedulerStatus updatedStatus = SchedulerUpdatedEventFixture.aCurrentStatus();

        SchedulerUpdatedEvent event = scheduler.update(
            updatedName,
            updatedCronExpression,
            updatedStatus
        );

        assertAll(
            () -> assertEquals(scheduler.getSchedulerId(), event.schedulerId()),
            () -> assertEquals(updatedName, event.schedulerName()),
            () -> assertEquals(updatedCronExpression, event.cronExpression()),
            () -> assertEquals(previousStatus, event.previousStatus()),
            () -> assertEquals(updatedStatus, event.currentStatus()),
            () -> assertNotNull(event.occurredAt()),
            () -> assertEquals(1, scheduler.pullDomainEvents().size())
        );
    }

    @Test
    @DisplayName("변경 사항이 없으면 이벤트를 발행하지 않는다")
    void shouldNotPublishEventWhenNoChanges() {
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        SchedulerUpdatedEvent event = scheduler.update(
            scheduler.getSchedulerName(),
            scheduler.getCronExpression(),
            scheduler.getStatus()
        );

        assertAll(
            () -> assertNull(event),
            () -> assertEquals(0, scheduler.pullDomainEvents().size())
        );
    }
}

