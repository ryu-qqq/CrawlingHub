package com.ryuqq.crawlinghub.domain.common;

import com.ryuqq.crawlinghub.domain.seller.exception.SellerException;
import com.ryuqq.crawlinghub.domain.schedule.exception.ScheduleException;

import java.util.Map;

/**
 * Domain Exception - Sealed Interface
 *
 * <p>모든 도메인 예외의 루트 인터페이스입니다.</p>
 * <p>Sealed로 허용된 예외 계층만 정의할 수 있어 타입 안전성을 보장합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Sealed Interface로 허용된 예외만 정의</li>
 *   <li>✅ Bounded Context별 예외 계층 분리 (SellerException, ScheduleException)</li>
 *   <li>✅ ErrorCode 인터페이스를 통한 일관된 에러 정보 제공</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public sealed interface DomainException
    permits SellerException, ScheduleException {

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드 (예: "SELLER-001", "SCHEDULE-001")
     */
    String code();

    /**
     * 에러 메시지 반환
     *
     * @return 에러 메시지
     */
    String message();

    /**
     * 에러 메시지 템플릿 파라미터 반환
     *
     * @return 파라미터 맵 (선택적)
     */
    Map<String, Object> args();
}
