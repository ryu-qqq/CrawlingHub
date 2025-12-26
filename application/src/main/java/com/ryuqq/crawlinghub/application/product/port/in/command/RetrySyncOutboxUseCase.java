package com.ryuqq.crawlinghub.application.product.port.in.command;

import com.ryuqq.crawlinghub.application.product.dto.command.RetrySyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.OutboxRetryResponse;

/**
 * SyncOutbox 수동 재시도 UseCase
 *
 * <p>FAILED 상태의 SyncOutbox를 수동으로 재시도합니다.
 */
public interface RetrySyncOutboxUseCase {

    /**
     * SyncOutbox 수동 재시도 실행
     *
     * @param command 재시도 커맨드
     * @return 재시도 결과
     */
    OutboxRetryResponse execute(RetrySyncOutboxCommand command);
}
