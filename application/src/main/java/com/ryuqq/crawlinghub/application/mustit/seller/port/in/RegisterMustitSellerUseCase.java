package com.ryuqq.crawlinghub.application.mustit.seller.port.in;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;

/**
 * 머스트잇 셀러 등록 UseCase (Inbound Port)
 * <p>
 * 헥사고날 아키텍처의 Inbound Port로서,
 * Application Layer의 진입점을 정의합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public interface RegisterMustitSellerUseCase {

    /**
     * 새로운 셀러를 등록합니다.
     *
     * @param command 셀러 등록 Command
     * @return 등록된 셀러 Aggregate
     */
    com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller execute(RegisterMustitSellerCommand command);
}
