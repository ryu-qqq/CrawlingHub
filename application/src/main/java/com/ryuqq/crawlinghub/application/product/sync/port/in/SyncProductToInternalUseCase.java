package com.ryuqq.crawlinghub.application.product.sync.port.in;

import com.ryuqq.crawlinghub.application.product.sync.dto.command.SyncProductCommand;
import com.ryuqq.crawlinghub.application.product.sync.dto.response.SyncResultResponse;

/**
 * 상품 내부 동기화 UseCase
 *
 * <p>크롤링된 상품 데이터를 내부 시스템으로 동기화합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface SyncProductToInternalUseCase {

    /**
     * 상품 동기화
     *
     * @param command 동기화 Command
     * @return 동기화 결과
     */
    SyncResultResponse execute(SyncProductCommand command);
}
