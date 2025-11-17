package com.ryuqq.crawlinghub.application.port.out.command;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * Seller Persistence Port (Command)
 *
 * <p>Domain Aggregate를 영속화하는 쓰기 전용 Port</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ PersistencePort 명명 규칙: *PersistencePort</li>
 *   <li>✅ persist() 메서드 하나만 제공</li>
 *   <li>✅ Domain Aggregate 파라미터, Value Object 반환</li>
 *   <li>✅ save/update/delete 메서드 금지</li>
 *   <li>✅ 조회 메서드 금지 (QueryPort로 분리)</li>
 * </ul>
 *
 * <p><strong>구현체 위치:</strong> persistence-mysql/adapter/command</p>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public interface SellerPersistencePort {

    /**
     * Seller 저장 (신규 생성 또는 수정)
     *
     * <p>신규 생성 (ID 없음) → INSERT</p>
     * <p>기존 수정 (ID 있음) → UPDATE (JPA 더티체킹)</p>
     *
     * @param seller 저장할 Seller (Domain Aggregate)
     * @return 저장된 Seller의 ID (Value Object)
     */
    SellerId persist(Seller seller);
}
