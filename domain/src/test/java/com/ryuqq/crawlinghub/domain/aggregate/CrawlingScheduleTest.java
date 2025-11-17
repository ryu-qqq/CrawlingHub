package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.schedule.CrawlingSchedule;
import com.ryuqq.crawlinghub.domain.crawler.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrawlingSchedule Aggregate Root 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ CrawlingSchedule 생성 (ACTIVE 상태)</li>
 *   <li>✅ scheduleRule 자동 생성 (mustit-crawler-seller_{sellerId})</li>
 *   <li>✅ scheduleExpression 자동 변환 (rate(1 day), rate(6 hours))</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class CrawlingScheduleTest {

    @Test
    void shouldCreateCrawlingScheduleWithActiveStatus() {
        // Given
        SellerId sellerId = new SellerId("seller_12345");
        CrawlingInterval interval = new CrawlingInterval(1, ChronoUnit.DAYS);

        // When
        CrawlingSchedule schedule = CrawlingSchedule.create(sellerId, interval);

        // Then
        assertThat(schedule.getScheduleId()).isNotNull();
        assertThat(schedule.getSellerId()).isEqualTo(sellerId);
        assertThat(schedule.getCrawlingInterval()).isEqualTo(interval);
        assertThat(schedule.getScheduleRule()).isEqualTo("mustit-crawler-seller_12345");
        assertThat(schedule.getScheduleExpression()).isEqualTo("rate(1 day)");
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
    }

    @Test
    void shouldGenerateCorrectScheduleExpressionForHourInterval() {
        // Given
        SellerId sellerId = new SellerId("seller_67890");
        CrawlingInterval interval = new CrawlingInterval(6, ChronoUnit.HOURS);

        // When
        CrawlingSchedule schedule = CrawlingSchedule.create(sellerId, interval);

        // Then
        assertThat(schedule.getScheduleExpression()).isEqualTo("rate(6 hours)");
    }
}
