package com.ryuqq.crawlinghub.domain.common;

import java.util.Map;

/**
 * Domain Exception - Sealed Abstract Class
 *
 * <p>모든 도메인 예외의 루트 Sealed 추상 클래스입니다.</p>
 * <p>RuntimeException을 상속하여 @ExceptionHandler 호환성을 보장합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ RuntimeException 상속으로 @ExceptionHandler 호환성 확보</li>
 *   <li>✅ Sealed Classes로 타입 안전성 보장 (컴파일 타임 검증)</li>
 *   <li>✅ Bounded Context별 예외 계층 분리 (SellerException, ScheduleException, UserAgentException)</li>
 *   <li>✅ ErrorCode 인터페이스를 통한 일관된 에러 정보 제공</li>
 * </ul>
 *
 * <p><strong>Sealed Classes 장점:</strong></p>
 * <ul>
 *   <li>✅ 허용된 예외만 상속 가능 (컴파일 타임 검증)</li>
 *   <li>✅ Switch Expression에서 Exhaustive Checking (모든 케이스 처리 강제)</li>
 *   <li>✅ 타입 안전성 향상</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public sealed abstract class DomainException extends RuntimeException
    permits com.ryuqq.crawlinghub.domain.seller.exception.SellerException,
            com.ryuqq.crawlinghub.domain.schedule.exception.ScheduleException,
            com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentException {

    /**
     * DomainException 생성자
     *
     * @param message 에러 메시지
     */
    protected DomainException(String message) {
        super(message);
    }

    /**
     * DomainException 생성자 (원인 포함)
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드 (예: "SELLER-001", "SCHEDULE-001")
     */
    public abstract String code();

    /**
     * 에러 메시지 템플릿 파라미터 반환
     *
     * @return 파라미터 맵 (선택적)
     */
    public abstract Map<String, Object> args();

    /**
     * 에러 메시지 반환
     * <p>RuntimeException의 getMessage()를 사용하므로 별도 구현 불필요</p>
     *
     * @return 에러 메시지
     */
    public String message() {
        return getMessage();
    }
}
