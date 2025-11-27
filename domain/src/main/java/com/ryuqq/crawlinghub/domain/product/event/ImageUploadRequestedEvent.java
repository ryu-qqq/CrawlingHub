package com.ryuqq.crawlinghub.domain.product.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이미지 업로드 요청 이벤트
 *
 * <p>CrawledProduct 저장 후 업로드가 필요한 이미지가 있을 때 발행됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public record ImageUploadRequestedEvent(
        CrawledProductId crawledProductId,
        List<ImageUploadTarget> targets,
        LocalDateTime occurredAt) implements DomainEvent {

    public ImageUploadRequestedEvent {
        if (crawledProductId == null) {
            throw new IllegalArgumentException("crawledProductId는 필수입니다.");
        }
        if (targets == null || targets.isEmpty()) {
            throw new IllegalArgumentException("업로드 대상이 비어있습니다.");
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }

    /**
     * 팩토리 메서드
     */
    public static ImageUploadRequestedEvent of(
            CrawledProductId crawledProductId,
            List<ImageUploadTarget> targets) {
        return new ImageUploadRequestedEvent(crawledProductId, targets, LocalDateTime.now());
    }

    /**
     * 단일 이미지 타입으로 생성
     */
    public static ImageUploadRequestedEvent ofUrls(
            CrawledProductId crawledProductId,
            List<String> imageUrls,
            ImageType imageType) {
        List<ImageUploadTarget> targets = imageUrls.stream()
                .map(url -> new ImageUploadTarget(url, imageType))
                .toList();
        return new ImageUploadRequestedEvent(crawledProductId, targets, LocalDateTime.now());
    }

    /**
     * 이미지 업로드 대상 정보
     *
     * @param originalUrl 원본 이미지 URL
     * @param imageType 이미지 타입
     */
    public record ImageUploadTarget(String originalUrl, ImageType imageType) {
        public ImageUploadTarget {
            if (originalUrl == null || originalUrl.isBlank()) {
                throw new IllegalArgumentException("originalUrl은 필수입니다.");
            }
            if (imageType == null) {
                throw new IllegalArgumentException("imageType은 필수입니다.");
            }
        }
    }
}
