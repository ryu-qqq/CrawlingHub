package com.ryuqq.crawlinghub.domain.task.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Clock;
import java.time.Instant;

/**
 * CrawlTask 등록 이벤트
 *
 * <p><strong>용도</strong>: CrawlTask가 등록될 때 발행하여 SQS 발행을 트리거합니다.
 *
 * @param crawlTaskId CrawlTask ID
 * @param crawlSchedulerId 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param taskType 태스크 유형
 * @param endpoint 크롤링 엔드포인트
 * @param outboxPayload Outbox 페이로드 (JSON)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskRegisteredEvent(
        CrawlTaskId crawlTaskId,
        CrawlSchedulerId crawlSchedulerId,
        SellerId sellerId,
        CrawlTaskType taskType,
        CrawlEndpoint endpoint,
        String outboxPayload,
        Instant occurredAt)
        implements DomainEvent {

    /** Compact Constructor (검증 로직) */
    public CrawlTaskRegisteredEvent {
        if (crawlTaskId == null) {
            throw new IllegalArgumentException("crawlTaskId는 null일 수 없습니다.");
        }
        if (crawlSchedulerId == null) {
            throw new IllegalArgumentException("crawlSchedulerId는 null일 수 없습니다.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 null일 수 없습니다.");
        }
        if (taskType == null) {
            throw new IllegalArgumentException("taskType은 null일 수 없습니다.");
        }
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint는 null일 수 없습니다.");
        }
        if (outboxPayload == null || outboxPayload.isBlank()) {
            throw new IllegalArgumentException("outboxPayload는 null이거나 빈 값일 수 없습니다.");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt은 null일 수 없습니다.");
        }
    }

    /**
     * 팩토리 메서드 (도메인 규칙)
     *
     * @param crawlTaskId CrawlTask ID
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param taskType 태스크 유형
     * @param endpoint 크롤링 엔드포인트
     * @param outboxPayload Outbox 페이로드
     * @param clock 시간 제어
     * @return CrawlTaskRegisteredEvent
     */
    public static CrawlTaskRegisteredEvent of(
            CrawlTaskId crawlTaskId,
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            CrawlTaskType taskType,
            CrawlEndpoint endpoint,
            String outboxPayload,
            Clock clock) {
        return new CrawlTaskRegisteredEvent(
                crawlTaskId,
                crawlSchedulerId,
                sellerId,
                taskType,
                endpoint,
                outboxPayload,
                clock.instant());
    }

    public Long getCrawlTaskIdValue() {
        return crawlTaskId.value();
    }

    public Long getCrawlSchedulerIdValue() {
        return crawlSchedulerId.value();
    }

    public Long getSellerIdValue() {
        return sellerId.value();
    }

    public String getEndpointUrl() {
        return endpoint.toFullUrl();
    }
}
