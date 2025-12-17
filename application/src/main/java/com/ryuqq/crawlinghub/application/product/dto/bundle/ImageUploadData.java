package com.ryuqq.crawlinghub.application.product.dto.bundle;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

/**
 * 이미지 업로드 요청 데이터 VO
 *
 * <p>Bundle 패턴에서 이미지 업로드 관련 데이터를 캡슐화합니다. 이미지 및 Outbox 생성 책임을 내부에 가집니다.
 *
 * <p><strong>사용 흐름</strong>:
 *
 * <pre>
 * 1. Factory에서 ImageUploadData 생성 (crawledProductId는 null)
 * 2. CrawledProduct 저장 후 enrichWithProductId()로 ID 설정
 * 3. createImages()로 이미지 생성
 * 4. 이미지 저장 후 createOutboxes()로 Outbox 생성
 * 5. createEvent()로 Event 생성
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class ImageUploadData {

    private final CrawledProductId crawledProductId;
    private final List<String> imageUrls;
    private final ImageType imageType;

    private ImageUploadData(
            CrawledProductId crawledProductId, List<String> imageUrls, ImageType imageType) {
        this.crawledProductId = crawledProductId;
        this.imageUrls =
                imageUrls == null || imageUrls.isEmpty() ? List.of() : List.copyOf(imageUrls);
        this.imageType = Objects.requireNonNull(imageType, "imageType must not be null");
    }

    /**
     * 초기 생성 (CrawledProduct 저장 전, ID 없음)
     *
     * @param imageUrls 이미지 URL 목록
     * @param imageType 이미지 타입
     * @return ImageUploadData
     */
    public static ImageUploadData of(List<String> imageUrls, ImageType imageType) {
        return new ImageUploadData(null, imageUrls, imageType);
    }

    /**
     * CrawledProduct ID로 보강된 새 인스턴스 반환
     *
     * @param crawledProductId CrawledProduct ID
     * @return ID가 설정된 새 ImageUploadData
     */
    public ImageUploadData enrichWithProductId(CrawledProductId crawledProductId) {
        Objects.requireNonNull(crawledProductId, "crawledProductId must not be null");
        return new ImageUploadData(crawledProductId, this.imageUrls, this.imageType);
    }

    /**
     * CrawledProductImage 목록 생성
     *
     * <p>ID가 설정되지 않은 경우 IllegalStateException 발생
     *
     * @param clock 시간 제어
     * @return CrawledProductImage 목록
     */
    public List<CrawledProductImage> createImages(Clock clock) {
        validateProductIdSet();
        int displayOrder = 0;
        return imageUrls.stream()
                .map(
                        url -> {
                            int order = displayOrder;
                            return CrawledProductImage.forNew(
                                    crawledProductId, url, imageType, order, clock);
                        })
                .toList();
    }

    /**
     * ProductImageOutbox 목록 생성 (저장된 이미지 기반)
     *
     * <p>이미지가 저장된 후, 이미지 ID로 Outbox를 생성합니다.
     *
     * @param savedImages 저장된 이미지 목록 (ID 포함)
     * @param clock 시간 제어
     * @return ProductImageOutbox 목록
     */
    public List<ProductImageOutbox> createOutboxes(
            List<CrawledProductImage> savedImages, Clock clock) {
        return savedImages.stream()
                .map(
                        image ->
                                ProductImageOutbox.forNewWithImageId(
                                        image.getId(), image.getOriginalUrl(), clock))
                .toList();
    }

    /**
     * 이미지 업로드 요청 이벤트 생성
     *
     * <p>ID가 설정되지 않은 경우 IllegalStateException 발생
     *
     * @param clock 시간 제어
     * @return ImageUploadRequestedEvent
     */
    public ImageUploadRequestedEvent createEvent(Clock clock) {
        validateProductIdSet();
        return ImageUploadRequestedEvent.ofUrls(crawledProductId, imageUrls, imageType, clock);
    }

    private void validateProductIdSet() {
        if (crawledProductId == null) {
            throw new IllegalStateException(
                    "crawledProductId must be set via enrichWithProductId() before creating"
                            + " images or events");
        }
    }

    /**
     * 필터링된 URL로 새 인스턴스 생성
     *
     * <p>이미 저장된 URL을 제외한 새로운 URL만으로 ImageUploadData를 생성합니다.
     *
     * @param filteredUrls 필터링된 URL 목록
     * @return 필터링된 URL이 설정된 새 ImageUploadData
     */
    public ImageUploadData withFilteredUrls(List<String> filteredUrls) {
        return new ImageUploadData(this.crawledProductId, filteredUrls, this.imageType);
    }

    /**
     * 업로드할 이미지가 있는지 확인
     *
     * @return 이미지가 있으면 true
     */
    public boolean hasImages() {
        return !imageUrls.isEmpty();
    }

    public CrawledProductId crawledProductId() {
        return crawledProductId;
    }

    public List<String> imageUrls() {
        return imageUrls;
    }

    public ImageType imageType() {
        return imageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageUploadData that = (ImageUploadData) o;
        return Objects.equals(crawledProductId, that.crawledProductId)
                && Objects.equals(imageUrls, that.imageUrls)
                && imageType == that.imageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(crawledProductId, imageUrls, imageType);
    }

    @Override
    public String toString() {
        return "ImageUploadData{"
                + "crawledProductId="
                + crawledProductId
                + ", imageUrlsCount="
                + imageUrls.size()
                + ", imageType="
                + imageType
                + '}';
    }
}
