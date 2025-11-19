package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.ChangeSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;

/**
 * 셀러 상태 변경 UseCase Port.
 */
public interface ChangeSellerStatusUseCase {

    SellerResponse changeStatus(ChangeSellerStatusCommand command);
}

