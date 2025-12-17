package com.ryuqq.crawlinghub.application.image.service.command;

import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.image.manager.ImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.image.port.in.command.CompleteImageUploadUseCase;
import com.ryuqq.crawlinghub.application.product.manager.ImageOutboxManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
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

    private final ImageOutboxReadManager imageOutboxReadManager;
    private final ImageOutboxManager imageOutboxManager;
    private final TransactionEventRegistry eventRegistry;
    private final Clock clock;

    public CompleteImageUploadService(
            ImageOutboxReadManager imageOutboxReadManager,
            ImageOutboxManager imageOutboxManager,
            TransactionEventRegistry eventRegistry,
            Clock clock) {
        this.imageOutboxReadManager = imageOutboxReadManager;
        this.imageOutboxManager = imageOutboxManager;
        this.eventRegistry = eventRegistry;
        this.clock = clock;
    }

    @Override
    public void complete(Long outboxId, String s3Url) {
        Optional<CrawledProductImageOutbox> outboxOpt = imageOutboxReadManager.findById(outboxId);
        if (outboxOpt.isEmpty()) {
            return;
        }

        CrawledProductImageOutbox outbox = outboxOpt.get();

        // Outbox 상태 갱신
        imageOutboxManager.markAsCompleted(outbox, s3Url);

        // CrawledProduct 업데이트를 위한 이벤트 등록 (커밋 후 발행)
        ImageUploadCompletedEvent event =
                ImageUploadCompletedEvent.of(
                        outbox.getCrawledProductId(), outbox.getOriginalUrl(), s3Url, clock);
        eventRegistry.registerForPublish(event);
    }
}
