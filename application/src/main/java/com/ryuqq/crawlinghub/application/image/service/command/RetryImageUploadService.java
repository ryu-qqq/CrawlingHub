package com.ryuqq.crawlinghub.application.image.service.command;

import com.ryuqq.crawlinghub.application.image.dto.command.ImageUploadRetryResult;
import com.ryuqq.crawlinghub.application.image.manager.ImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.image.port.in.command.RetryImageUploadUseCase;
import com.ryuqq.crawlinghub.application.product.manager.ImageOutboxManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import java.util.List;
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

    private final ImageOutboxReadManager imageOutboxReadManager;
    private final ImageOutboxManager imageOutboxManager;
    private final FileServerClient fileServerClient;

    public RetryImageUploadService(
            ImageOutboxReadManager imageOutboxReadManager,
            ImageOutboxManager imageOutboxManager,
            FileServerClient fileServerClient) {
        this.imageOutboxReadManager = imageOutboxReadManager;
        this.imageOutboxManager = imageOutboxManager;
        this.fileServerClient = fileServerClient;
    }

    @Override
    public ImageUploadRetryResult execute() {
        List<CrawledProductImageOutbox> retryableOutboxes =
                imageOutboxReadManager.findRetryableOutboxes(MAX_RETRY_COUNT, BATCH_SIZE);

        if (retryableOutboxes.isEmpty()) {
            return ImageUploadRetryResult.empty();
        }

        log.info("이미지 업로드 재시도 시작: {} 건", retryableOutboxes.size());

        int succeeded = 0;
        int failed = 0;

        for (CrawledProductImageOutbox outbox : retryableOutboxes) {
            try {
                processOutbox(outbox);
                succeeded++;
            } catch (Exception e) {
                failed++;
                log.warn("이미지 업로드 재시도 실패: outboxId={}, error={}", outbox.getId(), e.getMessage());
            }
        }

        boolean hasMore = retryableOutboxes.size() >= BATCH_SIZE;
        return ImageUploadRetryResult.of(retryableOutboxes.size(), succeeded, failed, hasMore);
    }

    private void processOutbox(CrawledProductImageOutbox outbox) {
        FileServerClient.ImageUploadRequest request =
                FileServerClient.ImageUploadRequest.of(
                        outbox.getIdempotencyKey(),
                        outbox.getOriginalUrl(),
                        outbox.getImageType().name());

        boolean requestSuccess = fileServerClient.requestImageUpload(request);

        if (requestSuccess) {
            imageOutboxManager.markAsProcessing(outbox);
            log.debug("이미지 업로드 재요청 성공: outboxId={}", outbox.getId());
        } else {
            imageOutboxManager.markAsFailed(outbox, "FileServer 요청 실패 (재시도)");
            throw new RuntimeException("FileServer 요청 실패");
        }
    }
}
