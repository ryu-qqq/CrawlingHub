package com.ryuqq.crawlinghub.application.product.sync.port.in;

import com.ryuqq.crawlinghub.application.product.sync.dto.command.BulkSyncCommand;
import com.ryuqq.crawlinghub.application.product.sync.dto.response.BulkSyncResponse;

/**
 * 대량 상품 동기화 UseCase
 *
 * <p>특정 조건에 맞는 상품들을 대량으로 내부 시스템에 동기화합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface BulkSyncProductsUseCase {

    /**
     * 대량 동기화 실행
     *
     * @param command 대량 동기화 Command
     * @return 대량 동기화 결과
     */
    BulkSyncResponse execute(BulkSyncCommand command);
}
