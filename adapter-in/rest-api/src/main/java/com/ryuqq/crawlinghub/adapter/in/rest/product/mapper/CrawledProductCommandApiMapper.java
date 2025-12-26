package com.ryuqq.crawlinghub.adapter.in.rest.product.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.ManualSyncTriggerApiResponse;
import com.ryuqq.crawlinghub.application.product.dto.command.TriggerManualSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.ManualSyncTriggerResponse;
import org.springframework.stereotype.Component;

/**
 * CrawledProduct Command API Mapper
 *
 * <p>REST API 요청/응답과 Application Layer 간의 변환을 담당합니다.
 */
@Component
public class CrawledProductCommandApiMapper {

    /**
     * 수동 동기화 커맨드 생성
     *
     * @param crawledProductId CrawledProduct ID
     * @return TriggerManualSyncCommand
     */
    public TriggerManualSyncCommand toTriggerManualSyncCommand(Long crawledProductId) {
        return new TriggerManualSyncCommand(crawledProductId);
    }

    /**
     * 수동 동기화 응답 변환
     *
     * @param response Application Layer 응답
     * @return API 응답
     */
    public ManualSyncTriggerApiResponse toApiResponse(ManualSyncTriggerResponse response) {
        return new ManualSyncTriggerApiResponse(
                response.crawledProductId(),
                response.syncOutboxId(),
                response.syncType(),
                response.message());
    }
}
