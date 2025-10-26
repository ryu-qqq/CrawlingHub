package com.ryuqq.crawlinghub.application.mustit.seller.port.out;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;

import java.util.Optional;

/**
 * 머스트잇 셀러 조회 Port (Outbound Port)
 * <p>
 * 헥사고날 아키텍처의 Outbound Port로서,
 * Application Layer가 Persistence Layer에 의존하지 않도록
 * 의존성을 역전시킵니다.
 * </p>
 * <p>
 * 이 인터페이스는 Application Layer에 정의되며,
 * Adapter-Persistence Layer에서 구현됩니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public interface LoadMustitSellerPort {

    /**
     * sellerId로 셀러를 조회합니다.
     *
     * @param sellerId 조회할 셀러 ID
     * @return 조회된 셀러 (존재하지 않으면 Optional.empty())
     */
    Optional<MustitSeller> findBySellerId(String sellerId);

    /**
     * sellerId로 셀러가 존재하는지 확인합니다.
     *
     * @param sellerId 확인할 셀러 ID
     * @return 존재 여부
     */
    boolean existsBySellerId(String sellerId);
}
