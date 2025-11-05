package com.ryuqq.crawlinghub.application.seller.port.in;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;

/**
 * 셀러 등록 UseCase
 *
 * <p>신규 셀러를 등록합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface RegisterSellerUseCase {

    /**
     * 셀러 등록
     *
     * @param command 등록할 셀러 정보
     * @return 등록된 셀러 정보
     */
    SellerResponse execute(RegisterSellerCommand command);
}
