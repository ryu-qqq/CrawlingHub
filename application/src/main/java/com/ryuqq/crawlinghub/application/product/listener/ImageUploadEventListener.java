package com.ryuqq.crawlinghub.application.product.listener;

import com.ryuqq.crawlinghub.application.image.manager.ImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.image.manager.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient.ImageUploadRequest;
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
 * <p><strong>용도</strong>: 트랜잭션 커밋 후 파일서버 API 호출 및 Outbox 상태 업데이트
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후)
 *   <li>이벤트의 originalUrl로 이미지 조회
 *   <li>이미지 ID로 Outbox 조회
 *   <li>각 Outbox에 대해 상태 PROCESSING으로 변경
 *   <li>파일서버 API 호출 (비동기 업로드 요청)
 *   <li>실패 시: Outbox 상태 → FAILED (재시도 스케줄러에서 처리)
 * </ol>
 *
 * <p><strong>완료 처리</strong>: 파일서버 웹훅에서 완료 콜백 수신
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ImageUploadEventListener {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadEventListener.class);

    private final ImageOutboxReadManager imageOutboxReadManager;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;
    private final FileServerClient fileServerClient;

    public ImageUploadEventListener(
            ImageOutboxReadManager imageOutboxReadManager,
            ProductImageOutboxTransactionManager outboxTransactionManager,
            FileServerClient fileServerClient) {
        this.imageOutboxReadManager = imageOutboxReadManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.fileServerClient = fileServerClient;
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
        List<CrawledProductImage> images =
                imageOutboxReadManager.findImagesByCrawledProductId(productId);

        // URL로 이미지 매핑
        Map<String, CrawledProductImage> imageByUrl =
                images.stream()
                        .collect(
                                Collectors.toMap(
                                        CrawledProductImage::getOriginalUrl,
                                        Function.identity(),
                                        (existing, replacement) -> existing));

        int processedCount = 0;
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
                    imageOutboxReadManager.findByCrawledProductImageId(image.getId());
            if (outboxOpt.isEmpty()) {
                log.warn(
                        "Outbox를 찾을 수 없음: productId={}, imageId={}",
                        productId.value(),
                        image.getId());
                continue;
            }

            processOutbox(outboxOpt.get(), image);
            processedCount++;
        }

        log.info(
                "이미지 업로드 요청 이벤트 처리 완료: productId={}, processedCount={}",
                productId.value(),
                processedCount);
    }

    private void processOutbox(ProductImageOutbox outbox, CrawledProductImage image) {
        try {
            // 1. 상태 → PROCESSING
            outboxTransactionManager.markAsProcessing(outbox);

            // 2. 파일서버 API 호출 (callbackUrl은 Adapter에서 관리)
            ImageUploadRequest request =
                    ImageUploadRequest.of(
                            outbox.getIdempotencyKey(),
                            image.getOriginalUrl(),
                            image.getImageType().name());

            boolean success = fileServerClient.requestImageUpload(request);

            if (!success) {
                // API 호출 실패 → FAILED
                outboxTransactionManager.markAsFailed(outbox, "FileServer API 호출 실패");
                log.warn(
                        "파일서버 API 호출 실패: outboxId={}, url={}",
                        outbox.getId(),
                        image.getOriginalUrl());
            } else {
                log.debug(
                        "파일서버 API 호출 성공: outboxId={}, url={}",
                        outbox.getId(),
                        image.getOriginalUrl());
            }

        } catch (Exception e) {
            // 예외 발생 → FAILED
            outboxTransactionManager.markAsFailed(outbox, e.getMessage());
            log.error(
                    "이미지 업로드 요청 처리 중 오류: outboxId={}, url={}, error={}",
                    outbox.getId(),
                    image.getOriginalUrl(),
                    e.getMessage(),
                    e);
        }
    }
}
