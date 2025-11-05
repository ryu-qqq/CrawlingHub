package com.ryuqq.crawlinghub.application.seller.port.out;

import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.util.Optional;

/**
 * 셀러 조회 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface LoadSellerPort {

    /**
     * ID로 셀러 조회
     *
     * @param id 셀러 ID (null 불가)
     * @return 셀러 (없으면 Optional.empty())
     * @throws IllegalArgumentException id가 null인 경우
     */
    Optional<MustitSeller> findById(MustitSellerId id);

    /**
     * 셀러 코드로 셀러 조회
     *
     * @param code 셀러 코드 (null, blank 불가)
     * @return 셀러 (없으면 Optional.empty())
     * @throws IllegalArgumentException code가 null 또는 blank인 경우
     */
    Optional<MustitSeller> findByCode(String code);
}
