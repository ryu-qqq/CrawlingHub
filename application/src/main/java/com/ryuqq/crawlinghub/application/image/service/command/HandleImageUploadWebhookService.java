package com.ryuqq.crawlinghub.application.image.service.command;

import com.ryuqq.crawlinghub.application.image.dto.command.ImageUploadWebhookCommand;
import com.ryuqq.crawlinghub.application.image.manager.command.CrawledProductImageTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.image.manager.query.ProductImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.image.port.in.command.HandleImageUploadWebhookUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import java.time.Clock;
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
 *   <li>externalDownloadId(idempotencyKey)로 Outbox 조회
 *   <li>상태에 따라 처리
 *   <li>COMPLETED: fileUrl, fileAssetId 저장 + 상태 COMPLETED
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

    private final ProductImageOutboxReadManager outboxReadManager;
    private final CrawledProductImageReadManager imageReadManager;
    private final CrawledProductImageTransactionManager imageTransactionManager;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;
    private final Clock clock;

    public HandleImageUploadWebhookService(
            ProductImageOutboxReadManager outboxReadManager,
            CrawledProductImageReadManager imageReadManager,
            CrawledProductImageTransactionManager imageTransactionManager,
            ProductImageOutboxTransactionManager outboxTransactionManager,
            Clock clock) {
        this.outboxReadManager = outboxReadManager;
        this.imageReadManager = imageReadManager;
        this.imageTransactionManager = imageTransactionManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.clock = clock;
    }

    @Override
    public void execute(ImageUploadWebhookCommand command) {
        log.info(
                "이미지 업로드 웹훅 수신: externalDownloadId={}, status={}",
                command.externalDownloadId(),
                command.status());

        ProductImageOutbox outbox =
                outboxReadManager
                        .findByIdempotencyKey(command.externalDownloadId())
                        .orElseThrow(
                                () -> {
                                    log.warn(
                                            "Outbox를 찾을 수 없음: externalDownloadId={}",
                                            command.externalDownloadId());
                                    return new IllegalArgumentException(
                                            "Outbox not found: " + command.externalDownloadId());
                                });

        if (command.isCompleted()) {
            handleCompleted(outbox, command.fileUrl(), command.fileAssetId());
        } else if (command.isFailed()) {
            handleFailed(outbox, command.errorMessage());
        } else {
            log.warn(
                    "알 수 없는 상태: externalDownloadId={}, status={}",
                    command.externalDownloadId(),
                    command.status());
        }
    }

    private void handleCompleted(ProductImageOutbox outbox, String fileUrl, String fileAssetId) {
        // Outbox 상태 갱신
        outboxTransactionManager.markAsCompleted(outbox);

        // 이미지 조회 및 업로드 완료 처리
        CrawledProductImage image =
                imageReadManager
                        .findById(outbox.getCrawledProductImageId())
                        .orElseThrow(
                                () -> {
                                    log.error(
                                            "이미지를 찾을 수 없음: outboxId={}, imageId={}",
                                            outbox.getId(),
                                            outbox.getCrawledProductImageId());
                                    return new IllegalStateException(
                                            "Image not found: "
                                                    + outbox.getCrawledProductImageId());
                                });

        // 도메인 로직 → 영속화 (비즈니스 로직은 Service에서 처리)
        image.completeUpload(fileUrl, fileAssetId, clock);
        imageTransactionManager.persist(image);

        log.info(
                "이미지 업로드 완료: outboxId={}, fileUrl={}, fileAssetId={}",
                outbox.getId(),
                fileUrl,
                fileAssetId);
    }

    private void handleFailed(ProductImageOutbox outbox, String errorMessage) {
        outboxTransactionManager.markAsFailed(outbox, errorMessage);
        log.warn("이미지 업로드 실패: outboxId={}, error={}", outbox.getId(), errorMessage);
    }
}
