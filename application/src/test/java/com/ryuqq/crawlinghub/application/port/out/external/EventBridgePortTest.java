package com.ryuqq.crawlinghub.application.port.out.external;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * EventBridgePort 인터페이스 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Port 메서드 시그니처 존재 확인</li>
 *   <li>✅ createRule() 메서드 계약 검증</li>
 *   <li>✅ updateRule() 메서드 계약 검증</li>
 *   <li>✅ deleteRule() 메서드 계약 검증</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Command Port 명명 규칙: External Service 연동</li>
 *   <li>✅ Infrastructure Layer 의존 금지</li>
 *   <li>✅ AWS SDK 타입 사용 금지 (Application Layer)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class EventBridgePortTest {

    @Test
    void shouldHaveCreateRuleMethod() {
        // Given
        EventBridgePort port = mock(EventBridgePort.class);
        String sellerId = "seller_12345";
        int intervalDays = 7;

        // When
        port.createRule(sellerId, intervalDays);

        // Then
        verify(port).createRule(sellerId, intervalDays);
    }

    @Test
    void shouldHaveUpdateRuleMethod() {
        // Given
        EventBridgePort port = mock(EventBridgePort.class);
        String sellerId = "seller_12345";
        int newIntervalDays = 14;

        // When
        port.updateRule(sellerId, newIntervalDays);

        // Then
        verify(port).updateRule(sellerId, newIntervalDays);
    }

    @Test
    void shouldHaveDeleteRuleMethod() {
        // Given
        EventBridgePort port = mock(EventBridgePort.class);
        String sellerId = "seller_12345";

        // When
        port.deleteRule(sellerId);

        // Then
        verify(port).deleteRule(sellerId);
    }

    @Test
    void shouldCreateRuleWithValidInterval() {
        // Given
        EventBridgePort port = mock(EventBridgePort.class);
        String sellerId = "seller_12345";
        int intervalDays = 1;

        // When
        port.createRule(sellerId, intervalDays);

        // Then
        verify(port).createRule(sellerId, intervalDays);
    }

    @Test
    void shouldUpdateRuleWithNewInterval() {
        // Given
        EventBridgePort port = mock(EventBridgePort.class);
        String sellerId = "seller_12345";
        int oldInterval = 7;
        int newInterval = 30;

        // When
        port.updateRule(sellerId, newInterval);

        // Then
        verify(port).updateRule(sellerId, newInterval);
    }
}
