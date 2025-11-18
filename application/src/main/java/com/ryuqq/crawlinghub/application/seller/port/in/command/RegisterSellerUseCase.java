package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;

/**
 * Seller 등록 UseCase (Command)
 *
 * <p>상태 변경을 담당하는 Inbound Port</p>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public interface RegisterSellerUseCase {

    /**
     * Seller 등록
     *
     * @param command 등록 명령
     */
    void execute(RegisterSellerCommand command);
}
