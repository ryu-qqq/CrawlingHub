package com.ryuqq.crawlinghub.domain.product.aggregate.productoutbox;

import com.ryuqq.crawlinghub.domain.product.vo.OutboxEventType;
import com.ryuqq.crawlinghub.domain.product.vo.OutboxId;
import com.ryuqq.crawlinghub.domain.product.vo.OutboxStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductId;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * ProductOutbox Aggregate Root
 *
 * <p>Product 이벤트를 외부 시스템으로 전송하기 위한 Outbox 패턴 구현입니다.</p>
 *
 * <p>Zero-Tolerance Rules 준수:</p>
 * <ul>
 *   <li>Lombok 금지 - Plain Java 사용</li>
 *   <li>Tell, Don't Ask - 비즈니스 로직은 ProductOutbox 내부에 캡슐화</li>
 *   <li>Long FK 전략 - JPA 관계 어노테이션 없음</li>
 * </ul>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>생성 시 상태는 항상 WAITING</li>
 *   <li>생성 시 retryCount는 0</li>
 *   <li>상태 전환: WAITING → SENDING → COMPLETED/FAILED</li>
 * </ul>
 */
public class ProductOutbox {

    private static final int MAX_RETRY_COUNT = 5;

    private final OutboxId outboxId;
    private final ProductId productId;
    private final OutboxEventType eventType;
    private final String payload;
    private OutboxStatus status;
    private Integer retryCount;
    private String errorMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Clock clock;

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성
     *
     * @param productId 상품 ID
     * @param eventType 이벤트 타입
     * @param payload 이벤트 페이로드 (JSON)
     * @param clock 시간 제어 (테스트 가능성)
     */
    private ProductOutbox(ProductId productId, OutboxEventType eventType, String payload, Clock clock) {
        this.outboxId = OutboxId.forNew();
        this.productId = productId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.WAITING;
        this.retryCount = 0;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 새로운 ProductOutbox 생성 (표준 패턴)
     *
     * <p>forNew() 패턴: 신규 엔티티 생성</p>
     * <ul>
     *   <li>ID 자동 생성 (OutboxId.forNew())</li>
     *   <li>초기 상태: WAITING</li>
     *   <li>초기 retryCount: 0</li>
     *   <li>errorMessage: null</li>
     *   <li>createdAt/updatedAt: 현재 시각</li>
     * </ul>
     *
     * @param productId 상품 ID
     * @param eventType 이벤트 타입 (PRODUCT_CREATED, PRODUCT_UPDATED 등)
     * @param payload 이벤트 페이로드 JSON
     * @return 새로 생성된 ProductOutbox
     */
    public static ProductOutbox forNew(ProductId productId, OutboxEventType eventType, String payload) {
        return forNew(productId, eventType, payload, Clock.systemDefaultZone());
    }

    /**
     * 새로운 ProductOutbox 생성 (표준 패턴 + Clock 주입)
     *
     * <p>forNew(Clock) 패턴: 신규 엔티티 생성 + Clock 주입</p>
     *
     * @param productId 상품 ID
     * @param eventType 이벤트 타입
     * @param payload 이벤트 페이로드 JSON
     * @param clock 시간 제어 (테스트 가능성)
     * @return 새로 생성된 ProductOutbox
     */
    public static ProductOutbox forNew(ProductId productId, OutboxEventType eventType, String payload, Clock clock) {
        return new ProductOutbox(productId, eventType, payload, clock);
    }

    /**
     * 불변 속성으로 ProductOutbox 재구성 (표준 패턴)
     *
     * <p>of() 패턴: 테스트용 간편 생성</p>
     * <ul>
     *   <li>ID 자동 생성</li>
     *   <li>초기 상태: WAITING</li>
     * </ul>
     *
     * @param productId 상품 ID
     * @param eventType 이벤트 타입
     * @param payload 페이로드
     * @return 재구성된 ProductOutbox
     */
    public static ProductOutbox of(ProductId productId, OutboxEventType eventType, String payload) {
        return of(productId, eventType, payload, Clock.systemDefaultZone());
    }

    /**
     * 불변 속성으로 ProductOutbox 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>of(Clock) 패턴: 테스트용 간편 생성 + Clock 주입</p>
     *
     * @param productId 상품 ID
     * @param eventType 이벤트 타입
     * @param payload 페이로드
     * @param clock 시간 제어
     * @return 재구성된 ProductOutbox
     */
    public static ProductOutbox of(ProductId productId, OutboxEventType eventType, String payload, Clock clock) {
        return new ProductOutbox(productId, eventType, payload, clock);
    }

    /**
     * 완전한 ProductOutbox 재구성 (표준 패턴)
     *
     * <p>reconstitute() 패턴: DB에서 조회한 엔티티 재구성</p>
     * <p>⚠️ 주의: 현재 구현은 임시입니다. 모든 필드를 받는 private 생성자가 필요합니다.</p>
     *
     * @param productId 상품 ID
     * @param eventType 이벤트 타입
     * @param payload 페이로드
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @return 재구성된 ProductOutbox
     */
    public static ProductOutbox reconstitute(ProductId productId, OutboxEventType eventType, String payload,
                                              OutboxStatus status, Integer retryCount, String errorMessage) {
        return reconstitute(productId, eventType, payload, status, retryCount, errorMessage, Clock.systemDefaultZone());
    }

    /**
     * 완전한 ProductOutbox 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>reconstitute(Clock) 패턴: DB에서 조회한 엔티티 재구성 + Clock 주입</p>
     * <p>⚠️ 주의: 현재 구현은 임시입니다. 모든 필드를 받는 private 생성자가 필요합니다.</p>
     *
     * @param productId 상품 ID
     * @param eventType 이벤트 타입
     * @param payload 페이로드
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param clock 시간 제어
     * @return 재구성된 ProductOutbox
     */
    public static ProductOutbox reconstitute(ProductId productId, OutboxEventType eventType, String payload,
                                              OutboxStatus status, Integer retryCount, String errorMessage, Clock clock) {
        // TODO: 모든 필드를 받는 private 생성자 추가 필요
        ProductOutbox outbox = new ProductOutbox(productId, eventType, payload, clock);

        // 임시 구현: Reflection이나 상태 조작으로 필드 설정
        // Green Phase에서는 최소 구현으로 테스트 통과
        if (status == OutboxStatus.SENDING) {
            outbox.send();
        } else if (status == OutboxStatus.COMPLETED) {
            outbox.send();
            outbox.complete();
        } else if (status == OutboxStatus.FAILED) {
            outbox.send();
            // retryCount만큼 fail() 호출하여 retryCount 재현
            for (int i = 0; i < retryCount; i++) {
                outbox.fail(errorMessage != null ? errorMessage : "");
                if (i < retryCount - 1) {
                    // 마지막 반복이 아니면 다시 SENDING 상태로 전환
                    outbox.send();
                }
            }
        }

        return outbox;
    }

    /**
     * 새로운 ProductOutbox 생성 (레거시)
     *
     * @deprecated Use {@link #forNew(ProductId, OutboxEventType, String)} instead
     * @param productId 상품 ID
     * @param eventType 이벤트 타입 (PRODUCT_CREATED, PRODUCT_UPDATED 등)
     * @param payload 이벤트 페이로드 JSON
     * @return 새로 생성된 ProductOutbox
     */
    @Deprecated
    public static ProductOutbox create(ProductId productId, OutboxEventType eventType, String payload) {
        return forNew(productId, eventType, payload);
    }

    /**
     * Outbox를 전송 상태로 전환
     *
     * <p>상태 전환 규칙:</p>
     * <ul>
     *   <li>WAITING → SENDING</li>
     *   <li>FAILED → SENDING (재시도)</li>
     *   <li>그 외 상태에서는 IllegalStateException 발생</li>
     * </ul>
     *
     * @throws IllegalStateException WAITING 또는 FAILED 상태가 아닌 경우
     */
    public void send() {
        if (status != OutboxStatus.WAITING && status != OutboxStatus.FAILED) {
            throw new IllegalStateException("WAITING 또는 FAILED 상태에서만 전송할 수 있습니다");
        }
        this.status = OutboxStatus.SENDING;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Outbox를 완료 상태로 전환
     *
     * <p>상태 전환 규칙:</p>
     * <ul>
     *   <li>SENDING → COMPLETED</li>
     *   <li>SENDING 상태가 아닌 경우 IllegalStateException 발생</li>
     * </ul>
     *
     * @throws IllegalStateException SENDING 상태가 아닌 경우
     */
    public void complete() {
        if (status != OutboxStatus.SENDING) {
            throw new IllegalStateException("SENDING 상태에서만 완료할 수 있습니다");
        }
        this.status = OutboxStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Outbox를 실패 상태로 전환
     *
     * <p>상태 전환 규칙:</p>
     * <ul>
     *   <li>SENDING → FAILED</li>
     *   <li>SENDING 상태가 아닌 경우 IllegalStateException 발생</li>
     *   <li>에러 메시지 저장</li>
     *   <li>재시도 횟수 증가</li>
     * </ul>
     *
     * @param errorMessage 실패 원인
     * @throws IllegalStateException SENDING 상태가 아닌 경우
     */
    public void fail(String errorMessage) {
        if (status != OutboxStatus.SENDING) {
            throw new IllegalStateException("SENDING 상태에서만 실패할 수 있습니다");
        }
        this.status = OutboxStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 재시도 가능 여부 확인 (Tell Don't Ask)
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>retryCount < MAX_RETRY_COUNT (5회) 일 때만 재시도 가능</li>
     *   <li>외부에서 getRetryCount()로 판단하지 않고 객체가 스스로 판단</li>
     * </ul>
     *
     * @return 재시도 가능 여부
     */
    public boolean canRetry() {
        return retryCount < MAX_RETRY_COUNT;
    }

    // Getters (필요한 것만)
    public OutboxId getOutboxId() {
        return outboxId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public OutboxEventType getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
