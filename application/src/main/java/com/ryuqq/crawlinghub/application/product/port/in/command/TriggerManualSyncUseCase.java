package com.ryuqq.crawlinghub.application.product.port.in.command;

import com.ryuqq.crawlinghub.application.product.dto.command.TriggerManualSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.ManualSyncTriggerResponse;

/**
 * 수동 동기화 트리거 UseCase
 *
 * <p>CrawledProduct에 대해 수동으로 동기화를 트리거합니다.
 */
public interface TriggerManualSyncUseCase {

    /**
     * 수동 동기화 트리거 실행
     *
     * @param command 수동 동기화 커맨드
     * @return 트리거 결과
     */
    ManualSyncTriggerResponse execute(TriggerManualSyncCommand command);
}
