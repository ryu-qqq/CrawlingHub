package com.ryuqq.crawlinghub.application.image.service.command;

import com.ryuqq.crawlinghub.application.image.dto.response.ImageUploadRetryResponse;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.image.manager.query.ProductImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.image.port.in.command.RetryImageUploadUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 이미지 업로드 재시도 Service
 *
 * <p>실패한 이미지 업로드를 재시도합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>ReadManager로 재시도 가능한 Outbox 조회
 *   <li>FileServerClient로 업로드 재요청
 *   <li>성공 시: PROCESSING 상태로 변경
 *   <li>실패 시: FAILED 상태 유지 (재시도 횟수 증가)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RetryImageUploadService implements RetryImageUploadUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryImageUploadService.class);
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 3;

    private final ProductImageOutboxReadManager outboxReadManager;
    private final CrawledProductImageReadManager imageReadManager;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;
    private final FileServerClient fileServerClient;

    public RetryImageUploadService(
            ProductImageOutboxReadManager outboxReadManager,
            CrawledProductImageReadManager imageReadManager,
            ProductImageOutboxTransactionManager outboxTransactionManager,
            FileServerClient fileServerClient) {
        this.outboxReadManager = outboxReadManager;
        this.imageReadManager = imageReadManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.fileServerClient = fileServerClient;
    }

    @Override
    public ImageUploadRetryResponse execute() {
        List<ProductImageOutbox> retryableOutboxes =
                outboxReadManager.findRetryableOutboxes(MAX_RETRY_COUNT, BATCH_SIZE);

        if (retryableOutboxes.isEmpty()) {
            return ImageUploadRetryResponse.empty();
        }

        log.info("이미지 업로드 재시도 시작: {} 건", retryableOutboxes.size());

        int succeeded = 0;
        int failed = 0;

        for (ProductImageOutbox outbox : retryableOutboxes) {
            try {
                processOutbox(outbox);
                succeeded++;
            } catch (Exception e) {
                failed++;
                log.warn("이미지 업로드 재시도 실패: outboxId={}, error={}", outbox.getId(), e.getMessage());
            }
        }

        boolean hasMore = retryableOutboxes.size() >= BATCH_SIZE;
        return ImageUploadRetryResponse.of(retryableOutboxes.size(), succeeded, failed, hasMore);
    }

    private void processOutbox(ProductImageOutbox outbox) {
        // 이미지 정보 조회
        Optional<CrawledProductImage> imageOpt =
                imageReadManager.findById(outbox.getCrawledProductImageId());
        if (imageOpt.isEmpty()) {
            log.warn(
                    "이미지를 찾을 수 없음: outboxId={}, imageId={}",
                    outbox.getId(),
                    outbox.getCrawledProductImageId());
            outboxTransactionManager.markAsFailed(outbox, "이미지를 찾을 수 없음");
            throw new RuntimeException("Image not found");
        }

        CrawledProductImage image = imageOpt.get();

        FileServerClient.ImageUploadRequest request =
                FileServerClient.ImageUploadRequest.of(
                        outbox.getIdempotencyKey(),
                        image.getOriginalUrl(),
                        image.getImageType().name());

        boolean requestSuccess = fileServerClient.requestImageUpload(request);

        if (requestSuccess) {
            outboxTransactionManager.markAsProcessing(outbox);
            log.debug("이미지 업로드 재요청 성공: outboxId={}", outbox.getId());
        } else {
            outboxTransactionManager.markAsFailed(outbox, "FileServer 요청 실패 (재시도)");
            throw new RuntimeException("FileServer 요청 실패");
        }
    }
}
