package com.ryuqq.crawlinghub.application.image.service.command;

import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.image.manager.command.CrawledProductImageTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.image.manager.query.ProductImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.image.port.in.command.CompleteImageUploadUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadCompletedEvent;
import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * 이미지 업로드 완료 처리 Service
 *
 * <p>이미지 파일서버 업로드가 완료되면 Outbox 상태를 갱신하고 이벤트를 발행합니다.
 *
 * <p><strong>순환 의존성 해결</strong>: CrawledProduct 업데이트는 이벤트를 통해 처리합니다. ImageUploadCompletedEvent를
 * 발행하면, product 패키지의 EventListener에서 수신하여 CrawledProduct를 업데이트합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class CompleteImageUploadService implements CompleteImageUploadUseCase {

    private final ProductImageOutboxReadManager outboxReadManager;
    private final CrawledProductImageReadManager imageReadManager;
    private final CrawledProductImageTransactionManager imageTransactionManager;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;
    private final TransactionEventRegistry eventRegistry;
    private final Clock clock;

    public CompleteImageUploadService(
            ProductImageOutboxReadManager outboxReadManager,
            CrawledProductImageReadManager imageReadManager,
            CrawledProductImageTransactionManager imageTransactionManager,
            ProductImageOutboxTransactionManager outboxTransactionManager,
            TransactionEventRegistry eventRegistry,
            Clock clock) {
        this.outboxReadManager = outboxReadManager;
        this.imageReadManager = imageReadManager;
        this.imageTransactionManager = imageTransactionManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.eventRegistry = eventRegistry;
        this.clock = clock;
    }

    @Override
    public void complete(Long outboxId, String s3Url) {
        complete(outboxId, s3Url, null);
    }

    @Override
    public void complete(Long outboxId, String s3Url, String fileAssetId) {
        Optional<ProductImageOutbox> outboxOpt = outboxReadManager.findById(outboxId);
        if (outboxOpt.isEmpty()) {
            return;
        }

        ProductImageOutbox outbox = outboxOpt.get();

        // 이미지 조회 및 S3 URL 업데이트
        Optional<CrawledProductImage> imageOpt =
                imageReadManager.findById(outbox.getCrawledProductImageId());
        if (imageOpt.isEmpty()) {
            return;
        }

        CrawledProductImage image = imageOpt.get();

        // Outbox 상태 갱신
        outboxTransactionManager.markAsCompleted(outbox);

        // 이미지 업로드 완료 처리
        imageTransactionManager.completeUpload(image, s3Url, fileAssetId);

        // CrawledProduct 업데이트를 위한 이벤트 등록 (커밋 후 발행)
        ImageUploadCompletedEvent event =
                ImageUploadCompletedEvent.of(
                        image.getCrawledProductId(), image.getOriginalUrl(), s3Url, clock);
        eventRegistry.registerForPublish(event);
    }
}
