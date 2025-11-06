package com.ryuqq.crawlinghub.domain.schedule.outbox;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * ScheduleOutbox Test Fixture (Object Mother Pattern)
 *
 * <p><strong>역할:</strong></p>
 * <ul>
 *   <li>✅ ScheduleOutbox 도메인 테스트 객체 생성</li>
 *   <li>✅ Object Mother 패턴 적용</li>
 *   <li>✅ 다양한 시나리오별 테스트 데이터 제공</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public class ScheduleOutboxFixture {

    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_OP_ID = "op-12345-abcde";
    private static final Long DEFAULT_SELLER_ID = 100L;
    private static final String DEFAULT_IDEM_KEY = "idem-key-12345";
    private static final String DEFAULT_DOMAIN = "seller";
    private static final String DEFAULT_EVENT_TYPE = "ScheduleCreated";
    private static final String DEFAULT_BIZ_KEY = "biz-seller-100";
    private static final String DEFAULT_PAYLOAD = "{\"sellerId\":100,\"scheduleId\":1}";
    private static final String DEFAULT_OUTCOME_JSON = "{\"status\":\"ok\"}";
    private static final Integer DEFAULT_RETRY_COUNT = 0;
    private static final Integer DEFAULT_MAX_RETRIES = 3;
    private static final Long DEFAULT_TIMEOUT_MILLIS = 30000L;

    private static final Clock DEFAULT_CLOCK = Clock.fixed(
        Instant.parse("2025-01-01T00:00:00Z"),
        ZoneId.systemDefault()
    );

    /**
     * 기본 ScheduleOutbox 생성 (신규, ID 없음)
     *
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox create() {
        return ScheduleOutbox.forNew(
            null,  // opId는 나중에 설정
            DEFAULT_SELLER_ID,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS
        );
    }

    /**
     * ID를 가진 ScheduleOutbox 생성
     *
     * @param id Outbox ID
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createWithId(Long id) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ScheduleOutbox.reconstitute(
            id,
            DEFAULT_OP_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            null,  // outcomeJson
            ScheduleOutbox.OperationState.PENDING,
            ScheduleOutbox.WriteAheadState.PENDING,
            null,  // errorMessage
            DEFAULT_RETRY_COUNT,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS,
            null,  // completedAt
            now,
            now
        );
    }

    /**
     * 특정 Idempotency Key로 ScheduleOutbox 생성
     *
     * @param idemKey Idempotency Key
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createWithIdemKey(String idemKey) {
        return ScheduleOutbox.forNew(
            null,
            DEFAULT_SELLER_ID,
            idemKey,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS
        );
    }

    /**
     * PENDING 상태의 ScheduleOutbox 생성 (WAL State)
     *
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createPending() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ScheduleOutbox.reconstitute(
            DEFAULT_ID,
            DEFAULT_OP_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            null,
            ScheduleOutbox.OperationState.PENDING,
            ScheduleOutbox.WriteAheadState.PENDING,
            null,
            DEFAULT_RETRY_COUNT,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS,
            null,
            now,
            now
        );
    }

    /**
     * IN_PROGRESS 상태의 ScheduleOutbox 생성 (Operation State)
     *
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createInProgress() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ScheduleOutbox.reconstitute(
            DEFAULT_ID,
            DEFAULT_OP_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            DEFAULT_OUTCOME_JSON,
            ScheduleOutbox.OperationState.IN_PROGRESS,
            ScheduleOutbox.WriteAheadState.PENDING,
            null,
            DEFAULT_RETRY_COUNT,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS,
            null,
            now,
            now
        );
    }

    /**
     * COMPLETED 상태의 ScheduleOutbox 생성
     *
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createCompleted() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ScheduleOutbox.reconstitute(
            DEFAULT_ID,
            DEFAULT_OP_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            DEFAULT_OUTCOME_JSON,
            ScheduleOutbox.OperationState.COMPLETED,
            ScheduleOutbox.WriteAheadState.COMPLETED,
            null,
            DEFAULT_RETRY_COUNT,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS,
            now,
            now,
            now
        );
    }

    /**
     * FAILED 상태의 ScheduleOutbox 생성 (재시도 가능)
     *
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createFailed() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ScheduleOutbox.reconstitute(
            DEFAULT_ID,
            DEFAULT_OP_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            DEFAULT_OUTCOME_JSON,
            ScheduleOutbox.OperationState.FAILED,
            ScheduleOutbox.WriteAheadState.PENDING,
            "External API timeout",
            1,  // retryCount
            DEFAULT_MAX_RETRIES,  // maxRetries
            DEFAULT_TIMEOUT_MILLIS,
            null,
            now,
            now
        );
    }

    /**
     * 재시도 횟수 초과로 FAILED 상태의 ScheduleOutbox 생성
     *
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createFailedExceedRetry() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ScheduleOutbox.reconstitute(
            DEFAULT_ID,
            DEFAULT_OP_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            DEFAULT_OUTCOME_JSON,
            ScheduleOutbox.OperationState.FAILED,
            ScheduleOutbox.WriteAheadState.PENDING,
            "Max retries exceeded",
            DEFAULT_MAX_RETRIES,  // retryCount = maxRetries
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS,
            null,
            now,
            now
        );
    }

    /**
     * 특정 Seller ID로 ScheduleOutbox 생성
     *
     * @param sellerId Seller ID
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createWithSellerId(Long sellerId) {
        return ScheduleOutbox.forNew(
            null,
            sellerId,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS
        );
    }

    /**
     * 특정 OpId로 ScheduleOutbox 생성
     *
     * @param opId Orchestrator OpId
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createWithOpId(String opId) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ScheduleOutbox.reconstitute(
            DEFAULT_ID,
            opId,
            DEFAULT_SELLER_ID,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            null,
            ScheduleOutbox.OperationState.PENDING,
            ScheduleOutbox.WriteAheadState.PENDING,
            null,
            DEFAULT_RETRY_COUNT,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS,
            null,
            now,
            now
        );
    }

    /**
     * DB reconstitute용 ScheduleOutbox 생성
     *
     * @param id Outbox ID
     * @param operationState Operation State
     * @param walState WAL State
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox reconstitute(
        Long id,
        ScheduleOutbox.OperationState operationState,
        ScheduleOutbox.WriteAheadState walState
    ) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return ScheduleOutbox.reconstitute(
            id,
            DEFAULT_OP_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_IDEM_KEY,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            DEFAULT_OUTCOME_JSON,
            operationState,
            walState,
            null,
            DEFAULT_RETRY_COUNT,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS,
            null,
            now,
            now
        );
    }

    /**
     * 완전한 커스텀 ScheduleOutbox 생성
     *
     * @param id Outbox ID (null 가능)
     * @param sellerId Seller ID
     * @param idemKey Idempotency Key
     * @param operationState Operation State
     * @param walState WAL State
     * @return ScheduleOutbox
     */
    public static ScheduleOutbox createCustom(
        Long id,
        Long sellerId,
        String idemKey,
        ScheduleOutbox.OperationState operationState,
        ScheduleOutbox.WriteAheadState walState
    ) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);

        if (id == null) {
            return ScheduleOutbox.forNew(
                null,
                sellerId,
                idemKey,
                DEFAULT_DOMAIN,
                DEFAULT_EVENT_TYPE,
                DEFAULT_BIZ_KEY,
                DEFAULT_PAYLOAD,
                DEFAULT_MAX_RETRIES,
                DEFAULT_TIMEOUT_MILLIS
            );
        }

        return ScheduleOutbox.reconstitute(
            id,
            DEFAULT_OP_ID,
            sellerId,
            idemKey,
            DEFAULT_DOMAIN,
            DEFAULT_EVENT_TYPE,
            DEFAULT_BIZ_KEY,
            DEFAULT_PAYLOAD,
            DEFAULT_OUTCOME_JSON,
            operationState,
            walState,
            null,
            DEFAULT_RETRY_COUNT,
            DEFAULT_MAX_RETRIES,
            DEFAULT_TIMEOUT_MILLIS,
            null,
            now,
            now
        );
    }
}
