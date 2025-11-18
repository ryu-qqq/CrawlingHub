package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;

/**
 * Seller 이름 변경 UseCase (Command)
 *
 * <p>상태 변경을 담당하는 Inbound Port</p>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public interface UpdateSellerNameUseCase {

    /**
     * Seller 이름 변경
     *
     * @param command 이름 변경 명령
     * @return 변경된 Seller 정보
     */
    SellerResponse execute(UpdateSellerNameCommand command);
}
