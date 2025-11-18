package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;

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
     */
    void execute(UpdateSellerNameCommand command);
}
