package com.ryuqq.crawlinghub.application.mustit.seller.port.in;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.UpdateMustitSellerCommand;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;

/**
 * 머스트잇 셀러 수정 UseCase (Inbound Port)
 * <p>
 * 헥사고날 아키텍처의 Inbound Port로서,
 * Application Layer의 진입점을 정의합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public interface UpdateMustitSellerUseCase {

    /**
     * 기존 셀러의 정보를 수정합니다.
     * <p>
     * 활성 상태 및 크롤링 주기를 변경할 수 있습니다.
     * sellerId는 수정할 수 없습니다 (PK).
     * </p>
     *
     * @param command 셀러 수정 Command
     * @return 수정된 셀러 Aggregate
     * @throws com.ryuqq.crawlinghub.domain.mustit.seller.exception.SellerNotFoundException sellerId가 존재하지 않는 경우
     */
    MustitSeller execute(UpdateMustitSellerCommand command);
}
