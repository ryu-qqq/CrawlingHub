package com.ryuqq.crawlinghub.application.mustit.seller.port.out;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;

/**
 * 셀러 저장 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface SaveSellerPort {

    /**
     * 셀러 저장 (신규 생성 또는 수정)
     *
     * @param seller 저장할 셀러 (null 불가)
     * @return 저장된 셀러 (ID 포함)
     * @throws IllegalArgumentException seller가 null인 경우
     */
    MustitSeller save(MustitSeller seller);
}
