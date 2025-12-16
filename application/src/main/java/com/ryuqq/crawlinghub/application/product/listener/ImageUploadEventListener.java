package com.ryuqq.crawlinghub.application.product.listener;

import com.ryuqq.crawlinghub.application.image.manager.ImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.manager.ImageOutboxManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient.ImageUploadRequest;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent.ImageUploadTarget;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 이미지 업로드 요청 이벤트 리스너
 *
 * <p><strong>용도</strong>: 트랜잭션 커밋 후 파일서버 API 호출 및 Outbox 상태 업데이트
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후)
 *   <li>이벤트의 targets로 Outbox 배치 조회
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
    private final ImageOutboxManager imageOutboxManager;
    private final FileServerClient fileServerClient;

    public ImageUploadEventListener(
            ImageOutboxReadManager imageOutboxReadManager,
            ImageOutboxManager imageOutboxManager,
            FileServerClient fileServerClient) {
        this.imageOutboxReadManager = imageOutboxReadManager;
        this.imageOutboxManager = imageOutboxManager;
        this.fileServerClient = fileServerClient;
    }

    /**
     * 이미지 업로드 요청 이벤트 처리
     *
     * <p>트랜잭션 커밋 후 실행됩니다.
     *
     * @param event 이미지 업로드 요청 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleImageUploadRequested(ImageUploadRequestedEvent event) {
        CrawledProductId productId = event.crawledProductId();
        List<ImageUploadTarget> targets = event.targets();

        log.info(
                "이미지 업로드 요청 이벤트 수신: productId={}, targetCount={}",
                productId.value(),
                targets.size());

        // 이벤트의 originalUrl 목록 추출
        List<String> originalUrls = targets.stream().map(ImageUploadTarget::originalUrl).toList();

        // Outbox 배치 조회 (N+1 방지)
        List<CrawledProductImageOutbox> outboxes =
                imageOutboxReadManager.findByCrawledProductIdAndOriginalUrls(
                        productId, originalUrls);

        // 각 Outbox에 대해 파일서버 API 호출
        for (CrawledProductImageOutbox outbox : outboxes) {
            processOutbox(outbox);
        }

        log.info(
                "이미지 업로드 요청 이벤트 처리 완료: productId={}, processedCount={}",
                productId.value(),
                outboxes.size());
    }

    private void processOutbox(CrawledProductImageOutbox outbox) {
        try {
            // 1. 상태 → PROCESSING
            imageOutboxManager.markAsProcessing(outbox);

            // 2. 파일서버 API 호출 (callbackUrl은 Adapter에서 관리)
            ImageUploadRequest request =
                    ImageUploadRequest.of(
                            outbox.getIdempotencyKey(),
                            outbox.getOriginalUrl(),
                            outbox.getImageType().name());

            boolean success = fileServerClient.requestImageUpload(request);

            if (!success) {
                // API 호출 실패 → FAILED
                imageOutboxManager.markAsFailed(outbox, "FileServer API 호출 실패");
                log.warn(
                        "파일서버 API 호출 실패: outboxId={}, url={}",
                        outbox.getId(),
                        outbox.getOriginalUrl());
            } else {
                log.debug(
                        "파일서버 API 호출 성공: outboxId={}, url={}",
                        outbox.getId(),
                        outbox.getOriginalUrl());
            }

        } catch (Exception e) {
            // 예외 발생 → FAILED
            imageOutboxManager.markAsFailed(outbox, e.getMessage());
            log.error(
                    "이미지 업로드 요청 처리 중 오류: outboxId={}, url={}, error={}",
                    outbox.getId(),
                    outbox.getOriginalUrl(),
                    e.getMessage(),
                    e);
        }
    }
}
