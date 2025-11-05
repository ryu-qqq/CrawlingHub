package com.ryuqq.crawlinghub.application.mustit.seller.port.in;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.UpdateSellerStatusCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerResponse;

/**
 * 셀러 상태 변경 UseCase
 *
 * <p>셀러의 상태를 변경합니다 (ACTIVE, INACTIVE, SUSPENDED).
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface UpdateSellerStatusUseCase {

    /**
     * 셀러 상태 변경
     *
     * @param command 변경할 상태 정보
     * @return 변경된 셀러 정보
     */
    SellerResponse execute(UpdateSellerStatusCommand command);
}
