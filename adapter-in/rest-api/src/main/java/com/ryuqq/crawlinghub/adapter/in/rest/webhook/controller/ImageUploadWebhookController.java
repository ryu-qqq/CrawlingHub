package com.ryuqq.crawlinghub.adapter.in.rest.webhook.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.webhook.dto.command.ImageUploadWebhookApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.webhook.mapper.WebhookApiMapper;
import com.ryuqq.crawlinghub.application.image.dto.command.ImageUploadWebhookCommand;
import com.ryuqq.crawlinghub.application.image.port.in.command.HandleImageUploadWebhookUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이미지 업로드 웹훅 Controller
 *
 * <p>Fileflow에서 이미지 업로드 완료 시 호출되는 웹훅 엔드포인트입니다.
 *
 * <p>제공하는 API:
 *
 * <ul>
 *   <li>POST /api/v1/webhook/image-upload - 이미지 업로드 완료 웹훅
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag(name = "Webhook", description = "외부 서비스 웹훅 API")
@RestController
@RequestMapping("/api/v1/webhook")
@Validated
public class ImageUploadWebhookController {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadWebhookController.class);

    private final HandleImageUploadWebhookUseCase handleImageUploadWebhookUseCase;
    private final WebhookApiMapper webhookApiMapper;

    public ImageUploadWebhookController(
            HandleImageUploadWebhookUseCase handleImageUploadWebhookUseCase,
            WebhookApiMapper webhookApiMapper) {
        this.handleImageUploadWebhookUseCase = handleImageUploadWebhookUseCase;
        this.webhookApiMapper = webhookApiMapper;
    }

    /**
     * 이미지 업로드 웹훅 수신
     *
     * <p>Fileflow에서 이미지 업로드가 완료되면 이 엔드포인트로 결과를 전송합니다.
     *
     * @param request 웹훅 요청
     * @return 처리 결과
     */
    @Operation(summary = "이미지 업로드 웹훅", description = "Fileflow에서 이미지 업로드 완료/실패 시 호출되는 웹훅 엔드포인트입니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "웹훅 처리 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Outbox를 찾을 수 없음")
    })
    @PostMapping("/image-upload")
    public ResponseEntity<ApiResponse<Void>> handleImageUploadWebhook(
            @RequestBody @Valid ImageUploadWebhookApiRequest request) {

        log.info(
                "이미지 업로드 웹훅 수신: idempotencyKey={}, eventType={}",
                request.idempotencyKey(),
                request.eventType());

        ImageUploadWebhookCommand command = webhookApiMapper.toCommand(request);
        handleImageUploadWebhookUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.ofSuccess());
    }
}
