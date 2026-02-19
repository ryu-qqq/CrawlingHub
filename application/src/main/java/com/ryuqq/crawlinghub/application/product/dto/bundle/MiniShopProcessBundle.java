package com.ryuqq.crawlinghub.application.product.dto.bundle;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * MINI_SHOP 처리 Bundle
 *
 * <p>MiniShopCrawlData와 ImageUploadData를 함께 묶어 처리하는 Bundle입니다. Facade에서 Bundle 단위로 처리하여 비즈니스 로직을
 * 캡슐화합니다.
 *
 * <p><strong>사용 흐름</strong>:
 *
 * <pre>
 * 1. Factory에서 MiniShopProcessBundle 생성 (crawledProductId 없음)
 * 2. Facade에서 CrawledProduct 생성/업데이트 후 enrichWithProductId() 호출
 * 3. Bundle에서 이미지 생성 → 저장 → Outbox 생성
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public record MiniShopProcessBundle(MiniShopCrawlData crawlData, ImageUploadData imageUploadData) {

    public MiniShopProcessBundle {
        Objects.requireNonNull(crawlData, "crawlData must not be null");
        Objects.requireNonNull(imageUploadData, "imageUploadData must not be null");
    }

    /**
     * CrawledProduct ID로 보강된 새 Bundle 반환
     *
     * @param crawledProductId CrawledProduct ID
     * @return ID가 설정된 새 Bundle
     */
    public MiniShopProcessBundle enrichWithProductId(CrawledProductId crawledProductId) {
        ImageUploadData enrichedImageData = imageUploadData.enrichWithProductId(crawledProductId);
        return new MiniShopProcessBundle(this.crawlData, enrichedImageData);
    }

    /**
     * CrawledProductImage 목록 생성
     *
     * @param now 현재 시각
     * @return CrawledProductImage 목록 (이미지가 없으면 빈 리스트)
     */
    public List<CrawledProductImage> createImages(Instant now) {
        if (!hasImageUpload()) {
            return List.of();
        }
        return imageUploadData.createImages(now);
    }

    /**
     * ProductImageOutbox 목록 생성 (저장된 이미지 기반)
     *
     * @param savedImages 저장된 이미지 목록 (ID 포함)
     * @param now 현재 시각
     * @return ProductImageOutbox 목록 (이미지가 없으면 빈 리스트)
     */
    public List<ProductImageOutbox> createOutboxes(
            List<CrawledProductImage> savedImages, Instant now) {
        if (savedImages == null || savedImages.isEmpty()) {
            return List.of();
        }
        return imageUploadData.createOutboxes(savedImages, now);
    }

    /**
     * 이미지 업로드 요청 이벤트 생성
     *
     * @param now 현재 시각
     * @return ImageUploadRequestedEvent (이미지가 없으면 Optional.empty())
     */
    public Optional<ImageUploadRequestedEvent> createEvent(Instant now) {
        if (!hasImageUpload()) {
            return Optional.empty();
        }
        return Optional.of(imageUploadData.createEvent(now));
    }

    /**
     * 이미지 업로드가 필요한지 확인
     *
     * @return 업로드할 이미지가 있으면 true
     */
    public boolean hasImageUpload() {
        return imageUploadData.hasImages();
    }

    /**
     * 썸네일 이미지 URL 목록 반환
     *
     * @return 이미지 URL 목록 (없으면 빈 리스트)
     */
    public List<String> getImageUrls() {
        return imageUploadData.imageUrls();
    }

    /**
     * 필터링된 이미지 URL로 새 Bundle 생성
     *
     * <p>이미 저장된 URL을 제외한 새로운 URL만으로 Bundle을 생성합니다.
     *
     * @param filteredUrls 필터링된 URL 목록
     * @return 필터링된 URL이 설정된 새 Bundle
     */
    public MiniShopProcessBundle withFilteredImageUrls(List<String> filteredUrls) {
        ImageUploadData filteredImageData = imageUploadData.withFilteredUrls(filteredUrls);
        return new MiniShopProcessBundle(this.crawlData, filteredImageData);
    }
}
