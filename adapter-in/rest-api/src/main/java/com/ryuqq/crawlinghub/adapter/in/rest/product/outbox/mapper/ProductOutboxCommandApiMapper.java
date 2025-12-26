package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.OutboxRetryApiResponse;
import com.ryuqq.crawlinghub.application.product.dto.command.RetryImageOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.command.RetrySyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.OutboxRetryResponse;
import org.springframework.stereotype.Component;

/**
 * ProductOutbox Command API Mapper
 *
 * <p>REST API 요청/응답과 Application Layer 간의 변환을 담당합니다.
 */
@Component
public class ProductOutboxCommandApiMapper {

    /**
     * SyncOutbox 재시도 커맨드 생성
     *
     * @param outboxId Outbox ID
     * @return RetrySyncOutboxCommand
     */
    public RetrySyncOutboxCommand toRetrySyncOutboxCommand(Long outboxId) {
        return new RetrySyncOutboxCommand(outboxId);
    }

    /**
     * ImageOutbox 재시도 커맨드 생성
     *
     * @param outboxId Outbox ID
     * @return RetryImageOutboxCommand
     */
    public RetryImageOutboxCommand toRetryImageOutboxCommand(Long outboxId) {
        return new RetryImageOutboxCommand(outboxId);
    }

    /**
     * Outbox 재시도 응답 변환
     *
     * @param response Application Layer 응답
     * @return API 응답
     */
    public OutboxRetryApiResponse toApiResponse(OutboxRetryResponse response) {
        return new OutboxRetryApiResponse(
                response.outboxId(),
                response.previousStatus(),
                response.newStatus(),
                response.message());
    }
}
