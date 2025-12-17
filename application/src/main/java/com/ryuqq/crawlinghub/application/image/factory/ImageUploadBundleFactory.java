package com.ryuqq.crawlinghub.application.image.factory;

import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.image.manager.CrawledProductImageTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 이미지 업로드 Bundle 처리 Factory
 *
 * <p>Clock을 캡슐화하여 이미지, Outbox, Event 생성 로직을 담당합니다. Facade에서 Clock 의존성을 제거하고 이미지 처리 로직을 일관되게 관리합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledProductImage 생성 및 저장
 *   <li>ProductImageOutbox 생성 및 저장
 *   <li>ImageUploadRequestedEvent 생성 및 등록
 * </ul>
 *
 * <p><strong>사용 흐름</strong>:
 *
 * <pre>
 * 1. Facade에서 Bundle.enrichWithProductId() 호출
 * 2. Factory.processImageUpload(bundle.imageUploadData()) 호출
 * 3. Factory 내부에서 이미지/Outbox/Event 처리
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ImageUploadBundleFactory {

    private final CrawledProductImageTransactionManager imageTransactionManager;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;
    private final TransactionEventRegistry eventRegistry;
    private final Clock clock;

    public ImageUploadBundleFactory(
            CrawledProductImageTransactionManager imageTransactionManager,
            ProductImageOutboxTransactionManager outboxTransactionManager,
            TransactionEventRegistry eventRegistry,
            Clock clock) {
        this.imageTransactionManager = imageTransactionManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.eventRegistry = eventRegistry;
        this.clock = clock;
    }

    /**
     * 이미지 업로드 처리
     *
     * <p>ImageUploadData를 받아 이미지 생성/저장, Outbox 생성/저장, Event 등록을 수행합니다.
     *
     * @param imageUploadData 이미지 업로드 데이터 (enrichWithProductId 호출 후)
     */
    public void processImageUpload(ImageUploadData imageUploadData) {
        if (!imageUploadData.hasImages()) {
            return;
        }

        // 1. 이미지 생성 및 저장
        List<CrawledProductImage> images = imageUploadData.createImages(clock);
        List<CrawledProductImage> savedImages = imageTransactionManager.saveAll(images);

        // 2. Outbox 생성 및 저장 (저장된 이미지 ID 사용)
        List<ProductImageOutbox> outboxes = imageUploadData.createOutboxes(savedImages, clock);
        outboxTransactionManager.persistAll(outboxes);

        // 3. Event 생성 및 등록 (커밋 후 발행)
        ImageUploadRequestedEvent event = imageUploadData.createEvent(clock);
        eventRegistry.registerForPublish(event);
    }

    /**
     * 이미지 생성 (저장 없이)
     *
     * <p>테스트나 특수한 경우에 이미지 객체만 생성할 때 사용합니다.
     *
     * @param imageUploadData 이미지 업로드 데이터
     * @return 생성된 CrawledProductImage 목록
     */
    public List<CrawledProductImage> createImages(ImageUploadData imageUploadData) {
        return imageUploadData.createImages(clock);
    }

    /**
     * Outbox 생성 (저장 없이)
     *
     * <p>테스트나 특수한 경우에 Outbox 객체만 생성할 때 사용합니다.
     *
     * @param savedImages 저장된 이미지 목록 (ID 포함)
     * @param imageUploadData 이미지 업로드 데이터
     * @return 생성된 ProductImageOutbox 목록
     */
    public List<ProductImageOutbox> createOutboxes(
            List<CrawledProductImage> savedImages, ImageUploadData imageUploadData) {
        return imageUploadData.createOutboxes(savedImages, clock);
    }

    /**
     * Event 생성 (등록 없이)
     *
     * <p>테스트나 특수한 경우에 Event 객체만 생성할 때 사용합니다.
     *
     * @param imageUploadData 이미지 업로드 데이터
     * @return 생성된 ImageUploadRequestedEvent
     */
    public ImageUploadRequestedEvent createEvent(ImageUploadData imageUploadData) {
        return imageUploadData.createEvent(clock);
    }
}
