package com.ryuqq.crawlinghub.adapter.in.rest.webhook.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.webhook.dto.command.ImageUploadWebhookApiRequest;
import com.ryuqq.crawlinghub.application.image.dto.command.ImageUploadWebhookCommand;
import org.springframework.stereotype.Component;

/**
 * Webhook API Mapper
 *
 * <p>웹훅 API DTO와 Application Command 간의 매핑을 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class WebhookApiMapper {

    /**
     * API Request를 Application Command로 변환
     *
     * @param request API Request
     * @return Application Command
     */
    public ImageUploadWebhookCommand toCommand(ImageUploadWebhookApiRequest request) {
        return new ImageUploadWebhookCommand(
                request.externalDownloadId(),
                request.status(),
                request.fileUrl(),
                request.fileAssetId(),
                request.errorMessage(),
                request.completedAt());
    }
}
