package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.aggregate.ProductOutbox;
import com.ryuqq.crawlinghub.domain.vo.OutboxEventType;
import com.ryuqq.crawlinghub.domain.vo.OutboxId;
import com.ryuqq.crawlinghub.domain.vo.OutboxStatus;
import com.ryuqq.crawlinghub.domain.vo.ProductId;

import java.time.Clock;

/**
 * ProductOutbox 관련 테스트 데이터 생성 Fixture
 *
 * <p>ProductOutbox와 관련된 Value Object, Enum, Aggregate의 기본값을 제공합니다.</p>
 *
 * <p>표준 패턴 준수:</p>
 * <ul>
 *   <li>{@link #forNew()} - 새 ProductOutbox 생성 (ID 자동 생성)</li>
 *   <li>{@link #of(OutboxId, ProductId, OutboxEventType, String)} - 불변 속성으로 재구성</li>
 *   <li>{@link #reconstitute(OutboxId, ProductId, OutboxEventType, String, OutboxStatus, Integer, String)} - 완전한 재구성</li>
 * </ul>
 *
 * <p>레거시 호환 메서드:</p>
 * <ul>
 *   <li>{@link #defaultOutboxId()} - 새로운 OutboxId 생성</li>
 *   <li>{@link #defaultOutboxEventType()} - 기본 이벤트 타입 (PRODUCT_CREATED)</li>
 *   <li>{@link #defaultOutboxStatus()} - 기본 상태 (WAITING)</li>
 *   <li>{@link #waitingOutbox()} - WAITING 상태의 ProductOutbox (레거시)</li>
 *   <li>{@link #failedOutboxWithRetryCount(int)} - FAILED 상태의 ProductOutbox (레거시)</li>
 * </ul>
 */
public class ProductOutboxFixture {

    private static final String DEFAULT_PAYLOAD = "{\"itemNo\":123456,\"name\":\"상품명\"}";

    /**
     * 새로운 ProductOutbox 생성 (표준 패턴)
     *
     * <p>forNew() 패턴: ID 자동 생성, WAITING 상태, retryCount=0</p>
     *
     * @return 새로 생성된 ProductOutbox
     */
    public static ProductOutbox forNew() {
        return forNew(Clock.systemDefaultZone());
    }

    /**
     * 새로운 ProductOutbox 생성 (표준 패턴 + Clock 주입)
     *
     * <p>forNew(Clock) 패턴: ID 자동 생성, WAITING 상태, retryCount=0, Clock 주입</p>
     * <p>⚠️ Tidy First: Clock 파라미터는 준비되었지만, ProductOutbox가 Clock을 지원할 때까지 무시됩니다.</p>
     *
     * @param clock 시간 제어 (테스트 가능성) - 현재는 무시됨
     * @return 새로 생성된 ProductOutbox
     */
    public static ProductOutbox forNew(Clock clock) {
        ProductId productId = ProductFixture.defaultProductId();
        OutboxEventType eventType = OutboxEventType.PRODUCT_CREATED;
        // TODO: ProductOutbox가 Clock을 지원하면 clock 파라미터 전달
        return ProductOutbox.forNew(productId, eventType, DEFAULT_PAYLOAD);
    }

    /**
     * 불변 속성으로 ProductOutbox 재구성 (표준 패턴)
     *
     * <p>of() 패턴: ID 포함, 테스트용 간편 생성</p>
     *
     * @param outboxId Outbox ID
     * @param productId Product ID
     * @param eventType 이벤트 타입
     * @param payload 페이로드
     * @return 재구성된 ProductOutbox
     */
    public static ProductOutbox of(OutboxId outboxId, ProductId productId, OutboxEventType eventType, String payload) {
        return of(outboxId, productId, eventType, payload, Clock.systemDefaultZone());
    }

