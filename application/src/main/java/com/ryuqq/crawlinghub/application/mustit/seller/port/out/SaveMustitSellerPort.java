package com.ryuqq.crawlinghub.application.mustit.seller.port.out;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;

/**
 * 머스트잇 셀러 저장 Port (Outbound Port)
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
public interface SaveMustitSellerPort {

    /**
     * 머스트잇 셀러를 저장합니다.
     * <p>
     * 새로운 셀러를 저장하거나 기존 셀러를 업데이트합니다.
     * </p>
     *
     * @param seller 저장할 셀러 Aggregate
     * @return 저장된 셀러 Aggregate
     */
    MustitSeller save(MustitSeller seller);
}
