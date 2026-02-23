package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;

/**
 * Register Seller UseCase (Command)
 *
 * <p>셀러 등록을 담당하는 Inbound Port
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RegisterSellerUseCase {

    /**
     * 셀러 등록
     *
     * @param command 등록 명령
     * @return 등록된 셀러 ID
     */
    long execute(RegisterSellerCommand command);
}
