package com.ryuqq.crawlinghub.application.image.service.command;

import com.ryuqq.crawlinghub.application.image.dto.command.ImageUploadWebhookCommand;
import com.ryuqq.crawlinghub.application.image.manager.ImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.image.port.in.command.HandleImageUploadWebhookUseCase;
import com.ryuqq.crawlinghub.application.product.manager.ImageOutboxManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 이미지 업로드 웹훅 처리 Service
 *
 * <p>Fileflow에서 이미지 업로드 완료/실패 시 웹훅을 수신하여 Outbox 상태를 업데이트합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>idempotencyKey로 Outbox 조회
 *   <li>이벤트 타입에 따라 상태 업데이트
 *   <li>COMPLETED: s3Url 저장 + 상태 COMPLETED
 *   <li>FAILED: errorMessage 저장 + 상태 FAILED
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class HandleImageUploadWebhookService implements HandleImageUploadWebhookUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(HandleImageUploadWebhookService.class);

    private final ImageOutboxReadManager imageOutboxReadManager;
    private final ImageOutboxManager imageOutboxManager;

    public HandleImageUploadWebhookService(
            ImageOutboxReadManager imageOutboxReadManager, ImageOutboxManager imageOutboxManager) {
        this.imageOutboxReadManager = imageOutboxReadManager;
        this.imageOutboxManager = imageOutboxManager;
    }

    @Override
    public void execute(ImageUploadWebhookCommand command) {
        log.info(
                "이미지 업로드 웹훅 수신: idempotencyKey={}, eventType={}",
                command.idempotencyKey(),
                command.eventType());

        CrawledProductImageOutbox outbox =
                imageOutboxReadManager
                        .findByIdempotencyKey(command.idempotencyKey())
                        .orElseThrow(
                                () -> {
                                    log.warn(
                                            "Outbox를 찾을 수 없음: idempotencyKey={}",
                                            command.idempotencyKey());
                                    return new IllegalArgumentException(
                                            "Outbox not found: " + command.idempotencyKey());
                                });

        if (command.isCompleted()) {
            handleCompleted(outbox, command.s3Url());
        } else if (command.isFailed()) {
            handleFailed(outbox, command.errorMessage());
        } else {
            log.warn(
                    "알 수 없는 이벤트 타입: idempotencyKey={}, eventType={}",
                    command.idempotencyKey(),
                    command.eventType());
        }
    }

    private void handleCompleted(CrawledProductImageOutbox outbox, String s3Url) {
        imageOutboxManager.markAsCompleted(outbox, s3Url);
        log.info("이미지 업로드 완료: outboxId={}, s3Url={}", outbox.getId(), s3Url);
    }

    private void handleFailed(CrawledProductImageOutbox outbox, String errorMessage) {
        imageOutboxManager.markAsFailed(outbox, errorMessage);
        log.warn("이미지 업로드 실패: outboxId={}, error={}", outbox.getId(), errorMessage);
    }
}
