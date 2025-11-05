package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

/**
 * Seller Exception - Sealed Interface
 *
 * <p>Seller Bounded Context의 모든 예외를 묶는 Sealed 인터페이스입니다.</p>
 * <p>컴파일 타임에 허용된 예외만 정의할 수 있어 타입 안전성을 보장합니다.</p>
 *
 * <p><strong>예외 계층:</strong></p>
 * <ul>
 *   <li>SellerNotFoundException - 셀러를 찾을 수 없음 (404)</li>
 *   <li>InactiveSellerException - 셀러가 비활성 상태 (409)</li>
 *   <li>DuplicateSellerCodeException - 중복된 셀러 코드 (409)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public sealed interface SellerException extends DomainException
    permits SellerNotFoundException, InactiveSellerException, DuplicateSellerCodeException {
}

