package com.ryuqq.crawlinghub.application.image.factory;

import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 이미지 업로드 관련 도메인 객체 생성 Factory
 *
 * <p>순수한 Factory 패턴 구현으로, Clock을 캡슐화하여 도메인 객체 생성만 담당합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledProductImage 객체 생성
 *   <li>ProductImageOutbox 객체 생성
 *   <li>ImageUploadRequestedEvent 객체 생성
 * </ul>
 *
 * <p><strong>제한 사항</strong>:
 *
 * <ul>
 *   <li>저장/조회 로직 금지 (→ TransactionManager 책임)
 *   <li>이벤트 발행 로직 금지 (→ EventRegistry 책임)
 *   <li>비즈니스 오케스트레이션 금지 (→ Orchestrator 책임)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 * @see ImageUploadOrchestrator 오케스트레이션 담당
 */
@Component
public class ImageUploadBundleFactory {

    private final Clock clock;

    public ImageUploadBundleFactory(Clock clock) {
        this.clock = clock;
    }

    /**
     * CrawledProductImage 목록 생성
     *
     * @param imageUploadData 이미지 업로드 데이터 (enrichWithProductId 호출 후)
     * @return 생성된 CrawledProductImage 목록 (ID 없음)
     */
    public List<CrawledProductImage> createImages(ImageUploadData imageUploadData) {
        return imageUploadData.createImages(clock);
    }

    /**
     * ProductImageOutbox 목록 생성
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
     * ImageUploadRequestedEvent 생성
     *
     * @param imageUploadData 이미지 업로드 데이터 (enrichWithProductId 호출 후)
     * @return 생성된 ImageUploadRequestedEvent
     */
    public ImageUploadRequestedEvent createEvent(ImageUploadData imageUploadData) {
        return imageUploadData.createEvent(clock);
    }
}
