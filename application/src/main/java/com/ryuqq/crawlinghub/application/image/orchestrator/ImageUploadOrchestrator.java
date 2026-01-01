package com.ryuqq.crawlinghub.application.image.orchestrator;

import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.image.factory.ImageUploadBundleFactory;
import com.ryuqq.crawlinghub.application.image.manager.command.CrawledProductImageTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 이미지 업로드 오케스트레이터
 *
 * <p>이미지 업로드 프로세스의 전체 워크플로우를 조율합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>이미지 생성 → 저장 → Outbox 생성 → 저장 → Event 등록 흐름 조율
 *   <li>중복 Outbox 필터링 로직
 *   <li>트랜잭션 커밋 후 이벤트 발행 보장
 * </ul>
 *
 * <p><strong>협력 객체</strong>:
 *
 * <ul>
 *   <li>{@link ImageUploadBundleFactory} - 도메인 객체 생성
 *   <li>{@link CrawledProductImageTransactionManager} - 이미지 저장
 *   <li>{@link ProductImageOutboxTransactionManager} - Outbox 저장
 *   <li>{@link ImageOutboxQueryPort} - 중복 Outbox 조회
 *   <li>{@link TransactionEventRegistry} - 트랜잭션 후 이벤트 등록
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 * @see ImageUploadBundleFactory Factory 패턴 (순수 생성)
 */
@Component
public class ImageUploadOrchestrator {

    private final ImageUploadBundleFactory factory;
    private final CrawledProductImageTransactionManager imageTransactionManager;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;
    private final ImageOutboxQueryPort imageOutboxQueryPort;
    private final TransactionEventRegistry eventRegistry;

    public ImageUploadOrchestrator(
            ImageUploadBundleFactory factory,
            CrawledProductImageTransactionManager imageTransactionManager,
            ProductImageOutboxTransactionManager outboxTransactionManager,
            ImageOutboxQueryPort imageOutboxQueryPort,
            TransactionEventRegistry eventRegistry) {
        this.factory = factory;
        this.imageTransactionManager = imageTransactionManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.imageOutboxQueryPort = imageOutboxQueryPort;
        this.eventRegistry = eventRegistry;
    }

    /**
     * 이미지 업로드 프로세스 실행
     *
     * <p>ImageUploadData를 받아 다음 워크플로우를 실행합니다:
     *
     * <ol>
     *   <li>이미지 생성 및 저장
     *   <li>중복 PENDING/PROCESSING Outbox 필터링
     *   <li>Outbox 생성 및 저장
     *   <li>Event 생성 및 등록 (커밋 후 발행)
     * </ol>
     *
     * @param imageUploadData 이미지 업로드 데이터 (enrichWithProductId 호출 후)
     */
    public void processImageUpload(ImageUploadData imageUploadData) {
        if (!imageUploadData.hasImages()) {
            return;
        }

        // 1. Factory로 이미지 생성 후 저장
        List<CrawledProductImage> images = factory.createImages(imageUploadData);
        List<CrawledProductImage> savedImages = imageTransactionManager.persistAll(images);

        // 2. 중복 PENDING/PROCESSING Outbox 필터링
        List<CrawledProductImage> imagesWithoutPendingOutbox =
                filterImagesWithoutPendingOutbox(savedImages);

        if (imagesWithoutPendingOutbox.isEmpty()) {
            return;
        }

        // 3. Factory로 Outbox 생성 후 저장
        List<ProductImageOutbox> outboxes =
                factory.createOutboxes(imagesWithoutPendingOutbox, imageUploadData);
        outboxTransactionManager.persistAll(outboxes);

        // 4. 필터링된 이미지 URL로 새 ImageUploadData 생성
        List<String> filteredUrls =
                imagesWithoutPendingOutbox.stream()
                        .map(CrawledProductImage::getOriginalUrl)
                        .toList();
        ImageUploadData filteredImageUploadData = imageUploadData.withFilteredUrls(filteredUrls);

        // 5. Factory로 Event 생성 후 등록 (커밋 후 발행)
        ImageUploadRequestedEvent event = factory.createEvent(filteredImageUploadData);
        eventRegistry.registerForPublish(event);
    }

    /**
     * PENDING 또는 PROCESSING 상태의 Outbox가 없는 이미지만 필터링
     *
     * @param savedImages 저장된 이미지 목록
     * @return PENDING/PROCESSING Outbox가 없는 이미지 목록
     */
    private List<CrawledProductImage> filterImagesWithoutPendingOutbox(
            List<CrawledProductImage> savedImages) {
        return savedImages.stream().filter(image -> !hasPendingOrProcessingOutbox(image)).toList();
    }

    /**
     * 이미지에 대해 PENDING 또는 PROCESSING 상태의 Outbox가 존재하는지 확인
     *
     * @param image 확인할 이미지
     * @return PENDING/PROCESSING Outbox가 있으면 true
     */
    private boolean hasPendingOrProcessingOutbox(CrawledProductImage image) {
        String idempotencyKey =
                ProductImageOutbox.generateIdempotencyKeyFromUrl(
                        image.getId(), image.getOriginalUrl());

        Optional<ProductImageOutbox> existingOutbox =
                imageOutboxQueryPort.findByIdempotencyKey(idempotencyKey);

        return existingOutbox
                .map(outbox -> outbox.isPending() || outbox.getStatus().isProcessing())
                .orElse(false);
    }
}
