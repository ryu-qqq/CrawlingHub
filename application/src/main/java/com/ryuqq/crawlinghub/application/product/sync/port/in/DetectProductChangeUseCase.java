package com.ryuqq.crawlinghub.application.product.sync.port.in;

import com.ryuqq.crawlinghub.application.product.sync.dto.command.DetectChangeCommand;
import com.ryuqq.crawlinghub.application.product.sync.dto.response.ChangeDetectionResponse;

/**
 * 상품 변경 감지 UseCase
 *
 * <p>크롤링된 상품 데이터가 이전과 비교해 변경되었는지 감지합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface DetectProductChangeUseCase {

    /**
     * 상품 변경 감지
     *
     * @param command 변경 감지 Command
     * @return 변경 감지 결과
     */
    ChangeDetectionResponse execute(DetectChangeCommand command);
}
