package com.ryuqq.crawlinghub.application.product.listener;

import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.messaging.ProductImageMessageManager;
import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.image.manager.query.ProductImageOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent.ImageUploadTarget;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 이미지 업로드 요청 이벤트 리스너
 *
 * <p><strong>용도</strong>: 트랜잭션 커밋 후 SQS로 메시지 발행
 *
 * <p><strong>Transactional Outbox 패턴</strong>:
 *
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후)
 *   <li>Outbox 조회
 *   <li>SQS로 메시지 발행 시도
 *   <li>성공 시: SENT 상태로 전환
 *   <li>실패 시: PENDING 유지 → 스케줄러에서 재처리
 * </ol>
 *
 * <p><strong>실패 복구</strong>: 스케줄러가 PENDING/FAILED 상태 Outbox를 주기적으로 재처리
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ImageUploadEventListener {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadEventListener.class);

    private final CrawledProductImageReadManager imageReadManager;
    private final ProductImageOutboxReadManager outboxReadManager;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;
    private final ProductImageMessageManager messageManager;

    public ImageUploadEventListener(
            CrawledProductImageReadManager imageReadManager,
            ProductImageOutboxReadManager outboxReadManager,
            ProductImageOutboxTransactionManager outboxTransactionManager,
            ProductImageMessageManager messageManager) {
        this.imageReadManager = imageReadManager;
        this.outboxReadManager = outboxReadManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.messageManager = messageManager;
    }

    /**
     * 이미지 업로드 요청 이벤트 처리
     *
     * <p>트랜잭션 커밋 후 실행됩니다.
     *
     * @param event 이미지 업로드 요청 이벤트
     */
    @EventListener
    public void handleImageUploadRequested(ImageUploadRequestedEvent event) {
        CrawledProductId productId = event.crawledProductId();
        List<ImageUploadTarget> targets = event.targets();

        log.info(
                "이미지 업로드 요청 이벤트 수신: productId={}, targetCount={}",
                productId.value(),
                targets.size());

        // 이벤트의 originalUrl 목록 추출
        List<String> originalUrls = targets.stream().map(ImageUploadTarget::originalUrl).toList();

        // 이미지 조회 (originalUrl → CrawledProductImage)
        List<CrawledProductImage> images = imageReadManager.findByCrawledProductId(productId);

        // URL로 이미지 매핑
        Map<String, CrawledProductImage> imageByUrl =
                images.stream()
                        .collect(
                                Collectors.toMap(
                                        CrawledProductImage::getOriginalUrl,
                                        Function.identity(),
                                        (existing, replacement) -> existing));

        int publishedCount = 0;
        int failedCount = 0;

        for (String originalUrl : originalUrls) {
            CrawledProductImage image = imageByUrl.get(originalUrl);
            if (image == null) {
                log.warn(
                        "이미지를 찾을 수 없음: productId={}, originalUrl={}",
                        productId.value(),
                        originalUrl);
                continue;
            }

            // 이미지 ID로 Outbox 조회
            Optional<ProductImageOutbox> outboxOpt =
                    outboxReadManager.findByCrawledProductImageId(image.getId());
            if (outboxOpt.isEmpty()) {
                log.warn(
                        "Outbox를 찾을 수 없음: productId={}, imageId={}",
                        productId.value(),
                        image.getId());
                continue;
            }

            boolean success = publishToSqs(outboxOpt.get());
            if (success) {
                publishedCount++;
            } else {
                failedCount++;
            }
        }

        log.info(
                "이미지 업로드 이벤트 처리 완료: productId={}, published={}, failed={} (스케줄러에서 재처리)",
                productId.value(),
                publishedCount,
                failedCount);
    }

    /**
     * SQS로 메시지 발행 및 상태 전환
     *
     * @param outbox 발행할 Outbox
     * @return 발행 성공 여부
     */
    private boolean publishToSqs(ProductImageOutbox outbox) {
        try {
            // SQS 발행
            messageManager.publish(outbox);

            // 발행 성공 → SENT 상태로 전환
            outboxTransactionManager.markAsSent(outbox);

            log.debug(
                    "SQS 발행 성공 → SENT: outboxId={}, imageId={}",
                    outbox.getId(),
                    outbox.getCrawledProductImageId());

            return true;

        } catch (Exception e) {
            // 발행 실패 → PENDING 유지 (스케줄러에서 재처리)
            log.warn(
                    "SQS 발행 실패 (스케줄러에서 재처리): outboxId={}, imageId={}, error={}",
                    outbox.getId(),
                    outbox.getCrawledProductImageId(),
                    e.getMessage());

            return false;
        }
    }
}