    /**
     * 불변 속성으로 ProductOutbox 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>of(Clock) 패턴: ID 포함, 테스트용 간편 생성, Clock 주입</p>
     * <p>⚠️ Tidy First: Clock 파라미터는 준비되었지만, ProductOutbox가 Clock을 지원할 때까지 무시됩니다.</p>
     *
     * @param outboxId Outbox ID
     * @param productId Product ID
     * @param eventType 이벤트 타입
     * @param payload 페이로드
     * @param clock 시간 제어 - 현재는 무시됨
     * @return 재구성된 ProductOutbox
     */
    public static ProductOutbox of(OutboxId outboxId, ProductId productId, OutboxEventType eventType, String payload, Clock clock) {
        // TODO: ProductOutbox가 Clock을 지원하면 clock 파라미터 전달
        return ProductOutbox.of(productId, eventType, payload);
    }

    /**
     * 완전한 ProductOutbox 재구성 (표준 패턴)
     *
     * <p>reconstitute() 패턴: 모든 필드 포함, DB 조회 시뮬레이션</p>
     *
     * @param outboxId Outbox ID
     * @param productId Product ID
     * @param eventType 이벤트 타입
     * @param payload 페이로드
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @return 재구성된 ProductOutbox
     */
    public static ProductOutbox reconstitute(OutboxId outboxId, ProductId productId, OutboxEventType eventType,
                                              String payload, OutboxStatus status, Integer retryCount, String errorMessage) {
        return reconstitute(outboxId, productId, eventType, payload, status, retryCount, errorMessage, Clock.systemDefaultZone());
    }

    /**
     * 완전한 ProductOutbox 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>reconstitute(Clock) 패턴: 모든 필드 포함, DB 조회 시뮬레이션, Clock 주입</p>
     * <p>⚠️ Tidy First: Clock 파라미터는 준비되었지만, ProductOutbox가 Clock을 지원할 때까지 무시됩니다.</p>
     *
     * @param outboxId Outbox ID
     * @param productId Product ID
     * @param eventType 이벤트 타입
     * @param payload 페이로드
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param clock 시간 제어 - 현재는 무시됨
     * @return 재구성된 ProductOutbox
     */
    public static ProductOutbox reconstitute(OutboxId outboxId, ProductId productId, OutboxEventType eventType,
                                              String payload, OutboxStatus status, Integer retryCount, String errorMessage, Clock clock) {
        // TODO: ProductOutbox가 Clock을 지원하면 clock 파라미터 전달
        return ProductOutbox.reconstitute(productId, eventType, payload, status, retryCount, errorMessage);
    }

    /**
     * 기본 OutboxId 생성
     *
     * @return 새로운 UUID 기반 OutboxId
     */
    public static OutboxId defaultOutboxId() {
        return OutboxId.generate();
    }

    /**
     * 기본 OutboxEventType 반환
     *
     * @return PRODUCT_CREATED 타입
     */
    public static OutboxEventType defaultOutboxEventType() {
        return OutboxEventType.PRODUCT_CREATED;
    }

    /**
     * 기본 OutboxStatus 반환
     *
     * @return WAITING 상태
     */
    public static OutboxStatus defaultOutboxStatus() {
        return OutboxStatus.WAITING;
    }

    /**
     * OutboxStatus.WAITING 상태의 ProductOutbox 생성 (레거시)
     *
     * @deprecated Use {@link #forNew()} instead
     * @return WAITING 상태의 ProductOutbox
     */
    @Deprecated
    public static ProductOutbox waitingOutbox() {
        return forNew();
    }

    /**
     * OutboxStatus.FAILED 상태의 ProductOutbox 생성 (지정된 재시도 횟수)
     *
     * <p>재시도 로직 테스트를 위해 사용됩니다.</p>
     * <p>실제 fail() 메서드를 호출하여 retryCount를 설정합니다.</p>
     *
     * @param retryCount 재시도 횟수
     * @return FAILED 상태의 ProductOutbox (지정된 retryCount)
     */
    public static ProductOutbox failedOutboxWithRetryCount(int retryCount) {
        ProductOutbox outbox = waitingOutbox();
        outbox.send();

        // fail() 메서드를 retryCount번 호출하여 retryCount 설정
        for (int i = 0; i < retryCount; i++) {
            outbox.fail("테스트 에러 메시지 " + (i + 1));
            if (i < retryCount - 1) {
                // 마지막 반복이 아니면 다시 SENDING 상태로 전환
                outbox.send();
            }
        }

        return outbox;
    }
}
