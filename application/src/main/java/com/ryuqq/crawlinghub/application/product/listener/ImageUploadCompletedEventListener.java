package com.ryuqq.crawlinghub.application.product.listener;

import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadCompletedEvent;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이미지 업로드 완료 이벤트 리스너
 *
 * <p>ImageUploadCompletedEvent를 수신하여 CrawledProduct의 이미지 URL을 S3 URL로 업데이트합니다.
 *
 * <p><strong>순환 의존성 해결</strong>: image 모듈에서 발행한 이벤트를 product 모듈에서 수신하여 처리합니다. 이를 통해 image → product
 * 직접 의존성을 제거하고 이벤트 기반 디커플링을 달성합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ImageUploadCompletedEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(ImageUploadCompletedEventListener.class);

    private final CrawledProductReadManager crawledProductReadManager;
    private final CrawledProductTransactionManager crawledProductTransactionManager;

    public ImageUploadCompletedEventListener(
            CrawledProductReadManager crawledProductReadManager,
            CrawledProductTransactionManager crawledProductTransactionManager) {
        this.crawledProductReadManager = crawledProductReadManager;
        this.crawledProductTransactionManager = crawledProductTransactionManager;
    }

    /**
     * 이미지 업로드 완료 이벤트 처리
     *
     * <p>CrawledProduct의 이미지 URL을 원본 URL에서 S3 URL로 교체합니다.
     *
     * @param event 이미지 업로드 완료 이벤트
     */
    @EventListener
    @Transactional
    public void handleImageUploadCompleted(ImageUploadCompletedEvent event) {
        Optional<CrawledProduct> productOpt =
                crawledProductReadManager.findById(event.crawledProductId());

        if (productOpt.isEmpty()) {
            log.warn(
                    "CrawledProduct not found for ImageUploadCompletedEvent: crawledProductId={}",
                    event.crawledProductId().value());
            return;
        }

        CrawledProduct product = productOpt.get();
        crawledProductTransactionManager.markImageAsUploaded(
                product, event.originalUrl(), event.s3Url());

        log.debug(
                "CrawledProduct image updated: crawledProductId={}, originalUrl={}, s3Url={}",
                event.crawledProductId().value(),
                event.originalUrl(),
                event.s3Url());
    }
}
