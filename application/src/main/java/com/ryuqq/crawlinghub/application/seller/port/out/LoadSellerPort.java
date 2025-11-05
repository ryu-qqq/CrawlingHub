package com.ryuqq.crawlinghub.application.seller.port.out;

import com.ryuqq.crawlinghub.application.seller.dto.query.SellerQueryDto;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.util.Optional;

/**
 * 셀러 조회 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 *
 * <p><strong>CQRS 패턴 적용 - DTO 직접 반환 ⭐</strong></p>
 * <ul>
 *   <li>✅ Domain Model 대신 DTO 직접 반환 (성능 향상)</li>
 *   <li>✅ QueryDSL DTO Projection 사용</li>
 *   <li>✅ N+1 문제 방지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface LoadSellerPort {

    /**
     * ID로 셀러 조회
     *
     * <p>QueryDSL DTO Projection으로 직접 조회하여 Domain Model을 거치지 않습니다.</p>
     *
     * @param id 셀러 ID (null 불가)
     * @return 셀러 Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException id가 null인 경우
     */
    Optional<SellerQueryDto> findById(MustitSellerId id);

    /**
     * 셀러 코드로 셀러 조회
     *
     * <p>QueryDSL DTO Projection으로 직접 조회하여 Domain Model을 거치지 않습니다.</p>
     *
     * @param code 셀러 코드 (null, blank 불가)
     * @return 셀러 Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException code가 null 또는 blank인 경우
     */
    Optional<SellerQueryDto> findByCode(String code);
}
