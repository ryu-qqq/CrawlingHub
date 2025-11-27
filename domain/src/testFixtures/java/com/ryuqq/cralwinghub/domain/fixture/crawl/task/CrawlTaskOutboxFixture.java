package com.ryuqq.cralwinghub.domain.fixture.crawl.task;

import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.LocalDateTime;

/**
 * CrawlTaskOutbox 테스트 Fixture
 *
 * <p>Object Mother 패턴: 테스트용 CrawlTaskOutbox 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlTaskOutboxFixture {

    private CrawlTaskOutboxFixture() {
        throw new UnsupportedOperationException("Fixture 클래스입니다.");
    }

    /**
     * PENDING 상태의 Outbox 생성
     *
     * @return PENDING 상태 CrawlTaskOutbox
     */
    public static CrawlTaskOutbox aPendingOutbox() {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(1L),
                "outbox-1-abcd1234",
                "{\"taskId\": 1, \"sellerId\": 100}",
                OutboxStatus.PENDING,
                0,
                LocalDateTime.now(),
                null);
    }

    /**
     * SENT 상태의 Outbox 생성
     *
     * @return SENT 상태 CrawlTaskOutbox
     */
    public static CrawlTaskOutbox aSentOutbox() {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(2L),
                "outbox-2-efgh5678",
                "{\"taskId\": 2, \"sellerId\": 100}",
                OutboxStatus.SENT,
                0,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now());
    }

    /**
     * FAILED 상태의 Outbox 생성
     *
     * @return FAILED 상태 CrawlTaskOutbox
     */
    public static CrawlTaskOutbox aFailedOutbox() {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(3L),
                "outbox-3-ijkl9012",
                "{\"taskId\": 3, \"sellerId\": 100}",
                OutboxStatus.FAILED,
                1,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(5));
    }

    /**
     * 재시도 가능한 FAILED 상태의 Outbox 생성
     *
     * @return 재시도 가능한 FAILED 상태 CrawlTaskOutbox
     */
    public static CrawlTaskOutbox aRetryableFailedOutbox() {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(4L),
                "outbox-4-mnop3456",
                "{\"taskId\": 4, \"sellerId\": 100}",
                OutboxStatus.FAILED,
                2,
                LocalDateTime.now().minusMinutes(15),
                LocalDateTime.now().minusMinutes(10));
    }

    /**
     * 최대 재시도 횟수 도달한 FAILED 상태의 Outbox 생성
     *
     * @return 재시도 불가 FAILED 상태 CrawlTaskOutbox
     */
    public static CrawlTaskOutbox aMaxRetriedFailedOutbox() {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(5L),
                "outbox-5-qrst7890",
                "{\"taskId\": 5, \"sellerId\": 100}",
                OutboxStatus.FAILED,
                3,
                LocalDateTime.now().minusMinutes(20),
                LocalDateTime.now().minusMinutes(15));
    }
}
