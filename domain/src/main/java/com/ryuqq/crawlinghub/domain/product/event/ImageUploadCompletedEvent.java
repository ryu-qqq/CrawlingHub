package com.ryuqq.crawlinghub.domain.product.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.time.Instant;

/**
 * 이미지 업로드 완료 이벤트
 *
 * <p>이미지 파일서버 업로드가 완료되었을 때 발행됩니다.
 *
 * <p><strong>순환 의존성 해결</strong>: 이 이벤트를 통해 image 모듈에서 product 모듈로의 직접 의존성을 제거합니다.
 * CompleteImageUploadService에서 이벤트를 발행하면, product 패키지의 EventListener에서 수신하여 CrawledProduct를
 * 업데이트합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public record ImageUploadCompletedEvent(
        CrawledProductId crawledProductId, String originalUrl, String s3Url, Instant occurredAt)
        implements DomainEvent {

    public ImageUploadCompletedEvent {
        if (crawledProductId == null) {
            throw new IllegalArgumentException("crawledProductId는 필수입니다.");
        }
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("originalUrl은 필수입니다.");
        }
        if (s3Url == null || s3Url.isBlank()) {
            throw new IllegalArgumentException("s3Url은 필수입니다.");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt은 필수입니다.");
        }
    }

    /**
     * 팩토리 메서드
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 이미지 URL
     * @param s3Url 업로드된 S3 URL
     * @param now 현재 시각
     * @return 이벤트 인스턴스
     */
    public static ImageUploadCompletedEvent of(
            CrawledProductId crawledProductId, String originalUrl, String s3Url, Instant now) {
        return new ImageUploadCompletedEvent(crawledProductId, originalUrl, s3Url, now);
    }
}
