package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;

/**
 * Update Seller UseCase (Command)
 *
 * <p>셀러 수정을 담당하는 Inbound Port
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UpdateSellerUseCase {

    /**
     * 셀러 수정
     *
     * @param command 수정 명령
     */
    void execute(UpdateSellerCommand command);
}
