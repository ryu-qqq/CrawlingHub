package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.OutboxEventType;
import com.ryuqq.crawlinghub.domain.vo.OutboxId;
import com.ryuqq.crawlinghub.domain.vo.OutboxStatus;
import com.ryuqq.crawlinghub.domain.vo.ProductId;

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

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성
     *
     * @param productId 상품 ID
     * @param eventType 이벤트 타입
     * @param payload 이벤트 페이로드 (JSON)
     */
    private ProductOutbox(ProductId productId, OutboxEventType eventType, String payload) {
        this.outboxId = OutboxId.generate();
        this.productId = productId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.WAITING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 새로운 ProductOutbox 생성
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>초기 상태: WAITING</li>
     *   <li>초기 retryCount: 0</li>
     *   <li>errorMessage: null</li>
     * </ul>
     *
     * @param productId 상품 ID
     * @param eventType 이벤트 타입 (PRODUCT_CREATED, PRODUCT_UPDATED 등)
     * @param payload 이벤트 페이로드 JSON
     * @return 새로 생성된 ProductOutbox
     */
    public static ProductOutbox create(ProductId productId, OutboxEventType eventType, String payload) {
        return new ProductOutbox(productId, eventType, payload);
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
        this.updatedAt = LocalDateTime.now();
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
        this.updatedAt = LocalDateTime.now();
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
        this.updatedAt = LocalDateTime.now();
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
}
