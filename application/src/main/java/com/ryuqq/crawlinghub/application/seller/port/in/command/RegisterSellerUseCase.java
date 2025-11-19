package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;

/**
 * 셀러 등록 입력 Port.
 */
public interface RegisterSellerUseCase {

    /**
     * 셀러 등록을 수행한다.
     *
     * @param command 등록 명령
     * @return 등록된 셀러 응답
     */
    SellerResponse register(RegisterSellerCommand command);
}

