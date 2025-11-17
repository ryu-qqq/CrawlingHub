package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.schedule.CrawlingSchedule;
import com.ryuqq.crawlinghub.domain.crawler.exception.CrawlingScheduleInvalidStateException;
import com.ryuqq.crawlinghub.domain.crawler.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleStatus;
import com.ryuqq.crawlinghub.domain.fixture.CrawlingScheduleFixture;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlingSchedule Aggregate Root 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ CrawlingSchedule 생성 (ACTIVE 상태)</li>
 *   <li>✅ scheduleRule 자동 생성 (mustit-crawler-seller_{sellerId})</li>
 *   <li>✅ scheduleExpression 자동 변환 (rate(1 day), rate(6 hours))</li>
 *   <li>✅ CrawlingSchedule 주기 변경 (updateInterval)</li>
 *   <li>✅ INACTIVE 상태에서 주기 변경 불가 검증</li>
 *   <li>✅ CrawlingSchedule 비활성화 (deactivate)</li>
 *   <li>✅ CrawlingSchedule 활성화 (activate)</li>
 *   <li>✅ 이미 ACTIVE인 스케줄 활성화 시도 시 예외 발생</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class CrawlingScheduleTest {

    @Test
    void shouldCreateCrawlingScheduleWithActiveStatus() {
        // Given
        SellerId sellerId = new SellerId(1L);
        CrawlingInterval interval = new CrawlingInterval(1, ChronoUnit.DAYS);

        // When
        CrawlingSchedule schedule = CrawlingSchedule.create(sellerId, interval);

        // Then
        assertThat(schedule.getScheduleId()).isNotNull();
        assertThat(schedule.getSellerId()).isEqualTo(sellerId);
        assertThat(schedule.getCrawlingInterval()).isEqualTo(interval);
        assertThat(schedule.getScheduleRule()).isEqualTo("mustit-crawler-1");
        assertThat(schedule.getScheduleExpression()).isEqualTo("rate(1 day)");
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
    }

    @Test
    void shouldGenerateCorrectScheduleExpressionForHourInterval() {
        // Given
        SellerId sellerId = new SellerId(1L);
        CrawlingInterval interval = new CrawlingInterval(6, ChronoUnit.HOURS);

        // When
        CrawlingSchedule schedule = CrawlingSchedule.create(sellerId, interval);

        // Then
        assertThat(schedule.getScheduleExpression()).isEqualTo("rate(6 hours)");
    }

    @Test
    void shouldUpdateCrawlingInterval() {
        // Given
        CrawlingSchedule schedule = CrawlingScheduleFixture.defaultSchedule();
        CrawlingInterval newInterval = new CrawlingInterval(12, ChronoUnit.HOURS);

        // When
        schedule.updateInterval(newInterval);

        // Then
        assertThat(schedule.getCrawlingInterval()).isEqualTo(newInterval);
        assertThat(schedule.getScheduleExpression()).isEqualTo("rate(12 hours)");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingInactiveSchedule() {
        // Given
        CrawlingSchedule schedule = CrawlingScheduleFixture.inactiveSchedule();
        CrawlingInterval newInterval = new CrawlingInterval(1, ChronoUnit.DAYS);

        // When & Then
        assertThatThrownBy(() -> schedule.updateInterval(newInterval))
            .isInstanceOf(CrawlingScheduleInvalidStateException.class)
            .hasMessageContaining("Cannot updateInterval schedule")
            .hasMessageContaining("INACTIVE");
    }

    @Test
    void shouldDeactivateSchedule() {
        // Given
        CrawlingSchedule schedule = CrawlingScheduleFixture.defaultSchedule();

        // When
        schedule.deactivate();

        // Then
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.INACTIVE);
    }

    @Test
    void shouldActivateSchedule() {
        // Given
        CrawlingSchedule schedule = CrawlingScheduleFixture.inactiveSchedule();

        // When
        schedule.activate();

        // Then
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
    }

    @Test
    void shouldThrowExceptionWhenActivatingActiveSchedule() {
        // Given
        CrawlingSchedule schedule = CrawlingScheduleFixture.defaultSchedule();

        // When & Then
        assertThatThrownBy(() -> schedule.activate())
            .isInstanceOf(CrawlingScheduleInvalidStateException.class)
            .hasMessageContaining("Cannot activate schedule")
            .hasMessageContaining("ACTIVE");
    }
}
